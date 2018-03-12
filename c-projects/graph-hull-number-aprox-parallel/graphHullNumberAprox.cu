#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <string>
#include <sstream>
#include <dirent.h>
#include <unistd.h>
#include <fstream>
#include <sys/stat.h>
#include <sys/types.h>
#include <vector>
#include <math.h> 
#include <string.h>
#include <cuda_runtime.h>
#include <cuda.h>
#include <time.h>
#include <limits.h>
#include "nvgraph.h"

#define CHARACTER_INIT_COMMENT '#'

#define BLOCK_SIZE_OPTIMAL 256
#define BLOCK_FACTOR_OPTIMAL 2

#define DEFAULT_THREAD_PER_BLOCK 28
#define DEFAULT_BLOCK 256 
#define MAX_DEFAULT_SIZE_QUEUE 256

#define WARP_SIZE 32
#define SINCLUDED 4
#define PROCESSED 3
#define INCLUDED 2
#define NEIGHBOOR_COUNT_INCLUDED 1
/* */
#define MAX(x, y) (((x) > (y)) ? (x) : (y))
#define MIN(x, y) (((x) < (y)) ? (x) : (y))
#define COPY_ARRAY(SRC,DST,LEN) { memcpy((DST), (SRC), LEN); }

#define verboseKernel false

bool verbose = false;
bool graphByThread = false;
bool graphByBlock = false;
bool graphByBlockOptimal = false;
bool verticesBythread = false;
bool serial = false;
bool printResults = true;

struct graphCsr {
    int *data;
    //    data[0] nVertices;
    //    data[0] nEdges
    //    data[2] *csrColIdxs;
    //    data[nVertices+3] *csrRowOffset;
};

__host__ __device__
int addVertToS(int vert, unsigned char* aux, int *graphData) {
    int countIncluded = 0;
    int nvertices = graphData[0];
    int *csrColIdxs = &graphData[2];
    int *csrRowOffset = &graphData[nvertices + 3];

    if (aux[vert] >= INCLUDED) {
        return countIncluded;
    }
    int headQueue = vert;
    int tailQueue = vert;
    aux[vert] = INCLUDED;

    while (headQueue <= tailQueue) {
        int verti = headQueue;
        if (verti >= nvertices || aux[verti] != INCLUDED) {
            headQueue++;
            continue;
        }
        int end = csrColIdxs[verti + 1];
        for (int j = csrColIdxs[verti]; j < end; j++) {
            int vertn = csrRowOffset[j];
            if (vertn >= nvertices) continue;
            if (vertn != verti && aux[vertn] < INCLUDED) {
                aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                if (aux[vertn] == INCLUDED) {
                    headQueue = MIN(headQueue, vertn);
                    tailQueue = MAX(tailQueue, vertn);
                }
            }
        }
        aux[verti] = PROCESSED;
        countIncluded++;
    }
    aux[vert] = SINCLUDED;
    return countIncluded;
}

__host__ __device__
int exapandHullSetFromV(int v, int nvertices, unsigned char *aux, unsigned char *auxb, int *graphData, int idx) {
    for (int j = 0; j < nvertices; j++) {
        aux[j] = 0;
    }
    if (verboseKernel) printf("thread-%d: cleanded aux\n", idx);
    if (verboseKernel) printf("thread-%d: add Vert %d to S\n", idx, v);

    int sizeHs = addVertToS(v, aux, graphData);
    int sSize = 1;
    int bv;
    do {
        bv = -1;
        int maiorGrau = 0;
        int maiorDeltaHs = 0;
        for (int i = 0; i < nvertices; i++) {
            if (aux[i] >= INCLUDED) {
                continue;
            }
            COPY_ARRAY(aux, auxb, nvertices);
            int deltaHsi = addVertToS(i, auxb, graphData);

            int neighborCount = 0;
            for (int j = 0; j < nvertices; j++) {
                if (auxb[j] == INCLUDED) {
                    neighborCount++;
                }
            }

            if (bv == -1 || (deltaHsi >= maiorDeltaHs && neighborCount > maiorGrau)) {
                maiorDeltaHs = deltaHsi;
                maiorGrau = neighborCount;
                bv = i;
            }
        }
        if (verboseKernel) printf("thread-%d: add Vert %d to S\n", idx, bv);
        sizeHs = sizeHs + addVertToS(bv, aux, graphData);
        sSize++;
    } while (sizeHs < nvertices);
    return sSize;
}

__global__
void kernelAproxHullNumberGraphByBlock(int* graphsGpu, int* dataGraphs, int* results) {
    //void kernelAproxHullNumber(graphCsr *graphs, int* results) {
    int offset = blockIdx.x * blockDim.x;
    int idx = threadIdx.x;
    if (verboseKernel) printf("thread-%d\n", idx);

    int start = graphsGpu[offset];
    if (verboseKernel) printf("thread-%d Graph-start: %d\n", idx, start);

    int *graphData = &dataGraphs[start];
    int nvertices = graphData[0];

    if (idx == 0) {
        results[offset] = nvertices;
    }
    __syncthreads();

    if (verboseKernel) printf("thread-%d Graph-nvertices: %d\n", idx, nvertices);
    if (verboseKernel) printf("thread-%d Graph-edges: %d\n", idx, graphData[1]);


    if (idx < nvertices) {
        unsigned char *aux = new unsigned char [nvertices * 2];
        unsigned char *auxb = &aux[nvertices];
        int minHullSet = nvertices;
        int v = idx;

        int sSize = exapandHullSetFromV(v, nvertices, aux, auxb, graphData, idx);

        minHullSet = MIN(minHullSet, sSize);

        if (verboseKernel) printf("thread-%d: minHullSet %d\n", idx, minHullSet);

        free(aux);
        //    free(auxb);
        if (verboseKernel) printf("thread-%d: memory freed\n", idx);
        atomicMin(&results[offset], minHullSet);
    }
}

__global__
void kernelAproxHullNumberByVertex(int offset, int* graphsGpu,
        int* dataGraphs, int* results) {
    //void kernelAproxHullNumber(graphCsr *graphs, int* results) {
    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    if (verboseKernel)
        printf("thread-%d\n", idx);

    int start = graphsGpu[offset];
    if (verboseKernel)
        printf("thread-%d Graph-start: %d\n", idx, start);

    int *graphData = &dataGraphs[start];
    int nvertices = graphData[0];

    if (idx == 0) {
        results[offset] = nvertices;
    }
    __syncthreads();

    if (idx < nvertices) {
        if (verboseKernel)
            printf("thread-%d Graph-nvertices: %d\n", idx, nvertices);
        if (verboseKernel)
            printf("thread-%d Graph-edges: %d\n", idx, graphData[1]);

        unsigned char *aux = new unsigned char [nvertices * 2];
        unsigned char *auxb = &aux[nvertices];
        int minHullSet = nvertices;
        int v = idx;

        int sSize = exapandHullSetFromV(v, nvertices, aux, auxb, graphData, idx);
        minHullSet = MIN(minHullSet, sSize);

        if (verboseKernel)
            printf("thread-%d: minHullSet %d\n", idx, minHullSet);
        if (verboseKernel)
            printf("thread-%d: memory freed\n", idx);
        atomicMin(&results[offset], minHullSet);
        free(aux);
    }
}

__global__
void kernelAproxHullNumberGraphByThread(int* graphsGpu, int* dataGraphs, int* results) {
    //void kernelAproxHullNumber(graphCsr *graphs, int* results) {
    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    if (verboseKernel)
        printf("thread-%d\n", idx);

    int start = graphsGpu[idx];
    if (verboseKernel)
        printf("thread-%d Graph-start: %d\n", idx, start);

    int *graphData = &dataGraphs[start];
    int nvertices = graphData[0];

    if (verboseKernel)
        printf("thread-%d Graph-nvertices: %d\n", idx, nvertices);
    if (verboseKernel)
        printf("thread-%d Graph-edges: %d\n", idx, graphData[1]);

    unsigned char *aux = new unsigned char [nvertices * 2];
    //    unsigned char *auxb = new unsigned char [nvertices];
    unsigned char *auxb = &aux[nvertices];

    int minHullSet = nvertices;

    for (int v = 0; v < nvertices; v++) {
        for (int j = 0; j < nvertices; j++) {
            aux[j] = 0;
        }
        if (verboseKernel) printf("thread-%d: cleanded aux\n", idx);
        if (verboseKernel) printf("thread-%d: add Vert %d to S\n", idx, v);

        int sizeHs = addVertToS(v, aux, graphData);
        int sSize = 1;
        int bv;
        do {
            bv = -1;
            int maiorGrau = 0;
            int maiorDeltaHs = 0;
            for (int i = 0; i < nvertices; i++) {
                if (aux[i] >= INCLUDED) {
                    continue;
                }
                COPY_ARRAY(aux, auxb, nvertices);
                int deltaHsi = addVertToS(i, auxb, graphData);

                int neighborCount = 0;
                for (int j = 0; j < nvertices; j++) {
                    if (auxb[j] == INCLUDED) {
                        neighborCount++;
                    }
                }

                if (bv == -1 || (deltaHsi >= maiorDeltaHs && neighborCount > maiorGrau)) {
                    maiorDeltaHs = deltaHsi;
                    maiorGrau = neighborCount;
                    bv = i;
                }
            }
            if (verboseKernel) printf("thread-%d: add Vert %d to S\n", idx, bv);
            sizeHs = sizeHs + addVertToS(bv, aux, graphData);
            sSize++;
        } while (sizeHs < nvertices);
        minHullSet = MIN(minHullSet, sSize);
        if (verboseKernel) printf("thread-%d: minHullSet %d\n", idx, minHullSet);
    }
    free(aux);
    //    free(auxb);
    if (verboseKernel) printf("thread-%d: memory freed\n", idx);
    results[idx] = minHullSet;
}

int parallelAproxHullNumberGraphs(graphCsr *graphs, int cont) {
    if (verbose) printf("ParallelAproxHullNumberGraphs\n");
    int numbytesDataGraph = 0;
    cudaError_t r;
    for (int i = 0; i < cont; i++) {
        int size = (graphs[i].data[0] + 3 + graphs[i].data[1]) * sizeof (int);
        numbytesDataGraph = numbytesDataGraph + size;
    }

    if (verbose) printf("Cuda Malloc Graph\n");

    int device;
    cudaDeviceProp deviceProp;
    cudaGetDevice(&device);
    cudaGetDeviceProperties(&deviceProp, device);

    if ((deviceProp.concurrentKernels == 0)) {
        perror("not support for concurrent kernel\n");
    }
    if (verbose)
        printf("Compute SM %d.%d with %d multi-processors\n", deviceProp.major, deviceProp.minor, deviceProp.multiProcessorCount);

    int* dataGraphsGpu;
    int *graphsGpu;
    int* resultGpu;
    int* graphsHost = new int[cont];
    cudaMalloc((void**) &resultGpu, cont * sizeof (int));
    cudaMalloc((void**) &dataGraphsGpu, numbytesDataGraph);
    cudaMalloc((void**) &graphsGpu, cont * sizeof (int));
    r = cudaMemcpy(graphsGpu, graphs, cont * sizeof (int), cudaMemcpyHostToDevice);
    int offset = 0;

    //if (verbose) printf("Cuda Atrib Graph\n");
    for (int i = 0; i < cont; i++) {
        //if (verbose) printf("Cuda Atrib Graph-%d\n", i);
        graphCsr *graph = &graphs[i];
        int sizeGraph = graph->data[0] + 3 + graph->data[1];
        int nbytes = sizeGraph * sizeof (int);
        r = cudaMemcpy(dataGraphsGpu + offset, graph->data, nbytes, cudaMemcpyHostToDevice);
        if (r != cudaSuccess) {
            fprintf(stderr, "Failed to copy memory 1 \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }
        graphsHost[i] = offset;
        offset = offset + sizeGraph;
    }

    r = cudaMemcpy(graphsGpu, graphsHost, cont * sizeof (int), cudaMemcpyHostToDevice);
    if (r != cudaSuccess) {
        fprintf(stderr, "Failed to copy memory 2 \nError: %s\n", cudaGetErrorString(r));
        exit(EXIT_FAILURE);
    }

    cudaStream_t *streams = (cudaStream_t *) malloc(cont * sizeof (cudaStream_t));
    for (int i = 0; i < cont; i++) {
        cudaStreamCreate(&(streams[i]));
    }

    if (verbose) printf("Launch Kernel\n");

    int* resultLocal = new int[cont];

    cudaEvent_t start, stop;
    cudaEventCreate(&start);
    cudaEventCreate(&stop);

    /* Graphs by Thread */
    if (graphByThread) {
        if (verbose) printf("Lanch Kernel One graph by thread\n");

        cudaEventRecord(start);

        kernelAproxHullNumberGraphByThread << <1, cont>>>(graphsGpu, dataGraphsGpu, resultGpu);
        r = cudaDeviceSynchronize();

        cudaEventRecord(stop);
        cudaEventSynchronize(stop);
        float milliseconds = 0;
        cudaEventElapsedTime(&milliseconds, start, stop);
        printf("Time for the kernel(one graph by thread): %2.f ms\n", milliseconds);

        if (verbose) printf("Read Result\n");

        if (r != cudaSuccess) {
            fprintf(stderr, "Failed cudaDeviceSynchronize \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }

        r = cudaMemcpy(resultLocal, resultGpu, sizeof (int)*cont, cudaMemcpyDeviceToHost);
        if (r != cudaSuccess) {
            fprintf(stderr, "Failed to copy memory 4 \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }

        if (printResults)
            for (int i = 0; i < cont; i++) {
                printf("MinHullNumberAprox-Thread Graph-%d: %d\n", i, resultLocal[i]);
            }
    }

    /* Graphs by Block */
    if (graphByBlock) {
        if (verbose)
            printf("Lauch Kernel One graph by Block Optimal\n");
        cudaEventRecord(start);

        int maxvertice = 0;
        for (int i = 0; i < cont; i++) {
            graphCsr *graph = &graphs[i];
            int nvertice = graph->data[0];
            if (nvertice > maxvertice) {
                maxvertice = nvertice;
            }
        }

        kernelAproxHullNumberGraphByBlock << <cont, maxvertice>>>(graphsGpu, dataGraphsGpu, resultGpu);

        //        r = cudaDeviceSynchronize();

        cudaEventRecord(stop);
        cudaEventSynchronize(stop);

        float milliseconds = 0;
        cudaEventElapsedTime(&milliseconds, start, stop);
        printf("Time for the (one graph by block): %2.f ms\n", milliseconds);

        if (verbose)
            printf("Read Result\n");


        if (r != cudaSuccess) {
            fprintf(stderr, "Failed cudaDeviceSynchronize \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }


        r = cudaMemcpy(resultLocal, resultGpu, sizeof (int)*cont, cudaMemcpyDeviceToHost);
        if (r != cudaSuccess) {
            fprintf(stderr, "Failed to copy memory 4 \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }

        if (printResults)
            for (int i = 0; i < cont; i++) {
                printf("MinHullNumberAprox-Block Graph-%d: %d\n", i, resultLocal[i]);
            }
    }

    /* Graph by Grid and Vertices by Thread */
    if (verticesBythread) {
        if (verbose)
            printf("Vertices by Thread One graph by kernel\n");
        cudaEventRecord(start);

        for (int i = 0; i < cont; i++) {
            graphCsr *graph = &graphs[i];
            int nvertice = graph->data[0];
            int nblocks = nvertice / DEFAULT_THREAD_PER_BLOCK;
            if ((nvertice % DEFAULT_THREAD_PER_BLOCK) > 0) {
                nblocks++;
            }
            //            int nthreads = MIN(DEFAULT_THREAD_PER_BLOCK, nvertice);
            int nthreads = DEFAULT_THREAD_PER_BLOCK;
            kernelAproxHullNumberByVertex << <nblocks, nthreads, 0, streams[i]>>>(i, graphsGpu, dataGraphsGpu, resultGpu);
        }
        //        r = cudaDeviceSynchronize();

        cudaEventRecord(stop);
        cudaEventSynchronize(stop);

        float milliseconds = 0;
        cudaEventElapsedTime(&milliseconds, start, stop);
        printf("Time for Vt/Thread kernel(one graph by kernel): %2.f ms\n", milliseconds);

        if (verbose)
            printf("Read Result\n");


        if (r != cudaSuccess) {
            fprintf(stderr, "Failed cudaDeviceSynchronize \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }


        r = cudaMemcpy(resultLocal, resultGpu, sizeof (int)*cont, cudaMemcpyDeviceToHost);
        if (r != cudaSuccess) {
            fprintf(stderr, "Failed to copy memory 4 \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }

        if (printResults)
            for (int i = 0; i < cont; i++) {
                printf("MinHullNumberAprox-Vertices Thread-%d: %d\n", i, resultLocal[i]);
            }
    }

    /* Graphs by Block Optimal */
    if (graphByBlockOptimal) {
        if (verbose)
            printf("Lauch Kernel graph by Block optimal\n");
        cudaEventRecord(start);

        int maxvertice = 0;
        int minvertice = INT_MAX;
        int totalvertices = 0;

        for (int i = 0; i < cont; i++) {
            graphCsr *graph = &graphs[i];
            int nvertice = graph->data[0];
            if (nvertice > maxvertice) {
                maxvertice = nvertice;
            }
            if(nvertice < minvertice){
                minvertice = nvertice;
            }
            totalvertices = totalvertices + nvertice;
        }

        //https://devtalk.nvidia.com/default/topic/1026825/how-to-choose-how-many-threads-blocks-to-have-/
        //Therefore very small block sizes (e.g. 32 threads per block) may limit performance due to occupancy. 
        //Very large block sizes for example 1024 threads per block, may also limit performance, if there are resource limits 
        //(e.g. registers per thread usage, or shared memory usage) which prevent 2 threadblocks (in this example of 1024 threads per block) from being resident on a SM
        //Threadblock size choices in the range of 128 - 512 are less likely to run into the aforementioned issues


        int numblocks = totalvertices / BLOCK_SIZE_OPTIMAL;
        if ((totalvertices % BLOCK_SIZE_OPTIMAL) > 0) {
            numblocks++;
        }
        int nthreads = BLOCK_SIZE_OPTIMAL / BLOCK_FACTOR_OPTIMAL;
        
        int maxgraphsbyblock  = BLOCK_SIZE_OPTIMAL/minvertice;
        if ((BLOCK_SIZE_OPTIMAL%minvertice;L) > 0) {
            maxgraphsbyblock++;
        }
        
        //kernelAproxHullNumberGraphByBlock <<<numblocks, nthreads,maxgraphsbyblock*sizeof(int)>>>(graphsGpu, dataGraphsGpu, resultGpu,minvertice,maxvertice,totalvertices,maxgraphsbyblock);
        //        r = cudaDeviceSynchronize();


        cudaEventRecord(stop);
        cudaEventSynchronize(stop);

        float milliseconds = 0;
        cudaEventElapsedTime(&milliseconds, start, stop);
        printf("Time for the kernel(graph by block optimal): %2.f ms\n", milliseconds);

        if (verbose)
            printf("Read Result\n");


        if (r != cudaSuccess) {
            fprintf(stderr, "Failed cudaDeviceSynchronize \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }


        r = cudaMemcpy(resultLocal, resultGpu, sizeof (int)*cont, cudaMemcpyDeviceToHost);
        if (r != cudaSuccess) {
            fprintf(stderr, "Failed to copy memory 4 \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }

        if (printResults)
            for (int i = 0; i < cont; i++) {
                printf("MinHullNumberAprox-Block Graph  optimal-%d: %d\n", i, resultLocal[i]);
            }
    }



    r = cudaGetLastError();
    if (r != cudaSuccess) {
        fprintf(stderr, "Failed in kernelAproxHullNumber \nError: %s\n", cudaGetErrorString(r));
        exit(EXIT_FAILURE);
    }


    free(streams);
    free(resultLocal);
    free(graphsHost);
    cudaFree(resultGpu);
    cudaFree(graphsGpu);
    cudaFree(dataGraphsGpu);
}

int serialAproxHullNumber(int *graphData) {
    int nvertices = graphData[0];
    //    printf("Nvertices: %d\n", nvertices);
    unsigned char *aux = (unsigned char *) malloc(sizeof (unsigned char)*nvertices);
    unsigned char *auxb = (unsigned char *) malloc(sizeof (unsigned char)*nvertices);
    if (aux == NULL || auxb == NULL) perror("Not memory allocation");
    int minHullSet = nvertices;

    for (int v = 0; v < nvertices; v++) {
        for (int j = 0; j < nvertices; j++) {
            aux[j] = 0;
        }

        int sizeHs = addVertToS(v, aux, graphData);
        int sSize = 1;
        int bv;
        do {
            bv = -1;
            int maiorGrau = 0;
            int maiorDeltaHs = 0;
            for (int i = 0; i < nvertices; i++) {
                if (aux[i] >= INCLUDED) {
                    continue;
                }
                COPY_ARRAY(aux, auxb, nvertices);
                int deltaHsi = addVertToS(i, auxb, graphData);

                int neighborCount = 0;
                for (int j = 0; j < nvertices; j++) {
                    if (auxb[j] == INCLUDED) {
                        neighborCount++;
                    }
                }

                if (bv == -1 || (deltaHsi >= maiorDeltaHs && neighborCount > maiorGrau)) {
                    maiorDeltaHs = deltaHsi;
                    maiorGrau = neighborCount;
                    bv = i;
                }
            }
            sizeHs = sizeHs + addVertToS(bv, aux, graphData);
            sSize++;
        } while (sizeHs < nvertices);
        minHullSet = MIN(minHullSet, sSize);
    }
    free(aux);
    free(auxb);
    return minHullSet;
}

void serialAproxHullNumberGraphs(graphCsr *graphs, int cont) {
    if (verbose) printf("Serial execution \n");
    int *results = new int[cont];
    clock_t begin_serial_time, end_serial_time;
    begin_serial_time = clock();
    for (int i = 0; i < cont; i++) {
        results[i] = serialAproxHullNumber(graphs[i].data);
        //        printf("MinHullNumberAprox-Serial Graph-%d: %d\n", i, results[i]);
    }
    end_serial_time = clock();

    double diff = end_serial_time - begin_serial_time;
    diff = diff / (CLOCKS_PER_SEC / 1000);

    printf("Time for the serial: %2.f ms\n", diff);


    if (printResults)
        for (int i = 0; i < cont; i++) {
            printf("MinHullNumberAprox-Serial Graph-%d: %d\n", i, results[i]);
        }

    free(results);
}

void processFiles(int argc, char** argv) {
    graphCsr* graphs = (graphCsr*) malloc((sizeof (graphCsr)) * argc);
    std::vector<int> values;

    int contGraph = 0;
    int verticesMedian = 0;

    for (int x = 1; x < argc; x++) {
        char* strFile = argv[x];
        DIR *dpdf;
        struct dirent *epdf;
        struct stat filestat;

        if (strFile[0] == '-') {
            continue;
        }

        dpdf = opendir(strFile);
        std::string filepath = std::string(strFile);

        while (dpdf && (epdf = readdir(dpdf))) {
            filepath = std::string(strFile) + "/" + epdf->d_name;
            if (epdf->d_name == "." || epdf->d_name == "..")
                continue;
            if (stat(filepath.c_str(), &filestat))
                continue;
            if (S_ISDIR(filestat.st_mode))
                continue;
            else break;
        }
        closedir(dpdf);

        std::string line, strCArray, strRArray;
        std::ifstream infile(filepath.c_str());

        if (infile) {
            while (getline(infile, line)) {
                if (line.at(0) != CHARACTER_INIT_COMMENT && line.at(1) != CHARACTER_INIT_COMMENT) {
                    if (strCArray.empty()) {
                        strCArray = line;
                    } else if (strRArray.empty()) {
                        strRArray = line;
                    }
                }
            }
            infile.close();
        } else {
            continue;
        }

        if (strCArray.empty() || strRArray.empty()) {
            perror("Invalid file format");
            continue;
        }

        std::stringstream stream(strCArray.c_str());
        values.clear();

        int n;
        while (stream >> n) {
            values.push_back(n);
        }
        strCArray.clear();

        int numVertices = values.size() - 1;
        stream.str("");

        std::stringstream stream2(strRArray);
        while (stream2 >> n) {
            values.push_back(n);
        }
        stream2.str("");
        strRArray.clear();

        int numedges = values.size() - (numVertices + 1);
        values.insert(values.begin(), numedges);
        values.insert(values.begin(), numVertices);
        int *data = new int[values.size()];
        std::copy(values.begin(), values.end(), data);

        values.clear();

        if (numVertices < 1) {
            continue;
        }

        graphCsr* graph = &graphs[contGraph];
        graph->data = data;
        contGraph++;
        verticesMedian = verticesMedian + numVertices;
    }

    if (contGraph > 0)
        printf("Processing: %d graphs  %dv/g (avg) \n", contGraph, verticesMedian / contGraph);

    if (graphByBlock || graphByThread || verticesBythread)
        parallelAproxHullNumberGraphs(graphs, contGraph);

    if (serial)
        serialAproxHullNumberGraphs(graphs, contGraph);

    for (int i = 0; i < contGraph; i++) {
        free(graphs[i].data);
    }
    free(graphs);
}

void runTest() {
    //    int numVertices = 10;
    //    int colIdx[] = {0, 3, 6, 9, 12, 15, 18, 21, 24, 27, 30};
    //    int sizeRowOffset = numVertices + 1;
    //    int rowOffset[] = {2, 5, 6, 3, 4, 6, 0, 4, 7, 1, 5, 7, 1, 2, 9, 0, 3, 9, 0, 1, 8, 2, 3, 8, 6, 7, 9, 4, 5, 8};

    printf("graphHullNumberAprox -[svtbnx] dir\n");

    int data[] = {10, 30,
        0, 3, 6, 9, 12, 15, 18, 21, 24, 27, 30,
        2, 5, 6, 3, 4, 6, 0, 4, 7, 1, 5, 7, 1, 2, 9, 0, 3, 9, 0, 1, 8, 2, 3, 8, 6, 7, 9, 4, 5, 8};
    graphCsr* graph = (graphCsr*) malloc(sizeof (graphCsr));
    graph->data = data;
    parallelAproxHullNumberGraphs(graph, 1);
}

int main(int argc, char** argv) {
    printf("Main\n");

    long opt = 0;

    while ((opt = getopt(argc, argv, "svtbnxo")) != -1) {
        switch (opt) {
            case 't':
                graphByThread = true;
                break;
            case 'b':
                graphByBlock = true;
                break;
            case 's':
                serial = true;
                break;
            case 'v':
                verbose = true;
                break;
            case 'x':
                verticesBythread = true;
                break;
            case 'o':
                graphByBlockOptimal = true;
                break;
            case 'n':
                printResults = false;
                break;
            case '?':
                printf("Unknow option: %c", char(opt));
                break;
        }
    }

    if (argc > 1) {
        processFiles(argc, argv);
    } else {
        runTest();
    }
    return 0;
}
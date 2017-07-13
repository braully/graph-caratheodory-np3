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
#include "nvgraph.h"

#define CHARACTER_INIT_COMMENT '#'

#define DEFAULT_THREAD_PER_BLOCK 256
#define DEFAULT_BLOCK 256 
#define MAX_DEFAULT_SIZE_QUEUE 256

#define SINCLUDED 4
#define PROCESSED 3
#define INCLUDED 2
#define NEIGHBOOR_COUNT_INCLUDED 1
/* */
#define MAX(x, y) (((x) > (y)) ? (x) : (y))
#define MIN(x, y) (((x) < (y)) ? (x) : (y))
#define COPY_ARRAY(SRC,DST,LEN) { memcpy((DST), (SRC), LEN); }

#define verboseSerial false

typedef struct nvgraphCSRTopology32I_st graphCsr;

__host__ __device__
int addVertToS(int vert, unsigned char* aux, graphCsr *graph) {
    int countIncluded = 0;
    int nvertices = graph->nvertices;

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
        int end = graph->destination_indices[verti + 1];
        for (int j = graph->destination_indices[verti]; j < end; j++) {
            int vertn = graph->source_offsets[j];
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

__global__
void kernelSerialAproxHullNumber(graphCsr &graphs, int* results) {
    //void kernelSerialAproxHullNumber(graphCsr *graphs, int* results) {
    int idx = blockIdx.x * blockDim.x + threadIdx.x;
    graphCsr *graph = &graphs[idx];
    int nvertices = graph->nvertices;
    unsigned char *aux = new unsigned char [nvertices];
    unsigned char *auxb = new unsigned char [nvertices];
    int minHullSet = nvertices;

    for (int v = 0; v < nvertices; v++) {
        for (int j = 0; j < nvertices; j++) {
            aux[j] = 0;
        }

        int sizeHs = addVertToS(v, aux, graph);
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
                int deltaHsi = addVertToS(i, auxb, graph);

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
            sizeHs = sizeHs + addVertToS(bv, aux, graph);
            sSize++;
        } while (sizeHs < nvertices);
        minHullSet = MIN(minHullSet, sSize);
    }
    free(aux);
    free(auxb);
    results[idx] = minHullSet;
}

int serialAproxHullNumberGraphs(graphCsr *graphs, int cont) {
    printf("serialAproxHullNumberGraphs\n");
    int numbytesDataGraph = 0;
    cudaError_t r;
    for (int i = 0; i < cont; i++) {
        int size = (graphs[i].nvertices + 1) * sizeof (int);
        size = size + graphs[i].nedges * sizeof (int);
        numbytesDataGraph = numbytesDataGraph + size;
    }

    printf("Cuda Malloc Graph\n");

    int* dataGraphsGpu;
    graphCsr *graphsGpu;
    int* resultGpu;
    cudaMalloc((void**) &resultGpu, cont * sizeof (int));
    cudaMalloc((void**) &dataGraphsGpu, cont * sizeof (graphCsr) + numbytesDataGraph);
    r = cudaMemcpy(graphsGpu, graphs, cont * sizeof (graphCsr), cudaMemcpyHostToDevice);

    int offset = cont * sizeof (graphCsr);

    printf("Cuda Atrib Graph\n");
    for (int i = 0; i < cont; i++) {
        graphCsr *graph = &graphs[i];
        graphCsr *graphGpu = &graphsGpu[i];
      
        int nbytes = (graph->nvertices + 1) * sizeof (int);
        r = cudaMemcpy(dataGraphsGpu + offset, graph->destination_indices, nbytes, cudaMemcpyHostToDevice);
        if (r != cudaSuccess) {
            fprintf(stderr, "Failed to copy memory 1 \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }
        graphGpu->destination_indices = dataGraphsGpu + offset;

        offset = offset + nbytes;

        nbytes = graph->nedges * sizeof (int);

        r = cudaMemcpy(dataGraphsGpu + offset, graph->source_offsets, nbytes, cudaMemcpyHostToDevice);
        if (r != cudaSuccess) {
            fprintf(stderr, "Failed to copy memory 1 \nError: %s\n", cudaGetErrorString(r));
            exit(EXIT_FAILURE);
        }
        graphGpu->source_offsets = dataGraphsGpu + offset;
        offset = offset + graph->nedges;
    }


    printf("Launch Kernel\n");

    //    kernelSerialAproxHullNumber << <1, cont>>>(graphsGpu, resultGpu);

    printf("Read Result\n");

    //    r = cudaDeviceSynchronize();
    if (r != cudaSuccess) {
        fprintf(stderr, "Failed cudaDeviceSynchronize \nError: %s\n", cudaGetErrorString(r));
        exit(EXIT_FAILURE);
    }


    //    int* resultLocal;

    //    r = cudaMemcpyFromSymbol(&resultLocal, resultGpu, sizeof (int)*cont);
    if (r != cudaSuccess) {
        fprintf(stderr, "Failed to copy memory 4 \nError: %s\n", cudaGetErrorString(r));
        exit(EXIT_FAILURE);
    }

    r = cudaGetLastError();
    if (r != cudaSuccess) {
        fprintf(stderr, "Failed in kernelSerialAproxHullNumber \nError: %s\n", cudaGetErrorString(r));
        exit(EXIT_FAILURE);
    }

    for (int i = 0; i < cont; i++) {
        //        printf("MinHullNumberAprox Graph-%d: %d\n", i, resultLocal[i]);
    }

}

void processFiles(int argc, char** argv) {
    graphCsr* graphs = (graphCsr*) malloc((sizeof (graphCsr)) * argc);
    std::vector<int> values;

    int contGraph = 0;

    for (int x = 1; x < argc; x++) {
        char* strFile = "graph-test.txt";
        strFile = argv[x];
        DIR *dpdf;
        struct dirent *epdf;
        struct stat filestat;

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
                if (line.at(0) != CHARACTER_INIT_COMMENT) {
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
        int *colIdx = new int[numVertices + 1];
        std::copy(values.begin(), values.end(), colIdx);
        values.clear();
        stream.str("");

        std::stringstream stream2(strRArray);
        while (stream2 >> n) {

            values.push_back(n);
        }
        stream2.str("");
        strRArray.clear();

        int sizeRowOffset = values.size();
        int *rowOffset = new int[sizeRowOffset];
        std::copy(values.begin(), values.end(), rowOffset);
        values.clear();

        //        graphCsr* graph = (graphCsr*) malloc(sizeof (graphCsr));
        graphCsr* graph = &graphs[contGraph];
        graph->nvertices = numVertices;
        graph->destination_indices = colIdx;
        graph->source_offsets = rowOffset;
        contGraph++;
    }
    serialAproxHullNumberGraphs(graphs, contGraph);

}

void runTest() {

    int numVertices = 10;
    int colIdx[] = {0, 3, 6, 9, 12, 15, 18, 21, 24, 27, 30};
    int sizeRowOffset = numVertices + 1;
    int rowOffset[] = {2, 5, 6, 3, 4, 6, 0, 4, 7, 1, 5, 7, 1, 2, 9, 0, 3, 9, 0, 1, 8, 2, 3, 8, 6, 7, 9, 4, 5, 8};
    graphCsr* graph = (graphCsr*) malloc(sizeof (graphCsr));
    graph->nvertices = numVertices;
    graph->nedges = 30;
    graph->destination_indices = colIdx;
    graph->source_offsets = rowOffset;
    //    int minSerialAprox = serialAproxHullNumber(graph);
    //    printf("MinAproxHullSet: %d\n", minSerialAprox);

    serialAproxHullNumberGraphs(graph, 1);
}

int main(int argc, char** argv) {
    printf("Main\n");
    if (argc > 1) {
        processFiles(argc, argv);
    } else {
        runTest();
    }
    return 0;
}
package com.github.braully.graph;

import junit.framework.TestCase;
import java.util.Arrays;

/**
 *
 * @author strike
 */
public class StrategyTest extends TestCase {

    public static final int BLOCK_WINDOWS = 32;
    private static int BLOCK_SIZE_OPTIMAL = BLOCK_WINDOWS;
    private static int BLOCK_FACTOR_OPTIMAL = 2;

    private static void kernelAproxHullNumberGraphByBlockOptimal(int numblocks, int nthreads, int maxgraphsbyblock, int[] idxGraphsGpu, int[] graphsGpu, int cont, int[] dataGraphsGpu, int[] resultGpu, int minvertice, int maxvertice, int totalvertices) {
        int[] cache = new int[nthreads * 2];
        for (int i = 0; i < numblocks; i++) {
            for (int j = 0; j < nthreads; j++) {
                kernelMerge(idxGraphsGpu, graphsGpu, dataGraphsGpu, resultGpu, cache, i, j);
            }
        }
    }

    private static int MIN(int BLOCK_WINDOWS, int nvertices) {
        return Math.min(BLOCK_WINDOWS, nvertices);
    }

//    @Test
    public void testMerge() {
        new StrategyTest().main();
    }

    public static void kernelMerge(int[] idxGraphsGpu, int[] graphsGpu, int[] dataGraphs, int[] resultGpu, int cache[], int blockIdx, int threadIdx) {
        int offset = blockIdx;
        int idx = threadIdx;
        int[] graphData;
        int nvertices;

        if (idx == 0) {
            int cont = blockIdx;
            while (idxGraphsGpu[cont] < blockIdx) {
                cont++;
            }
            offset = graphsGpu[idxGraphsGpu[cont]];
//            graphData =  &dataGraphs[offset];
            graphData = subarray(dataGraphs, offset);
            nvertices = graphData[0];
            int proxcont = cont;
            int i = 0;
            cache[i] = idxGraphsGpu[proxcont];
            resultGpu[proxcont] = nvertices;

            for (i = 0; i < BLOCK_WINDOWS; i++) {
                cache[i] = idxGraphsGpu[proxcont];
            }

//            proxcont++;
            i = 0;

            for (int j = 0; j < BLOCK_WINDOWS; j++) {
                cache[j] = i++;
                if (i >= nvertices) {
                    proxcont++;
                    if (proxcont < graphsGpu.length && idxGraphsGpu[cont] == idxGraphsGpu[proxcont]) {
                        graphData = subarray(dataGraphs, graphsGpu[proxcont]);
                        nvertices = graphData[0];
                        resultGpu[proxcont] = nvertices;
                    }
                    i = 0;
                }
            }

//            while (proxcont < graphsGpu.length && idxGraphsGpu[cont] == idxGraphsGpu[proxcont]) {
//                offset = graphsGpu[idxGraphsGpu[proxcont]];
////                graphData =  & dataGraphs[offset];
//                graphData = subarray(dataGraphs, offset);
//                nvertices = graphData[0];
//                resultGpu[proxcont] = nvertices;
//
//                for (int j = 0; j < MIN(BLOCK_WINDOWS, nvertices); j++) {
//                    cache[i] = proxcont;//graph
//                    cache[i++] = j;//vertice
//                }
//                proxcont++;
//            }
        }

//        offset = graphsGpu[cache[idx]];
//        graphData =  dataGraphs[offset];
        graphData = subarray(dataGraphs, offset);
        nvertices = graphData[0];

        System.out.printf("thread-%d in block %d operate in graph %d\n", idx, blockIdx, cache[idx]);
    }

    public static synchronized int[] subarray(int[] arr, int start) {
        int[] copy = Arrays.copyOfRange(arr, start, arr.length - start);
        return copy;
    }

    public static void main(String... args) {
        int[] graphsGpu = {0, 22, 40, 208};
        int[] dataGraphsGpu = {7, 12, 0, 2, 5, 8, 9, 10, 11, 12, 1, 2, 0, 3, 4, 0, 5, 6, 1, 1, 2, 2, 5, 10, 0, 2, 5, 7, 8, 10, 1, 4, 0, 2, 4, 1, 3, 2, 0, 1, 25, 140, 0, 5, 10, 15, 20, 25, 31, 37, 43, 49, 55, 61, 67, 73, 79, 85, 91, 97, 103, 109, 115, 120, 125, 130, 135, 140, 1, 2, 3, 4, 5, 0, 2, 3, 4, 6, 0, 1, 3, 4, 7, 0, 1, 2, 4, 8, 0, 1, 2, 3, 9, 0, 6, 7, 8, 9, 10, 1, 5, 7, 8, 9, 11, 2, 5, 6, 8, 9, 12, 3, 5, 6, 7, 9, 13, 4, 5, 6, 7, 8, 14, 5, 11, 12, 13, 14, 15, 16, 6, 10, 12, 13, 14, 17, 7, 10, 11, 13, 14, 18, 8, 10, 11, 12, 14, 19, 9, 10, 11, 12, 13, 16, 17, 18, 19, 20, 10, 17, 18, 19, 21, 11, 15, 16, 18, 19, 22, 12, 15, 16, 17, 19, 23, 13, 15, 16, 17, 18, 24, 14, 15, 21, 22, 23, 24, 15, 16, 20, 22, 23, 24, 17, 20, 21, 23, 24, 18, 20, 21, 22, 24, 19, 20, 21, 22, 23, 15, 28, 0, 2, 5, 8, 11, 14, 17, 20, 21, 22, 23, 24, 25, 26, 27, 28, 1, 2, 0, 3, 4, 0, 5, 6, 1, 7, 8, 1, 9, 10, 2, 11, 12, 2, 13, 14, 3, 3, 4, 4, 5, 5, 6, 6};
        int[] resultGpu = new int[graphsGpu.length];
        int[] idxGraphsGpu = new int[graphsGpu.length];
        int cont = 0;

        int maxvertice = 0;
        int minvertice = Integer.MAX_VALUE;
        int totalvertices = 0;

        int totalVert = 0;
        int currentCont = 0;

        System.out.print("idxGraphsGpu[i]={");

        for (int i = 0; i < graphsGpu.length; i++) {
            int sizeGraph = dataGraphsGpu[graphsGpu[i]];
            currentCont = currentCont + (totalVert / BLOCK_WINDOWS);
            idxGraphsGpu[i] = currentCont;
//            totalVert = (totalVert + sizeGraph) % BLOCK_WINDOWS;
            totalVert = (totalVert % BLOCK_WINDOWS) + sizeGraph;

            System.out.printf("%d, ", idxGraphsGpu[i]);

            totalvertices = totalvertices + sizeGraph;
        }
        System.out.println("}");

        int numblocks = totalvertices / BLOCK_SIZE_OPTIMAL;
        if ((totalvertices % BLOCK_SIZE_OPTIMAL) > 0) {
            numblocks++;
        }

        int nthreads = BLOCK_SIZE_OPTIMAL;
        //int nthreads = BLOCK_SIZE_OPTIMAL / BLOCK_FACTOR_OPTIMAL;

        int[] map = new int[numblocks + numblocks * BLOCK_SIZE_OPTIMAL];

        int i = 0;
//
//        for (int j = 0; j < BLOCK_SIZE_OPTIMAL; j++) {
//            map[j] = i++;
//            if (i >= nvertices) {
//                proxcont++;
//
//                if (proxcont < graphsGpu.length && idxGraphsGpu[cont] == idxGraphsGpu[proxcont]) {
//                    graphData = subarray(dataGraphs, graphsGpu[proxcont]);
//                    nvertices = graphData[0];
//                    resultGpu[proxcont] = nvertices;
//                }
//                i = 0;
//            }
//        }

        int maxgraphsbyblock = BLOCK_SIZE_OPTIMAL / minvertice;
        if ((BLOCK_SIZE_OPTIMAL % minvertice) > 0) {
            maxgraphsbyblock++;
        }

        kernelAproxHullNumberGraphByBlockOptimal(numblocks, nthreads, maxgraphsbyblock, idxGraphsGpu, graphsGpu, cont, dataGraphsGpu, resultGpu, minvertice, maxvertice, totalvertices);
    }

}

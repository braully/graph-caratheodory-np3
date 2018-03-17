/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import junit.framework.TestCase;
import java.util.Arrays;

/**
 *
 * @author strike
 */
public class StrategyTest extends TestCase {

    public static final int BLOCK_WINDOWS = 32;
    private static int BLOCK_SIZE_OPTIMAL = 128;
    private static int BLOCK_FACTOR_OPTIMAL = 2;

    private static void kernelAproxHullNumberGraphByBlockOptimal(int numblocks, int nthreads, int maxgraphsbyblock, int[] idxGraphsGpu, int[] graphsGpu, int cont, int[] dataGraphsGpu, int[] resultGpu, int minvertice, int maxvertice, int totalvertices) {
        int[] cache = new int[numblocks];
        for (int i = 0; i < numblocks; i++) {
            for (int j = 0; j < nthreads; j++) {
                kernelMerge(idxGraphsGpu, graphsGpu, dataGraphsGpu, resultGpu, cache, i, j);
            }
        }
    }

//    @Test
    public void testMerge() {

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
            proxcont++;

            for (i = 0; i < BLOCK_WINDOWS; i++) {
                cache[i] = idxGraphsGpu[proxcont];
            }

            i = 0;
            while (idxGraphsGpu[cont] == idxGraphsGpu[proxcont]) {
                offset = graphsGpu[idxGraphsGpu[proxcont]];
//                graphData =  & dataGraphs[offset];
                graphData = subarray(dataGraphs, offset);
                nvertices = graphData[0];
                resultGpu[proxcont] = nvertices;
                cache[i] = idxGraphsGpu[proxcont];
                proxcont++;
            }
        }

        offset = graphsGpu[cache[idx]];
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
        int[] graphs = null;
        int[] idxGraphsGpu = null;
        int[] graphsGpu = null;
        int[] dataGraphsGpu = null;
        int[] resultGpu = null;
        int cont = 0;

        int maxvertice = 0;
        int minvertice = Integer.MAX_VALUE;
        int totalvertices = 0;

        int numblocks = totalvertices / BLOCK_SIZE_OPTIMAL;
        if ((totalvertices % BLOCK_SIZE_OPTIMAL) > 0) {
            numblocks++;
        }
        int nthreads = BLOCK_SIZE_OPTIMAL / BLOCK_FACTOR_OPTIMAL;

        int maxgraphsbyblock = BLOCK_SIZE_OPTIMAL / minvertice;
        if ((BLOCK_SIZE_OPTIMAL % minvertice) > 0) {
            maxgraphsbyblock++;
        }
        kernelAproxHullNumberGraphByBlockOptimal(numblocks, nthreads, maxgraphsbyblock, idxGraphsGpu, graphsGpu, cont, dataGraphsGpu, resultGpu, minvertice, maxvertice, totalvertices);
    }

}

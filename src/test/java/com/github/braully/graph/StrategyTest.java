/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import junit.framework.TestCase;

/**
 *
 * @author strike
 */
public class StrategyTest extends TestCase {

    public static final int BLOCK_WINDOWS = 32;

//    @Test
    public void testMerge() {

    }

    public void kernelMerge(int[] idxGraphsGpu, int[] graphGpu, int[] dataGraphs, int cache[], int blockIdx, int threadIdx) {
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
            graphData = dataGraphs[offset];
            nvertices = graphData[0];
            int proxcont = cont;
            int i = 0;
            cache[i] = idxGraphsGpu[proxcont];
            results[proxcont] = nvertices;
            proxcont++;

            for (i = 0; i < BLOCK_WINDOWS; i++) {
                cache[i] = idxGraphsGpu[proxcont];
            }

            i = 0;
            while (idxGraphsGpu[cont] == idxGraphsGpu[proxcont]) {
                offset = graphsGpu[idxGraphsGpu[proxcont]];
//                graphData =  & dataGraphs[offset];
                graphData = dataGraphs[offset];
                nvertices = graphData[0];
                results[proxcont] = nvertices;
                cache[i] = idxGraphsGpu[proxcont];
                proxcont++;
            }
        }

        offset = graphsGpu[cache[idx]];
//        graphData =  dataGraphs[offset];
        graphData = dataGraphs[offset];
        nvertices = graphData[0];

        if (verboseKernel) {
            printf("thread-%d in block %d operate in graph %d\n", idx, blockIdx, cache[idx]);
        }
    }
}

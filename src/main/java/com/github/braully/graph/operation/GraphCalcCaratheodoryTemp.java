/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.util.CombinatoricsUtils;

/**
 *
 * @author strike
 */
public class GraphCalcCaratheodoryTemp extends GraphCheckCaratheodorySet {

    static final int PROCESSED = 3;

    static final String type = "P3-Convexity";
    static final String description = "Temp";

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        OperationConvexityGraphResult processedCaratheodroySet = null;
        Map<String, Object> result = null;
        if (graph == null) {
            return result;
        }
        int maxSizeSet = (graph.getVertexCount() + 1) / 2;
        int currentSize = maxSizeSet;
        int left = 0;
        int rigth = maxSizeSet;
        result = new HashMap<>();

        List<Integer> csrColIdxs = UtilGraph.csrColIdxs(graph);
        List<Integer> rowOffset = UtilGraph.rowOffset(graph);
        int[] csrColIdxsArray = new int[csrColIdxs.size()];
        for (int i = 0; i < csrColIdxsArray.length; i++) {
            csrColIdxsArray[i] = csrColIdxs.get(i);
        }
        int[] rowOffsetArray = new int[rowOffset.size()];
        for (int i = 0; i < rowOffsetArray.length; i++) {
            rowOffsetArray[i] = rowOffset.get(i);
        }

        int vertexCount = graph.getVertexCount();

        while (left <= rigth) {
            currentSize = (left + rigth) / 2;
            processedCaratheodroySet = findCaratheodroySetBruteForce(csrColIdxsArray, rowOffsetArray, currentSize, vertexCount);
            if (processedCaratheodroySet != null
                    && processedCaratheodroySet.caratheodorySet != null
                    && !processedCaratheodroySet.caratheodorySet.isEmpty()) {
                result.clear();
                result.putAll(processedCaratheodroySet.toMap());
                left = currentSize + 1;
            } else {
                rigth = currentSize - 1;
            }
        }
        return result;
    }

    OperationConvexityGraphResult findCaratheodroySetBruteForce(int[] csrColIdxsArray, int[] rowOffsetArray, int currentSize, int vertexCount) {
        OperationConvexityGraphResult processedHullSet = null;
        if (csrColIdxsArray == null || vertexCount <= 0) {
            return processedHullSet;
        }
        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(vertexCount, currentSize);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            int[] aux = new int[vertexCount];
            int[] auxc = new int[vertexCount];
            int hsp3g = checkCaratheodorySetP3CSR(csrColIdxsArray, vertexCount, rowOffsetArray, rowOffsetArray.length, aux, auxc, vertexCount, currentSet, currentSize, 0);

            if (hsp3g > 0) {
                processedHullSet = new OperationConvexityGraphResult();
                processedHullSet.caratheodorySet = new HashSet<>();
                for (int i = 0; i < currentSize; i++) {
                    processedHullSet.caratheodorySet.add(currentSet[i]);
                }
                processedHullSet.caratheodoryNumber = currentSize;
                break;
            }
        }
        return processedHullSet;
    }

    int checkCaratheodorySetP3CSR(int[] csrColIdxs, int nvertices,
            int[] csrRowOffset, int sizeRowOffset,
            int[] aux, int[] auxc,
            int auxSize,
            int[] currentCombinations,
            int k, int idx) {

        for (int i = 0; i < nvertices; i++) {
            aux[i] = 0;
            auxc[i] = 0;
        }

        int headQueue = nvertices;
        //        int tailQueue = -1;
        int tailQueue = 0;

        for (int i = 0; i < k; i++) {
            int idi = currentCombinations[i];
            aux[idi] = INCLUDED;
            auxc[idi] = 1;
            headQueue = Math.min(headQueue, idi);
            tailQueue = Math.max(tailQueue, idi);
        }

        while (headQueue <= tailQueue) {
            int verti = currentCombinations[headQueue];

            if (verti >= nvertices || aux[verti] != INCLUDED) {
                headQueue++;
                continue;
            }

            int end = csrColIdxs[verti + 1];
            for (int i = csrColIdxs[verti]; i < end; i++) {
                int vertn = csrRowOffset[i];
                if (vertn >= nvertices) {
                    continue;
                }
                if (vertn != verti && aux[vertn] < INCLUDED) {
                    int previousValue = aux[vertn];
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (previousValue < INCLUDED) {
                        if (aux[vertn] >= INCLUDED) {
                            //                            tailQueue = (tailQueue + 1) % maxSizeQueue;
                            //                            queue[tailQueue] = vertn;
                            headQueue = Math.min(headQueue, aux[vertn]);
                            tailQueue = Math.max(tailQueue, aux[vertn]);
                        }
                        auxc[vertn] = auxc[vertn] + auxc[verti];
                    }
                }
            }
            aux[verti] = PROCESSED;
        }
        return 0;
    }

    int checkCaratheodorySetP3CSRBkp(int[] csrColIdxs, int nvertices,
            int[] csrRowOffset, int sizeRowOffset,
            int[] aux, int[] auxc,
            int auxSize,
            int[] currentCombination,
            int sizeComb, int idx) {
        //clean aux vector            
        for (int i = 0; i < auxSize; i++) {
            aux[i] = 0;
            auxc[i] = 0;
        }
        int maxSizeQueue = Math.max((auxSize / 2), 100);
        int[] queue = new int[(int) maxSizeQueue];
        int headQueue = 0;
        int tailQueue = -1;

        for (int i = 0; i < sizeComb; i++) {
            tailQueue = (tailQueue + 1) % maxSizeQueue;
            queue[tailQueue] = currentCombination[i];
            aux[currentCombination[i]] = INCLUDED;
            auxc[currentCombination[i]] = 1;
        }

        int countExec = 1;

        while (headQueue <= tailQueue) {
            int verti = queue[headQueue];
            headQueue = (headQueue + 1) % maxSizeQueue;
            int end = csrColIdxs[verti + 1];
            for (int i = csrColIdxs[verti]; i < end; i++) {
                int vertn = csrRowOffset[i];
                if (vertn != verti && vertn < nvertices) {
                    int previousValue = aux[vertn];
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (previousValue < INCLUDED) {
                        if (aux[vertn] >= INCLUDED) {
                            tailQueue = (tailQueue + 1) % maxSizeQueue;
                            queue[tailQueue] = vertn;
                        }
                        auxc[vertn] = auxc[vertn] + auxc[verti];
                    }
                }
            }
        }

        int sizederivated = 0;

        int[] auxbackup = null;
        int[] auxcbackup = null;

        for (int i = 0; i < auxSize; i++) {
            if (auxc[i] >= sizeComb) {
                auxbackup = aux.clone();
                auxcbackup = auxc.clone();
                sizederivated = calcDerivatedPartial(csrColIdxs, nvertices,
                        csrRowOffset, sizeRowOffset, aux, auxc, auxSize,
                        currentCombination, sizeComb, queue, maxSizeQueue);
                break;
            }
        }
        if (sizederivated > 0 && GraphWS.verbose) {
            System.out.print("Cmc = {");
            for (int i = 0; i < currentCombination.length; i++) {
                System.out.print(currentCombination[i] + " | ");
            }
            System.out.println("}");

            System.out.print("Aux = {");
            for (int i = 0; i < nvertices; i++) {
                System.out.print(auxbackup[i] + " | ");
            }
            System.out.println("}");

            System.out.print("Auxc= {");
            for (int i = 0; i < nvertices; i++) {
                System.out.print(auxcbackup[i] + " | ");
            }
            System.out.println("}");
        }
        return sizederivated;
    }

    int calcDerivatedPartial(int[] csrColIdxs, int nvertices,
            int[] csrRowOffset, int sizeRowOffset,
            int[] aux, int[] auxc,
            int auxSize, int[] currentCombination,
            int sizeComb, int[] queue, int maxSizeQueue) {

        for (int i = 0; i < sizeComb; i++) {
            int p = currentCombination[i];
            int headQueue = 0;
            int tailQueue = -1;

            for (int j = 0; j < auxSize; j++) {
                auxc[j] = 0;
            }

            for (int j = 0; j < sizeComb; j++) {
                int v = currentCombination[j];
                if (v != p) {
                    tailQueue = (tailQueue + 1) % maxSizeQueue;
                    queue[tailQueue] = v;
                    auxc[v] = INCLUDED;
                }
            }
            while (headQueue <= tailQueue) {
                int verti = queue[headQueue];
                headQueue = (headQueue + 1) % maxSizeQueue;
                aux[verti] = 0;
                int end = csrColIdxs[verti + 1];
                for (int x = csrColIdxs[verti]; x < end; x++) {
                    int vertn = csrRowOffset[x];
                    if (vertn != verti) {
                        int previousValue = auxc[vertn];
                        auxc[vertn] = auxc[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                        if (previousValue < INCLUDED && auxc[vertn] >= INCLUDED) {
                            tailQueue = (tailQueue + 1) % maxSizeQueue;
                            queue[tailQueue] = vertn;
                        }
                    }
                }
            }
        }
        int countDerivated = 0;
        for (int i = 0; i < auxSize; i++) {
            if (aux[i] >= INCLUDED) {
                countDerivated++;
            }
        }
        return countDerivated;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }
}

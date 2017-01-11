package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import static com.github.braully.graph.operation.GraphCalcCaratheodoryTemp.PROCESSED;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.apache.commons.math3.util.CombinatoricsUtils;

public class GraphTemOperation implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "Tmp Check Aux";

    public static final int INCLUDED = 2;
    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;

//    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
//        long totalTimeMillis = -1;
//        Collection<Integer> set = graphRead.getSet();
//        int[] arr = new int[set.size()];
//        int i = 0;
//        for (Integer v : set) {
//            arr[i] = v;
//            i++;
//        }
//        totalTimeMillis = System.currentTimeMillis();
//
//
//        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
//        Map<String, Object> response = new HashMap<>();
//        
//        if (set.size() >= 2) {
//            Map<String, Object> tMap = hsp3(graphRead, arr);
//            response.putAll(tMap);
//        }
//        totalTimeMillis = System.currentTimeMillis() - totalTimeMillis;
//        return response;
//    }
    @Override
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

        while (left <= rigth) {
            currentSize = (left + rigth) / 2;
            processedCaratheodroySet = findCaratheodroySetBruteForce(graph, currentSize);
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

    OperationConvexityGraphResult findCaratheodroySetBruteForce(UndirectedSparseGraphTO<Integer, Integer> graph, int currentSize) {
        OperationConvexityGraphResult processedHullSet = null;
        if (graph == null || graph.getVertexCount() <= 0) {
            return processedHullSet;
        }
        Collection vertices = graph.getVertices();

        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(graph.getVertexCount(), currentSize);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            int hsp3 = hsp3(graph, currentSet);

            if (hsp3 > 0) {
                processedHullSet = new OperationConvexityGraphResult();
                Set<Integer> curSet = new HashSet<Integer>();
                for (int h = 0; h < currentSize; h++) {
                    curSet.add(currentSet[h]);
                }
                processedHullSet.caratheodoryNumber = currentSize;
                processedHullSet.caratheodorySet = curSet;
                break;
            }
        }
        return processedHullSet;
    }

    public int hsp3(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] currentCombinations) {

        List<Integer> csrColIdxsList = UtilGraph.csrColIdxs(graph);
        List<Integer> rowOffsetList = UtilGraph.rowOffset(graph);
        int[] csrColIdxs = new int[csrColIdxsList.size()];
        for (int i = 0; i < csrColIdxs.length; i++) {
            csrColIdxs[i] = csrColIdxsList.get(i);
        }
        int[] rowOffset = new int[rowOffsetList.size()];
        for (int i = 0; i < rowOffset.length; i++) {
            rowOffset[i] = rowOffsetList.get(i);
        }

        int vertexCount = graph.getVertexCount();
        int nvertices = vertexCount;

        int[] aux = new int[vertexCount];
        int[] auxc = new int[vertexCount];
        int k = currentCombinations.length;

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
            int verti = headQueue;

            if (verti >= nvertices || aux[verti] != INCLUDED) {
                headQueue++;
                continue;
            }

            int end = csrColIdxs[verti + 1];
            for (int i = csrColIdxs[verti]; i < end; i++) {
                int vertn = rowOffset[i];
                if (vertn >= nvertices) {
                    continue;
                }
                if (vertn != verti && aux[vertn] < INCLUDED) {
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (aux[vertn] == INCLUDED) {
                        headQueue = Math.min(headQueue, vertn);
                        tailQueue = Math.max(tailQueue, vertn);
                    }
                    auxc[vertn] = auxc[vertn] + auxc[verti];
                }
            }
            aux[verti] = PROCESSED;
        }

        //derivated 
        boolean checkDerivated = false;

        for (int i = 0; i < nvertices; i++) {
            if (auxc[i] >= k && aux[i] == PROCESSED) {
                checkDerivated = true;
                break;
            }
        }

        int sizederivated = 0;
        if (checkDerivated) {
            int[] auxbackup = aux.clone();
            int[] auxcbackup = auxc.clone();

            for (int i = 0; i < k; i++) {
                int p = currentCombinations[i];
                headQueue = nvertices;
                tailQueue = -1;

                for (int j = 0; j < nvertices; j++) {
                    auxcbackup[j] = 0;
                }

                for (int j = 0; j < k; j++) {
                    int v = currentCombinations[j];
                    if (v != p) {
                        auxcbackup[v] = INCLUDED;
                        headQueue = Math.min(headQueue, v);
                        tailQueue = Math.max(tailQueue, v);
                    }
                }
                while (headQueue <= tailQueue) {
                    int verti = headQueue;

                    if (verti >= nvertices || auxcbackup[verti] != INCLUDED) {
                        headQueue++;
                        continue;
                    }
                    auxbackup[verti] = 0;
                    int end = csrColIdxs[verti + 1];
                    for (int x = csrColIdxs[verti]; x < end; x++) {
                        int vertn = rowOffset[x];
                        if (vertn != verti && auxcbackup[vertn] < INCLUDED) {
                            auxcbackup[vertn] = auxcbackup[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                            if (auxcbackup[vertn] == INCLUDED) {
                                headQueue = Math.min(headQueue, vertn);
                                tailQueue = Math.max(tailQueue, vertn);
                            }
                        }
                    }
                    auxcbackup[verti] = PROCESSED;
                }
            }
            for (int i = 0; i < nvertices; i++) {
                if (auxbackup[i] == PROCESSED) {
                    sizederivated++;
                }
            }
        }

        if (sizederivated == 0 && checkDerivated) {
            System.out.println("*** MISSCHECK ***");
        }

//        Map<String, Object> tmap = new HashMap<>();
//        tmap.put("Aux", aux);
//        tmap.put("Auxc", auxc);
//        tmap.put("Sizederivated", sizederivated);
//        if (true) {
//            System.out.print("Aux = {");
//            for (int i = 0; i < nvertices; i++) {
//                System.out.print(aux[i] + " | ");
//            }
//            System.out.println("}");
//
//            System.out.print("Auxc= {");
//            for (int i = 0; i < nvertices; i++) {
//                System.out.print(auxc[i] + " | ");
//            }
//            System.out.println("}");
//        }
//
//        return tmap;
        return sizederivated;
    }

    /**
     * ∂H(S)=H(S)\⋃p∈SH(S\{p})
     *
     * @param graph
     * @param hsp3g
     * @param currentSet
     * @return
     */
    public Set<Integer> calcDerivatedPartial(UndirectedSparseGraphTO<Integer, Integer> graph,
            Set<Integer> hsp3g, int[] currentSet) {
        Set<Integer> partial = new HashSet<>();
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        partial.addAll(hsp3g);

        for (Integer p : currentSet) {
            int[] aux = new int[graph.getVertexCount()];
            for (Integer v : currentSet) {
                if (!v.equals(p)) {
                    mustBeIncluded.add(v);
                    aux[v] = INCLUDED;
                }
            }
            while (!mustBeIncluded.isEmpty() && !partial.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                partial.remove(verti);
                Collection<Integer> neighbors = graph.getNeighbors(verti);
                for (int vertn : neighbors) {
                    if (vertn != verti) {
                        int previousValue = aux[vertn];
                        aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                        if (previousValue < INCLUDED && aux[vertn] >= INCLUDED) {
                            mustBeIncluded.add(vertn);
                        }
                    }
                }
            }
        }
        return partial;
    }

    @Override
    public String getTypeProblem() {
        return type;
    }

    @Override
    public String getName() {
        return description;
    }
}

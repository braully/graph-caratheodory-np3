package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import static com.github.braully.graph.operation.GraphCalcCaratheodoryTemp.PROCESSED;
import static com.github.braully.graph.operation.GraphCheckCaratheodorySet.INCLUDED;
import static com.github.braully.graph.operation.GraphCheckCaratheodorySet.NEIGHBOOR_COUNT_INCLUDED;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphTemOperation implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "Tmp Check Aux";

    public static final int INCLUDED = 2;
    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        long totalTimeMillis = -1;
        Collection<Integer> set = graphRead.getSet();
        int[] arr = new int[set.size()];
        int i = 0;
        for (Integer v : set) {
            arr[i] = v;
            i++;
        }
        totalTimeMillis = System.currentTimeMillis();


        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();

        if (set.size() >= 2) {
            Map<String, Object> tMap = hsp3(graphRead, arr);
            response.putAll(tMap);
        }
        totalTimeMillis = System.currentTimeMillis() - totalTimeMillis;
        return response;
    }

    public Map<String, Object> hsp3(UndirectedSparseGraphTO<Integer, Integer> graph,
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
                    int previousValue = aux[vertn];
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (previousValue < INCLUDED) {
                        if (aux[vertn] == INCLUDED) {
                            headQueue = Math.min(headQueue, vertn);
                            tailQueue = Math.max(tailQueue, vertn);
                        }
                        auxc[vertn] = auxc[vertn] + auxc[verti];
                    }
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
                headQueue = 0;
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
                    if (verti < nvertices) {
                        continue;
                    }

                    int end = csrColIdxs[verti + 1];
                    for (int x = csrColIdxs[verti]; x < end; x++) {
                        int vertn = rowOffset[x];
                        if (vertn != verti && auxcbackup[vertn] < INCLUDED) {
                            int previousValue = auxcbackup[vertn];
                            auxcbackup[vertn] = auxcbackup[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                            if (previousValue < INCLUDED && auxcbackup[vertn] == INCLUDED) {
                                headQueue = Math.min(headQueue, vertn);
                                tailQueue = Math.max(tailQueue, vertn);
                            }
                        }
                    }

                }
            }
            for (int i = 0; i < nvertices; i++) {
                if (auxbackup[i] == PROCESSED) {
                    sizederivated++;
                }
            }
        }

        Map<String, Object> tmap = new HashMap<>();
        tmap.put("Aux", aux);
        tmap.put("Auxc", auxc);
        tmap.put("Sizederivated", sizederivated);
        if (true) {
            System.out.print("Aux = {");
            for (int i = 0; i < nvertices; i++) {
                System.out.print(aux[i] + " | ");
            }
            System.out.println("}");

            System.out.print("Auxc= {");
            for (int i = 0; i < nvertices; i++) {
                System.out.print(auxc[i] + " | ");
            }
            System.out.println("}");
        }

        return tmap;
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

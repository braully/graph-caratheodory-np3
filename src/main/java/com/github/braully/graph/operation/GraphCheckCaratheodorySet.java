package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphCheckCaratheodorySet implements IGraphOperation {

    static final String name = "checkset";
    static final String description = "Check Set(S)";

    public static final String PARAM_NAME_CARATHEODORY_NUMBER = "number";
    public static final String PARAM_NAME_CARATHEODORY_SET = "set";
    public static final String PARAM_NAME_CONVEX_HULL = "hs";
    public static final String PARAM_NAME_AUX_PROCESS = "aux";
    public static final String PARAM_NAME_TOTAL_TIME_MS = "tms";
    public static final String PARAM_NAME_PARTIAL_DERIVATED = "phs";

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
        OperationGraphResult caratheodoryNumberGraph = null;
        if (set.size() >= 2) {
            caratheodoryNumberGraph = hsp3(graphRead, arr);
        }
        totalTimeMillis = System.currentTimeMillis() - totalTimeMillis;

        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();
        if (caratheodoryNumberGraph == null) {
            caratheodoryNumberGraph = new OperationGraphResult();
        }
        if (caratheodoryNumberGraph.caratheodorySet != null
                && !caratheodoryNumberGraph.caratheodorySet.isEmpty()) {
            response.putAll(caratheodoryNumberGraph.toMap());
            response.put(PARAM_NAME_CARATHEODORY_NUMBER, caratheodoryNumberGraph.caratheodorySet.size());
        }
        return response;
    }

    public OperationGraphResult hsp3(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] currentSet) {
        int currentSetSize = 0;
        OperationGraphResult processedHullSet = null;
        Set<Integer> hsp3g = new HashSet<>();
        int[] aux = new int[graph.getVertexCount()];
        int[] auxa = new int[graph.getVertexCount()];
        int[] auxb = new int[graph.getVertexCount()];
        int[] auxc = new int[graph.getVertexCount()];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
            auxc[i] = 0;
            auxa[i] = auxb[i] = -1;
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer v : currentSet) {
            mustBeIncluded.add(v);
            aux[v] = INCLUDED;
            auxc[v] = 1;
            currentSetSize++;
        }
        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
            hsp3g.add(verti);
//            aux[verti] = aux[verti] + INCLUDED;
            Collection<Integer> neighbors = graph.getNeighbors(verti);

            for (int vertn : neighbors) {
                if (vertn != verti) {
                    int previousValue = aux[vertn];
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (previousValue < INCLUDED) {
                        if (aux[vertn] >= INCLUDED) {
                            mustBeIncluded.add(vertn);
                            auxb[vertn] = verti;
                            auxc[vertn] = auxc[vertn] + auxc[verti];
                        } else {
                            auxa[vertn] = verti;
                            auxc[vertn] = auxc[vertn] + auxc[verti];
                        }
                    }
                }
            }
        }

        if (GraphWS.verbose) {
            System.out.print("Aux = {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                System.out.print(aux[i] + " | ");
            }
            System.out.println("}");

            System.out.print("Auxa= {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                System.out.print((auxa[i] < 0 ? "-" : auxa[i]) + " | ");
            }
            System.out.println("}");

            System.out.print("Auxb= {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                System.out.print((auxb[i] < 0 ? "-" : auxb[i]) + " | ");
            }
            System.out.println("}");

            System.out.print("Auxc= {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                System.out.print(auxc[i] + " | ");
            }
            System.out.println("}");
        }

        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (auxc[i] >= currentSetSize) {
                Queue<Integer> queueu = new ArrayDeque<>();
                Set<Integer> hs = new HashSet<>(currentSetSize);
                queueu.add(auxa[i]);
                queueu.add(auxb[i]);
                while (!queueu.isEmpty()) {
                    Integer actual = queueu.remove();
                    if (actual == -1) {
                        continue;
                    }
                    if (auxa[actual] == -1) {
                        hs.add(actual);
                    } else {
                        queueu.add(auxa[actual]);
                    }
                    if (auxb[actual] == -1) {
                        hs.add(actual);
                    } else {
                        queueu.add(auxb[actual]);
                    }
                }
                if (GraphWS.verbose) {
                    System.out.println("hs(" + i + ") = " + hs);
                }
                if (hs.size() == currentSetSize) {
                    Set<Integer> partial = calcDerivatedPartial(graph,
                            hsp3g, currentSet);
                    if (partial != null && !partial.isEmpty()) {
                        processedHullSet = new OperationGraphResult();
                        processedHullSet.caratheodoryNumber = currentSetSize;
                        processedHullSet.auxProcessor = aux;
                        processedHullSet.convexHull = hsp3g;
                        processedHullSet.caratheodorySet = hs;
                        processedHullSet.partial = calcDerivatedPartial(graph,
                                hsp3g, currentSet);

                    }
                    break;
                }
            }
        }
        return processedHullSet;
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
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }
}

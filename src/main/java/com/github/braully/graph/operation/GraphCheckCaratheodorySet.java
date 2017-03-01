package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphCheckCaratheodorySet implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "Check Set(S)";

    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;
    public static final int INCLUDED = 2;
    public static final int PROCESSED = 3;

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
        OperationConvexityGraphResult caratheodoryNumberGraph = null;
        if (set.size() >= 2) {
            caratheodoryNumberGraph = hsp3(graphRead, arr);
        }
        totalTimeMillis = System.currentTimeMillis() - totalTimeMillis;

        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();
        if (caratheodoryNumberGraph == null) {
            caratheodoryNumberGraph = new OperationConvexityGraphResult();
        }
        if (caratheodoryNumberGraph.caratheodorySet != null
                && !caratheodoryNumberGraph.caratheodorySet.isEmpty()) {
            response.putAll(caratheodoryNumberGraph.toMap());
            response.put(OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_NUMBER, caratheodoryNumberGraph.caratheodorySet.size());
        }
        return response;
    }

    public OperationConvexityGraphResult hsp3(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] currentSet) {
        int currentSetSize = 0;
        OperationConvexityGraphResult processedHullSet = null;
        Set<Integer> hsp3g = new HashSet<>();
        int[] aux = new int[graph.getVertexCount()];
        int[] auxc = new int[graph.getVertexCount()];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
            auxc[i] = 0;
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
            Collection<Integer> neighbors = graph.getNeighbors(verti);

            for (int vertn : neighbors) {
                if (vertn == verti) {
                    continue;
                }
                if (vertn != verti && aux[vertn] < INCLUDED) {
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (aux[vertn] == INCLUDED) {
                        mustBeIncluded.add(vertn);
                    }
                    auxc[vertn] = auxc[vertn] + auxc[verti];
                }
            }
            aux[verti] = PROCESSED;
        }

        boolean checkDerivated = false;

        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (auxc[i] >= currentSet.length && aux[i] == PROCESSED) {
                checkDerivated = true;
                break;
            }
        }
        Set<Integer> partial = null;
        if (checkDerivated) {
            partial = calcDerivatedPartial(graph,
                    hsp3g, currentSet);
            if (partial != null && !partial.isEmpty()) {
                Set<Integer> setCurrent = new HashSet<>();
                for (int i : currentSet) {
                    setCurrent.add(i);
                }
                processedHullSet = new OperationConvexityGraphResult();
                processedHullSet.caratheodoryNumber = currentSetSize;
                processedHullSet.auxProcessor = aux;
                processedHullSet.convexHull = hsp3g;
                processedHullSet.caratheodorySet = setCurrent;
                processedHullSet.partial = partial;
            }
        }

        if (false) {
            Set<Integer> curSet = new HashSet<>();
            for (int i = 0; i < currentSet.length; i++) {
                curSet.add(currentSet[i]);
            }

            System.out.print("\n∂H(S)= {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                if (partial != null && partial.contains(i)) {
                    System.out.printf("%2d | ", i);
                } else {
                    System.out.print("   | ");
                }
            }
            System.out.println("}");

            System.out.print("H(S) = {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                if (hsp3g.contains(i)) {
                    System.out.printf("%2d | ", i);
                } else {
                    System.out.print("   | ");
                }
            }
            System.out.println("}");

            System.out.print("S    = {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                if (curSet.contains(i)) {
                    System.out.printf("%2d | ", i);
                } else {
                    System.out.print("   | ");
                }
            }
            System.out.println("}");

            System.out.print("Aux  = {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                System.out.printf("%2d | ", aux[i]);
            }
            System.out.println("}");

            System.out.print("Auxc = {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                System.out.printf("%2d | ", auxc[i]);
            }
            System.out.println("}");
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
    public String getTypeProblem() {
        return type;
    }

    @Override
    public String getName() {
        return description;
    }
}

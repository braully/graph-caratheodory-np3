package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphAlgorithmHullSet implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "H(S) Hull Algorithm";

    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;
    public static final int INCLUDED = 2;
    public static final int PROCESSED = 3;

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        Collection<Integer> set = graphRead.getSet();

        List<Integer> sugestions = new ArrayList<>(set);
        List<Integer> vlinha = new ArrayList<Integer>(graphRead.getVertices());
        List<Integer> hspresumido = new ArrayList<Integer>();
        List<Integer> union = new ArrayList();
        List<Integer> intersection = new ArrayList<>();

        List<Integer> s = new ArrayList<>();

        Integer v = 0;
        if (sugestions.isEmpty()) {
            v = (int) Math.random() * graphRead.getVertexCount();
        } else {
            v = sugestions.get(0);
        }

        int[] aux = new int[graphRead.getVertexCount()];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }

        List<Integer> hsreal = new ArrayList<>();
        List<Integer> frontier = new ArrayList<>();
        List<Integer> unreachable = new ArrayList<>();

        int j = 0;
        boolean acabou = false;

        while (!acabou) {
            frontier.clear();
            hsreal.clear();
            unreachable.clear();

            System.out.println("Passo-" + j);
            System.out.println("\tv=" + v);
            addVertToAux(aux, graphRead, v);
            s.add(v);

            hspresumido.add(v);

            for (int i = 0; i < aux.length; i++) {
                int val = aux[i];
                if (val == 0) {
                    unreachable.add(i);
                } else if (val == NEIGHBOOR_COUNT_INCLUDED) {
                    frontier.add(i);
                } else if (val >= INCLUDED) {
                    hsreal.add(i);
                }
            }
            Collection neighbors = graphRead.getNeighbors(v);
            List tmp = new ArrayList(neighbors);
            tmp.add(v);
            tmp.retainAll(union);
            intersection.addAll(tmp);

            union.addAll(neighbors);
            union.add(v);

            vlinha.removeAll(union);

            System.out.println("\tUnion:|" + union + "|=" + union.size());
            System.out.println("\tV-Union:|" + vlinha + "|=" + vlinha.size());
            System.out.println("\tIntersection:|" + intersection + "|" + intersection.size());
            System.out.println("\tH(S) Real:" + hsreal);
            System.out.println("\tH(S) Linh:" + hspresumido);

            List<Integer> possiblesNextVertices = new ArrayList<>();

            for (Integer vi : vlinha) {
                if (!union.containsAll(graphRead.getNeighbors(vi))) {
                    possiblesNextVertices.add(vi);
                }
            }
            System.out.println("\tV's não saturados:" + possiblesNextVertices);
            if (possiblesNextVertices.isEmpty()) {
                acabou = true;
                System.out.println("\t--Fim todos vértices saturado ");
            }
            j++;
        }

        Map response = new HashMap();
        response.put(OperationConvexityGraphResult.PARAM_NAME_CONVEX_HULL, "");
        return response;
    }

    public OperationConvexityGraphResult hsp3(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] currentSet) {
        OperationConvexityGraphResult processedHullSet = null;
        processedHullSet = hsp3aux(graph, currentSet);
        return processedHullSet;
    }

    public OperationConvexityGraphResult hsp3aux(UndirectedSparseGraphTO<Integer, Integer> graph, int[] currentSet) {
        OperationConvexityGraphResult processedHullSet = null;
        int[] aux = new int[graph.getVertexCount()];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }
        addVertToAux(aux, graph, currentSet);

        Set<Integer> hsp3g = new HashSet<>();
        for (int i = 0; i < aux.length; i++) {
            if (aux[i] >= PROCESSED) {
                hsp3g.add(i);
            }
        }

        processedHullSet = new OperationConvexityGraphResult();
        processedHullSet.auxProcessor = aux;
        processedHullSet.convexHull = hsp3g;
        return processedHullSet;
    }

    public int addVertToAux(int[] aux, UndirectedSparseGraphTO<Integer, Integer> graph, int v) {
        return addVertToAux(aux, graph, new int[]{v});
    }

    public int addVertToAux(int[] aux, UndirectedSparseGraphTO<Integer, Integer> graph, int[] currentSet) {
        int cont = 0;
        if (currentSet == null || currentSet.length == 0) {
            return cont;
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer v : currentSet) {
            if (aux[v] < INCLUDED) {
                mustBeIncluded.add(v);
                aux[v] = INCLUDED;
            }
        }

        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
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
                }
            }
            aux[verti] = PROCESSED;
            cont++;
        }
        return cont;
    }

    public OperationConvexityGraphResult hsp3(UndirectedSparseGraphTO<Integer, Integer> graphRead, Collection<Integer> set) {
        int[] arr = new int[set.size()];
        int i = 0;
        for (Integer v : set) {
            arr[i] = v;
            i++;
        }
        return hsp3(graphRead, arr);
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

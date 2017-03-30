package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GraphCaratheodoryHeuristicV2
        extends GraphCaratheodoryHeuristic {

    static final String description = "Nº Caratheodory (Heuristic v2)";

    public static final int INCLUDED = 2;
    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;

    static boolean verbose = true;

    @Override
    public String getName() {
        return description;
    }

    @Override
    public Integer selectBestPromotableVertice(Set<Integer> s,
            Integer partial, Set<Integer> promotable,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        Integer bestVertex = null;
        Integer bestRanking = null;
        int vertexCount = graph.getVertexCount();
        if (verbose) {
            System.out.print("Aux        ");
            printArrayAux(aux);

            System.out.print(String.format("bfs(%2d)    ", partial));
            System.out.print(" = {");
            for (int i = 0; i < vertexCount; i++) {
                int distance = bdl.getDistance(graph, i);
                System.out.printf("%2d | ", distance);
            }
            System.out.println("}");
        }

        for (Integer vtmp : promotable) {
            Collection neighbors = new HashSet(graph.getNeighbors(vtmp));
            neighbors.removeAll(s);
            neighbors.remove(partial);

            for (int i = 0; i < aux.length; i++) {
                if (aux[i] >= INCLUDED) {
                    neighbors.remove(i);
                }
            }
            Integer vtmpRanking = neighbors.size();
            if (bestVertex == null || (vtmpRanking >= 2 && vtmpRanking < bestRanking)) {
                bestRanking = vtmpRanking;
                bestVertex = vtmp;
            }
        }
        return bestVertex;
    }

    @Override
    public Integer selectBestNeighbor(Integer v, UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux, Integer partial, int[] auxBackup) {
        Integer ret = null;
        int vertexCount = graph.getVertexCount();

        if (verbose) {
            System.out.print("Aux        ");
            printArrayAux(aux);

            System.out.print(String.format("bfs(%2d)    ", v));
            System.out.print(" = {");
            for (int i = 0; i < vertexCount; i++) {
                int distance = bdl.getDistance(graph, i);
                System.out.printf("%2d | ", distance);
            }
            System.out.println("}");

            if (auxBackup != null) {
                System.out.print("AuxVp      ");
                printArrayAux(auxBackup);
            }
        }

        Set<Integer> neighbors = new HashSet<>(graph.getNeighbors(v));
        neighbors.remove(partial);
        neighbors.remove(v);
        Integer ranking = null;
        for (int i = 0; i < aux.length; i++) {
            if (aux[i] >= 2 || (auxBackup != null && auxBackup[i] >= 2)) {
                neighbors.remove(i);
            }
        }

        for (Integer nei : neighbors) {
            int neiRanking = aux[nei] * 100 + graph.degree(nei);
            if (ret == null || neiRanking < ranking) {
                ret = nei;
                ranking = neiRanking;
            }
        }
        return ret;
    }
}

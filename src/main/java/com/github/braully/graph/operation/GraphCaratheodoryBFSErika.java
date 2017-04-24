package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphCaratheodoryBFSErika
        extends GraphCheckCaratheodorySet
        implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "Nº Caratheodory (BFS Bloco Erika)";

    public static boolean verbose = true;

//    public static boolean verbose = false;
    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        long totalTimeMillis = -1;

        totalTimeMillis = System.currentTimeMillis();
        Set<Integer> caratheodorySet = buildMaxCaratheodorySet(graphRead);
        totalTimeMillis = System.currentTimeMillis() - totalTimeMillis;

        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();
        if (!caratheodorySet.isEmpty()) {
            graphRead.setSet(caratheodorySet);
            response = super.doOperation(graphRead);
        }
        return response;
    }

    public Set<Integer> buildMaxCaratheodorySet(UndirectedSparseGraphTO<Integer, Integer> graph) {
        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
        Set<Integer> caratheodorySet = new HashSet<>();
        Collection<Integer> vertices = graph.getVertices();
        int vertexCount = graph.getVertexCount();

        System.out.printf("V(G)    = {");
        for (int i = 0; i < vertexCount; i++) {
            System.out.printf("%2d | ", i);
        }
        System.out.println("}");

        for (Integer v : vertices) {
            int[] lv1 = new int[vertexCount];
            int[] lv2 = new int[vertexCount];
            bdl.labelDistances(graph, v);
            System.out.printf("bfs(%2d) = {", v);
            for (int i = 0; i < vertexCount; i++) {
                int distance = bdl.getDistance(graph, i);
                System.out.printf("%2d | ", distance);
            }

            for (int w = 0; w < vertexCount; w++) {
                //w é folha
                if (bdl.getDistance(graph, w) == 1) {
                    lv1[w] = 1;
                    lv2[w] = Integer.MIN_VALUE;
                }
            }
            System.out.println("}");
        }
        return caratheodorySet;
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

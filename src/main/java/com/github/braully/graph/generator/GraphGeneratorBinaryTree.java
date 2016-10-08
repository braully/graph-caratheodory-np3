package com.github.braully.graph.generator;

import com.github.braully.graph.hn.UndirectedSparseGraphTO;
import java.util.ArrayDeque;
import java.util.Queue;

public class GraphGeneratorBinaryTree implements IGraphGenerator {

    static final String name = "binary";
    static final String description = "Binary Tree";

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UndirectedSparseGraphTO<Integer, Integer> generateGraph(Integer nvertices, Integer minDegree, Double maxDegree) {
        double lognv = Math.log(nvertices + 1) / Math.log(2);
        double pow = Math.pow(2, Math.ceil(lognv)) - 1;
        int nvert = (int) pow;
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        Queue<Integer> frontier = new ArrayDeque<>();
        graph.addVertex(0);
        int countEdge = 0;
        int countVertice = 1;
        frontier.add(0);

        while (!frontier.isEmpty() && countVertice < nvert) {
            Integer verti = frontier.remove();
            Integer target1 = countVertice++;
            Integer target2 = countVertice++;
            graph.addEdge(countEdge++, verti, target1);
            graph.addEdge(countEdge++, verti, target2);
            if (countVertice < nvert) {
                frontier.add(target1);
                frontier.add(target2);
            }
        }
        return graph;
    }
}

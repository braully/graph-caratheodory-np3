package com.github.braully.graph.generator;

import com.github.braully.graph.hn.UndirectedSparseGraphTO;

public class GraphGeneratorCycle extends GraphGeneratorPath {

    static final String name = "cycle";
    static final String description = "Cycle";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public UndirectedSparseGraphTO<Integer, Integer> generateGraph(Integer nvertices, Integer minDegree, Double maxDegree) {
        UndirectedSparseGraphTO<Integer, Integer> graph = super.generateGraph(nvertices, minDegree, maxDegree);
        graph.addEdge(graph.getEdgeCount(), graph.getVertexCount() - 1, 0);
        return graph;
    }
}

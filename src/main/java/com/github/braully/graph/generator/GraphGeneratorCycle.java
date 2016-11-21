package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Map;

public class GraphGeneratorCycle extends GraphGeneratorPath {
    
    static final String description = "Cycle";
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public UndirectedSparseGraphTO<Integer, Integer> generateGraph(Map parameters) {
        UndirectedSparseGraphTO<Integer, Integer> graph = super.generateGraph(parameters);
        graph.setName("C" + N_VERTICES);
        graph.addEdge(graph.getEdgeCount(), graph.getVertexCount() - 1, 0);
        return graph;
    }
}

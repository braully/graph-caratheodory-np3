package com.github.braully.graph.generator;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.List;

public class GraphGeneratorPath implements IGraphGenerator {

    static final String name = "path";
    static final String description = "Path";

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
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        List<Integer> vertexElegibles = new ArrayList<>(nvertices);
        Integer[] vertexs = new Integer[nvertices];
        for (int i = 0; i < nvertices; i++) {
            vertexElegibles.add(i);
            vertexs[i] = i;
            graph.addVertex(vertexs[i]);
        }
        int countEdge = 0;
        for (int i = 0; i < nvertices - 1; i++) {
            Integer source = vertexs[i];
            Integer target = vertexs[i] + 1;
            graph.addEdge(countEdge++, source, target);
        }
        return graph;
    }
}

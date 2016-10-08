package com.github.braully.graph.generator;

import com.github.braully.graph.hn.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.List;

public class GraphGeneratorRandom implements IGraphGenerator {

    static final String name = "random";
    static final String description = "Random";

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
        int[] degree = new int[nvertices];
        for (int i = 0; i < nvertices; i++) {
            vertexElegibles.add(i);
            vertexs[i] = i;
            degree[i] = 0;
            graph.addVertex(vertexs[i]);
        }
        int countEdge = 0;
        double offset = maxDegree - minDegree;
        for (int i = nvertices - 1; i > 0; i--) {
            long limite = minDegree + Math.round(Math.random() * (offset));
            int size = vertexElegibles.size();
            Integer source = vertexs[i];
            for (int j = 0; j <= limite; j++) {
                //Exclude last element from choose (no loop)
                Integer target = null;
                if (vertexElegibles.size() > 1) {
                    int vrandom = (int) Math.round(Math.random() * (size - 2));
                    target = vertexElegibles.get(vrandom);
                    if (graph.addEdge(countEdge++, source, target)) {
                        if (degree[target]++ >= maxDegree) {
                            vertexElegibles.remove(target);
                        }
                        if (degree[source]++ >= maxDegree) {
                            vertexElegibles.remove(source);
                        }
                    }
                    size = vertexElegibles.size();
                } else {
                    int vrandom = (int) Math.round(Math.random() * (nvertices - 1));
                    target = vertexs[vrandom];
                    graph.addEdge(countEdge++, source, target);
                }
            }
        }
        return graph;
    }
}

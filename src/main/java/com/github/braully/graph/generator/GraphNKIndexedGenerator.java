package com.github.braully.graph.generator;

import com.github.braully.graph.CombinationsFacade;
import com.github.braully.graph.UndirectedSparseGraphTO;
import static com.github.braully.graph.generator.GraphGeneratorCompleteBipartite.P_VERTICES;
import static com.github.braully.graph.generator.GraphGeneratorKP.K_VERTICES;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

public class GraphNKIndexedGenerator extends AbstractGraphGenerator {

    static final Logger log = Logger.getLogger(GraphNKIndexedGenerator.class);

    static final String N_VERTICES = "N";
    static final String P_EDEGES = "M";
    static final String NK_INDEX = "Index";
    static final String[] parameters = {K_VERTICES, P_EDEGES, NK_INDEX};
    static final String description = "N,M-Indexed";
    static final Integer DEFAULT_NVERTICES = 5;
    static final Integer DEFAULT_PEDEGES = 2;
    static final Integer DEFAULT_NK_INDEX = 0;

    @Override
    public String[] getParameters() {
        return parameters;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public UndirectedSparseGraphTO<Integer, Integer> generateGraph(Map parameters) {
        Integer nvertices = getIntegerParameter(parameters, N_VERTICES);
        Integer pvertices = getIntegerParameter(parameters, P_VERTICES);
        Integer nkindex = getIntegerParameter(parameters, NK_INDEX);

        if (nvertices == null) {
            nvertices = DEFAULT_NVERTICES;
        }
        if (pvertices == null) {
            pvertices = DEFAULT_PEDEGES;
        }
        if (nkindex == null) {
            nkindex = DEFAULT_NK_INDEX;
        }

        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        String name = "N" + nvertices + ",M" + pvertices + "-Indexed" + nkindex;
        graph.setName(name);

        List<Integer> vertexElegibles = new ArrayList<>(nvertices);
        Integer[] vertexs = new Integer[nvertices];
        for (int i = 0; i < nvertices; i++) {
            vertexElegibles.add(i);
            vertexs[i] = i;
            graph.addVertex(vertexs[i]);
        }

        int maxEdges = (nvertices * (nvertices - 1)) / 2;
        int[] combination = CombinationsFacade.getCombinationNKByLexicographIndex(maxEdges, pvertices, nkindex);
        log.info("Max Edges: " + maxEdges);

        if (combination != null && combination.length > 0) {
            Set<Integer> edges = new HashSet<Integer>();
            for (int e : combination) {
                edges.add(e);
            }
            log.info("Edges Combinations: " + edges);

            int countEdge = 0;
            for (int i = 0; i < nvertices; i++) {
                for (int j = i; j < nvertices - 1; j++) {
                    if (edges.contains(countEdge)) {
                        Integer source = vertexs[i];
                        Integer target = vertexs[j] + 1;
                        graph.addEdge(source, target);
                    }
                    countEdge++;
                }
            }
        }
        log.info("Graph: " + name);
        return graph;
    }
}

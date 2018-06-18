package com.github.braully.graph;

import java.util.Arrays;
import junit.framework.TestCase;

/**
 *
 * @author strike
 */
public class GraphStructureTest extends TestCase {

    public void testContainsSubgraph() {
        UndirectedSparseGraphTO graph = new UndirectedSparseGraphTO();
        UndirectedSparseGraphTO subgraph = new UndirectedSparseGraphTO();
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);

        subgraph.addEdge(0, 1);
        subgraph.addEdge(1, 3);
        subgraph.addEdge(2, 3);
        subgraph.addEdge(3, 1);

        assertTrue(graph.containStrict(subgraph, Arrays.asList(new Integer[]{0, 1, 3, 2})));

        assertFalse(graph.containStrict(subgraph));
    }
}

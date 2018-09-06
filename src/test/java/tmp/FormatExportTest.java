/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import edu.uci.ics.jung.graph.util.Pair;
import junit.framework.TestCase;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.junit.*;

import java.io.*;
import java.util.Collection;
import org.jgrapht.io.Graph6Sparse6Exporter;

/**
 *
 * @author braully
 */
public class FormatExportTest extends TestCase {

    @Test
    public void testRandomGraphsG6() throws Exception {

        Graph<Integer, Integer> g = new SimpleGraph(DefaultEdge.class);

        UndirectedSparseGraphTO<Integer, Integer> loadGraphES = UtilGraph.loadGraphES(new FileInputStream("/home/braully/Nuvem/Workspace-nuvem/ultimo-grafo-de-moore.es"));
        for (Integer v : (Collection<Integer>) loadGraphES.getVertices()) {
            g.addVertex(v);
        }
        for (Integer e : (Collection<Integer>) loadGraphES.getEdges()) {
            Pair<Integer> endpoints = loadGraphES.getEndpoints(e);
            g.addEdge(endpoints.getFirst(), endpoints.getSecond());
        }
        String res = exportGraph(g, Graph6Sparse6Exporter.Format.GRAPH6);
        System.out.println(res);
    }

    private <V, E> String exportGraph(Graph<V, E> g, Graph6Sparse6Exporter.Format format)
            throws Exception {

        Graph6Sparse6Exporter<V, E> exporter = new Graph6Sparse6Exporter<>(format);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        exporter.exportGraph(g, os);
        return new String(os.toByteArray(), "UTF-8");
    }
}

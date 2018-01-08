package com.github.braully.graph.hn;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import com.github.braully.graph.operation.GraphStatistics;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;

/**
 *
 * @author strike
 */
public class GeracaoGrafoMooreTest extends TestCase {

    public GeracaoGrafoMooreTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConverterFronJson() throws IOException {
        Integer nvertices = 5;

        // Criação do grafo em branco
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        graph.setName("BGraph");

        Set<Integer> verticesC5 = new HashSet<>();

        //Geração do C5
        List<Integer> vertexElegibles = new ArrayList<>(nvertices);
        Integer[] vertexs = new Integer[nvertices];
        for (int i = 0; i < nvertices; i++) {
            vertexElegibles.add(i);
            vertexs[i] = i;
            graph.addVertex(vertexs[i]);
            verticesC5.add(i);
        }
        int countEdge = 0;
        for (int i = 0; i < nvertices - 1; i++) {
            Integer source = vertexs[i];
            Integer target = vertexs[i] + 1;
            graph.addEdge(countEdge++, source, target);
        }
        graph.addEdge(countEdge++, graph.getVertexCount() - 1, 0);
        int limite = 3250;
        //Criação da instabilidade
        graph.addVertex(5);
        graph.addEdge(countEdge++, 0, 5);

        int pai1 = 5;
        int pai2 = 5;

        int pendencia1 = 6;
        graph.addVertex(pendencia1);
        graph.addEdge(countEdge++, pai1, pendencia1);
        graph.addEdge(countEdge++, pendencia1, 3);

        int pendencia2 = 7;
        graph.addVertex(pendencia2);
        graph.addEdge(countEdge++, pai2, pendencia2);
        graph.addEdge(countEdge++, pendencia2, 2);

        Set<Integer> vertPendente = new HashSet<>(verticesC5);

        int vertexCont = graph.getVertexCount();

        //Adição de vértices extras até o limite estabelecido
        while (vertexCont < limite) {
            vertPendente.addAll(verticesC5);

            for (Integer v : (Collection<Integer>) graph.getNeighbors(pendencia1)) {
                vertPendente.remove(v);
                vertPendente.removeAll(graph.getNeighbors(v));
            }

            if (vertPendente.size() != 1) {
                System.err.println("Falha na Pendencia vertice " + pendencia1 + " " + vertPendente);
                throw new IllegalStateException();
            }

            System.out.println("Pendencia vertice " + pendencia1 + " " + vertPendente);
            Integer vertTargetC5 = vertPendente.iterator().next();
            pai1 = pendencia1;
            pendencia1 = vertexCont++;
            graph.addVertex(pendencia1);
            graph.addEdge(countEdge++, pai1, pendencia1);
            graph.addEdge(countEdge++, pendencia1, vertTargetC5);

            vertPendente.addAll(verticesC5);

            for (Integer v : (Collection<Integer>) graph.getNeighbors(pendencia2)) {
                vertPendente.remove(v);
                vertPendente.removeAll(graph.getNeighbors(v));
            }

            if (vertPendente.size() != 1) {
                System.err.println("Falha na Pendencia vertice " + pendencia2 + " " + vertPendente);
                throw new IllegalStateException();
            }

            vertTargetC5 = vertPendente.iterator().next();
            pai2 = pendencia2;
            pendencia2 = vertexCont++;
            graph.addVertex(pendencia2);
            graph.addEdge(countEdge++, pai2, pendencia2);
            graph.addEdge(countEdge++, pendencia2, vertTargetC5);
            System.out.println("Pendencia vertice " + pendencia2 + " " + vertPendente);
        }
        try {
            System.out.println("Saving file");
            FileWriter fileWriter = new FileWriter("/home/braully/tmp.csr");
            UtilGraph.writerGraphToCsr(fileWriter, graph);
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Statistics");
            GraphStatistics operation = new GraphStatistics();
            Map<String, Object> result = operation.doOperation(graph);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

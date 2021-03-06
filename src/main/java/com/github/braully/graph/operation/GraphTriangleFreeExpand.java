package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import com.github.braully.graph.generator.GraphGeneratorMTF;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.log4j.Logger;

public class GraphTriangleFreeExpand implements IGraphOperation {

    static final String type = "Graph Class";
    static final String description = "Triangle-Free Expand Infinite (Java)";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {

        /* Processar a buscar pelo hullset e hullnumber */
        final GraphCaratheodoryExistsSetOfSize caratheodoryExistsSetOfSize = new GraphCaratheodoryExistsSetOfSize();
        Map<String, Object> response = new HashMap<>();

        GraphGeneratorMTF generatorMTF = new GraphGeneratorMTF() {
            @Override
            public void observerGraph(UndirectedSparseGraphTO graphProcessing) {
                boolean cart4 = false;
                OperationConvexityGraphResult findCaratheodroySetBruteForce = caratheodoryExistsSetOfSize.findCaratheodroySetBruteForce(graphProcessing, 4);
                if (findCaratheodroySetBruteForce != null) {
                    cart4 = true;
//                    System.out.println("Caratheodory size 4 found: " + findCaratheodroySetBruteForce.caratheodorySet);
                } else {
//                    System.out.println("Caratheodory size 4 NOT found");
                }
                findCaratheodroySetBruteForce = caratheodoryExistsSetOfSize.findCaratheodroySetBruteForce(graphProcessing, 5);
                if (findCaratheodroySetBruteForce != null) {
                    System.out.println("******************************************************************************");
                    System.out.println("Graph: " + graphProcessing);
                    System.out.println("Caratheodory size 5 found: " + findCaratheodroySetBruteForce.caratheodorySet);
                    System.out.println("******************************************************************************");
                    System.out.println("===================== ERROOOOOOOOOOORRRRRRRRRR ================================");

                    try {
                        String graphName = graphProcessing.getName() + ".csr";
                        File graphFile = new File(graphName);
                        FileWriter fileWriter = new FileWriter(graphFile);
                        UtilGraph.writerGraphToAdjMatrix(fileWriter, graphProcessing);
                        fileWriter.flush();
                        fileWriter.close();
                        System.out.println("Save on: " + graphFile.getPath());
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(GraphTriangleFreeExpand.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    System.exit(-1);
                } else {
//                    System.out.println("Caratheodory size 5 NOT found");
                }
                if (cart4) {
                    this.interrupt();
                }
            }
        };
        try {
            boolean erro = false;
            UndirectedSparseGraphTO lastGraph = graph;
            while (!erro) {
                generatorMTF.addNewVertice(lastGraph);
                lastGraph = generatorMTF.getLastGraph();

                String graphName = lastGraph.getName() + "-v" + lastGraph.getVertexCount() + "-f" + generatorMTF.getCount() + ".mat";
                File graphFile = new File("/home/strike/Dropbox/documentos/mestrado/estudo-mtf/todos-mtf-carat4/descendente/", graphName);
                FileWriter fileWriter = new FileWriter(graphFile);
                UtilGraph.writerGraphToAdjMatrix(fileWriter, lastGraph);
                fileWriter.flush();
                fileWriter.close();

                System.out.println("Child size of " + (lastGraph.getVertexCount()) + "...  OK");
                System.out.println("\tTrying child size of " + (lastGraph.getVertexCount() + 1));
            }
        } catch (Exception ex) {
            log.error(null, ex);
        }
        return response;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

}

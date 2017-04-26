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

public class GraphTriangleFreeNext implements IGraphOperation {

    static final String type = "Graph Class";
    static final String description = "Triangle-Free Next (Java)";

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
                    System.out.println("Caratheodory size 4 found: " + findCaratheodroySetBruteForce.caratheodorySet);
                } else {
                    System.out.println("Caratheodory size 4 NOT found");
//                    try {
//                        String fileName = graphProcessing.getName() + "-f" + getCount() + "-nao-carat-4.mat";
//                        File file = new File("/home/strike/Dropbox/documentos/mestrado/estudo-mtf/todos-mtf-carat4/filhos-nao-c4/", fileName);
//                        FileWriter fileWriter = new FileWriter(file);
//                        graphProcessing.setName(graphProcessing.getName() + "-f" + getCount() + "-nao-carat-4");
//                        UtilGraph.writerGraphToAdjMatrix(fileWriter, graphProcessing);
//                        fileWriter.flush();
//                        fileWriter.close();
//                    } catch (IOException ex) {
//                        java.util.logging.Logger.getLogger(GraphTriangleFreeNext.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }

                findCaratheodroySetBruteForce = caratheodoryExistsSetOfSize.findCaratheodroySetBruteForce(graphProcessing, 5);
                if (findCaratheodroySetBruteForce != null) {
                    System.out.println("******************************************************************************");
                    System.out.println("Graph: " + graphProcessing);
                    System.out.println("Caratheodory size 5 found: " + findCaratheodroySetBruteForce.caratheodorySet);
                    System.out.println("******************************************************************************");
                    System.out.println("===================== ERROOOOOOOOOOORRRRRRRRRR ================================");
                    System.exit(-1);
                } else {
                    System.out.println("Caratheodory size 5 NOT found");
                }
                if (cart4) {
//                    try {
//                        String fileName = graphProcessing.getName() + "-f" + getCount() + "-carat-4.mat";
//                        File file = new File("/home/strike/Dropbox/documentos/mestrado/estudo-mtf/todos-mtf-carat4/filhos-c4/", fileName);
//                        FileWriter fileWriter = new FileWriter(file);
//                        graphProcessing.setName(graphProcessing.getName() + "-f" + getCount() + "-carat-4");
//                        UtilGraph.writerGraphToAdjMatrix(fileWriter, graphProcessing);
//                        fileWriter.flush();
//                        fileWriter.close();
//                    } catch (IOException ex) {
//                        java.util.logging.Logger.getLogger(GraphTriangleFreeNext.class.getName()).log(Level.SEVERE, null, ex);
//                    }
                }
            }
        };
        try {
            generatorMTF.addNewVertice(graph);
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

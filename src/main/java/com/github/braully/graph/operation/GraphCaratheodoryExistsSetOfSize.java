package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Map;

/**
 *
 * @author strike
 */
public class GraphCaratheodoryExistsSetOfSize extends GraphCalcCaratheodoryNumber {

    static final String type = "P3-Convexity";
    static final String description = "Caratheodory Set of Size  (Java)";

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        OperationGraphResult processedCaratheodroySet = null;
        Map<String, Object> result = null;
        if (graph == null) {
            return result;
        }

        Integer size = null;

        try {
            String inputData = graph.getInputData();
            size = Integer.parseInt(inputData);
        } catch (Exception e) {

        }
        if (size == null) {
            throw new IllegalArgumentException("Input invalid (not integer): " + graph.getInputData());
        }

        if (size >= 2) {
            processedCaratheodroySet = findCaratheodroySetBruteForce(graph, size);
        }

        if (processedCaratheodroySet == null) {
            processedCaratheodroySet = new OperationGraphResult();
        }
        return processedCaratheodroySet.toMap();
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import com.github.braully.graph.hn.UndirectedSparseGraphTO;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.math3.util.CombinatoricsUtils;

/**
 *
 * @author strike
 */
public class GraphCaratheodoryExistsSetOfSize extends GraphCalcCaratheodoryNumber {

    static final String name = "caratheodoryjsize";
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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}

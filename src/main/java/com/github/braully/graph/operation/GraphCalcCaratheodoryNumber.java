/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.math3.util.CombinatoricsUtils;

/**
 *
 * @author strike
 */
public class GraphCalcCaratheodoryNumber extends GraphCheckCaratheodorySet {

    static final String name = "ncaratheodoryj";
    static final String description = "Nº Caratheodory (Java)";

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        OperationGraphResult processedCaratheodroySet = null;
        Map<String, Object> result = null;
        if (graph == null) {
            return result;
        }
        int maxSizeSet = (graph.getVertexCount() + 1) / 2;
        int currentSize = maxSizeSet;

        while (currentSize >= 2) {
            processedCaratheodroySet = findCaratheodroySetBruteForce(graph, currentSize);
            if (processedCaratheodroySet != null
                    && processedCaratheodroySet.caratheodorySet != null
                    && !processedCaratheodroySet.caratheodorySet.isEmpty()) {
                result = new HashMap<>();
                result.putAll(processedCaratheodroySet.toMap());
                break;
            }
            currentSize--;
        }
        return result;
    }

    OperationGraphResult findCaratheodroySetBruteForce(UndirectedSparseGraphTO<Integer, Integer> graph, int currentSize) {
        OperationGraphResult processedHullSet = null;
        if (graph == null || graph.getVertexCount() <= 0) {
            return processedHullSet;
        }
        Collection vertices = graph.getVertices();
        int veticesCount = vertices.size();
        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(graph.getVertexCount(), currentSize);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            OperationGraphResult hsp3g = hsp3(graph, currentSet);

            if (hsp3g != null) {
                processedHullSet = hsp3g;
                break;
            }
        }
        return processedHullSet;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}

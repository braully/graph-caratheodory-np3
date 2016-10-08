/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import static com.github.braully.graph.operation.GraphCheckCaratheodorySet.PARAM_NAME_AUX_PROCESS;
import static com.github.braully.graph.operation.GraphCheckCaratheodorySet.PARAM_NAME_CARATHEODORY_NUMBER;
import static com.github.braully.graph.operation.GraphCheckCaratheodorySet.PARAM_NAME_CARATHEODORY_SET;
import static com.github.braully.graph.operation.GraphCheckCaratheodorySet.PARAM_NAME_CONVEX_HULL;
import static com.github.braully.graph.operation.GraphCheckCaratheodorySet.PARAM_NAME_PARTIAL_DERIVATED;
import static com.github.braully.graph.operation.GraphCheckCaratheodorySet.PARAM_NAME_TOTAL_TIME_MS;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author strike
 */
public class OperationGraphResult {

    Set<Integer> caratheodorySet;
    Set<Integer> convexHull;
    int[] auxProcessor;
    Set<Integer> partial;
    Long totalTimeMillis;
    Integer caratheodoryNumber;

    Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put(PARAM_NAME_CARATHEODORY_NUMBER, caratheodoryNumber);
        result.put(PARAM_NAME_CARATHEODORY_SET, caratheodorySet);
        result.put(PARAM_NAME_CONVEX_HULL, convexHull);
        result.put(PARAM_NAME_AUX_PROCESS, auxProcessor);
        result.put(PARAM_NAME_TOTAL_TIME_MS, (double) ((double) totalTimeMillis / 1000));
        result.put(PARAM_NAME_PARTIAL_DERIVATED, partial);
        return result;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author strike
 */
public class OperationConvexityGraphResult {

    public static final String PARAM_NAME_CARATHEODORY_NUMBER = "Caratheodroy number(c)";
    public static final String PARAM_NAME_CARATHEODORY_SET = "Caratheodroy set(S)";
    public static final String PARAM_NAME_CONVEX_HULL = "Convex hull(H(S))";
//    public static final String PARAM_NAME_AUX_PROCESS = "aux";
    public static final String PARAM_NAME_TOTAL_TIME_MS = "Time(s)";
    public static final String PARAM_NAME_PARTIAL_DERIVATED = "∂H(S)=H(S)\\⋃p∈SH(S\\{p})";

    Set<Integer> caratheodorySet;
    Set<Integer> convexHull;
    int[] auxProcessor;
    Set<Integer> partial;
    long totalTimeMillis;
    Integer caratheodoryNumber;

    Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        if (caratheodoryNumber != null && caratheodoryNumber > 0) {
            result.put(PARAM_NAME_CARATHEODORY_NUMBER, caratheodoryNumber);
        } else {
            result.put(PARAM_NAME_CARATHEODORY_NUMBER, -1);
        }
        if (caratheodorySet != null && !caratheodorySet.isEmpty()) {
            result.put(PARAM_NAME_CARATHEODORY_SET, caratheodorySet);
        } else {
            result.put(PARAM_NAME_CARATHEODORY_SET, "∄");
        }
        result.put(PARAM_NAME_CONVEX_HULL, convexHull);
//        result.put(PARAM_NAME_AUX_PROCESS, auxProcessor);
        if (totalTimeMillis > 0) {
            result.put(PARAM_NAME_TOTAL_TIME_MS, (double) ((double) totalTimeMillis / 1000));
        }
        if (partial != null && !partial.isEmpty()) {
            result.put(PARAM_NAME_PARTIAL_DERIVATED, partial);
        }
        return result;
    }
}
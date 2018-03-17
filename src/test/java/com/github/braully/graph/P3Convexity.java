/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import static com.github.braully.graph.operation.GraphCheckCaratheodorySet.INCLUDED;
import static com.github.braully.graph.operation.GraphCheckCaratheodorySet.NEIGHBOOR_COUNT_INCLUDED;
import static com.github.braully.graph.operation.GraphCheckCaratheodorySet.PROCESSED;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author strike
 */
public class P3Convexity {

    public Set<Integer> convexHullP3(UndirectedSparseGraph<Integer, Integer> graph, Set<Integer> S) {
        int currentSetSize = 0;
        Set<Integer> convexHullP3 = new HashSet<>();
        int[] aux = new int[graph.getVertexCount()];
        int[] auxc = new int[graph.getVertexCount()];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
            auxc[i] = 0;
        }
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer v : S) {
            mustBeIncluded.add(v);
            aux[v] = INCLUDED;
            auxc[v] = 1;
            currentSetSize++;
        }
        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
            convexHullP3.add(verti);
            Collection<Integer> neighbors = graph.getNeighbors(verti);

            for (int vertn : neighbors) {
                if (vertn == verti) {
                    continue;
                }
                if (vertn != verti && aux[vertn] < INCLUDED) {
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (aux[vertn] == INCLUDED) {
                        mustBeIncluded.add(vertn);
                    }
                    auxc[vertn] = auxc[vertn] + auxc[verti];
                }
            }
            aux[verti] = PROCESSED;
        }
        return convexHullP3;
    }
}

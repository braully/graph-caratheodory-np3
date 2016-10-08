package com.github.braully.graph.generator;

import edu.uci.ics.jung.graph.AbstractGraph;

/**
 *
 * @author strike
 */
public interface IGraphGenerator {

    public String getName();

    public String getDescription();

    public AbstractGraph<Integer, Integer> generateGraph(Integer nvertices, Integer minDegree, Double maxDegree);
}

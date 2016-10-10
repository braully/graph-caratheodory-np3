package com.github.braully.graph.generator;

import edu.uci.ics.jung.graph.AbstractGraph;

/**
 *
 * @author strike
 */
public interface IGraphGenerator {

    /**
     * Name unique (key) of graph generator.
     *
     * @return
     */
    public String getName();

    /**
     * Description in Visual representation of Generator.
     *
     * @return
     */
    public String getDescription();

    /**
     * Generate um graph of this type with nvertices.
     *
     * @param nvertices
     * @param minDegree
     * @param maxDegree
     * @return
     */
    public AbstractGraph<Integer, Integer> generateGraph(Integer nvertices, Integer minDegree, Double maxDegree);
}

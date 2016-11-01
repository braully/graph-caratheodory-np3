/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Map;

/**
 * Graph operation.
 *
 * @author braully
 */
public interface IGraphOperation {

    public String getTypeProblem();

    public String getName();

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph);
}

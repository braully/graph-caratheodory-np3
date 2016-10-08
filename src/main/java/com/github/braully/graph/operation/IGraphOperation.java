/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import com.github.braully.graph.hn.UndirectedSparseGraphTO;
import java.util.Map;

/**
 *
 * @author strike
 */
public interface IGraphOperation {

    public String getName();

    public String getDescription();

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph);
}

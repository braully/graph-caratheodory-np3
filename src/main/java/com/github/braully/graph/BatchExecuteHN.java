/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.github.braully.graph.operation.GraphHullNumber;
import com.github.braully.graph.operation.IGraphOperation;

/**
 *
 * @author strike
 */
public class BatchExecuteHN extends BatchExecuteG6 {

    static final IGraphOperation[] operations = new IGraphOperation[]{new GraphHullNumber()};

    @Override
    public String getDefaultInput() {
        return "/home/strike/grafos-para-processar/almhypo";
    }

    public static void main(String... args) {
        BatchExecuteHN executor = new BatchExecuteHN();
        executor.processMain(args);
    }

    @Override
    public IGraphOperation[] getOperations() {
        return operations;
    }
}

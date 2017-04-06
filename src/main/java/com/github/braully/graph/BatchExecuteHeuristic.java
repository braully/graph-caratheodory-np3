package com.github.braully.graph;

import com.github.braully.graph.operation.GraphCaratheodoryHeuristic;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristicV2;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristicV3;
import com.github.braully.graph.operation.IGraphOperation;

/**
 *
 * @author strike
 */
public class BatchExecuteHeuristic extends BatchExecuteG6 {

    static final IGraphOperation[] operations = new IGraphOperation[]{
        new GraphCaratheodoryHeuristic(),
        new GraphCaratheodoryHeuristicV2(),
        new GraphCaratheodoryHeuristicV3()};

    @Override
    public String getDefaultInput() {
        return "/home/strike/grafos-para-processar/almhypo";
    }

    public static void main(String... args) {
        BatchExecuteHeuristic executor = new BatchExecuteHeuristic();
        executor.processMain(args);
    }

    @Override
    public IGraphOperation[] getOperations() {
        return operations;
    }
}

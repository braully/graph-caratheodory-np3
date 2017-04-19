package com.github.braully.graph;

import com.github.braully.graph.operation.GraphCalcCaratheodoryNumberBinaryStrategy;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristic;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristicV2;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristicV3;
import com.github.braully.graph.operation.IGraphOperation;

/**
 *
 * @author strike
 */
public class BatchExecuteHeuristicCompare extends BatchExecuteG6 {

    static final IGraphOperation[] operations = new IGraphOperation[]{
        new GraphCalcCaratheodoryNumberBinaryStrategy(),
        new GraphCaratheodoryHeuristic(),
        new GraphCaratheodoryHeuristicV2(),
        new GraphCaratheodoryHeuristicV3()};

    @Override
    public String getDefaultInput() {
        return "/home/strike/Documentos/grafos-processamento/Highly_irregular/highlyirregular/";
    }

    public static void main(String... args) {
        BatchExecuteHeuristicCompare executor = new BatchExecuteHeuristicCompare();
        executor.processMain(args);
    }

    @Override
    public IGraphOperation[] getOperations() {
        return operations;
    }
}

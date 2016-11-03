/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import java.io.File;

/**
 *
 * @author strike
 */
public class GraphCalcCaratheodoryNumberCSerialBinary extends GraphCalcCaratheodoryNumberParallel {

    private static final String COMMAND_GRAPH_HN = System.getProperty("user.home") + File.separator + "graph-caratheodory-np3.sh";

    static final String description = "Nº Caratheodory (C Binary Strategy)";

    @Override
    public String getName() {
        return description;
    }

    @Override
    String getExecuteCommand(String path) {
        return COMMAND_GRAPH_HN + " -sb " + path;
    }
}

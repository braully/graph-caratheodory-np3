/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.github.braully.graph.operation.IGraphOperation;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author strike
 */
public class ExecuteOperation extends Thread {

    private static final Logger log = Logger.getLogger(ExecuteOperation.class.getName());
    /* */
    private IGraphOperation graphOperation;
    private UndirectedSparseGraphTO graph;
    private Map<String, Object> result = null;

    private boolean processing = false;

    @Override
    public void run() {
        try {
            log.log(Level.INFO, "[START]");
            processing = true;
            result = graphOperation.doOperation(graph);
            log.log(Level.INFO, "[FINISH]");
            log.log(Level.INFO, "[RESULT]");
            log.log(Level.INFO, result.toString());
        } catch (Exception e) {
            log.log(Level.INFO, "[FAILED]", e);
        } finally {
            processing = false;
        }
    }

    public IGraphOperation getGraphOperation() {
        return graphOperation;
    }

    public void setGraphOperation(IGraphOperation graphOperation) {
        this.graphOperation = graphOperation;
    }

    public UndirectedSparseGraphTO getGraph() {
        return graph;
    }

    public void setGraph(UndirectedSparseGraphTO graph) {
        this.graph = graph;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public boolean isProcessing() {
        return processing;
    }
}

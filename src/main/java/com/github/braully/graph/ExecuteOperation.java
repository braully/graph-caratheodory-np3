/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.github.braully.graph.operation.IGraphOperation;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author strike
 */
public class ExecuteOperation extends Thread {

    private static final Logger log = Logger.getLogger("WEBCONSOLE");
    /* */
    private IGraphOperation graphOperation;
    private UndirectedSparseGraphTO graph;
    private Map<String, Object> result = null;

    private boolean processing = false;

    @Override
    public void run() {
        try {
            WebConsoleAppender.clear();
            log.info("[START]");
            processing = true;
            result = graphOperation.doOperation(graph);
            log.info("[FINISH]");
            log.info("[RESULT]");
            log.info(result.toString());
        } catch (Exception e) {
            log.info("[FAILED]", e);
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

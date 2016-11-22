/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.github.braully.graph.operation.IGraphOperation;
import com.github.braully.graph.operation.OperationConvexityGraphResult;
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
            processing = true;
            log.info("[START]");
            log.info(graphOperation.getTypeProblem() + " : " + graphOperation.getName());
            long currentTimeMillis = System.currentTimeMillis();
            result = graphOperation.doOperation(graph);
            currentTimeMillis = System.currentTimeMillis() - currentTimeMillis;
            if (result != null) {
                log.info(result.toString());
                if (result.get(OperationConvexityGraphResult.PARAM_NAME_TOTAL_TIME_MS) == null) {
                    result.put(OperationConvexityGraphResult.PARAM_NAME_TOTAL_TIME_MS, (double) ((double) currentTimeMillis / 1000));
                }
                DatabaseFacade.saveResult(graph, graphOperation, result);
            }
        } catch (Exception e) {
            log.info("[FAILED]", e);
        } finally {
            log.info("[FINISH]");
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

package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author strike
 */
public class GraphAllCaratheodoryExistsSetOfSize extends GraphCalcCaratheodoryNumberBinaryStrategy {

    static final Logger log = Logger.getLogger(GraphAllCaratheodoryExistsSetOfSize.class);
    static final String type = "P3-Convexity";
    static final String description = "All Caratheodory Set of Size  (Java)";

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Map<String, Object> result = new HashMap<>();
        if (graph == null || graph.getVertexCount() <= 0) {
            return result;
        }

        Integer size = null;

        try {
            String inputData = graph.getInputData();
            size = Integer.parseInt(inputData);
        } catch (Exception e) {

        }
        if (size == null) {
            throw new IllegalArgumentException("Input invalid (not integer): " + graph.getInputData());
        }

        int countNCarat = 0;

        if (size >= 2) {
            Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(graph.getVertexCount(), size);
            while (combinationsIterator.hasNext()) {
                int[] currentSet = combinationsIterator.next();
                OperationConvexityGraphResult hsp3g = hsp3(graph, currentSet);
                if (hsp3g != null) {
                    String key = "Caratheodory Set-" + (countNCarat++) + " |HS|=" + hsp3g.convexHull.size();
                    result.put(key, hsp3g.caratheodorySet);
                    log.info(key + ": " + hsp3g.caratheodorySet);
                }
            }
        }

        result.put("Nº Caratheodory Set of Size(" + size + ")", countNCarat);
        log.info("Nº Caratheodory Set of Size(" + size + "): " + countNCarat);
        return result;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }
}

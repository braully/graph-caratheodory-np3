package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

public class GraphStatistics implements IGraphOperation {

    static final String type = "Graph Class";
    static final String description = "Statistics";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        try {
            DistanceStatistics distanceStatistics = new DistanceStatistics();
            double diameter = distanceStatistics.diameter(graph);
            response.put("Diameter", diameter);
        } catch (Exception ex) {
            log.error(null, ex);
        }
        return response;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }
}

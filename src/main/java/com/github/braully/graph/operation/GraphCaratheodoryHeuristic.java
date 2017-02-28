package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class GraphCaratheodoryHeuristic implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "Nº Caratheodory (Heuristic)";

    public static final int INCLUDED = 2;
    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        long totalTimeMillis = -1;

        totalTimeMillis = System.currentTimeMillis();
        OperationConvexityGraphResult caratheodoryNumberGraph = null;

        Collection<Integer> vertices = graphRead.getVertices();
        Set<Integer> caratheodorySet = new HashSet<>();

        for (Integer v : vertices) {
            int neighborCount = graphRead.getNeighborCount(v);
            if (neighborCount >= 2) {
                Set<Integer> tmp = buildCaratheodorySetFromPartialElement(graphRead, v);
            }
        }

        totalTimeMillis = System.currentTimeMillis() - totalTimeMillis;

        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();
        if (caratheodoryNumberGraph == null) {
            caratheodoryNumberGraph = new OperationConvexityGraphResult();
        }
        if (caratheodoryNumberGraph.caratheodorySet != null
                && !caratheodoryNumberGraph.caratheodorySet.isEmpty()) {
            response.putAll(caratheodoryNumberGraph.toMap());
            response.put(OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_NUMBER, caratheodoryNumberGraph.caratheodorySet.size());
        }
        return response;
    }

    @Override
    public String getTypeProblem() {
        return type;
    }

    @Override
    public String getName() {
        return description;
    }

    private Set<Integer> buildCaratheodorySetFromPartialElement(UndirectedSparseGraphTO<Integer, Integer> graphRead, Integer a) {
        Set<Integer> partialElements = new HashSet<>();
        Set<Integer> convexHull = new HashSet<>();
        Set<Integer> maxCaratheodorySet = new HashSet<>();

        partialElements.add(a);
//        List<Integer> promote = new ArrayList<>();
//        promote.add(v);
        Collection<Integer> neighbors = graphRead.getNeighbors(a);
        Iterator<Integer> iterator = neighbors.iterator();
        Integer v1a = iterator.next();
        Integer v2a = iterator.next();
        promoteVerticeToPartial(graphRead, a, v1a, v2a, partialElements, convexHull, maxCaratheodorySet);

        return maxCaratheodorySet;
    }

    private void promoteVerticeToPartial(UndirectedSparseGraphTO<Integer, Integer> graphRead, Integer a, Integer v1a, Integer v2a, Set<Integer> partialElements, Set<Integer> convexHull, Set<Integer> maxCaratheodorySet) {
        partialElements.add(a);
        convexHull.add(v1a);
        convexHull.add(v2a);
        maxCaratheodorySet.remove(a);
        Collection neighbors = graphRead.getNeighbors(v1a);
        Collection intersection = CollectionUtils.intersection(maxCaratheodorySet, neighbors);
        if (intersection.size() < 2) {
            maxCaratheodorySet.add(v1a);
        }
        neighbors = graphRead.getNeighbors(v2a);
        intersection = CollectionUtils.intersection(maxCaratheodorySet, neighbors);
        if (intersection.size() < 2) {
            maxCaratheodorySet.add(v2a);
        }
    }
}

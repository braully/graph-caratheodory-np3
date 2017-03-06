package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphCaratheodoryHeuristic
        extends GraphCheckCaratheodorySet
        implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "Nº Caratheodory (Heuristic)";

    public static final int INCLUDED = 2;
    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        long totalTimeMillis = -1;

        totalTimeMillis = System.currentTimeMillis();
        OperationConvexityGraphResult caratheodoryNumberGraph = new OperationConvexityGraphResult();

        Collection<Integer> vertices = graphRead.getVertices();
        Set<Integer> caratheodorySet = new HashSet<>();

        for (Integer v : vertices) {
            int neighborCount = graphRead.getNeighborCount(v);
            if (graphRead.isNeighbor(v, v)) {
                neighborCount--;
            }
            if (neighborCount >= 2) {
                Set<Integer> s = new HashSet<>();
                Set<Integer> hs = new HashSet<>();
                Set<Integer> partial = new HashSet<>();
                Set<Integer> tmp = buildCaratheodorySetFromPartialElement(graphRead, v, s, hs, partial);
                if (tmp != null && tmp.size() > caratheodorySet.size()) {
                    caratheodorySet = tmp;
                    caratheodoryNumberGraph.caratheodorySet = caratheodorySet;
                    caratheodoryNumberGraph.convexHull = hs;
                    caratheodoryNumberGraph.partial = partial;
                    caratheodoryNumberGraph.caratheodoryNumber = caratheodorySet.size();
                }
            }
        }

        totalTimeMillis = System.currentTimeMillis() - totalTimeMillis;

        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();
        if (!caratheodorySet.isEmpty()) {
            graphRead.setSet(caratheodorySet);
            response = super.doOperation(graphRead);
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

    private Set<Integer> buildCaratheodorySetFromPartialElement(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer v, Set<Integer> s, Set<Integer> hs,
            Set<Integer> partial) {
        int[] aux = new int[graph.getVertexCount()];
        Set<Integer> promotable = new HashSet<>();

        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }

        partial.add(v);
        hs.add(v);

        Integer nv0 = selectBestNeighbor(v, s, hs, partial, graph, aux);
        addVertToS(nv0, s, hs, partial, graph, aux);
        promotable.add(nv0);

        Integer nv1 = selectBestNeighbor(v, s, hs, partial, graph, aux);
        addVertToS(nv1, s, hs, partial, graph, aux);
        promotable.add(nv1);

        while (!promotable.isEmpty()) {
            Integer vp = selectBestPromotableVertice(s, hs, partial,
                    promotable, graph, aux);

            if (vp != null) {
                promotable.remove(vp);
                removeVertFromS(vp, s, hs, partial, graph, aux);

                nv0 = selectBestNeighbor(vp, s, hs, partial, graph, aux);
                if (nv0 != null) {
                    addVertToS(nv0, s, hs, partial, graph, aux);
                } else {
                    addVertToS(vp, s, hs, partial, graph, aux);
                    continue;
                }

                nv1 = selectBestNeighbor(vp, s, hs, partial, graph, aux);
                if (nv1 != null) {
                    addVertToS(nv1, s, hs, partial, graph, aux);
                    promotable.add(nv0);
                    promotable.add(nv1);
                } else {
                    removeVertFromS(nv0, s, hs, partial, graph, aux);
                    addVertToS(vp, s, hs, partial, graph, aux);
                }
            }
        }

//        boolean checkDerivated = false;
//        for (int i = 0; i < graph.getVertexCount(); i++) {
//            if (auxc[i] >= s.size() && aux[i] == PROCESSED) {
//                checkDerivated = true;
//                break;
//            }
//        }
//        if (checkDerivated) {
//            partial = calcDerivatedPartial(graph,
//                    hs, s);
//        }
        Set<Integer> derivatedPartialReal = calcDerivatedPartial(graph, hs, s);

        System.out.print("\n∂H(S)= {");
        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (partial != null && partial.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("∂®Hs=  {");
        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (derivatedPartialReal != null && derivatedPartialReal.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("H(S) = {");
        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (hs.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("S    = {");
        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (s.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("Aux  = {");
        for (int i = 0; i < graph.getVertexCount(); i++) {
            System.out.printf("%2d | ", aux[i]);
        }
        System.out.println("}");

        return s;
    }

    private void removeVertFromS(Integer verti, Set<Integer> s,
            Set<Integer> hs, Set<Integer> partial,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        s.remove(verti);
        aux[verti] = aux[verti] - INCLUDED;
        if (aux[verti] < 0) {
            aux[verti] = 0;
        }

        Collection<Integer> neighbors = graph.getNeighbors(verti);
        Queue<Integer> mustBeRemoved = new ArrayDeque<>();
        mustBeRemoved.add(verti);
        while (!mustBeRemoved.isEmpty()) {
            verti = mustBeRemoved.remove();
            hs.remove(verti);
            for (int vertn : neighbors) {
                if (vertn == verti) {
                    continue;
                }
                if (aux[vertn]-- >= INCLUDED) {
                    if (aux[vertn] < INCLUDED && !s.contains(vertn)) {
                        mustBeRemoved.add(vertn);
                    }
                }
                if (aux[vertn] < 0) {
                    aux[vertn] = 0;
                }
            }
//            aux[verti] = PROCESSED;
        }
    }

    private Integer selectBestPromotableVertice(Set<Integer> s, Set<Integer> hs,
            Set<Integer> partial, Set<Integer> promotable,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        Integer bestVertex = null;
        Integer bestRanking = null;
        if (promotable != null) {
            Set<Integer> removable = new HashSet<>();
            for (Integer vtmp : promotable) {
                boolean canBePromoted = true;
                if (canBePromoted) {
//                    Integer vtmpRanking = graph.degree(vtmp);
                    Collection neighbors = new HashSet(graph.getNeighbors(vtmp));
                    neighbors.remove(s);
                    neighbors.remove(partial);
                    neighbors.remove(hs);
                    Integer vtmpRanking = neighbors.size();
                    if (vtmpRanking >= 2) {
                        if (bestVertex == null || vtmpRanking < bestRanking) {
                            bestRanking = vtmpRanking;
                            bestVertex = vtmp;
                        }
                    } else {
                        removable.add(vtmp);
                    }
                }
            }
            promotable.removeAll(removable);
        }
        return bestVertex;
    }

    private Integer selectBestNeighbor(Integer v, Set<Integer> s, Set<Integer> hs,
            Set<Integer> partial, UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        Integer ret = null;
        Set<Integer> neighbors = new HashSet<>(graph.getNeighbors(v));
        neighbors.removeAll(s);
        neighbors.removeAll(hs);
        neighbors.removeAll(partial);

        for (Integer nei : neighbors) {
            if (ret == null || graph.degree(nei) < graph.degree(ret)) {
                ret = nei;
            }
        }
        return ret;
    }

    private void addVertToS(Integer verti,
            Set<Integer> s,
            Set<Integer> hs,
            Set<Integer> partial,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {
        s.add(verti);
        aux[verti] = aux[verti] + INCLUDED;

        Collection<Integer> neighbors = graph.getNeighbors(verti);
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        mustBeIncluded.add(verti);
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            hs.add(verti);
            for (int vertn : neighbors) {
                if (vertn == verti) {
                    continue;
                }
                if (vertn != verti && aux[vertn] < INCLUDED) {
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (aux[vertn] == INCLUDED) {
                        mustBeIncluded.add(vertn);
                    }
                }
            }
//            aux[verti] = PROCESSED;
        }
    }

    public Set<Integer> calcDerivatedPartial(UndirectedSparseGraphTO<Integer, Integer> graph,
            Set<Integer> hsp3g, Set<Integer> currentSet) {
        Set<Integer> partial = new HashSet<>();
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        partial.addAll(hsp3g);

        for (Integer p : currentSet) {
            int[] aux = new int[graph.getVertexCount()];
            for (Integer v : currentSet) {
                if (!v.equals(p)) {
                    mustBeIncluded.add(v);
                    aux[v] = INCLUDED;
                }
            }
            while (!mustBeIncluded.isEmpty() && !partial.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                partial.remove(verti);
                Collection<Integer> neighbors = graph.getNeighbors(verti);
                for (int vertn : neighbors) {
                    if (vertn != verti) {
                        int previousValue = aux[vertn];
                        aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                        if (previousValue < INCLUDED && aux[vertn] >= INCLUDED) {
                            mustBeIncluded.add(vertn);
                        }
                    }
                }
            }
        }
        return partial;
    }

//    private void promoteVerticeToPartial(UndirectedSparseGraphTO<Integer, Integer> graphRead, Integer a, Integer v1a, Integer v2a, Set<Integer> partialElements, Set<Integer> convexHull, Set<Integer> maxCaratheodorySet) {
//
////        partialElements.add(a);
////        List<Integer> promote = new ArrayList<>();
////        promote.add(v);
////        Collection<Integer> neighbors = graphRead.getNeighbors(a);
////        Iterator<Integer> iterator = neighbors.iterator();
////        Integer v1a = iterator.next();
////        Integer v2a = iterator.next();
////        promoteVerticeToPartial(graphRead, a, v1a, v2a, partialElements, convexHull, maxCaratheodorySet);
//        partialElements.add(a);
//        convexHull.add(v1a);
//        convexHull.add(v2a);
//        maxCaratheodorySet.remove(a);
//        Collection neighbors = graphRead.getNeighbors(v1a);
//        Collection intersection = CollectionUtils.intersection(maxCaratheodorySet, neighbors);
//        if (intersection.size() < 2) {
//            maxCaratheodorySet.add(v1a);
//        }
//        neighbors = graphRead.getNeighbors(v2a);
//        intersection = CollectionUtils.intersection(maxCaratheodorySet, neighbors);
//        if (intersection.size() < 2) {
//            maxCaratheodorySet.add(v2a);
//        }
//    }
//    public OperationConvexityGraphResult
//            hsp3(UndirectedSparseGraphTO<Integer, Integer> graph,
//                    int[] currentSet) {
//        int currentSetSize = 0;
//        OperationConvexityGraphResult processedHullSet = null;
//        Set<Integer> s = new HashSet<>();
//        Set<Integer> hs = new HashSet<>();
//        Set<Integer> partial = new HashSet<>();
//
//        int[] aux = new int[graph.getVertexCount()];
//        int[] auxc = new int[graph.getVertexCount()];
//
//        for (int i = 0; i < aux.length; i++) {
//            aux[i] = 0;
//            auxc[i] = 0;
//        }
//
//        for (Integer v : currentSet) {
//            aux[v] = INCLUDED;
//            auxc[v] = 1;
//            currentSetSize++;
//        }
//
//        for (Integer v : currentSet) {
//            addVertToS(v, s, hs, partial, graph, aux, auxc);
//        }
//
//        boolean checkDerivated = false;
//
//        for (int i = 0; i < graph.getVertexCount(); i++) {
//            if (auxc[i] >= currentSet.length && aux[i] == PROCESSED) {
//                checkDerivated = true;
//                break;
//            }
//        }
//
//        if (checkDerivated) {
//            Set<Integer> setCurrent = new HashSet<>();
//            for (int i : currentSet) {
//                setCurrent.add(i);
//            }
//            partial = calcDerivatedPartial(graph,
//                    hs, setCurrent);
//            if (partial != null && !partial.isEmpty()) {
//
//                processedHullSet = new OperationConvexityGraphResult();
//                processedHullSet.caratheodoryNumber = currentSetSize;
//                processedHullSet.auxProcessor = aux;
//                processedHullSet.convexHull = hs;
//                processedHullSet.caratheodorySet = setCurrent;
//                processedHullSet.partial = partial;
//            }
//        }
//
//        if (true) {
//            Set<Integer> curSet = new HashSet<>();
//            for (int i = 0; i < currentSet.length; i++) {
//                curSet.add(currentSet[i]);
//            }
//
//            System.out.print("\n∂H(S)= {");
//            for (int i = 0; i < graph.getVertexCount(); i++) {
//                if (partial != null && partial.contains(i)) {
//                    System.out.printf("%2d | ", i);
//                } else {
//                    System.out.print("   | ");
//                }
//            }
//            System.out.println("}");
//
//            System.out.print("H(S) = {");
//            for (int i = 0; i < graph.getVertexCount(); i++) {
//                if (hs.contains(i)) {
//                    System.out.printf("%2d | ", i);
//                } else {
//                    System.out.print("   | ");
//                }
//            }
//            System.out.println("}");
//
//            System.out.print("S    = {");
//            for (int i = 0; i < graph.getVertexCount(); i++) {
//                if (curSet.contains(i)) {
//                    System.out.printf("%2d | ", i);
//                } else {
//                    System.out.print("   | ");
//                }
//            }
//            System.out.println("}");
//
//            System.out.print("Aux  = {");
//            for (int i = 0; i < graph.getVertexCount(); i++) {
//                System.out.printf("%2d | ", aux[i]);
//            }
//            System.out.println("}");
//
//            System.out.print("Auxc = {");
//            for (int i = 0; i < graph.getVertexCount(); i++) {
//                System.out.printf("%2d | ", auxc[i]);
//            }
//            System.out.println("}");
//        }
//
//        return processedHullSet;
//    }
}

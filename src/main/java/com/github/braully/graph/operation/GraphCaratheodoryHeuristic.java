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

    static boolean verbose = true;

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
        int[] auxVp = new int[graph.getVertexCount()];
        int[] auxNv0 = new int[graph.getVertexCount()];
        int[] auxNv1 = new int[graph.getVertexCount()];

        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }

        partial.add(v);

        if (verbose) {
            System.out.println("Adding vertice " + v + " to parcial");
        }

        Integer nv0 = selectBestNeighbor(v, graph, aux, partial, auxVp);
        if (verbose) {
            System.out.println("Adding vertice " + nv0 + " to S");
        }

        addVertToS(nv0, s, graph, aux);
        promotable.add(nv0);

        Integer nv1 = selectBestNeighbor(v, graph, aux, partial, auxVp);
        if (verbose) {
            System.out.println("Adding vertice " + nv1 + " to S");
        }
        addVertToS(nv1, s, graph, aux);
        promotable.add(nv1);

        if (verbose) {
            printSituation(graph.getVertexCount(), partial, s, aux);
        }

        while (!promotable.isEmpty()) {
            Integer vp = selectBestPromotableVertice(s, partial,
                    promotable, graph, aux);

            if (vp != null) {
                if (verbose) {
                    System.out.println("Selectd " + vp + " from priority list");

                    System.out.print("Aux(" + vp + ")   = {");
                    for (int i = 0; i < graph.getVertexCount(); i++) {
                        System.out.printf("%2d | ", aux[i]);
                    }
                    System.out.println("}");
//                    System.out.print("Trying promote " + vp + " From S to H(S) ... ");
                }

                //Backup aux
                for (int i = 0; i < graph.getVertexCount(); i++) {
                    auxVp[i] = aux[i];
                }
                promotable.remove(vp);
                removeVertFromS(vp, s, graph, aux);

                for (int i = 0; i < graph.getVertexCount(); i++) {
                    auxNv0[i] = aux[i];
                    auxNv1[i] = aux[i];
                }

                System.out.print("Aux(-" + vp + ")  = {");
                for (int i = 0; i < graph.getVertexCount(); i++) {
                    System.out.printf("%2d | ", aux[i]);
                }
                System.out.println("}");

                nv0 = selectBestNeighbor(vp, graph, auxNv0, partial, auxVp);
                if (nv0 == null) {
                    for (int i = 0; i < graph.getVertexCount(); i++) {
                        aux[i] = auxVp[i];
                    }
                    s.add(vp);
                    if (verbose) {
                        System.out.println("Not promotable");
                    }
                    continue;
                }
                addVertToS(nv0, s, graph, auxNv0);

                System.out.print("Aux(-" + vp + "+" + nv0 + ")= {");
                for (int i = 0; i < graph.getVertexCount(); i++) {
                    System.out.printf("%2d | ", auxNv0[i]);
                }
                System.out.println("}");

                nv1 = selectBestNeighbor(vp, graph, auxNv0, partial, auxVp);

                if (nv1 == null) {
                    for (int i = 0; i < graph.getVertexCount(); i++) {
                        aux[i] = auxVp[i];
                    }
                    s.add(vp);
                    if (verbose) {
                        System.out.println("Not promotable");
                    }
                    continue;
                }

                addVertToS(nv1, s, graph, auxNv1);
                System.out.print("Aux(-" + vp + "+" + nv1 + ")= {");
                for (int i = 0; i < graph.getVertexCount(); i++) {
                    System.out.printf("%2d | ", auxNv1[i]);
                }
                System.out.println("}");

                promotable.add(nv0);
                promotable.add(nv1);

                addVertToS(nv1, s, graph, auxNv0);

                for (int i = 0; i < graph.getVertexCount(); i++) {
                    aux[i] = auxNv0[i];
                }

                if (verbose) {
                    System.out.println("OK");
                    System.out.println("Adding vertice " + nv0 + " to S");
                    System.out.println("Adding vertice " + nv1 + " to S");
                    printSituation(graph.getVertexCount(), partial, s, aux);

                    System.out.print("Auxf = {");
                    for (int i = 0; i < graph.getVertexCount(); i++) {
                        System.out.printf("%2d | ", aux[i]);
                    }
                    System.out.println("}");
                }
            }
        }

        int stmp[] = new int[s.size()];
        Integer cont = 0;
        for (Integer vs : s) {
            stmp[cont++] = vs;
        }
        OperationConvexityGraphResult hsp3 = hsp3aux(graph, stmp);
        Set<Integer> derivatedPartialReal = hsp3.partial;
        int[] auxReal = hsp3.auxProcessor;
        Set<Integer> convexHullReal = hsp3.convexHull;

        if (verbose) {
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
                if (aux[i] >= 2) {
                    System.out.printf("%2d | ", i);
                } else {
                    System.out.print("   | ");
                }
            }
            System.out.println("}");

            System.out.print("H®s  = {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                if (convexHullReal.contains(i)) {
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

            System.out.print("Aux® = {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                System.out.printf("%2d | ", auxReal[i]);
            }
            System.out.println("}");
        }

        return s;
    }

    public void printSituation(int numVertices, Set<Integer> partial, Set<Integer> s, int[] aux) {
        System.out.print("\n∂H(S)= {");
        for (int i = 0; i < numVertices; i++) {
            if (partial != null && partial.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("H(S) = {");
        for (int i = 0; i < numVertices; i++) {
            if (aux[i] >= 2) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("S    = {");
        for (int i = 0; i < numVertices; i++) {
            if (s.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("Aux  = {");
        for (int i = 0; i < numVertices; i++) {
            System.out.printf("%2d | ", aux[i]);
        }
        System.out.println("}");
    }

    public void addVertToS(Integer verti, Set<Integer> s,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {

        if (aux[verti] >= INCLUDED) {
            return;
        }

        aux[verti] = aux[verti] + INCLUDED;
        s.add(verti);

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        mustBeIncluded.add(verti);
        while (!mustBeIncluded.isEmpty()) {
            verti = mustBeIncluded.remove();
            Collection<Integer> neighbors = graph.getNeighbors(verti);
            for (int vertn : neighbors) {
                if (vertn == verti) {
                    continue;
                }
                if (vertn != verti && ++aux[vertn] == INCLUDED) {
                    mustBeIncluded.add(vertn);
                }
            }
        }
    }

    public void removeVertFromS(Integer verti, Set<Integer> s,
            UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux) {

        for (int i = 0; i < graph.getVertexCount(); i++) {
            aux[i] = 0;
        }
        s.remove(verti);
        for (Integer v : s) {
            addVertToS(v, s, graph, aux);
        }

//        if (!s.remove(verti)) {
//            return;
//        }
//        aux[verti] = aux[verti] - INCLUDED;
//        Queue<Integer> mustBeRemoved = new ArrayDeque<>();
//        mustBeRemoved.add(verti);
//        Set<Integer> removed = new HashSet<>();
//        while (!mustBeRemoved.isEmpty()) {
//            verti = mustBeRemoved.remove();
//            removed.add(verti);
//            Collection<Integer> neighbors = graph.getNeighbors(verti);
//            for (int vertn : neighbors) {
//                if (vertn == verti) {
//                    continue;
//                }
//                if (aux[vertn]-- >= INCLUDED && aux[vertn] < INCLUDED && !removed.contains(vertn)) {
//                    mustBeRemoved.add(vertn);
//                }
//            }
//        }
    }

    public Integer selectBestPromotableVertice(Set<Integer> s,
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
                    neighbors.removeAll(s);
                    neighbors.removeAll(partial);
//                    neighbors.removeAll(hs);
                    for (int i = 0; i < aux.length; i++) {
                        if (aux[i] >= 2) {
                            neighbors.remove(i);
                        }
                    }
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

    public Integer selectBestNeighbor(Integer v, UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] aux, Set<Integer> partial, int[] auxBackup) {
        Integer ret = null;
        Set<Integer> neighbors = new HashSet<>(graph.getNeighbors(v));
//        neighbors.removeAll(s);
//        neighbors.removeAll(hs);
        neighbors.removeAll(partial);
        neighbors.remove(v);
        for (int i = 0; i < aux.length; i++) {
            if (aux[i] >= 2 || auxBackup[i] >= 2) {
                neighbors.remove(i);
            }
        }

        for (Integer nei : neighbors) {
            if (ret == null || graph.degree(nei) < graph.degree(ret)) {
                ret = nei;
            }
        }
        return ret;
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

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
    static final String description = "Nº Caratheodory (Heuristic v1)";

    public static final int INCLUDED = 2;
    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;

//    static boolean verbose = true;
    static boolean verbose = false;

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        long totalTimeMillis = -1;

        totalTimeMillis = System.currentTimeMillis();
        Set<Integer> caratheodorySet = buildMaxCaratheodorySet(graphRead);
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

    Set<Integer> buildCaratheodorySetFromPartialElement(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer v, Set<Integer> s, Set<Integer> hs) {
        int vertexCount = graph.getVertexCount();

        Set<Integer> promotable = new HashSet<>();
        Set<Integer> partial = new HashSet<>();
        int[] aux = new int[vertexCount];
        int[] auxVp = new int[vertexCount];

        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }

        partial.add(v);

        if (verbose) {
            System.out.println("\n\t* Adding vertice " + v + " to parcial");
        }

        Integer nv0 = selectBestNeighbor(v, graph, aux, partial, auxVp);
        if (verbose) {
            System.out.println("\t* Adding vertice " + nv0 + " to S");
        }

        addVertToS(nv0, s, graph, aux);
        promotable.add(nv0);

        Integer nv1 = selectBestNeighbor(v, graph, aux, partial, auxVp);
        if (verbose) {
            System.out.println("\t* Adding vertice " + nv1 + " to S");
        }
        addVertToS(nv1, s, graph, aux);
        promotable.add(nv1);

        if (verbose) {
            printSituation(vertexCount, partial, s, aux);
        }

        while (!promotable.isEmpty()) {
            Integer vp = selectBestPromotableVertice(s, partial,
                    promotable, graph, aux);

            if (vp != null) {
                if (verbose) {
                    System.out.println("\n\t* Selectd " + vp + " from priority list");
                    System.out.print(String.format("Aux(%2d)    ", vp));
                    printArrayAux(aux);
                }

                copyArray(auxVp, aux);
                promotable.remove(vp);
                removeVertFromS(vp, s, graph, aux);

                nv0 = selectBestNeighbor(vp, graph, aux, partial, auxVp);
                if (nv0 == null) {
                    copyArray(aux, auxVp);
                    s.add(vp);
                    if (verbose) {
                        System.out.println("\t* Not promotable - nvo");
                    }
                    continue;
                }
                addVertToS(nv0, s, graph, aux);
                nv1 = selectBestNeighbor(vp, graph, aux, partial, auxVp);

                if (nv1 == null) {
                    copyArray(aux, auxVp);
                    s.add(vp);
                    s.remove(nv0);
                    continue;
                }

                addVertToS(nv1, s, graph, aux);

                boolean checkIfCaratheodory = checkIfCaratheodrySet(auxVp, aux, s, v, vp, nv0, nv1, graph);

                if (verbose) {
                    System.out.print("Auxf       ");
                    printArrayAux(aux);
                    printSatusVS(aux, partial, nv0, nv1, vp, s, graph);
                    printDifference(auxVp, aux, graph);
                    System.out.println("=========> Check Caratheodory Available: " + (checkIfCaratheodory ? "Ok" : "Erro"));
                }

                if (!checkIfCaratheodory) {
                    //roll back
                    if (verbose) {
                        System.out.println("\t* Roll back checkIfCaratheodory=false");
                    }
                    copyArray(aux, auxVp);
                    s.add(vp);
                    s.remove(nv0);
                    s.remove(nv1);
                    continue;
                }

                promotable.add(nv0);
                promotable.add(nv1);

                if (verbose) {
                    System.out.println("\t-- OK");
                    System.out.println("\t* Adding vertice " + nv0 + " to S");
                    System.out.println("\t* Adding vertice " + nv1 + " to S");
                    printSituation(vertexCount, partial, s, aux);
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
            if (derivatedPartialReal == null || derivatedPartialReal.isEmpty()) {
                System.out.println("============== ERRO ==================");
            } else {
                System.out.println("-------------- OK --------------------");
            }
            printFinalState(graph, partial, derivatedPartialReal, aux, convexHullReal, s, auxReal);
        }

        return s;
    }

    private void printSatusVS(int[] aux, Set<Integer> partial, Integer nv0, Integer nv1, Integer vp, Set<Integer> s, UndirectedSparseGraphTO<Integer, Integer> graph) {
        System.out.print("V(S)       ");
        System.out.print(" = {");
        for (int i = 0; i < aux.length; i++) {
            if (partial.contains(i)) {
                System.out.printf(" P | ", i);
            } else if (nv0 != null && nv0.equals(i)) {
                System.out.print(" 0 | ");
            } else if (nv1 != null && nv1.equals(i)) {
                System.out.print(" 1 | ");
            } else if (vp.equals(i)) {
                System.out.print(" V | ");
            } else if (s.contains(i)) {
                System.out.print(" S | ");
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("D(V)       ");
        System.out.print(" = {");
        for (int i = 0; i < aux.length; i++) {
            System.out.printf("%2d | ", graph.getNeighborCount(i));
        }
        System.out.println("}");
    }

    void printFinalState(UndirectedSparseGraphTO<Integer, Integer> graph, Set<Integer> partial, Set<Integer> derivatedPartialReal, int[] aux, Set<Integer> convexHullReal, Set<Integer> s, int[] auxReal) {
        int vertexCount = graph.getVertexCount();
        System.out.print("∂H(S)       = {");
        for (int i = 0; i < vertexCount; i++) {
            if (partial != null && partial.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("∂®Hs        = {");
        for (int i = 0; i < vertexCount; i++) {
            if (derivatedPartialReal != null && derivatedPartialReal.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("H(S)        = {");
        for (int i = 0; i < vertexCount; i++) {
            if (aux[i] >= 2) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("H®s         = {");
        for (int i = 0; i < vertexCount; i++) {
            if (convexHullReal.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("S           = {");
        for (int i = 0; i < vertexCount; i++) {
            if (s.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("Aux         = {");
        for (int i = 0; i < vertexCount; i++) {
            System.out.printf("%2d | ", aux[i]);
        }
        System.out.println("}");

        System.out.print("Aux®        = {");
        for (int i = 0; i < vertexCount; i++) {
            System.out.printf("%2d | ", auxReal[i]);
        }
        System.out.println("}");
    }

    void printArrayAux(int[] aux) {
        System.out.print(" = {");
        for (int i = 0; i < aux.length; i++) {
            System.out.printf("%2d | ", aux[i]);
        }
        System.out.println("}");
    }

    void copyArray(int[] auxtg, int[] auxsrc) {
        //Backup aux
        for (int i = 0; i < auxtg.length; i++) {
            auxtg[i] = auxsrc[i];
        }
    }

    public void printSituation(int numVertices, Set<Integer> partial, Set<Integer> s, int[] aux) {
        System.out.print("\n∂H(S)       = {");
        for (int i = 0; i < numVertices; i++) {
            if (partial != null && partial.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("H(S)        = {");
        for (int i = 0; i < numVertices; i++) {
            if (aux[i] >= 2) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

        System.out.print("S           = {");
        for (int i = 0; i < numVertices; i++) {
            if (s.contains(i)) {
                System.out.printf("%2d | ", i);
            } else {
                System.out.print("   | ");
            }
        }
        System.out.println("}");

//        System.out.print("Aux  = {");
//        for (int i = 0; i < numVertices; i++) {
//            System.out.printf("%2d | ", aux[i]);
//        }
//        System.out.println("}");
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

        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
        }
        s.remove(verti);
        for (Integer v : s) {
            addVertToS(v, s, graph, aux);
        }
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
                    Collection neighbors = new HashSet(graph.getNeighbors(vtmp));
                    neighbors.removeAll(s);
                    neighbors.removeAll(partial);

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
        neighbors.removeAll(partial);
        neighbors.remove(v);
        Integer ranking = null;
        for (int i = 0; i < aux.length; i++) {
            if (aux[i] >= 2 || auxBackup[i] >= 2) {
                neighbors.remove(i);
            }
        }

        for (Integer nei : neighbors) {
            int neiRanking = aux[nei] * 100 + graph.degree(nei);
            if (ret == null || neiRanking < ranking) {
                ret = nei;
                ranking = neiRanking;
            }
        }
        return ret;
    }

    private void printDifference(int[] aux, int[] auxNv0, UndirectedSparseGraphTO graph) {
        System.out.print("F-I        ");
        System.out.print(" = {");
        for (int i = 0; i < aux.length; i++) {
            System.out.printf("%2d | ", (auxNv0[i] - aux[i]));
        }
        System.out.println("}");

        System.out.print("F-D        ");
        System.out.print(" = {");
        for (int i = 0; i < aux.length; i++) {
            System.out.printf("%2d | ", (auxNv0[i] - graph.getNeighborCount(i)));
        }
        System.out.println("}");

        System.out.print("D-F-I      ");
        System.out.print(" = {");
        for (int i = 0; i < aux.length; i++) {
            System.out.printf("%2d | ", (graph.getNeighborCount(i) - (auxNv0[i] - aux[i])));
        }
        System.out.println("}");
    }

    private boolean checkIfCaratheodrySet(int[] auxi, int[] auxf, Set<Integer> s,
            Integer v, Integer vp, Integer nv0,
            Integer nv1, UndirectedSparseGraphTO<Integer, Integer> graph) {
        boolean ret = true;
        int[] auxbackp = new int[auxf.length];
        int deltaParcial = auxf[v] - auxi[v];
        int deltaVp = auxf[vp] - auxi[vp];

        for (Integer i : s) {
//            for (int ia = 0; ia < auxf.length; ia++) {
//                auxbackp[ia] = auxf[ia];
//            }
            int deltaSi = auxf[i] - auxi[i];
            if (deltaSi >= INCLUDED || deltaParcial >= 1 || deltaVp >= INCLUDED) {
                Set<Integer> sbackup = new HashSet<>(s);
                removeVertFromS(i, sbackup, graph, auxbackp);
                if (auxbackp[i] >= INCLUDED || auxbackp[v] >= INCLUDED) {
                    ret = false;
                    break;
                }
            }
        }
        return ret;
    }

    public Set<Integer> buildMaxCaratheodorySet(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        Set<Integer> caratheodorySet = new HashSet<>();
        Collection<Integer> vertices = graphRead.getVertices();
        for (Integer v : vertices) {
            int neighborCount = graphRead.getNeighborCount(v);
            if (graphRead.isNeighbor(v, v)) {
                neighborCount--;
            }
            if (neighborCount >= 2) {
                Set<Integer> s = new HashSet<>();
                Set<Integer> hs = new HashSet<>();
                Set<Integer> tmp = buildCaratheodorySetFromPartialElement(graphRead, v, s, hs);
                if (tmp != null && tmp.size() > caratheodorySet.size()) {
                    caratheodorySet = tmp;
                }
            }
        }
        return caratheodorySet;
    }

}

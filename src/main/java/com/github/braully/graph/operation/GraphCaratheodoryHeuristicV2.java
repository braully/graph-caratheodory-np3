package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphCaratheodoryHeuristicV2
        extends GraphCaratheodoryHeuristic {

    static final String description = "Nº Caratheodory (Heuristic v2)";

    public static final int INCLUDED = 2;
    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;

    static boolean verbose = true;

    @Override
    public String getName() {
        return description;
    }

    Set<Integer> buildCaratheodorySetFromPartialElement(UndirectedSparseGraphTO<Integer, Integer> graph,
            Integer v, Set<Integer> s, Set<Integer> hs) {
        int vertexCount = graph.getVertexCount();
        int[] aux = new int[vertexCount];
        Set<Integer> promotable = new HashSet<>();
        Set<Integer> partial = new HashSet<>();
        int[] auxVp = new int[vertexCount];
        int[] auxNv0 = new int[vertexCount];
        int[] auxNv1 = new int[vertexCount];

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
            printSituation(vertexCount, partial, s, aux);
        }

        while (!promotable.isEmpty()) {
            Integer vp = selectBestPromotableVertice(s, partial,
                    promotable, graph, aux);

            if (vp != null) {
                if (verbose) {
                    System.out.println("Selectd " + vp + " from priority list");
                    System.out.print("Aux(" + vp + ")  ");
                    printArrayAux(aux);
                }

                copyArray(auxVp, aux);
                promotable.remove(vp);
                removeVertFromS(vp, s, graph, aux);

                for (int i = 0; i < vertexCount; i++) {
                    auxNv0[i] = aux[i];
                    auxNv1[i] = aux[i];
                }

                System.out.print("Aux(-" + vp + ") ");
                printArrayAux(aux);

                nv0 = selectBestNeighbor(vp, graph, auxNv0, partial, auxVp);
                if (nv0 == null) {
                    copyArray(aux, auxVp);
                    s.add(vp);
                    if (verbose) {
                        System.out.println("Not promotable - nvo");
                    }
                    continue;
                }
                addVertToS(nv0, s, graph, auxNv0);

                System.out.print("Aux(-" + vp + "+" + nv0 + ")");
                printArrayAux(auxNv0);

                nv1 = selectBestNeighbor(vp, graph, auxNv0, partial, auxVp);

                if (nv1 == null) {
                    copyArray(aux, auxVp);
                    s.add(vp);
                    s.remove(nv0);
                    if (verbose) {
                        System.out.println("Not promotable - nv1");
                    }
                    continue;
                }

                addVertToS(nv1, s, graph, auxNv1);

                System.out.print("Aux(-" + vp + "+" + nv1 + ")");
                printArrayAux(auxNv1);

                if (auxNv0[vp] >= INCLUDED || auxNv0[v] >= INCLUDED) {
                    //vertice nv0 include partial and vp
                }

                if (auxNv1[vp] >= INCLUDED || auxNv1[v] >= INCLUDED) {
                    //vertice nv1 include partial and vp
                }

                promotable.add(nv0);
                promotable.add(nv1);

                addVertToS(nv1, s, graph, auxNv0);

                copyArray(aux, auxNv0);

                if (verbose) {
                    System.out.println("OK");
                    System.out.println("Adding vertice " + nv0 + " to S");
                    System.out.println("Adding vertice " + nv1 + " to S");
                    printSituation(vertexCount, partial, s, aux);

                    System.out.print("Auxf ");
                    printArrayAux(aux);

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
            printFinalState(graph, partial, derivatedPartialReal, aux, convexHullReal, s, auxReal);
        }

        return s;
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
}

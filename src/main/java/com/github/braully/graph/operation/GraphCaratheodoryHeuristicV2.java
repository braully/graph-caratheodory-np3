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

    static boolean verbose = false;

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
            boolean rollback = false;

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

                if (verbose) {
                    System.out.print("Aux(-" + vp + ") ");
                    printArrayAux(aux);
                }

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

                if (verbose) {
                    System.out.print("Aux(-" + vp + "+" + nv0 + ")");
                    printArrayAux(auxNv0);
                }

                if (auxNv0[vp] >= INCLUDED || auxNv0[v] >= INCLUDED) {
                    //vertice nv0 include partial and vp
                    //best complementary
                    if (verbose) {
                        System.out.println("Nv0 promotable vp and partial");
                    }
                    if (graph.getNeighborCount(nv0) >= 2) {
                        promotable.add(nv0);
                        copyArray(aux, auxNv0);
                        continue;
                    } else {
                        rollback = true;
                    }
                }

                nv1 = selectBestNeighbor(vp, graph, auxNv0, partial, auxVp);

                if (nv1 == null) {
                    rollback(aux, auxVp, s, promotable, vp, nv0, nv1);
                    if (verbose) {
                        System.out.println("Not promotable - nv1");
                    }
                    continue;
                }

                addVertToS(nv1, s, graph, auxNv1);

                if (verbose) {
                    System.out.print("Aux(-" + vp + "+" + nv1 + ")");
                    printArrayAux(auxNv1);
                }

                if (auxNv1[vp] >= INCLUDED || auxNv1[v] >= INCLUDED) {
                    //vertice nv1 include partial and vp
                    //best complementary
                    System.out.println("Nv1 promotable vp and partial");
                    if (graph.getNeighborCount(nv1) >= 2) {
                        promotable.add(nv1);
                        copyArray(aux, auxNv1);
                        continue;
                    } else {
                        rollback = true;
                    }
                }

                if (rollback) {
                    rollback(aux, auxVp, s, promotable, vp, nv0, nv1);
                    continue;
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

    private void rollback(int[] aux, int[] auxVp, Set<Integer> s, Set<Integer> promotable, Integer vp, Integer nv0, Integer nv1) {
        copyArray(aux, auxVp);
        s.add(vp);
        s.remove(nv0);
        s.remove(nv1);
        promotable.remove(nv0);
        promotable.remove(nv1);

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
                    if (vtmpRanking > 0) {
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

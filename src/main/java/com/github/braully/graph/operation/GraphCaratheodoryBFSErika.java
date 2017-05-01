package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphCaratheodoryBFSErika
        extends GraphCheckCaratheodorySet
        implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "Nº Caratheodory (BFS Bloco Erika)";

    public static boolean verbose = true;

//    public static boolean verbose = false;
    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {

        int maxmaxcg = 0;

        System.out.printf("V(G)    = {");
        for (int i = 0; i < graph.getVertexCount(); i++) {
            System.out.printf("%2d | ", i);
        }
        System.out.println("}");
        Collection<Integer> vertices = graph.getVertices();

        for (Integer v : vertices) {
            int maxcg = 0;
            maxcg = ncaratBfsBloco(graph, v);
            if (maxmaxcg < maxcg) {
                maxmaxcg = maxcg;
            }
        }
        System.out.println("c(G) = " + maxmaxcg);
        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();
        response.put(OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_NUMBER, maxmaxcg);
        response.put(IGraphOperation.DEFAULT_PARAM_NAME_RESULT, maxmaxcg);
        return response;
    }

    public int ncaratBfsBloco(UndirectedSparseGraphTO<Integer, Integer> graph, Integer v) {
        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
        Collection<Integer> vertices = graph.getVertices();
        int vertexCount = graph.getVertexCount();

        int[] lv = new int[vertexCount];
        int[] lvLinha = new int[vertexCount];
        int[] l1v = new int[vertexCount];
        int[] l2v = new int[vertexCount];
        int[] l3v = new int[vertexCount];
        int maxcg = 0;
        System.out.println("0 - Enraizando: " + v);
        //BFS
        bdl.labelDistances(graph, v);

        if (GraphCaratheodoryHeuristic.verbose) {
            System.out.printf("bfs(%2d) = {", v);
            for (int i = 0; i < vertexCount; i++) {
                int distance = bdl.getDistance(graph, i);
                System.out.printf("%2d | ", distance);
            }
            System.out.println("}");
        }
        //Computar arvore
        Map<Integer, Integer> predecessorCount = new HashMap<>();
        Map<Integer, Set<Integer>> childs = new HashMap<>();
        for (int w = 0; w < vertexCount; w++) {
            Set<Integer> predecessors = bdl.getPredecessors(w);
            if (predecessors != null) {
                for (Integer vp : predecessors) {
                    Integer cont = predecessorCount.get(vp);
                    if (cont == null) {
                        cont = 0;
                    }
                    cont = cont + 1;
                    predecessorCount.put(vp, cont);

                    Set<Integer> chvp = childs.get(vp);
                    if (chvp == null) {
                        chvp = new HashSet<>();
                        childs.put(vp, chvp);
                    }
                    chvp.add(w);
                }
            }
        }
        //Folhas
        Set<Integer> keySet = predecessorCount.keySet();
        Set<Integer> leafs = new HashSet<>(vertices);
        leafs.removeAll(keySet);
        if (GraphCaratheodoryHeuristic.verbose) {
            System.out.println("Leafs(" + v + "): " + leafs);
        }
        // 1 - Se w é folha
        for (Integer w : leafs) {
            if (GraphCaratheodoryHeuristic.verbose) {
                System.out.println("1 - É folha: " + w);
            }
            lv[w] = 1;
            lvLinha[w] = Integer.MIN_VALUE;

        }
        // 2 - Se w tem no maximo um filho u
        for (int w = 0; w < vertexCount; w++) {
            Integer pdcount = predecessorCount.get(w);
            if (pdcount != null && pdcount.equals(1)) {
                Set<Integer> ChvW = childs.get(w);
                System.out.println("2 - Apenas 1 filho: " + w + " -> " + ChvW);
                for (Integer u : ChvW) {
                    lv[w] = 1;
                    lvLinha[w] = lv[u]; //duvida
                }
            }
        }
        // 3 - Se w tem pelo menos dois filho u1, u2
        for (int w = 0; w < vertexCount; w++) {
            Integer pdcount = predecessorCount.get(w);
            if (pdcount != null && pdcount.intValue() >= 2) {
                Set<Integer> su = childs.get(w);
                if (GraphCaratheodoryHeuristic.verbose) {
                    System.out.println("3 - Pelo menos 2 filhos: " + w + " -> " + su);
                }
                // 3a - Se dois filhos são folha
                int contFilhosFolha = 0;
                for (Integer u : su) {
                    if (leafs.contains(u)) {
                        contFilhosFolha++;
                    }
                }
                if (contFilhosFolha >= 2) {
                    if (GraphCaratheodoryHeuristic.verbose) {
                        System.out.println("\t3a - 2 filhos folha: " + w);
                    }
                    l1v[w] = 2;
                }

                //3b - Conjunto de componentes conexas do subgrafo induzido G|Chv(w)
                UndirectedSparseGraphTO inducedSubgraph = FilterUtils.createInducedSubgraph(su, graph);
                if (GraphCaratheodoryHeuristic.verbose) {
                    System.out.println("\t3b - subgrafo induzido G|Chv(w) : " + inducedSubgraph);
                }
                List<UndirectedSparseGraphTO> componentesConexas = listaComponentesConexas(inducedSubgraph);
                if (GraphCaratheodoryHeuristic.verbose) {
                    System.out.println("\t Compomentes conexas (" + componentesConexas.size() + "): " + componentesConexas);
                }
                //3c -
                l2v[w] = Integer.MIN_VALUE;
                if (componentesConexas.size() >= 2) {
                    int maxlvu[] = new int[componentesConexas.size()];
                    for (int c = 0; c < componentesConexas.size(); c++) {
                        maxlvu[c] = Integer.MIN_VALUE;
                        UndirectedSparseGraphTO componente = componentesConexas.get(c);
                        for (Integer u : (Collection<Integer>) componente.getVertices()) {
                            maxlvu[c] = Math.max(maxlvu[c], lv[u]);
                        }
                    }
                    Arrays.sort(maxlvu);
                    l2v[w] = maxlvu[maxlvu.length - 1] + maxlvu[maxlvu.length - 2];
                }

                //3d -
                int maxlvu1lvu2 = Integer.MIN_VALUE;
                for (int c = 0; c < componentesConexas.size(); c++) {
                    int u1 = -1;
                    int maxlvu = Integer.MIN_VALUE;
                    int maxlvulinha = Integer.MIN_VALUE;
                    UndirectedSparseGraphTO componente = componentesConexas.get(c);
                    for (Integer u : (Collection<Integer>) componente.getVertices()) {
                        if (maxlvu < lv[u]) {
                            maxlvu = lv[u];
                            u1 = u;
                        }
                        if (maxlvulinha < lvLinha[u] && !u.equals(u1)) {
                            maxlvulinha = lvLinha[u];
                        }
                    }
                    if (maxlvu > maxlvu1lvu2 && maxlvulinha > maxlvu1lvu2 && (maxlvu + maxlvulinha) > maxlvu1lvu2) {
                        maxlvu1lvu2 = maxlvu + maxlvulinha;
                    }
                }
                l3v[w] = maxlvu1lvu2;

                //3e - lv(w) = max{l1v(w), l2v(w), l3v(w)}, lvlinha(w) = max{lv(u) |uEChv(w)}
                lv[w] = Math.max(Math.max(l1v[w], l2v[w]), l3v[w]);
                lvLinha[w] = Integer.MIN_VALUE;
                for (Integer u : su) {
                    lvLinha[w] = Math.max(lvLinha[w], lv[u]);
                }
            }
        }
        //4 - c(G) = max{lv(v)|vEV(G)}
        for (Integer vert : vertices) {
            if (lv[vert] > maxcg) {
                maxcg = lv[vert];
            }
        }
        if (GraphCaratheodoryHeuristic.verbose) {
            System.out.println("4 - c(G) = max{lv(v)|vEV(G) = " + maxcg);
        }
        return maxcg;
    }

    @Override
    public String getTypeProblem() {
        return type;
    }

    @Override
    public String getName() {
        return description;
    }

    private List<UndirectedSparseGraphTO> listaComponentesConexas(UndirectedSparseGraphTO inducedSubgraph) {
        List<UndirectedSparseGraphTO> listaComponetesConexas = new ArrayList<>();
        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
        Collection<Integer> vertices = inducedSubgraph.getVertices();
        Set<Integer> discoverd = new HashSet<>();
        for (Integer v : vertices) {
            if (!discoverd.contains(v)) {
                bdl.labelDistances(inducedSubgraph, v);
                List<Integer> verticesDiscoverd = bdl.getVerticesInOrderVisited();
                discoverd.addAll(discoverd);
                UndirectedSparseGraphTO componente = FilterUtils.createInducedSubgraph(verticesDiscoverd, inducedSubgraph);
                listaComponetesConexas.add(componente);
            }
        }
        return listaComponetesConexas;
    }

}

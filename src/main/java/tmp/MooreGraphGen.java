package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 *
 * @author strike
 */
public class MooreGraphGen {

    private static final boolean verbose = true;

    private static int K = 57;
    private static int NUM_ARESTAS = ((K * K + 1) * K) / 2;

    public static void main(String... args) {
        K = 57;
        if (K == 7) {
            NUM_ARESTAS = ((K * K + 1) * K) / 2;
            UndirectedSparseGraphTO graphTemplate = HoffmanGraphGen.subgraph;
            generateGraph(K, NUM_ARESTAS, graphTemplate);
        }
        if (K == 57) {
            NUM_ARESTAS = ((K * K + 1) * K) / 2;
            UndirectedSparseGraphTO graphTemplate = LGMGen.subgraph;
            generateGraph(K, NUM_ARESTAS, graphTemplate);
        }
    }

    private static void generateGraph(int K, int NUM_ARESTAS, UndirectedSparseGraphTO graphTemplate) {
        Collection<Integer> vertices = graphTemplate.getVertices();
        int numvert = vertices.size();
        List<Integer> incompletVertices = new ArrayList<>();
        int len = NUM_ARESTAS - graphTemplate.getEdgeCount();

        for (Integer v : vertices) {
            if (graphTemplate.degree(v) < K) {
                incompletVertices.add(v);
            }
        }

        System.out.print("Graph[");
        System.out.print(graphTemplate.getVertexCount());
        System.out.print(", ");
        System.out.print(graphTemplate.getEdgeCount());
        System.out.println("]");

        System.out.print("Incomplete vertices[");
        System.out.print(incompletVertices.size());
        System.out.print("]: ");
        System.out.println(incompletVertices);
        System.out.print("Edges remain: ");
        System.out.println(len);
//        Integer[] bfs = new Integer[vertices.size()];
        sincronizarVerticesIncompletos(graphTemplate, vertices, incompletVertices);

        System.out.println("Montando mapa BFS Inicial");
        Integer[][] bfsAtual = new Integer[numvert][];
//        for (Integer v : incompletVertices) {
//            Integer[] tmpBfs = new Integer[numvert];
//            bfsAtual[v] = tmpBfs;
//            UtilTmp.bfs(graphTemplate, tmpBfs, v);
//        }
//        System.out.println("Montado");

        Integer[] edgesAdded = new Integer[len];
        int countEdeges = 0;
        int[] pos = new int[len];
        for (int i = 0; i < len; i++) {
            pos[i] = 0;
        }

        UndirectedSparseGraphTO lastgraph = graphTemplate.clone();
        List<Integer> poss = new ArrayList<>();
        List<Integer> bestVals = new ArrayList<>();
        Integer[] bfsTmp = new Integer[numvert];

        while (!incompletVertices.isEmpty() && lastgraph.getEdgeCount() < NUM_ARESTAS) {
            Integer v = incompletVertices.get(0);
            Integer[] bfs = bfsAtual[v];
            if (bfs == null) {
                bfs = new Integer[numvert];
                bfsAtual[v] = bfs;
            }
            sincronizarListaPossibilidades(bfs, lastgraph, poss, v);

            while (lastgraph.degree(v) < K) {
                poss.clear();
                for (Integer i = 0; i < bfs.length; i++) {
                    if (bfs[i] > 3 && lastgraph.degree(i) < K) {
                        poss.add(i);
                    }
                }

                int dv = lastgraph.degree(v);
                int posssize = poss.size();
                int idx = pos[countEdeges];
                if (posssize == 0 || posssize < K - dv || idx >= posssize) {
                    if (verbose) {
                        UtilTmp.printArrayUntil0(pos);
                    }
                    countEdeges = rollback(countEdeges, pos, edgesAdded, lastgraph);
                    UtilTmp.bfs(lastgraph, bfs, v);
                    sincronizarVerticesIncompletos(lastgraph, vertices, incompletVertices);
                    continue;
                }

                Integer peso = null;
                Integer bestVal = null;

                for (Integer p : poss) {
                    UtilTmp.arrayCopy(bfs, bfsTmp);
                    Integer tmpEdge = (Integer) lastgraph.addEdge(v, p);
                    UtilTmp.revisitVertex(v, bfsTmp, lastgraph);
                    int pesoLocal = 0;
                    for (int z = 0; z < bfsTmp.length; z++) {
                        if (bfsTmp[z] > 3) {
                            pesoLocal++;
                        }
                    }
                    if (peso == null || pesoLocal > peso) {
                        bestVal = p;
                        peso = pesoLocal;
                        bestVals.clear();
                        bestVals.add(p);
                    } else if (peso == pesoLocal) {
                        bestVals.add(p);
                    }
                    lastgraph.removeEdge(tmpEdge);
                }

                if (idx >= bestVals.size()) {//roolback
                    if (verbose) {
                        UtilTmp.printArrayUntil0(pos);
                    }
                    countEdeges = rollback(countEdeges, pos, edgesAdded, lastgraph);
                    UtilTmp.bfs(lastgraph, bfs, v);
                    sincronizarVerticesIncompletos(lastgraph, vertices, incompletVertices);
                    continue;
                }

                bestVal = bestVals.get(idx);
                edgesAdded[countEdeges] = (Integer) lastgraph.addEdge(v, bestVal);
                pos[countEdeges]++;
                countEdeges++;
                UtilTmp.revisitVertex(v, bfs, lastgraph);

                if (verbose) {
                    System.out.print("add(");
                    System.out.print(v);
                    System.out.print(", ");
                    System.out.print(bestVal);
                    System.out.print(")| ");
                    System.out.print(len - countEdeges);
                    System.out.println();
                }
            }
            sincronizarVerticesIncompletos(lastgraph, vertices, incompletVertices);
        }

        try {
            System.out.print("Added-Edges: ");
            for (int i = 0; i < len; i++) {
                Pair endpoints = lastgraph.getEndpoints(edgesAdded[i]);
                System.out.print(endpoints);
                System.out.print(", ");
            }
        } catch (Exception e) {
        } finally {
            System.out.println();
        }

        System.out.println("Final Graph: ");
        String edgeString = lastgraph.getEdgeString();
        System.out.println(edgeString);
//        UtilTmp.dumpString(edgeString);    }
    }

    public static int rollback(int countEdeges, int[] pos, Integer[] edgesAdded, UndirectedSparseGraphTO hoff) {
        for (int i = countEdeges; i < pos.length; i++) {
            pos[i] = 0;
        }
        countEdeges--;
        Integer ultima = edgesAdded[countEdeges];
        hoff.removeEdge(ultima);
        edgesAdded[countEdeges] = null;
        return countEdeges;
    }

    public static void sincronizarListaPossibilidades(Integer[] bfs, UndirectedSparseGraphTO lastgraph, List<Integer> poss, Integer v) {
        poss.clear();
        UtilTmp.bfs(lastgraph, bfs, v);
        for (Integer i = 0; i < bfs.length; i++) {
            if (bfs[i] > 3 && lastgraph.degree(i) < K) {
                poss.add(i);
            }
        }
    }

    public static void sincronizarVerticesIncompletos(UndirectedSparseGraphTO lastgraph, Collection<Integer> vertices, List<Integer> incompletVertices) {
        incompletVertices.clear();
        for (Integer v : vertices) {
            if (lastgraph.degree(v) < K) {
                incompletVertices.add(v);
            }
        }
    }

}

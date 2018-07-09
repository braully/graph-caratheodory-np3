package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author strike
 */
public class MooreGraphGen2 {

    private static final boolean verbose = true;

    private static int K = 57;
    private static int NUM_ARESTAS = ((K * K + 1) * K) / 2;

    public static void main(String... args) {
        K = 7;

        List<Integer> startArray = new ArrayList<>();

        if (args != null && args.length > 0) {
            for (String str : args) {
                str = str.replaceAll("\\D", "").trim();
                Integer val = Integer.parseInt(str);
                startArray.add(val);
            }
            System.out.println("Starting with: arr[" + startArray.size() + "]=" + startArray);
        }

        if (K == 7) {
            NUM_ARESTAS = ((K * K + 1) * K) / 2;
            UndirectedSparseGraphTO graphTemplate = HoffmanGraphGen.subgraph;
            generateGraph(K, NUM_ARESTAS, graphTemplate, startArray);
        }
        if (K == 57) {
            NUM_ARESTAS = ((K * K + 1) * K) / 2;
            UndirectedSparseGraphTO graphTemplate = LGMGen.subgraph;
            generateGraph(K, NUM_ARESTAS, graphTemplate, startArray);
        }

    }

    private static void generateGraph(int K, int NUM_ARESTAS, UndirectedSparseGraphTO graphTemplate, List<Integer> startArray) {
        Collection<Integer> vertices = graphTemplate.getVertices();
        int numvert = vertices.size();
        int numvertincompletos = 0;
        List<Integer> incompletVertices = new ArrayList<>();
        int len = NUM_ARESTAS - graphTemplate.getEdgeCount();

        for (Integer v : vertices) {
            if (graphTemplate.degree(v) < K) {
                incompletVertices.add(v);
            }
        }
        numvertincompletos = numvert - incompletVertices.size();

        long lastime = System.currentTimeMillis();

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
        for (Integer i : incompletVertices) {
            bfsAtual[i] = new Integer[numvert];
//            UtilTmp.bfs(graphTemplate, bfsAtual[i], i);
        }

        int[] pos = new int[len];
        for (int i = 0; i < len; i++) {
            pos[i] = 0;
        }

        if (startArray != null) {
            for (int i = 0; i < startArray.size(); i++) {
                Integer val = startArray.get(i);
                if (val > 0) {
                    pos[i] = val - 1;
                }
            }
        }

        UndirectedSparseGraphTO lastgraph = graphTemplate.clone();
        List<Integer> poss = new ArrayList<>();
        List<Integer> bestVals = new ArrayList<>();
        Integer[] bfsTmp = new Integer[numvert];
        Deque<Integer> stack = new LinkedList<>();
//        Integer[] bfsWork = new Integer[numvert];
//        Integer[][] bfsBackup = new Integer[K][numvert];

        while (!incompletVertices.isEmpty() && lastgraph.getEdgeCount() < NUM_ARESTAS) {
            Integer v = incompletVertices.get(0);

            int offset = stack.size();
            int deltaLocal = calcDetalAtual(incompletVertices, lastgraph, bfsAtual);
            while (lastgraph.degree(v) < K) {
                poss.clear();
                Integer[] bfsWork = bfsAtual[v];
                sincronizarListaPossibilidades(bfsWork, lastgraph, poss, v);
                for (Integer w = 0; w < bfsWork.length; w++) {
                    if (bfsWork[w] > 3 && lastgraph.degree(w) < K) {
                        poss.add(w);
                    }
                }

                int dv = lastgraph.degree(v);
                int posssize = poss.size();
                int idx = pos[stack.size()];

                if (posssize == 0 || posssize < K - dv || idx >= posssize) {
                    if (verbose) {
                        UtilTmp.printArrayUntil0(pos);
                    }
                    rollback(pos, stack, lastgraph);
//                    UtilTmp.arrayCopy(bfsBackup[stack.size() - offset], bfsWork);
                    sincronizarVerticesIncompletos(lastgraph, vertices, incompletVertices);
                    calcDetalAtual(incompletVertices, lastgraph, bfsAtual);
                    continue;
                }

                Integer peso = null;
                Integer bestVal = null;

                for (Integer p : poss) {
                    Integer tmpEdge = (Integer) lastgraph.addEdge(v, p);
                    int deltaGlobal = 0;
                    for (Integer x : incompletVertices) {
                        for (Integer i = 0; i < bfsAtual[x].length; i++) {
                            UtilTmp.arrayCopy(bfsAtual[x], bfsTmp);
                            UtilTmp.revisitVertex(x, bfsTmp, lastgraph);
                            if (bfsAtual[x][i] > 3 && lastgraph.degree(i) < K) {
                                deltaGlobal++;
                            }
                        }
                    }

//                    int pesoLocal = deltaLocal - deltaGlobal;
                    int pesoLocal = deltaGlobal;

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
                    rollback(pos, stack, lastgraph);
//                    UtilTmp.arrayCopy(bfsBackup[stack.size() - offset], bfsWork);
                    sincronizarVerticesIncompletos(lastgraph, vertices, incompletVertices);
                    calcDetalAtual(incompletVertices, lastgraph, bfsAtual);
                    continue;
                }

                //Backup bfs
//                UtilTmp.arrayCopy(bfsWork, bfsBackup[stack.size() - offset]);
                bestVal = bestVals.get(idx);
                Integer ed = (Integer) lastgraph.addEdge(v, bestVal);
                pos[stack.size()]++;
                stack.push(ed);
                UtilTmp.revisitVertex(v, bfsWork, lastgraph);
                calcDetalAtual(incompletVertices, lastgraph, bfsAtual);

                if (verbose) {
                    System.out.print("add(");
                    System.out.print(v);
                    System.out.print(", ");
                    System.out.print(bestVal);
                    System.out.print(")| ");
                    System.out.print(stack.size());
                    System.out.print("/");
                    System.out.print(len);
                    System.out.println();
                }
                if (System.currentTimeMillis() - lastime > UtilTmp.ALERT_HOUR) {
                    lastime = System.currentTimeMillis();
                    UtilTmp.dumpArrayUntil0(pos);
                    System.out.print("last-add(");
                    System.out.print(v);
                    System.out.print(", ");
                    System.out.print(bestVal);
                    System.out.print(")| ");
                    System.out.print(stack.size());
                    System.out.print("/");
                    System.out.print(len);
                    System.out.print(" - ");
                    System.out.print(numvertincompletos - incompletVertices.size());
                    System.out.print("/");
                    System.out.print(numvertincompletos);

                    System.out.println();

                }
            }
            sincronizarVerticesIncompletos(lastgraph, vertices, incompletVertices);
        }

        try {
            System.out.print("Added-Edges: ");
            List<Integer> stackList = (List<Integer>) stack;
            for (int i = stackList.size() - 1; i >= 0; i--) {
                Pair endpoints = lastgraph.getEndpoints(stackList.get(i));
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

    public static int calcDetalAtual(List<Integer> incompletVertices, UndirectedSparseGraphTO lastgraph, Integer[][] bfsAtual) {
        int deltaLocal = 0;
        for (Integer x : incompletVertices) {
            UtilTmp.bfs(lastgraph, bfsAtual[x], x);
            for (Integer i = 0; i < bfsAtual[x].length; i++) {
                if (bfsAtual[x][i] > 3 && lastgraph.degree(i) < K) {
                    deltaLocal++;
                }
            }
        }
        return deltaLocal;
    }

    public static void rollback(int[] pos, Deque<Integer> stack, UndirectedSparseGraphTO hoff) {
        for (int i = stack.size(); i < pos.length; i++) {
            pos[i] = 0;
        }
        hoff.removeEdge(stack.pop());
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
        vertices.stream().filter((v) -> (lastgraph.degree(v) < K)).forEachOrdered((v) -> {
            incompletVertices.add(v);
        });
    }

}

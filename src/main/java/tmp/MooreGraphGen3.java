package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 *
 * @author strike
 */
public class MooreGraphGen3 {

    private static final boolean verbose = true;

    private static int K = 0;
    private static int NUM_ARESTAS = 0;

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

    private static void generateGraph(int K, int numArestas, UndirectedSparseGraphTO graphTemplate, List<Integer> startArray) {
        Collection<Integer> vertices = graphTemplate.getVertices();
        int numvert = vertices.size();
//        List<Integer> incompletVertices = new ArrayList<>();
        TreeSet<Integer> incompletVertices = new TreeSet<>();
        int len = numArestas - graphTemplate.getEdgeCount();

        for (Integer v : vertices) {
            if (graphTemplate.degree(v) < K) {
                incompletVertices.add(v);
            }
        }
        int numvertincompletos = incompletVertices.size();

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

        System.out.println("Montando mapa BFS Inicial");
        Integer[][] bfsAtual = new Integer[numvert][numvert];

        for (Integer inc : incompletVertices) {
            UtilTmp.bfs(graphTemplate, bfsAtual[inc], inc);
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
        Integer[] bfsTmp = new Integer[numvert];
        Deque<Integer> stack = new LinkedList<>();

        int[] sortindex = new int[numvert];

        Comparator<Integer> comparatorBySortIndex = new Comparator<Integer>() {
            @Override
            public int compare(Integer t, Integer t1) {
                int compare = 0;
                compare = Integer.compare(sortindex[t1], sortindex[t]);//maximizar
//                compare = Integer.compare(sortindex[t], sortindex[t1]);//minimizar
                if (compare == 0) {
                    compare = Integer.compare(t, t1);
                }
                return compare;
            }
        };

//        while (lastgraph.getEdgeCount() < numArestas) {
        while (!incompletVertices.isEmpty()) {
            Integer v = incompletVertices.first();
            poss.clear();
            for (Integer i = 0; i < bfsAtual[v].length; i++) {
                if (bfsAtual[v][i] > 3 && lastgraph.degree(i) < K) {
                    poss.add(i);
                }
            }

            int dv = lastgraph.degree(v);
            int posssize = poss.size();
            int idx = pos[stack.size()];

            if (posssize == 0 || posssize < K - dv || idx >= posssize) {
                for (int i = stack.size(); i < pos.length; i++) {
                    pos[i] = 0;
                }
                Integer edge = stack.pop();
                Pair endpoints = lastgraph.getEndpoints(edge);
                Integer f = (Integer) endpoints.getFirst();
                Integer s = (Integer) endpoints.getSecond();
                lastgraph.removeEdge(edge);
                UtilTmp.bfs(lastgraph, bfsAtual[f], f);
                UtilTmp.bfs(lastgraph, bfsAtual[s], s);
                incompletVertices.add(f);
                incompletVertices.add(s);
                if (!v.equals(f) && !v.equals(s)) {
                    UtilTmp.bfs(lastgraph, bfsAtual[v], v);
                }

                if (verbose) {
                    System.out.print("remove(");
                    System.out.print(f);
                    System.out.print(", ");
                    System.out.print(s);
                    System.out.println(");");
                    UtilTmp.printArrayUntil0(pos);
                }

                continue;
            }

            for (Integer p : poss) {
                sortindex[p] = 0;
                UtilTmp.arrayCopy(bfsAtual[v], bfsTmp);
                Integer tmpEdge = (Integer) lastgraph.addEdge(v, p);
                UtilTmp.revisitVertex(v, bfsTmp, lastgraph);
                for (int z = 0; z < bfsTmp.length; z++) {
                    if (bfsTmp[z] > 3) {
                        sortindex[p]++;
                    }
                }
                lastgraph.removeEdge(tmpEdge);
            }
            Collections.sort(poss, comparatorBySortIndex);

            //Backup bfs
            Integer val = poss.get(idx);
            Integer ed = (Integer) lastgraph.addEdge(v, val);
            pos[stack.size()]++;
            stack.push(ed);
            UtilTmp.revisitVertex(v, bfsAtual[v], lastgraph);
            UtilTmp.revisitVertex(val, bfsAtual[val], lastgraph);
            if (lastgraph.degree(v) >= K) {
                incompletVertices.remove(v);
            }
            if (lastgraph.degree(val) >= K) {
                incompletVertices.remove(val);
            }

            if (verbose) {
                System.out.print("add(");
                System.out.print(v);
                System.out.print(", ");
                System.out.print(val);
                System.out.print(")| ");
                System.out.print(stack.size());
                System.out.print("/");
                System.out.print(len);

                System.out.print(" - ");
                System.out.print((numvertincompletos - incompletVertices.size()));
                System.out.print("/");
                System.out.print(numvertincompletos);
                System.out.println();
            }
            if (System.currentTimeMillis() - lastime > UtilTmp.ALERT_HOUR) {
                lastime = System.currentTimeMillis();
                UtilTmp.dumpArrayUntil0(pos);
                StringBuilder sb = new StringBuilder();
                sb.append("last-add(");
                sb.append(v);
                sb.append(", ");
                sb.append(val);
                sb.append(")| ");
                sb.append(stack.size());
                sb.append("/");
                sb.append(len);
                sb.append(" - ");
                sb.append((numvertincompletos - incompletVertices.size()));
                sb.append("/");
                sb.append(numvertincompletos);
                sb.append("\n");
                UtilTmp.dumpString(sb.toString());
            }
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

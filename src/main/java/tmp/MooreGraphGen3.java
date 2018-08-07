package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author strike
 */
public class MooreGraphGen3 {

    private static final boolean verbose = true;

    private static int K = 0;
    private static int NUM_ARESTAS = 0;
    private static Queue<Integer> queue = new LinkedList<Integer>();

    public static void main(String... args) {
        K = 57;

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
        int min = numvert;
        int max = 0;
        Map<Integer, List<Integer>> possibilidadesIniciais = new HashMap<>();
        Map<Integer, Set<Integer>> possibilidadesAtuais = new HashMap<>();

        List<Integer> incompletVertices = new ArrayList<>();
        TreeSet<Integer> incompletSet = new TreeSet<>();
        int len = numArestas - graphTemplate.getEdgeCount();

        for (Integer v : vertices) {
            if (graphTemplate.degree(v) < K) {
                incompletVertices.add(v);
                incompletSet.add(v);
                if (v > max) {
                    max = v;
                }
                if (v < min) {
                    min = v;
                }
                possibilidadesIniciais.put(v, new ArrayList<>());
                possibilidadesAtuais.put(v, new HashSet<>());
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
        Integer[][] bfsAtual = new Integer[numvert][numvert + 1];

        for (Integer inc : incompletVertices) {
            bfs(graphTemplate, bfsAtual[inc], inc);
            for (Integer i = min; i <= max; i++) {
                if (bfsAtual[inc][i] > 3) {
                    possibilidadesIniciais.get(inc).add(i);
                    possibilidadesAtuais.get(inc).add(i);
                }
            }
            bfsAtual[inc][numvert] = possibilidadesIniciais.get(inc).size();
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
        Integer[] bfstmpv = new Integer[numvert + 1];
        Integer[] bfstmpf = new Integer[numvert + 1];
        Deque<Integer> stack = new LinkedList<>();

        int[] sortindex = new int[numvert];

        Comparator<Integer> comparatorBySortIndex = (Integer t, Integer t1) -> {
            int compare = 0;
            compare = Integer.compare(sortindex[t1], sortindex[t]);//maximizar
//                compare = Integer.compare(sortindex[t], sortindex[t1]);//minimizar
            if (compare == 0) {
                compare = Integer.compare(t, t1);
            }
            return compare;
        };

        Comparator<Integer> comparatorByRemain = (Integer t, Integer t1) -> {
            int compare = 0;
//                compare = Integer.compare(bfsAtual[t][numvert], bfsAtual[t1][numvert]);
            compare = Integer.compare(bfsAtual[t1][numvert], bfsAtual[t][numvert]);
            if (compare == 0) {
                compare = Integer.compare(t, t1);
            }
            return compare;
        };

//        while (lastgraph.getEdgeCount() < numArestas) {
        long contr1 = 0;
        long contr2 = 0;
        long contr3 = 0;
        long contr4 = 0;
        boolean r4 = false;
        while (!incompletVertices.isEmpty()) {
//            Integer v = incompletVertices.first();
            Integer v = incompletVertices.get(0);
            poss.clear();
            for (Integer i = min; i <= max; i++) {
                if (lastgraph.degree(i) < K && bfsAtual[v][i] > 3 && bfsAtual[i][v] > 3) {
                    poss.add(i);
                }
            }

            int dv = lastgraph.degree(v);
            int posssize = poss.size();
            int idx = pos[stack.size()];
            boolean r1 = posssize == 0;
            boolean r2 = posssize < K - dv;
            boolean r3 = idx >= posssize;
            if (r1 || r2 || r3 || r4) {
                for (int i = stack.size(); i < pos.length; i++) {
                    pos[i] = 0;
                }
                Integer edge = stack.pop();
                Pair endpoints = lastgraph.getEndpoints(edge);
                Integer f = (Integer) endpoints.getFirst();
                Integer s = (Integer) endpoints.getSecond();
                lastgraph.removeEdge(edge);
                bfs(lastgraph, bfsAtual[f], f);
                recalcPossibilidades(bfsAtual[f], possibilidadesIniciais.get(f), possibilidadesAtuais.get(f));
                bfs(lastgraph, bfsAtual[s], s);
                recalcPossibilidades(bfsAtual[s], possibilidadesIniciais.get(s), possibilidadesAtuais.get(s));
                if (incompletSet.add(f)) {
                    incompletVertices.add(f);
                }
                if (incompletSet.add(s)) {
                    incompletVertices.add(s);
                }
                if (!v.equals(f) && !v.equals(s)) {
                    bfs(lastgraph, bfsAtual[v], v);
                }
                if (r1) {
                    contr1++;
                } else if (r2) {
                    contr2++;
                } else if (r3) {
                    contr3++;
                }
                if (r4) {
                    contr4++;
                }

                if (verbose) {
                    System.out.print("remove(");
                    System.out.print(f);
                    System.out.print(", ");
                    System.out.print(s);
                    System.out.print("); ");
                    System.out.print("r1=");
                    System.out.print(contr1);
                    System.out.print(" r2=");
                    System.out.print(contr2);
                    System.out.print(" r3=");
                    System.out.print(contr3);
                    System.out.print(" r4=");
                    System.out.print(contr4);
                    System.out.println();
                    UtilTmp.printArrayUntil0(pos);
                }
                r4 = false;
                continue;
            }

            rankearPossibilidades(poss, sortindex, bfsAtual, v, bfstmpv, bfstmpf, lastgraph, incompletSet, comparatorBySortIndex);

            //Add Edge
            Integer val = poss.get(idx);
            Integer ed = (Integer) lastgraph.addEdge(v, val);
            pos[stack.size()]++;
            stack.push(ed);
//            revisitVertex(v, bfsAtual[v], lastgraph);
//            revisitVertex(val, bfsAtual[val], lastgraph);
            revisitVertex(v, bfsAtual, lastgraph, possibilidadesAtuais);
            revisitVertex(val, bfsAtual, lastgraph, possibilidadesAtuais);
            if (lastgraph.degree(v) >= K) {
                incompletSet.remove(v);
                incompletVertices.remove(v);
                if (verbose) {
                    int count = 0;
                    for (int i = min; i <= max; i++) {
                        if (bfsAtual[v][i] > 3) {
                            count++;
                        }
                    }
                    System.out.print("Vertice ");
                    System.out.print(v);
                    System.out.print(" complete... resto=");
                    System.out.print(count);
                    System.out.println();
                }
            }
            if (lastgraph.degree(val) >= K) {
                incompletVertices.remove(val);
                incompletSet.remove(val);
                if (verbose) {
                    int count = 0;
                    for (int i = min; i <= max; i++) {
                        if (bfsAtual[val][i] > 3) {
                            count++;
                        }
                    }
                    System.out.print("Vertice ");
                    System.out.print(val);
                    System.out.print(" complete... resto=");
                    System.out.print(count);
                    System.out.println();
                }
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
                System.out.print("\t");
//                System.out.println();
            }
            if (System.currentTimeMillis() - lastime > UtilTmp.ALERT_HOUR_12) {
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

            r4 = recalcSortIndexVertices(incompletVertices, bfsAtual, numvert, lastgraph, possibilidadesAtuais, K, r4);
            reOrderIncompleteVertices(incompletVertices, comparatorByRemain);
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
        if (K > 7) {
            UtilTmp.dumpString(edgeString);
        }
    }

    private static void reOrderIncompleteVertices(List<Integer> incompletVertices, Comparator<Integer> comparatorByRemain) {
        Collections.sort(incompletVertices, comparatorByRemain);
    }

    private static boolean recalcSortIndexVertices(List<Integer> incompletVertices, Integer[][] bfsAtual, int numvert, UndirectedSparseGraphTO lastgraph, Map<Integer, Set<Integer>> possibilidadesAtuais, int K1, boolean r4) {
        for (Integer i : incompletVertices) {
            bfsAtual[i][numvert] = 0;
            int di = lastgraph.degree(i);
            for (Integer j : possibilidadesAtuais.get(i)) {
                if (di < K1 && bfsAtual[i][j] > 3 && bfsAtual[j][i] > 3) {
                    bfsAtual[i][numvert]++;
                }
            }
            if (bfsAtual[i][numvert] < K1 - di) {
                r4 = true;
                if (verbose) {
                    System.out.print("Possibilidades esgotadas: ");
                    System.out.print(i);
                    System.out.print(" - ");
                    System.out.print(bfsAtual[i][numvert]);
                    System.out.print("/");
                    System.out.print(K1 - di);
                    System.out.println();
                }
            }
        }
        return r4;
    }

    private static void rankearPossibilidades(List<Integer> poss, int[] sortindex, Integer[][] bfsAtual, Integer v, Integer[] bfstmpv, Integer[] bfstmpf, UndirectedSparseGraphTO lastgraph, Set<Integer> incompleteVertices, Comparator<Integer> comparatorBySortIndex) {
        for (Integer p : poss) {
            sortindex[p] = 0;
            UtilTmp.arrayCopy(bfsAtual[v], bfstmpv);
            UtilTmp.arrayCopy(bfsAtual[p], bfstmpf);
            Integer tmpEdge = (Integer) lastgraph.addEdge(v, p);
            UtilTmp.revisitVertex(v, bfstmpv, lastgraph);
            UtilTmp.revisitVertex(p, bfstmpf, lastgraph);
            lastgraph.removeEdge(tmpEdge);
            for (Integer z : incompleteVertices) {
//                    if (bfstmpv[z] > 3) {
//                        sortindex[p]++;
//                    }
                if (bfstmpv[z] > 3) {
                    sortindex[p]++;
                }
                if (bfsAtual[p][z] > 3 && bfstmpf[z] <= 3) {
                    sortindex[p]--;
                }
                if (bfsAtual[v][z] > 3 && bfstmpv[z] <= 3) {
                    sortindex[p]--;
                }
            }
        }
        Collections.sort(poss, comparatorBySortIndex);
    }

    public static void bfs(UndirectedSparseGraphTO<Integer, Integer> subgraph, Integer[] bfs, Integer v) {
        for (int i = 0; i < bfs.length; i++) {
            bfs[i] = null;
        }
        bfs[v] = 0;
        visitVertex(v, bfs, subgraph);
    }

    public static void visitVertex(Integer v, Integer[] bfs, UndirectedSparseGraphTO<Integer, Integer> curgraph) {
        queue.clear();
        queue.add(v);
        while (!queue.isEmpty()) {
            Integer poll = queue.poll();
            int depth = bfs[poll] + 1;
            Collection<Integer> ns = (Collection<Integer>) curgraph.getNeighborsUnprotected(poll);
            for (Integer nv : ns) {
                if (bfs[nv] == null) {
                    bfs[nv] = depth;
                    queue.add(nv);
                } else if (depth < bfs[nv]) {//revisit
                    bfs[nv] = depth;
                    queue.add(nv);
                }
            }
        }
    }

    static void revisitVertex(Integer hold, Integer[] bfs3, UndirectedSparseGraphTO<Integer, Integer> subgraph) {
        if (hold == null || bfs3[hold] != 0) {
            throw new IllegalStateException("BFS From another root");
        }
        visitVertex(hold, bfs3, subgraph);
    }

    static void revisitVertex(Integer hold, Integer[][] bfs3,
            UndirectedSparseGraphTO<Integer, Integer> curgraph,
            Map<Integer, Set<Integer>> possibilidades) {
        Integer v = hold;
        Integer[] bfs = bfs3[hold];
        queue.clear();
        queue.add(v);
        while (!queue.isEmpty()) {
            Integer poll = queue.poll();
            int depth = bfs[poll] + 1;
            Collection<Integer> ns = (Collection<Integer>) curgraph.getNeighborsUnprotected(poll);
            for (Integer nv : ns) {
                if (bfs[nv] == null) {
                    bfs[nv] = depth;
                    queue.add(nv);
                } else if (depth < bfs[nv]) {//revisit
                    if (bfs[nv] == 4) {
                        possibilidades.get(v).remove(nv);
//                        bfs[bfs.length - 1]--;
                    }
                    bfs[nv] = depth;
                    queue.add(nv);
                }
            }
        }
        bfs[bfs.length - 1] = possibilidades.get(v).size();
    }

    private static void recalcPossibilidades(Integer[] bfs, List<Integer> inicial, Collection<Integer> atual) {
        atual.clear();
        for (Integer x : inicial) {
            if (bfs[x] == 4) {
                atual.add(x);
            }
        }
        bfs[bfs.length - 1] = atual.size();
    }
}

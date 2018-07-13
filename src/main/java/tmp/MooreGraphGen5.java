package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author strike
 */
public class MooreGraphGen5 {

    private static final boolean verbose = true;

    private static int K = 0;
    private static int NUM_ARESTAS = 0;
    private static Queue<Integer> queue = new LinkedList<Integer>();
    static long lastime = System.currentTimeMillis();
    static long contr1 = 0;
    static long contr2 = 0;
    static long contr3 = 0;
    static long contr4 = 0;
    static boolean r4 = false;

    public static void main(String... args) {
//        K = 57;
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

    static class BFSProcessamento {

        Integer[][] bfsAtual;
        Integer linha;
        Integer coluna;
        Map<Integer, List<Integer>> possibilidadesIniciais = new HashMap<>();
        Map<Integer, List<Integer>> possibilidadesAtuais = new HashMap<>();
        Collection<Integer> vertices = null;
        List<Integer> incompletVertices = new ArrayList<>();
        UndirectedSparseGraphTO graph = null;

        Comparator<Integer> comparatorByRemain = (Integer t, Integer t1) -> {
            int compare = 0;
            compare = Integer.compare(possibilidadesAtuais.get(t1).size(), possibilidadesAtuais.get(t).size());
            if (compare == 0) {
                compare = Integer.compare(t, t1);
            }
            return compare;
        };

        private BFSProcessamento(int linha, int coluna) {
            bfsAtual = new Integer[linha][coluna];
            this.linha = linha;
            this.coluna = coluna;
        }

        private BFSProcessamento(int linha) {
            bfsAtual = new Integer[linha][];
            this.linha = linha;
        }

        private void loadGraph(UndirectedSparseGraphTO graph) {
            this.graph = graph;
            vertices = graph.getVertices();
            for (Integer v : vertices) {
                if (graph.degree(v) < K) {
                    incompletVertices.add(v);
                    possibilidadesIniciais.put(v, new ArrayList<>());
                    possibilidadesAtuais.put(v, new ArrayList<>());
                }
            }

            for (Integer inc : incompletVertices) {
                bfs(inc);
                for (Integer i : incompletVertices) {
                    if (get(inc, i) > 3) {
                        possibilidadesIniciais.get(inc).add(i);
                        possibilidadesAtuais.get(inc).add(i);
                    }
                }
//            bfsAtual[inc][numvert] = possibilidadesIniciais.get(inc).size();
            }
        }

        private void rollback(Integer v, Integer f, Integer s) {
            bfs(f);
            recalcPossibilidades(f);
            bfs(s);
            recalcPossibilidades(s);
            if (!incompletVertices.contains(v)) {
                incompletVertices.add(f);
            }
            if (!incompletVertices.contains(s)) {
                incompletVertices.add(s);
            }
            if (!v.equals(f) && !v.equals(s)) {
                bfs(v);
            }
        }

        private void bfs(Integer inc) {
            for (Integer i : vertices) {
                set(inc, i, null);
            }
            set(inc, inc, 0);
            visitVertex(inc);
        }

        public void visitVertex(Integer v) {
            queue.clear();
            queue.add(v);
            while (!queue.isEmpty()) {
                Integer poll = queue.poll();
                int depth = get(v, poll) + 1;
                Collection<Integer> ns = (Collection<Integer>) graph.getNeighborsUnprotected(poll);
                for (Integer nv : ns) {
                    evalBfs(v, nv, depth);
                }
            }
        }

        private void evalBfs(Integer hold, Integer nv, int depth) {
            Integer cur = get(hold, nv);
            if (cur == null) {
                set(hold, nv, depth);
                queue.add(nv);
            } else if (depth < cur) {//revisit
                set(hold, nv, depth);
                queue.add(nv);
            }
        }

        private void revisitVertex(Integer v) {
            visitVertex(v);
        }

        private void set(Integer inc, Integer i, Integer val) {
            bfsAtual[inc][i] = val;
        }

        private Integer get(Integer inc, Integer i) {
            return bfsAtual[inc][i];
        }

        private void recalcPossibilidades(Integer f) {

        }

        private int numVerticesIncompletos() {
            return incompletVertices.size();
        }

        private boolean temVerticesIncompletos() {
            return !incompletVertices.isEmpty();
        }

        private Integer proximoVertice() {
            return incompletVertices.get(0);
        }

        private List<Integer> possibilidadesAtuais(Integer v) {
            return possibilidadesAtuais.get(v);
        }

        private void rankearPossibilidades(Integer v) {

        }

        private boolean recalcSortIndexVertices() {
//            Collections.sort(incompletVertices, comparatorByRemain);
            return false;
        }
    }

    private static void generateGraph(int K, int numArestas, UndirectedSparseGraphTO graphTemplate, List<Integer> startArray) {
        UndirectedSparseGraphTO lastgraph = graphTemplate.clone();
        Collection<Integer> vertices = lastgraph.getVertices();
        int numvert = vertices.size();
        int len = numArestas - lastgraph.getEdgeCount();

//        Integer[][] bfsAtual = new Integer[numvert][numvert + 1];
        BFSProcessamento bfsAtual = new BFSProcessamento(numvert, numvert);
        bfsAtual.loadGraph(lastgraph);
        int numvertincompletos = bfsAtual.numVerticesIncompletos();
        System.out.print("Graph[");
        System.out.print(lastgraph.getVertexCount());
        System.out.print(", ");
        System.out.print(lastgraph.getEdgeCount());
        System.out.println("]");

        System.out.print("Incomplete vertices[");
        System.out.print(bfsAtual.incompletVertices.size());
        System.out.print("]: ");
        System.out.println(bfsAtual.incompletVertices);
        System.out.print("Edges remain: ");
        System.out.println(len);

        System.out.println("Montando mapa BFS Inicial");

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

//        while (lastgraph.getEdgeCount() < numArestas) {
        while (bfsAtual.temVerticesIncompletos()) {
            Integer v = bfsAtual.proximoVertice();
            List<Integer> poss = bfsAtual.possibilidadesAtuais(v);
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
                bfsAtual.rollback(v, f, s);
                verboseDumpRollback(r1, r2, r3, f, s, pos);
                r4 = false;
                continue;
            }

            bfsAtual.rankearPossibilidades(v);

            //Add Edge
            Integer val = poss.get(idx);
            Integer ed = (Integer) lastgraph.addEdge(v, val);
            pos[stack.size()]++;
            stack.push(ed);
//            revisitVertex(v, bfsAtual[v], lastgraph);
//            revisitVertex(val, bfsAtual[val], lastgraph);
            bfsAtual.revisitVertex(v);
            bfsAtual.revisitVertex(val);

            verboseDump(v, val, stack, len, numvertincompletos, bfsAtual, pos, K, lastgraph);
            r4 = bfsAtual.recalcSortIndexVertices();
//            reOrderIncompleteVertices(incompletVertices, comparatorByRemain);
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

    private static void verboseDumpRollback(boolean r1, boolean r2, boolean r3, Integer f, Integer s, int[] pos) {
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
    }

    private static void verboseDump(Integer v, Integer val, Deque<Integer> stack, int len, int numvertincompletos, BFSProcessamento bfsAtual, int[] pos, int K1, UndirectedSparseGraphTO lastgraph) {
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
            System.out.print(numvertincompletos - bfsAtual.numVerticesIncompletos());
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
            sb.append(numvertincompletos - bfsAtual.numVerticesIncompletos());
            sb.append("/");
            sb.append(numvertincompletos);
            sb.append("\n");
            UtilTmp.dumpString(sb.toString());
            if (K1 > 7) {
                UtilTmp.dumpString(lastgraph.getEdgeString(), ".graph");
            }
        }
    }

    private static boolean recalcSortIndexVertices(List<Integer> incompletVertices,
            Integer[][] bfsAtual, int numvert, UndirectedSparseGraphTO lastgraph,
            Map<Integer, Set<Integer>> possibilidadesAtuais) {
        boolean r4 = false;
        for (Integer i : incompletVertices) {
            bfsAtual[i][numvert] = 0;
            int di = lastgraph.degree(i);
            for (Integer j : possibilidadesAtuais.get(i)) {
                if (di < K && bfsAtual[i][j] > 3 && bfsAtual[j][i] > 3) {
                    bfsAtual[i][numvert]++;
                }
            }
            if (bfsAtual[i][numvert] < K - di) {
                r4 = true;
                if (verbose) {
                    System.out.print("Possibilidades esgotadas: ");
                    System.out.print(i);
                    System.out.print(" - ");
                    System.out.print(bfsAtual[i][numvert]);
                    System.out.print("/");
                    System.out.print(K - di);
                    System.out.println();
                }
            }
        }
        return r4;
    }

    private static void rankearPossibilidades(List<Integer> poss, int[] sortindex,
            Integer[][] bfsAtual, Integer v,
            Integer[] bfstmpv, Integer[] bfstmpf, UndirectedSparseGraphTO lastgraph,
            Set<Integer> incompleteVertices,
            Comparator<Integer> comparatorBySortIndex, Map<Integer, Set<Integer>> possibilidadesAtuais) {
        for (Integer p : poss) {
            sortindex[p] = 0;
            UtilTmp.arrayCopy(bfsAtual[v], bfstmpv);
            UtilTmp.arrayCopy(bfsAtual[p], bfstmpf);
//            Integer tmpEdge = (Integer) lastgraph.addEdge(v, p);
            revisitVertex(v, bfstmpv, lastgraph, p);
            revisitVertex(p, bfstmpf, lastgraph, v);
//            lastgraph.removeEdge(tmpEdge);
            for (Integer z : possibilidadesAtuais.get(v)) {
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
            for (Integer z : possibilidadesAtuais.get(p)) {
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
                evalBfs(bfs, nv, depth);
            }
        }
    }

    static void revisitVertex(Integer hold, Integer[] bfs3, UndirectedSparseGraphTO<Integer, Integer> subgraph) {
        if (hold == null || bfs3[hold] != 0) {
            throw new IllegalStateException("BFS From another root");
        }
        visitVertex(hold, bfs3, subgraph);
    }

    static void revisitVertex(Integer v, Integer[] bfs,
            UndirectedSparseGraphTO<Integer, Integer> curgraph,
            Integer extra) {
        if (v == null || bfs[v] != 0) {
            throw new IllegalStateException("BFS From another root");
        }
        queue.clear();
        queue.add(v);
        while (!queue.isEmpty()) {
            Integer poll = queue.poll();
            int depth = bfs[poll] + 1;
            Collection<Integer> ns = (Collection<Integer>) curgraph.getNeighborsUnprotected(poll);
            for (Integer nv : ns) {
                evalBfs(bfs, nv, depth);
            }
            if (poll.equals(v)) {
                evalBfs(bfs, extra, depth);
            } else if (poll.equals(extra)) {
                evalBfs(bfs, v, depth);
            }
        }
    }

    private static void evalBfs(Integer[] bfs, Integer nv, int depth) {
        if (bfs[nv] == null) {
            bfs[nv] = depth;
            queue.add(nv);
        } else if (depth < bfs[nv]) {//revisit
            bfs[nv] = depth;
            queue.add(nv);
        }
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

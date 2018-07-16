package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
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
public class MooreGraphGen6 {

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
        int[] countPos = null;
        Integer linha;
        Integer coluna;
        Map<Integer, List<Integer>> possibilidadesIniciais = new HashMap<>();
        Collection<Integer> vertices = null;
        TreeSet<Integer> incompletVertices = new TreeSet<>();
        List<Integer> incompletVerticesOriginal = new ArrayList<>();
        int numvertincompletosOriginal = 0;
        UndirectedSparseGraphTO graph = null;
        int[] degreecount = null;

        Comparator<Integer> comparatorByRemain = (Integer t, Integer t1) -> {
            int compare = 0;
            compare = Integer.compare(countPos[t], countPos[t1]);//minimizar
            if (compare == 0) {
                compare = Integer.compare(t, t1);
            }
            return compare;
        };

        private BFSProcessamento(int linha, int coluna) {
            bfsAtual = new Integer[linha][coluna];
            this.linha = linha;
            this.coluna = coluna;
            this.countPos = new int[linha];
        }

        private BFSProcessamento(int linha) {
            bfsAtual = new Integer[linha][];
            this.linha = linha;
        }

        private void loadGraph(UndirectedSparseGraphTO graph) {
            this.graph = graph;
            vertices = graph.getVertices();
            degreecount = new int[vertices.size()];
            for (Integer v : vertices) {
                degreecount[v] = graph.degree(v);
                if (degreecount[v] < K) {
                    incompletVertices.add(v);
                    incompletVerticesOriginal.add(v);
                    possibilidadesIniciais.put(v, new ArrayList<>());
//                    possibilidadesAtuais.put(v, new ArrayList<>());
                }
            }
            numvertincompletosOriginal = incompletVerticesOriginal.size();
            for (Integer inc : incompletVertices) {
                bfs(inc);
                for (Integer i : incompletVertices) {
                    if (get(inc, i) > 3) {
                        possibilidadesIniciais.get(inc).add(i);
//                        possibilidadesAtuais.get(inc).add(i);
                        countPos[inc]++;
                    }
                }
//            bfsAtual[inc][numvert] = possibilidadesIniciais.get(inc).size();
            }
        }

        private void removeEdgeAndRollback(Integer v, Integer edge) {
            Pair endpoints = graph.getEndpoints(edge);
            Integer f = (Integer) endpoints.getFirst();
            Integer s = (Integer) endpoints.getSecond();
//            getCountPos(v);
            graph.removeEdge(edge);
            bfs(f, true);
            countPos[f]--;
            bfs(s, true);
            countPos[s]--;
            if (!v.equals(f) && !v.equals(s)) {
                bfs(v, true);
            }
            if (degreecount[f]-- == K) {
                incompletVertices.add(f);
            }
            if (degreecount[s]-- == K) {
                incompletVertices.add(s);
            }
        }

        private void bfs(Integer inc, boolean recalc) {
            for (Integer i : vertices) {
                set(inc, i, null);
            }
            countPos[inc] = 0;
            set(inc, inc, 0);
            visitVertex(inc, recalc);
        }

        private void bfs(Integer inc) {
            for (Integer i : vertices) {
                set(inc, i, null);
            }
            set(inc, inc, 0);
            visitVertex(inc);
        }

        public void addEdge(int[] pos, Deque<Integer> stack, Integer v, Integer val, int len) {
            Integer ed = (Integer) graph.addEdge(v, val);
            pos[stack.size()]++;
            stack.push(ed);
            revisitVertex(v, val, true);
            countPos[v]--;
            revisitVertex(val, v, true);
            countPos[val]--;
            if (++degreecount[v] >= K) {
                incompletVertices.remove(v);
            }
            if (++degreecount[val] >= K) {
                incompletVertices.remove(val);
            }
            verboseDump(v, val, stack, len, pos, K);
        }

        public void revisitVertex(Integer v, Integer t, boolean recalc) {
            set(v, t, null);
            visitVertex(v, recalc);
        }

        public void visitVertex(Integer v) {
            visitVertex(v, false);
        }

        public void visitVertex(Integer v, boolean recalc) {
            queue.clear();
            queue.add(v);
            while (!queue.isEmpty()) {
                Integer poll = queue.poll();
                int depth = get(v, poll) + 1;
                Collection<Integer> ns = (Collection<Integer>) graph.getNeighborsUnprotected(poll);
                for (Integer nv : ns) {
                    evalBfs(v, nv, depth, recalc);
                }
            }
        }

        private void evalBfs(Integer hold, Integer nv, int depth, boolean recalPoss) {
            Integer cur = get(hold, nv);
            if (cur == null) {
                set(hold, nv, depth);
                queue.add(nv);
                if (recalPoss && depth == 4) {
                    countPos[nv]++;
                    countPos[hold]++;
                }
            } else if (depth < cur) {//revisit
                if (recalPoss && cur == 4) {
                    countPos[nv]--;
                    countPos[hold]--;
                }
                set(hold, nv, depth);
                queue.add(nv);
            }
        }

        private void set(Integer l, Integer c, Integer val) {
            if (l > c) {
                bfsAtual[c][l] = val;
            } else {
                bfsAtual[l][c] = val;
            }
        }

        private Integer get(Integer l, Integer c) {
            if (l > c) {
                return bfsAtual[c][l];
            } else {
                return bfsAtual[l][c];
            }
        }

        private int numVerticesIncompletos() {
            return incompletVertices.size();
        }

        private boolean temVerticesIncompletos() {
            return !incompletVertices.isEmpty();
        }

        private Integer proximoVertice() {
//            int v = incompletVertices.get(0);
//            for (int i = 1; i < incompletVertices.size(); i++) {
//                if (countPos[incompletVertices.get(i)] < countPos[v]) {
//                    v = incompletVertices.get(i);
//                }
//            }
//            return v;
            return incompletVertices.first();
        }

        private void verboseDump(Integer v, Integer val, Deque<Integer> stack, int len, int[] pos, int K1) {
            if (verbose) {
                System.out.print("+(");
                System.out.print(v);
                System.out.print(", ");
                System.out.print(val);
                System.out.print(")| ");
                System.out.print(stack.size());
                System.out.print("/");
                System.out.print(len);

                System.out.print(" - ");
                System.out.print(numvertincompletosOriginal - numVerticesIncompletos());
                System.out.print("/");
                System.out.print(numvertincompletosOriginal);
                System.out.print("\t");
//                System.out.println();
            }
            if (System.currentTimeMillis() - lastime > UtilTmp.ALERT_HOUR_12) {
                lastime = System.currentTimeMillis();
                UtilTmp.dumpArrayUntil0(pos);
                StringBuilder sb = new StringBuilder();
                sb.append("last-+(");
                sb.append(v);
                sb.append(", ");
                sb.append(val);
                sb.append(")| ");
                sb.append(stack.size());
                sb.append("/");
                sb.append(len);
                sb.append(" - ");
                sb.append(numvertincompletosOriginal - numVerticesIncompletos());
                sb.append("/");
                sb.append(numvertincompletosOriginal);
                sb.append("\n");
                UtilTmp.dumpString(sb.toString());
                if (K1 > 7) {
                    UtilTmp.dumpString(graph.getEdgeString(), ".graph");
                }
            }
        }

        private int getCountPos(Integer v) {
//            int cont = countPos[v];
//            int contreal = 0;
//            for (Integer i : possibilidadesIniciais.get(v)) {
//                if (get(v, i) == 4) {
//                    contreal++;
//                }
//            }
//            if (cont != contreal) {
//                throw new IllegalArgumentException("Count inconsistece " + v + " c=" + cont + " != c-real=" + contreal);
//            }
//            return cont;
            return countPos[v];
        }

        private Integer getOpcao(Integer v, int idx) {
            Integer ret = null;
            for (Integer i : possibilidadesIniciais.get(v)) {
                Integer d = get(v, i);
                if (i > v && d.equals(4)) {
                    if (idx == 0) {
                        ret = i;
                        break;
                    } else {
                        idx--;
                    }
                }
            }
            return ret;
        }

        private boolean verificaSemPossibilides() {
            boolean ret = false;
            for (Integer i : incompletVertices) {
                if (K - degreecount[i] > countPos[i]) {
                    ret = true;
                    break;
                }
            }
            return ret;
        }
    }

    private static void generateGraph(int K, int numArestas, UndirectedSparseGraphTO graphTemplate, List<Integer> startArray) {
        UndirectedSparseGraphTO lastgraph = graphTemplate.clone();
        Collection<Integer> vertices = lastgraph.getVertices();
        int numvert = vertices.size();
        int len = numArestas - lastgraph.getEdgeCount();

        BFSProcessamento bfsAtual = new BFSProcessamento(numvert, numvert);
        bfsAtual.loadGraph(lastgraph);

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
        while (bfsAtual.temVerticesIncompletos()) {
            Integer v = bfsAtual.proximoVertice();
            int dv = lastgraph.degree(v);
            int posssize = bfsAtual.getCountPos(v);
            int idx = pos[stack.size()];
            boolean r1 = posssize == 0;
            boolean r2 = posssize < K - dv;
            boolean r3 = idx >= posssize;
            r4 = bfsAtual.verificaSemPossibilides();
            if (r1 || r2 || r3 || r4) {
                for (int i = stack.size(); i < pos.length; i++) {
                    pos[i] = 0;
                }
                Integer edge = stack.pop();
                Pair endpoints = lastgraph.getEndpoints(edge);
                bfsAtual.removeEdgeAndRollback(v, edge);
                verboseDumpRollback(r1, r2, r3, endpoints, pos);
                r4 = false;
                continue;
            }
            //Add Edge
            Integer val = bfsAtual.getOpcao(v, idx);
            if (val == null) {
                for (int i = stack.size(); i < pos.length; i++) {
                    pos[i] = 0;
                }
                Integer edge = stack.pop();
                Pair endpoints = lastgraph.getEndpoints(edge);
                bfsAtual.removeEdgeAndRollback(v, edge);
                verboseDumpRollback(r1, r2, r3, endpoints, pos);
                r4 = false;
                continue;
            }
            bfsAtual.addEdge(pos, stack, v, val, len);
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

    private static void verboseDumpRollback(boolean r1, boolean r2, boolean r3, Pair<Integer> end, int[] pos) {
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
            System.out.print("-(");
            System.out.print(end.getFirst());
            System.out.print(", ");
            System.out.print(end.getSecond());
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
}

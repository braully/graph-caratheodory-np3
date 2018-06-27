package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import java.io.File;
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
public class HoffmanGraphGen {

    private static final long HOUR = 1000 * 60 * 60 * 12;
    private static String fileDump = System.getenv("user.dir") + File.separator + ".comb-moore-java.txt";
    private static final int NUM_ARESTAS = 175;
    private static final int K = 7;

    private static final UndirectedSparseGraphTO<Integer, Integer> subgraph = new UndirectedSparseGraphTO<>();

    static {
        subgraph.addEdgeC(0, 6).addEdgeC(1, 7).addEdgeC(2, 8).addEdgeC(3, 9).addEdgeC(4, 10).addEdgeC(5, 11).addEdgeC(0, 12).addEdgeC(12, 42).addEdgeC(12, 7)
                .addEdgeC(6, 13).addEdgeC(49, 42).addEdgeC(13, 42).addEdgeC(13, 1).addEdgeC(0, 14).addEdgeC(14, 43).addEdgeC(14, 8).addEdgeC(6, 15).addEdgeC(49, 43)
                .addEdgeC(15, 43).addEdgeC(15, 2).addEdgeC(0, 16).addEdgeC(16, 44).addEdgeC(16, 9).addEdgeC(6, 17).addEdgeC(49, 44).addEdgeC(17, 44).addEdgeC(17, 3).addEdgeC(0, 18)
                .addEdgeC(18, 45).addEdgeC(18, 10).addEdgeC(6, 19).addEdgeC(49, 45).addEdgeC(19, 45).addEdgeC(19, 4).addEdgeC(0, 20).addEdgeC(20, 46).addEdgeC(20, 11).addEdgeC(6, 21)
                .addEdgeC(49, 46).addEdgeC(21, 46).addEdgeC(21, 5).addEdgeC(1, 22).addEdgeC(22, 44).addEdgeC(22, 8).addEdgeC(7, 23).addEdgeC(49, 44).addEdgeC(23, 44).addEdgeC(23, 2).addEdgeC(1, 24)
                .addEdgeC(24, 45).addEdgeC(24, 9).addEdgeC(7, 25).addEdgeC(49, 45).addEdgeC(25, 45).addEdgeC(25, 3).addEdgeC(1, 26).addEdgeC(26, 46).addEdgeC(26, 10).addEdgeC(7, 27).addEdgeC(49, 46)
                .addEdgeC(27, 46).addEdgeC(27, 4).addEdgeC(1, 28).addEdgeC(28, 43).addEdgeC(28, 11).addEdgeC(7, 29).addEdgeC(49, 43).addEdgeC(29, 43).addEdgeC(29, 5).addEdgeC(2, 30).addEdgeC(30, 46)
                .addEdgeC(30, 9).addEdgeC(8, 31).addEdgeC(49, 46).addEdgeC(31, 46).addEdgeC(31, 3).addEdgeC(2, 32).addEdgeC(32, 42).addEdgeC(32, 10).addEdgeC(8, 33).addEdgeC(49, 42).addEdgeC(33, 42)
                .addEdgeC(33, 4).addEdgeC(2, 34).addEdgeC(34, 45).addEdgeC(34, 11).addEdgeC(8, 35).addEdgeC(49, 45).addEdgeC(35, 45).addEdgeC(35, 5).addEdgeC(3, 36).addEdgeC(36, 43).addEdgeC(36, 10)
                .addEdgeC(9, 37).addEdgeC(49, 43).addEdgeC(37, 43).addEdgeC(37, 4).addEdgeC(3, 38).addEdgeC(38, 42).addEdgeC(38, 11).addEdgeC(9, 39).addEdgeC(49, 42).addEdgeC(39, 42).addEdgeC(39, 5)
                .addEdgeC(4, 40).addEdgeC(40, 44).addEdgeC(40, 11).addEdgeC(10, 41).addEdgeC(49, 44).addEdgeC(41, 44).addEdgeC(41, 5).addEdgeC(47, 49).addEdgeC(49, 48).addEdgeC(47, 0).addEdgeC(48, 6)
                .addEdgeC(47, 1).addEdgeC(48, 7).addEdgeC(47, 2).addEdgeC(48, 8).addEdgeC(47, 3).addEdgeC(48, 9).addEdgeC(47, 4).addEdgeC(48, 10).addEdgeC(47, 5).addEdgeC(48, 11);
    }

    public static void main(String... args) {
        Collection<Integer> vertices = subgraph.getVertices();
        Collection pairs = subgraph.getPairs();
        List<Integer> incompletVertices = new ArrayList<>();
        int len = NUM_ARESTAS - subgraph.getEdgeCount();

        for (Integer v : vertices) {
            if (subgraph.degree(v) < K) {
                incompletVertices.add(v);
            }
        }

        System.out.print("Incomplete vertices[" + incompletVertices.size() + "]: ");
        System.out.println(incompletVertices);

        System.out.print("Edges remain: ");
        System.out.println(len);

        int totalComb = 1;
        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
        Integer[] bfs = new Integer[vertices.size()];

        Map<Integer, List<Integer>> mapossibilidades = calcPossibilidades(incompletVertices, bdl, bfs, vertices, totalComb);
    }

    public static Map<Integer, List<Integer>> calcPossibilidades(List<Integer> incompletVertices, BFSDistanceLabeler<Integer, Integer> bdl, Integer[] bfs, Collection<Integer> vertices, int totalComb) throws IllegalStateException {
        Map<Integer, List<Integer>> mapossibilidades = new HashMap<>();

        for (Integer v : incompletVertices) {
            List<Integer> listPoss = new ArrayList<>();
            bdl.labelDistances(subgraph, v);
            bfs(subgraph, bfs, v);
            for (Integer i : incompletVertices) {
                int distance = bdl.getDistance(subgraph, i);
                if (distance > 3) {
                    listPoss.add(i);
                }
                if (distance != bfs[i]) {
                    System.out.printf("BFS: ");
                    UtilTmp.printArray(bfs);
                    System.out.printf("JNG: [");
                    for (Integer dv : vertices) {
                        System.out.print(bdl.getDistance(subgraph, dv));
                        System.out.print(", ");
                    }
                    System.out.println("]");
                    throw new IllegalStateException("Divergencia no BFS na posição " + i);
                }
            }
            mapossibilidades.put(v, listPoss);
            int possv = listPoss.size();
            System.out.println(v + "[" + possv + "]=" + listPoss);
            totalComb = totalComb * possv;
        }
        return mapossibilidades;
    }

    static void bfs(UndirectedSparseGraphTO<Integer, Integer> subgraph, Integer[] bfs, Integer v) {
        for (int i = 0; i < bfs.length; i++) {
            bfs[i] = null;
        }
        bfs[v] = 0;
        visitVertex(v, bfs, subgraph);
    }

    private static void visitVertex(Integer v, Integer[] bfs, UndirectedSparseGraphTO<Integer, Integer> subgraph1) {
        Queue<Integer> queue = new LinkedList<Integer>();
        queue.add(v);
        while (!queue.isEmpty()) {
            Integer poll = queue.poll();
            int depth = bfs[poll] + 1;
            Collection<Integer> ns = (Collection<Integer>) subgraph1.getNeighbors(poll);
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

    private static boolean nextCombination(Integer[] comb, int maxcount) {
        int i = comb.length - 1;
        while (i >= 0 && comb[i]++ >= maxcount) {
            comb[i] = 0;
            i--;
        }
        return i >= 0;
    }

    private static boolean checkCombination(Integer[] comb) {
        return false;
    }
//        System.out.print("Total de combinaçoes possiveis: ");
//        System.out.println(totalComb);
//
//        int nverinc = incompletVertices.size();
//        Integer[] combseq = new Integer[nverinc * 3];
//        Integer[][] edgrests = new Integer[nverinc][3];
//        int count = 0;
//        Integer[] comb = new Integer[nverinc];
//        Integer[] arr = new Integer[nverinc * 3];
//        int tamListpos = 8;
//        while (count < combseq.length && count >= 0) {
//            int idx = count % 3;
//            Integer v = incompletVertices.get(idx);
//            List<Integer> listPoss = mapossibilidades.get(v);
//            if (combseq[count] >= tamListpos) {
//                combseq[count] = 0;
//                count--;
//                continue;
//            }
//            int val = listPoss.get(combseq[count]);
//            arr[count] = val;
//            arr[count] = 0;
//            combseq[count]++;
//            count++;
//        }
//
//        int maxcount = 8 * 7;
//        boolean hasnext = true;
//        boolean fit = false;
//
//        for (int i = 0; i < nverinc; i++) {
//            comb[i] = maxcount;
//        }
//        comb[nverinc - 1] = maxcount;
//
//        while (hasnext && !fit) {
//            UtilTmp.printArray(comb);
//            hasnext = nextCombination(comb, maxcount);
//            fit = checkCombination(comb);
//            UtilTmp.printArray(comb);
//        }
//
//        if (fit) {
//            System.out.println("Solução encontrada");
//        } else {
//            System.out.println("Solução não encontrada");
//        }
}

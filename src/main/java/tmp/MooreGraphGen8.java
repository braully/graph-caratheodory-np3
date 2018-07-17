package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author strike
 */
public class MooreGraphGen8 {

    private static final boolean verbose = true;

    private static int K = 57;
    private static int NUM_ARESTAS = ((K * K + 1) * K) / 2;

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

    private static void generateGraph(int K, int NUM_ARESTAS, UndirectedSparseGraphTO graphTemplate, List<Integer> startArray) {
        Collection<Integer> vertices = graphTemplate.getVertices();
        LinkedList<Integer> incompletVertices = new LinkedList<>();
        Map<Integer, List<Integer>> possibilidadesIniciais = new HashMap<>();
        int len = NUM_ARESTAS - graphTemplate.getEdgeCount();

        for (Integer v : vertices) {
            int remain = K - graphTemplate.degree(v);
            for (int i = 0; i < remain; i++) {
                if (graphTemplate.degree(v) < K) {
                    incompletVertices.add(v);
                }
            }
            if (remain > 0) {
                List<Integer> possi = possibilidadesIniciais.getOrDefault(v, new ArrayList<>());
                possi.add(v);
                possibilidadesIniciais.put(v, possi);
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

        int[] edgeArr = new int[incompletVertices.size()];
        for (int i = 0; i < incompletVertices.size(); i++) {
            edgeArr[i] = 0;
        }

        if (startArray != null) {
            for (int i = 0; i < startArray.size(); i++) {
                Integer val = startArray.get(i);
                if (val > 0) {
                    edgeArr[i] = val;
                }
            }
        }

        UndirectedSparseGraphTO lastgraph = graphTemplate.clone();
        int cont = 0;
        while (cont < edgeArr.length) {
            Integer v = incompletVertices.pollFirst();
            if ((cont & 1) == 0) {
                edgeArr[cont] = v;
            } else {
                lastgraph.addEdge(v, edgeArr[cont - 1]);
            }
            cont++;
        }

        try {
            System.out.print("Added-Edges: ");
            for (int i = 0; i < cont - 1; i++) {
                System.out.print(edgeArr[i]);
                System.out.print("-");
                System.out.print(edgeArr[i + 1]);
                System.out.print(", ");
            }
        } catch (Exception e) {
        } finally {
            System.out.println();
        }

        System.out.println("Final Graph: ");
        String edgeString = lastgraph.getEdgeString();
        System.out.println(edgeString);
    }
}

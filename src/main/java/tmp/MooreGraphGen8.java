package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
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
        LinkedList<Integer> trabalhoPorFazer = new LinkedList<>();
        Map<Integer, List<Integer>> caminhosPossiveis = new HashMap<>();
        Map<Integer, List<Integer>> caminhoPercorrido = new HashMap<>();
        int len = NUM_ARESTAS - graphTemplate.getEdgeCount();

        for (Integer v : vertices) {
            int remain = K - graphTemplate.degree(v);
            if (remain > 0) {
                trabalhoPorFazer.add(v);
                caminhosPossiveis.put(v, new ArrayList<>());
                caminhoPercorrido.put(v, new ArrayList<>());
            }
        }

        verboseInit(graphTemplate, trabalhoPorFazer, len);

        UndirectedSparseGraphTO insumo = graphTemplate.clone();
        Deque<Integer> trabalhoRealizado = new LinkedList<>();
        while (!trabalhoPorFazer.isEmpty()) {
            Integer trabalhoAtual = trabalhoPorFazer.peekFirst();
            List<Integer> opcoesPossiveis = caminhosPossiveis.get(trabalhoAtual);

            while (trabalhoNaoAcabou(trabalhoAtual) && temOpcoesDisponiveis(trabalhoAtual, opcoesPossiveis)) {

            }
            if (trabalhoRealizado(trabalhoAtual) && temFuturo(trabalhoAtual)) {
                trabalhoRealizado.add(trabalhoAtual);
                trabalhoPorFazer.remove(trabalhoAtual);
            } else {
                desfazerUltimoTrabalho(trabalhoRealizado);
            }
        }

        verboseResultadoFinal(trabalhoRealizado, insumo);
    }

    private static void verboseResultadoFinal(Deque<Integer> trabalhoRealizado, UndirectedSparseGraphTO insumo) {
        try {
            System.out.print("Added-Edges: ");
            List<Integer> stackList = (List<Integer>) trabalhoRealizado;
            for (int i = stackList.size() - 1; i >= 0; i--) {
                Pair endpoints = insumo.getEndpoints(stackList.get(i));
                System.out.print(endpoints);
                System.out.print(", ");
            }
        } catch (Exception e) {
        } finally {
            System.out.println();
        }
        System.out.println("Final Graph: ");
        String edgeString = insumo.getEdgeString();
        System.out.println(edgeString);
    }

    private static void verboseInit(UndirectedSparseGraphTO graphTemplate, LinkedList<Integer> incompletVertices, int len) {
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
    }

    private static boolean trabalhoNaoAcabou(Integer trabalhoAtual) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static boolean temOpcoesDisponiveis(Integer trabalhoAtual, List<Integer> opcoesPossiveis) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static boolean trabalhoRealizado(Integer trabalhoAtual) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static boolean temFuturo(Integer trabalhoAtual) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static void desfazerUltimoTrabalho(Deque<Integer> trabalhoRealizado) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

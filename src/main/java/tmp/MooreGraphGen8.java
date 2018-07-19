package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
        TreeMap<Integer, List<Integer>> caminhoPercorrido = new TreeMap<>();
        int len = NUM_ARESTAS - graphTemplate.getEdgeCount();

        BFSDistanceLabeler<Integer, Integer> bfsalg = new BFSDistanceLabeler<>();

        for (Integer v : vertices) {
            int remain = K - graphTemplate.degree(v);
            if (remain > 0) {
                trabalhoPorFazer.add(v);
                caminhosPossiveis.put(v, new ArrayList<>());
            }
        }

        for (Integer v : trabalhoPorFazer) {
            bfsalg.labelDistances(graphTemplate, v);
            for (Integer u : trabalhoPorFazer) {
                if (bfsalg.getDistance(graphTemplate, u) == 4) {
                    caminhosPossiveis.get(v).add(u);
                }
            }
        }

        verboseInit(graphTemplate, trabalhoPorFazer, len);
        UndirectedSparseGraphTO insumo = graphTemplate.clone();
        while (!trabalhoPorFazer.isEmpty()) {
            Integer trabalhoAtual = trabalhoPorFazer.peekFirst();
            List<Integer> opcoesPossiveis = caminhosPossiveis.get(trabalhoAtual);
            Integer janelaCaminhoPercorrido = caminhoPercorrido.size();
            while (trabalhoNaoAcabou(insumo, trabalhoAtual) && temOpcoesDisponiveis(insumo, caminhoPercorrido, opcoesPossiveis, trabalhoAtual)) {
                Integer melhorOpcaoLocal = avaliarMelhorOpcao(caminhoPercorrido, janelaCaminhoPercorrido, opcoesPossiveis, trabalhoAtual);
                Integer aresta = (Integer) insumo.addEdge(trabalhoAtual, melhorOpcaoLocal);
                List<Integer> subcaminho = caminhoPercorrido.getOrDefault(aresta, new ArrayList<>());
                subcaminho.add(melhorOpcaoLocal);
                caminhoPercorrido.putIfAbsent(aresta, subcaminho);
            }
            if (trabalhoRealizado(insumo, trabalhoAtual) && temFuturo(trabalhoAtual)) {
                trabalhoPorFazer.remove(trabalhoAtual);
            } else {
                desfazerUltimoTrabalho(caminhoPercorrido);
            }
        }
        verboseResultadoFinal(caminhoPercorrido, insumo);
    }

    private static void verboseResultadoFinal(TreeMap<Integer, List<Integer>> trabalhoRealizado, UndirectedSparseGraphTO insumo) {
        try {
            System.out.print("Added-Edges: ");
            for (Integer e : trabalhoRealizado.navigableKeySet()) {
                Pair endpoints = insumo.getEndpoints(e);
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

    private static boolean trabalhoNaoAcabou(UndirectedSparseGraphTO insumo, Integer trabalhoAtual) {
        return !trabalhoRealizado(insumo, trabalhoAtual);
    }

    private static boolean temFuturo(Integer trabalhoAtual) {
        return true;
    }

    private static boolean temOpcoesDisponiveis(UndirectedSparseGraphTO insumo, TreeMap<Integer, List<Integer>> caminhoPercorrido, List<Integer> opcoesPossiveis, Integer trabalhoAtual) {
        return (K - insumo.degree(trabalhoAtual)) < opcoesPossiveis.size();
    }

    private static Integer avaliarMelhorOpcao(TreeMap<Integer, List<Integer>> caminhoPercorrido, Integer janelaCaminhoPercorrido, List<Integer> opcoesPossiveis, Integer trabalhoAtual) {
        return opcoesPossiveis.get(caminhoPercorrido.size() - janelaCaminhoPercorrido);
    }

    private static void desfazerUltimoTrabalho(TreeMap<Integer, List<Integer>> caminhoPercorrido) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static boolean trabalhoRealizado(UndirectedSparseGraphTO insumo, Integer trabalhoAtual) {
        return insumo.degree(trabalhoAtual) == K;
    }
}

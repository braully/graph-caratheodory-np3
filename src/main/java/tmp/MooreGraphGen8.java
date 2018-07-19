package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
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
    private static BFSDistanceLabeler<Integer, Integer> bfsalg = new BFSDistanceLabeler<>();

    private static Integer getOpcao(List<Integer> opcoesPossiveis,
            Integer indice, List<Integer> excludentes) {
        Integer opcao = null;
        int restantes = opcoesPossiveis.size() - excludentes.size();
        if (indice < restantes) {
            int cont = indice;
            for (int i = 0; i < cont; i++) {
                if (excludentes.contains(opcoesPossiveis.get(i))) {
                    cont++;
                }
            }
            opcao = opcoesPossiveis.get(cont);
        }
        return opcao;
    }

    static class ComparatorMap implements Comparator<Integer> {

        Map<Integer, Number> map = null;

        public Comparator<Integer> setMap(Map<Integer, Number> map) {
            this.map = map;
            return (Comparator<Integer>) this;
        }

        public int compare(Integer o1, Integer o2) {
            int ret = 0;
            ret = Integer.compare(this.map.get(o2).intValue(), this.map.get(o1).intValue());
            if (ret == 0) {
                ret = Integer.compare(o1, o2);
            }
            return ret;
        }
    }

    static ComparatorMap comparator = new ComparatorMap();

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

        //Marco zero
        caminhoPercorrido.put(insumo.getEdgeCount(), new ArrayList<>());
        Set<Integer> verificarTrabalhoRealizado = new HashSet<>();
        while (!trabalhoPorFazer.isEmpty() && !caminhoPercorrido.isEmpty()) {
            Integer trabalhoAtual = trabalhoPorFazer.peekFirst();
            List<Integer> opcoesPossiveis = caminhosPossiveis.get(trabalhoAtual);
            Integer janelaCaminhoPercorrido = caminhoPercorrido.size();
            while (trabalhoNaoAcabou(insumo, trabalhoAtual)
                    && temOpcoesDisponiveis(insumo, caminhoPercorrido, opcoesPossiveis, trabalhoAtual)) {
                if (!caminhoPercorrido.containsKey(insumo.getEdgeCount())) {
                    caminhoPercorrido.put(insumo.getEdgeCount(), new ArrayList<>());
                }
                Integer melhorOpcaoLocal = avaliarMelhorOpcao(caminhoPercorrido, caminhosPossiveis, janelaCaminhoPercorrido, opcoesPossiveis, insumo, trabalhoAtual);
                if (opcaoViavel(insumo, melhorOpcaoLocal)) {
                    Integer aresta = (Integer) insumo.addEdge(trabalhoAtual, melhorOpcaoLocal);
                    List<Integer> subcaminho = caminhoPercorrido.getOrDefault(aresta, new ArrayList<>());
                    subcaminho.add(melhorOpcaoLocal);
                    caminhoPercorrido.putIfAbsent(aresta, subcaminho);
                    verificarTrabalhoRealizado.add(trabalhoAtual);
                    verificarTrabalhoRealizado.add(melhorOpcaoLocal);
                    System.out.printf("+(%3d,%3d) ", trabalhoAtual, melhorOpcaoLocal);
                    if (trabalhoAcabou(insumo, melhorOpcaoLocal)) {
                        trabalhoPorFazer.remove(melhorOpcaoLocal);
                    }
                } else {
                    desfazerUltimoTrabalho(caminhoPercorrido, trabalhoPorFazer, insumo);
                }
            }
            if (trabalhoAcabou(insumo, trabalhoAtual) && temFuturo(trabalhoAtual)) {
                trabalhoPorFazer.remove(trabalhoAtual);
                System.out.printf(".. %d \n", trabalhoAtual);
            } else {
                System.out.printf("!! %d \n", trabalhoAtual);
                desfazerUltimoTrabalho(caminhoPercorrido, trabalhoPorFazer, insumo);
                break;
            }
        }
        verboseResultadoFinal(caminhoPercorrido, insumo);
    }

    private static boolean opcaoViavel(UndirectedSparseGraphTO insumo, Integer melhorOpcao) {
        int distanciaMelhorOpcao = bfsalg.getDistance(insumo, melhorOpcao);
        if (distanciaMelhorOpcao < 4) {
            return false;
        }
        return true;
    }

    private static Pair<Integer> desfazerUltimoTrabalho(TreeMap<Integer, List<Integer>> caminhoPercorrido, List<Integer> trabalhoPorFazer, UndirectedSparseGraphTO insumo) {
        Integer ultimoPasso = insumo.getEdgeCount() - 1;
        Pair<Integer> desfazer = insumo.getEndpoints(ultimoPasso);
        caminhoPercorrido.get(ultimoPasso).add(desfazer.getSecond());
        insumo.removeEdge(ultimoPasso);
        if (!trabalhoPorFazer.contains(desfazer.getSecond())) {
            trabalhoPorFazer.add(desfazer.getSecond());
        }
        System.out.printf("-(%3d,%3d) ", desfazer.getFirst(), desfazer.getSecond());
        return desfazer;
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

    private static boolean trabalhoAcabou(UndirectedSparseGraphTO insumo, Integer trabalhoAtual) {
        return insumo.degree(trabalhoAtual) == K;
    }

    private static boolean trabalhoNaoAcabou(UndirectedSparseGraphTO insumo, Integer trabalhoAtual) {
        return !trabalhoAcabou(insumo, trabalhoAtual);
    }

    private static boolean temFuturo(Integer trabalhoAtual) {
        return true;
    }

    private static boolean temOpcoesDisponiveis(UndirectedSparseGraphTO insumo, TreeMap<Integer, List<Integer>> caminhoPercorrido, List<Integer> opcoesPossiveis, Integer trabalhoAtual) {
        return (K - insumo.degree(trabalhoAtual)) < opcoesPossiveis.size();
    }

    private static Integer avaliarMelhorOpcao(TreeMap<Integer, List<Integer>> caminhoPercorrido,
            Map<Integer, List<Integer>> caminhosPossiveis,
            Integer janelaCaminhoPercorrido, List<Integer> opcoesPossiveis,
            UndirectedSparseGraphTO insumo, Integer trabalhoAtual) {
        bfsalg.labelDistances(insumo, trabalhoAtual);
        sort(opcoesPossiveis, bfsalg.getDistanceDecorator());
        List<Integer> jaSelecionados = caminhoPercorrido.get(insumo.getEdgeCount());
        Integer indice = jaSelecionados.size();
        Integer melhorOpcao = getOpcao(opcoesPossiveis, indice, jaSelecionados);
        return melhorOpcao;
    }

    private static void sort(List<Integer> opcoesPossiveis, Map<Integer, Number> distanceDecorator) {
        opcoesPossiveis.sort(comparator.setMap(distanceDecorator));
    }
}

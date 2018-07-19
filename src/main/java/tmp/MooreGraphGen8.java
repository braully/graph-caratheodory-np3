package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
//    private static BFSDistanceLabeler<Integer, Integer> bfsalg = new BFSDistanceLabeler<>();
    private static BFSTmp bfsalg = null;

    private static Integer getOpcao(List<Integer> opcoesPossiveis,
            Collection<Integer> excludentes) {
        Integer opcao = null;
        for (int i = 0; i < opcoesPossiveis.size(); i++) {
            Integer val = opcoesPossiveis.get(i);
            if (!excludentes.contains(val)) {
                opcao = val;
                break;
            }
        }
        return opcao;
    }

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

    private static void generateGraph(int K, int numArestas,
            UndirectedSparseGraphTO graphTemplate, List<Integer> startArray) {
        Collection<Integer> vertices = graphTemplate.getVertices();
        LinkedList<Integer> trabalhoPorFazer = new LinkedList<>();
        Map<Integer, List<Integer>> caminhosPossiveis = new HashMap<>();
        TreeMap<Integer, Collection<Integer>> caminhoPercorrido = new TreeMap<>();
        bfsalg = new BFSTmp(vertices.size());
        int len = numArestas - graphTemplate.getEdgeCount();

        System.out.println("Calculando trabalho a fazer");

        for (Integer v : vertices) {
            int remain = K - graphTemplate.degree(v);
            if (remain > 0) {
                trabalhoPorFazer.add(v);
                caminhosPossiveis.put(v, new ArrayList<>());
            }
        }

        System.out.println("Calculando possibilidades de caminho");

//        Integer v = trabalhoPorFazer.getFirst();
//        long currentTimeMillis = System.currentTimeMillis();
//        bfsalg.labelDistances(graphTemplate, v);
//        currentTimeMillis = System.currentTimeMillis() - currentTimeMillis;
//        System.out.println("Tempo função 1: " + currentTimeMillis);
//       
//        currentTimeMillis = System.currentTimeMillis();
//        UtilTmp.bfs(graphTemplate, bfs, v);
//        currentTimeMillis = System.currentTimeMillis() - currentTimeMillis;
//        System.out.println("Tempo função 2: " + currentTimeMillis);
//
//        if (true) {
//            return;
//        }
        for (int i = 0; i < trabalhoPorFazer.size(); i++) {
            Integer v = trabalhoPorFazer.get(i);
            bfsalg.labelDistances(graphTemplate, v);
            for (int j = i; j < trabalhoPorFazer.size(); j++) {
                Integer u = trabalhoPorFazer.get(j);
                if (bfsalg.getDistance(graphTemplate, u) == 4) {
                    caminhosPossiveis.get(v).add(u);
                }
            }
        }
        verboseInit(graphTemplate, trabalhoPorFazer, len);
        System.out.print("Caminhos possiveis: ");
        caminhosPossiveis.entrySet().forEach(e -> System.out.printf("%d|%d|=%s\n", e.getKey(), e.getValue().size(), e.getValue().toString()));
        System.out.println();

        UndirectedSparseGraphTO insumo = graphTemplate.clone();

//        if (true) {
//            return;
//        }
        //Marco zero
        caminhoPercorrido.put(insumo.getEdgeCount(), new ArrayList<>());
        Set<Integer> verificarTrabalhoRealizado = new HashSet<>();
        while (!trabalhoPorFazer.isEmpty() && !caminhoPercorrido.isEmpty()) {
            Integer trabalhoAtual = trabalhoPorFazer.get(0);
            List<Integer> opcoesPossiveis = caminhosPossiveis.get(trabalhoAtual);
            Integer marcoInicial = insumo.getEdgeCount();
            while (trabalhoNaoAcabou(insumo, trabalhoAtual)
                    && temOpcoesDisponiveis(insumo, caminhoPercorrido,
                            opcoesPossiveis, marcoInicial, trabalhoAtual)) {
                if (!caminhoPercorrido.containsKey(insumo.getEdgeCount())) {
                    caminhoPercorrido.put(insumo.getEdgeCount(), new ArrayList<>());
                }
                Integer melhorOpcaoLocal = avaliarMelhorOpcao(caminhoPercorrido, caminhosPossiveis,
                        marcoInicial, opcoesPossiveis, insumo, trabalhoAtual);
//                boolean fakeProblem = trabalhoAtual.equals(13) && insumo.degree(13) == K - 1;
//                if (opcaoViavel(insumo, melhorOpcaoLocal) && !fakeProblem) {
                if (opcaoViavel(insumo, melhorOpcaoLocal)) {
                    Integer aresta = (Integer) insumo.addEdge(trabalhoAtual, melhorOpcaoLocal);
                    Collection<Integer> subcaminho = caminhoPercorrido.getOrDefault(aresta, new ArrayList<>());
                    subcaminho.add(melhorOpcaoLocal);
                    caminhoPercorrido.putIfAbsent(aresta, subcaminho);
                    verificarTrabalhoRealizado.add(trabalhoAtual);
                    verificarTrabalhoRealizado.add(melhorOpcaoLocal);
                    System.out.printf("+[%5d](%3d,%3d) ", aresta, trabalhoAtual, melhorOpcaoLocal);
                    if (trabalhoAcabou(insumo, melhorOpcaoLocal)) {
                        trabalhoPorFazer.remove(melhorOpcaoLocal);
                    }
                } else {
                    desfazerUltimoTrabalho(caminhoPercorrido, trabalhoPorFazer, insumo, trabalhoAtual);
                }
            }
            if (trabalhoAcabou(insumo, trabalhoAtual) && temFuturo(trabalhoAtual)) {
                trabalhoPorFazer.remove(trabalhoAtual);
                System.out.printf(".. %d \n", trabalhoAtual);
            } else {
                System.out.printf("!! %d \n", trabalhoAtual);
            }
            verboseFimEtapa(caminhoPercorrido);
            Collections.sort(trabalhoPorFazer);
        }
        verboseResultadoFinal(caminhoPercorrido, insumo);
    }

    private static void verboseFimEtapa(TreeMap<Integer, Collection<Integer>> caminhoPercorrido) {
        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.print("Caminhos percorrido: ");
        caminhoPercorrido.entrySet().forEach(e -> System.out.printf("%d=%s\n", e.getKey(), e.getValue().toString()));
        System.out.println();
    }

    private static boolean opcaoViavel(UndirectedSparseGraphTO insumo, Integer melhorOpcao) {
        if (melhorOpcao == null) {
            return false;
        }
        int distanciaMelhorOpcao = bfsalg.getDistance(insumo, melhorOpcao);
        if (distanciaMelhorOpcao < 4) {
            return false;
        }
        return true;
    }

    private static Pair<Integer> desfazerUltimoTrabalho(TreeMap<Integer, Collection<Integer>> caminhoPercorrido,
            List<Integer> trabalhoPorFazer, UndirectedSparseGraphTO insumo, Integer trabalhoAtual) {
        caminhoPercorrido.tailMap(insumo.getEdgeCount()).values().forEach(l -> l.clear());//Zerar as opções posteriores
        Integer ultimoPasso = insumo.getEdgeCount() - 1;
        Pair<Integer> desfazer = insumo.getEndpoints(ultimoPasso);
//        caminhoPercorrido.get(ultimoPasso).add(desfazer.getSecond());
        insumo.removeEdge(ultimoPasso);
        if (!trabalhoPorFazer.contains(desfazer.getSecond())) {
            trabalhoPorFazer.add(desfazer.getSecond());
        }
        if (!trabalhoAtual.equals(desfazer.getFirst())
                && !trabalhoPorFazer.contains(desfazer.getFirst())) {
            trabalhoPorFazer.add(desfazer.getFirst());
        }
        //Zerar as opções posteriores
        System.out.printf("-[%5d](%3d,%3d) ", ultimoPasso, desfazer.getFirst(), desfazer.getSecond());
        return desfazer;
    }

    private static void verboseResultadoFinal(TreeMap<Integer, Collection<Integer>> trabalhoRealizado,
            UndirectedSparseGraphTO insumo) {
        System.out.println();
        if (insumo.getEdgeCount() < NUM_ARESTAS) {
            System.out.println("Busca pelo grafo Falhou ***");
        } else {
            System.out.println("Grafo Encontrado");
        }

        try {
            System.out.print("Added-Edges: ");
            for (Integer e : trabalhoRealizado.navigableKeySet()) {
                Pair endpoints = insumo.getEndpoints(e);
                if (endpoints != null) {
                    System.out.print(endpoints);
                    System.out.print(", ");
                }
            }
        } catch (Exception e) {
        } finally {
            System.out.println();
        }
        System.out.println("Final Graph: ");
        String edgeString = insumo.getEdgeString();
        System.out.println(edgeString);
    }

    private static void verboseInit(UndirectedSparseGraphTO graphTemplate,
            LinkedList<Integer> incompletVertices, int len) {
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

    private static boolean temOpcoesDisponiveis(UndirectedSparseGraphTO insumo,
            TreeMap<Integer, Collection<Integer>> caminhoPercorrido,
            List<Integer> opcoesPossiveis, Integer marcoInicial,
            Integer trabalhoAtual) {
        boolean condicao0 = insumo.getEdgeCount() >= marcoInicial;
        boolean condicao1 = (K - insumo.degree(trabalhoAtual)) < opcoesPossiveis.size();
        return condicao0 && condicao1;
    }

    private static Integer avaliarMelhorOpcao(TreeMap<Integer, Collection<Integer>> caminhoPercorrido,
            Map<Integer, List<Integer>> caminhosPossiveis,
            Integer janelaCaminhoPercorrido, List<Integer> opcoesPossiveis,
            UndirectedSparseGraphTO insumo, Integer trabalhoAtual) {
        bfsalg.labelDistances(insumo, trabalhoAtual);
//        sort(opcoesPossiveis, bfsalg.getDistanceDecorator());
        sort(opcoesPossiveis, bfsalg.bfs);
        Collection<Integer> jaSelecionados = caminhoPercorrido.get(insumo.getEdgeCount());
        Integer indice = jaSelecionados.size();
        Integer melhorOpcao = getOpcao(opcoesPossiveis, jaSelecionados);
        return melhorOpcao;
    }

    private static void sort(List<Integer> opcoesPossiveis, Integer[] bfs) {
        opcoesPossiveis.sort(comparator.setBfs(bfs));
    }

    private static void sort(List<Integer> opcoesPossiveis, Map<Integer, Number> distanceDecorator) {
        opcoesPossiveis.sort(comparator.setMap(distanceDecorator));
    }

    static class ComparatorMap implements Comparator<Integer> {

        Map<Integer, Number> map = null;
        Integer[] bfs = null;

        public Comparator<Integer> setMap(Map<Integer, Number> map) {
            this.map = map;
            this.bfs = null;
            return (Comparator<Integer>) this;
        }

        public Comparator<Integer> setBfs(Integer[] bfs) {
            this.map = null;
            this.bfs = bfs;
            return (Comparator<Integer>) this;
        }

        public int compare(Integer o1, Integer o2) {
            int ret = 0;
            if (map != null) {
                ret = Integer.compare(this.map.get(o2).intValue(), this.map.get(o1).intValue());
            }
            if (bfs != null) {
                ret = Integer.compare(bfs[o2], bfs[o1]);
            }
            if (ret == 0) {
                ret = Integer.compare(o1, o2);
            }
            return ret;
        }
    }

    static ComparatorMap comparator = new ComparatorMap();

}

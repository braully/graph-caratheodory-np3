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
    private static final boolean veboseFimEtapa = false;
    private static final boolean rankearOpcoes = false;
//    private static final boolean rankearOpcoes = true;
    private static final boolean anteciparVazio = true;
    private static final boolean falhaPrimeiroRollBack = true;
//    private static final boolean falhaPrimeiroRollBack = false;

//    private static int K = 57;
    private static int K = 7;
    private static int NUM_ARESTAS = ((K * K + 1) * K) / 2;
//    private static BFSDistanceLabeler<Integer, Integer> bfsalg = new BFSDistanceLabeler<>();
    private static BFSTmp bfsalg = null;
    private static BFSTmp bfsRanking = null;
    private static Integer[] ranking = null;

    public static void main(String... args) {
        LinkedList<Integer> startArray = new LinkedList<>();

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
            UndirectedSparseGraphTO graphTemplate,
            LinkedList<Integer> loadInital) {
        Collection<Integer> vertices = graphTemplate.getVertices();
        LinkedList<Integer> trabalhoPorFazer = null;
        Map<Integer, List<Integer>> caminhosPossiveis = null;
        TreeMap<Integer, Collection<Integer>> caminhoPercorrido = new TreeMap<>();
        long lastime = System.currentTimeMillis();
        int numArestasIniciais = graphTemplate.getEdgeCount();
        int numVertices = vertices.size();
        int len = numArestas - numArestasIniciais;

        bfsalg = new BFSTmp(numVertices);
        bfsRanking = new BFSTmp(numVertices);
        ranking = new Integer[numVertices];

        if (K > 7) {
            trabalhoPorFazer = (LinkedList<Integer>) UtilTmp.loadFromCache("trabalho-por-fazer-partial.dat");
            caminhosPossiveis = (Map<Integer, List<Integer>>) UtilTmp.loadFromCache("caminhos-possiveis.dat");
        }

        if (trabalhoPorFazer == null || caminhosPossiveis == null || trabalhoPorFazer.isEmpty() || caminhosPossiveis.isEmpty()) {
            System.out.println("Building");
            trabalhoPorFazer = new LinkedList<>();
            caminhosPossiveis = new HashMap<>();
            initialLoad(vertices, graphTemplate, trabalhoPorFazer, caminhosPossiveis);
        } else {
            System.out.println("Loaded");
        }
        verboseInit(graphTemplate, trabalhoPorFazer, caminhosPossiveis, len);
        UndirectedSparseGraphTO insumo = graphTemplate.clone();

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
                if (!loadInital.isEmpty()) {
                    melhorOpcaoLocal = loadInital.pollFirst();
                }
                //boolean fakeProblem = trabalhoAtual.equals(13) && insumo.degree(13) == K - 1;
                //if (opcaoViavel(insumo, melhorOpcaoLocal) && !fakeProblem) {
                if (opcaoViavel(insumo, trabalhoAtual, melhorOpcaoLocal)) {
                    Integer aresta = (Integer) insumo.addEdge(trabalhoAtual, melhorOpcaoLocal);
                    Collection<Integer> subcaminho = caminhoPercorrido.getOrDefault(aresta, new ArrayList<>());
                    subcaminho.add(melhorOpcaoLocal);
                    caminhoPercorrido.putIfAbsent(aresta, subcaminho);
                    verificarTrabalhoRealizado.add(trabalhoAtual);
                    verificarTrabalhoRealizado.add(melhorOpcaoLocal);
                    if (verbose) {
                        System.out.printf("+[%5d](%4d,%4d) ", aresta, trabalhoAtual, melhorOpcaoLocal);
                    }
                    if (System.currentTimeMillis() - lastime > UtilTmp.ALERT_HOUR) {
                        lastime = System.currentTimeMillis();
                        printVertAddArray(insumo, numArestasIniciais);
                    }

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
            if (veboseFimEtapa) {
                verboseFimEtapa(caminhoPercorrido);
            }
            Collections.sort(trabalhoPorFazer);
        }
        verboseResultadoFinal(caminhoPercorrido, insumo);
    }

    public static void initialLoad(Collection<Integer> vertices,
            UndirectedSparseGraphTO graphTemplate,
            LinkedList<Integer> trabalhoPorFazer,
            Map<Integer, List<Integer>> caminhosPossiveis) {
        System.out.println("Calculando trabalho a fazer");

//        Arrays.stream(LGMGen.cacheTrabalhoFazer).forEachOrdered(t -> trabalhoPorFazer.add(t));
        for (Integer v : vertices) {
            int remain = K - graphTemplate.degree(v);
            if (remain > 0) {
                trabalhoPorFazer.add(v);
                caminhosPossiveis.put(v, new ArrayList<>());
            }
        }

        System.out.println("Calculando possibilidades de caminho");
        for (int i = 0; i < trabalhoPorFazer.size(); i++) {
            Integer v = trabalhoPorFazer.get(i);
            bfsalg.labelDistances(graphTemplate, v);
            caminhosPossiveis.put(v, new ArrayList<>());
            for (int j = i; j < trabalhoPorFazer.size(); j++) {
                Integer u = trabalhoPorFazer.get(j);
                if (bfsalg.getDistance(graphTemplate, u) == 4) {
                    caminhosPossiveis.get(v).add(u);
                }
            }
        }
        if (K > 7) {
            UtilTmp.saveToCache(trabalhoPorFazer, "/home/braully/trabalho-por-fazer-partial.dat");
            UtilTmp.saveToCache(caminhosPossiveis, "/home/braully/caminhos-possiveis.dat");
        }
    }

    private static boolean opcaoViavel(UndirectedSparseGraphTO insumo, Integer trabalhoAtual,
            Integer melhorOpcao) {
        if (melhorOpcao == null) {
            return false;
        }
        int distanciaMelhorOpcao = bfsalg.getDistance(insumo, melhorOpcao);
        if (distanciaMelhorOpcao < 4) {
            return false;
        }

        if (anteciparVazio && bfsalg.getDistance(insumo, trabalhoAtual) == 0) {
            boolean condicao1 = true;
            int dv = (K - insumo.degree(trabalhoAtual));
            condicao1 = dv <= bfsalg.depthcount[4];
            if (!condicao1 && verbose) {
                System.err.printf("Falha condicao 1 v=%d rdv=%d 4count=%d \n", trabalhoAtual, dv, bfsalg.depthcount[4]);
            }
            if (!condicao1) {
                return false;
            }
        }
        return true;
    }

    private static Pair<Integer> desfazerUltimoTrabalho(TreeMap<Integer, Collection<Integer>> caminhoPercorrido,
            List<Integer> trabalhoPorFazer,
            UndirectedSparseGraphTO insumo,
            Integer trabalhoAtual) {

        if (falhaPrimeiroRollBack) {
            throw new IllegalStateException("Interrução forçada");
        }

        caminhoPercorrido.tailMap(insumo.getEdgeCount()).values().forEach(l -> l.clear());//Zerar as opções posteriores
        Integer ultimoPasso = insumo.getEdgeCount() - 1;
        Pair<Integer> desfazer = insumo.getEndpoints(ultimoPasso);
        //caminhoPercorrido.get(ultimoPasso).add(desfazer.getSecond());
        insumo.removeEdge(ultimoPasso);
        if (!trabalhoPorFazer.contains(desfazer.getSecond())) {
            trabalhoPorFazer.add(desfazer.getSecond());
        }
        if (!trabalhoAtual.equals(desfazer.getFirst())
                && !trabalhoPorFazer.contains(desfazer.getFirst())) {
            trabalhoPorFazer.add(desfazer.getFirst());
        }
        //Zerar as opções posteriores
        if (verbose) {
            System.out.printf("-[%5d](%4d,%4d) ", ultimoPasso, desfazer.getFirst(), desfazer.getSecond());
        }
        return desfazer;
    }

    private static boolean trabalhoAcabou(UndirectedSparseGraphTO insumo,
            Integer trabalhoAtual) {
        return insumo.degree(trabalhoAtual) == K;
    }

    private static boolean trabalhoNaoAcabou(UndirectedSparseGraphTO insumo,
            Integer trabalhoAtual) {
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

//        return condicao0 && condicao1;
        return condicao0;
    }

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

    private static Integer avaliarMelhorOpcao(TreeMap<Integer, Collection<Integer>> caminhoPercorrido,
            Map<Integer, List<Integer>> caminhosPossiveis,
            Integer janelaCaminhoPercorrido, List<Integer> opcoesPossiveis,
            UndirectedSparseGraphTO insumo, Integer trabalhoAtual) {
        bfsalg.labelDistances(insumo, trabalhoAtual);
//        sort(opcoesPossiveis, bfsalg.getDistanceDecorator());
        sortAndRanking(caminhoPercorrido,
                opcoesPossiveis,
                trabalhoAtual,
                insumo, bfsalg.bfs);
        Collection<Integer> jaSelecionados = caminhoPercorrido.get(insumo.getEdgeCount());
        Integer indice = jaSelecionados.size();
        Integer melhorOpcao = getOpcao(opcoesPossiveis, jaSelecionados);
        return melhorOpcao;
    }

    private static void sortAndRanking(TreeMap<Integer, Collection<Integer>> caminhoPercorrido,
            List<Integer> opcoesPossiveis, Integer trabalhoAtual,
            UndirectedSparseGraphTO insumo,
            Integer[] bfs) {
        opcoesPossiveis.sort(comparatorProfundidade.setBfs(bfs));
        if (rankearOpcoes) {
            int i = 0;
            for (i = 0; i < ranking.length; i++) {
                ranking[i] = 0;
            }
            for (i = 0; i < opcoesPossiveis.size(); i++) {
                Integer val = opcoesPossiveis.get(i);
                bfsRanking.bfsRanking(insumo, val);
                if (bfs[val] == 4) {
                    ranking[val] = bfsRanking.depthcount[4];
//                    ranking[val] = bfsRanking.depthcount[4] + bfsRanking.depthcount[3];
//                    ranking[val] = bfsRanking.depthcount[4] * 1000 + bfsRanking.depthcount[3];
//                    ranking[val] = bfsRanking.depthcount[3];
//                    ranking[val] = bfsRanking.depthcount[4] * 3000 + bfsRanking.depthcount[3] * 100 + bfsRanking.depthcount[3];
                } else {
                    break;
                }
//                if (trabalhoAtual.equals(18) && (val.equals(22) || val.equals(23))) {
//                if (trabalhoAtual.equals(14)) {
//                    System.out.printf("Ranking (%4d,%4d): ", val, trabalhoAtual);
//                    UtilTmp.printArray(bfsRanking.depthcount);
//                    System.out.println("");
//                }
            }
            opcoesPossiveis.subList(0, i).sort(comparatorProfundidade.setBfs(ranking));
        }
    }

    private static void sort(List<Integer> opcoesPossiveis, Map<Integer, Number> distanceDecorator) {
        opcoesPossiveis.sort(comparatorProfundidade.setMap(distanceDecorator));
    }

    static class ComparatorMap implements Comparator<Integer> {

        Map<Integer, Number> mapRanking = null;
        Integer[] ranking = null;

        public Comparator<Integer> setMap(Map<Integer, Number> map) {
            this.mapRanking = map;
            this.ranking = null;
            return (Comparator<Integer>) this;
        }

        public Comparator<Integer> setBfs(Integer[] bfs) {
            this.mapRanking = null;
            this.ranking = bfs;
            return (Comparator<Integer>) this;
        }

        @Override
        public int compare(Integer o1, Integer o2) {
            int ret = 0;
            if (mapRanking != null) {
                ret = Integer.compare(this.mapRanking.get(o2).intValue(), this.mapRanking.get(o1).intValue());
            }
            if (ranking != null) {
                ret = Integer.compare(ranking[o2], ranking[o1]);
            }
            if (ret == 0) {
                ret = Integer.compare(o1, o2);
            }
            return ret;
        }
    }

    static ComparatorMap comparatorProfundidade = new ComparatorMap();

    /* Verboses */
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

    private static void verboseFimEtapa(TreeMap<Integer, Collection<Integer>> caminhoPercorrido) {
        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.print("Caminhos percorrido: ");
        caminhoPercorrido.entrySet().forEach(e -> System.out.printf("%d=%s\n", e.getKey(), e.getValue().toString()));
        System.out.println();
    }

    private static void verboseInit(UndirectedSparseGraphTO graphTemplate,
            LinkedList<Integer> incompletVertices,
            Map<Integer, List<Integer>> caminhosPossiveis, int len) {
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

        if (verbose && false) {
            System.out.print("Caminhos possiveis: ");
//        caminhosPossiveis.entrySet().forEach(e -> System.out.printf("%d|%d|=%s\n", e.getKey(), e.getValue().size(), e.getValue().toString()));
            caminhosPossiveis.entrySet().forEach(e -> System.out.printf("{%d, %s},\n", e.getKey(), e.getValue().toString()));
        }
        System.out.println();
    }

    private static void printVertAddArray(UndirectedSparseGraphTO lastgraph, int numArestasIniciais) {
        System.out.print("vert-add: ");
        for (int i = numArestasIniciais; i < lastgraph.getEdgeCount(); i++) {
            System.out.printf("%d, ", lastgraph.getEndpoints(i).getFirst());
        }
        System.out.println(" | ");
        for (int i = numArestasIniciais; i < lastgraph.getEdgeCount(); i++) {
            System.out.printf("%d, ", lastgraph.getEndpoints(i).getSecond());
        }
        System.out.println();
    }

}

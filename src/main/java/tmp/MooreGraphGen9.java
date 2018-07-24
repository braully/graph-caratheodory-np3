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
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author strike
 */
public class MooreGraphGen9 {

    private static final boolean verbose = true;
//    private static final boolean verbose = false;
//    private static final boolean vebosePossibilidadesIniciais = true;
    private static final boolean vebosePossibilidadesIniciais = false;
//    private static final boolean veboseFimEtapa = false;
    private static final boolean veboseFimEtapa = true;
//    private static final boolean rankearOpcoes = false;
    private static final boolean verboseRankingOption = false;
    private static final boolean rankearOpcoes = true;
    private static final int rankearOpcoesProfundidade = 1;
    private static final boolean anteciparVazio = true;
//    private static final boolean falhaPrimeiroRollBack = true;
    private static final boolean falhaPrimeiroRollBack = false;
    private static final boolean falhaInCommitCount = true;
//    private static final boolean falhaInCommitCount = false;
    private static int falhaCommitCount = 0;
    private static final boolean descartarOpcoesNaoOptimais = true;
    private static final boolean ordenarTrabalhoPorFazerPorPrimeiraOpcao = true;
//    private static final boolean ordenarTrabalhoPorFazerPorPrimeiraOpcao = false;
    private static final boolean dumpResultadoPeriodicamente = true;
//    private static final boolean dumpResultadoPeriodicamente = false;
//
    private static final String estrategiaString = (rankearOpcoes ? "rt0t" : "rt0f") + rankearOpcoesProfundidade + "-" + (ordenarTrabalhoPorFazerPorPrimeiraOpcao ? "opft" : "otpff") + "-" + (descartarOpcoesNaoOptimais ? "dnot" : "dnof") + "-" + (anteciparVazio ? "avt" : "avf");

    private static int K = 57;
//    private static int K = 7;
    private static int NUM_ARESTAS = ((K * K + 1) * K) / 2;
//    private static BFSDistanceLabeler<Integer, Integer> bfsalg = new BFSDistanceLabeler<>();
    private static long lastime = System.currentTimeMillis();
    private static long lastime2 = System.currentTimeMillis();
    private static long[] rbcount = new long[4];

    public static void main(String... args) {
        if (K == 7) {
            NUM_ARESTAS = ((K * K + 1) * K) / 2;
            UndirectedSparseGraphTO graphTemplate = HoffmanGraphGen.subgraph;
            generateGraph(K, NUM_ARESTAS, graphTemplate, args);
        }
        if (K == 57) {
            NUM_ARESTAS = ((K * K + 1) * K) / 2;
            UndirectedSparseGraphTO graphTemplate = LGMGen.subgraph;
            generateGraph(K, NUM_ARESTAS, graphTemplate, args);
        }

    }

    private static class Processamento {

        UndirectedSparseGraphTO insumo;
        Collection<Integer> vertices;
        LinkedList<Integer> trabalhoPorFazer;
        Map<Integer, List<Integer>> caminhosPossiveis;
        Map<Integer, List<Integer>> caminhosPossiveisOriginal;
        TreeMap<Integer, Collection<Integer>> caminhoPercorrido = new TreeMap<>();
        Map<Integer, Map<Integer, List<Integer>>> historicoRanking = new TreeMap<>();
        int numArestasIniciais;
        int numVertices;
        int len;
        Integer trabalhoAtual;
        List<Integer> opcoesPossiveis;
        int marcoInicial;

        BFSTmp bfsalg = null;
        BFSTmp bfsRanking = null;
        Integer[] ranking = null;
        long longestresult = 11948;
        private Integer melhorOpcaoLocal;

    }

    private static void generateGraph(int K, int numArestas,
            UndirectedSparseGraphTO graphTemplate, String... args) {

        Processamento processamento = new Processamento();
        processamento.vertices = graphTemplate.getVertices();
        processamento.numArestasIniciais = graphTemplate.getEdgeCount();
        processamento.numVertices = processamento.vertices.size();
        processamento.len = numArestas - processamento.numArestasIniciais;

        processamento.bfsalg = new BFSTmp(processamento.numVertices);
        processamento.bfsRanking = new BFSTmp(processamento.numVertices);
        processamento.ranking = new Integer[processamento.numVertices];

        if (K > 7) {
            processamento.trabalhoPorFazer = (LinkedList<Integer>) UtilTmp.loadFromCache("trabalho-por-fazer-partial.dat");
            processamento.caminhosPossiveis = (Map<Integer, List<Integer>>) UtilTmp.loadFromCache("caminhos-possiveis.dat");
        }

        if (processamento.trabalhoPorFazer == null || processamento.caminhosPossiveis == null || processamento.trabalhoPorFazer.isEmpty() || processamento.caminhosPossiveis.isEmpty()) {
            System.out.println("Building");
            processamento.trabalhoPorFazer = new LinkedList<>();
            processamento.caminhosPossiveis = new HashMap<>();
            initialLoad(graphTemplate, processamento);
        } else {
            System.out.println("Loaded");
        }
        verboseInit(graphTemplate, processamento);
        processamento.caminhosPossiveisOriginal = UtilTmp.cloneMap(processamento.caminhosPossiveis);

        processamento.insumo = graphTemplate.clone();
        ComparatorTrabalhoPorFazer comparatorTrabalhoPorFazer = new ComparatorTrabalhoPorFazer(processamento.caminhosPossiveisOriginal);

        //Marco zero
        processamento.caminhoPercorrido.put(processamento.insumo.getEdgeCount(), new ArrayList<>());
        Set<Integer> verificarTrabalhoRealizado = new HashSet<>();

        if (args != null && args.length > 0) {
            if (args.length == 1) {
                args = args[0].split(" ");
            }
            System.out.print("Load-Status from Args");
            String strpattern = "\\{(\\d+)\\}\\((\\d+),(\\d+)\\)\\[([0-9,]+)\\]";
            Pattern pattern = Pattern.compile(strpattern);
            for (String str : args) {
                Matcher matcher = pattern.matcher(str);
                System.out.print(str);
                System.out.print("->");
                if (matcher.matches()) {
                    Integer numEdge = Integer.parseInt(matcher.group(2));
                    Integer e1 = Integer.parseInt(matcher.group(2));
                    Integer e2 = Integer.parseInt(matcher.group(3));
                    //System.out.println(matcher.group(3));
                    List<Integer> caminho = UtilTmp.strToList(matcher.group(4));
                    Integer aresta = (Integer) processamento.insumo.addEdge(e1, e2);
                    if (numEdge.equals(aresta)) {
                        throw new IllegalStateException("Incorrect load info");
                    }
                    processamento.caminhoPercorrido.put(aresta, caminho);
                    System.out.printf("e1=%d,e2=%d,e=%d:", e1, e2, aresta);
                    System.out.print(caminho);
                    System.out.print("  ");
//                    System.out.println(matcher.group(4));
                }
            }
        }

        List<Integer> trabalhPorFazerOriginal = new ArrayList<>();

        if (ordenarTrabalhoPorFazerPorPrimeiraOpcao) {
            Collections.sort(processamento.trabalhoPorFazer, comparatorTrabalhoPorFazer);
        } else {
            Collections.sort(processamento.trabalhoPorFazer);
        }
        trabalhPorFazerOriginal.addAll(processamento.trabalhoPorFazer);

        if (vebosePossibilidadesIniciais) {
            System.out.print("Caminhos possiveis: \n");
            List<Integer> ant = processamento.caminhosPossiveis.get(processamento.trabalhoPorFazer.get(0));
            for (Integer e : processamento.trabalhoPorFazer) {
                List<Integer> at = processamento.caminhosPossiveis.get(e);
                if (!at.equals(ant)) {
                    System.out.println("----------------------------------------------------------------------------------------------");
                }
                System.out.printf("%d|%d|=%s\n", e, at.size(), at.toString());
                ant = at;
            }
//            caminhosPossiveis.entrySet().forEach(e -> System.out.printf("{%d, %s},\n", e.getKey(), e.getValue().toString()));
        }
        System.out.println();

        while (!processamento.trabalhoPorFazer.isEmpty() && !processamento.caminhoPercorrido.isEmpty()) {
            processamento.trabalhoAtual = processamento.trabalhoPorFazer.get(0);
            processamento.opcoesPossiveis = processamento.caminhosPossiveis.get(processamento.trabalhoAtual);
            processamento.marcoInicial = processamento.insumo.getEdgeCount();

            verboseInicioEtapa(processamento);
//            printMapOpcoes(trabalhPorFazerOriginal, insumo, caminhosPossiveis);

            while (trabalhoNaoAcabou(processamento)
                    && temOpcoesDisponiveis(processamento)) {

//                if (trabalhoAtual.equals(216)) {
//                if (trabalhoAtual.equals(221)) {
//                    desfazerUltimoTrabalho(caminhoPercorrido, trabalhoPorFazer, insumo, trabalhoAtual);
//                    System.out.println("Buscando proxima combinação");
//                    continue;
//                }
                if (!processamento.caminhoPercorrido.containsKey(processamento.insumo.getEdgeCount())) {
                    processamento.caminhoPercorrido.put(processamento.insumo.getEdgeCount(), new ArrayList<>());
                }
                processamento.melhorOpcaoLocal = avaliarMelhorOpcao(processamento);

                //boolean fakeProblem = trabalhoAtual.equals(13) && insumo.degree(13) == K - 1;
                //if (opcaoViavel(insumo, melhorOpcaoLocal) && !fakeProblem) {
                if (opcaoViavel(processamento)) {
                    Integer aresta = (Integer) processamento.insumo.addEdge(processamento.trabalhoAtual, processamento.melhorOpcaoLocal);
                    Collection<Integer> subcaminho = processamento.caminhoPercorrido.getOrDefault(aresta, new ArrayList<>());
                    subcaminho.add(processamento.melhorOpcaoLocal);
                    processamento.caminhoPercorrido.putIfAbsent(aresta, subcaminho);
                    verificarTrabalhoRealizado.add(processamento.trabalhoAtual);
                    verificarTrabalhoRealizado.add(processamento.melhorOpcaoLocal);
                    if (trabalhoAcabou(processamento.insumo, processamento.melhorOpcaoLocal)) {
                        processamento.trabalhoPorFazer.remove(processamento.melhorOpcaoLocal);
                    }
                    observadorDeEtapa(aresta, processamento.melhorOpcaoLocal, processamento);
                } else {
                    desfazerUltimoTrabalho(processamento.caminhoPercorrido, processamento.trabalhoPorFazer, processamento.insumo, processamento.trabalhoAtual);
                }
            }

            if (trabalhoAcabou(processamento.insumo, processamento.trabalhoAtual) && temFuturo(processamento.trabalhoAtual)) {
                processamento.trabalhoPorFazer.remove(processamento.trabalhoAtual);
//                UtilTmp.dumpVertAddArray(insumo,
//                        numArestasIniciais,
//                        caminhoPercorrido, ".comb", false);

                System.out.printf(".. %d [%d] \n", processamento.trabalhoAtual, processamento.insumo.getEdgeCount());
            } else {
                System.out.printf("!! %d \n", processamento.trabalhoAtual);
            }
            System.out.printf("rbcount[%d,%d,%d,%d]=%d ", rbcount[0], rbcount[1], rbcount[2], rbcount[3], (rbcount[0] + rbcount[1] + rbcount[2] + rbcount[3]));
            System.out.println(estrategiaString);
            UtilTmp.printCurrentItme();
            verboseFimEtapa(processamento.caminhoPercorrido, processamento.insumo, processamento.trabalhoAtual, processamento.opcoesPossiveis);

            if (ordenarTrabalhoPorFazerPorPrimeiraOpcao) {
                Collections.sort(processamento.trabalhoPorFazer, comparatorTrabalhoPorFazer);
            } else {
                Collections.sort(processamento.trabalhoPorFazer);
            }
        }
//        printMapOpcoes(trabalhPorFazerOriginal, insumo, caminhosPossiveis);
        verboseResultadoFinal(processamento.caminhoPercorrido, processamento.insumo);
    }

    private static void printMapOpcoes(Processamento processamento,
            List<Integer> trabalhPorFazerOriginal,
            UndirectedSparseGraphTO insumo,
            Map<Integer, List<Integer>> caminhosPossiveis) {
        int[] count = new int[]{0, 0, 0, 0, 0};

//        TreeSet<Integer> trabalhPorFazerOriginalOrdenado = new TreeSet<>(trabalhPorFazerOriginal);
        int linha = 0;
        for (Integer v : trabalhPorFazerOriginal) {
//        for (Integer v : trabalhPorFazerOriginalOrdenado) {
            processamento.bfsalg.labelDistances(insumo, v);
            System.out.printf("[%4d]: [", v);
            TreeSet<Integer> opcoesPossiveisOrdenada = new TreeSet<>(caminhosPossiveis.get(v));
            int coluna = 0;
            for (Integer o : trabalhPorFazerOriginal) {
                if (coluna >= linha) {
//                if (opcoesPossiveisOrdenada.contains(o)) {
                    int distancia = processamento.bfsalg.getDistance(insumo, o);
                    if (distancia == 1) {
                        System.out.print('x');
                    } else if (distancia == 4) {
                        System.out.print('4');
                    } else if (distancia == 2) {
                        System.out.print(' ');
                    } else if (distancia == 3) {
                        System.out.print('#');
                    } else {
                        System.out.print('\\');
                    }
                    count[distancia]++;
//                } else {
//                    System.out.print(' ');
//                }
                } else {
                    System.out.print(' ');
                }
                coluna++;
            }
            linha++;
            System.out.print("]\n");
        }
        System.out.printf("Total count: d4=%d d3=%d d2=%d \n", count[4], count[3], count[2], count[1]);
    }

    private static void verboseFimEtapa(TreeMap<Integer, Collection<Integer>> caminhoPercorrido,
            UndirectedSparseGraphTO insumo, Integer trabalhoAtual,
            List<Integer> opcoesPossiveis) throws IllegalStateException {
        if (veboseFimEtapa) {
            verboseFimEtapa(caminhoPercorrido);
        }
        if (falhaInCommitCount) {
//            verboseFimEtapa(caminhoPercorrido);
//            bfsalg.labelDistances(insumo, trabalhoAtual);
//            System.out.printf("\nbfs-map-opf[%4d]: [", trabalhoAtual);
//            TreeSet<Integer> tmp = new TreeSet<>(opcoesPossiveis);
//            for (Integer o : tmp) {
//                int distance = bfsalg.getDistance(insumo, o);
//                if (distance == 1) {
//                    System.out.print('x');
//                } else if (distance == 4) {
//                    System.out.print('4');
//                } else {
//                    System.out.print(' ');
//                }
//            }
//            System.out.print("]\n");
            if (falhaCommitCount-- <= 0) {
                throw new IllegalStateException("Interrução forçada -- commit");
            }
        }
    }

    private static void verboseInicioEtapa(Processamento processamento) {
//        if (falhaInCommitCount) {
//            bfsalg.labelDistances(insumo, trabalhoAtual);
//            System.out.printf("\nbfs-map-ini[%4d]: [", trabalhoAtual);
//            for (Integer o : opcoesPossiveis) {
//                if (bfsalg.getDistance(insumo, o) == 1) {
//                    System.out.print('x');
//                } else if (bfsalg.getDistance(insumo, o) == 4) {
//                    System.out.print('4');
//                } else {
//                    System.out.print(' ');
//                }
//            }
//            System.out.print("]\n");
//        }
    }

    private static void observadorDeEtapa(Integer aresta,
            Integer melhorOpcaoLocal, Processamento processamento) {
        if (verbose) {
            System.out.printf("+[%5d](%4d,%4d) ", aresta, processamento.trabalhoAtual, melhorOpcaoLocal);
        }
        if (dumpResultadoPeriodicamente && System.currentTimeMillis() - lastime > UtilTmp.ALERT_HOUR) {
            System.out.println("Alert hour ");
            UtilTmp.dumpString(estrategiaString);
            UtilTmp.dumpString(String.format("rbcount[%d,%d,%d,%d]=%d", rbcount[0], rbcount[1], rbcount[2], rbcount[3], (rbcount[0] + rbcount[1] + rbcount[2] + rbcount[3])));
            rbcount[0] = rbcount[1] = rbcount[2] = rbcount[3] = 0;
            lastime = System.currentTimeMillis();
            //                        printVertAddArray(insumo, numArestasIniciais);

            String lastAdd = String.format("last+[%5d](%4d,%4d) \n", aresta, processamento.trabalhoAtual, melhorOpcaoLocal);
            UtilTmp.dumpString(lastAdd);
            UtilTmp.printCurrentItme();

            if (processamento.longestresult < processamento.insumo.getEdgeCount() || System.currentTimeMillis() - lastime2 > UtilTmp.ALERT_HOUR_12) {
                lastime2 = System.currentTimeMillis();
                if (processamento.longestresult < processamento.insumo.getEdgeCount()) {
                    System.out.print("new longest  result: ");
                    processamento.longestresult = processamento.insumo.getEdgeCount();
                    System.out.println(processamento.longestresult);
                }
                UtilTmp.dumpVertAddArray(processamento.insumo,
                        processamento.numArestasIniciais,
                        processamento.caminhoPercorrido);
                if (K > 7) {
                    UtilTmp.dumpOverrideString(processamento.insumo.getEdgeString(), ".graph.g9." + estrategiaString);
                }
            }
        }
    }

    public static void initialLoad(UndirectedSparseGraphTO graphTemplate, Processamento processamento) {
        System.out.println("Calculando trabalho a fazer");

//        Arrays.stream(LGMGen.cacheTrabalhoFazer).forEachOrdered(t -> trabalhoPorFazer.add(t));
        for (Integer v : processamento.vertices) {
            int remain = K - graphTemplate.degree(v);
            if (remain > 0) {
                processamento.trabalhoPorFazer.add(v);
                processamento.caminhosPossiveis.put(v, new ArrayList<>());
            }
        }

        System.out.println("Calculando possibilidades de caminho");
        for (int i = 0; i < processamento.trabalhoPorFazer.size(); i++) {
            Integer v = processamento.trabalhoPorFazer.get(i);
            processamento.bfsalg.labelDistances(graphTemplate, v);
            processamento.caminhosPossiveis.put(v, new ArrayList<>());
            for (int j = i; j < processamento.trabalhoPorFazer.size(); j++) {
                Integer u = processamento.trabalhoPorFazer.get(j);
                if (processamento.bfsalg.getDistance(graphTemplate, u) == 4) {
                    processamento.caminhosPossiveis.get(v).add(u);
                }
            }
        }
        if (K > 7) {
            UtilTmp.saveToCache(processamento.trabalhoPorFazer, "/home/braully/trabalho-por-fazer-partial.dat");
            UtilTmp.saveToCache(processamento.caminhosPossiveis, "/home/braully/caminhos-possiveis.dat");
        }
    }

    private static boolean opcaoViavel(Processamento processamento) {
        Integer melhorOpcao = processamento.melhorOpcaoLocal;
        if (melhorOpcao == null) {
            rbcount[0]++;
            return false;
        }

//        if (trabalhoAtual.equals(113)) {
//            return false;
//        }
        int posicao = processamento.insumo.getEdgeCount();
        int distanciaMelhorOpcao = processamento.bfsalg.getDistance(processamento.insumo, melhorOpcao);
        if (distanciaMelhorOpcao < 4) {
            rbcount[1]++;
            return false;
        }

        if (anteciparVazio && processamento.bfsalg.getDistance(processamento.insumo, processamento.trabalhoAtual) == 0) {
            boolean condicao1 = true;
            int dv = (K - processamento.insumo.degree(processamento.trabalhoAtual));
            condicao1 = dv <= processamento.bfsalg.depthcount[4];
            if (!condicao1 && verbose) {
                System.out.printf("*[%d](%d,%d -> rdv=%d 4c=%d) ", posicao, processamento.trabalhoAtual, melhorOpcao, dv, processamento.bfsalg.depthcount[4]);
            }
            if (!condicao1) {
                rbcount[2]++;
                return false;
            }
        }
        if (descartarOpcoesNaoOptimais && !processamento.caminhoPercorrido.get(posicao).isEmpty()) {
            Integer escolhaAnterior = ((List<Integer>) processamento.caminhoPercorrido.get(posicao)).get(0);
            Integer rankingEscolhaAnterior = processamento.historicoRanking.get(posicao).get(escolhaAnterior).get(0);
            if (processamento.historicoRanking.get(posicao).get(melhorOpcao).get(0) < rankingEscolhaAnterior) {
                rbcount[3]++;
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

    private static boolean trabalhoNaoAcabou(Processamento processamento) {
        return !trabalhoAcabou(processamento.insumo, processamento.trabalhoAtual);
    }

    private static boolean temFuturo(Integer trabalhoAtual) {
        return true;
    }

    private static boolean temOpcoesDisponiveis(Processamento processamento) {
        boolean condicao0 = processamento.insumo.getEdgeCount() >= processamento.marcoInicial;

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

    private static Integer avaliarMelhorOpcao(Processamento processsamento) {
        processsamento.bfsalg.labelDistances(processsamento.insumo, processsamento.trabalhoAtual);
//        sort(opcoesPossiveis, bfsalg.getDistanceDecorator());
        sortAndRanking(processsamento);
        Collection<Integer> jaSelecionados = processsamento.caminhoPercorrido.get(processsamento.insumo.getEdgeCount());
        Integer indice = jaSelecionados.size();
        Integer melhorOpcao = getOpcao(processsamento.opcoesPossiveis, jaSelecionados);
        return melhorOpcao;
    }

    private static void sortAndRanking(Processamento processamento) {
        Integer[] bfs = processamento.bfsalg.bfs;
        processamento.opcoesPossiveis.sort(comparatorProfundidade.setBfs(bfs));
        int posicaoAtual = processamento.insumo.getEdgeCount();
        Collection<Integer> opcoesPassadas = processamento.caminhoPercorrido.get(processamento.insumo.getEdgeCount());
        if (rankearOpcoes) {
            Map<Integer, List<Integer>> rankingAtual = processamento.historicoRanking.getOrDefault(posicaoAtual, new HashMap<>());
            processamento.historicoRanking.putIfAbsent(posicaoAtual, rankingAtual);
            if (opcoesPassadas.isEmpty() || rankingAtual.isEmpty()) {
                rankingAtual.clear();
                int i = 0;
                for (i = 0; i < processamento.ranking.length; i++) {
                    processamento.ranking[i] = 0;
                }
                for (i = 0; i < processamento.opcoesPossiveis.size(); i++) {
                    Integer val = processamento.opcoesPossiveis.get(i);
                    if (bfs[val] == 4) {
                        processamento.bfsRanking.bfsRanking(processamento.insumo, processamento.trabalhoAtual, val);
                        processamento.ranking[val] = processamento.bfsRanking.depthcount[4];
                        List<Integer> listRankingVal = rankingAtual.get(val);
                        if (listRankingVal == null) {
                            listRankingVal = new ArrayList<>(4);
                            rankingAtual.put(val, listRankingVal);
                        } else {
                            listRankingVal.clear();
                        }
                        listRankingVal.add(processamento.bfsRanking.depthcount[4]);
                        listRankingVal.add(-processamento.bfsRanking.depthcount[3]);
                        listRankingVal.add(processamento.bfsRanking.depthcount[2]);
//                        listRankingVal.add(bfsRanking.depthcount[1]);
//                    ranking[val] = bfsRanking.depthcount[4] + bfsRanking.depthcount[3];
//                    ranking[val] = bfsRanking.depthcount[4] * 1000 + bfsRanking.depthcount[3];
//                    ranking[val] = bfsRanking.depthcount[3];
//                    ranking[val] = bfsRanking.depthcount[4] * 3000 + bfsRanking.depthcount[3] * 100 + bfsRanking.depthcount[3];
                    } else {
                        break;
                    }
//                if (trabalhoAtual.equals(18) && (val.equals(22) || val.equals(23))) {
//                    if (trabalhoAtual.equals(18)) {
//                    if (verboseRankingOption || posicaoAtual == 9505) {
                    if (verboseRankingOption) {
                        System.out.printf("Ranking (%4d,%4d): ", val, processamento.trabalhoAtual);
                        UtilTmp.printArray(processamento.bfsRanking.depthcount);
                    }
//                    }
                }
//                opcoesPossiveis.subList(0, i).sort(comparatorProfundidade.setBfs(ranking));
                processamento.opcoesPossiveis.subList(0, i).sort(comparatorProfundidade.setMapList(rankingAtual));
            } else {
//                opcoesPossiveis.subList(0, rankingAtual.size()).sort(comparatorProfundidade.setMap(rankingAtual));
                processamento.opcoesPossiveis.subList(0, rankingAtual.size()).sort(comparatorProfundidade.setMapList(rankingAtual));
                //Reaproveintando ranking anteriormente calculado
            }
        }
    }

    private static void sort(List<Integer> opcoesPossiveis, Map<Integer, Number> distanceDecorator) {
        opcoesPossiveis.sort(comparatorProfundidade.setMap(distanceDecorator));
    }

    static class ComparatorTrabalhoPorFazer implements Comparator<Integer> {

        Map<Integer, List<Integer>> caminhosPossiveis;

        public ComparatorTrabalhoPorFazer(Map<Integer, List<Integer>> caminhosPossiveis) {
            this.caminhosPossiveis = caminhosPossiveis;
        }

        @Override
        public int compare(Integer t, Integer t1) {
            int ret = 0;
            ret = Integer.compare(caminhosPossiveis.get(t1).size(), caminhosPossiveis.get(t).size());
            int cont = 0;
            while (ret == 0 && cont < caminhosPossiveis.get(t).size()) {
                ret = Integer.compare(caminhosPossiveis.get(t).get(cont), caminhosPossiveis.get(t1).get(cont));
                cont++;
            }

            if (ret == 0) {
                ret = Integer.compare(t, t1);
            }
            return ret;
        }

    }

    static class ComparatorMap implements Comparator<Integer> {

        Map<Integer, ? extends Number> mapRanking = null;
        Integer[] ranking = null;
        Map<Integer, List<Integer>> mapListRanking = null;

        public Comparator<Integer> setMap(Map<Integer, ? extends Number> map) {
            this.mapRanking = map;
            this.ranking = null;
            this.mapListRanking = null;
            return (Comparator<Integer>) this;
        }

        public Comparator<Integer> setBfs(Integer[] bfs) {
            this.mapRanking = null;
            this.mapListRanking = null;
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
            if (mapListRanking != null) {
                int cont = 0;
                while (ret == 0 && cont < rankearOpcoesProfundidade && cont < mapListRanking.get(o1).size()) {
                    ret = Integer.compare(mapListRanking.get(o2).get(cont), mapListRanking.get(o1).get(cont));
                    cont++;
                }
            }
            if (ret == 0) {
                ret = Integer.compare(o1, o2);
            }
            return ret;
        }

        private Comparator<? super Integer> setMapList(Map<Integer, List<Integer>> rankingAtual) {
            this.mapRanking = null;
            this.ranking = null;
            this.mapListRanking = rankingAtual;
            return (Comparator<Integer>) this;
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

    private static void verboseInit(UndirectedSparseGraphTO graphTemplate, Processamento processamento) {
        System.out.print("Graph[");
        System.out.print(graphTemplate.getVertexCount());
        System.out.print(", ");
        System.out.print(graphTemplate.getEdgeCount());
        System.out.println("]");

        System.out.print("Incomplete vertices[");
        System.out.print(processamento.trabalhoPorFazer.size());
        System.out.print("]: ");
        System.out.println(processamento.trabalhoPorFazer);
        System.out.print("Edges remain: ");
        System.out.println(processamento.len);
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

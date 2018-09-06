package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author strike
 */
public class StrategyEstagnacaoCrescente implements IGenStrategy {

    public String getName() {
        return "Estagnação de Vertice Crescente";
    }

    private Comparator<Integer> comparatorTrabalhoPorFazer;
    private ComparatorMap comparatorProfundidade;

    public ComparatorMap getComparatorProfundidade(Processamento processamento) {
        if (comparatorProfundidade == null) {
            comparatorProfundidade = new ComparatorMap(processamento.rankearOpcoesProfundidade);
        }
        return comparatorProfundidade;
    }

    public Comparator<Integer> getComparatorTrabalhoPorFazer(Processamento processamento) {
        if (comparatorTrabalhoPorFazer == null) {
            comparatorTrabalhoPorFazer = new ComparatorTrabalhoPorFazer(processamento.caminhosPossiveis);
        }
        return comparatorTrabalhoPorFazer;
    }

    public void generateGraph(Processamento processamento) {
        ordenacaoFimEtapa(processamento);
        verboseInicioGeracao(processamento);
        UtilTmp.printCurrentItme();
        processamento.marcoInicial();
        while (!processamento.trabalhoPorFazer.isEmpty() && processamento.deuPassoFrente()) {
            processamento.trabalhoAtual = processamento.trabalhoPorFazer.get(0);
            estagnarVertice(processamento);
            verboseFimEtapa(processamento);
            ordenacaoFimEtapa(processamento);
        }
        processamento.printGraphCount();
        verboseResultadoFinal(processamento);
    }

    public void estagnarVertice(Processamento processamento) throws IllegalStateException {
        verboseInicioEtapa(processamento);
        System.out.println("Estagnando vertice: " + processamento.trabalhoAtual);
        while (trabalhoNaoAcabou(processamento) && processamento.deuPassoFrente()) {
            processamento.getCaminhoPercorridoPosicaoAtual();
            processamento.melhorOpcaoLocal = avaliarMelhorOpcao(processamento);
            adicionarMellhorOpcao(processamento);
        }
        if (trabalhoAcabou(processamento, processamento.trabalhoAtual) && temFuturo(processamento.trabalhoAtual)) {
            processamento.trabalhoPorFazer.remove(processamento.trabalhoAtual);
        }
    }

    public void ordenacaoFimEtapa(Processamento processamento) {
        if (processamento.ordenarTrabalhoPorFazerPorPrimeiraOpcao) {
            Collections.sort(processamento.trabalhoPorFazer, getComparatorTrabalhoPorFazer(processamento));
        } else {
            Collections.sort(processamento.trabalhoPorFazer);
        }
    }

    void verboseInicioGeracao(Processamento processamento) {
        System.out.println(this.getClass().getSimpleName());
        if (processamento.vebosePossibilidadesIniciais) {
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
        }
        System.out.println();
    }

    void adicionarMellhorOpcao(Processamento processamento) {
        if (opcaoViavel(processamento)) {
            Integer posicaoAtual = processamento.getPosicaoAtualAbsoluta();
            Collection<Integer> subcaminho = processamento.caminhoPercorrido.getOrDefault(posicaoAtual, new ArrayList<>());
            subcaminho.add(processamento.melhorOpcaoLocal);
            if (processamento.verticeComplete(processamento.melhorOpcaoLocal)) {
                throw new IllegalStateException("vertice statured " + posicaoAtual + " " + processamento.trabalhoAtual + " " + processamento.melhorOpcaoLocal);
            }
            Integer aresta = processamento.addEge();
            if (!aresta.equals(posicaoAtual)) {
                throw new IllegalStateException("Edge not added: " + posicaoAtual + " " + processamento.trabalhoAtual + " " + processamento.melhorOpcaoLocal);
            }
            processamento.caminhoPercorrido.putIfAbsent(aresta, subcaminho);
            if (trabalhoAcabou(processamento, processamento.melhorOpcaoLocal)) {
                processamento.trabalhoPorFazer.remove(processamento.melhorOpcaoLocal);
            }
            observadorDeEtapa(aresta, processamento.melhorOpcaoLocal, processamento);
        } else {
            desfazerUltimoTrabalho(processamento);
        }
    }

    void verboseInicioEtapa(Processamento processamento) {

    }

    boolean opcaoViavel(Processamento processamento) {
        Integer melhorOpcao = processamento.melhorOpcaoLocal;

        if (!processamento.bfsalg.getDistance(processamento.insumo, processamento.trabalhoAtual).equals(0)) {
            throw new IllegalStateException("Etado do bfs incorreto para" + processamento.trabalhoAtual + " " + processamento.getPosicaoAtualAbsoluta());
        }

        if (melhorOpcao == null) {
            processamento.rbcount[0]++;
            if (processamento.verbose) {
                System.out.println("melhor opçao é nula");
            }
            return false;
        }

        int posicao = processamento.getPosicaoAtualAbsoluta();
        int distanciaMelhorOpcao = processamento.bfsalg.getDistance(processamento.insumo, melhorOpcao);
        if (distanciaMelhorOpcao < 4) {
            processamento.rbcount[1]++;
            if (processamento.verbose) {
                System.out.println("cintura inadequada");
            }
            return false;
        }

        if (processamento.anteciparVazio && processamento.bfsalg.getDistance(processamento.insumo, processamento.trabalhoAtual) == 0) {
            boolean condicao1 = true;
            int dv = processamento.getDvTrabalhoAtual();
            int contRemanescente = 0;
            Integer lastAdd = processamento.getLastAdd();
            if (lastAdd != null) {
                for (Integer vd : processamento.getOpcoesPossiveisAtuais()) {
                    if (processamento.bfsalg.getDistance(processamento.insumo, vd) == 4 && vd.compareTo(lastAdd) == 1) {
                        contRemanescente++;
                    }
                }
            } else {
                contRemanescente = processamento.bfsalg.depthcount[4];
            }
//            condicao1 = dv <= processamento.bfsalg.depthcount[4];
            condicao1 = dv <= contRemanescente;

            if (!condicao1 && processamento.verbose) {
                System.out.printf("*[%d](%d,%d -> rdv=%d 4c=%d) ", posicao, processamento.trabalhoAtual, melhorOpcao, dv, processamento.bfsalg.depthcount[4]);
            }
            if (!condicao1) {
                processamento.rbcount[2]++;
                return false;
            }
        }
        if (processamento.descartarOpcoesNaoOptimais && !processamento.caminhoPercorrido.get(posicao).isEmpty()) {
            Integer escolhaAnterior = ((List<Integer>) processamento.caminhoPercorrido.get(posicao)).get(0);
            List<Integer> rankingAnterior = processamento.historicoRanking.get(posicao).get(escolhaAnterior);
            if (rankingAnterior != null) {
                Integer rankingEscolhaAnterior = rankingAnterior.get(0);
                if (processamento.historicoRanking.get(posicao).get(melhorOpcao).get(0) < rankingEscolhaAnterior) {
                    processamento.rbcount[3]++;
                    return false;
                }
            }
        }
        return true;
    }

    boolean trabalhoAcabou(Processamento processamento, Integer vertice) {
        return processamento.verticeComplete(vertice);
    }

    boolean trabalhoNaoAcabou(Processamento processamento) {
        return !trabalhoAcabou(processamento, processamento.trabalhoAtual);
    }

    boolean temFuturo(Integer trabalhoAtual) {
        return true;
    }

    void observadorDeEtapa(Integer aresta,
            Integer melhorOpcaoLocal, Processamento processamento) {
        if (processamento.verbose) {
            System.out.printf("+[%5d](%4d,%4d) ", aresta, processamento.trabalhoAtual, melhorOpcaoLocal);
        }
        processamento.dumpResultadoSeInteressante();
    }

    void verboseResultadoFinal(Processamento processamento) {
        System.out.println();
        if (!processamento.atingiuObjetivo()) {
            System.out.println("Busca pelo grafo Falhou ***");
        } else {
            System.out.println("Grafo Encontrado");
        }

        try {
            System.out.print("Added-Edges: ");
            for (Integer e : processamento.caminhoPercorrido.navigableKeySet()) {
                Pair endpoints = processamento.insumo.getEndpoints(e);
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
        String edgeString = processamento.insumo.getEdgeString();
        System.out.println(edgeString);
    }

    void verboseFimEtapa(Processamento processamento) throws IllegalStateException {
        if (trabalhoAcabou(processamento, processamento.trabalhoAtual) && temFuturo(processamento.trabalhoAtual)) {
            System.out.printf(".. %d [%d] \n", processamento.trabalhoAtual, processamento.countEdges());
        } else {
            System.out.printf("!! %d \n", processamento.trabalhoAtual);
        }
        System.out.printf("rbcount[%d,%d,%d,%d]=%d ", processamento.rbcount[0], processamento.rbcount[1],
                processamento.rbcount[2], processamento.rbcount[3],
                (processamento.rbcount[0] + processamento.rbcount[1]
                + processamento.rbcount[2] + processamento.rbcount[3]));
        System.out.println(processamento.getEstrategiaString());
        UtilTmp.printCurrentItme();

        if (processamento.veboseFimEtapa) {
            verboseFimEtapa(processamento.caminhoPercorrido, processamento.insumo);
        }
        if (processamento.falhaInCommitCount) {
            if (processamento.falhaCommitCount-- <= 0) {
                throw new IllegalStateException("Interrução forçada -- commit");
            }
        }
    }

    void verboseFimEtapa(TreeMap<Integer, Collection<Integer>> caminhoPercorrido, UndirectedSparseGraphTO insumo) {
        System.out.println("------------------------------------------------------------------------------------------------");
        System.out.print("Caminhos percorrido: ");
        caminhoPercorrido.entrySet().forEach(e -> System.out.printf("%d(%s)=%s\n", e.getKey(), insumo.getEndpoints(e.getKey()), e.getValue().toString()));
        System.out.println();
    }

    Integer avaliarMelhorOpcao(Processamento processsamento) {
        processsamento.bfsalg.labelDistances(processsamento.insumo, processsamento.trabalhoAtual);
        Collection<Integer> jaSelecionados = processsamento.getCaminhoPercorridoPosicaoAtual();
        sortAndRanking(processsamento);
        Integer melhorOpcao = null;
        List<Integer> opcoesPossiveisAtuais = processsamento.getOpcoesPossiveisAtuais();
        Integer lastAdd = processsamento.getLastAdd();
        for (int i = 0; i < opcoesPossiveisAtuais.size(); i++) {
            Integer val = opcoesPossiveisAtuais.get(i);
            if (lastAdd == null || val.compareTo(lastAdd) == 1) {
                if (!jaSelecionados.contains(val)) {
                    melhorOpcao = val;
                    break;
                }
            }
        }
        return melhorOpcao;
    }

    void sortAndRanking(Processamento processamento) {
        if (processamento.rankearOpcoes) {
            rankearOpcoes(processamento);
        }
    }

    public void rankearOpcoes(Processamento processamento) throws RuntimeException {
        Integer[] bfs = processamento.bfsalg.bfs;
        int posicaoAtual = processamento.getPosicaoAtualAbsoluta();
        Collection<Integer> opcoesPassadas = processamento.getCaminhoPercorridoPosicaoAtual();
        Map<Integer, List<Integer>> rankingAtual = processamento.historicoRanking.getOrDefault(posicaoAtual, new HashMap<>());
        processamento.historicoRanking.putIfAbsent(posicaoAtual, rankingAtual);
        if (opcoesPassadas.isEmpty() || rankingAtual.isEmpty()) {
            processamento.getOpcoesPossiveisAtuais().sort(getComparatorProfundidade(processamento).setBfs(bfs));
            rankingAtual.clear();
            int i = 0;
            for (i = 0; i < processamento.getOpcoesPossiveisAtuais().size(); i++) {
                Integer val = processamento.getOpcoesPossiveisAtuais().get(i);
                if (bfs[val] == 4) {
                    List<Integer> listRankingVal = rankingAtual.get(val);
                    if (listRankingVal == null) {
                        listRankingVal = new ArrayList<>(4);
                        rankingAtual.put(val, listRankingVal);
                    }
                    rankearOpcao(processamento, posicaoAtual, val);
                } else {
                    break;
                }
                if (processamento.verboseRankingOption) {
                    System.out.printf("Ranking (%4d,%4d): ", val, processamento.trabalhoAtual);
                    UtilTmp.printArray(processamento.bfsRanking.depthcount);

                }
            }
            processamento.getOpcoesPossiveisAtuais().subList(0, i).sort(getComparatorProfundidade(processamento).setMapList(rankingAtual));
        } else {
            List<Integer> subList = null;
            try {
                processamento.getOpcoesPossiveisAtuais().sort(getComparatorProfundidade(processamento).setBfs(bfs));
                subList = processamento.getOpcoesPossiveisAtuais().subList(0, rankingAtual.size());
                subList.sort(getComparatorProfundidade(processamento).setMapList(rankingAtual));
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }

    public void rankearOpcao(Processamento processamento, Integer posicaoAtual, Integer val) {
        processamento.bfsRanking(val);
        List<Integer> listRankingVal = processamento.historicoRanking.get(posicaoAtual).get(val);
        listRankingVal.clear();
        listRankingVal.add(processamento.bfsRanking.depthcount[4]);
        listRankingVal.add(-processamento.bfsRanking.depthcount[3]);
        listRankingVal.add(processamento.bfsRanking.depthcount[2]);
        if (processamento.rankearSegundaOpcoes) {
            processamento.bfsRankingSegundaOpcao.bfsRanking(processamento.insumo, val, processamento.trabalhoAtual);
            int f = processamento.bfsRankingSegundaOpcao.depthcount[3];
            processamento.bfsRankingSegundaOpcao.bfsRanking(processamento.insumo, val);
            listRankingVal.add(processamento.bfsRankingSegundaOpcao.depthcount[3] - f);
        }
    }

    Pair<Integer> desfazerUltimoTrabalho(Processamento processamento) {
        return processamento.desfazerUltimoTrabalho();
    }
}

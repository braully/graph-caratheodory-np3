package tmp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrategyEstagnacaoLenta
        extends StrategyEstagnacao
        implements IGenStrategy {

    public String getName() {
        return "Estagnação lenta";
    }

    void sortAndRanking(Processamento processamento) {
        Integer[] bfs = processamento.bfsalg.bfs;
        int posicaoAtual = processamento.getPosicaoAtualAbsoluta();
        Collection<Integer> opcoesPassadas = processamento.getCaminhoPercorridoPosicaoAtual();
        if (processamento.rankearOpcoes) {
            Map<Integer, List<Integer>> rankingAtual = processamento.historicoRanking.getOrDefault(posicaoAtual, new HashMap<>());
            processamento.historicoRanking.putIfAbsent(posicaoAtual, rankingAtual);
            if (opcoesPassadas.isEmpty() || rankingAtual.isEmpty()) {
                processamento.getOpcoesPossiveisAtuais().sort(getComparatorProfundidade(processamento).setBfs(bfs));
                rankingAtual.clear();
                int i = 0;
                for (i = 0; i < processamento.getOpcoesPossiveisAtuais().size(); i++) {
                    Integer val = processamento.getOpcoesPossiveisAtuais().get(i);
                    if (bfs[val] == 4) {
                        processamento.bfsRanking(val);
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
                        if (processamento.rankearSegundaOpcoes) {
                            processamento.bfsRankingSegundaOpcao.bfsRanking(processamento.insumo, val, processamento.trabalhoAtual);
                            int f = processamento.bfsRankingSegundaOpcao.depthcount[3];
                            processamento.bfsRankingSegundaOpcao.bfsRanking(processamento.insumo, val);
                            listRankingVal.add(processamento.bfsRankingSegundaOpcao.depthcount[3] - f);
                        }
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
                    subList = processamento.getOpcoesPossiveisAtuais().subList(0, rankingAtual.size());
                    subList.sort(getComparatorProfundidade(processamento).setMapList(rankingAtual));
                } catch (RuntimeException e) {
                    throw e;
                }
            }
        }
    }
}

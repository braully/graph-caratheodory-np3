package tmp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StrategyBlock
        extends StrategyEstagnacao
        implements IGenStrategy {

    public String getName() {
        return "Gerar em Bloco";
    }

    public void generateGraph(Processamento processamento) {
        verboseInicioGeracao(processamento);
        ordenacaoFimEtapa(processamento);

        TreeMap<Integer, LinkedList<Integer>> blocos = new TreeMap<>();
        TreeMap<Integer, LinkedList<Integer>> blocosConcluidos = new TreeMap<>();
        Integer count = 0;
        List<Integer> ant = processamento.caminhosPossiveis.get(processamento.trabalhoPorFazer.get(0));
        blocos.put(count, new LinkedList<>());

        for (Integer e : processamento.trabalhoPorFazer) {
            List<Integer> at = processamento.caminhosPossiveis.get(e);
            if (!at.equals(ant)) {
                count++;
                blocos.put(count, new LinkedList<>());
                if (processamento.vebosePossibilidadesIniciais) {
                    System.out.println("----------------------------------------------------------------------------------------------");
                }
            }
            blocos.get(count).add(e);
            if (processamento.vebosePossibilidadesIniciais) {
                System.out.printf("%d|%d|=%s\n", e, at.size(), at.toString());
            }
            ant = at;
        }
//        blocos.pollLastEntry();

        while (!blocos.isEmpty()) {
            Map.Entry<Integer, LinkedList<Integer>> firstEntry = blocos.firstEntry();
            LinkedList<Integer> bloco = firstEntry.getValue();
            System.out.printf("Processando bloco %d vertices %s\n", firstEntry.getKey(), firstEntry.getValue().toString());

            while (temTrabalhoNoBloco(processamento, bloco)) {
                processamento.trabalhoAtual = UtilTmp.getOverflow(bloco, processamento.getPosicaoAtual());
                processamento.marcoInicial = processamento.insumo.getEdgeCount();
                verboseInicioEtapa(processamento);

                if (trabalhoNaoAcabou(processamento)
                        && temOpcoesDisponiveis(processamento)) {
                    if (!processamento.caminhoPercorrido.containsKey(processamento.insumo.getEdgeCount())) {
                        processamento.caminhoPercorrido.put(processamento.insumo.getEdgeCount(), new ArrayList<>());
                    }
                    processamento.melhorOpcaoLocal = avaliarMelhorOpcao(processamento);
                    adicionarMellhorOpcao(processamento);
                }
                if (trabalhoAcabou(processamento, processamento.trabalhoAtual)
                        && temFuturo(processamento.trabalhoAtual)) {
                    processamento.trabalhoPorFazer.remove(processamento.trabalhoAtual);
                    verboseFimEtapa(processamento);
                }
            }
            blocosConcluidos.put(firstEntry.getKey(), firstEntry.getValue());
            blocos.remove(firstEntry.getKey());
            System.out.printf("Concluido bloco %d vertices %s\n", firstEntry.getKey(), firstEntry.getValue().toString());
            verboseFimEtapa(processamento);
        }
        verboseResultadoFinal(processamento);
    }

//    @Override
//    void adicionarMellhorOpcao(Processamento processamento) {
//        super.adicionarMellhorOpcao(processamento);
//    }
//    @Override
//    Pair<Integer> desfazerUltimoTrabalho(Processamento processamento) {
////        Pair<Integer> desfazer = super.desfazerUltimoTrabalho(processamento);
////        blocoidx--;
////        Integer idx = bloco.indexOf(desfazer.getFirst());
////        blocoidx = idx;
////        return desfazer;
//        return super.desfazerUltimoTrabalho(processamento);
//    }
//
    boolean temTrabalhoNoBloco(Processamento processamento, LinkedList<Integer> bloco) {
        boolean ret = true;
        for (Integer i : bloco) {
            ret = ret && processamento.verticeComplete(i);
            if (!ret) {
                break;
            }
        }
        return !ret;
    }
}

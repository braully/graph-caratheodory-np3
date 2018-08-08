package tmp;

import java.util.ArrayList;
import java.util.Collections;

public class StrategyAvoidCollid extends StrategyEstagnacao implements IGenStrategy {

    public String getName() {
        return "Evitar colisão";
    }

    public void generateGraph(Processamento processamento) {
        verboseInicioGeracao(processamento);

        while (!processamento.trabalhoPorFazer.isEmpty() && !processamento.caminhoPercorrido.isEmpty()) {
            processamento.trabalhoAtual = processamento.trabalhoPorFazer.get(0);
            processamento.opcoesPossiveis = processamento.caminhosPossiveis.get(processamento.trabalhoAtual);
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
            ordenacaoFimEtapa(processamento);
        }
        verboseResultadoFinal(processamento);
    }

    public void ordenacaoFimEtapa(Processamento processamento) {
        if (processamento.ordenarTrabalhoPorFazerPorPrimeiraOpcao) {
            Collections.sort(processamento.trabalhoPorFazer, getComparatorTrabalhoPorFazer(processamento));
        } else {
            Collections.sort(processamento.trabalhoPorFazer);
        }
    }
}

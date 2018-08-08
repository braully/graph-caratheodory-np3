package tmp;

import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StrategyAvoidCollid
        extends StrategyEstagnacao
        implements IGenStrategy {

    Comparator<Integer> comparatorTrabalhoPorFazer;
    int[] sortindex;

    public String getName() {
        return "Evitar colisão";
    }

    public void generateGraph(Processamento processamento) {
        verboseInicioGeracao(processamento);
        sortindex = new int[processamento.numVertices];
        for (int i = 0; i < processamento.numVertices; i++) {
            sortindex[i] = Integer.MIN_VALUE;
        }
        for (Integer v : processamento.trabalhoPorFazer) {
            sortindex[v] = processamento.caminhosPossiveis.get(v).size();
        }
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

    @Override
    public void ordenacaoFimEtapa(Processamento processamento) {
        if (processamento.ordenarTrabalhoPorFazerPorPrimeiraOpcao) {
            Collections.sort(processamento.trabalhoPorFazer, getComparatorTrabalhoPorFazer());
        } else {
            Collections.sort(processamento.trabalhoPorFazer);
        }
    }

    public Comparator<Integer> getComparatorTrabalhoPorFazer() {
        if (comparatorTrabalhoPorFazer == null) {
            comparatorTrabalhoPorFazer = new ComparatorSortIndex(sortindex).reversed();
        }
        return comparatorTrabalhoPorFazer;
    }

    @Override
    Pair<Integer> desfazerUltimoTrabalho(Processamento processamento) {
        Pair<Integer> ultimoTrabalho = super.desfazerUltimoTrabalho(processamento);
        sortindex[ultimoTrabalho.getFirst()]++;
        sortindex[ultimoTrabalho.getSecond()]++;
        return ultimoTrabalho;
    }

    @Override
    void adicionarMellhorOpcao(Processamento processamento) {
        super.adicionarMellhorOpcao(processamento);
        sortindex[processamento.trabalhoAtual]--;
        sortindex[processamento.melhorOpcaoLocal]--;
    }
}

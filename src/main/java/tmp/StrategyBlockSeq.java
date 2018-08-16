package tmp;

import java.util.LinkedList;

public class StrategyBlockSeq
        extends StrategyBlock
        implements IGenStrategy {

    public String getName() {
        return "Gerar em Bloco Sequencial";
    }

    @Override
    public void estagnarBloco(Processamento processamento, LinkedList<Integer> bloco) {
        for (Integer v : bloco) {
            processamento.marcoInicial();
            processamento.trabalhoAtual = v;
            estagnarVertice(processamento);
            verboseFimEtapa(processamento);
        }
    }
}

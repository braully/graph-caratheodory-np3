package tmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StrategyBlockParallel
        extends StrategyBlock
        implements IGenStrategy {

    public String getName() {
        return "Gerar em Bloco Paralelamente";
    }

    @Override
    public void processarBlocos(TreeMap<Integer, LinkedList<Integer>> blocos, Processamento processamento) throws IllegalStateException {
        Map<Integer, List<Integer>> blocksBySize = new HashMap<>();
        Integer great = 0;
        for (Map.Entry<Integer, LinkedList<Integer>> es : blocos.entrySet()) {
            Integer size = processamento.caminhosPossiveis.get(es.getValue().get(0)).size();
            List<Integer> list = blocksBySize.getOrDefault(size, new ArrayList<Integer>());
            list.add(es.getKey());
            if (size > great) {
                great = size;
            }
            blocksBySize.putIfAbsent(size, list);
        }

        System.out.println(" " + blocos.size() + " Blocos ");
        System.out.println(blocksBySize.get(great).size() + " blocos de tamanho " + great + " serão processados");

        UtilTmp.printCurrentItme();
        /* */
        List<TrabalhoProcessamento> processos = new ArrayList<>();
        Integer numThreads = blocksBySize.get(great).size();
        List<Integer> blocosPraProcessar = blocksBySize.get(great);

        for (Integer i = 0; i < numThreads; i++) {
            LinkedList<Integer> bloco = blocos.get(blocosPraProcessar.get(i));
//            Integer vp = blocos.get().get(0);
            Integer vertice = bloco.get(0);
            Integer indexOf = processamento.trabalhoPorFazer.indexOf(vertice);
            processos.add(new TrabalhoProcessamento(indexOf));
        }
        processos.parallelStream().forEach(p -> p.generateGraph(processamento.fork()));
        List<Processamento> processamentos = new ArrayList<>();
        for (TrabalhoProcessamento processo : processos) {
            processamentos.add(processo.last);
        }
        System.out.println("Barreira atingida");
        UtilTmp.printCurrentItme();
        processos.parallelStream().forEach(p -> p.processarProximo());
        System.out.println("Merge");
        processamento.mergeProcessamentos(processamentos);
    }
}

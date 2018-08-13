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
    }
}

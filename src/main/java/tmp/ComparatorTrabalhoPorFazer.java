/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author strike
 */
public class ComparatorTrabalhoPorFazer implements Comparator<Integer> {

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

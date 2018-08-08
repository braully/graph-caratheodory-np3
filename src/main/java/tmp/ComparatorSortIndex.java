/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

import java.util.Comparator;

/**
 *
 * @author braully
 */
public class ComparatorSortIndex
        implements Comparator<Integer> {

    int[] sortindex;

    public ComparatorSortIndex(int[] sortindex) {
        this.sortindex = sortindex;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
        int ret = 0;
        ret = Integer.compare(sortindex[o1], sortindex[o2]);
        if (ret == 0) {
            ret = o1.compareTo(o2);
        }
        return ret;
    }

}

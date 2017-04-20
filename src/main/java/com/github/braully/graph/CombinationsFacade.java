package com.github.braully.graph;

import java.util.Iterator;
import org.apache.commons.math3.util.CombinatoricsUtils;

/**
 *
 * @author braully
 */
public class CombinationsFacade {

    public static synchronized int[] getCombinationNKByLexicographIndex(int n, int k, int index) {
        int[] comb = null;
        if (n <= 0 || k <= 0 || index < 0) {
            return comb;
        }
        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(n, k);
        int cont = 0;
        while (combinationsIterator.hasNext()) {
            if (cont++ == index) {
                comb = combinationsIterator.next();
                break;
            }
        }
        return comb;
    }
}

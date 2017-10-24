/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author strike
 */
public class CombinationsFacadeTest {

    @Test
    @Ignore
    public void testCombination5_3() {
        int n = 10;
        int k = 3;
        int[] comb = new int[k];
        long maxCombinations = CombinationsFacade.maxCombinations(n, k);
        CombinationsFacade.initialCombination(n, k, comb);
        for (int i = 0; i < maxCombinations; i++) {
            System.out.printf("%3d-", i);
            CombinationsFacade.printCombination(comb);
            System.out.print(" - ");
            System.out.print(CombinationsFacade.lexicographicIndex(n, k, comb));
            System.out.println();
            CombinationsFacade.nextCombination(n, k, comb);
        }
    }

    @Test
    public void testCombinationBig() {
        int n = 52;
        int k = 9;
        long maxCombinations = CombinationsFacade.maxCombinations(n, k);
        BigInteger maxBig = CombinationsFacade.maxCombinationsBig(n, k);
        System.out.println("(" + n + ", " + k + ")=" + maxCombinations);
        System.out.println("(" + n + ", " + k + ")=" + maxBig.longValue());
        Assert.assertEquals(maxCombinations, maxBig.longValue());
    }

    @Test
    public void testCombinationHoff() {
        int n = 5279625;
        int k = 92625;
        BigInteger maxCombinationsBig = CombinationsFacade.maxCombinationsBig(n, k);
        System.out.println("(" + n + ", " + k + ")=" + maxCombinationsBig);
    }
}

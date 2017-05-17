/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

/**
 *
 * @author strike
 */
public class CombinationsFacadeTest {
    
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
}

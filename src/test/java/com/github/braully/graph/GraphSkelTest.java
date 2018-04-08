/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import junit.framework.TestCase;

/**
 *
 * @author strike
 */
public class GraphSkelTest extends TestCase {

    public void testSkelGraphMoore() {
        int k = 7;
        int cont = 1;
        for (int i = 0; i < k * k; i = k + i) {
            for (int j = 0; j < k - 1; j++) {
                System.out.printf("%d-%d,", i, cont + j);
            }
            cont = cont + k;
            System.out.println();
        }

        System.out.println();

        for (int j = 1; j < k; j++) {
            int ant = j;
            for (int i = k + j; i < k * k; i = k + i) {
                System.out.printf("%d-%d,", ant, i);
                ant = i;
            }
            System.out.println();
        }
    }
}

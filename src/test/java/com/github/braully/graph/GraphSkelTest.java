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
        System.out.println("Estrategia-1");
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

    public void testSkelGraphMoore2() {
        System.out.println();
        System.out.println("Estrategia-2");

        int k = 7;

        int cont = 1;
        int lastVert = k * k + 1;

        cont = k;

        for (int j = 0; j < k - 1; j++) {
            System.out.printf("%d-%d,", j, j + k - 1);
            for (int i = j; i < k - 1; i++) {
                int x = i + (2 + j) * (k - 1);
                int y = i + (2 + j) * (k - 1) + 1;
                System.out.printf("%d-%d,", j, x);
                System.out.printf("%d-%d,", j + k - 1, y);
            }
            System.out.println();
        }
        System.out.println();
    }
}

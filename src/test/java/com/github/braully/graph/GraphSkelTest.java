/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        int maxvert = k * k;
        for (int j = 0; j < k - 1; j++) {
            System.out.printf("%d-%d,", j, j + k - 1);
        }
        System.out.println();

        int offset = 2 * (k - 1);
        int join = k * (k - 1);

        for (int j = 0; j < k - 1; j++) {
            int u = j;
            int v = j + k - 1;
            //System.out.printf("%d-%d,", j, j + k - 1);
            int tu = u;
            int tv = v;
            for (int i = 0; i < k - (2 + j); i++) {
                System.out.printf("%d-%d,", u, offset);
                if (j == 0) {
                    System.out.printf("%d-%d,", offset, join);
                }
                System.out.printf("%d-%d,", offset++, ++tv);

                System.out.printf("%d-%d,", v, offset);
                if (j == 0) {
                    System.out.printf("%d-%d,", maxvert, join);
                    System.out.printf("%d-%d,", offset, join++);
                }
                System.out.printf("%d-%d,", offset++, ++tu);
            }
            System.out.println();
        }

        System.out.printf("%d-%d,", join, maxvert);
        System.out.printf("%d-%d,", maxvert, join + 1);
        System.out.println();
        for (int j = 0; j < k - 1; j++) {
            int u = j;
            int v = j + k - 1;
            System.out.printf("%d-%d,", join, u);
            System.out.printf("%d-%d,", join + 1, v);
        }
        System.out.println();
    }

    public void testCombinacao() {
        int k = 7;
        int ko = k - 2;
        int len = ((ko + 1) * ko) / 2;
        int idx = len;
        int arr[] = new int[len];
        Map<Integer, List<Integer>> possibilidades = new HashMap<>(len);

        for (int i = 0; i < ko; i++) {
            arr[i] = idx++;
            List<Integer> listaPossiveis = new ArrayList<>(len);
            possibilidades.put(i, listaPossiveis);
        }

        int pos = ko;

        while (pos <= len) {
            arr[pos] = 0;
            pos++;
        }
    }
}

package com.github.braully.graph;

import java.util.ArrayList;
import java.util.Arrays;
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
        int[] arr = getCombincaoInterna(k);

        System.out.println("\nComb-" + k + ":");
        printArray(arr);
//        k = 57;
        arr = getCombincaoInterna2(k);
        System.out.println("\nComb-" + k + ":");
        printArray(arr);
    }

    public void testSkelGraphMoore3() {
        System.out.println();
        System.out.println("Estrategia-3");
        int k = 2;

        int ko = k - 2;
        int maxvert = k * k;
        for (int j = 0; j < k - 1; j++) {
            System.out.printf("%d-%d,", j, j + k - 1);
        }
        System.out.println();

        int offset = 2 * (k - 1);
        int join = k * (k - 1);

        int[] comb = getCombincaoInterna(k);
        int idx = 0;

        for (int j = 0; j < k - 1; j++) {
            int u = j;
            int v = j + k - 1;
            //System.out.printf("%d-%d,", j, j + k - 1);
            int tu = u;
            int tv = v;
            for (int i = 0; i < k - (2 + j); i++) {
                System.out.printf("%d-%d,", u, offset);
//                if (j == 0) {
//                    System.out.printf("%d-%d,", offset, join);
//                }
                System.out.printf("%d-%d,", offset, join + comb[idx]);
                System.out.printf("%d-%d,", offset++, ++tv);
                System.out.printf("%d-%d,", v, offset);
//                if (j == 0) {
//                    System.out.printf("%d-%d,", maxvert, join);
//                    System.out.printf("%d-%d,", offset, join++);
//                }
                System.out.printf("%d-%d,", maxvert, join + comb[idx]);
                System.out.printf("%d-%d,", offset, join + comb[idx++]);
                System.out.printf("%d-%d,", offset++, ++tu);
            }
            System.out.println();
        }
        join = join + ko;
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

    private boolean exclude(int[] arrup, int[] arrdown, int[] arr, int pos, int val) {
        int up = arrup[pos];
        int down = arrdown[pos];
        boolean ret = false;
        for (int i = 0; i < pos && !ret; i++) {
            if (arrdown[i] == up || arrdown[i] == down || arrup[i] == up) {
                ret = ret || arr[i] == val;
            }
        }
        return ret;
    }

    public void testSequel() {
        int k = 7;
        int ko = k - 2;
        int[] arr = new int[]{11, 6, 2, 12, 0, 7, 5, 4, 10, 1, 13, 3, 9, 14, 8};
        System.out.println("Genetic algo: [");
        for (int i = 0; i < ko; i++) {
            System.out.print(i);
            System.out.print(", ");
        }
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] % ko);
            System.out.print(", ");
        }
        System.out.println("]");
    }

//    public static synchronized void nextCombination(int n,
//            int k,
//            int[] currentCombination) {
//        if (currentCombination[0] == n - k) {
//            return;
//        }
//        int i;
//        for (i = k - 1; i > 0 && currentCombination[i] == n - k + i; --i);
//        ++currentCombination[i];
//        for (int j = i; j < k - 1; ++j) {
//            currentCombination[j + 1] = currentCombination[j] + 1;
//        }
//    }
//    public static synchronized void nextCombination(int n,
//            int k,
//            int[] currentCombination) {
//        if (currentCombination[0] == n - k) {
//            return;
//        }
//        int i;
//        for (i = k - 1; i > 0 && currentCombination[i] == n - k + i; --i);
//        ++currentCombination[i];
//        for (int j = i; j < k - 1; ++j) {
//            currentCombination[j + 1] = currentCombination[j] + 1;
//        }
//    }
    public void testSequence() {
        int[] arr = new int[]{3, 4, 2, 1, 0, 4, 2, 1, 3, 0};
        int k = 7;
        boolean test = checkSequenceParcial(k, arr);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }
        printArray(arr);

        arr = new int[]{0, 1, 2, 3, 4, 2, 3, 4, 1, 4, 0, 3, 1, 0, 2};
        test = checkSequence(k, arr);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }
        printArray(arr);

        arr = new int[]{2, 3, 4, 1, 4, 0, 3, 1, 0, 2};
        test = checkSequenceParcial(k, arr);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }
        printArray(arr);

        arr = new int[]{0, 1, 2, 3, 4, 2, 3, 4, 1, 4, 0, 3, 1, 0, 2};
        test = checkSequence(k, arr);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }
        printArray(arr);

        //arr = new int[]{38, 13, 32, 37, 51, 26, 23, 18, 2, 43, 5, 34, 49, 8, 3, 40, 33, 53, 17, 22, 16, 16, 12, 42, 46, 48, 44, 4, 19, 6, 28, 50, 21, 11, 25, 14, 29, 20, 27, 45, 54, 10, 0, 9, 28, 2, 31, 18, 39, 47, 30, 41, 10, 7, 25, 30, 48, 32, 54, 2, 42, 10, 26, 34, 22, 46, 29, 46, 44, 27, 20, 49, 8, 47, 6, 21, 9, 37, 54, 50, 36, 24, 4, 51, 41, 3, 35, 18, 5, 20, 39, 49, 1, 7, 16, 19, 17, 12, 33, 43, 52, 19, 14, 53, 45, 28, 29, 39, 10, 44, 10, 29, 16, 53, 33, 40, 14, 51, 1, 7, 49, 47, 31, 21, 36, 0, 35, 11, 8, 42, 14, 38, 34, 6, 26, 18, 3, 41, 37, 13, 37, 17, 48, 22, 5, 54, 30, 22, 15, 45, 24, 12, 28, 46, 15, 19, 18, 52, 9, 33, 14, 4, 36, 45, 27, 44, 35, 0, 10, 18, 2, 34, 9, 18, 40, 41, 29, 11, 15, 49, 47, 43, 44, 54, 15, 16, 32, 35, 42, 20, 50, 53, 36, 53, 21, 21, 5, 1, 48, 1, 51, 26, 8, 12, 6, 19, 22, 24, 39, 38, 48, 12, 50, 36, 8, 46, 0, 44, 22, 16, 15, 29, 34, 19, 42, 23, 1, 7, 17, 0, 49, 45, 38, 27, 2, 17, 13, 42, 24, 39, 54, 39, 23, 30, 35, 31, 32, 6, 18, 8, 47, 52, 44, 26, 46, 7, 4, 3, 11, 41, 20, 12, 30, 36, 6, 49, 21, 50, 39, 19, 41, 33, 28, 23, 14, 3, 25, 52, 45, 11, 21, 54, 32, 52, 13, 1, 51, 31, 44, 43, 46, 35, 7, 47, 18, 26, 29, 9, 17, 49, 42, 37, 1, 45, 4, 11, 15, 0, 22, 5, 7, 15, 0, 21, 13, 19, 11, 53, 42, 40, 25, 12, 37, 8, 48, 32, 6, 26, 46, 1, 44, 45, 47, 31, 12, 51, 43, 37, 15, 46, 33, 14, 42, 24, 28, 23, 35, 38, 27, 30, 39, 11, 44, 49, 22, 34, 36, 29, 34, 27, 53, 47, 34, 21, 31, 1, 13, 52, 24, 35, 40, 38, 50, 32, 45, 15, 9, 52, 39, 8, 0, 17, 43, 33, 12, 31, 26, 30, 18, 13, 37, 41, 10, 16, 46, 4, 3, 42, 30, 49, 46, 44, 3, 25, 45, 32, 32, 28, 15, 20, 3, 47, 6, 40, 34, 17, 31, 53, 19, 5, 0, 7, 49, 22, 51, 22, 8, 29, 54, 16, 37, 50, 41, 27, 43, 46, 44, 9, 11, 48, 24, 42, 15, 47, 1, 38, 12, 3, 2, 50, 31, 48, 30, 47, 32, 28, 51, 8, 2, 10, 4, 46, 39, 49, 25, 41, 38, 37, 21, 38, 35, 19, 20, 44, 13, 42, 11, 7, 33, 17, 43, 50, 45, 47, 14, 5, 1, 9, 51, 22, 18, 52, 6, 12, 0, 2, 3, 27, 5, 51, 7, 12, 35, 36, 16, 24, 18, 29, 32, 19, 44, 40, 49, 39, 10, 15, 39, 13, 3, 34, 23, 42, 8, 29, 11, 15, 6, 52, 43, 36, 1, 48, 14, 28, 38, 45, 33, 25, 50, 16, 14, 17, 6, 49, 38, 29, 19, 7, 50, 36, 54, 41, 52, 30, 22, 37, 10, 51, 25, 26, 2, 23, 4, 43, 33, 21, 51, 27, 35, 8, 7, 20, 41, 26, 42, 15, 3, 11, 43, 24, 31, 18, 23, 36, 0, 35, 48, 17, 53, 38, 37, 52, 31, 11, 18, 32, 31, 7, 28, 49, 12, 11, 30, 25, 30, 8, 27, 1, 5, 50, 41, 34, 6, 37, 20, 50, 25, 4, 5, 29, 0, 9, 13, 33, 23, 34, 11, 20, 5, 44, 0, 7, 47, 40, 1, 29, 16, 30, 47, 14, 22, 35, 17, 4, 10, 19, 48, 16, 39, 12, 20, 13, 52, 17, 33, 44, 9, 24, 9, 5, 28, 37, 53, 42, 2, 30, 17, 5, 40, 54, 25, 52, 23, 20, 43, 28, 10, 8, 32, 44, 11, 30, 16, 10, 24, 0, 26, 19, 31, 6, 27, 18, 51, 7, 47, 4, 17, 36, 19, 53, 2, 14, 50, 37, 15, 13, 15, 6, 31, 1, 36, 45, 48, 44, 51, 11, 21, 41, 26, 43, 43, 54, 22, 29, 26, 52, 40, 25, 16, 47, 14, 44, 33, 23, 37, 50, 42, 9, 8, 12, 36, 53, 4, 39, 40, 0, 48, 3, 54, 42, 28, 34, 31, 15, 39, 9, 52, 20, 27, 45, 8, 38, 53, 20, 52, 32, 2, 48, 19, 18, 36, 37, 5, 33, 25, 53, 7, 0, 29, 50, 46, 10, 6, 7, 48, 20, 15, 49, 42, 46, 25, 26, 16, 53, 9, 2, 31, 45, 52, 14, 44, 37, 53, 29, 27, 30, 32, 35, 43, 19, 45, 10, 36, 41, 54, 54, 21, 19, 0, 13, 40, 9, 29, 21, 1, 45, 9, 2, 48, 15, 43, 36, 29, 3, 19, 23, 16, 26, 54, 1, 49, 7, 10, 46, 51, 30, 25, 32, 12, 42, 41, 6, 28, 14, 39, 11, 31, 32, 25, 2, 23, 13, 14, 5, 47, 18, 20, 35, 15, 17, 45, 4, 20, 28, 38, 30, 27, 16, 49, 32, 2, 6, 54, 33, 29, 1, 51, 23, 24, 25, 50, 2, 18, 9, 26, 34, 42, 29, 44, 41, 54, 33, 1, 35, 10, 13, 6, 3, 11, 5, 0, 49, 39, 38, 25, 45, 2, 50, 28, 32, 9, 51, 21, 53, 26, 15, 45, 8, 3, 5, 14, 5, 27, 25, 28, 6, 39, 49, 2, 4, 10, 32, 24, 49, 36, 27, 41, 11, 12, 44, 47, 33, 17, 13, 19, 45, 34, 30, 15, 2, 36, 37, 8, 0, 52, 51, 46, 20, 38, 45, 25, 53, 17, 50, 28, 22, 20, 1, 54, 31, 7, 35, 33, 41, 10, 8, 43, 6, 12, 4, 12, 24, 5, 1, 17, 14, 6, 10, 48, 43, 23, 27, 3, 10, 8, 0, 53, 39, 38, 35, 26, 13, 31, 32, 34, 7, 41, 20, 22, 51, 41, 8, 52, 27, 7, 49, 29, 52, 24, 53, 50, 6, 13, 35, 12, 39, 38, 46, 28, 40, 16, 37, 22, 54, 19, 3, 10, 31, 4, 17, 22, 21, 45, 53, 36, 43, 34, 16, 8, 32, 23, 24, 4, 41, 2, 43, 52, 6, 50, 30, 48, 13, 24, 27, 39, 42, 54, 37, 9, 53, 11, 18, 48, 23, 7, 51, 22, 28, 47, 17, 6, 5, 19, 1, 24, 25, 42, 20, 44, 23, 0, 43, 30, 39, 40, 4, 41, 43, 33, 36, 15, 11, 40, 7, 16, 9, 54, 48, 26, 13, 38, 35, 45, 51, 22, 52, 12, 46, 31, 20, 19, 48, 28, 14, 22, 10, 16, 14, 23, 43, 3, 28, 52, 46, 12, 37, 24, 18, 2, 26, 38, 0, 19, 42, 9, 33, 47, 53, 30, 13, 31, 3, 25, 32, 48, 39, 10, 40, 4, 54, 36, 6, 21, 17, 45, 41, 25, 9, 7, 43, 46, 1, 27, 50, 35, 21, 40, 50, 47, 27, 18, 5, 23, 15, 25, 17, 39, 3, 46, 13, 33, 41, 31, 30, 34, 38, 29, 42, 44, 21, 37, 33, 44, 38, 31, 19, 17, 35, 42, 26, 15, 18, 42, 4, 39, 40, 24, 33, 3, 28, 41, 16, 46, 42, 15, 53, 2, 47, 30, 23, 12, 49, 38, 34, 24, 51, 18, 16, 1, 22, 54, 0, 9, 40, 17, 24, 26, 14, 36, 20, 0, 21, 48, 10, 27, 49, 38, 29, 25, 12, 34, 13, 47, 22, 19, 54, 2, 38, 41, 28, 49, 4, 3, 40, 26, 5, 9, 34, 23, 14, 0, 46, 36, 31, 48, 21, 34, 46, 33, 15, 40, 2, 8, 5, 11, 4, 52, 41, 32, 16, 28, 6, 47, 13, 14, 29, 9, 34, 28, 11, 48, 16, 34, 43, 12, 35, 18, 51, 30, 21, 24, 49, 12, 43, 10, 47, 23, 44, 24, 8, 0, 40, 4, 38, 28, 22, 32, 39, 8, 53, 16, 29, 43, 32, 36, 26, 13, 21, 23, 37, 17, 50, 0, 40, 45, 23, 3, 54, 37, 46, 32, 31, 24, 45, 51, 4, 40, 48, 49, 44, 22, 53, 33, 52, 9, 54, 20, 6, 26, 48, 47, 26, 7, 40, 17, 31, 11, 50, 20, 23, 39, 52, 36, 25, 33, 14, 24, 21, 29, 27, 27, 51, 5, 14, 54, 34, 35, 8, 46, 12, 4, 19, 35, 51, 50, 37, 14, 34, 7, 21, 23, 21, 4, 3, 2, 9, 7, 30, 27, 18, 3, 50, 2, 40, 35, 21, 10, 18, 1, 22, 11, 47, 5, 26, 18, 35, 30, 11, 13, 51, 1, 14, 53, 5, 22, 13, 33, 4, 1, 27, 36, 27, 20, 16, 34, 24, 40, 8, 52, 38, 5, 3, 25, 37, 20};
        //arr = new int[]{22, 20, 42, 23, 44, 31, 46, 21, 35, 7, 37, 33, 51, 25, 0, 50, 40, 39, 14, 3, 34, 29, 26, 12, 8, 5, 49, 38, 33, 53, 18, 36, 24, 30, 47, 50, 42, 4, 1, 10, 53, 37, 32, 11, 27, 17, 8, 30, 10, 2, 54, 9, 41, 45, 40, 45, 53, 15, 11, 1, 5, 46, 0, 9, 9, 34, 6, 2, 47, 38, 35, 51, 4, 41, 38, 36, 32, 48, 26, 52, 8, 27, 21, 33, 49, 11, 20, 54, 37, 25, 7, 44, 17, 42, 53, 29, 16, 30, 39, 19, 50, 46, 31, 18, 35, 28, 10, 23, 7, 38, 13, 12, 16, 34, 3, 5, 52, 29, 51, 42, 17, 27, 53, 19, 41, 54, 6, 20, 37, 27, 3, 10, 47, 1, 32, 2, 30, 44, 22, 49, 50, 24, 22, 8, 46, 0, 10, 35, 46, 39, 54, 45, 28, 31, 26, 1, 14, 18, 9, 40, 4, 49, 7, 31, 36, 38, 24, 43, 30, 54, 37, 14, 22, 22, 34, 44, 20, 12, 25, 47, 32, 45, 46, 41, 8, 50, 0, 29, 16, 23, 33, 29, 19, 13, 45, 2, 52, 33, 25, 48, 17, 9, 26, 17, 18, 31, 6, 5, 44, 11, 54, 39, 30, 53, 46, 9, 49, 35, 10, 37, 7, 22, 42, 1, 16, 5, 36, 18, 3, 29, 54, 21, 42, 20, 26, 50, 28, 43, 4, 6, 8, 19, 0, 41, 47, 33, 31, 40, 27, 23, 32, 10, 16, 11, 45, 52, 39, 37, 36, 38, 27, 6, 50, 39, 52, 21, 15, 0, 52, 54, 11, 13, 25, 7, 0, 28, 1, 50, 20, 24, 31, 45, 19, 30, 38, 3, 16, 18, 40, 10, 12, 46, 11, 14, 43, 23, 51, 33, 14, 26, 20, 3, 34, 53, 12, 17, 36, 22, 49, 26, 23, 52, 38, 15, 32, 12, 30, 33, 39, 14, 21, 24, 22, 45, 50, 13, 5, 51, 7, 16, 47, 52, 2, 35, 9, 19, 3, 34, 25, 53, 7, 0, 11, 28, 22, 44, 10, 43, 51, 4, 1, 36, 54, 46, 25, 34, 6, 18, 50, 22, 53, 34, 17, 2, 9, 54, 39, 13, 10, 42, 25, 5, 41, 11, 13, 48, 47, 1, 14, 16, 23, 21, 4, 15, 8, 31, 49, 35, 6, 27, 48, 47, 51, 33, 0, 20, 11, 24, 14, 38, 37, 32, 43, 36, 4, 0, 29, 30, 1, 9, 19, 13, 7, 42, 49, 44, 35, 42, 49, 51, 25, 33, 43, 14, 52, 6, 47, 1, 32, 46, 36, 24, 18, 53, 27, 35, 2, 21, 45, 34, 38, 12, 39, 37, 54, 8, 30, 41, 20, 10, 12, 21, 13, 2, 41, 38, 21, 18, 18, 54, 33, 15, 51, 32, 49, 47, 2, 22, 44, 51, 11, 27, 52, 31, 35, 5, 23, 43, 32, 5, 44, 29, 20, 40, 19, 24, 37, 54, 52, 8, 34, 25, 13, 14, 26, 10, 19, 37, 5, 30, 18, 54, 1, 40, 35, 33, 28, 30, 26, 41, 24, 23, 2, 20, 43, 34, 5, 15, 42, 39, 20, 49, 4, 52, 16, 25, 45, 3, 17, 31, 18, 46, 48, 11, 53, 14, 15, 13, 7, 37, 49, 36, 53, 8, 31, 33, 41, 32, 26, 10, 15, 2, 6, 16, 22, 0, 44, 37, 23, 12, 19, 28, 4, 7, 40, 17, 0, 27, 38, 46, 30, 7, 18, 43, 13, 8, 42, 47, 54, 18, 35, 5, 5, 40, 52, 24, 51, 54, 27, 2, 0, 53, 11, 45, 44, 49, 20, 22, 39, 13, 26, 38, 36, 9, 7, 47, 10, 23, 29, 41, 29, 19, 17, 32, 8, 16, 28, 31, 42, 18, 50, 46, 46, 17, 45, 32, 44, 19, 36, 53, 23, 39, 18, 19, 28, 16, 11, 50, 36, 4, 7, 15, 17, 6, 21, 48, 22, 12, 42, 3, 20, 25, 14, 24, 43, 26, 29, 14, 8, 27, 34, 13, 52, 0, 16, 1, 38, 7, 23, 47, 20, 30, 35, 37, 25, 31, 0, 24, 16, 51, 0, 54, 17, 2, 43, 19, 34, 48, 8, 5, 23, 33, 46, 13, 33, 27, 4, 3, 14, 21, 49, 29, 28, 22, 12, 21, 31, 4, 44, 8, 5, 27, 54, 43, 41, 26, 40, 39, 49, 3, 13, 18, 28, 45, 51, 47, 20, 11, 23, 14, 34, 32, 33, 17, 36, 6, 35, 13, 12, 43, 5, 1, 45, 18, 14, 5, 37, 9, 51, 41, 7, 21, 10, 29, 2, 33, 26, 48, 51, 19, 17, 43, 3, 44, 15, 53, 46, 24, 16, 12, 52, 38, 41, 1, 25, 29, 28, 27, 4, 32, 0, 2, 50, 29, 51, 15, 44, 22, 30, 1, 37, 48, 32, 53, 6, 25, 4, 35, 42, 8, 28, 3, 52, 10, 41, 11, 25, 8, 16, 34, 30, 23, 26, 9, 2, 40, 27, 9, 13, 2, 44, 23, 26, 33, 13, 26, 15, 12, 3, 0, 11, 6, 8, 20, 15, 17, 17, 21, 51, 19, 52, 9, 5, 54, 45, 36, 40, 50, 22, 29, 4, 39, 24, 30, 48, 18, 12, 28, 51, 18, 38, 8, 35, 35, 25, 9, 30, 44, 40, 10, 2, 43, 43, 21, 15, 4, 14, 1, 45, 47, 20, 48, 5, 6, 48, 37, 30, 0, 16, 52, 16, 6, 19, 54, 9, 30, 12, 11, 36, 31, 47, 14, 46, 35, 53, 52, 41, 1, 4, 39, 7, 41, 22, 27, 13, 26, 33, 38, 21, 49, 42, 45, 28, 50, 17, 47, 48, 43, 14, 5, 52, 6, 20, 48, 2, 11, 34, 50, 14, 32, 3, 8, 44, 45, 25, 10, 40, 42, 31, 22, 0, 38, 46, 43, 39, 23, 29, 8, 17, 3, 54, 49, 0, 31, 24, 52, 37, 40, 50, 36, 7, 33, 25, 10, 51, 37, 29, 11, 49, 33, 50, 34, 9, 28, 27, 19, 21, 31, 15, 15, 10, 45, 3, 31, 47, 40, 48, 39, 43, 37, 16, 9, 24, 1, 29, 31, 18, 25, 41, 32, 22, 53, 21, 33, 34, 52, 6, 28, 17, 6, 50, 40, 2, 3, 7, 46, 34, 21, 52, 53, 42, 0, 38, 43, 22, 22, 6, 31, 11, 16, 1, 53, 9, 47, 37, 48, 19, 27, 4, 1, 19, 26, 49, 43, 36, 4, 46, 14, 50, 45, 17, 51, 27, 0, 6, 2, 40, 28, 22, 44, 5, 12, 34, 53, 9, 24, 29, 23, 39, 4, 50, 50, 51, 32, 23, 5, 14, 12, 27, 14, 51, 6, 37, 19, 41, 47, 35, 6, 49, 52, 46, 36, 25, 44, 53, 1, 41, 39, 13, 15, 28, 33, 0, 18, 29, 23, 17, 35, 16, 42, 44, 34, 19, 9, 27, 10, 41, 1, 3, 4, 21, 30, 51, 25, 45, 28, 12, 53, 48, 26, 32, 18, 9, 24, 37, 29, 42, 21, 36, 35, 13, 7, 17, 5, 40, 23, 15, 8, 12, 29, 42, 16, 29, 10, 38, 46, 41, 49, 18, 41, 39, 21, 20, 2, 32, 48, 3, 36, 13, 24, 17, 34, 26, 40, 28, 17, 25, 9, 24, 39, 34, 48, 29, 19, 54, 5, 23, 21, 41, 11, 22, 15, 44, 7, 10, 49, 38, 33, 35, 24, 42, 31, 11, 32, 8, 46, 36, 19, 37, 52, 9, 28, 53, 38, 7, 36, 18, 35, 48, 29, 27, 43, 16, 33, 45, 35, 11, 49, 20, 36, 54, 13, 15, 42, 7, 46, 27, 43, 2, 44, 10, 39, 41, 40, 7, 51, 10, 23, 14, 50, 48, 31, 3, 12, 25, 9, 8, 52, 45, 33, 54, 22, 49, 38, 35, 49, 6, 38, 36, 13, 1, 14, 48, 34, 28, 31, 21, 39, 25, 48, 20, 47, 8, 54, 44, 3, 41, 27, 26, 13, 15, 30, 1, 23, 28, 6, 37, 17, 7, 37, 43, 14, 30, 24, 8, 48, 42, 7, 46, 26, 39, 50, 3, 11, 41, 12, 5, 40, 22, 4, 21, 54, 20, 35, 12, 47, 28, 30, 25, 44, 2, 18, 19, 6, 5, 34, 40, 47, 31, 43, 9, 26, 15, 20, 6, 0, 47, 40, 24, 16, 21, 42, 30, 12, 46, 32, 48, 47, 37, 36, 31, 20, 4, 16, 38, 45, 9, 34, 2, 28, 3, 0, 23, 24, 43, 32, 53, 45, 15, 48, 38, 1, 50, 34, 1, 39, 53, 45, 40, 15, 26, 20, 50, 16, 51, 4, 21, 31, 42, 40, 4, 13, 44, 54, 28, 36, 19, 24, 39, 26, 25, 27, 0, 12, 52, 6, 49, 4, 47, 14, 5, 8, 24, 53, 2, 3, 40, 12, 11, 30, 16, 29, 2, 15, 22, 35, 46, 39, 4, 38, 3, 24, 13, 12, 33, 15, 51, 51, 10, 47, 1, 48, 32, 17, 32, 20, 18, 10, 49, 27, 30, 23, 43, 19, 32, 11, 7, 26, 42, 50, 45, 15, 42, 3, 44};
        arr = new int[]{41, 50, 51, 16, 30, 23, 34, 31, 21, 11, 14, 42, 46, 41, 5, 19, 49, 38, 47, 49, 10, 26, 54, 20, 7, 1, 45, 16, 40, 29, 2, 25, 3, 9, 37, 8, 12, 4, 43, 44, 0, 39, 52, 35, 36, 12, 17, 24, 22, 53, 28, 32, 6, 18, 25, 33, 40, 18, 6, 53, 3, 47, 8, 48, 16, 45, 47, 52, 51, 44, 0, 3, 17, 35, 41, 1, 49, 15, 11, 46, 27, 43, 5, 37, 13, 17, 38, 14, 34, 50, 14, 2, 31, 54, 50, 26, 42, 54, 9, 30, 36, 48, 7, 12, 29, 32, 20, 34, 31, 41, 3, 16, 36, 38, 48, 17, 19, 52, 28, 21, 6, 12, 31, 13, 4, 42, 40, 14, 13, 3, 30, 7, 41, 35, 42, 2, 6, 0, 1, 38, 27, 39, 37, 5, 25, 29, 45, 54, 24, 51, 20, 49, 4, 11, 1, 15, 5, 23, 22, 1, 25, 32, 30, 22, 20, 33, 19, 6, 46, 41, 29, 32, 19, 28, 17, 50, 37, 14, 26, 8, 27, 28, 12, 31, 54, 53, 3, 4, 9, 52, 48, 39, 27, 42, 44, 49, 4, 2, 35, 18, 36, 7, 15, 11, 34, 13, 38, 53, 45, 40, 44, 26, 45, 43, 25, 54, 29, 2, 21, 23, 14, 30, 13, 3, 52, 34, 32, 27, 19, 21, 36, 20, 37, 35, 33, 38, 42, 2, 24, 30, 28, 6, 20, 51, 9, 22, 8, 33, 49, 37, 15, 41, 39, 8, 45, 40, 47, 24, 48, 12, 28, 46, 54, 52, 25, 0, 15, 28, 13, 27, 52, 16, 42, 48, 36, 26, 20, 22, 51, 35, 49, 11, 45, 1, 8, 37, 15, 17, 24, 37, 32, 2, 31, 20, 53, 44, 50, 0, 4, 10, 33, 19, 40, 3, 12, 23, 7, 18, 34, 5, 44, 51, 7, 32, 28, 48, 0, 25, 15, 47, 30, 21, 16, 25, 35, 2, 38, 46, 8, 31, 17, 45, 36, 10, 22, 15, 14, 53, 43, 40, 15, 1, 23, 37, 34, 6, 30, 44, 38, 54, 3, 17, 12, 4, 18, 1, 11, 40, 26, 14, 54, 17, 42, 29, 49, 44, 48, 25, 46, 41, 12, 49, 6, 16, 21, 47, 23, 27, 38, 2, 9, 32, 2, 33, 43, 22, 4, 48, 1, 20, 5, 7, 11, 10, 21, 52, 50, 15, 4, 33, 35, 51, 28, 13, 34, 25, 45, 50, 15, 49, 8, 17, 14, 10, 40, 54, 48, 8, 27, 37, 23, 12, 5, 48, 7, 26, 13, 7, 30, 16, 27, 52, 1, 12, 31, 21, 9, 39, 35, 32, 21, 50, 1, 42, 44, 51, 20, 43, 24, 5, 23, 0, 25, 18, 43, 45, 2, 30, 28, 54, 27, 39, 26, 10, 15, 6, 32, 31, 14, 36, 43, 27, 48, 7, 22, 31, 33, 44, 9, 12, 17, 19, 11, 53, 15, 17, 32, 24, 37, 16, 21, 46, 36, 50, 38, 38, 31, 23, 18, 51, 4, 9, 53, 27, 12, 1, 43, 41, 19, 6, 15, 49, 37, 50, 31, 24, 39, 32, 40, 46, 28, 42, 21, 46, 21, 30, 48, 54, 36, 18, 3, 33, 45, 13, 26, 10, 12, 5, 22, 1, 19, 0, 36, 26, 44, 2, 6, 46, 7, 12, 22, 43, 16, 29, 32, 3, 47, 23, 34, 33, 43, 39, 11, 51, 36, 28, 27, 37, 21, 46, 24, 1, 14, 52, 51, 19, 10, 50, 3, 40, 31, 18, 8, 36, 9, 45, 32, 5, 29, 33, 16, 41, 11, 47, 34, 53, 39, 10, 31, 17, 28, 49, 48, 37, 26, 4, 20, 35, 52, 50, 14, 41, 19, 21, 3, 28, 34, 43, 16, 11, 22, 12, 42, 54, 1, 0, 27, 34, 40, 35, 23, 54, 28, 12, 33, 48, 30, 5, 31, 8, 16, 5, 9, 22, 47, 2, 10, 19, 32, 24, 26, 20, 11, 44, 6, 17, 45, 3, 51, 7, 39, 13, 4, 36, 51, 40, 1, 21, 35, 43, 7, 2, 36, 3, 8, 29, 37, 19, 54, 18, 7, 44, 50, 19, 26, 30, 16, 9, 13, 10, 51, 22, 42, 47, 53, 12, 48, 17, 11, 20, 5, 47, 27, 38, 52, 50, 32, 54, 25, 26, 45, 28, 51, 7, 19, 24, 16, 38, 32, 40, 4, 19, 44, 27, 24, 2, 35, 48, 47, 8, 29, 53, 13, 33, 46, 6, 18, 7, 0, 10, 34, 41, 37, 39, 36, 11, 33, 47, 39, 4, 42, 41, 39, 45, 14, 36, 48, 26, 9, 23, 21, 29, 46, 44, 24, 17, 35, 43, 48, 49, 18, 0, 28, 31, 16, 50, 9, 34, 53, 40, 20, 8, 18, 9, 24, 19, 7, 27, 16, 20, 51, 4, 26, 4, 47, 43, 44, 11, 12, 42, 5, 25, 30, 40, 50, 45, 35, 52, 43, 10, 14, 8, 1, 31, 41, 29, 53, 0, 37, 13, 36, 19, 15, 20, 40, 47, 2, 34, 8, 21, 48, 41, 9, 22, 4, 25, 27, 16, 23, 3, 43, 52, 44, 1, 38, 49, 17, 10, 7, 39, 14, 21, 4, 46, 46, 1, 23, 46, 52, 14, 9, 49, 5, 43, 22, 25, 45, 30, 51, 5, 49, 39, 37, 20, 26, 32, 33, 9, 38, 6, 2, 3, 42, 18, 11, 31, 16, 50, 8, 42, 38, 9, 28, 3, 43, 8, 22, 29, 39, 10, 40, 0, 45, 44, 10, 41, 21, 7, 2, 5, 47, 19, 53, 25, 34, 39, 35, 6, 37, 27, 23, 32, 14, 45, 50, 45, 23, 20, 30, 53, 49, 12, 27, 41, 51, 48, 47, 34, 53, 46, 10, 33, 52, 2, 15, 11, 40, 22, 11, 6, 13, 43, 3, 0, 35, 32, 47, 16, 14, 1, 48, 29, 22, 25, 0, 49, 33, 20, 6, 11, 18, 18, 2, 19, 11, 39, 19, 13, 0, 51, 30, 37, 21, 24, 15, 33, 10, 23, 53, 0, 38, 44, 48, 25, 8, 4, 29, 51, 46, 21, 13, 11, 6, 41, 34, 33, 43, 29, 31, 40, 35, 24, 53, 30, 15, 10, 38, 1, 2, 7, 53, 31, 25, 4, 14, 1, 24, 42, 52, 46, 5, 23, 38, 39, 28, 0, 7, 28, 32, 11, 50, 28, 36, 45, 6, 52, 17, 13, 4, 3, 52, 9, 37, 13, 44, 10, 41, 14, 54, 4, 0, 47, 33, 18, 39, 10, 20, 14, 26, 48, 4, 12, 30, 32, 17, 29, 34, 42, 11, 42, 4, 47, 21, 13, 38, 50, 50, 33, 20, 39, 3, 23, 9, 27, 0, 22, 2, 6, 36, 28, 9, 36, 19, 45, 13, 52, 24, 6, 20, 40, 47, 14, 23, 2, 50, 15, 10, 25, 54, 34, 46, 21, 1, 30, 53, 13, 44, 4, 42, 29, 18, 36, 17, 24, 44, 33, 11, 45, 24, 16, 40, 38, 18, 39, 21, 25, 35, 47, 51, 5, 43, 2, 40, 0, 15, 23, 31, 52, 30, 22, 41, 18, 9, 16, 28, 39, 11, 22, 26, 15, 2, 36, 53, 7, 19, 32, 5, 0, 52, 21, 6, 49, 23, 44, 17, 46, 3, 25, 5, 24, 50, 54, 51, 35, 18, 15, 37, 9, 6, 12, 23, 42, 20, 19, 39, 16, 34, 48, 14, 26, 52, 23, 26, 31, 15, 38, 0, 1, 32, 54, 33, 34, 33, 19, 39, 22, 14, 24, 46, 51, 49, 30, 34, 46, 18, 35, 6, 47, 3, 12, 37, 43, 5, 16, 28, 17, 23, 31, 24, 8, 27, 48, 13, 36, 47, 53, 21, 12, 26, 11, 13, 20, 5, 46, 8, 9, 54, 41, 49, 29, 42, 37, 8, 10, 15, 24, 53, 42, 29, 41, 44, 35, 7, 34, 52, 39, 27, 50, 14, 26, 31, 25, 30, 10, 8, 7, 15, 40, 29, 9, 36, 8, 6, 22, 19, 10, 38, 16, 48, 45, 35, 42, 41, 54, 29, 24, 30, 13, 43, 6, 26, 29, 17, 34, 49, 42, 35, 36, 3, 0, 46, 28, 0, 27, 30, 3, 51, 23, 13, 5, 53, 32, 43, 47, 38, 31, 52, 11, 14, 47, 10, 50, 52, 29, 45, 4, 54, 31, 53, 27, 22, 17, 16, 5, 25, 53, 34, 13, 49, 25, 42, 45, 4, 32, 36, 54, 6, 24, 12, 20, 49, 41, 27, 37, 17, 8, 40, 33, 29, 7, 19, 54, 15, 52, 49, 23, 13, 41, 18, 42, 12, 5, 44, 18, 46, 24, 9, 12, 38, 40, 1, 18, 28, 46, 21, 30, 17, 26, 29, 54, 10, 16, 20, 15, 51, 22, 41, 2, 25, 3, 31, 50, 28, 44, 8, 20, 32, 26, 39, 45, 7, 1, 26, 30, 0, 44, 38, 22, 50, 47, 51, 38, 29, 22, 25, 18, 37, 35, 2, 9, 14, 5, 8, 7, 33, 34, 27, 37, 3, 41, 18, 53, 0, 40, 17, 43, 49, 51, 35, 35, 54, 33, 43, 49, 1};
        test = checkSequenceParcial(57, arr);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }
        printArray(arr);

    }

    private boolean checkSequenceParcial(int k, int[] arr) {
        int[] arr2 = new int[arr.length + k - 2];
        int i = 0;
        for (; i < k - 2; i++) {
            arr2[i] = i;
        }
        for (; i < arr2.length; i++) {
            arr2[i] = arr[i - (k - 2)];
        }
        return checkSequence(k, arr2);
    }

    private boolean checkSequence(int k, int[] arr) {
        boolean ret = true;
        int len = arr.length;
        int ko = k - 2;
        int maxcount = len / ko;
        int[] arrup = new int[len];
        int[] arrdown = new int[len];
        int[] countval = new int[ko];
        int offsetup = ko - 1;
        int up = 0;
        int down = 1;

        for (int i = 0; i < len; i++) {
            arrup[i] = up;
            arrdown[i] = down++;
            if (i == offsetup) {
                up++;
                offsetup += (ko - up);
            }
            if (down == ko + 1) {
                down = up + 1;
            }
            if (i < ko) {
                countval[i] = 0;
            }
        }
        for (int i = 0; i < len && ret; i++) {
            int val = arr[i];
            boolean max = countval[val] <= maxcount;
            ret = ret && max;
            up = arrup[i];
            down = arrdown[i];
            for (int j = 0; j < i && ret; j++) {
                if (arrdown[j] == up || arrdown[j] == down || arrup[j] == up) {
                    ret = ret && arr[j] != val;
                }
            }
            if (!ret) {
                if (!max) {
                    System.out.println("Max count exceded: " + val + " " + countval[val]);
                }
                System.out.println("Failed in position: " + i + "/" + len);
            }
            countval[val]++;
        }
        return ret;
    }

    public Map<Integer, List<Integer>> mapInvalidPositions(int k) {
//        int k = 57;
        int ko = k - 2;
        int len = ((ko + 1) * ko) / 2;
        Map<Integer, List<Integer>> excludeMapList = new HashMap<>();
        boolean verbose = false;

        int[] arrup = new int[len];
        int[] arrdown = new int[len];
        int offsetup = ko - 1;
        int up = 0;
        int down = 1;

        for (int i = 0; i < len; i++) {
            arrup[i] = up;
            arrdown[i] = down++;
            if (i == offsetup) {
                up++;
                offsetup += (ko - up);
            }
            if (down == ko + 1) {
                down = up + 1;
            }
        }
        if (verbose) {
            System.out.println("Seq: ");
        }
        for (int i = 0; i < len; i++) {
            up = arrup[i];
            down = arrdown[i];
            int count = 0;
            int countko = 0;
            StringBuilder sb = new StringBuilder();
            if (verbose) {
                sb.append(String.format("%4d ", i));
                sb.append("|%4d|:");
            }
            List<Integer> listExclude = new ArrayList<>();
            excludeMapList.put(i, listExclude);
            for (int j = 0; j < len; j++) {
                if (i != j && (arrdown[j] == up || arrdown[j] == down || arrup[j] == up)) {
                    if (verbose) {
                        sb.append(String.format("%4d ", j));
                    }
                    listExclude.add(j);
                    count++;
                    if (j < ko) {
                        countko++;
                    }
                }
            }
            if (countko >= ko) {
                throw new IllegalStateException("Impossible graph");
            }
            if (verbose) {
                System.out.printf(sb.toString(), count);
                System.out.println();
            }
        }
        return excludeMapList;
    }

    private void printArray(int[] arr) {
        int len = arr.length;
        System.out.print("[");
        for (int i = 0; i < len; i++) {
            System.out.print(arr[i]);
            if (i < len - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

    private int[] getCombincaoInterna2(int k) {
        int ko = k - 2;
        boolean verbose = false;
        Map<Integer, List<Integer>> mapExcludePosition = mapInvalidPositions(k);
        int len = ((ko + 1) * ko) / 2;
        int arr[] = new int[len];
        int arrup[] = new int[len];
        int arrdown[] = new int[len];
        int[] countpos = new int[len];
        int[] countval = new int[ko];

        int max_val_count = 0;
        if (ko != 0) {
            max_val_count = len / ko;
        }

        Map<Integer, List<Integer>> possibilidades = new HashMap<>(len);
        Integer[] targetv = new Integer[ko];

        for (int j = 0; j < ko; j++) {
            targetv[j] = j;
            countval[j]++;
        }

        int offsetup = ko - 1;
        int up = 0;
        int down = 1;
        for (int i = 0; i < len; i++) {
            arr[i] = -1;
            arrup[i] = up;
            arrdown[i] = down++;
            if (i == offsetup) {
                up++;
                offsetup += (ko - up);
            }
            if (down == ko + 1) {
                down = up + 1;
            }
        }

        if (verbose) {
            System.out.println("\nUp:");
            printArray(arrup);
            System.out.println("\nDown:");
            printArray(arrdown);
        }

        for (int i = 0; i < ko; i++) {
            countpos[i] = 0;
            arr[i] = i;
            List<Integer> listaPossiveis = new ArrayList<>(len);
            listaPossiveis.addAll(Arrays.asList(targetv));
            possibilidades.put(i, listaPossiveis);
        }

        for (int i = ko; i < len; i++) {
            List<Integer> listaPossiveis = new ArrayList<>(len);
            listaPossiveis.addAll(Arrays.asList(targetv));
            possibilidades.put(i, listaPossiveis);
        }

        int pos = ko;

        while (pos < len && pos >= ko) {
            List<Integer> list = possibilidades.get(pos);
            if (countpos[pos] >= ko) {
                if (verbose) {
                    System.out.println("arr");
                    printArray(arr);
                    System.err.print("deadlock: empty-list in: " + pos);
                }
                for (int i = pos; i < len; i++) {
                    countpos[i] = 0;
                    int val = arr[i];
                    if (val >= 0) {
                        countval[val]--;
                        arr[i] = -1;
                    }
                }
                pos--;
                countval[arr[pos]]--;
                if (verbose) {
                    System.err.println(" rollback to: " + pos);
                }
                continue;
            }
            int lsize = list.size();
            int val = -1;
            boolean skip = true;
            boolean excluded = true;
            boolean overflow = true;
            while (skip && countpos[pos] < lsize) {
                val = list.get(countpos[pos]++);
                overflow = countval[val] >= max_val_count;
                excluded = exclude(arrup, arrdown, arr, pos, val);
                skip = overflow || excluded;
            }
            if (!skip) {
                arr[pos] = val;
                countval[val]++;
                pos++;
            }
        }
        if (verbose) {
            System.out.println("\nCombinação:");
            printArray(arr);
        }
        if (pos < len) {
            throw new IllegalStateException("Combination impossible");
        }
        return arr;
    }

    private int[] getCombincaoInterna(int k) {
        boolean verbose = false;
        int ko = k - 2;
        int len = ((ko + 1) * ko) / 2;
        int arr[] = new int[len];
        int arrup[] = new int[len];
        int arrdown[] = new int[len];
        int[] countpos = new int[len];
        int[] countval = new int[ko];

        int max_val_count = 0;
        if (ko != 0) {
            max_val_count = len / ko;
        }

        Map<Integer, List<Integer>> possibilidades = new HashMap<>(len);
        Integer[] targetv = new Integer[ko];

        for (int j = 0; j < ko; j++) {
            targetv[j] = j;
            countval[j]++;
        }

        int offsetup = ko - 1;
        int up = 0;
        int down = 1;
        for (int i = 0; i < len; i++) {
            arr[i] = -1;
            arrup[i] = up;
            arrdown[i] = down++;
            if (i == offsetup) {
                up++;
                offsetup += (ko - up);
            }
            if (down == ko + 1) {
                down = up + 1;
            }
        }

        if (verbose) {
            System.out.println("\nUp:");
            printArray(arrup);
            System.out.println("\nDown:");
            printArray(arrdown);
        }

        for (int i = 0; i < ko; i++) {
            countpos[i] = 0;
            arr[i] = i;
            List<Integer> listaPossiveis = new ArrayList<>(len);
            listaPossiveis.addAll(Arrays.asList(targetv));
            possibilidades.put(i, listaPossiveis);
        }

        for (int i = ko; i < len; i++) {
            List<Integer> listaPossiveis = new ArrayList<>(len);
            listaPossiveis.addAll(Arrays.asList(targetv));
            possibilidades.put(i, listaPossiveis);
        }

        int pos = ko;

        while (pos < len && pos >= ko) {
            List<Integer> list = possibilidades.get(pos);
            if (countpos[pos] >= ko) {
                if (verbose) {
                    System.out.println("arr");
                    printArray(arr);
                    System.err.print("deadlock: empty-list in: " + pos);
                }
                for (int i = pos; i < len; i++) {
                    countpos[i] = 0;
                    int val = arr[i];
                    if (val >= 0) {
                        countval[val]--;
                        arr[i] = -1;
                    }
                }
                pos--;
                countval[arr[pos]]--;
                if (verbose) {
                    System.err.println(" rollback to: " + pos);
                }
                continue;
            }
            int lsize = list.size();
            int val = -1;
            boolean skip = true;
            boolean excluded = true;
            boolean overflow = true;
            while (skip && countpos[pos] < lsize) {
                val = list.get(countpos[pos]++);
                overflow = countval[val] >= max_val_count;
                excluded = exclude(arrup, arrdown, arr, pos, val);
                skip = overflow || excluded;
            }
            if (!skip) {
                arr[pos] = val;
                countval[val]++;
                pos++;
            }
        }
        if (verbose) {
            System.out.println("\nCombinação:");
            printArray(arr);
        }
        if (pos < len) {
            throw new IllegalStateException("Combination impossible");
        }
        return arr;
    }

}

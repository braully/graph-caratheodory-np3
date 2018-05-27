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
        int ko = k - 2;
        int len = ((ko + 1) * ko) / 2;
        int arr[] = new int[len];
        int arrup[] = new int[len];
        int arrdown[] = new int[len];
        int[] countpos = new int[len];
        int[] countval = new int[ko];
        int max_val_count = len / ko;

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

        System.out.println("\nUp:");
        printArray(arrup);
        System.out.println("\nDown:");
        printArray(arrdown);

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

        while (pos < len) {
            List<Integer> list = possibilidades.get(pos);
            if (countpos[pos] > list.size()) {
                //rollback
                break;
            }
            int lsize = list.size();
            int idx = countpos[pos];
            int val = list.get(idx);
            while ((countval[val] >= max_val_count || exclude(arrup, arrdown, arr, pos, val)) && idx++ <= lsize * 2) {
                val = list.get(idx++ % lsize);
            }
            if (countval[val] >= max_val_count) {
                //rollback
                break;
            }
            arr[pos] = val;
            countval[val]++;
            countpos[pos]++;
            pos++;
        }

        System.out.println("\nCombinação:");
        printArray(arr);
    }

    private boolean exclude(int[] arrup, int[] arrdown, int[] arr, int pos, int val) {
        return false;
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
}

//        int pos = ko;
//
//        int i;
//        for (i = len - 1; i > 0 && arr[i] == ko + i; --i);
//        ++arr[i];
//        for (int j = i; j < len - 1; ++j) {
//            arr[j + 1] = arr[j] + 1;
//        }
//        while (pos <= len) {
//            arr[pos] = 0;
//            pos++;
//        }

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
    }

    public void testSkelGraphMoore3() {
        System.out.println();
        System.out.println("Estrategia-3");
        int k = 7;

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

    private int[] getCombincaoInterna(int k) {
        boolean verbose = false;
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

package com.github.braully.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import static tmp.CombMooreGraph.mapInvalidPositions;
import tmp.UtilTmp;

/**
 *
 * @author strike
 */
public class GraphCombTest extends TestCase {

    private static boolean verbose = true;

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
            excludeMapList.put(i, new ArrayList<>());
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
            List<Integer> listExclude = excludeMapList.get(i);
            for (int j = 0; j < len; j++) {
                if (i != j && (arrdown[j] == up || arrdown[j] == down || arrup[j] == up)) {
                    if (verbose) {
                        sb.append(String.format("%4d ", j));
                    }
                    listExclude.add(j);
                    List<Integer> list2 = excludeMapList.get(j);
                    if (!list2.contains(i)) {
                        list2.add(i);
                    }
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

        for (int i = 0; i < len; i++) {
            Collections.sort(excludeMapList.get(i));
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

    public void testIncCombinacao() {
        if (true) {
            return;
        }
        int k = 57;
        int[] initial = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 50, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 52, 51, 54, 53, 0, 4, 2, 3, 7, 5, 6, 10, 8, 9, 13, 11, 12, 16, 14, 15, 19, 17, 18, 22, 20, 21, 25, 23, 24, 28, 26, 27, 31, 29, 30, 34, 32, 33, 37, 35, 36, 40, 38, 39, 43, 41, 42, 46, 44, 45, 49, 47, 48, 53, 54, 51, 52, 5, 6, 7, 3, 4, 9, 11, 12, 8, 10, 14, 15, 13, 17, 18, 16, 20, 21, 19, 23, 24, 22, 26, 27, 25, 29, 30, 28, 32, 33, 31, 35, 36, 34, 38, 39, 37, 41, 42, 40, 44, 45, 43, 47, 48, 46, 50, 51, 54, 53, 52, 49, 0, 1, 8, 9, 10, 6, 7, 12, 14, 15, 11, 17, 13, 19, 20, 16, 22, 18, 24, 25, 21, 27, 23, 29, 30, 26, 32, 28, 34, 35, 31, 37, 33, 39, 40, 36, 42, 38, 44, 45, 41, 47, 43, 49, 52, 53, 54, 46, 48, 50, 51, 8, 1, 10, 5, 7, 11, 13, 9, 16, 17, 12, 18, 14, 15, 21, 23, 24, 19, 20, 26, 22, 28, 30, 25, 31, 27, 33, 29, 36, 37, 32, 38, 34, 35, 41, 43, 44, 39, 40, 46, 42, 48, 51, 53, 54, 52, 45, 47, 49, 50, 0, 2, 11, 12, 6, 14, 15, 9, 10, 18, 19, 13, 21, 22, 16, 17, 25, 26, 20, 28, 29, 23, 24, 32, 33, 27, 35, 30, 36, 31, 39, 40, 34, 42, 37, 43, 38, 46, 47, 41, 49, 52, 54, 51, 53, 44, 50, 45, 48, 11, 2, 4, 13, 15, 16, 10, 9, 19, 12, 20, 14, 23, 17, 25, 18, 27, 28, 21, 22, 24, 31, 33, 26, 34, 36, 29, 30, 38, 32, 41, 42, 35, 44, 37, 45, 39, 40, 48, 53, 54, 51, 52, 43, 50, 49, 46, 47, 0, 1, 3, 16, 8, 17, 18, 20, 21, 12, 13, 14, 15, 23, 26, 19, 27, 29, 30, 22, 32, 24, 25, 35, 28, 37, 38, 39, 31, 33, 41, 34, 36, 45, 46, 47, 51, 52, 54, 53, 50, 40, 42, 49, 43, 48, 44, 3, 1, 4, 17, 18, 16, 21, 20, 22, 12, 13, 14, 15, 27, 28, 19, 30, 31, 32, 23, 25, 24, 26, 37, 38, 29, 40, 41, 42, 33, 43, 34, 35, 49, 51, 53, 54, 52, 50, 36, 39, 44, 48, 45, 47, 46, 0, 2, 5, 19, 20, 22, 23, 21, 24, 15, 13, 14, 16, 17, 18, 31, 32, 33, 34, 35, 36, 25, 26, 27, 28, 29, 30, 43, 44, 45, 48, 49, 50, 52, 54, 53, 51, 37, 38, 41, 39, 47, 46, 40, 42, 5, 2, 4, 19, 23, 22, 24, 25, 26, 27, 16, 14, 15, 17, 18, 20, 21, 33, 34, 35, 36, 38, 28, 39, 30, 29, 31, 32, 44, 49, 48, 53, 54, 52, 51, 50, 40, 37, 46, 47, 41, 42, 43, 45, 0, 1, 3, 6, 7, 23, 22, 24, 25, 26, 17, 18, 29, 19, 21, 20, 35, 36, 34, 37, 27, 39, 40, 28, 42, 30, 47, 48, 51, 52, 54, 53, 49, 50, 31, 32, 33, 45, 46, 38, 41, 44, 43, 3, 1, 4, 6, 7, 23, 25, 24, 27, 28, 29, 30, 20, 18, 19, 21, 22, 37, 38, 39, 26, 41, 42, 40, 46, 51, 47, 53, 54, 52, 48, 50, 49, 32, 31, 34, 33, 45, 43, 44, 35, 36, 0, 2, 5, 6, 7, 8, 26, 28, 29, 30, 31, 32, 33, 34, 20, 21, 22, 23, 24, 25, 27, 41, 46, 47, 49, 52, 54, 53, 51, 50, 48, 35, 36, 43, 45, 44, 37, 39, 38, 42, 40, 5, 2, 4, 6, 7, 8, 29, 30, 31, 32, 33, 34, 35, 22, 23, 21, 24, 25, 40, 26, 48, 54, 52, 45, 46, 50, 51, 47, 49, 27, 28, 37, 36, 44, 43, 38, 42, 39};
//        int k = 7;
        int ko = k - 2;
//        boolean roolback = false;
        int len = ((ko + 1) * ko) / 2;
        LinkedList<Integer> arr = new LinkedList<>();
        Map<Integer, Integer> countval = new HashMap<>();
        Map<Integer, List<Integer>> mapExcludePosition = mapInvalidPositions(k);

        int maxValCount = 0;
        if (ko != 0) {
            maxValCount = len / ko;
        }

        Map<Integer, List<Integer>> possibilidades = new HashMap<>(len);
        Integer[] targetv = new Integer[ko];

        for (int j = 0; j < ko; j++) {
            targetv[j] = j;
            countval.put(j, 0);
        }
        List<Integer> targetvList = Arrays.asList(targetv);

        for (int i = 0; i < len; i++) {
            possibilidades.put(i, new LinkedList<>(targetvList));
        }

        if (initial.length > 0) {
            for (int i = 0; i < initial.length; i++) {
                int val = initial[i];
                arr.add(val);
                List<Integer> posicoesExcluidas = mapExcludePosition.get(i);
                clearEmptyCombination(i, val, countval, maxValCount, possibilidades, posicoesExcluidas);
            }
        } else {
            for (int i = 0; i < ko; i++) {
                arr.add(i);
                List<Integer> posicoesExcluidas = mapExcludePosition.get(i);
                clearEmptyCombination(i, i, countval, maxValCount, possibilidades, posicoesExcluidas);
            }
        }

        int longest = 0;

        while (arr.size() < len && arr.size() >= ko) {
            int pos = arr.size();
            LinkedList<Integer> list = (LinkedList<Integer>) possibilidades.get(pos);
            List<Integer> posicoesExcluidas = mapExcludePosition.get(pos);
            Integer val = null;
            if (!list.isEmpty()) {
                while ((val = list.poll()) != null && countval.get(val) < maxValCount) {

                }
            } else {

            }
            if (val != null && countval.get(val) < maxValCount) {
                arr.add(val);
                countval.put(val, countval.get(val) + 1);
            } else {
                arr.add(null);
            }
            if (val != null) {
                clearEmptyCombination(pos, val, countval, maxValCount, possibilidades, posicoesExcluidas);
            }

//            if (pos >= longest) {
//                longest = pos;
//                System.out.print("arr[");
//                System.out.print(longest);
//                System.out.print("]: ");
//                System.out.print(arr);
//                System.out.println();
//            }
        }

        for (int j = 0; j < ko; j++) {
            countval.put(j, 0);
        }

        Integer[] toArray = arr.toArray(new Integer[0]);

        for (int i = 0; i < toArray.length; i++) {
            if (toArray[i] != null) {
                countval.put(toArray[i], countval.get(toArray[i]) + 1);
            }
        }

        for (int i = 0; i < toArray.length; i++) {
            if (toArray[i] == null) {
                for (int j = 0; j < ko; j++) {
                    int cont = countval.get(j);
                    if (cont < maxValCount) {
                        toArray[i] = j;
                        countval.put(j, cont + 1);
                        break;
                    }
                }
            }
        }

        System.out.print("\nCount-vals m=" + maxValCount + ": ");
        System.out.println(countval);

        if (arr.size() < len) {
            throw new IllegalStateException("Combination impossible");
        }
        System.out.print("\nCombinação[" + toArray.length + "]: ");
        UtilTmp.printArray(toArray);

        boolean test = checkSequence(k, toArray);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }
        assertTrue("Combination invalid", test);
    }

    public void testEstrategiaCombinacao() {
        if (true) {
            return;
        }
//        int k = 57;
        int k = 7;
        int ko = k - 2;
        boolean roolback = false;
        int len = ((ko + 1) * ko) / 2;
        LinkedList<Integer> arr = new LinkedList<>();
        Map<Integer, Integer> countval = new HashMap<>();
        Map<Integer, List<Integer>> mapExcludePosition = mapInvalidPositions(k);

        int maxValCount = 0;
        if (ko != 0) {
            maxValCount = len / ko;
        }

        Map<Integer, List<Integer>> possibilidades = new HashMap<>(len);
        Integer[] targetv = new Integer[ko];

        for (int j = 0; j < ko; j++) {
            targetv[j] = j;
            countval.put(j, 0);
        }
        List<Integer> targetvList = Arrays.asList(targetv);

        for (int i = 0; i < len; i++) {
            possibilidades.put(i, new LinkedList<>(targetvList));
        }

        for (int i = 0; i < ko; i++) {
            arr.add(i);
            List<Integer> posicoesExcluidas = mapExcludePosition.get(i);
            clearEmptyCombination(i, i, countval, maxValCount, possibilidades, posicoesExcluidas);
        }

        int longest = 0;

        while (arr.size() < len && arr.size() >= ko) {
            int pos = arr.size();
            LinkedList<Integer> list = (LinkedList<Integer>) possibilidades.get(pos);
            List<Integer> posicoesExcluidas = mapExcludePosition.get(pos);
            if (roolback || list.isEmpty()) {
                Integer valRollback = arr.pollLast();
                /*  rollback */
                //rest-countval
                for (int j = 0; j < ko; j++) {
                    countval.put(j, 0);
                }
                //reset-possibilidades
                for (int j = pos; j < len; j++) {
                    List<Integer> lis = possibilidades.get(j);
                    lis.clear();
                    lis.addAll(targetvList);
                }
                for (int i = 0; i < arr.size(); i++) {
                    posicoesExcluidas = mapExcludePosition.get(i);
                    int post = i;
                    int valt = arr.get(i);
                    //roolback está vindo true... Verificar isso
                    clearEmptyCombination(post, valt, countval, maxValCount, possibilidades, posicoesExcluidas, false);
                }
                possibilidades.get(arr.size()).remove(valRollback);
                roolback = false;
                continue;
            }
            Integer val = list.poll();
            arr.add(val);
            roolback = clearEmptyCombination(pos, val, countval, maxValCount, possibilidades, posicoesExcluidas);
            if (pos >= longest) {
                longest = pos;
                System.out.print("arr[");
                System.out.print(longest);
                System.out.print("]: ");
                System.out.print(arr);
                System.out.println();
            }
        }

        if (arr.size() < len) {
            throw new IllegalStateException("Combination impossible");
        }
        System.out.print("\nCombinação:");
        System.out.println(arr);

        boolean test = checkSequence(k, arr.toArray(new Integer[0]));
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }
        assertTrue("Combination invalid", test);
    }

    public void testCheckSequence() {
        if (true) {
            return;
        }
        System.out.println("check big sequence");
        int k = 57;
        int ko = k - 2;
        Integer[] seq = new Integer[]{30, 1, 2, 54, 4, 36, 18, 50, 8, 43, 29, 11, 54, 44, 14, 27, 43, 28, 49, 41, 27, 37, 46, 23, 50, 36, 38, 27, 50, 29, 52, 49, 21, 50, 34, 50, 47, 42, 30, 54, 40, 41, 48, 31, 44, 3,
            32, 41, 17, 49, 50, 51, 52, 23, 54, 25, 15, 37, 52, 11, 30, 1, 24, 12, 22, 43, 14, 39, 16, 26, 23, 2, 49, 15, 2, 6, 17, 19, 16, 38, 1, 51, 8, 16, 37, 51, 21, 43, 37, 29, 5, 46, 21, 18, 51, 17, 12, 1, 40, 10,
            13, 26, 4, 25, 36, 31, 7, 22, 46, 49, 22, 23, 48, 22, 54, 33, 0, 50, 17, 8, 26, 11, 7, 33, 39, 0, 36, 18, 37, 3, 14, 7, 32, 36, 23, 31, 49, 22, 20, 26, 19, 52, 18, 46, 9, 23, 18, 52, 12, 39, 3, 10, 7, 21, 12,
            17, 12, 36, 31, 37, 5, 34, 7, 33, 12, 19, 33, 46, 35, 51, 44, 32, 26, 6, 46, 25, 36, 7, 3, 34, 17, 7, 54, 13, 15, 45, 47, 39, 37, 12, 52, 6, 44, 19, 51, 31, 47, 13, 46, 37, 15, 13, 11, 53, 32, 17, 28, 23, 22,
            33, 33, 18, 12, 24, 30, 9, 53, 47, 17, 45, 48, 24, 2, 46, 35, 41, 27, 40, 45, 7, 0, 47, 47, 37, 47, 37, 11, 50, 26, 7, 48, 25, 47, 28, 29, 13, 39, 34, 12, 7, 48, 20, 4, 36, 53, 41, 17, 29, 53, 9, 15, 26, 32, 23,
            6, 39, 40, 12, 31, 41, 38, 51, 37, 1, 7, 44, 52, 6, 53, 31, 29, 3, 28, 45, 52, 6, 37, 0, 24, 21, 33, 38, 5, 35, 21, 22, 13, 11, 18, 3, 41, 27, 21, 30, 14, 35, 18, 16, 45, 20, 37, 29, 38, 32, 40, 31, 15, 54, 4, 32,
            49, 38, 25, 6, 36, 4, 32, 4, 29, 6, 29, 40, 48, 17, 21, 42, 31, 24, 53, 0, 38, 10, 26, 36, 8, 53, 14, 11, 20, 19, 51, 0, 12, 0, 27, 53, 28, 33, 24, 17, 37, 40, 18, 49, 17, 24, 21, 49, 25, 52, 54, 37, 23, 25, 22, 30,
            13, 45, 19, 48, 26, 54, 8, 54, 42, 15, 44, 26, 17, 1, 18, 13, 23, 18, 41, 48, 27, 34, 51, 32, 48, 29, 2, 51, 25, 13, 42, 27, 2, 13, 0, 37, 11, 51, 27, 25, 31, 25, 9, 43, 1, 20, 42, 43, 17, 24, 25, 2, 9, 16,
            38, 21, 43, 45, 48, 44, 8, 39, 17, 49, 4, 32, 52, 14, 50, 1, 45, 9, 1, 22, 17, 2, 30, 18, 31, 24, 16, 22, 3, 25, 49, 32, 24, 36, 40, 24, 3, 13, 5, 38, 4, 32, 39, 52, 33, 4, 30, 47, 27, 49, 42, 6, 16, 24, 46,
            2, 27, 24, 30, 3, 16, 50, 45, 10, 54, 20, 42, 20, 12, 2, 5, 21, 14, 45, 20, 14, 33, 5, 24, 46, 7, 12, 25, 10, 34, 38, 0, 10, 20, 28, 37, 14, 49, 39, 13, 28, 3, 8, 16, 45, 22, 43, 3, 31, 36, 52, 31, 35, 47,
            32, 48, 40, 52, 34, 23, 22, 29, 12, 41, 0, 37, 49, 41, 33, 4, 19, 24, 5, 45, 2, 31, 39, 37, 30, 23, 11, 32, 20, 40, 11, 23, 51, 7, 39, 38, 51, 53, 28, 42, 49, 48, 3, 7, 53, 48, 38, 4, 26, 27, 8, 41, 54, 25,
            19, 25, 28, 0, 5, 1, 7, 0, 22, 48, 18, 4, 6, 1, 18, 43, 33, 44, 31, 12, 6, 29, 35, 2, 54, 23, 39, 53, 21, 53, 46, 48, 32, 10, 17, 5, 27, 45, 25, 31, 46, 5, 20, 15, 45, 36, 51, 18, 9, 11, 23, 16, 5, 6, 53, 7,
            19, 33, 40, 31, 25, 51, 4, 51, 5, 20, 0, 14, 1, 41, 13, 46, 9, 4, 34, 54, 51, 14, 28, 41, 2, 25, 26, 5, 42, 10, 3, 38, 18, 14, 1, 8, 54, 45, 0, 17, 9, 8, 32, 38, 42, 46, 10, 50, 28, 28, 5, 41, 25, 21, 12, 47,
            27, 45, 0, 12, 39, 42, 12, 34, 20, 40, 38, 29, 30, 14, 49, 26, 39, 19, 45, 37, 10, 39, 1, 48, 4, 48, 4, 35, 37, 19, 15, 7, 25, 47, 28, 5, 42, 13, 43, 5, 2, 27, 13, 5, 34, 46, 44, 15, 1, 21, 44, 18, 11, 41, 44,
            14, 20, 19, 20, 40, 9, 21, 7, 50, 42, 44, 1, 43, 34, 20, 36, 53, 22, 44, 27, 38, 49, 12, 44, 51, 35, 39, 51, 9, 44, 49, 14, 1, 34, 22, 33, 31, 43, 50, 23, 32, 29, 19, 40, 10, 15, 13, 41, 35, 5, 26, 10, 4, 35,
            25, 6, 48, 8, 4, 5, 52, 26, 46, 9, 28, 29, 50, 32, 45, 44, 19, 13, 15, 42, 17, 49, 29, 9, 10, 5, 20, 11, 35, 40, 15, 48, 48, 48, 33, 5, 25, 21, 38, 13, 30, 52, 40, 52, 44, 21, 39, 45, 42, 2, 31, 9, 45, 13, 46,
            24, 1, 8, 6, 28, 9, 17, 50, 11, 44, 26, 30, 11, 37, 15, 45, 7, 7, 15, 21, 29, 48, 53, 24, 26, 13, 35, 16, 33, 42, 48, 9, 9, 29, 11, 22, 34, 31, 23, 23, 17, 42, 48, 12, 46, 30, 47, 25, 38, 46, 51, 51, 1, 53, 53,
            41, 29, 6, 22, 20, 7, 3, 35, 16, 10, 30, 16, 31, 2, 22, 33, 47, 42, 29, 24, 15, 52, 26, 34, 11, 54, 36, 15, 35, 24, 18, 50, 35, 10, 24, 2, 34, 34, 6, 0, 3, 3, 34, 47, 0, 14, 16, 10, 39, 1, 29, 7, 51, 33, 40,
            52, 8, 54, 42, 50, 47, 1, 38, 53, 4, 47, 3, 26, 52, 4, 16, 41, 26, 39, 0, 23, 11, 54, 11, 36, 45, 28, 53, 40, 12, 20, 33, 0, 43, 54, 36, 42, 3, 25, 27, 3, 20, 52, 45, 28, 30, 31, 52, 40, 17, 6, 36, 30, 25, 16,
            32, 29, 53, 18, 26, 38, 15, 29, 51, 53, 8, 19, 43, 0, 44, 8, 43, 51, 35, 35, 11, 47, 35, 30, 44, 3, 32, 14, 54, 6, 47, 8, 5, 41, 43, 36, 8, 19, 47, 26, 45, 13, 2, 15, 46, 20, 21, 2, 15, 36, 33, 48, 35, 11, 47,
            29, 5, 53, 21, 6, 32, 28, 13, 49, 40, 40, 39, 37, 42, 27, 5, 22, 15, 6, 23, 41, 18, 47, 9, 33, 16, 24, 24, 14, 13, 7, 18, 12, 13, 14, 3, 10, 37, 28, 28, 33, 14, 9, 29, 4, 11, 52, 50, 1, 1, 9, 44, 7, 38, 27, 46,
            4, 16, 0, 17, 54, 20, 40, 14, 27, 34, 16, 21, 12, 47, 2, 43, 15, 50, 5, 7, 0, 37, 22, 43, 53, 54, 39, 3, 32, 37, 38, 51, 19, 46, 32, 18, 28, 16, 43, 2, 49, 43, 47, 41, 14, 2, 27, 8, 16, 4, 39, 26, 9, 15, 54, 10,
            35, 24, 27, 21, 30, 38, 27, 22, 2, 24, 35, 17, 0, 46, 35, 4, 50, 48, 32, 34, 45, 22, 1, 10, 32, 11, 22, 50, 23, 16, 2, 40, 10, 22, 34, 18, 19, 24, 10, 50, 11, 27, 21, 46, 35, 18, 3, 15, 6, 3, 3, 16, 42, 9, 35, 8,
            53, 18, 21, 38, 15, 0, 2, 16, 52, 19, 41, 30, 3, 23, 36, 35, 28, 49, 14, 19, 28, 30, 0, 10, 0, 42, 4, 25, 40, 43, 11, 16, 28, 30, 52, 21, 9, 47, 34, 26, 0, 49, 31, 26, 35, 34, 4, 18, 10, 48, 11, 7, 47, 46, 5, 40,
            43, 27, 31, 51, 40, 15, 20, 24, 2, 36, 53, 12, 31, 39, 8, 49, 2, 8, 33, 42, 53, 14, 13, 46, 3, 24, 48, 30, 39, 53, 54, 13, 9, 3, 33, 42, 50, 31, 49, 21, 16, 43, 30, 20, 1, 25, 6, 13, 20, 15, 43, 52, 44, 8, 43, 9,
            8, 10, 23, 38, 33, 12, 41, 33, 26, 1, 46, 52, 54, 6, 41, 35, 0, 30, 8, 20, 54, 19, 32, 34, 32, 8, 25, 10, 33, 36, 43, 54, 34, 47, 16, 44, 15, 44, 23, 23, 42, 31, 28, 22, 39, 19, 14, 53, 20, 30, 35, 19, 22, 44, 47,
            26, 17, 33, 41, 44, 1, 28, 6, 6, 45, 45, 17, 39, 5, 26, 36, 28, 36, 38, 19, 22, 24, 43, 36, 18, 51, 46, 6, 8, 50, 36, 4, 6, 12, 7, 28, 21, 19, 19, 27, 29, 50, 41, 10, 14, 32, 13, 37, 51, 34, 49, 14, 10, 9, 7, 2, 10,
            38, 14, 50, 49, 52, 8, 49, 40, 9, 31, 34, 29, 41, 42, 38, 23, 5, 37, 34, 17, 23, 50, 36, 18, 29, 41, 27, 39, 4, 16, 6, 43, 44, 22, 30, 40, 39, 30, 20, 19, 19, 12, 27, 9, 23, 34, 42, 21, 44, 52, 11, 54, 11, 50, 12, 1, 17, 45, 8};
        //Traduzir sequencia
        System.out.println("arr[" + seq.length + "]=");
        for (int i = 0; i < seq.length; i++) {
            seq[i] = seq[i] % ko;
//            System.out.print(seq[i]);
//            System.out.print(", ");
        }
        System.out.println();

        System.out.println("Check sequence 1");
        boolean test = false;
//        boolean test = checkSequence(k, seq);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }

        System.out.println("Check sequence 2");
        Map<Integer, List<Integer>> mapInvalidPositions = mapInvalidPositions(k);
//        test = checkSequence2(seq, mapInvalidPositions);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }

//        assertTrue("Combination invalid", test);
    }

    private boolean clearEmptyCombination(Integer pos, Integer val, Map<Integer, Integer> countval, int maxValCount, Map<Integer, List<Integer>> possibilidades, List<Integer> posicoesExcluidas) {
        return clearEmptyCombination(pos, val, countval, maxValCount, possibilidades, posicoesExcluidas, true);
    }

    private boolean clearEmptyCombination(Integer pos, Integer val, Map<Integer, Integer> countval, int max_val_count, Map<Integer, List<Integer>> possibilidades, List<Integer> posicoesExcluidas, boolean failEmpty) {
        boolean roolback = false;
        int len = possibilidades.size();
        int divPoint = Collections.binarySearch(posicoesExcluidas, pos);
        if (divPoint < 0) {
            divPoint = (-(divPoint) - 1);
        }
        countval.put(val, countval.get(val) + 1);
        // Remover val das futuras listas de possibilidade
        if (countval.get(val) >= max_val_count) {
            for (int i = pos; i < len; i++) {
                List<Integer> possiPosi = possibilidades.get(i);
                possiPosi.remove(val);
                if (failEmpty && possiPosi.isEmpty()) {
                    roolback = true;
                    break;
                }
            }
        }
        for (int i = divPoint; i < posicoesExcluidas.size(); i++) {
            Integer posicao = posicoesExcluidas.get(i);
            List<Integer> possiPosi = possibilidades.get(posicao);
            possiPosi.remove(val);
            if (failEmpty && possiPosi.isEmpty()) {
                roolback = true;
                break;
            }
        }
        return roolback;
    }

    private boolean checkSequence2(Integer[] arr, Map<Integer, List<Integer>> mapExcludePos) {
        int res = 0;
        for (int i = 0; i < arr.length; i++) {
            int pos = i;
            List<Integer> posExcl = mapExcludePos.get(pos);
            int val = arr[i];
            for (int j = 0; j < posExcl.size(); j++) {
                int val2 = arr[posExcl.get(j)];
                if (val == val2) {
                    res++;
                    System.out.println("Value " + arr[i] + " Failed in position: " + i + " conflict " + posExcl.get(j));
                }
            }
        }
        System.out.println("Inconsistence count: " + res);
        return res == 0;
    }

    private boolean checkSequence(int k, Integer[] arr) {
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

        int incount = 0;
        for (int i = 0; i < len; i++) {
            int val = arr[i];
            boolean max = countval[val] <= maxcount;
            boolean retlocal = true;
            retlocal = retlocal && max;
            up = arrup[i];
            down = arrdown[i];
            int j = 0;
            for (j = 0; j < i && retlocal; j++) {
                if (arrdown[j] == up || arrdown[j] == down || arrup[j] == up) {
                    retlocal = retlocal && arr[j] != val;
                }
            }
            if (!retlocal) {
                if (!max) {
                    System.out.println("Max count exceded: " + val + " " + countval[val]);
                }
                System.out.println("Value " + val + " Failed in position: " + i + " conflict " + (j - 1));
                incount++;
            }
            countval[val]++;
            ret = ret && retlocal;
        }
        System.out.println("Inconsistence count: " + incount);

        return ret;
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

    public void testCombTotal() {
        int k = 57;
        int ko = k - 2;
        int len = ((ko + 1) * ko) / 2;
        Integer[] arr = new Integer[len];
        Map<Integer, Integer> countval = new HashMap<>();
        Map<Integer, List<Integer>> mapExcludePosition = mapInvalidPositions(k);

        int maxValCount = 0;
        if (ko != 0) {
            maxValCount = len / ko;
        }

        Map<Integer, List<Integer>> possibilidades = new HashMap<>(len);
        Integer[] targetv = new Integer[ko];

        for (int j = 0; j < ko; j++) {
            targetv[j] = j;
            countval.put(j, 0);
        }
        List<Integer> targetvList = Arrays.asList(targetv);

        for (int i = 0; i < len; i++) {
            possibilidades.put(i, new LinkedList<>(targetvList));
        }

        for (int i = 0; i < ko; i++) {
            arr[i] = i;
            List<Integer> posicoesExcluidas = mapExcludePosition.get(i);
            clearEmptyCombination(i, i, countval, maxValCount, possibilidades, posicoesExcluidas);
        }

        for (int i = ko; i < len; i++) {
            Integer bestVal = null;
            Integer weight = 0;
            List<Integer> posicoesExcluidas = mapExcludePosition.get(i);
            int divPoint = Collections.binarySearch(posicoesExcluidas, i);
            if (divPoint < 0) {
                divPoint = (-(divPoint) - 1);
            }
            int pesoAtual = 0;
            int maiorPossi = 0;
            int menorPossi = 0;
            for (int j = ko; j < len; j++) {
                List<Integer> possi = possibilidades.get(j);
                int posiz = possi.size();
                pesoAtual = pesoAtual + posiz;
                if (j == 0) {
                    maiorPossi = posiz;
                    menorPossi = posiz;
                } else {
                    if (posiz > maiorPossi) {
                        maiorPossi = posiz;
                    }
                    if (posiz < menorPossi) {
                        menorPossi = posiz;
                    }
                }
            }

            List<Integer> posis = null;
            List<Integer> listaPossibilidades = possibilidades.get(i);

            if (!listaPossibilidades.isEmpty()) {
                posis = listaPossibilidades;
            } else {
                posis = targetvList;
            }

            for (Integer j : posis) {
                int peso = 0;
                int val = j;
                int delta = 0;
//                int locmenorPossi = Integer.MAX_VALUE;

                // Remover val das futuras listas de possibilidade
                if (countval.get(val) < maxValCount) {
                    for (int z = divPoint; z < posicoesExcluidas.size(); z++) {
                        Integer posicao = posicoesExcluidas.get(z);
                        List<Integer> possiPosi = possibilidades.get(posicao);
                        int posiz = possiPosi.size();
                        if (possiPosi.contains(val)) {
                            delta++;
//                            if (posiz - 1 < locmenorPossi) {
//                                locmenorPossi = posiz - 1;
//                            }
                        }
                    }
//                    peso = pesoAtual - delta + locmenorPossi;
                    peso = pesoAtual - delta;
                }
                if (peso > weight) {
                    bestVal = j;
                    weight = peso;
                }
            }

            if (bestVal != null) {
                clearEmptyCombination(i, bestVal, countval, maxValCount, possibilidades, posicoesExcluidas);
            }
            arr[i] = bestVal;
        }

        for (int j = 0; j < ko; j++) {
            countval.put(j, 0);
        }

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null) {
                countval.put(arr[i], countval.get(arr[i]) + 1);
            }
        }

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null) {
                for (int j = 0; j < ko; j++) {
                    int cont = countval.get(j);
                    if (cont < maxValCount) {
                        arr[i] = j;
                        countval.put(j, cont + 1);
                        break;
                    }
                }
            }
        }

        System.out.print("\nCombinação:");
//        System.out.println(arr);
        UtilTmp.printArray(arr);
        boolean test = checkSequence(k, arr);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }
        assertTrue("Combination invalid", test);
    }

    private int checkVal(Integer[] arr, Integer pos, Integer val, Map<Integer, Integer> countval, int maxValCount, Map<Integer, List<Integer>> possibilidades, Map<Integer, List<Integer>> mapExcludePosition) {
        int peso = 0;
        int delta = 0;
        int clean = 0;

        if (countval.get(val) >= maxValCount) {
            return 0;
        }

        List<Integer> posicoesExcluidas = mapExcludePosition.get(pos);
        int len = possibilidades.size();
        int divPoint = Collections.binarySearch(posicoesExcluidas, pos);
        if (divPoint < 0) {
            divPoint = (-(divPoint) - 1);
        }
        // Remover val das futuras listas de possibilidade
        if (countval.get(val) >= maxValCount) {
            for (int w = pos; w < len; w++) {
                List<Integer> possiPosi = possibilidades.get(w);
                if (possiPosi.contains(val)) {
                    delta++;
                }
            }
        }
        for (int z = divPoint; z < posicoesExcluidas.size(); z++) {
            Integer posicao = posicoesExcluidas.get(z);
            List<Integer> possiPosi = possibilidades.get(posicao);
            if (possiPosi.contains(val)) {
                delta++;
            }
        }
        return peso;
    }
}

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
        Integer[] seq = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 1, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 3, 0, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 5, 0, 0, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 7, 0, 1, 2, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 9, 0, 1, 2, 3, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 15, 54, 0, 1, 11, 2, 53, 4, 0, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 13, 2, 3, 4, 5, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 15, 3, 4, 5, 6, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 17, 3, 4, 5, 6, 7, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 19, 4, 5, 6, 7, 8, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 21, 4, 5, 6, 7, 8, 9, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 23, 4, 5, 6, 8, 9, 10, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 25, 4, 5, 6, 7, 9, 8, 11, 10, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 27, 6, 7, 8, 10, 11, 9, 12, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 29, 6, 7, 8, 11, 12, 10, 13, 32, 33, 0, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 31, 7, 8, 9, 10, 13, 12, 14, 11, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 33, 7, 8, 9, 11, 12, 14, 13, 10, 15, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 35, 8, 9, 11, 10, 14, 15, 16, 12, 13, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 37, 8, 9, 11, 10, 12, 16, 17, 14, 0, 13, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 8, 39, 9, 11, 10, 12, 13, 17, 16, 14, 15, 18, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 8, 41, 9, 11, 10, 12, 13, 14, 18, 19, 15, 16, 17, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 43, 10, 12, 13, 14, 15, 19, 18, 17, 20, 16, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 45, 14, 15, 16, 20, 21, 18, 17, 19, 48, 49, 50, 51, 52, 53, 54, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 14, 15, 16, 17, 47, 18, 21, 20, 19, 22, 1, 50, 51, 52, 53, 54, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 49, 23, 21, 52, 22, 1, 53, 54, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 22, 51, 2, 24, 54, 1, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 22, 2, 25, 24, 24, 53, 3, 2, 5, 4, 7, 6, 9, 8, 10, 11, 13, 12, 15, 14, 17, 16, 19, 18, 21, 20, 22, 23, 24, 25, 26, 3, 26, 4, 6, 7, 5, 8, 11, 10, 9, 12, 14, 15, 13, 16, 18, 19, 17, 20, 23, 22, 25, 24, 26, 27, 3, 21, 27, 7, 6, 8, 9, 10, 11, 5, 13, 15, 14, 16, 17, 19, 12, 20, 21, 22, 25, 18, 26, 27, 24, 23, 28, 6, 8, 9, 11, 12, 13, 14, 15, 10, 16, 17, 18, 20, 21, 23, 22, 19, 24, 26, 25, 29, 27, 28, 4, 28, 11, 10, 13, 12, 15, 14, 16, 17, 9, 19, 21, 18, 22, 23, 24, 26, 27, 29, 28, 30, 20, 25, 7, 12, 14, 15, 13, 16, 17, 18, 19, 20, 10, 23, 21, 24, 26, 29, 28, 27, 22, 31, 25, 30, 7, 15, 14, 16, 17, 18, 19, 20, 21, 23, 22, 13, 25, 27, 28, 24, 31, 30, 26, 29, 32, 8, 16, 17, 18, 19, 20, 21, 23, 22, 24, 25, 26, 28, 31, 29, 30, 33, 32, 27, 9, 11, 18, 19, 20, 21, 23, 22, 24, 25, 26, 27, 30, 33, 17, 28, 31, 29, 32, 41, 34, 20, 21, 23, 22, 24, 25, 26, 27, 12, 33, 30, 31, 32, 34, 28, 35, 19, 29, 23, 22, 24, 25, 26, 27, 29, 28, 32, 34, 30, 33, 35, 36, 31, 14, 21, 24, 25, 26, 27, 29, 28, 30, 34, 32, 33, 35, 36, 37, 22, 31, 15, 26, 27, 29, 28, 30, 31, 35, 36, 32, 34, 37, 33, 29, 38, 25, 28, 15, 30, 31, 32, 36, 35, 34, 37, 38, 39, 33, 18, 16, 30, 31, 33, 34, 37, 38, 35, 36, 32, 40, 39, 39, 20, 33, 32, 36, 38, 37, 39, 40, 41, 34, 31, 29, 35, 34, 37, 39, 40, 36, 38, 32, 42, 23, 35, 42, 38, 40, 41, 42, 30, 42, 35, 36, 34, 36, 41, 39, 40, 42, 43, 44, 38, 44, 47, 42, 44, 33, 44, 38, 43, 43, 45, 46, 49, 43, 50, 44, 51, 46, 41, 46, 45, 37, 50, 52, 39, 43, 46, 47, 48, 47, 48, 40, 45, 41, 48, 49, 45, 40, 37};
        //0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 1, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 3, 0, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 5, 0, 0, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 7, 0, 1, 2, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 9, 0, 1, 2, 3, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 15, 54, 0, 1, 11, 2, 53, 4, 0, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 13, 2, 3, 4, 5, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 15, 3, 4, 5, 6, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 17, 3, 4, 5, 6, 7, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 19, 4, 5, 6, 7, 8, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 21, 4, 5, 6, 7, 8, 9, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 23, 4, 5, 6, 8, 9, 10, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 25, 4, 5, 6, 7, 9, 8, 11, 10, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 27, 6, 7, 8, 10, 11, 9, 12, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 29, 6, 7, 8, 11, 12, 10, 13, 32, 33, 0, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 31, 7, 8, 9, 10, 13, 12, 14, 11, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 33, 7, 8, 9, 11, 12, 14, 13, 10, 15, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 35, 8, 9, 11, 10, 14, 15, 16, 12, 13, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 37, 8, 9, 11, 10, 12, 16, 17, 14, 13, 40, 0, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 8, 39, 9, 11, 10, 12, 13, 17, 16, 14, 15, 18, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 8, 41, 9, 11, 10, 12, 13, 14, 18, 19, 15, 16, 17, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 43, 10, 12, 13, 14, 15, 19, 18, 17, 20, 16, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 45, 14, 15, 16, 20, 21, 18, 17, 19, 48, 49, 50, 51, 52, 53, 54, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 14, 15, 16, 17, 47, 18, 21, 20, 19, 22, 1, 50, 51, 52, 53, 54, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 49, 23, 21, 52, 22, 1, 53, 54, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 22, 51, 2, 24, 54, 1, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 22, 2, 25, 24, 24, 53, 3, 2, 5, 4, 7, 6, 9, 8, 10, 11, 13, 12, 15, 14, 17, 16, 19, 18, 21, 20, 22, 23, 24, 25, 26, 3, 26, 4, 6, 7, 5, 8, 11, 10, 9, 12, 14, 15, 13, 16, 18, 19, 17, 20, 23, 22, 25, 24, 26, 27, 3, 21, 27, 7, 6, 8, 9, 10, 11, 5, 13, 15, 14, 16, 17, 19, 12, 20, 21, 22, 25, 18, 26, 27, 24, 23, 28, 6, 8, 9, 11, 12, 13, 14, 15, 10, 16, 17, 18, 20, 21, 23, 22, 19, 24, 26, 25, 29, 27, 28, 4, 28, 11, 10, 13, 12, 15, 14, 16, 17, 9, 19, 21, 18, 22, 23, 24, 26, 27, 29, 28, 30, 20, 25, 7, 12, 14, 15, 13, 16, 17, 18, 19, 20, 10, 23, 21, 24, 26, 29, 28, 27, 22, 31, 25, 30, 7, 15, 14, 16, 17, 18, 19, 20, 21, 23, 22, 13, 25, 27, 28, 24, 31, 30, 26, 29, 32, 8, 16, 17, 18, 19, 20, 21, 23, 22, 24, 25, 26, 28, 31, 29, 30, 33, 32, 27, 9, 11, 18, 19, 20, 21, 23, 22, 24, 25, 26, 27, 30, 33, 17, 28, 31, 29, 32, 41, 34, 20, 21, 23, 22, 24, 25, 26, 27, 12, 33, 30, 31, 32, 34, 28, 35, 19, 29, 23, 22, 24, 25, 26, 27, 29, 28, 32, 34, 30, 33, 35, 36, 31, 14, 21, 24, 25, 26, 27, 29, 28, 30, 34, 32, 33, 35, 36, 37, 22, 31, 15, 26, 27, 29, 28, 30, 31, 35, 36, 32, 34, 37, 33, 29, 38, 25, 28, 15, 30, 31, 32, 36, 35, 34, 37, 38, 39, 33, 18, 16, 30, 31, 33, 34, 37, 38, 35, 36, 32, 40, 39, 39, 20, 33, 32, 36, 38, 37, 39, 40, 41, 34, 31, 29, 35, 34, 37, 39, 40, 36, 38, 32, 42, 23, 35, 42, 38, 40, 41, 42, 30, 42, 35, 36, 34, 36, 41, 39, 40, 42, 43, 44, 38, 44, 47, 42, 44, 33, 44, 38, 43, 43, 45, 46, 49, 43, 50, 44, 51, 46, 41, 46, 45, 37, 50, 52, 39, 43, 46, 47, 48, 47, 48, 40, 45, 41, 48, 49, 45, 40, 37};
        //0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 1, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 3, 0, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 5, 0, 0, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 7, 0, 1, 2, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 9, 0, 1, 2, 3, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 15, 54, 0, 1, 11, 2, 53, 4, 0, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 13, 2, 3, 4, 5, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 15, 3, 4, 5, 6, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 17, 3, 4, 5, 6, 7, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 19, 4, 5, 6, 7, 8, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 21, 4, 5, 6, 7, 8, 9, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 23, 4, 5, 6, 8, 9, 10, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 25, 4, 5, 6, 7, 9, 8, 11, 10, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 27, 6, 7, 8, 10, 11, 9, 12, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 29, 6, 7, 8, 11, 12, 10, 13, 0, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 31, 7, 8, 9, 10, 13, 12, 14, 11, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 33, 7, 8, 9, 11, 12, 14, 13, 10, 15, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 35, 8, 9, 11, 10, 14, 15, 16, 12, 13, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 37, 8, 9, 11, 10, 12, 16, 17, 14, 13, 40, 0, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 8, 39, 9, 11, 10, 12, 13, 17, 16, 14, 15, 18, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 8, 41, 9, 11, 10, 12, 13, 14, 18, 19, 15, 16, 17, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 43, 10, 12, 13, 14, 15, 19, 18, 17, 20, 16, 46, 47, 48, 49, 50, 51, 52, 53, 54, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 45, 14, 15, 16, 20, 21, 18, 17, 19, 48, 49, 50, 51, 52, 53, 54, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 14, 15, 16, 17, 47, 18, 21, 20, 19, 22, 1, 50, 51, 52, 53, 54, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 49, 23, 21, 52, 22, 1, 53, 54, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 22, 51, 24, 54, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 22, 2, 25, 24, 24, 53, 3, 2, 5, 4, 7, 6, 9, 8, 10, 11, 13, 12, 15, 14, 17, 16, 19, 18, 21, 20, 22, 23, 24, 25, 26, 3, 26, 4, 6, 7, 5, 8, 11, 10, 9, 12, 14, 15, 13, 16, 18, 19, 17, 20, 23, 22, 25, 24, 26, 27, 3, 21, 27, 7, 6, 8, 9, 10, 11, 5, 13, 15, 14, 16, 17, 19, 12, 20, 21, 22, 25, 18, 26, 27, 24, 23, 28, 6, 8, 9, 11, 12, 13, 14, 15, 10, 16, 17, 18, 20, 21, 23, 22, 19, 24, 26, 25, 29, 27, 28, 4, 28, 11, 10, 13, 12, 15, 14, 16, 17, 9, 19, 21, 18, 22, 23, 24, 26, 27, 29, 28, 30, 20, 25, 7, 12, 14, 15, 13, 16, 17, 18, 19, 20, 10, 23, 21, 24, 26, 29, 28, 27, 22, 31, 25, 30, 7, 15, 14, 16, 17, 18, 19, 20, 21, 23, 22, 13, 25, 27, 28, 24, 31, 30, 26, 29, 32, 8, 16, 17, 18, 19, 20, 21, 23, 22, 24, 25, 26, 28, 31, 29, 30, 33, 32, 27, 9, 11, 18, 19, 20, 21, 23, 22, 24, 25, 26, 27, 30, 33, 17, 28, 31, 29, 32, 41, 34, 20, 21, 23, 22, 24, 25, 26, 27, 12, 33, 30, 31, 32, 34, 28, 35, 19, 29, 23, 22, 24, 25, 26, 27, 29, 28, 32, 34, 30, 33, 35, 36, 31, 14, 21, 24, 25, 26, 27, 29, 28, 30, 34, 32, 33, 35, 36, 37, 22, 31, 15, 26, 27, 29, 28, 30, 31, 35, 36, 32, 34, 37, 33, 29, 38, 25, 28, 15, 30, 31, 32, 36, 35, 34, 37, 38, 39, 33, 18, 16, 30, 31, 33, 34, 37, 38, 35, 36, 32, 40, 39, 39, 20, 33, 32, 36, 38, 37, 39, 40, 41, 34, 31, 29, 35, 34, 37, 39, 40, 36, 38, 42, 32, 23, 35, 42, 38, 40, 41, 42, 30, 42, 35, 36, 34, 36, 41, 39, 40, 42, 43, 44, 38, 44, 47, 42, 44, 33, 44, 38, 43, 46, 49, 43, 50, 45, 43, 44, 51, 46, 41, 46, 45, 37, 50, 52, 39, 43, 46, 47, 48, 47, 48, 40, 45, 41, 48, 49, 45, 40, 37};
        //Traduzir sequencia

        System.out.println("Check sequence 1");
        boolean test = checkSequence(k, seq);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }

        System.out.println("Check sequence 2");
        Map<Integer, List<Integer>> mapInvalidPositions = mapInvalidPositions(k);
        test = checkSequence2(seq, mapInvalidPositions) == 0;
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }

        assertTrue("Combination invalid", test);
    }

    public void testIncCombinacao() {
        if (true) {
            return;
        }
        System.out.println("\nInc-Combination:");
        int k = 57;
        Integer[] initial = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 50, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 52, 51, 54, 53, 0, 4, 2, 3, 7, 5, 6, 10, 8, 9, 13, 11, 12, 16, 14, 15, 19, 17, 18, 22, 20, 21, 25, 23, 24, 28, 26, 27, 31, 29, 30, 34, 32, 33, 37, 35, 36, 40, 38, 39, 43, 41, 42, 46, 44, 45, 49, 47, 48, 53, 54, 51, 52, 5, 6, 7, 3, 4, 9, 11, 12, 8, 10, 14, 15, 13, 17, 18, 16, 20, 21, 19, 23, 24, 22, 26, 27, 25, 29, 30, 28, 32, 33, 31, 35, 36, 34, 38, 39, 37, 41, 42, 40, 44, 45, 43, 47, 48, 46, 50, 51, 54, 53, 52, 49, 0, 1, 8, 9, 10, 6, 7, 12, 14, 15, 11, 17, 13, 19, 20, 16, 22, 18, 24, 25, 21, 27, 23, 29, 30, 26, 32, 28, 34, 35, 31, 37, 33, 39, 40, 36, 42, 38, 44, 45, 41, 47, 43, 49, 52, 53, 54, 46, 48, 50, 51, 8, 1, 10, 5, 7, 11, 13, 9, 16, 17, 12, 18, 14, 15, 21, 23, 24, 19, 20, 26, 22, 28, 30, 25, 31, 27, 33, 29, 36, 37, 32, 38, 34, 35, 41, 43, 44, 39, 40, 46, 42, 48, 51, 53, 54, 52, 45, 47, 49, 50, 0, 2, 11, 12, 6, 14, 15, 9, 10, 18, 19, 13, 21, 22, 16, 17, 25, 26, 20, 28, 29, 23, 24, 32, 33, 27, 35, 30, 36, 31, 39, 40, 34, 42, 37, 43, 38, 46, 47, 41, 49, 52, 54, 51, 53, 44, 50, 45, 48, 11, 2, 4, 13, 15, 16, 10, 9, 19, 12, 20, 14, 23, 17, 25, 18, 27, 28, 21, 22, 24, 31, 33, 26, 34, 36, 29, 30, 38, 32, 41, 42, 35, 44, 37, 45, 39, 40, 48, 53, 54, 51, 52, 43, 50, 49, 46, 47, 0, 1, 3, 16, 8, 17, 18, 20, 21, 12, 13, 14, 15, 23, 26, 19, 27, 29, 30, 22, 32, 24, 25, 35, 28, 37, 38, 39, 31, 33, 41, 34, 36, 45, 46, 47, 51, 52, 54, 53, 50, 40, 42, 49, 43, 48, 44, 3, 1, 4, 17, 18, 16, 21, 20, 22, 12, 13, 14, 15, 27, 28, 19, 30, 31, 32, 23, 25, 24, 26, 37, 38, 29, 40, 41, 42, 33, 43, 34, 35, 49, 51, 53, 54, 52, 50, 36, 39, 44, 48, 45, 47, 46, 0, 2, 5, 19, 20, 22, 23, 21, 24, 15, 13, 14, 16, 17, 18, 31, 32, 33, 34, 35, 36, 25, 26, 27, 28, 29, 30, 43, 44, 45, 48, 49, 50, 52, 54, 53, 51, 37, 38, 41, 39, 47, 46, 40, 42, 5, 2, 4, 19, 23, 22, 24, 25, 26, 27, 16, 14, 15, 17, 18, 20, 21, 33, 34, 35, 36, 38, 28, 39, 30, 29, 31, 32, 44, 49, 48, 53, 54, 52, 51, 50, 40, 37, 46, 47, 41, 42, 43, 45, 0, 1, 3, 6, 7, 23, 22, 24, 25, 26, 17, 18, 29, 19, 21, 20, 35, 36, 34, 37, 27, 39, 40, 28, 42, 30, 47, 48, 51, 52, 54, 53, 49, 50, 31, 32, 33, 45, 46, 38, 41, 44, 43, 3, 1, 4, 6, 7, 23, 25, 24, 27, 28, 29, 30, 20, 18, 19, 21, 22, 37, 38, 39, 26, 41, 42, 40, 46, 51, 47, 53, 54, 52, 48, 50, 49, 32, 31, 34, 33, 45, 43, 44, 35, 36, 0, 2, 5, 6, 7, 8, 26, 28, 29, 30, 31, 32, 33, 34, 20, 21, 22, 23, 24, 25, 27, 41, 46, 47, 49, 52, 54, 53, 51, 50, 48, 35, 36, 43, 45, 44, 37, 39, 38, 42, 40, 5, 2, 4, 6, 7, 8, 29, 30, 31, 32, 33, 34, 35, 22, 23, 21, 24, 25, 40, 26, 48, 54, 52, 45, 46, 50, 51, 47, 49, 27, 28, 37, 36, 44, 43, 38, 42, 39};
        Integer[] arr = fillArray(k, initial);

        System.out.print("\nCombinação-final:");
        UtilTmp.printArray(arr);
        System.out.println("Check sequence 1.1: ");

        boolean test = checkSequence(k, arr);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }

        System.out.println("Check sequence 2.1: ");
        Map<Integer, List<Integer>> mapInvalidPositions = mapInvalidPositions(k);
        test = checkSequence2(arr, mapInvalidPositions) == 0 && test;
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }

        assertTrue("Combination invalid", test);
    }

    public void testCombTotal() {
//        if (true) {
//            return;
//        }
        int k = 57;
        int ko = k - 2;

        Integer[] startArray = new Integer[ko];
        for (int i = 0; i < startArray.length; i++) {
            startArray[i] = i;
        }

        Integer[] arr = fillArray(k, startArray);

        System.out.print("\nCombinação-final:");
        UtilTmp.printArray(arr);
        System.out.println("Check sequence 1.1: ");

        boolean test = checkSequence(k, arr);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }

        System.out.println("Check sequence 2.1: ");
        Map<Integer, List<Integer>> mapInvalidPositions = mapInvalidPositions(k);
        test = checkSequence2(arr, mapInvalidPositions) == 0 && test;
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }

        assertTrue("Combination invalid", test);
    }

    public Integer[] fillArray(int k, Integer[] startArray) {
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

        for (int i = 0; i < startArray.length; i++) {
            arr[i] = startArray[i];
            List<Integer> posicoesExcluidas = mapExcludePosition.get(i);
            clearEmptyCombination(i, arr[i], countval, maxValCount, possibilidades, posicoesExcluidas);
        }

        int[] deltaposition = new int[len];

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
                int menorPossiLocal = 0;

                for (int u = 0; u < deltaposition.length; u++) {
                    deltaposition[u] = 0;
                }

                // Remover val das futuras listas de possibilidade
                if (countval.get(val) < maxValCount) {
                    for (int z = divPoint; z < posicoesExcluidas.size(); z++) {
                        Integer posicao = posicoesExcluidas.get(z);
                        List<Integer> possiPosi = possibilidades.get(posicao);
                        int posiz = possiPosi.size();
                        if (possiPosi.contains(val)) {
                            delta++;
                            deltaposition[z]++;
                        }
                    }
//                    peso = pesoAtual - delta + locmenorPossi;

                    for (int x = i + 1; x < len; x++) {
                        List<Integer> possi = possibilidades.get(x);
                        int posiz = possi.size() - deltaposition[x];
                        if (x == i + 1) {
                            menorPossiLocal = posiz;
                        } else {
                            if (posiz < menorPossiLocal) {
                                menorPossiLocal = posiz;
                            }
                        }
                    }

                    if (menorPossiLocal < 0) {
                        menorPossiLocal = 0;
                    }

                    peso = pesoAtual - delta + menorPossiLocal;
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

        System.out.print("\nCombinação-ini:");
        UtilTmp.printArray(arr);

        for (int j = 0; j < ko; j++) {
            countval.put(j, 0);
        }

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != null) {
                countval.put(arr[i], countval.get(arr[i]) + 1);
            }
        }

        System.out.print("\nCount-val:");
        System.out.println(countval);

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
        return arr;
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
        Integer cur = countval.get(val);
        countval.put(val, cur + 1);
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

    private int checkSequence2(Integer[] arr, Map<Integer, List<Integer>> mapExcludePos) {
        return checkSequence2(arr, mapExcludePos, false);
    }

    private int checkSequence2(Integer[] arr, Map<Integer, List<Integer>> mapExcludePos, boolean silence) {
        int res = 0;
        for (int i = 0; i < arr.length; i++) {
            int pos = i;
            List<Integer> posExcl = mapExcludePos.get(pos);
            int val = arr[i];
            for (int j = 0; j < posExcl.size(); j++) {
                int val2 = arr[posExcl.get(j)];
                if (val == val2) {
                    res++;
                    if (!silence) {
                        System.out.println("Value " + arr[i] + " Failed in position: " + i + " conflict " + posExcl.get(j));
                    }
                }
            }
        }
        if (!silence) {
            System.out.println("Inconsistence count: " + res);
        }
        return res;
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
            for (j = 0; j < i; j++) {
                if (arrdown[j] == up || arrdown[j] == down || arrup[j] == up) {
                    if (arr[j] == val) {
                        System.out.println("Value " + val + " Failed in position: " + i + " conflict " + (j - 1));
                        retlocal = false;
                        incount++;
                    }
                }
            }
            if (!retlocal) {
                if (!max) {
                    System.out.println("Max count exceded: " + val + " " + countval[val]);
                }
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
}

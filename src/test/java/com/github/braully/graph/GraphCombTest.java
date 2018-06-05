package com.github.braully.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;

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
        int k = 57;
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
//                if (verbose) {
//                    System.out.print("deadlock: ");
//                    try {
//                        System.out.print(arr.get(ko));
//                    } catch (Exception e) {
//                    }
//                    System.out.print(" empty-list in: ");
//                    System.out.print(pos);
//                    System.out.println();
//                }
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
        countval.put(val, countval.get(val));
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
        for (int i = 0; i < len && ret; i++) {
            int val = arr[i];
            boolean max = countval[val] <= maxcount;
            ret = ret && max;
            up = arrup[i];
            down = arrdown[i];
            int j = 0;
            for (j = 0; j < i && ret; j++) {
                if (arrdown[j] == up || arrdown[j] == down || arrup[j] == up) {
                    ret = ret && arr[j] != val;
                }
            }
            if (!ret) {
                if (!max) {
                    System.out.println("Max count exceded: " + val + " " + countval[val]);
                }
                System.out.println("Value " + val + " Failed in position: " + i + " conflict " + (j - 1));
            }
            countval[val]++;
        }
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

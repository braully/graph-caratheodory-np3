/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author strike
 */
public class CombMooreGraph {

    private static boolean verbose = true;

    public static Map<Integer, List<Integer>> mapInvalidPositions(int k) {
//        int k = 57;
        int ko = k - 2;
        int len = ((ko + 1) * ko) / 2;

        Map<Integer, Set<Integer>> excludeMapSet = new HashMap<>();
        boolean verbose = false;

        int[] arrup = new int[len];
        int[] arrdown = new int[len];
        int offsetup = ko - 1;
        int up = 0;
        int down = 1;

        for (Integer i = 0; i < len; i++) {
            arrup[i] = up;
            arrdown[i] = down++;
            if (i == offsetup) {
                up++;
                offsetup += (ko - up);
            }
            if (down == ko + 1) {
                down = up + 1;
            }
            excludeMapSet.put(i, new HashSet<>());
        }
        if (verbose) {
            System.out.println("Seq: ");
        }
        for (Integer i = 0; i < len; i++) {
            up = arrup[i];
            down = arrdown[i];
            int count = 0;
            int countko = 0;
            StringBuilder sb = new StringBuilder();
            if (verbose) {
                sb.append(String.format("%4d ", i));
                sb.append("|%4d|:");
            }
            Set<Integer> listExclude = excludeMapSet.get(i);
            for (int j = 0; j < len; j++) {
                if (i != j && (arrdown[j] == up || arrdown[j] == down || arrup[j] == up)) {
                    if (verbose) {
                        sb.append(String.format("%4d ", j));
                    }
                    listExclude.add(j);
                    Set<Integer> list2 = excludeMapSet.get(j);
                    list2.add(i);
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

        Map<Integer, List<Integer>> excludeMapList = new HashMap<>();

        for (Integer i = 0; i < len; i++) {
            List<Integer> list = new ArrayList<>(excludeMapSet.get(i));
            Collections.sort(list);
            excludeMapList.put(i, list);
        }
        return excludeMapList;
    }

    public static void main(String... args) {
        int k = 57;
        int ko = k - 2;
        boolean roolback = false;
        int len = ((ko + 1) * ko) / 2;
        LinkedList<Integer> arr = new LinkedList<>();
        Map<Integer, Integer> countval = new HashMap<>();
        Map<Integer, List<Integer>> mapExcludePosition = mapInvalidPositions(k);

        long lastime = System.currentTimeMillis();

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
            clearEmptyCombination(i, i, countval, maxValCount, possibilidades, mapExcludePosition);
        }

        if (args != null && args.length > 0) {
            for (String str : args) {
                str = str.replaceAll("\\D", "").trim();
//                System.out.println(str);
                Integer val = Integer.parseInt(str);
                arr.add(val);
                roolback = clearEmptyCombination(arr.size(), val, countval, maxValCount, possibilidades, mapExcludePosition);
                if (roolback) {
                    System.err.println("Failed val " + str + " in position " + arr.size());
                    break;
                }
            }
            System.out.println("Starting with: arr[" + arr.size() + "]=" + arr);
        }

        int longest = 0;

        while (arr.size() < len && arr.size() >= ko) {
            int pos = arr.size();
            LinkedList<Integer> list = (LinkedList<Integer>) possibilidades.get(pos);
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
                    int post = i;
                    int valt = arr.get(i);
                    //roolback está vindo true... Verificar isso
                    clearEmptyCombination(post, valt, countval, maxValCount, possibilidades, mapExcludePosition, false);
                }
                possibilidades.get(arr.size()).remove(valRollback);
                roolback = false;
                continue;
            }
            Integer val = list.poll();
            arr.add(val);
            roolback = clearEmptyCombination(pos, val, countval, maxValCount, possibilidades, mapExcludePosition);
            if (pos > longest) {
                longest = pos;
                System.out.print("arr[");
                System.out.print(longest);
                System.out.print("]: ");
                System.out.print(arr);
                System.out.println();
                UtilTmp.dumpArray(arr);

            }
            if (System.currentTimeMillis() - lastime > UtilTmp.ALERT_HOUR) {
                lastime = System.currentTimeMillis();
                UtilTmp.dumpArray(arr);
            }
        }

        if (arr.size() < len) {
            throw new IllegalStateException("Combination impossible");
        }
        System.out.print("\nCombinação-Resultado:");
        System.out.println(arr);
        UtilTmp.dumpArray(arr, "Combinação-Resultado-");

        boolean test = checkSequence(k, arr.toArray(new Integer[0]));
        if (!test) {
            System.out.println("Invalid sequence");
            throw new IllegalArgumentException("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }
    }

    private static boolean clearEmptyCombination(Integer pos, Integer val, Map<Integer, Integer> countval, int maxValCount, Map<Integer, List<Integer>> possibilidades, Map<Integer, List<Integer>> mapExcludePosition) {
        return clearEmptyCombination(pos, val, countval, maxValCount, possibilidades, mapExcludePosition, true);
    }

    private static boolean clearEmptyCombination(Integer pos, Integer val, Map<Integer, Integer> countval, int max_val_count, Map<Integer, List<Integer>> possibilidades, Map<Integer, List<Integer>> mapExcludePosition, boolean failEmpty) {
        boolean roolback = false;
        int len = possibilidades.size();
        List<Integer> posicoesExcluidas = mapExcludePosition.get(pos);
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

    private static boolean checkSequence(int k, Integer[] arr) {
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
}

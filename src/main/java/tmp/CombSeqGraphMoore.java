/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static tmp.CombMooreGraph.mapInvalidPositions;

/**
 *
 * @author strike
 */
public class CombSeqGraphMoore {

    public static void main(String... args) {
        int k = 57;
        int ko = k - 2;

        Integer[] startArray = new Integer[ko];
        for (int i = 0; i < startArray.length; i++) {
            startArray[i] = i;
        }

        Integer[] arr = fillArray3(k, startArray);

        System.out.print("\nCombinação-final:");
        UtilTmp.printArray(arr);
        System.out.println("Check sequence 1.1: ");

        boolean test = checkSequence(k, arr);
        if (!test) {
            System.out.println("Invalid sequence");
        } else {
            System.out.println("Valid sequence");
        }
    }

    public static Integer[] fillArray3(int k, Integer[] startArray) {
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
        initVars(ko, countval, len, possibilidades, startArray, arr, mapExcludePosition, maxValCount);

        int[] deltaposition = new int[len];
        int[] countArr = new int[len - ko];
        Integer[] verticeAdded = new Integer[len - ko];
        Integer[] positionAdded = new Integer[len - ko];

        List<Integer> remainPositions = new ArrayList<>();
        for (int i = ko; i < len; i++) {
            remainPositions.add(i);
            countArr[i - ko] = 0;
        }

        Comparator<Integer> comPossibis = new Comparator<Integer>() {
            @Override
            public int compare(Integer pos1, Integer pos2) {
                int sizepos1 = possibilidades.get(pos1).size();
                int sizepos2 = possibilidades.get(pos2).size();
                if (sizepos1 == 0) {
                    sizepos1 = Integer.MAX_VALUE;
                }
                if (sizepos2 == 0) {
                    sizepos2 = Integer.MAX_VALUE;
                }
                return Integer.compare(sizepos1, sizepos2);
            }
        };
        Collections.sort(remainPositions, comPossibis);
        int count = 0;
        List<Integer> bestVals = new ArrayList<>();
        while (!remainPositions.isEmpty()) {
            Integer i = remainPositions.remove(0);
            Integer bestVal = null;
            Integer weight = 0;
            bestVals.clear();
            List<Integer> posicoesExcluidas = mapExcludePosition.get(i);
            int pesoAtual = 0;
            for (int j = ko; j < len; j++) {
                List<Integer> possi = possibilidades.get(j);
                if (arr[j] == null) {
                    int posiz = possi.size();
                    pesoAtual = pesoAtual + posiz;
                }
            }

            List<Integer> posis = null;
            List<Integer> listaPossibilidades = possibilidades.get(i);

            System.out.print("Posição-" + i + " peso atual " + pesoAtual);
            posis = listaPossibilidades;
            if (listaPossibilidades.isEmpty()) {
                throw new IllegalStateException("Lista vazia encontrada");
            }

            for (Integer j : posis) {
                int peso = 0;
                int val = j;
                int delta = 0;

                for (int u = 0; u < deltaposition.length; u++) {
                    deltaposition[u] = 0;
                }

                // Remover val das futuras listas de possibilidade
                if (countval.get(val) < maxValCount) {
                    for (int z = 0; z < posicoesExcluidas.size(); z++) {
                        Integer posicao = posicoesExcluidas.get(z);
                        List<Integer> possiPosi = possibilidades.get(posicao);
                        if (arr[posicao] == null && possiPosi.contains(val)) {
                            delta++;
                            deltaposition[posicao]++;
                        }
                    }
                    peso = pesoAtual - delta;
                }
                if (peso > weight) {
                    bestVal = j;
                    weight = peso;
                    bestVals.clear();
                    bestVals.add(j);
                } else if (peso == weight) {
                    bestVals.add(j);
                }
            }

            boolean rollback = false;
            if (countArr[count] < bestVals.size()) {
                bestVal = bestVals.get(countArr[count]);
                Integer pos = i;
                rollback = clearAuxPosition(countval, bestVal, maxValCount, pos, len, possibilidades, mapExcludePosition, arr, remainPositions);
//                if (!rollback) {
                arr[i] = bestVal;
                verticeAdded[count] = bestVal;
                positionAdded[count] = pos;
                countArr[count]++;
                count++;
                System.out.print(" added ");
                System.out.print(bestVal);
                if (!rollback) {
                    System.out.print("...ok");
                } else {
                    System.out.print("...erro");
                }
                System.out.print(" |");
                System.out.print(remainPositions.size());
//                }
            } else {
                rollback = true;
            }
            System.out.println();
            if (rollback) {
                for (int x = count; x < countArr.length; x++) {
                    countArr[x] = 0;
                }
                count--;
                Integer lastVal = verticeAdded[count];
                verticeAdded[count] = null;
                Integer lastPostion = positionAdded[count];
                remainPositions.add(lastPostion);
                positionAdded[count] = null;
                arr[lastPostion] = null;
                for (int x = 0; x < count; x++) {
                    arr[positionAdded[x]] = null;
                }

                System.out.println("Rollback ");
                System.out.print("LastVal ");
                System.out.print(lastVal);
                System.out.print(" LastPos ");
                System.out.print(lastPostion);
                System.out.print(" Count ");
                System.out.println(count);

//                System.out.print("Arr: ");
//                UtilTmp.printArray(arr);
                System.out.println("Re-initvars");
//                initVars(ko, countval, len, possibilidades, startArray, arr, mapExcludePosition, maxValCount);
                initVars(ko, countval, len, possibilidades, startArray, arr, mapExcludePosition, maxValCount);
                rollback = false;
                System.out.println("Re-add values in arr");
                for (int x = 0; x < count; x++) {
                    Integer val = verticeAdded[x];
                    Integer pos = positionAdded[x];
//                    System.out.print("Re-add-Posicao-");
//                    System.out.print(pos);
//                    System.out.print(" val ");
//                    System.out.print(val);
                    boolean tmprollback = clearAuxPosition(countval, val, maxValCount, pos, len, possibilidades, mapExcludePosition, arr, remainPositions);
                    arr[pos] = val;
                    rollback = tmprollback || rollback;
//                    System.out.println("");
                    if (rollback) {
                        System.err.println("Adição de valor " + val + " na posição " + pos + " ilegal");
                        throw new IllegalStateException("Estado ilegal após rollback!");
                    }
                }
                System.out.print("CountArr: ");
                UtilTmp.printArray(countArr);
//                if (rollback) {
//                    throw new IllegalStateException("Estado ilegal após rollback!");
//                }
                //rollback
//                continue

                //recalc remains positions
                remainPositions.clear();
                for (int x = 0; x < arr.length; x++) {
                    if (arr[x] == null) {
                        remainPositions.add(x);
                    }
                }
            }
            Collections.sort(remainPositions, comPossibis);
        }

        System.out.println("Count: " + count);
        System.out.print("CountArr: ");
        UtilTmp.printArray(countArr);

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

    private static boolean clearAuxPosition(Map<Integer, Integer> countval, Integer bestVal, int maxValCount, Integer pos, int len, Map<Integer, List<Integer>> possibilidades, Map<Integer, List<Integer>> mapExcludePosition, Integer[] arr, List<Integer> remainPositions) {
        boolean ret = false;
        List<Integer> posicoesExcluidas = mapExcludePosition.get(pos);
        Integer cur = countval.get(bestVal);
        countval.put(bestVal, cur + 1);
        // Remover val das futuras listas de possibilidade
        if (countval.get(bestVal) >= maxValCount) {
            for (int ii = 0; ii < len; ii++) {
                List<Integer> possiPosi = possibilidades.get(ii);
                if (!pos.equals(ii) && possiPosi.remove(bestVal) && possiPosi.isEmpty()) {
                    if (arr[ii] == null) {
//                        System.out.println(" Posição: " + ii + " esvaziada por estouro de count valor " + bestVal + " usado na posição " + pos + " faltando " + remainPositions.size() + " posições");
                        ret = true;
                    }
                }
            }
        }
        for (int ii = 0; ii < posicoesExcluidas.size(); ii++) {
            Integer posicao = posicoesExcluidas.get(ii);
            if (!pos.equals(posicao) && arr[posicao] == null) {
                List<Integer> possiPosi = possibilidades.get(posicao);
                if (possiPosi.remove(bestVal) && possiPosi.isEmpty()) {
//                    System.out.println("Posição: " + posicao + " esvaziada por valor " + bestVal + " na posição " + pos + " faltando " + remainPositions.size() + " posições");
                    ret = true;
                }
            }
        }
        return ret;
    }

    private static void initVars(int ko, Map<Integer, Integer> countval, int len, Map<Integer, List<Integer>> possibilidades, Integer[] startArray, Integer[] arr, Map<Integer, List<Integer>> mapExcludePosition, int maxValCount) {
        Integer[] targetv = new Integer[ko];
        for (int j = 0; j < ko; j++) {
            targetv[j] = j;
            countval.put(j, 0);
        }
        List<Integer> targetvList = Arrays.asList(targetv);

        for (int i = 0; i < len; i++) {
            if (!possibilidades.containsKey(i)) {
                possibilidades.put(i, new LinkedList<>(targetvList));
            } else {
                List<Integer> get = possibilidades.get(i);
                get.clear();
                get.addAll(targetvList);
            }
        }

        for (int i = 0; i < startArray.length; i++) {
            arr[i] = startArray[i];
            List<Integer> posicoesExcluidas = mapExcludePosition.get(i);
            clearEmptyCombination(i, arr[i], countval, maxValCount, possibilidades, posicoesExcluidas);
        }
    }

    private static boolean clearEmptyCombination(Integer pos, Integer val, Map<Integer, Integer> countval, int maxValCount, Map<Integer, List<Integer>> possibilidades, List<Integer> posicoesExcluidas) {
        return clearEmptyCombination(pos, val, countval, maxValCount, possibilidades, posicoesExcluidas, true);
    }

    private static boolean clearEmptyCombination(Integer pos, Integer val, Map<Integer, Integer> countval, int maxValCount, Map<Integer, List<Integer>> possibilidades, List<Integer> posicoesExcluidas, boolean failEmpty) {
        return clearEmptyCombination(pos, val, countval, maxValCount, possibilidades, posicoesExcluidas, failEmpty, false);
    }

    private static boolean clearEmptyCombination(Integer pos, Integer val, Map<Integer, Integer> countval, int max_val_count, Map<Integer, List<Integer>> possibilidades, List<Integer> posicoesExcluidas, boolean failEmpty, boolean backward) {
        boolean roolback = false;
        int len = possibilidades.size();
        int divPoint = Collections.binarySearch(posicoesExcluidas, pos);
        if (backward) {
            divPoint = 0;
        }
        if (divPoint < 0) {
            divPoint = (-(divPoint) - 1);
        }
        Integer cur = countval.get(val);
        countval.put(val, cur + 1);
        // Remover val das futuras listas de possibilidade
        if (countval.get(val) >= max_val_count) {
            for (int ii = pos; ii < len; ii++) {
                List<Integer> possiPosi = possibilidades.get(ii);
                possiPosi.remove(val);
                if (failEmpty && possiPosi.isEmpty()) {
                    roolback = true;
                    break;
                }
            }
        }
        for (int ii = divPoint; ii < posicoesExcluidas.size(); ii++) {
            Integer posicao = posicoesExcluidas.get(ii);
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
}

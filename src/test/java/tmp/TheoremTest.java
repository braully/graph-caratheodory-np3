/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

import junit.framework.TestCase;

/**
 *
 * @author strike
 */
public class TheoremTest extends TestCase {

    public void testTeorema() {
        int[][] grafos = new int[][]{{5, 2, 0, 1}, {10, 3, 0, 1}, {16, 5, 0, 2}, {16, 6, 2, 2},
        {50, 7, 0, 1}, {56, 10, 0, 2}, {81, 20, 1, 6}, {77, 16, 0, 4}, {100, 18, 8, 2}, {100, 22, 0, 6}, {112, 30, 2, 10}, {231, 30, 9, 3}, {243, 22, 1, 2}, {275, 112, 30, 56},
        {651, 90, 33, 9}, {416, 100, 36, 20}, {729, 112, 1, 20}};

        for (int i = 0; i < grafos.length; i++) {
            int[] grafo = grafos[i];
            int t1 = teorme1(grafo);
            int t2 = teorme2(grafo);
            int t3 = teorme3(grafo);
            int t4 = teorme4(grafo);

            System.out.print("Resultado teoremas: ");
            System.out.print(" & ");
            System.out.print(t1);
            System.out.print(" & ");
            System.out.print(t2);
            System.out.print(" & ");
            System.out.print(t3);
            System.out.print(" & ");
            System.out.print(t4);
            System.out.println();
        }
    }

    int teorme1(int[] grafo) {
        return grafo[1] + 1;
    }

    int teorme2(int[] grafo) {
        int h = 0;
        int delta = grafo[1];
        h = logceiling(delta + 1, 2) + 1;
        return h;
    }

    int teorme3(int[] grafo) {
        int k = grafo[1];
        int b = grafo[2];
        int h = (int) (Math.round((double) k / (double) (b + 1)) + 1);
        return h;
    }

    int teorme4(int[] grafo) {
        int h = 0;
        int k = grafo[1];
        int c = grafo[3];
        h = logceiling(k * c + 1, (c + 1)) + 1;
        return h;
    }

    static int logceiling(int x, int base) {
        return (int) Math.round(Math.log(x) / Math.log(base));
    }
}

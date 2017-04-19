package tmp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/* 
 * From Roshan Piyush  https://github.com/piyushroshan/GraphTheoryProject
 */
public class MaxTriangleFreeGraph {

    public static void main(String... args) {
        MaxTriangleFreeGraph g = new MaxTriangleFreeGraph();
        g.generateGraph(4);
//        g.getGraphIndex(4, 1);
    }

    public MaxTriangleFreeGraph() {
    }

    /**
     * The number of vertices.
     */
    int n;

    /**
     * The max number of vertices expected.
     */
    final int MAX = 10;

    /**
     * The max number of graphs expected.
     */
    final int MAXF = 10000;

    /**
     * The triangle-free graphs.
     */
    int[][][] tf = new int[MAXF][MAX][MAX];

    /**
     * The count of triangle-free graphs.
     */
    int cTF;

    /**
     * The filename to store output.
     */
    final String filename = "maxTriangleFreeGraph.txt";

    Isomorphism isomrphc;

    FileOutput fileOut;

    /**
     * Instantiates a new max triangle free graph.
     *
     * @param n the number of vertices
     */
    public void generateGraph(int n) {
        this.n = n;
        cTF = 0;
        isomrphc = new Isomorphism(n);
        fileOut = new FileOutput(filename);
        fileOut.writeTextFile("Maximal-Triangle-Free Graphs");

        /**
         * Adds the first edge and initiates the addEdge().
         */
        int[][] a = new int[n][n];
        int[] vSet = new int[n];
        a[0][1] = 1;
        a[1][0] = 1;
        vSet[1] = 1;
        vSet[2] = 1;
        addEdge(a, vSet, 2);
        fileOut.writeTextFile("No of Maximal-Triangle-Free Graphs of " + n + " vertices = " + cTF + "\n");
        System.out.println("No of Maximal-Triangle-Free Graphs of " + n + " vertices = " + cTF + "\n");
    }

    /**
     * Adds an edge to the graph a.
     *
     * @param graphProcessing the graph under processing
     * @param vSet the set of vertices processed
     * @param cV the count of vertices processed
     */
    void addEdge(int[][] graphProcessing, int[] vSet, int cV) {
        if (cV < n) {
            processAddEdge(graphProcessing, vSet, cV);
        } else if (cV == n) {
            processEndAddEdge(graphProcessing, vSet, cV);
        }
    }

    public void processAddEdge(int[][] graphProcessing, int[] vSet, int cV) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (i != j && graphProcessing[i][j] == 0
                        && ((vSet[i] == 0 && vSet[j] == 1) || (vSet[i] == 1 && vSet[j] == 0))) {
                    int[][] graphCopy = new int[n][n];
                    for (int ii = 0; ii < n; ii++) {
                        System.arraycopy(graphProcessing[ii], 0, graphCopy[ii], 0, n);
                    }
                    graphCopy[i][j] = 1;
                    graphCopy[j][i] = 1;
                    int oldi = vSet[i];
                    int oldj = vSet[j];
                    vSet[i] = 1;
                    vSet[j] = 1;

                    if (isTriangleFree(graphCopy, n)) {
                        int[] vSetCopy = new int[n];
                        System.arraycopy(vSet, 0, vSetCopy, 0, vSetCopy.length);
                        addEdge(graphCopy, vSetCopy, cV + vSet[i] - oldi + vSet[j] - oldj);
                    }
                    vSet[i] = oldi;
                    vSet[j] = oldj;
                }
            }
        }
    }

    public void processEndAddEdge(int[][] graphProcessing, int[] vSet, int cV) {
        boolean flag = true;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                if (i != j && graphProcessing[i][j] == 0 && vSet[i] == 1 && vSet[j] == 1) {
                    int[][] grpahCopy = new int[n][n];
                    for (int ii = 0; ii < n; ii++) {
                        System.arraycopy(graphProcessing[ii], 0, grpahCopy[ii], 0, n);
                    }
                    grpahCopy[i][j] = 1;
                    grpahCopy[j][i] = 1;
                    if (isTriangleFree(grpahCopy, n)) {
                        flag = false;
                        int[] vSetCopy = new int[n];
                        System.arraycopy(vSet, 0, vSetCopy, 0, vSetCopy.length);
                        addEdge(grpahCopy, vSetCopy, cV);
                    }
                }
            }
        }
        if (flag) {
            addGraph(graphProcessing, n);
        }
    }

    /**
     * Adds the graph to tf.
     *
     * @param a the candidate triangle-free graph formed
     * @param n the number of vertices
     */
    void addGraph(int[][] a, int n) {
        if (cTF == 0) {
            for (int i = 0; i < n; i++) {
                System.arraycopy(a[i], 0, tf[cTF][i], 0, n);
            }
            cTF++;
            fileOut.writeTextFile(a, n);
            return;
        }

        for (int c = 0; c < cTF; c++) {
            int sum = 0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < i; j++) {
                    if (tf[c][i][j] == a[i][j]) {
                        sum++;
                    }
                }
            }
            if (sum == (n * n - n) / 2) {
                return;
            }
        }

        for (int c = 0; c < cTF; c++) {
            int cTf = 0, cA = 0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < i; j++) {
                    if (tf[c][i][j] == 1) {
                        cTf++;
                    }
                    if (a[i][j] == 1) {
                        cA++;
                    }
                }
            }
            if (cTf != cA) {
                continue;
            }
            if (isomrphc.isIsomorphic(a, tf[c])) {
                return;
            }
        }
        for (int i = 0; i < n; i++) {
            System.arraycopy(a[i], 0, tf[cTF][i], 0, n);
        }
        cTF++;
        fileOut.writeTextFile(a, n);
        return;
    }

    /**
     * Checks if a graph is triangle free.
     *
     * @param a the graph to be checked
     * @param n the number of vertices
     * @return true if it is triangle free
     */
    Boolean isTriangleFree(int[][] a, int n) {
        int[][] x = new int[n][n];
        int[][] y = new int[n][n];
        int trace = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int sum = 0;
                for (int k = 0; k < n; k++) {
                    sum = sum + a[i][k] * a[k][j];
                }
                x[i][j] = sum;
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int sum = 0;
                for (int k = 0; k < n; k++) {
                    sum = sum + x[i][k] * a[k][j];
                }
                y[i][j] = sum;
            }
        }

        for (int i = 0; i < n; i++) {
            trace += y[i][i];
        }
        if (trace == 0) {
            return true;
        } else {
            return false;
        }
    }
}

class Isomorphism {

    /**
     * The max expected permutations.
     */
    final int MAX_P = 10000;

    /**
     * The max expected numbers to be permuted.
     */
    final int MAX = 10;

    /**
     * The count of permutations.
     */
    int cP;

    /**
     * The particular permutation of I.
     */
    int[][] P = new int[MAX][MAX];

    /**
     * The transpose of P.
     */
    int[][] PT = new int[MAX][MAX];

    /**
     * The unit matrix I.
     */
    int[][] I = new int[MAX][MAX];

    /**
     * The number of vertices.
     */
    int n;

    /**
     * The permutations.
     */
    int[][] permutations = new int[MAX_P][MAX];

    /**
     * Instantiates a new isomorphism.
     *
     * @param n the n
     */
    Isomorphism(int n) {
        this.n = n;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    I[i][j] = 1;
                } else {
                    I[i][j] = 0;
                }
            }
        }

        int[] array = new int[n];
        for (int i = 0; i < n; i++) {
            array[i] = i + 1;
        }
        permuteVertex(array, 0, n);
    }

    /**
     * Permute vertex.
     *
     * @param array the array to store all the permutations of 1..n
     * @param l the index of array till which numbers are permuted
     * @param h the number of items to be permuted
     */
    public void permuteVertex(int[] array, int l, int h) {
        int temp;
        boolean skip;
        if (l == h) {
            System.arraycopy(array, 0, permutations[cP++], 0, l);
        } else {
            for (int i = l; i < h; i++) {
                skip = false;
                for (int j = l; j < i; j++) {
                    if (array[j] == array[i]) {
                        skip = true;
                        break;
                    }
                }

                if (!skip) {
                    temp = array[l];
                    array[l] = array[i];
                    array[i] = temp;
                    permuteVertex(array, l + 1, h);
                    temp = array[l];
                    array[l] = array[i];
                    array[i] = temp;
                }
            }
        }
    }

    /**
     * Checks if two graphs are isomorphic.
     *
     * @param a the input graph
     * @param b the graph to be transformed by permutation
     * @return true, if the graphs are isomorphic
     */
    boolean isIsomorphic(int[][] a, int[][] b) {
        for (int w = 0; w < cP; w++) {
            int cE = 0;
            for (int i = 0; i < n; i++) {
                System.arraycopy(I[permutations[w][i] - 1], 0, P[i], 0, n);
            }

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    PT[j][i] = P[i][j];
                }
            }

            int[][] temp = new int[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    temp[i][j] = 0;
                    for (int k = 0; k < n; k++) {
                        temp[i][j] += P[i][k] * b[k][j];
                    }
                }
            }

            int[][] tempIsomorphic = new int[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    tempIsomorphic[i][j] = 0;
                    for (int k = 0; k < n; k++) {
                        tempIsomorphic[i][j] += temp[i][k] * PT[k][j];
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < i; j++) {
                    if (a[i][j] == tempIsomorphic[i][j]) {
                        cE++;
                    } else {
                        i = n;
                        j = n;
                    }
                }
            }

            if (cE == n * (n - 1) / 2) {
                return true;
            }
        }
        return false;
    }
}

class FileOutput {

    /**
     * The output filename.
     */
    String filename;

    private PrintWriter writer = null;

    /**
     * Instantiates a new file output.
     *
     * @param filename the output file name
     */
    public FileOutput(String filename) {
        this.filename = filename;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));
            System.out.println("Created\t\"" + filename + "\" sucessfully");
        } catch (IOException e) {
            System.out.println("Could not create\t" + filename);
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            writer.close();
        }

    }

    /**
     * Printing string to file.
     *
     * @param s the output string to print
     */
    public void writeTextFile(String s) {
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
            writer.println(s);
        } catch (IOException e) {
            System.out.println("Could not write to\t\"" + filename + "\"");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            writer.close();
        }
    }

    /**
     * Printing graph to file.
     *
     * @param a the graph to print
     * @param n the number of vertices in graph
     */
    public void writeTextFile(int[][] a, int n) {
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
            writer.print("_|_");
            for (int i = 0; i < n; i++) {
                writer.print(i + 1 + "_");
            }
            writer.println();
            for (int i = 0; i < n; i++) {
                writer.print(i + 1 + "| ");
                for (int j = 0; j < n; j++) {
                    writer.print(a[i][j] + " ");
                }
                writer.println();
            }
            writer.println();
        } catch (IOException e) {
            System.out.println("Could not write to\t\"" + filename + "\"");
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            writer.close();
        }

    }

    /**
     * Close file.
     */
    public void closeFile() {
        writer.close();
    }
}

package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author strike
 */
public class UtilTmp {

    public static final String fileDump = "/home/strike/.comb-moore-java-tmp.txt";
    public static final int max_length_file = 3000;
    public static final long ALERT_HOUR = 1000 * 60 * 60 * 1;
    public static final long ALERT_HOUR_6 = 1000 * 60 * 60 * 6;
    public static final long ALERT_HOUR_12 = 1000 * 60 * 60 * 12;
    private static Queue<Integer> queue = new LinkedList<Integer>();

    public static void printArray(Integer[] arr) {
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

    public static void printArray2Mask(Integer[] arr) {
        int len = arr.length;
        System.out.print("[");
        for (int i = 0; i < len; i++) {
            System.out.printf("%2d", arr[i]);
            if (i < len - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

    public static void printArray(int[] arr) {
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

    static void printArrayUntil0(int[] arr) {
        int len = arr.length;
        System.out.print("[");
        for (int i = 0; i < len; i++) {
            if (arr[i] == 0) {
                break;
            }
            System.out.print(arr[i]);
            if (i < len - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

    public static List<Integer> args2intlist(String... args) {
        List<Integer> start = new ArrayList<>();
        if (args != null && args.length > 0) {
            if (args.length > 1 || ((args = args[0].split(",")) != null)) {
                for (String str : args) {
                    start.add(Integer.parseInt(str.trim().replaceAll("\\D", "")));
                }
            }
        }
        return start;
    }

    public static int[] args2intarr(String... args) {
        List<Integer> start = args2intlist(args);
        int[] ret = new int[start.size()];
        for (int i = 0; i < start.size(); i++) {
            ret[i] = start.get(i);
        }
        return ret;
    }

    public static int indexOf(int needle, int[] haystack) {
        for (int i = 0; i < haystack.length; i++) {
            if (haystack[i] == needle) {
                return i;
            }
        }
        return -1;
    }

    public static void dumpString(String strt) {
        System.out.print("Dump-str: ");
        System.out.println(strt);
        try {
            FileWriter fileWriter = new FileWriter(fileDump, true);
            if (strt.length() > max_length_file) {
                int length = strt.length();
                for (int i = 0; i < length; i += max_length_file) {
                    fileWriter.append(strt.substring(i, Math.min(length, i + max_length_file))).append("\n");
                }
            } else {
                fileWriter.append(strt);
            }
            fileWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void dumpOverrideString(String strt, String fileoffset) {
        System.out.print("Dump-str: ");
        System.out.println(strt);
        try {
            FileWriter fileWriter = new FileWriter(fileDump + fileoffset);
            if (strt.length() > max_length_file) {
                int length = strt.length();
                for (int i = 0; i < length; i += max_length_file) {
                    fileWriter.append(strt.substring(i, Math.min(length, i + max_length_file))).append("\n");
                }
            } else {
                fileWriter.append(strt);
            }
            fileWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void dumpString(String strt, String fileoffset) {
        System.out.print("Dump-str: ");
        System.out.println(strt);
        try {
            FileWriter fileWriter = new FileWriter(fileDump + fileoffset, true);
            if (strt.length() > max_length_file) {
                int length = strt.length();
                for (int i = 0; i < length; i += max_length_file) {
                    fileWriter.append(strt.substring(i, Math.min(length, i + max_length_file))).append("\n");
                }
            } else {
                fileWriter.append(strt);
            }
            fileWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void dumpArray(Collection arr) {
        dumpArray(arr, null);
    }

    public static void dumpArray(Collection arr, String preset) {
        String strArra = "h-arr[" + arr.size() + "]: " + arr.toString() + "\n";
        try {
            new FileWriter(fileDump, true).append(strArra).close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void dumpArrayUntil0(int[] arr) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("h-arr[");
            sb.append(arr.length).append("]: ");
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] > 0) {
                    sb.append(arr[i]);
                    sb.append(", ");
                }
            }
            sb.append("\n");
            dumpString(sb.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void bfs(UndirectedSparseGraphTO<Integer, Integer> subgraph, Integer[] bfs, Integer v) {
        for (int i = 0; i < bfs.length; i++) {
            bfs[i] = null;
        }
        bfs[v] = 0;
        visitVertex(v, bfs, subgraph);
    }

    public static void visitVertex(Integer v, Integer[] bfs, UndirectedSparseGraphTO<Integer, Integer> subgraph1) {
        queue.clear();
        queue.add(v);
        while (!queue.isEmpty()) {
            Integer poll = queue.poll();
            int depth = bfs[poll] + 1;
            Collection<Integer> ns = (Collection<Integer>) subgraph1.getNeighborsUnprotected(poll);
            for (Integer nv : ns) {
                if (bfs[nv] == null) {
                    bfs[nv] = depth;
                    queue.add(nv);
                } else if (depth < bfs[nv]) {//revisit
                    bfs[nv] = depth;
                    queue.add(nv);
                }
            }
        }
    }

    static void revisitVertex(Integer hold, Integer[] bfs3, UndirectedSparseGraphTO<Integer, Integer> subgraph) {
        if (hold == null || bfs3[hold] != 0) {
            throw new IllegalStateException("BFS From another root");
        }
        visitVertex(hold, bfs3, subgraph);
    }

    static void arrayCopy(Integer[] bfs, Integer[] bfsBackup) {
        for (int i = 0; i < bfs.length; i++) {
            bfsBackup[i] = bfs[i];
        }
    }

    static void saveToCache(Object o, String caminho) {
        try {
            FileOutputStream fos = new FileOutputStream(new File(caminho));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Object loadFromCache(String objectdat) {
        Object ret = null;
        try {
            InputStream is = null;
            try {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(objectdat);
            } catch (Exception e) {

            }
            if (is == null) {
                is = new FileInputStream("/home/strike/" + objectdat);
            }
            ObjectInputStream iis = new ObjectInputStream(is);
            ret = iis.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    static void dumpVertAddArray(UndirectedSparseGraphTO lastgraph, int numArestasIniciais) {
        System.out.print("Dump-edge: ");
        try {
            FileWriter fileWriter = new FileWriter(fileDump, true);
            int charcount = max_length_file;
            System.out.print("vert-add: ");
            fileWriter.append("vert-add: ");
            for (int i = numArestasIniciais; i < lastgraph.getEdgeCount(); i++) {
                String str = String.format("%d, ", lastgraph.getEndpoints(i).getFirst());
                System.out.printf(str);
                fileWriter.append(str);
                charcount = charcount - str.length();
                if (charcount <= 0) {
                    charcount = max_length_file;
                    fileWriter.append("\n");
                }
            }
            System.out.println(" | ");
            fileWriter.append(" | ");
            for (int i = numArestasIniciais; i < lastgraph.getEdgeCount(); i++) {
                String str = String.format("%d, ", lastgraph.getEndpoints(i).getSecond());
                System.out.printf(str);
                fileWriter.append(str);
                charcount = charcount - str.length();
                if (charcount <= 0) {
                    charcount = max_length_file;
                    fileWriter.append("\n");
                }
            }
            System.out.println();
            fileWriter.append("\n");
            fileWriter.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}

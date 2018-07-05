package tmp;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author strike
 */
public class UtilTmp {

    public static final String fileDump = "/home/strike/.comb-moore-java-tmp.txt";
    public static final int max_length_file = 2000;
    public static final long ALERT_HOUR = 1000 * 60 * 60 * 12;

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
}

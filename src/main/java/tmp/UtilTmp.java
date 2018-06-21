package tmp;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author strike
 */
public class UtilTmp {

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
}

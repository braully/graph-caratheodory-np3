package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author braully
 */
public class BFSTest extends TestCase {

    public void testBFS() {
        UndirectedSparseGraphTO<Integer, Integer> subgraph = HoffmanGraphGen.subgraph.clone();
        Integer[] bfs = new Integer[subgraph.getVertexCount()];
        Collection vertices = subgraph.getVertices();
        List<Integer> incomplete = new ArrayList<>();
        for (Object v : vertices) {
            if (subgraph.degree(v) == 3) {
                incomplete.add((Integer) v);
            }
        }

//        for (Integer v : incomplete) {
//            UtilTmp.bfs(subgraph, bfs, v);
//            System.out.print("bfs[" + v + "]=");
//            UtilTmp.printArray(bfs);
//        }
    }

    public void testBFSAdd() {
        UndirectedSparseGraphTO<Integer, Integer> subgraph = HoffmanGraphGen.subgraph.clone();
        Integer[] bfs1 = new Integer[]{1, 2, 3, 3, 3, 3, 2, 1, 3, 3, 3, 3, 0, 2, 2, 3, 2, 3, 2, 3, 2, 3, 3, 2, 3, 2, 3, 2, 3, 2, 4, 4, 2, 2, 4, 4, 4, 4, 2, 2, 4, 4, 1, 3, 3, 3, 3, 2, 2, 2};
        Integer[] bfs2 = new Integer[subgraph.getVertexCount()];
        Integer[] bfs3 = new Integer[]{1, 2, 3, 3, 3, 3, 2, 1, 3, 3, 3, 3, 0, 2, 2, 3, 2, 3, 2, 3, 2, 3, 3, 2, 3, 2, 3, 2, 3, 2, 4, 4, 2, 2, 4, 4, 4, 4, 2, 2, 4, 4, 1, 3, 3, 3, 3, 2, 2, 2};

        Collection<Integer> vertices = subgraph.getVertices();
        List<Integer> incomplete = new ArrayList<>();
//        for (Object v : vertices) {
//            if (subgraph.degree(v) == 3) {
//                incomplete.add((Integer) v);
//            }
//        }
        System.out.print("Vertice = [");
        for (Integer v : vertices) {
            System.out.printf("%2d", v);
            System.out.print(", ");
        }
        System.out.println();

        System.out.print("bfs-0[" + 12 + "]=");
        UtilTmp.printArray2Mask(bfs1);

//        Integer[] novsArestas = new Integer[]{30, 35, 36, 40};
//        Integer hold = 12;
        Integer[] novsArestas = new Integer[]{39};
        Integer hold = 18;
        for (Integer a : novsArestas) {
            subgraph.addEdge(hold, a);
            UtilTmp.bfs(subgraph, bfs2, hold);
            UtilTmp.visitVertex(hold, bfs3, subgraph);
            UtilTmp.revisitVertex(hold, bfs3, subgraph);
            boolean ok = true;
            for (int i = 0; i < bfs2.length; i++) {
                if (!bfs2[i].equals(bfs3[i])) {
                    ok = false;
                }
            }
            if (ok) {
                System.out.println("Not problem for add (" + hold + ", " + a + ")");
            } else {
                System.out.println("Failed for e(" + hold + ", " + a + ")");
                System.out.print("bfs-2[" + hold + "]=");
                UtilTmp.printArray2Mask(bfs2);

                System.out.print("bfs-3[" + hold + "]=");
                UtilTmp.printArray2Mask(bfs3);
            }
        }
    }
}

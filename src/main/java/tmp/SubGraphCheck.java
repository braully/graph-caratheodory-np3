package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.apache.commons.collections4.iterators.PermutationIterator;

/**
 *
 * @author strike
 */
public class SubGraphCheck {

    private static final long HOUR = 1000 * 60 * 60 * 12;

    public static void main(String... args) throws FileNotFoundException, IOException {
        if (args.length < 2) {
            System.err.println("args: file-graphs.es file-subraph.es");
            return;
        }
        UndirectedSparseGraphTO graph = UtilGraph.loadGraphES(new FileInputStream(args[0]));
        UndirectedSparseGraphTO subgraph = UtilGraph.loadGraphES(new FileInputStream(args[1]));
        if (graph.getVertexCount() == 0 || graph.getEdgeCount() == 0 || subgraph.getVertexCount() == 0 || subgraph.getEdgeCount() == 0) {
            System.err.println("exists empty graph");
            return;
        }
        boolean found = false;

        Collection vertices = graph.getVertices();
        PermutationIterator combination = new PermutationIterator(vertices);
        List next = null;
        long lastime = System.currentTimeMillis();

        System.out.print("Checking graph ");
        System.out.print(args[0]);
        System.out.print(" as ");
        System.out.println(args[1]);

        System.out.print("graph: ");
        System.out.println(graph);

        System.out.print("subgraph: ");
        System.out.println(subgraph);

        while (combination.hasNext() && !found) {
            next = combination.next();
            found = graph.containStrict(subgraph, next);
            if (System.currentTimeMillis() - lastime > HOUR) {
                lastime = System.currentTimeMillis();
                System.out.print("h-");
                System.out.println(next);
            }
        }
        if (found) {
            System.out.println("Found maped subgraph isomorphic");
            System.out.println(next);
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author strike
 */
public class BFSTmp {

    Integer[] bfs = null;
    private Queue<Integer> queue = null;

    BFSTmp(int size) {
        bfs = new Integer[size];
        queue = new LinkedList<Integer>();
    }

    void labelDistances(UndirectedSparseGraphTO graphTemplate, Integer v) {
        bfs(graphTemplate, bfs, v);
    }

    Integer getDistance(UndirectedSparseGraphTO graphTemplate, Integer u) {
        return bfs[u];
    }

    void bfs(UndirectedSparseGraphTO<Integer, Integer> subgraph, Integer[] bfs, Integer v) {
        for (int i = 0; i < bfs.length; i++) {
            bfs[i] = null;
        }
        bfs[v] = 0;
        visitVertex(v, bfs, subgraph);
    }

    void visitVertex(Integer v, Integer[] bfs, UndirectedSparseGraphTO<Integer, Integer> subgraph1) {
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

}

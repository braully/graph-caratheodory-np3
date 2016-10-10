/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.hn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author strike
 */
public class UtilGraph {

    public static synchronized String saveTmpFileGraphInCsr(UndirectedSparseGraphTO<Integer, Integer> undGraph) {
        String strFile = null;
        if (undGraph != null && undGraph.getVertexCount() > 0) {
            try {
                int vertexCount = undGraph.getVertexCount();
                File file = File.createTempFile("graph-csr-", ".txt");
                file.deleteOnExit();

                strFile = file.getAbsolutePath();
                FileWriter writer = new FileWriter(file);
                writer.write("#Graph |V| = " + vertexCount + "\n");

                int sizeRowOffset = 0;
                List<Integer> csrColIdxs = new ArrayList<>();
                List<Integer> rowOffset = new ArrayList<>();

                int idx = 0;
                for (Integer i = 0; i < vertexCount; i++) {
                    csrColIdxs.add(idx);
                    Collection<Integer> neighbors = undGraph.getNeighbors(i);
                    Set<Integer> neighSet = new HashSet<>();
                    neighSet.addAll(neighbors);
                    for (Integer vn : neighSet) {
                        if (!vn.equals(i)) {
                            rowOffset.add(vn);
                            idx++;
                        }
                    }
                }
                csrColIdxs.add(idx);

                for (Integer i : csrColIdxs) {
                    writer.write("" + i);
                    writer.write(" ");
                }
                writer.write("\n");
                for (Integer i : rowOffset) {
                    writer.write("" + i);
                    writer.write(" ");
                }
                writer.write("\n");
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(GraphWS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Logger.getLogger(GraphWS.class.getName()).log(Level.INFO, "File tmp graph: " + strFile);
        return strFile;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Graphs utils
 *
 * @author Braully
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
                writerGraphToCsr(writer, undGraph);
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(GraphWS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Logger.getLogger(GraphWS.class.getName()).log(Level.INFO, "File tmp graph: " + strFile);
        return strFile;
    }

    public static synchronized void writerGraphToCsr(Writer writer, UndirectedSparseGraphTO<Integer, Integer> undGraph) throws IOException {
        if (undGraph == null || writer == null) {
            return;
        }
        int vertexCount = undGraph.getVertexCount();
        writer.write("#Graph |V| = " + vertexCount + "\n");

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
    }

    public static synchronized List<Integer> csrColIdxs(UndirectedSparseGraphTO<Integer, Integer> undGraph) {
        if (undGraph == null) {
            return null;
        }
        int vertexCount = undGraph.getVertexCount();
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
        return csrColIdxs;
    }

    public static synchronized List<Integer> rowOffset(UndirectedSparseGraphTO<Integer, Integer> undGraph) {
        if (undGraph == null) {
            return null;
        }
        int vertexCount = undGraph.getVertexCount();
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
        return rowOffset;
    }

    static String saveTmpFileGraphInAdjMatrix(UndirectedSparseGraphTO<Integer, Integer> graph) {
        String strFile = null;
        if (graph != null && graph.getVertexCount() > 0) {
            try {
                int vertexCount = graph.getVertexCount();
                File file = File.createTempFile("graph-csr-", ".txt");
                file.deleteOnExit();

                strFile = file.getAbsolutePath();
                FileWriter writer = new FileWriter(file);
                writerGraphToAdjMatrix(writer, graph);
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(GraphWS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Logger.getLogger(GraphWS.class.getName()).log(Level.INFO, "File tmp graph: " + strFile);
        return strFile;
    }

    public static synchronized void writerGraphToAdjMatrix(Writer writer, UndirectedSparseGraphTO<Integer, Integer> undGraph) throws IOException {
        if (undGraph == null || writer == null) {
            return;
        }
        int vertexCount = undGraph.getVertexCount();
//        writer.write("#Graph |V| = " + vertexCount + "\n");

        for (Integer i = 0; i < vertexCount; i++) {
            Collection<Integer> neighbors = undGraph.getNeighbors(i);
            for (Integer j = 0; j < vertexCount; j++) {
                if (neighbors.contains(j)) {
                    writer.write("1");
                } else {
                    writer.write("0");
                }
                if (j < vertexCount - 1) {
                    writer.write(" ");
                }
            }
            if (i < vertexCount - 1) {
                writer.write("\n");
            }
        }
        writer.write("\n");
    }

    static UndirectedSparseGraphTO<Integer, Integer> loadGraphCsr(InputStream uploadedInputStream) throws IOException {
        UndirectedSparseGraphTO<Integer, Integer> ret = null;
        if (uploadedInputStream != null) {
            BufferedReader r = new BufferedReader(new InputStreamReader(uploadedInputStream));
            String csrColIdxsStr = null;
            String rowOffsetStr = null;

            String readLine = null;
            while ((readLine = r.readLine()) == null || readLine.trim().isEmpty() || readLine.trim().startsWith("#")) {
            }
            csrColIdxsStr = readLine;
            while ((readLine = r.readLine()) == null || readLine.trim().isEmpty() || readLine.trim().startsWith("#")) {
            }
            rowOffsetStr = readLine;
//            System.out.println("CsrColIdxs: " + csrColIdxsStr);
//            System.out.println("RowOffset: " + rowOffsetStr);
        }
        return ret;
    }

    static UndirectedSparseGraphTO<Integer, Integer> loadGraphAdjMatrix(InputStream uploadedInputStream) throws IOException {
        UndirectedSparseGraphTO<Integer, Integer> ret = null;
        if (uploadedInputStream != null) {
            BufferedReader r = new BufferedReader(new InputStreamReader(uploadedInputStream));
            List<String> lines = new ArrayList<>();
            String readLine = null;
            Integer verticeCount = 0;
            ret = new UndirectedSparseGraphTO<>();
            while ((readLine = r.readLine()) != null) {
                if (!readLine.trim().isEmpty() && !readLine.trim().startsWith("#")) {
                    lines.add(readLine);
                    System.out.println(readLine);
                    ret.addVertex(verticeCount);
                    verticeCount = verticeCount + 1;
                }
            }
            int edgeCount = 0;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line != null) {
                    String[] split = line.split(" ");
                    if (split != null) {
                        for (int j = 0; j < split.length; j++) {
                            if ("1".equals(split[j])) {
                                ret.addEdge(edgeCount++, i, j);
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }
}

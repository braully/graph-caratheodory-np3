/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.braully.graph.operation.IGraphOperation;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author braully
 */
public class DatabaseFacade {

    private static final String DATABASE_DIRECTORY = System.getProperty("user.home") + File.separator + "." + "graph-problem";
    private static final String DATABASE_URL = DATABASE_DIRECTORY + File.separator + "graph-problem-results.json";

    static {
        try {
            new File(DATABASE_DIRECTORY).mkdirs();
        } catch (Exception e) {

        }
    }

    static class RecordResultGraph {

        String id, status, type, operation, graph, vertices, edges, results, date;

        public RecordResultGraph() {
        }

        public RecordResultGraph(String status, String type, String operation, String graph, String vertices, String edges, String results, String date) {
            this.status = status;
            this.type = type;
            this.operation = operation;
            this.graph = graph;
            this.vertices = vertices;
            this.edges = edges;
            this.results = results;
            this.date = date;
        }
    }

    public synchronized static List<RecordResultGraph> getAllResults() {
        List<RecordResultGraph> results = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            results = mapper.readValue(new File(DATABASE_URL), List.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public static synchronized void saveResult(UndirectedSparseGraphTO graph, IGraphOperation graphOperation, Map<String, Object> result) {
        if (result != null && graph != null && graphOperation != null) {
            try {

                List<RecordResultGraph> results = null;
                ObjectMapper mapper = new ObjectMapper();
                results = mapper.readValue(new File(DATABASE_URL), List.class);
                if (results == null) {
                    results = new ArrayList<RecordResultGraph>();
                }
                RecordResultGraph record = new RecordResultGraph();
                String fileGraph = saveGraph(graph);
                record.status = "ok";
                record.graph = fileGraph;
                record.edges = "" + graph.getEdgeCount();
                record.vertices = "" + graph.getVertexCount();
                record.operation = graphOperation.getName();
                record.type = graphOperation.getTypeProblem();
                record.date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                record.results = resultMapToString(result);
                record.id = "" + results.size();
                results.add(record);
                mapper.writeValue(new File(DATABASE_URL), results);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String saveGraph(UndirectedSparseGraphTO graph) {
        if (graph == null) {
            return null;
        }
        String fileName = graph.getName();

        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        fileName = fileName + "-" + sb.toString() + ".json";

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(DATABASE_DIRECTORY + File.separator + fileName), graph);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    private static String resultMapToString(Map<String, Object> result) {
        StringBuilder strResult = new StringBuilder();
        if (result != null) {
            Set<String> keySet = result.keySet();
            List<String> keyList = new ArrayList<String>(keySet);
            Collections.sort(keyList);
            for (String strKey : keyList) {
                Object obj = result.get(strKey);
                strResult.append(strKey);
                strResult.append(": ");
                strResult.append(objectTosString(obj));
                strResult.append("\n");
            }
        }
        return strResult.toString();
    }

    private static String objectTosString(Object obj) {
        String ret = null;
        if (obj != null) {
            ret = obj.toString();
        }
        return ret;
    }
}

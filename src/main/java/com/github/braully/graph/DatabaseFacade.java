/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.braully.graph.operation.IGraphOperation;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
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

    static class RecordResultGraph implements java.lang.Comparable<RecordResultGraph> {

        String id, status, type, operation, graph, name, vertices, edges, results, date;

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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getGraph() {
            return graph;
        }

        public void setGraph(String graph) {
            this.graph = graph;
        }

        public String getVertices() {
            return vertices;
        }

        public void setVertices(String vertices) {
            this.vertices = vertices;
        }

        public String getEdges() {
            return edges;
        }

        public void setEdges(String edges) {
            this.edges = edges;
        }

        public String getResults() {
            return results;
        }

        public void setResults(String results) {
            this.results = results;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        @Override
        public int compareTo(RecordResultGraph t) {
            if (t != null && id != null) {
                return id.compareToIgnoreCase(t.id);
            }
            return 0;
        }

    }

    public synchronized static List<RecordResultGraph> getAllResults() {
        List<RecordResultGraph> results = new ArrayList();
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<RecordResultGraph> tmp = results = mapper.readValue(new File(DATABASE_URL), List.class);
            if (tmp != null) {
                try {
                    for (RecordResultGraph t : tmp) {
                        results.add(t);
                    }
                } catch (ClassCastException e) { }
            }
            Collections.reverse(results);
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
                try {
                    results = mapper.readValue(new File(DATABASE_URL), List.class);
                } catch (Exception e) {

                }
                if (results == null) {
                    results = new ArrayList<RecordResultGraph>();
                }
                RecordResultGraph record = new RecordResultGraph();
                String fileGraph = saveGraph(graph);
                record.status = "ok";
                record.graph = fileGraph;
                record.name = graph.getName();
                record.edges = "" + graph.getEdgeCount();
                record.vertices = "" + graph.getVertexCount();
                record.operation = graphOperation.getName();
                record.type = graphOperation.getTypeProblem();
                record.date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                record.results = resultMapToString(result);
                record.id = "" + results.size();
                results.add(record);
                mapper.writeValue(new File(DATABASE_URL), results);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String saveGraph(UndirectedSparseGraphTO graph) {
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

    public static UndirectedSparseGraphTO openGraph(String nameFile) throws IOException {
        if (nameFile == null) {
            return null;
        }
        UndirectedSparseGraphTO graph = null;
        ObjectMapper mapper = new ObjectMapper();
        graph = mapper.readValue(new File(DATABASE_DIRECTORY + File.separator + nameFile), UndirectedSparseGraphTO.class);
        return graph;
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

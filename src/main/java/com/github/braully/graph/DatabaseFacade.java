/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.h2.jdbcx.JdbcConnectionPool;

/**
 *
 * @author braully
 */
public class DatabaseFacade {

    private static final String DATABASE_DIRECTORY = System.getProperty("user.home") + File.separator + "." + "graph-problem";
    private static final String DATABASE_URL = "jdbc:h2:" + DATABASE_DIRECTORY;
    private static final String PASSWORD = "";
    private static final String USER = "";

    private static final JdbcConnectionPool CONNECTION_POOL;

    static {
        new File(DATABASE_DIRECTORY).mkdirs();
        CONNECTION_POOL = JdbcConnectionPool.create(DATABASE_URL, USER, PASSWORD);
    }

    static class RecordResultGraph {

        String status, type, operation, graph, vertices, edges, results, date;

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

    public static List getAllResults() {
        return null;
    }

    static void saveResult(UndirectedSparseGraphTO graph, Map<String, Object> result) {

    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author strike
 */
public class GraphCalcCaratheodoryNumberParallel extends GraphCheckCaratheodorySet {

    private static final Logger log = Logger.getLogger(GraphCalcCaratheodoryNumberParallel.class.getName());

    private static final String COMMAND_GRAPH_HN = System.getProperty("user.home") + File.separator + "graph-caratheodory-np3.sh";

    private static final Pattern PATERN_CARATHEODORY_SET = Pattern.compile(".*?Combination: \\{([0-9, ]+)\\}.*?");
    private static final Pattern PATERN_CARATHEODORY_NUMBER = Pattern.compile(".*?S\\| = ([0-9]+).*?");
    private static final Pattern PATERN_PARALLEL_TIME = Pattern.compile("Total time parallel: (\\w+)");

    static final String type = "P3-Convexity";
    static final String description = "Nº Caratheodory (CUDA)";

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Integer caractheodoryNumber = null;
        int[] caratheodorySet = null;
        Integer[] convexHull = null;
        int[] auxProcessor = null;
        Integer[] partial = null;
        String pTime = null;
        UndirectedSparseGraphTO<Integer, Integer> undGraph = null;

        try {
            String path = UtilGraph.saveTmpFileGraphInCsr(graph);

            String commandToExecute = COMMAND_GRAPH_HN + " -p " + path;
//            String commandToExecute = COMMAND_GRAPH_HN + " -s " + path;

            log.log(Level.INFO, "Command: {0}", commandToExecute);
            log.log(Level.INFO, "Executing");
            Process p = Runtime.getRuntime().exec(commandToExecute);
            p.waitFor();
            log.log(Level.INFO, "Executed");
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            log.log(Level.INFO, "Output");
            while ((line = reader.readLine()) != null) {
                log.log(Level.INFO, line);
                try {
                    if (caratheodorySet == null) {
                        caratheodorySet = parseCaratheodorySet(line);
                    }
                    if (caractheodoryNumber == null) {
                        caractheodoryNumber = parseCaratheodoryNumber(line);
                    }
                    if (pTime == null) {
                        pTime = parseParallelTime(line);
                    }
                } catch (Exception e) {
                    log.log(Level.WARNING, "", e);
                }
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            log.log(Level.SEVERE, null, ex);
        }

        OperationGraphResult caratheodoryNumberGraph = null;
        if (caratheodorySet != null && caratheodorySet.length > 0) {

            caratheodoryNumberGraph = hsp3(undGraph, caratheodorySet);
        }

        if (caratheodoryNumberGraph != null
                && !caratheodoryNumberGraph.caratheodorySet.isEmpty()) {
            auxProcessor = caratheodoryNumberGraph.auxProcessor;
            convexHull = caratheodoryNumberGraph.convexHull.toArray(new Integer[0]);
            partial = caratheodoryNumberGraph.partial.toArray(new Integer[0]);
        }

        Map<String, Object> response = new HashMap<>();

        response.put(PARAM_NAME_CARATHEODORY_NUMBER, caractheodoryNumber);
        response.put(PARAM_NAME_CARATHEODORY_SET, caratheodorySet);
        response.put(PARAM_NAME_CONVEX_HULL, convexHull);
        response.put(PARAM_NAME_AUX_PROCESS, auxProcessor);
        response.put(PARAM_NAME_TOTAL_TIME_MS, pTime);
        response.put(PARAM_NAME_PARTIAL_DERIVATED, partial);

        return response;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }

    int[] parseCaratheodorySet(String line) {
        int[] ret = null;
        Matcher m = PATERN_CARATHEODORY_SET.matcher(line);
        if (m.find()) {
            String[] split = m.group(1).split(",");
            if (split != null && split.length > 0) {
                ret = new int[split.length];
                for (int i = 0; i < split.length; i++) {
                    String st = split[i];
                    ret[i] = Integer.parseInt(st.trim());
                }
            }
        }
        return ret;
    }

    Integer parseCaratheodoryNumber(String line) {
        Integer ret = null;
        Matcher m = PATERN_CARATHEODORY_NUMBER.matcher(line);
        if (m.find()) {
            String trim = m.group();
            trim = m.group(1);
//            String trim = m.group();
            if (trim != null && !trim.isEmpty()) {
                ret = Integer.parseInt(trim.trim());
            }
        }
        return ret;
    }

    String parseParallelTime(String line) {
        String ret = null;
        Matcher m = PATERN_PARALLEL_TIME.matcher(line);
        if (m.find()) {
            ret = m.group(1);
        }
        return ret;
    }
}

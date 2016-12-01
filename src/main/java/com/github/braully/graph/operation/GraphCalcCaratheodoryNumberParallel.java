/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import static com.github.braully.graph.operation.OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_NUMBER;
import static com.github.braully.graph.operation.OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_SET;
import static com.github.braully.graph.operation.OperationConvexityGraphResult.PARAM_NAME_CONVEX_HULL;
import static com.github.braully.graph.operation.OperationConvexityGraphResult.PARAM_NAME_PARTIAL_DERIVATED;
import static com.github.braully.graph.operation.OperationConvexityGraphResult.PARAM_NAME_TOTAL_TIME_MS;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 *
 * @author strike
 */
public class GraphCalcCaratheodoryNumberParallel
        extends GraphCheckCaratheodorySet implements Interruptible {

    private static final Logger log = Logger.getLogger(GraphCalcCaratheodoryNumberParallel.class);

    private static final String COMMAND_GRAPH_HN = System.getProperty("user.home") + File.separator + "Workspace/graph-caratheodory-np3-parallel/dist/Debug/CUDA-Linux/graph-caratheodory";

    private static final Pattern PATERN_CARATHEODORY_SET = Pattern.compile(".*?S = \\{([0-9, ]+)\\}.*?");
    private static final Pattern PATERN_CARATHEODORY_NUMBER = Pattern.compile(".*?S\\| = ([0-9]+).*?");
    private static final Pattern PATERN_PARALLEL_TIME = Pattern.compile("Total time parallel: (\\w+)");

    protected Process process = null;
//    protected Integer pid = null;
//    protected Executor executor = null;

    static final String type = "P3-Convexity";
    static final String description = "Nº Caratheodory (CUDA)";

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Integer caractheodoryNumber = null;
        int[] caratheodorySet = null;
        Set<Integer> caratheodorySetTmp = null;
        Set<Integer> convexHull = null;
        Set<Integer> partial = null;
        String pTime = null;

        try {
            String path = UtilGraph.saveTmpFileGraphInCsr(graph);
            String commandToExecute = getExecuteCommand(path);

            log.info("Command: " + commandToExecute);

//            ProcessBuilder processBuilder = new ProcessBuilder(commandToExecute);
//            process = processBuilder.start();
//            processBuilder.
            process = Runtime.getRuntime().exec(commandToExecute);
//            executor = new DefaultExecutor();
//            ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
//            executor.setWatchdog(watchdog);
//            CommandLine cmd = CommandLine.parse(commandToExecute);
//            int execute = executor.execute(cmd);
            InputStreamReader input = new InputStreamReader(process.getInputStream());
            BufferedReader reader = new BufferedReader(input);

            String line = "";
            while ((line = reader.readLine()) != null) {
                log.info(line);
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
                    log.error("Error", e);
                }
            }
            process.waitFor();
//            while ((line = reader.readLine()) != null) {
//                log.info(line);
//                try {
//                    if (caratheodorySet == null) {
//                        caratheodorySet = parseCaratheodorySet(line);
//                    }
//                    if (caractheodoryNumber == null) {
//                        caractheodoryNumber = parseCaratheodoryNumber(line);
//                    }
//                    if (pTime == null) {
//                        pTime = parseParallelTime(line);
//                    }
//                } catch (Exception e) {
//                    log.error("Error", e);
//                }
//            }
        } catch (Exception ex) {
            log.error("error", ex);
            return null;
        }

        OperationConvexityGraphResult caratheodoryNumberGraph = null;
        if (caratheodorySet != null && caratheodorySet.length > 0) {
            caratheodoryNumberGraph = hsp3(graph, caratheodorySet);
        }

        if (caratheodoryNumberGraph != null
                && !caratheodoryNumberGraph.caratheodorySet.isEmpty()) {
            convexHull = caratheodoryNumberGraph.convexHull;
            partial = caratheodoryNumberGraph.partial;
            caratheodorySetTmp = caratheodoryNumberGraph.caratheodorySet;
        }

        Map<String, Object> response = new HashMap<>();

        response.put(PARAM_NAME_CARATHEODORY_NUMBER, caractheodoryNumber);
        response.put(PARAM_NAME_CARATHEODORY_SET, caratheodorySetTmp);
        response.put(PARAM_NAME_CONVEX_HULL, convexHull);
        if (pTime != null) {
            response.put(PARAM_NAME_TOTAL_TIME_MS, pTime);
        }
        response.put(PARAM_NAME_PARTIAL_DERIVATED, partial);
        return response;
    }

    @Override
    public String getTypeProblem() {
        return type;
    }

    @Override
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

    @Override
    public void interrupt() {
        try {
//            executor.getWatchdog().destroyProcess();
//            ProcessBuilder pb = new ProcessBuilder();
            process.destroy();
            Process destroyForcibly = process.destroyForcibly();
//            destroyForcibly.isAlive();
            if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field f = process.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                Long pid = f.getLong(process);
                Process exec = Runtime.getRuntime().exec("kill -9 " + (pid + 1));
                destroyForcibly.exitValue();
            }
        } catch (Exception e) {
            log.error("fail on interrupt operation", e);
        }
    }

    String getExecuteCommand(String path) {
        return COMMAND_GRAPH_HN + " -pv " + path;
    }
}

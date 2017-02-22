/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.github.braully.graph.operation.IGraphOperation;
import com.github.braully.graph.operation.Interruptible;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author strike
 */
public class CBInaryOperation implements IGraphOperation, Interruptible {

    private static final Logger log = Logger.getLogger(CBInaryOperation.class);

    String type, name, exec;
    FormatGraphParameter format;
    protected Process process;

    CBInaryOperation(String exec, String type, String operation, String format) {
        this.type = type;
        this.exec = exec;
        this.name = operation;
        this.format = FormatGraphParameter.getFormat(format);
    }

    static enum FormatGraphParameter {
        FILE_CSR("FileCSR"), FILE_ADJACENCY_MATRIX("FileAM");
        String name;

        private FormatGraphParameter(String name) {
            this.name = name;
        }

        static FormatGraphParameter getFormat(String strformat) {
            FormatGraphParameter format = null;
            if (strformat != null && !strformat.isEmpty()) {
                strformat = strformat.trim();
                for (FormatGraphParameter f : values()) {
                    if (f.name.equalsIgnoreCase(strformat)) {
                        format = f;
                        break;
                    }
                }
            }
            return format;
        }
    }

    @Override
    public String getTypeProblem() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Map<String, Object> response = null;
        try {
            String commandToExecute = getExecuteCommand(graph);
            log.info(commandToExecute);
            File execFile = new File(exec);
            execFile.setExecutable(true);
            process = Runtime.getRuntime().exec(commandToExecute);
            InputStreamReader input = new InputStreamReader(process.getInputStream());
            BufferedReader reader = new BufferedReader(input);

            response = new HashMap<>();
            String lastLine = "";
            String line = "";
            while ((line = reader.readLine()) != null) {
                try {
                    if (line != null && !line.trim().isEmpty()) {
                        log.info(line);
                        String[] splits = line.split("=", 2);
                        if (splits != null && splits.length >= 2) {
                            response.put(splits[0].trim(), splits[1].trim());
                        }
                        lastLine = line;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (response.isEmpty()) {
                response.put("Result", lastLine);
            }
            int waitFor = process.waitFor();
        } catch (IOException | InterruptedException ex) {
            log.error("error", ex);
            return null;
        }

        return response;
    }

    @Override
    public void interrupt() {
        try {
            process.destroy();
            Process destroyForcibly = process.destroyForcibly();
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

    private String getExecuteCommand(UndirectedSparseGraphTO<Integer, Integer> graph) {
        String path = "";

        String inputData = graph.getInputData();

        if (format != null) {
            switch (format) {
                case FILE_CSR:
                    path = UtilGraph.saveTmpFileGraphInCsr(graph);
                    break;
                case FILE_ADJACENCY_MATRIX:
                    path = UtilGraph.saveTmpFileGraphInAdjMatrix(graph);
                    break;
                default:
                    break;
            }
        }
        String tmpExec = exec + " " + path;
        if (inputData == null) {
            tmpExec = tmpExec + " " + inputData;
        }
        return tmpExec;
    }
}

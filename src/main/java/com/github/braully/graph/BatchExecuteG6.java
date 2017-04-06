/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.github.braully.graph.operation.GraphCalcCaratheodoryNumberBinaryStrategy;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristic;
import com.github.braully.graph.operation.IGraphOperation;
import com.github.braully.graph.operation.OperationConvexityGraphResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.*;

/**
 *
 * @author strike
 */
public class BatchExecuteG6 implements IBatchExecute {

    public static final int TRESHOLD_PRINT_SET = 30;

    static final IGraphOperation[] operations = new IGraphOperation[]{new GraphCalcCaratheodoryNumberBinaryStrategy()};

//    static {
//    }
    @Override
    public String getDefaultInput() {
        return "/home/strike/grafos-para-processar/almhypo";
    }

    public static void main(String... args) {
        BatchExecuteG6 executor = new BatchExecuteG6();
        executor.processMain(args);
    }

    @Override
    public IGraphOperation[] getOperations() {
        return operations;
    }

    void processMain(String... args) {
        GraphCaratheodoryHeuristic.verbose = false;

        Options options = new Options();

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(false);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(false);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("BatchExecuteG6", options);
            System.exit(1);
            return;
        }

        String inputFilePath = cmd.getOptionValue("input");
        if (inputFilePath == null) {
            inputFilePath = getDefaultInput();
        }
        if (inputFilePath == null) {
            return;
        }

        File dir = new File(inputFilePath);
        if (dir.isDirectory()) {
            processDirectory(inputFilePath);
        } else if (inputFilePath.toLowerCase().endsWith(".mat")) {
            try {
                processFileMat(dir);
            } catch (IOException ex) {
                Logger.getLogger(BatchExecuteG6.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (inputFilePath.toLowerCase().endsWith(".g6")) {
            try {
                processFileG6(dir);
            } catch (IOException ex) {
                Logger.getLogger(BatchExecuteG6.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    void processDirectory(String directory) {
        try {
            File dir = new File(directory);
            File[] filesList = dir.listFiles();
            for (File file : filesList) {
                String name = null;
                try {
                    name = file.getName();
                    if (name.toLowerCase().endsWith(".mat")) {
                        processFileMat(file);
                    } else if (name.toLowerCase().endsWith(".g6")) {
                        processFileG6(file);
                    }
                } catch (Exception e) {
                    System.err.println("Fail in process: " + name);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
        }
    }

    void processFileMat(File file) throws IOException {
        UndirectedSparseGraphTO loadGraphAdjMatrix = UtilGraph.loadGraphAdjMatrix(new FileInputStream(file));
        loadGraphAdjMatrix.setName(file.getName());
        processGraph(loadGraphAdjMatrix);
    }

    void processFileG6(File file) throws IOException {
        if (file != null) {
            long graphcount = 0;
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String readLine = null;
            while ((readLine = r.readLine()) != null && !readLine.isEmpty()) {
                UndirectedSparseGraphTO ret = UtilGraph.loadGraphG6(readLine);
                if (ret != null) {
                    ret.setName(file.getName() + "-" + graphcount);
                    processGraph(ret);
                    graphcount++;
                }
            }
        }
    }

    public void processGraph(UndirectedSparseGraphTO loadGraphAdjMatrix) {
        if (loadGraphAdjMatrix == null || loadGraphAdjMatrix.getVertexCount() == 0) {
            return;
        }

        IGraphOperation[] opers = this.getOperations();
        for (IGraphOperation operation : opers) {
            long currentTimeMillis = System.currentTimeMillis();
            Map result = operation.doOperation(loadGraphAdjMatrix);
            currentTimeMillis = System.currentTimeMillis() - currentTimeMillis;
            if (result.get(OperationConvexityGraphResult.PARAM_NAME_TOTAL_TIME_MS) == null) {
                result.put(OperationConvexityGraphResult.PARAM_NAME_TOTAL_TIME_MS, (double) ((double) currentTimeMillis / 1000));
            }

            String name = loadGraphAdjMatrix.getName();
            String id = name.replaceAll(".mat", "").replaceAll(".g6", "").replaceAll(".json", "");
            try {
                int indexOf = indexOf(name, "\\d");
                if (indexOf > 0) {
                    name = name.substring(0, indexOf);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            printResult(name, id, loadGraphAdjMatrix, operation, result);
        }
    }

    public void printResult(String name, String id, UndirectedSparseGraphTO loadGraphAdjMatrix,
            IGraphOperation operation, Map result) {
        System.out.print(name);
        System.out.print("\t");
        System.out.print(id);
        System.out.print("\t");
        System.out.print(loadGraphAdjMatrix.getVertexCount());
        System.out.print("\t");
        System.out.print(operation.getName());
        System.out.print("\t");
        printResultMap(result, loadGraphAdjMatrix);
    }

    public void printResultMap(Map result, UndirectedSparseGraphTO loadGraphAdjMatrix) {
        System.out.print(result.get(OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_NUMBER));
        System.out.print("\t");
        System.out.println(result.get(OperationConvexityGraphResult.PARAM_NAME_TOTAL_TIME_MS));
        if (loadGraphAdjMatrix.getVertexCount() >= TRESHOLD_PRINT_SET) {
            System.out.print("\t");
            System.out.println(result.get(OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_SET));
        }
    }

    int indexOf(String str, String patern) {
        int ret = 0;
        try {

            Pattern pattern = Pattern.compile(patern);
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                ret = matcher.start();
//                System.out.println(matcher.start());//this will give you index
            }
        } catch (Exception e) {

        }
        return ret;
    }
}

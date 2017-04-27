/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.github.braully.graph.operation.GraphCalcCaratheodoryNumberBinaryStrategy;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristic;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristicV2;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristicV3;
import com.github.braully.graph.operation.GraphHullNumber;
import com.github.braully.graph.operation.IGraphOperation;
import com.github.braully.graph.operation.OperationConvexityGraphResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.apache.commons.cli.*;

/**
 *
 * @author strike
 */
public class BatchExecuteG6 implements IBatchExecute {

    public static final int TRESHOLD_PRINT_SET = 30;

    static final IGraphOperation[] operations = new IGraphOperation[]{
        new GraphCalcCaratheodoryNumberBinaryStrategy(),
        new GraphCaratheodoryHeuristic(),
        new GraphCaratheodoryHeuristicV2(),
        new GraphCaratheodoryHeuristicV3(),
        new GraphHullNumber()
    };
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

        OptionGroup exec = new OptionGroup();
        exec.setRequired(false);
        IGraphOperation[] opers = getOperations();
        Option[] execs = new Option[opers.length];
        for (int i = 0; i < opers.length; i++) {
            IGraphOperation oper = opers[i];
            execs[i] = new Option("" + i, false, oper.getName());
            exec.addOption(execs[i]);
        }
        options.addOptionGroup(exec);

        Option input = new Option("i", "input", true, "input file or directory");
        input.setRequired(true);
        options.addOption(input);

        Option cont = new Option("c", "continue", false, "continue from last processing");
        cont.setRequired(false);
        options.addOption(cont);

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

        boolean contProcess = false;

        String inputFilePath = cmd.getOptionValue("input");
        if (inputFilePath == null) {
            inputFilePath = getDefaultInput();
        }
        if (inputFilePath == null) {
            return;
        }

        if (cmd.hasOption("continue")) {
            contProcess = true;
        }

        List<IGraphOperation> operationsToExecute = new ArrayList<IGraphOperation>();
        for (int i = 0; i < opers.length; i++) {
            IGraphOperation oper = opers[i];
            if (cmd.hasOption(execs[i].getArgName())) {
                operationsToExecute.add(oper);
            }
        }

        File dir = new File(inputFilePath);
        if (dir.isDirectory()) {
            processDirectory(inputFilePath, contProcess);
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

    public String getResultFileName(IGraphOperation graphOperation, String group, String file) {
        StringBuilder resultFileName = new StringBuilder();
        resultFileName.append("resultado-");
        String nameOperation = graphOperation.getName();
        nameOperation = removerCaracteresEspeciais(nameOperation);
        resultFileName.append(nameOperation);
        if (group != null) {
            resultFileName.append(".");
            resultFileName.append(group);
        }
        if (file != null) {
            resultFileName.append(".");
            resultFileName.append(file);
        }
        resultFileName.append(".txt");
        return resultFileName.toString();
    }

    void processDirectory(String directory, boolean contProcess) {
        try {
            File dir = new File(directory);
            String dirname = dir.getName();
            File[] files = dir.listFiles();
            Arrays.sort(files);
//            List<File> filesList = sortFileArray(files);
//            for (File file : filesList) {
            for (File file : files) {
                String name = null;
                try {
                    name = file.getName();
                    if (name.toLowerCase().endsWith(".mat")) {
                        processFileMat(file, dirname);
                    } else if (name.toLowerCase().endsWith(".g6")) {
                        processFileG6(file, dirname);
                    } else if (name.toLowerCase().endsWith(".g6.gz")) {
                        processFileG6GZ(file, dirname);
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
        processFileMat(file, null);
    }

    void processFileMat(File file, String dirname) throws IOException {
        UndirectedSparseGraphTO loadGraphAdjMatrix = UtilGraph.loadGraphAdjMatrix(new FileInputStream(file));
        loadGraphAdjMatrix.setName(file.getName());
        processGraph(loadGraphAdjMatrix);
    }

    void processFileG6(File file) throws IOException {
        processFileG6(file, null);
    }

    void processFileG6(File file, String dirname) throws IOException {
        if (file != null) {
            long graphcount = 0;
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String readLine = null;
            while ((readLine = r.readLine()) != null && !readLine.isEmpty()) {
                UndirectedSparseGraphTO ret = UtilGraph.loadGraphG6(readLine);
                if (ret != null) {
                    ret.setName(file.getName() + "-" + graphcount);
                    processGraph(ret, graphcount);
                    graphcount++;
                }
            }
        }
    }

    void processFileG6GZ(File file) throws IOException {
        processFileG6GZ(file, null);
    }

    void processFileG6GZ(File file, String dirname) throws IOException {
        if (file != null) {
            long graphcount = 0;
            BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            String readLine = null;
            while ((readLine = r.readLine()) != null && !readLine.isEmpty()) {
                UndirectedSparseGraphTO ret = UtilGraph.loadGraphG6(readLine);
                if (ret != null) {
                    ret.setName(file.getName() + "-" + graphcount);
                    processGraph(ret, graphcount);
                    graphcount++;
                }
            }
        }
    }

    public void processGraph(UndirectedSparseGraphTO loadGraphAdjMatrix, String groupName,
            long graphcount, Writer output) {
        if (loadGraphAdjMatrix == null || loadGraphAdjMatrix.getVertexCount() == 0) {
            return;
        }

        IGraphOperation[] opers = this.getOperations();
        for (IGraphOperation operation : opers) {
            long currentTimeMillis = System.currentTimeMillis();
            Map result = operation.doOperation(loadGraphAdjMatrix);
            currentTimeMillis = System.currentTimeMillis() - currentTimeMillis;
            if (result.get(OperationConvexityGraphResult.PARAM_NAME_TOTAL_TIME_MS) == null) {
                result.put(OperationConvexityGraphResult.PARAM_NAME_TOTAL_TIME_MS,
                        (double) ((double) currentTimeMillis / 1000));
            }

            String group = loadGraphAdjMatrix.getName();
            String id = group.replaceAll(".mat", "").replaceAll(".g6", "").replaceAll(".json", "").replaceAll(".gz", "");
            try {
                int indexOf = indexOf(group, "\\d");
                if (indexOf > 0) {
                    group = group.substring(0, indexOf);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (groupName == null) {
                groupName = group;
            }

            inforResult(groupName, id, loadGraphAdjMatrix, operation, result);
            String formatResult = formatResult(groupName, id, loadGraphAdjMatrix, operation, result);
            if (output == null) {
                System.out.println(formatResult);
            } else {
                try {
                    output.write(formatResult);
                    output.flush();
                } catch (IOException ex) {
                    System.err.println(formatResult);
                }
            }
        }
    }

    public String formatResult(String name, String id, UndirectedSparseGraphTO loadGraphAdjMatrix,
            IGraphOperation operation, Map result) {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("\t");
        sb.append(id);
        sb.append("\t");
        sb.append(loadGraphAdjMatrix.getVertexCount());
        sb.append("\t");
        sb.append(operation.getName());
        sb.append("\t");
        sb.append(printResultMap(result, loadGraphAdjMatrix));
        sb.append("\n");
        return sb.toString();
    }

    public String printResultMap(Map result, UndirectedSparseGraphTO loadGraphAdjMatrix) {
        StringBuilder sb = new StringBuilder();
        sb.append(result.get(IGraphOperation.DEFAULT_PARAM_NAME_RESULT));
        sb.append("\t");
        Object t = result.get(OperationConvexityGraphResult.PARAM_NAME_TOTAL_TIME_MS);
        if (t != null) {
            sb.append(t);
        }
        if (loadGraphAdjMatrix.getVertexCount() >= TRESHOLD_PRINT_SET) {
            sb.append("\t");
            Object r = result.get(OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_SET);
            if (r != null) {
                sb.append(r);
            }
            try {
                Collection hs = (Collection) result.get(OperationConvexityGraphResult.PARAM_NAME_CONVEX_HULL);
                if (hs != null) {
                    sb.append("\t");
                    sb.append("|Hs|:");
                    sb.append(hs.size());
                }
            } catch (Exception e) {
            }
        }
        return sb.toString();
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

    static List<File> sortFileArray(File[] files) {
        List<File> fileList = new ArrayList<>(Arrays.asList(files));
        Collections.sort(fileList, new Comparator<File>() {
            public int compare(File t, File t1) {
                int ret = 0;
                try {
                    if (t != null && t1 != null) {
                        ret = t.getName().compareToIgnoreCase(t1.getName());
                    }
                } catch (Exception e) {

                }
                return ret;
            }
        });
        return fileList;
    }

    public void inforResult(String group, String id, UndirectedSparseGraphTO loadGraphAdjMatrix, IGraphOperation operation, Map result) {

    }

    void processGraph(UndirectedSparseGraphTO loadGraphAdjMatrix) {
        processGraph(loadGraphAdjMatrix, null, 0, null);
    }

    void processGraph(UndirectedSparseGraphTO loadGraphAdjMatrix, long countContinue) {
        processGraph(loadGraphAdjMatrix, null, countContinue, null);
    }

    private String removerCaracteresEspeciais(String nameOperation) {
        if (nameOperation == null) {
            return nameOperation;
        }
        return nameOperation.replaceAll("º", "-")
                .replaceAll(" ", "_")
                .replaceAll("(", "")
                .replaceAll(")", "");
    }
}

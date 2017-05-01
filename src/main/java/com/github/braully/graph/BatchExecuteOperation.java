/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.github.braully.graph.operation.GraphCalcCaratheodoryNumberBinaryStrategy;
import com.github.braully.graph.operation.GraphCaratheodoryBFSErika;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristic;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristicHybrid;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristicV2;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristicV3;
import com.github.braully.graph.operation.GraphHullNumber;
import com.github.braully.graph.operation.IGraphOperation;
import com.github.braully.graph.operation.OperationConvexityGraphResult;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class BatchExecuteOperation implements IBatchExecute {

    public static final int TRESHOLD_PRINT_SET = 30;

    static boolean verbose = true;

    static final IGraphOperation[] operations = new IGraphOperation[]{
        new GraphCalcCaratheodoryNumberBinaryStrategy(),
        new GraphCaratheodoryHeuristic(),
        new GraphCaratheodoryHeuristicV2(),
        new GraphCaratheodoryHeuristicV3(),
        new GraphCaratheodoryHeuristicHybrid(),
        new GraphHullNumber(),
        new GraphCaratheodoryBFSErika()
    };

    @Override
    public String getDefaultInput() {
        return "/home/strike/Documentos/grafos-processamento/Critical_H-free/critical";
    }

    public static void main(String... args) {
        BatchExecuteOperation executor = new BatchExecuteOperation();
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
            options.addOption(execs[i]);
        }
//        options.addOptionGroup(exec);

        Option input = new Option("i", "input", true, "input file or directory");
        options.addOption(input);

        Option cont = new Option("c", "continue", false, "continue from last processing");
        cont.setRequired(false);
        options.addOption(cont);

        Option verb = new Option("v", "verbose", false, "verbose processing");
        options.addOption(verb);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(false);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        //        input.setRequired(true);
        //        exec.setRequired(true);
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("BatchExecuteOperation", options);
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

        if (cmd.hasOption("verbose")) {
            verbose = true;
        }

        List<IGraphOperation> operationsToExecute = new ArrayList<IGraphOperation>();
        for (int i = 0; i < opers.length; i++) {
            IGraphOperation oper = opers[i];
            String value = execs[i].getOpt();
            if (cmd.hasOption(value)) {
                operationsToExecute.add(oper);
            }
        }

        if (operationsToExecute.isEmpty()) {
            operationsToExecute.add(opers[0]);
//            formatter.printHelp("BatchExecuteOperation", options);
//            System.exit(1);
//            return;
        }
        File dir = new File(inputFilePath);
        if (dir.isDirectory()) {
            processDirectory(operationsToExecute, inputFilePath, contProcess);
        } else if (inputFilePath.toLowerCase().endsWith(".mat")) {
            try {
                for (IGraphOperation operation : operationsToExecute) {
                    processFileMat(operation, dir);
                }
            } catch (Exception ex) {
                Logger.getLogger(BatchExecuteOperation.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (inputFilePath.toLowerCase().endsWith(".g6")) {
            try {
                for (IGraphOperation operation : operationsToExecute) {
                    processFileG6(operation, dir);
                }
            } catch (Exception ex) {
                Logger.getLogger(BatchExecuteOperation.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (inputFilePath.toLowerCase().endsWith(".g6.gz")) {
            try {
                for (IGraphOperation operation : operationsToExecute) {
                    processFileG6GZ(operation, dir);
                }
            } catch (Exception ex) {
                Logger.getLogger(BatchExecuteOperation.class.getName()).log(Level.SEVERE, null, ex);
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
            resultFileName.append(removerExtensao(file));
        }
        resultFileName.append(".txt");
        return resultFileName.toString();
    }

    void processDirectory(List<IGraphOperation> operationsToExecute, String directory, boolean contProcess) {
        if (verbose) {
            System.out.println("Processing directory: " + directory);
        }
        try {
            File dir = new File(directory);
            String dirname = dir.getName();
            File[] files = dir.listFiles();
            Arrays.sort(files);
//            List<File> filesList = sortFileArray(files);
//            for (File file : filesList) {

            long continueOffset = -1;

            for (IGraphOperation operation : operationsToExecute) {
                String resultFileNameGroup = getResultFileName(operation, dirname, null);

//                long continueOffset = -1;
                if (contProcess) {
                    File file = getExistResultFile(dir, resultFileNameGroup);
                    if (file != null && file.exists()) {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        while (reader.readLine() != null) {
                            continueOffset++;
                        }
                        reader.close();
                    }
                }

                for (File file : files) {
                    String name = null;
                    long graphCount = 0;
                    try {
                        name = file.getName();
                        if (name.toLowerCase().endsWith(".mat")) {
                            if (graphCount > continueOffset) {
                                processFileMat(operation, file, dirname);
                            }
                            graphCount++;
                        } else if (name.toLowerCase().endsWith(".g6")) {
                            processFileG6(operation, file, dirname, contProcess);
                        } else if (name.toLowerCase().endsWith(".g6.gz")) {
                            processFileG6GZ(operation, file, dirname, contProcess);
                        }
                    } catch (Exception e) {
                        System.err.println("Fail in process: " + name);
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void processFileMat(IGraphOperation operation, File file) throws IOException {
        processFileMat(operation, file, null);
    }

    void processFileMat(IGraphOperation operation, File file,
            String dirname) throws IOException {
        UndirectedSparseGraphTO loadGraphAdjMatrix = UtilGraph.loadGraphAdjMatrix(new FileInputStream(file));
        loadGraphAdjMatrix.setName(file.getName());
        processGraph(operation, loadGraphAdjMatrix, dirname, 0);
    }

    void processFileG6GZ(IGraphOperation operation, File file) throws IOException {
        processFileG6GZ(operation, file, null, false);
    }

    void processFileG6GZ(IGraphOperation operation, File file,
            String dirname, boolean contProcess) throws IOException {
        if (file != null) {
            String name = file.getName();
            long continueOffset = -1;
            if (contProcess) {
                continueOffset = getLastProcessCont(operation, dirname, name, file);
            }

            BufferedReader r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            File resultFile = getResultFile(operation, file, dirname);
            BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile, true));
            processStreamGraph(r, operation, dirname, file.getName(), writer, continueOffset);
            try {
                writer.flush();
                writer.close();
                r.close();
            } catch (Exception e) {
            }
        }
    }

    void processFileG6(IGraphOperation operation, File dir) throws IOException {
        processFileG6(operation, dir, null, false);
    }

    void processFileG6(IGraphOperation operation, File file,
            String dirname, boolean contProcess) throws IOException {

        if (file != null) {
            if (verbose) {
                System.out.println("Processing file: " + file.getAbsolutePath());
            }
            String name = file.getName();
            long continueOffset = -1;
            if (contProcess) {
                continueOffset = getLastProcessCont(operation, dirname, name, file);
            }

            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            File resultFile = getResultFile(operation, file, dirname);
            BufferedWriter writer = new BufferedWriter(new FileWriter(resultFile, true));
            processStreamGraph(r, operation, dirname, file.getName(), writer, continueOffset);
            try {
                writer.flush();
                writer.close();
                r.close();
            } catch (Exception e) {

            }
        }
    }

    public void processStreamGraph(BufferedReader r, IGraphOperation operation,
            String dirname, String graphFileName, BufferedWriter writer,
            long continueOffset) throws IOException {
        long graphcount = 0;
        String readLine;
        while ((readLine = r.readLine()) != null && !readLine.isEmpty()) {
            try {
                UndirectedSparseGraphTO ret = UtilGraph.loadGraphG6(readLine);
                if (ret != null) {
                    if (graphcount > continueOffset) {
                        ret.setName(graphFileName + "-" + graphcount);
                        String resultProcess = processGraph(operation, ret, dirname, graphcount);
                        writer.write(resultProcess);
                        writer.flush();
                        if (verbose) {
                            System.out.println(resultProcess);
                        }
                    }
                    graphcount++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    long getLastProcessCont(IGraphOperation operation, String dirname, String name, File file)
            throws FileNotFoundException, IOException {
        long continueOffset = -1;
        String resultFileNameArq = getResultFileName(operation, dirname, name);
        File fileExpress = getExistResultFile(file.getParentFile(), resultFileNameArq);
        if (fileExpress != null && fileExpress.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(fileExpress));
            while (reader.readLine() != null) {
                continueOffset++;
            }
            reader.close();
        }
        return continueOffset;
    }

    public String processGraph(IGraphOperation operation, UndirectedSparseGraphTO loadGraphAdjMatrix, String groupName,
            long graphcount) {
        if (loadGraphAdjMatrix == null || loadGraphAdjMatrix.getVertexCount() == 0) {
            return null;
        }

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
//            if (output == null) {
//                System.out.println(formatResult);
//            } else {
//                try {
//                    output.write(formatResult);
//                    output.flush();
//                } catch (IOException ex) {
//                    System.err.println(formatResult);
//                }
//            }

        return formatResult;
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
                        String tname = t.getName().toLowerCase();
                        String t1name = t1.getName().toLowerCase();
                        if (tname.contains("binary") || tname.contains("-00-ref")) {
                            tname = "a" + tname;
                        }
                        if (t1name.contains("binary") || t1name.contains("-00-ref")) {
                            t1name = "a" + t1name;
                        }
                        ret = tname.compareToIgnoreCase(t1name);
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

    private String removerCaracteresEspeciais(String nameOperation) {
        if (nameOperation == null) {
            return nameOperation;
        }
        return nameOperation.replaceAll("º", "-")
                .replaceAll(" ", "_")
                .replaceAll("\\(", "")
                .replaceAll("\\)", "");
    }

    File getExistResultFile(File dirBase, String resultFileNameGroup) {
        File f = null;
        if (resultFileNameGroup != null) {
            File resultDir = getExistResultDir(dirBase);
            File f1 = new File(dirBase, resultFileNameGroup);
            File f2 = new File(resultDir, resultFileNameGroup);
            if (f1.exists()) {
                f = f1;
            }
            if (f2.exists()) {
                f = f2;
            }
        }
        return f;
    }

    private File getExistResultDir(File dirBase) {
        File dirResult = null;
        try {
            File f2 = new File(dirBase, "resultado");
            if (f2.exists()) {
                dirResult = f2;
            }
        } catch (Exception e) {

        }
        try {
            File f3 = new File(dirBase.getParentFile(), "resultado");
            if (f3.exists()) {
                dirResult = f3;
            }
        } catch (Exception e) {

        }
        try {
            File f4 = new File(dirBase.getParentFile().getParentFile(), "resultado");
            if (f4.exists()) {
                dirResult = f4;
            }
        } catch (Exception e) {

        }
        if (dirResult == null) {
            System.err.println("Not find result folder in: " + dirBase);
        }
        return dirResult;
    }

    private File getResultFile(IGraphOperation operation, File fileGraph, String dirname) {
        File f = null;
        File resultDir = getExistResultDir(fileGraph);
        String resultFileName = getResultFileName(operation, dirname, fileGraph.getName());
        f = new File(resultDir, resultFileName);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(BatchExecuteOperation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return f;
    }

    String removerExtensao(String file) {
        if (file == null) {
            return file;
        }
        return file.replaceAll(".gz", "").replaceAll(".g6", "").replaceAll(".mat", "").replaceAll(".plc", "");
    }
}

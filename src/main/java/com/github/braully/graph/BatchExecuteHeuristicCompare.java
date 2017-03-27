package com.github.braully.graph;

import com.github.braully.graph.operation.GraphCalcCaratheodoryNumberBinaryStrategy;
import com.github.braully.graph.operation.GraphCaratheodoryHeuristic;
import com.github.braully.graph.operation.IGraphOperation;
import com.github.braully.graph.operation.OperationConvexityGraphResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
public class BatchExecuteHeuristicCompare {

    public static void main(String... args) {
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
//        if (inputFilePath == null) {
//            inputFilePath = "/home/strike/grafos-para-processar/almhypo";
//        }
//        String outputFilePath = cmd.getOptionValue("output");
//        System.out.println(inputFilePath);
//        System.out.println(outputFilePath);
        File dir = new File(inputFilePath);
        if (dir.isDirectory()) {
            processDirectory(inputFilePath);
        } else if (inputFilePath.toLowerCase().endsWith(".mat")) {
            try {
                processFile(dir);
            } catch (IOException ex) {
                Logger.getLogger(BatchExecuteHeuristicCompare.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    static void processDirectory(String directory) {
//        System.out.println("Processing directory: " + directory);
        try {
            File dir = new File(directory);
            File[] filesList = dir.listFiles();
            for (File file : filesList) {
                String name = null;
                try {
                    name = file.getName();
//                    System.out.println("Processing file: " + name);
                    if (name.toLowerCase().endsWith(".mat")) {
                        processFile(file);
                    }
                } catch (Exception e) {
                    System.err.println("Fail in process: " + name);
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    static void processFile(File file) throws IOException {
        BatchExecuteHeuristic.processFile(file);
        BatchExecuteG6.processFile(file);
    }

    static int indexOf(String str, String patern) {
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

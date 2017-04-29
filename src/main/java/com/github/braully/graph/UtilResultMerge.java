/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.apache.commons.cli.*;

/**
 *
 * @author strike
 */
public class UtilResultMerge {

    public static String OPERACAO_REFERENCIA = "Nº Caratheodory (Binary Java)";

    public static void main(String... args) throws Exception {
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
            formatter.printHelp("UtilResult", options);

            System.exit(1);
            return;
        }

        String[] inputs = cmd.getOptionValues("input");
        if (inputs == null) {
            inputs = new String[]{
                "/home/strike/Dropbox/documentos/mestrado/grafos-processamento/"
//                "/media/dados/documentos/grafos-processamento/Almost_hypohamiltonian",
//                "/home/strike/Documentos/grafos-processamento/Cubic",
//                "/home/strike/Documentos/grafos-processamento/Critical_H-free",
//                "/home/strike/Documentos/grafos-processamento/Highly_irregular",
//                "/home/strike/Documentos/grafos-processamento/Hypohamiltonian_graphs",
//                "/home/strike/Documentos/grafos-processamento/Maximal_triangle-free",
//                "/home/strike/Documentos/grafos-processamento/Minimal_Ramsey",
//                "/home/strike/Documentos/grafos-processamento/Strongly_regular",
//                "/home/strike/Documentos/grafos-processamento/Vertex-transitive",
//                "/home/strike/Documentos/grafos-processamento/Trees"
            };
        }
        if (inputs != null) {
            processInputs(inputs);
        }
    }

    private static void processInputs(String[] inputs)
            throws FileNotFoundException, IOException {
        if (inputs == null || inputs.length == 0) {
            return;
        }
        File file = null;
        for (String inputFilePath : inputs) {
            if (inputFilePath == null) {
                continue;
            }

            if ((file = new File(inputFilePath)).isFile()) {
                processFile(file);
            } else {
                processDirectory(file);
            }
        }
        printResultadoConsolidado();
    }

    public static void processDirectory(File file)
            throws FileNotFoundException, NumberFormatException, IOException {
        if (file == null || file.isFile()) {
            return;
        }
        File ftmp = new File(file, "resultado");
        if (!ftmp.exists() || !ftmp.isDirectory()) {
            File[] files = file.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file != null && file.isDirectory()) {
                        return true;
                    }
                    return false;
                }
            });
            if (files != null) {
                for (File f : files) {
                    processDirectory(f);
                }
            }
            return;
        }
        File[] files = ftmp.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file != null && file.isFile()
                        && file.getName().startsWith("resultado-")
                        && (file.getName().endsWith(".txt")
                        || file.getName().endsWith(".txt.gz"))) {
                    return true;
                }
                return false;
            }
        });
        if (files != null) {
            List<File> listFiles = BatchExecuteOperation.sortFileArray(files);
            for (File f : listFiles) {
                System.out.println("Process: " + f);
                processFile(f, file.getName());
            }
        }
    }

    public static void processFile(File file) throws FileNotFoundException, NumberFormatException, IOException {
        processFile(file, null);
    }

    public static void processFile(File file, String grupo) throws FileNotFoundException, NumberFormatException, IOException {
        if (file == null) {
            return;
        }
        BufferedReader r = null;

        if (file.getName().endsWith(".txt")) {
            r = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } else if (file.getName().endsWith(".txt.gz")) {
            r = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
        } else {
            return;
        }

        String readLine = null;
        while ((readLine = r.readLine()) != null) {
            String[] parts1 = readLine.split("\t");
            if (parts1 != null && parts1.length >= 6) {
                String grupo1 = parts1[0];
                String idgrafo1 = parts1[1];
                String nverticestr1 = parts1[2];
                String operacao1 = parts1[3];
                String resultao1 = parts1[4];
                String tempo1 = parts1[5];
                double tdouble1 = Double.parseDouble(tempo1);
                Integer resultado1 = null;
                try {
                    resultado1 = Integer.parseInt(resultao1);
                } catch (Exception e) {

                }
                if (grupo != null) {
                    grupo1 = grupo;
                }
                addResult(grupo1, idgrafo1, Integer.parseInt(nverticestr1),
                        operacao1, resultado1, tdouble1);
            }
        }
    }

    private static void addResult(String grafo, String id,
            int nvertices, String operacao,
            Integer resultado, double tempo) {
        String key = String.format("%s-%4d", grafo.trim(), nvertices);
        ResultadoLinha r = resultados.get(key);
        if (r == null) {
            r = new ResultadoLinha();
            r.nome = grafo;
            r.numvertices = nvertices;
            resultados.put(key, r);
        }
        r.addResultado(id, operacao, resultado, tempo);
    }

    static Map<String, ResultadoLinha> resultados = new HashMap<>();
    static int maxCarat = 0;
    static Set<String> operations = new HashSet<>();

    static List<String> getOperationsSorted() {
        List<String> opers = new ArrayList<>(operations);
        Collections.sort(opers);
        return opers;
    }

    private static void printResultadoConsolidado() {
        Set<String> keys = resultados.keySet();
        List<String> listKeys = new ArrayList<>(keys);
        Collections.sort(listKeys);

        System.out.print("Grafo");
        System.out.print("\t");
        System.out.print("Nº Vert");
        System.out.print("\t");
        System.out.print("Quantidade");
        System.out.print("\t");

        List<String> opers = getOperationsSorted();

        for (String str : opers) {
            System.out.print(str + " - T(s)");
            System.out.print("\t");
            System.out.print(str + " - Media");
            System.out.print("\t");
            System.out.print(str + " - Pior");
            System.out.print("\t");
            System.out.print(str + " - Melhor");
            System.out.print("\t");
            System.out.print(str + " - Max");
            System.out.print("\t");
            System.out.print(str + " - Erro");
            System.out.print("\t");
        }

        for (int i = 2; i <= maxCarat; i++) {
            System.out.print("QNC" + i);
            System.out.print("\t");
        }
        System.out.println("");

        for (String key : listKeys) {
            ResultadoLinha result = resultados.get(key);
            if (result != null && result.isValido()) {
                result.printResultado();
            }
        }
    }

    static class ResultadoColuna {

        Map<Integer, Integer> totalPorNum = new HashMap<>();
        double totalTime;
        int max;
        int min;
        long erros;
        long cont;
        long diffAc;
        long diff;
        long worst;
        long best;

        private void addResultadoReferencia(String id, Integer ncarat, double tempo) {
            totalTime += tempo;
            if (ncarat == 0) {
                erros++;
            }
            cont++;
            if (ncarat > max) {
                max = ncarat;
            }
            if (min == 0 || ncarat < min) {
                min = ncarat;
            }
            Integer tparcial = totalPorNum.get(ncarat);
            if (tparcial == null) {
                tparcial = 0;
            }
            if (ncarat > maxCarat) {
                maxCarat = ncarat;
            }
            if (ncarat > 0) {
                totalPorNum.put(ncarat, (tparcial + 1));
            }
        }

        private void addResultado(String id, Integer ncarat, double tempo, Integer ref) {
            if (ncarat == null || ncarat == 0) {
                erros++;
            } else {
                cont++;
                totalTime += tempo;
                if (ncarat > max) {
                    max = ncarat;
                }
                if (min == 0 || ncarat < min) {
                    min = ncarat;
                }
                Integer tparcial = totalPorNum.get(ncarat);
                if (tparcial == null) {
                    tparcial = 0;
                }
                if (ncarat > 0) {
                    totalPorNum.put(ncarat, (tparcial + 1));
                }

                addDiference(ncarat, ref);
            }
        }

        public void addDiference(int r1, int r2) {
            long tmpdiff = (r2 - r1);
            if (tmpdiff > worst) {
                worst = tmpdiff;
            }
            if (best == 0 || tmpdiff < best) {
                best = tmpdiff;
            }
            diffAc += tmpdiff;
            diff++;
        }

        public void printResultado(ResultadoColuna ref) {
            if (cont > 0) {
                String strMedia = "--";
                if (diff > 0) {
                    double media = ((double) diffAc / (double) diff);
                    strMedia = String.format("%.2f", media);
                }
                System.out.print(String.format("%.2f", totalTime));
                System.out.print("\t");
                System.out.print(worst);
                System.out.print("\t");
                System.out.print(best);
                System.out.print("\t");
                System.out.print(strMedia);
                System.out.print("\t");
                System.out.print(ref.max - max);
                System.out.print("\t");
                System.out.print(erros);
                System.out.print("\t");
            } else {
                System.out.print("--");
                System.out.print("\t");
                System.out.print("--");
                System.out.print("\t");
                System.out.print("--");
                System.out.print("\t");
                System.out.print("--");
                System.out.print("\t");
                System.out.print("--");
                System.out.print("\t");
                System.out.print("--");
                System.out.print("\t");
            }
        }

        public void printResultadoReference() {
            if (cont > 0) {
                System.out.print(cont);
                System.out.print("\t");
                System.out.print(String.format("%.2f", totalTime));
                System.out.print("\t");
                System.out.print(min);
                System.out.print("\t");
                System.out.print(max);
                System.out.print("\t");
            } else {
                System.out.print("--");
                System.out.print("\t");
                System.out.print("--");
                System.out.print("\t");
                System.out.print("--");
                System.out.print("\t");
                System.out.print("--");
                System.out.print("\t");
            }
        }
    }

    static class ResultadoLinha {

        static Map<String, Integer> resultadoReferencia = new HashMap<>();

        String nome;
        int numvertices;
        long diffAc;
        long diff;
        long worst;
        long best;

        Map<String, ResultadoColuna> resultados = new HashMap<>();

        public void printResultado() {
            System.out.print(nome);
            System.out.print("\t");
            System.out.print(numvertices);
            System.out.print("\t");
            List<String> opers = getOperationsSorted();
//            ResultadoColuna ref = resultados.get(opers.get(0));

            for (int i = 0; i < opers.size(); i++) {
                String str = opers.get(i);
                ResultadoColuna res = resultados.get(str);
                if (i == 0) {
                    res.printResultadoReference();
                } else {
                    ResultadoColuna ref = resultados.get(opers.get(0));
                    if (res == null) {
                        res = new ResultadoColuna();
                        resultados.put(str, res);

                    }
                    res.printResultado(ref);
                }
            }

            for (int i = 2; i <= maxCarat; i++) {
                StringBuilder tmp = new StringBuilder();
                int cont = 0;
                for (String str : opers) {
                    ResultadoColuna res = resultados.get(str);
                    Integer tcont = res.totalPorNum.get(i);
                    if (tcont == null) {
                        tcont = 0;
                    }
                    tmp.append(cont++).append(":").append(tcont);
                    if (cont <= opers.size() - 1) {
                        tmp.append("|");
                    }
                }
                System.out.print(tmp);
                System.out.print("\t");
            }

            System.out.println("");
        }

        public void addResultado(String id, String operacao,
                Integer resultado, double tempo) {
            ResultadoColuna r = resultados.get(operacao);
            if (r == null) {
                r = new ResultadoColuna();
                resultados.put(operacao, r);
                operations.add(operacao);
            }
            if (resultado != null && resultado > maxCarat) {
                maxCarat = resultado;
            }
            if (OPERACAO_REFERENCIA.equals(operacao)) {
                if (resultado != null) {
                    resultadoReferencia.put(id, resultado);
                    r.addResultadoReferencia(id, resultado, tempo);
                }
            } else {
                Integer ref = resultadoReferencia.get(id);
                if (ref != null) {
                    r.addResultado(id, resultado, tempo, ref);
                } else {
                    //will be ignored, not result reference
                    //throw new IllegalStateException("Not ref result to graph: " + id);
                }
            }
        }

//        public void addResultado1(int ncarat, double t1) {
//            totalTime1 += t1;
//            if (ncarat == 0) {
//                erros++;
//            } else {
//                if (ncarat > max1) {
//                    max1 = ncarat;
//                }
//                if (min1 == 0 || ncarat < min1) {
//                    min1 = ncarat;
//                }
//            }
//            Integer tparcial = totalPorNum.get(ncarat);
//            if (tparcial == null) {
//                tparcial = 0;
//            }
//            if (ncarat > maxCarat) {
//                maxCarat = ncarat;
//            }
//            if (ncarat > 0) {
//                totalPorNum.put(ncarat, (tparcial - 1));
//            } else {
//                totalPorNum.put(ncarat, (tparcial + 1));
//            }
//        }
//
//        public void addResultado2(int ncarat, double t2) {
//            totalTime2 += t2;
//            cont++;
//            if (ncarat > max2) {
//                max2 = ncarat;
//            }
//            if (min2 == 0 || ncarat < min2) {
//                min2 = ncarat;
//            }
//            Integer tparcial = totalPorNum.get(ncarat);
//            if (tparcial == null) {
//                tparcial = 0;
//            }
//            if (ncarat > maxCarat) {
//                maxCarat = ncarat;
//            }
//            totalPorNum.put(ncarat, (tparcial + 1));
//        }
//
//        public void addDiference(int r1, int r2) {
//            long tmpdiff = (r2 - r1);
//            if (tmpdiff > worst) {
//                worst = tmpdiff;
//            }
//            if (best == 0 || tmpdiff < best) {
//                best = tmpdiff;
//            }
//            diffAc += tmpdiff;
//            diff++;
//        }
        private boolean isValido() {
            List<String> opers = getOperationsSorted();
            ResultadoColuna ref = resultados.get(opers.get(0));
            return ref != null;
        }
    };
}

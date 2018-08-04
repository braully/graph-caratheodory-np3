package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author braully
 */
public class PipeGraph {

    private static boolean verbose = false;
    private static final boolean vebosePossibilidadesIniciais = false;
    private static final boolean veboseFimEtapa = false;
    private static final boolean verboseRankingOption = false;

    private static final boolean rankearOpcoes = true;
    private static final int rankearOpcoesProfundidade = 2;
    private static final boolean rankearSegundaOpcoes = false;
    private static final boolean anteciparVazio = true;
    private static final boolean falhaPrimeiroRollBack = false;
    private static final boolean falhaInCommitCount = false;
    private static int falhaCommitCount = 0;
    private static final boolean descartarOpcoesNaoOptimais = true;
    private static final boolean ordenarTrabalhoPorFazerPorPrimeiraOpcao = true;
    private static final boolean dumpResultadoPeriodicamente = true;

    private static BFSTmp bfsalg = null;

    public static void main(String... args) {

        Option input = new Option("i", "input", true, "input file graph");
        Options options = new Options();
        options.addOption(input);

        Option loadprocess = new Option("l", "load", true, "load state process");
        options.addOption(loadprocess);

        Option cont = new Option("c", "continue", false, "continue from last processing");
        cont.setRequired(false);
        options.addOption(cont);

        Option verb = new Option("v", "verbose", false, "verbose processing");
        options.addOption(verb);

        Option poss = new Option("p", "possibility", false, "check possiblities");
        options.addOption(poss);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(false);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
//            formatter.printHelp("PipeGraph", options);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("PipeGraph", options);
            System.exit(1);
            return;
        }

        boolean contProcess = false;

        String inputFilePath = cmd.getOptionValue("input");
        if (inputFilePath == null) {
            inputFilePath = "/home/strike/Nuvem/nextcloud/Workspace-nuvem/maior-grafo-direto-striped.es";
        }

        UndirectedSparseGraphTO graph = UtilGraph.loadGraph(new File(inputFilePath));
        Collection<Integer> vertices = (Collection<Integer>) graph.getVertices();
        LinkedList<Integer> trabalhoPorFazer = new LinkedList<>();
        Map<Integer, List<Integer>> caminhosPossiveis = new HashMap<>();

        int k = 0;
        for (Integer v : vertices) {
            int dg = graph.degree(v);
            if (dg > k) {
                k = dg;
            }
        }

        bfsalg = new BFSTmp(vertices.size());
        initialLoad(vertices, graph, trabalhoPorFazer, caminhosPossiveis, k);
        MooreGraphGen8.ComparatorTrabalhoPorFazer comparatorTrabalhoPorFazer = new MooreGraphGen8.ComparatorTrabalhoPorFazer(caminhosPossiveis);

        if (ordenarTrabalhoPorFazerPorPrimeiraOpcao) {
            Collections.sort(trabalhoPorFazer, comparatorTrabalhoPorFazer);
        } else {
            Collections.sort(trabalhoPorFazer);
        }

        System.out.print("Trabalho por fazer: \n");
        for (Integer e : trabalhoPorFazer) {
            System.out.printf("%d (%d), ", e, graph.degree(e));
        }
        System.out.println();

        System.out.print("Caminhos possiveis: \n");
        List<Integer> ant = caminhosPossiveis.get(trabalhoPorFazer.get(0));
        for (Integer e : trabalhoPorFazer) {
            List<Integer> at = caminhosPossiveis.get(e);
            if (!at.equals(ant)) {
                System.out.println("----------------------------------------------------------------------------------------------");
            }
            System.out.printf("%d|%d|=%s\n", e, at.size(), at.toString());
            ant = at;
            int dv = k - graph.degree(e);
            if (dv > at.size()) {
//                throw new IllegalStateException("Grafo inviavel: vetrice " + e + " dv=" + dv + " possi(" + at.size() + ")=" + at);
            }
        }

        System.out.println();

        if (cmd.hasOption("continue")) {
            contProcess = true;
        }

        if (cmd.hasOption("possibility")) {

        }

        if (cmd.hasOption("verbose")) {
            verbose = true;
        }
    }

    private static class Processamento {

        UndirectedSparseGraphTO insumo;
        Collection<Integer> vertices;
        LinkedList<Integer> trabalhoPorFazer;
        Map<Integer, List<Integer>> caminhosPossiveis;
        Map<Integer, List<Integer>> caminhosPossiveisOriginal;
        TreeMap<Integer, Collection<Integer>> caminhoPercorrido = new TreeMap<>();
        Map<Integer, Map<Integer, List<Integer>>> historicoRanking = new TreeMap<>();
        int numArestasIniciais;
        int numVertices;
        int len;
        Integer trabalhoAtual;
        List<Integer> opcoesPossiveis;
        int marcoInicial;

        BFSTmp bfsalg = null;
        BFSTmp bfsRanking = null;
        BFSTmp bfsRankingSegundaOpcao = null;
        long longestresult = 12160;
        private Integer melhorOpcaoLocal;

    }

    public static void initialLoad(Collection<Integer> vertices,
            UndirectedSparseGraphTO graphTemplate,
            LinkedList<Integer> trabalhoPorFazer,
            Map<Integer, List<Integer>> caminhosPossiveis, int k) {
        System.out.println("Calculando trabalho a fazer");

        for (Integer v : vertices) {
            int remain = k - graphTemplate.degree(v);
            if (remain > 0) {
                trabalhoPorFazer.add(v);
                caminhosPossiveis.put(v, new ArrayList<>());
            }
        }

        System.out.println("Calculando possibilidades de caminho");
        for (int i = 0; i < trabalhoPorFazer.size(); i++) {
            Integer v = trabalhoPorFazer.get(i);
            bfsalg.labelDistances(graphTemplate, v);
            caminhosPossiveis.put(v, new ArrayList<>());
            for (int j = i; j < trabalhoPorFazer.size(); j++) {
                Integer u = trabalhoPorFazer.get(j);
                if (bfsalg.getDistance(graphTemplate, u) == 4) {
                    caminhosPossiveis.get(v).add(u);
                }
            }
        }
    }
}

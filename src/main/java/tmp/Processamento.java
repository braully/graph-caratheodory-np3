/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author strike
 */
public class Processamento {

    /* Parametos */
    boolean verbose = false;
    boolean vebosePossibilidadesIniciais = false;
    boolean veboseFimEtapa = false;
    boolean verboseRankingOption = false;

    boolean rankearOpcoes = true;
    int rankearOpcoesProfundidade = 2;
    boolean rankearSegundaOpcoes = false;

    boolean anteciparVazio = true;
    boolean descartarOpcoesNaoOptimais = true;

    boolean falhaPrimeiroRollBack = false;
    boolean falhaInCommitCount = false;
    int falhaCommitCount = 0;

    final boolean ordenarTrabalhoPorFazerPorPrimeiraOpcao = true;
    final boolean dumpResultadoPeriodicamente = true;

    /* Acompamnehto */
    long lastime = System.currentTimeMillis();
    long lastime2 = System.currentTimeMillis();
    long[] rbcount = new long[4];

    /* Estado */
    UndirectedSparseGraphTO insumo;
    Collection<Integer> vertices;
    LinkedList<Integer> trabalhoPorFazer;
    Map<Integer, List<Integer>> caminhosPossiveis;
    Map<Integer, List<Integer>> caminhosPossiveisOriginal;
    TreeMap<Integer, Collection<Integer>> caminhoPercorrido = new TreeMap<>();
    Map<Integer, Map<Integer, List<Integer>>> historicoRanking = new TreeMap<>();
    int numArestasIniciais;
    int numVertices;
    int numAretasFinais;
    int len;
    int k;
    Integer trabalhoAtual;
    List<Integer> opcoesPossiveis;
    int marcoInicial;

    BFSTmp bfsalg;
    BFSTmp bfsRanking;
    BFSTmp bfsRankingSegundaOpcao;
    long longestresult = 12160;
    Integer melhorOpcaoLocal;
    Comparator<Integer> comparatorTrabalhoPorFazer;
    ComparatorMap comparatorProfundidade;

    public ComparatorMap getComparatorProfundidade() {
        if (comparatorProfundidade == null) {
            comparatorProfundidade = new ComparatorMap(rankearOpcoesProfundidade);
        }
        return comparatorProfundidade;
    }

    public Comparator<Integer> getComparatorTrabalhoPorFazer() {
        if (comparatorTrabalhoPorFazer == null) {
            comparatorTrabalhoPorFazer = new ComparatorTrabalhoPorFazer(caminhosPossiveis);
        }
        return comparatorTrabalhoPorFazer;
    }

    /* */
    public String getEstrategiaString() {
        return (rankearSegundaOpcoes ? "rsot" : "rsof") + "-" + (rankearOpcoes ? "rt0t" : "rt0f") + rankearOpcoesProfundidade + "-" + (ordenarTrabalhoPorFazerPorPrimeiraOpcao ? "opft" : "otpff") + "-" + (descartarOpcoesNaoOptimais ? "dnot" : "dnof") + "-" + (anteciparVazio ? "avt" : "avf");
    }

    void loadGraph(String inputFilePath) {
        UndirectedSparseGraphTO graph = UtilGraph.loadGraph(new File(inputFilePath));
        loadGraph(graph);
    }

    private void loadGraph(UndirectedSparseGraphTO graph) {
        this.insumo = graph;
        this.vertices = (Collection<Integer>) graph.getVertices();
        this.trabalhoPorFazer = new LinkedList<>();
        this.caminhosPossiveis = new HashMap<>();
        this.caminhoPercorrido = new TreeMap<>();
        this.historicoRanking = new TreeMap<>();

        k = 0;
        for (Integer v : vertices) {
            int dg = graph.degree(v);
            if (dg > k) {
                k = dg;
            }
        }
    }

    void loadCaminho(String loadProcess) {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(loadProcess));
            String line = null;
            while ((line = bf.readLine()) != null) {
                if (line != null && line.length() > 0) {
                    String[] args = line.split(" ");
                    System.out.print("Load-Status from Args");
                    String strpattern = "\\{(\\d+)\\}\\((\\d+),(\\d+)\\)\\[([0-9,]+)\\]";
                    Pattern pattern = Pattern.compile(strpattern);
                    for (String str : args) {
                        Matcher matcher = pattern.matcher(str);
                        System.out.print(str);
                        System.out.print("->");
                        if (matcher.matches()) {
                            Integer numEdge = Integer.parseInt(matcher.group(1));
                            Integer e1 = Integer.parseInt(matcher.group(2));
                            Integer e2 = Integer.parseInt(matcher.group(3));
                            List<Integer> caminho = UtilTmp.strToList(matcher.group(4));
                            Integer aresta = (Integer) insumo.addEdge(e1, e2);
                            if (!numEdge.equals(aresta)) {
                                throw new IllegalStateException(String.format("Incorrect load info edge %d expected %d for: %s ", aresta, numEdge, str));
                            }
                            caminhoPercorrido.put(aresta, caminho);
                            System.out.printf("e1=%d,e2=%d,e=%d:", e1, e2, aresta);
                            System.out.print(caminho);
                            System.out.print("  ");
                            Map<Integer, List<Integer>> rankingAtual = historicoRanking.getOrDefault(aresta, new HashMap<>());
                            historicoRanking.putIfAbsent(aresta, rankingAtual);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PipeGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void prepareStart() {
        bfsalg = new BFSTmp(vertices.size());
        if (caminhosPossiveis.isEmpty()) {
            initialLoad();
        }

        if (ordenarTrabalhoPorFazerPorPrimeiraOpcao) {
            Collections.sort(trabalhoPorFazer, getComparatorTrabalhoPorFazer());
        } else {
            Collections.sort(trabalhoPorFazer);
        }

        System.out.print("Trabalho por fazer: \n");
        for (Integer e : trabalhoPorFazer) {
            System.out.printf("%d (%d), ", e, insumo.degree(e));
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
            int dv = k - insumo.degree(e);
            if (dv > at.size()) {
//                throw new IllegalStateException("Grafo inviavel: vetrice " + e + " dv=" + dv + " possi(" + at.size() + ")=" + at);
            }
        }

        System.out.println();

    }

    public void initialLoad() {
        System.out.println("Calculando trabalho a fazer");
        trabalhoPorFazer.clear();
        caminhosPossiveis.clear();

        for (Integer v : vertices) {
            int remain = k - insumo.degree(v);
            if (remain > 0) {
                trabalhoPorFazer.add(v);
                caminhosPossiveis.put(v, new ArrayList<>());
            }
        }
        System.out.println("Calculando possibilidades de caminho");

        for (int i = 0; i < trabalhoPorFazer.size(); i++) {
            Integer v = trabalhoPorFazer.get(i);
            bfsalg.labelDistances(insumo, v);
            caminhosPossiveis.put(v, new ArrayList<>());
            int countp = 0;
            int dv = k - insumo.degree(v);
            for (int j = 0; j < trabalhoPorFazer.size(); j++) {
                Integer u = trabalhoPorFazer.get(j);
                if (bfsalg.getDistance(insumo, u) == 4) {
                    countp++;
                    if (j >= i) {
                        caminhosPossiveis.get(v).add(u);
                    }
                }
            }
            if (countp < dv) {
                throw new IllegalStateException("Grafo inviavel: vetrice " + v + " dv=" + dv + " possi(" + countp + ")=" + caminhosPossiveis.get(v));
            }
        }
    }

    void loadStartFromCache() {
        trabalhoPorFazer = (LinkedList<Integer>) UtilTmp.loadFromCache("trabalho-por-fazer-partial.dat");
        caminhosPossiveis = (Map<Integer, List<Integer>>) UtilTmp.loadFromCache("caminhos-possiveis.dat");
    }

    void recheckPossibilities() {
        for (Integer v : vertices) {
            int remain = k - insumo.degree(v);
            if (remain > 0) {
                int countp = 0;
                bfsalg.labelDistances(insumo, v);
                for (Integer u : vertices) {
                    if (bfsalg.getDistance(insumo, u) == 4) {
                        countp++;
                    }
                }
                if (countp < remain) {
                    throw new IllegalStateException("Grafo inviavel: vetrice " + v + " dv=" + remain + " possi(" + countp + ")");
                }
            }
        }
    }
}

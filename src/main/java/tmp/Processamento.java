/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import edu.uci.ics.jung.graph.util.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    int rankearOpcoesProfundidade = 3;
    boolean rankearSegundaOpcoes = false;

    boolean anteciparVazio = true;
    boolean descartarOpcoesNaoOptimais = true;

    boolean falhaInRollBack = false;
    int falhaRollbackCount = 0;

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
    LinkedList<Integer> trabalhoPorFazerOrigianl;
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
    int marcoInicial;

    BFSTmp bfsalg;
    BFSTmp bfsRanking;
    BFSTmp bfsRankingSegundaOpcao;
    long longestresult = 12214;
    Integer melhorOpcaoLocal;


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
        this.caminhosPossiveisOriginal = new HashMap<>();
        this.caminhoPercorrido = new TreeMap<>();
        this.historicoRanking = new TreeMap<>();

        k = 0;
        for (Integer v : vertices) {
            int dg = graph.degree(v);
            if (dg > k) {
                k = dg;
            }
        }
        this.numVertices = vertices.size();
        this.numAretasFinais = ((k * k + 1) * k) / 2;
        this.numArestasIniciais = this.insumo.getEdgeCount();
    }

    void loadCaminho(String loadProcess) {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(loadProcess));
            String line = null;
            int count = insumo.getEdgeCount();
            while ((line = bf.readLine()) != null) {
                if (line != null && line.length() > 0) {
                    String[] args = line.split(" ");
                    String strpattern = "\\{(\\d+)\\}\\((\\d+),(\\d+)\\)\\[([0-9,]+)\\]";
                    Pattern pattern = Pattern.compile(strpattern);
                    for (String str : args) {
                        Matcher matcher = pattern.matcher(str);
                        if (verbose) {
                            System.out.print(str);
                            System.out.print("->");
                        }
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
                            if (verbose) {
                                System.out.printf("e1=%d,e2=%d,e=%d:", e1, e2, aresta);
                                System.out.print(caminho);
                                System.out.print("  ");
                            }
                            if (insumo.degree(e1) == k) {
                                trabalhoPorFazer.remove(e1);
                            }
                            if (insumo.degree(e2) == k) {
                                trabalhoPorFazer.remove(e2);
                            }
                            Map<Integer, List<Integer>> rankingAtual = historicoRanking.getOrDefault(aresta, new HashMap<>());
                            historicoRanking.putIfAbsent(aresta, rankingAtual);
                        }
                    }
                }
            }
            System.out.print("Loaded " + (insumo.getEdgeCount() - count) + " edges added. ");
            printGraphCount();
        } catch (IOException ex) {
            Logger.getLogger(PipeGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void prepareStart() {
        bfsalg = new BFSTmp(numVertices);
        bfsRanking = new BFSTmp(numVertices);
        bfsRankingSegundaOpcao = new BFSTmp(numVertices);
        if (caminhosPossiveis.isEmpty()) {
            initialLoad();
        }

        if (ordenarTrabalhoPorFazerPorPrimeiraOpcao) {
            Collections.sort(trabalhoPorFazer);
        }

        System.out.printf("Trabalho por fazer[%d]: \n", trabalhoPorFazer.size());
        for (Integer e : trabalhoPorFazer) {
            if (verbose) {
                System.out.printf("%d (%d), ", e, insumo.degree(e));
            }
        }
        printGraphCount();

        if (vebosePossibilidadesIniciais) {
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
        }
        System.out.println();
    }

    public void initialLoad() {
        System.out.println("Calculando trabalho a fazer");
        trabalhoPorFazer.clear();
        caminhosPossiveis.clear();
        caminhosPossiveisOriginal.clear();

        for (Integer v : vertices) {
            int remain = k - insumo.degree(v);
            if (remain > 0) {
                trabalhoPorFazer.add(v);
                caminhosPossiveis.put(v, new ArrayList<>());
            }
        }
        System.out.print("Calculando possibilidades de caminho...");

        for (int i = 0; i < trabalhoPorFazer.size(); i++) {
            Integer v = trabalhoPorFazer.get(i);
            bfsalg.labelDistances(insumo, v);
            caminhosPossiveis.put(v, new ArrayList<>());
            caminhosPossiveisOriginal.put(v, new ArrayList<>());
            int countp = 0;
            int dv = k - insumo.degree(v);
            for (int j = 0; j < trabalhoPorFazer.size(); j++) {
                Integer u = trabalhoPorFazer.get(j);
                if (bfsalg.getDistance(insumo, u) == 4) {
                    countp++;
                    if (j >= i) {
                        caminhosPossiveis.get(v).add(u);
                    }
                    caminhosPossiveisOriginal.get(v).add(u);
                }
            }
            if (countp < dv) {
                throw new IllegalStateException("Grafo inviavel: vetrice " + v + " dv=" + dv + " possi(" + countp + ")=" + caminhosPossiveis.get(v));
            }
        }
//        this.caminhosPossiveisOriginal = UtilTmp.cloneMap(caminhosPossiveis);
        this.trabalhoPorFazerOrigianl = new LinkedList<>(trabalhoPorFazer);
        System.out.println("Grafo viavel");
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
                    if (bfsalg.getDistance(insumo, u) == 4 && insumo.degree(u) < k) {
                        countp++;
                    }
                }
                if (countp < remain) {
                    throw new IllegalStateException("Grafo inviavel: vetrice " + v + " dv=" + remain + " possi(" + countp + ")");
                }
            }
        }
    }

    void sanitizeGraphPossibility() {
        for (Integer v : vertices) {
            int remain = k - insumo.degree(v);
            if (remain > 0) {
                int countp = 0;
                bfsalg.labelDistances(insumo, v);
                for (Integer u : vertices) {
                    if (bfsalg.getDistance(insumo, u) == 4 && insumo.degree(u) < k) {
                        countp++;
                    }
                }
                if (countp < remain) {
                    throw new IllegalStateException("Grafo inviavel: vetrice " + v + " dv=" + remain + " possi(" + countp + ")");
                }
            }
        }
    }

    void printGraphCount() {
        System.out.println("Vertices : " + (numVertices - trabalhoPorFazer.size()) + "/" + numVertices + " Edges: " + insumo.getEdgeCount() + "/" + numAretasFinais);
    }

    boolean verticeComplete(Integer i) {
        return insumo.degree(i) == k;
    }

    synchronized Processamento fork() {
        Processamento sub = new Processamento();
//        this.insumo = graph;
        sub.vertices = this.vertices;
        sub.caminhosPossiveis = caminhosPossiveis;
        sub.caminhosPossiveisOriginal = caminhosPossiveisOriginal;
        sub.k = k;
        sub.numVertices = numVertices;
        sub.numAretasFinais = numAretasFinais;
        sub.numArestasIniciais = numArestasIniciais;

        sub.insumo = insumo.clone();
        sub.trabalhoPorFazer = new LinkedList<>(trabalhoPorFazer);
        sub.caminhoPercorrido = UtilTmp.cloneMap(caminhoPercorrido);
        sub.historicoRanking = new TreeMap<>();
        sub.bfsalg = new BFSTmp(numVertices);
        sub.bfsRanking = new BFSTmp(numVertices);
        sub.bfsRankingSegundaOpcao = new BFSTmp(numVertices);

        /* verboses */
        sub.verbose = this.verbose;
        sub.verboseRankingOption = this.verboseRankingOption;
        sub.veboseFimEtapa = this.veboseFimEtapa;
        sub.vebosePossibilidadesIniciais = this.vebosePossibilidadesIniciais;

        /* ranking */
        sub.rankearOpcoes = this.rankearOpcoes;
        sub.rankearOpcoesProfundidade = this.rankearOpcoesProfundidade;
        return sub;
    }

    void dumpResultadoSeInteressante() {
        dumpResultadoSeInteressante(this);
    }

    void dumpResultadoSeInteressante(Processamento processamento) {
        if (processamento.dumpResultadoPeriodicamente && System.currentTimeMillis() - processamento.lastime > UtilTmp.ALERT_HOUR) {
            System.out.println("Alert hour ");
            UtilTmp.dumpStringIdentified(processamento.getEstrategiaString());
            UtilTmp.dumpString(String.format(" rbcount[%d,%d,%d,%d]=%d", processamento.rbcount[0], processamento.rbcount[1],
                    processamento.rbcount[2], processamento.rbcount[3],
                    (processamento.rbcount[0] + processamento.rbcount[1] + processamento.rbcount[2] + processamento.rbcount[3])));
            processamento.rbcount[0] = processamento.rbcount[1] = processamento.rbcount[2] = processamento.rbcount[3] = 0;
            processamento.lastime = System.currentTimeMillis();
            //                        printVertAddArray(insumo, numArestasIniciais);

            String lastAdd = String.format(" last+[%5d](%4d,%4d) \n", insumo.getEdgeCount(), processamento.trabalhoAtual, melhorOpcaoLocal);
            UtilTmp.dumpString(lastAdd);
            UtilTmp.printCurrentItme();

            if (processamento.longestresult < processamento.insumo.getEdgeCount() || System.currentTimeMillis() - processamento.lastime2 > UtilTmp.ALERT_HOUR_12) {
                processamento.lastime2 = System.currentTimeMillis();
                if (processamento.longestresult < processamento.insumo.getEdgeCount()) {
                    System.out.print("new longest  result: ");
                    processamento.longestresult = processamento.insumo.getEdgeCount();
                    System.out.println(processamento.longestresult);
                }
                UtilTmp.dumpVertAddArray(processamento.insumo,
                        processamento.numArestasIniciais,
                        processamento.caminhoPercorrido);
                if (processamento.k > 7) {
                    UtilTmp.dumpOverrideString(processamento.insumo.getEdgeString(), ".graph.g9." + processamento.getEstrategiaString());
                }
            }
        }
    }

    void printGraphCaminhoPercorrido() {
        try {
            System.out.print("vert-adds: ");
            for (int i = numArestasIniciais; i < insumo.getEdgeCount(); i++) {
                Collection<Integer> opcoesTestadas = caminhoPercorrido.get(i);
                String str = String.format("{%d}(%d,%d)",
                        i, insumo.getEndpoints(i).getFirst(),
                        insumo.getEndpoints(i).getSecond());
                System.out.printf(str);
                System.out.print("[");

                for (Integer j : opcoesTestadas) {
                    String jstr = j.toString();
                    System.out.print(jstr);
                    System.out.print(",");
                }

                System.out.print("] ");
            }
            System.out.println();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void ordenarTrabalhoPorFazerNatual() {
        Collections.sort(trabalhoPorFazer);
    }

    void ordenarTrabalhoPorCaminhosPossiveis() {
        Collections.sort(trabalhoPorFazer, new ComparatorTrabalhoPorFazer(this.caminhosPossiveis));
    }

    List<Integer> getOpcoesPossiveisAtuais() {
        return caminhosPossiveis.get(trabalhoAtual);
    }

    public Integer getPosicaoAtual() {
        return insumo.getEdgeCount();
    }

    void mergeProcessamentos(List<Processamento> processamentos) {
        System.out.println("Merge current processamento");
        printGraphCount();
        System.out.println("With anothers " + processamentos.size() + " processamentos");
        int count = 0;
        int added = 0;
        for (Processamento p : processamentos) {
            System.out.println("Merging process " + count++);
            added = 0;
            Set<Map.Entry<Integer, Collection<Integer>>> entrySet = p.caminhoPercorrido.entrySet();
            for (Map.Entry<Integer, Collection<Integer>> e : entrySet) {
                Pair endpoints = p.insumo.getEndpoints(e.getKey());
                Integer first = (Integer) endpoints.getFirst();
                Integer second = (Integer) endpoints.getSecond();
                if (addEdgeIfConsistent(first, second)) {
                    added++;
                }
            }
            System.out.println("Added " + added);
        }
        removerTrabalhoPorFazerVerticesCompletos();
        printGraphCount();
    }

    boolean addEdgeIfConsistent(Integer first, Integer second) {
        boolean ret = false;
        bfsRankingSegundaOpcao.bfs(insumo, first);
        if (bfsRankingSegundaOpcao.getDistance(insumo, second) == 4) {
            insumo.addEdge(first, second);
            ret = true;
        }
        return ret;
    }

    private void removerTrabalhoPorFazerVerticesCompletos() {
        Set<Integer> removeList = new HashSet<>();
        for (Integer v : trabalhoPorFazer) {
            if (this.verticeComplete(v)) {
                removeList.add(v);
            }
        }
        trabalhoPorFazer.removeAll(removeList);
    }
}

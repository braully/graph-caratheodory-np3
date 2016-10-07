/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.hn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.math3.util.CombinatoricsUtils;

/**
 * REST Web Service
 *
 * @author strike
 */
@Path("graph")
public class GraphWS {

    private static final Logger log = Logger.getLogger(GraphWS.class.getSimpleName());

    private static final String PARAM_NAME_CARATHEODORY_NUMBER = "number";
    private static final String PARAM_NAME_CARATHEODORY_SET = "set";
    private static final String PARAM_NAME_CONVEX_HULL = "hs";
    private static final String PARAM_NAME_AUX_PROCESS = "aux";
    private static final String PARAM_NAME_TOTAL_TIME_MS = "tms";
    private static final String PARAM_NAME_PARTIAL_DERIVATED = "phs";

    private static final String COMMAND_GRAPH_HN = "~/bin/graph-caratheodory-np3.sh";

    private static final Pattern PATERN_CARATHEODORY_SET = Pattern.compile(".*?Combination: \\{([0-9, ]+)\\}.*?");
    private static final Pattern PATERN_CARATHEODORY_NUMBER = Pattern.compile(".*?S\\| = ([0-9]+).*?");
    private static final Pattern PATERN_PARALLEL_TIME = Pattern.compile("Total time parallel: (\\w+)");

    private static final boolean verbose = false;
    private static final boolean breankOnFirst = true;

    private final int INCLUDED = 2;
    private final int NEIGHBOOR_COUNT_INCLUDED = 1;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("random")
    public UndirectedSparseGraphTO randomGraph(
            @QueryParam("nvertices") @DefaultValue("5") Integer nvertices,
            @QueryParam("minDegree") @DefaultValue("1") Integer minDegree,
            @QueryParam("maxDegree") @DefaultValue("1.5") Double maxDegree,
            @QueryParam("typeGraph") @DefaultValue("random") String typeGraph) {
//        UndirectedSparseGraphTO<Integer, Integer> graph = generateRandomGraphSimple(nvertices, minDegree, maxDegree);

        UndirectedSparseGraphTO<Integer, Integer> graph = null;
        if (typeGraph != null) {
            switch (typeGraph) {
                case "path":
                    graph = generatePathGraph(nvertices, minDegree, maxDegree);
                    break;
                case "cycle":
                    graph = generateCycleGraph(nvertices, minDegree, maxDegree);
                    break;
                case "binary":
                    graph = generateBinaryGraph(nvertices, minDegree, maxDegree);
                    break;
                case "random":
                default:
                    graph = generateRandomGraph(nvertices, minDegree, maxDegree);
                    break;
            }
        }
        return graph;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("checkset")
    public Map<String, Object> checkset(String jsonGraph) {
        Integer caratheodoryNumber = -1;
        Integer[] caratheodorySet = null;
        Integer[] convexHull = null;
        int[] auxProcessor = null;
        Integer[] partial = null;
        long totalTimeMillis = -1;
        try {
            ObjectMapper mapper = new ObjectMapper();
            BeanDeserializer bd = null;
            UndirectedSparseGraphTO<Integer, Integer> graphRead = mapper.readValue(jsonGraph, UndirectedSparseGraphTO.class);
            Collection<Integer> set = graphRead.getSet();
            int[] arr = new int[set.size()];
            int i = 0;
            for (Integer v : set) {
                arr[i] = v;
                i++;
            }
            totalTimeMillis = System.currentTimeMillis();
            ProcessedSet caratheodoryNumberGraph = hsp3(graphRead, arr);
            totalTimeMillis = System.currentTimeMillis() - totalTimeMillis;
            if (caratheodoryNumberGraph != null
                    && !caratheodoryNumberGraph.caratheodorySet.isEmpty()) {
                caratheodoryNumber = caratheodoryNumberGraph.caratheodorySet.size();
                caratheodorySet = caratheodoryNumberGraph.caratheodorySet.toArray(new Integer[0]);
                auxProcessor = caratheodoryNumberGraph.auxProcessor;
                convexHull = caratheodoryNumberGraph.convexHull.toArray(new Integer[0]);
                partial = caratheodoryNumberGraph.partial.toArray(new Integer[0]);
            }
        } catch (IOException ex) {
            Logger.getLogger(GraphWS.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();
        response.put(PARAM_NAME_CARATHEODORY_NUMBER, caratheodoryNumber);
        response.put(PARAM_NAME_CARATHEODORY_SET, caratheodorySet);
        response.put(PARAM_NAME_CONVEX_HULL, convexHull);
        response.put(PARAM_NAME_AUX_PROCESS, auxProcessor);
        response.put(PARAM_NAME_TOTAL_TIME_MS, (double) ((double) totalTimeMillis / 1000));
        response.put(PARAM_NAME_PARTIAL_DERIVATED, partial);
        return response;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("caratheodory")
    public Map<String, Object> calcCaratheodroyNumberGraph(String jsonGraph) {
        Integer caratheodoryNumber = -1;
        Integer[] caratheodorySet = null;
        Integer[] convexHull = null;
        Integer[] partial = null;
        int[] auxProcessor = null;
        long totalTimeMillis = -1;
        try {
            ObjectMapper mapper = new ObjectMapper();
            BeanDeserializer bd = null;
            UndirectedSparseGraphTO<Integer, Integer> graphRead = mapper.readValue(jsonGraph, UndirectedSparseGraphTO.class);
            totalTimeMillis = System.currentTimeMillis();
            ProcessedSet caratheodoryNumberGraph = calcMaxCaratheodroyNumberGraph(graphRead);
            totalTimeMillis = System.currentTimeMillis() - totalTimeMillis;
            if (caratheodoryNumberGraph != null
                    && !caratheodoryNumberGraph.caratheodorySet.isEmpty()) {
                caratheodoryNumber = caratheodoryNumberGraph.caratheodorySet.size();
                caratheodorySet = caratheodoryNumberGraph.caratheodorySet.toArray(new Integer[0]);
                auxProcessor = caratheodoryNumberGraph.auxProcessor;
                convexHull = caratheodoryNumberGraph.convexHull.toArray(new Integer[0]);
                partial = caratheodoryNumberGraph.partial.toArray(new Integer[0]);
            }
        } catch (IOException ex) {
            Logger.getLogger(GraphWS.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();
        response.put(PARAM_NAME_CARATHEODORY_NUMBER, caratheodoryNumber);
        response.put(PARAM_NAME_CARATHEODORY_SET, caratheodorySet);
        response.put(PARAM_NAME_CONVEX_HULL, convexHull);
        response.put(PARAM_NAME_AUX_PROCESS, auxProcessor);
        response.put(PARAM_NAME_TOTAL_TIME_MS, (double) ((double) totalTimeMillis / 1000));
        response.put(PARAM_NAME_PARTIAL_DERIVATED, partial);
        return response;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("caratheodory-parallel")
    public Map<String, Object> calcCaratheodoryNumberGraphParallel(String jsonGraph) {
        Integer caractheodoryNumber = null;
        int[] caratheodorySet = null;
        Integer[] convexHull = null;
        int[] auxProcessor = null;
        Integer[] partial = null;
        String pTime = null;
        UndirectedSparseGraphTO<Integer, Integer> undGraph = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            BeanDeserializer bd = null;
            undGraph = mapper.readValue(jsonGraph, UndirectedSparseGraphTO.class);
            String path = saveTmpFileGraphInCsr(undGraph);

            String commandToExecute = COMMAND_GRAPH_HN + " -p " + path;

            log.log(Level.INFO, "Command: {0}", commandToExecute);
            log.log(Level.INFO, "Executing");
            Process p = Runtime.getRuntime().exec(commandToExecute);
            p.waitFor();
            log.log(Level.INFO, "Executed");
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            log.log(Level.INFO, "Output");
            while ((line = reader.readLine()) != null) {
                log.log(Level.INFO, line);
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
                    log.log(Level.WARNING, "", e);
                }
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            log.log(Level.SEVERE, null, ex);
        }

        ProcessedSet caratheodoryNumberGraph = null;
        if (caratheodorySet != null && caratheodorySet.length > 0) {

            caratheodoryNumberGraph = hsp3(undGraph, caratheodorySet);
        }

        if (caratheodoryNumberGraph != null
                && !caratheodoryNumberGraph.caratheodorySet.isEmpty()) {
            auxProcessor = caratheodoryNumberGraph.auxProcessor;
            convexHull = caratheodoryNumberGraph.convexHull.toArray(new Integer[0]);
            partial = caratheodoryNumberGraph.partial.toArray(new Integer[0]);
        }

        Map<String, Object> response = new HashMap<>();

        response.put(PARAM_NAME_CARATHEODORY_NUMBER, caractheodoryNumber);
        response.put(PARAM_NAME_CARATHEODORY_SET, caratheodorySet);
        response.put(PARAM_NAME_CONVEX_HULL, convexHull);
        response.put(PARAM_NAME_AUX_PROCESS, auxProcessor);
        response.put(PARAM_NAME_TOTAL_TIME_MS, pTime);
        response.put(PARAM_NAME_PARTIAL_DERIVATED, partial);

        return response;
    }

    private UndirectedSparseGraphTO<Integer, Integer> generatePathGraph(Integer nvertices, Integer minDegree, Double maxDegree) {
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        List<Integer> vertexElegibles = new ArrayList<>(nvertices);
        Integer[] vertexs = new Integer[nvertices];
        for (int i = 0; i < nvertices; i++) {
            vertexElegibles.add(i);
            vertexs[i] = i;
            graph.addVertex(vertexs[i]);
        }
        int countEdge = 0;
        for (int i = 0; i < nvertices - 1; i++) {
            Integer source = vertexs[i];
            Integer target = vertexs[i] + 1;
            graph.addEdge(countEdge++, source, target);
        }
        return graph;
    }

    private UndirectedSparseGraphTO<Integer, Integer> generateCycleGraph(Integer nvertices, Integer minDegree, Double maxDegree) {
        UndirectedSparseGraphTO<Integer, Integer> graph = this.generatePathGraph(nvertices, minDegree, maxDegree);
        graph.addEdge(graph.getEdgeCount(), graph.getVertexCount() - 1, 0);
        return graph;
    }

    private UndirectedSparseGraphTO<Integer, Integer> generateBinaryGraph(Integer nvertices, Integer minDegree, Double maxDegree) {
        double lognv = Math.log(nvertices + 1) / Math.log(2);
        double pow = Math.pow(2, Math.ceil(lognv)) - 1;
        int nvert = (int) pow;
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        Queue<Integer> frontier = new ArrayDeque<>();
        graph.addVertex(0);
        int countEdge = 0;
        int countVertice = 1;
        frontier.add(0);

        while (!frontier.isEmpty() && countVertice < nvert) {
            Integer verti = frontier.remove();
            Integer target1 = countVertice++;
            Integer target2 = countVertice++;
            graph.addEdge(countEdge++, verti, target1);
            graph.addEdge(countEdge++, verti, target2);
            if (countVertice < nvert) {
                frontier.add(target1);
                frontier.add(target2);
            }
        }
        return graph;
    }

    private UndirectedSparseGraphTO<Integer, Integer> generateRandomGraph(Integer nvertices,
            Integer minDegree,
            Double maxDegree) {
        UndirectedSparseGraphTO<Integer, Integer> graph = new UndirectedSparseGraphTO<>();
        List<Integer> vertexElegibles = new ArrayList<>(nvertices);
        Integer[] vertexs = new Integer[nvertices];
        int[] degree = new int[nvertices];
        for (int i = 0; i < nvertices; i++) {
            vertexElegibles.add(i);
            vertexs[i] = i;
            degree[i] = 0;
            graph.addVertex(vertexs[i]);
        }
        int countEdge = 0;
        double offset = maxDegree - minDegree;
        for (int i = nvertices - 1; i > 0; i--) {
            long limite = minDegree + Math.round(Math.random() * (offset));
            int size = vertexElegibles.size();
            Integer source = vertexs[i];
            for (int j = 0; j <= limite; j++) {
                //Exclude last element from choose (no loop)
                Integer target = null;
                if (vertexElegibles.size() > 1) {
                    int vrandom = (int) Math.round(Math.random() * (size - 2));
                    target = vertexElegibles.get(vrandom);
                    if (graph.addEdge(countEdge++, source, target)) {
                        if (degree[target]++ >= maxDegree) {
                            vertexElegibles.remove(target);
                        }
                        if (degree[source]++ >= maxDegree) {
                            vertexElegibles.remove(source);
                        }
                    }
                    size = vertexElegibles.size();
                } else {
                    int vrandom = (int) Math.round(Math.random() * (nvertices - 1));
                    target = vertexs[vrandom];
                    graph.addEdge(countEdge++, source, target);
                }
            }
        }
        return graph;
    }

    private ProcessedSet calcMaxCaratheodroyNumberGraph(UndirectedSparseGraphTO<Integer, Integer> graph) {
        ProcessedSet processedCaratheodroySet = null;
        if (graph == null) {
            return processedCaratheodroySet;
        }
        int maxSizeSet = (graph.getVertexCount() + 1) / 2;
//        int maxSizeSet = graph.getVertexCount() - 1;

        int currentSize = maxSizeSet;

        while (currentSize >= 2) {
            processedCaratheodroySet = findCaratheodroySetBruteForce(graph, currentSize);
            if (processedCaratheodroySet != null
                    && processedCaratheodroySet.caratheodorySet != null
                    && !processedCaratheodroySet.caratheodorySet.isEmpty()) {
                break;
            }
            currentSize--;
        }
        return processedCaratheodroySet;
    }

    public ProcessedSet findCaratheodroySetBruteForce(UndirectedSparseGraphTO<Integer, Integer> graph, int currentSetSize) {
        ProcessedSet processedHullSet = null;
        if (graph == null || graph.getVertexCount() <= 0) {
            return processedHullSet;
        }
        Collection vertices = graph.getVertices();
        int veticesCount = vertices.size();
        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(graph.getVertexCount(), currentSetSize);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            ProcessedSet hsp3g = hsp3(graph, currentSet);

//        return fecho;
            if (hsp3g != null) {
                processedHullSet = hsp3g;
                break;
            }
        }
        return processedHullSet;
    }

    public ProcessedSet hsp3(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] currentSet) {
        int currentSetSize = 0;
        ProcessedSet processedHullSet = null;
        Set<Integer> hsp3g = new HashSet<>();
        int[] aux = new int[graph.getVertexCount()];
        int[] auxa = new int[graph.getVertexCount()];
        int[] auxb = new int[graph.getVertexCount()];
        int[] auxc = new int[graph.getVertexCount()];
        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
            auxc[i] = 0;
            auxa[i] = auxb[i] = -1;
        }

        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer v : currentSet) {
            mustBeIncluded.add(v);
            aux[v] = INCLUDED;
            auxc[v] = 1;
            currentSetSize++;
        }
        while (!mustBeIncluded.isEmpty()) {
            Integer verti = mustBeIncluded.remove();
            hsp3g.add(verti);
//            aux[verti] = aux[verti] + INCLUDED;
            Collection<Integer> neighbors = graph.getNeighbors(verti);

            for (int vertn : neighbors) {
                if (vertn != verti) {
                    int previousValue = aux[vertn];
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (previousValue < INCLUDED) {
                        if (aux[vertn] >= INCLUDED) {
                            mustBeIncluded.add(vertn);
                            auxb[vertn] = verti;
                            auxc[vertn] = auxc[vertn] + auxc[verti];
                        } else {
                            auxa[vertn] = verti;
                            auxc[vertn] = auxc[vertn] + auxc[verti];
                        }
                    }
                }
            }
        }

        if (verbose) {
            System.out.print("Aux = {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                System.out.print(aux[i] + " | ");
            }
            System.out.println("}");

            System.out.print("Auxa= {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                System.out.print((auxa[i] < 0 ? "-" : auxa[i]) + " | ");
            }
            System.out.println("}");

            System.out.print("Auxb= {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                System.out.print((auxb[i] < 0 ? "-" : auxb[i]) + " | ");
            }
            System.out.println("}");

            System.out.print("Auxc= {");
            for (int i = 0; i < graph.getVertexCount(); i++) {
                System.out.print(auxc[i] + " | ");
            }
            System.out.println("}");
        }

        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (auxc[i] >= currentSetSize) {
                Queue<Integer> queueu = new ArrayDeque<>();
                Set<Integer> hs = new HashSet<>(currentSetSize);
                queueu.add(auxa[i]);
                queueu.add(auxb[i]);
                while (!queueu.isEmpty()) {
                    Integer actual = queueu.remove();
                    if (actual == -1) {
                        continue;
                    }
                    if (auxa[actual] == -1) {
                        hs.add(actual);
                    } else {
                        queueu.add(auxa[actual]);
                    }
                    if (auxb[actual] == -1) {
                        hs.add(actual);
                    } else {
                        queueu.add(auxb[actual]);
                    }
                }
                if (verbose) {
                    System.out.println("hs(" + i + ") = " + hs);
                }
                if (hs.size() == currentSetSize) {
                    Set<Integer> partial = calcDerivatedPartial(graph,
                            hsp3g, currentSet);
                    if (partial != null && !partial.isEmpty()) {
                        processedHullSet = new ProcessedSet();
                        processedHullSet.auxProcessor = aux;
                        processedHullSet.convexHull = hsp3g;
                        processedHullSet.caratheodorySet = hs;
                        processedHullSet.partial = calcDerivatedPartial(graph,
                                hsp3g, currentSet);

                    }
                    break;
                }
            }
        }
        return processedHullSet;
    }

    private Set<Integer> calcDerivatedPartial(UndirectedSparseGraphTO<Integer, Integer> graph,
            Set<Integer> hsp3g, int[] currentSet) {
        Set<Integer> partial = new HashSet<>();
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        partial.addAll(hsp3g);

        for (Integer p : currentSet) {
            int[] aux = new int[graph.getVertexCount()];
            for (Integer v : currentSet) {
                if (!v.equals(p)) {
                    mustBeIncluded.add(v);
                    aux[v] = INCLUDED;
                }
            }
            while (!mustBeIncluded.isEmpty() && !partial.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                partial.remove(verti);
                Collection<Integer> neighbors = graph.getNeighbors(verti);
                for (int vertn : neighbors) {
                    if (vertn != verti) {
                        int previousValue = aux[vertn];
                        aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                        if (previousValue < INCLUDED && aux[vertn] >= INCLUDED) {
                            mustBeIncluded.add(vertn);
                        }
                    }
                }
            }
        }
        return partial;
    }

    private String saveTmpFileGraphInCsr(UndirectedSparseGraphTO<Integer, Integer> undGraph) {
        String strFile = null;
        if (undGraph != null && undGraph.getVertexCount() > 0) {
            try {
                int vertexCount = undGraph.getVertexCount();
                File file = File.createTempFile("graph-csr-", ".txt");
                file.deleteOnExit();

                strFile = file.getAbsolutePath();
                FileWriter writer = new FileWriter(file);
                writer.write("#Graph |V| = " + vertexCount + "\n");

                int sizeRowOffset = 0;
                List<Integer> csrColIdxs = new ArrayList<>();
                List<Integer> rowOffset = new ArrayList<>();

                int idx = 0;
                for (Integer i = 0; i < vertexCount; i++) {
                    csrColIdxs.add(idx);
                    Collection<Integer> neighbors = undGraph.getNeighbors(i);
                    Set<Integer> neighSet = new HashSet<>();
                    neighSet.addAll(neighbors);
                    for (Integer vn : neighSet) {
                        if (!vn.equals(i)) {
                            rowOffset.add(vn);
                            idx++;
                        }
                    }
                }

                for (Integer i : csrColIdxs) {
                    writer.write("" + i);
                    writer.write(" ");
                }
                writer.write("\n");
                for (Integer i : rowOffset) {
                    writer.write("" + i);
                    writer.write(" ");
                }
                writer.write("\n");
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(GraphWS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Logger.getLogger(GraphWS.class.getName()).log(Level.INFO, "File tmp graph: " + strFile);
        return strFile;
    }

    private int[] parseCaratheodorySet(String line) {
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

    private Integer parseCaratheodoryNumber(String line) {
        Integer ret = null;
        Matcher m = PATERN_CARATHEODORY_NUMBER.matcher(line);
        if (m.find()) {
            String trim = m.group();
            trim = m.group(1);
//            String trim = m.group();
            if (trim != null && !trim.isEmpty()) {
                ret = Integer.parseInt(trim.trim());
            }
        }
        return ret;
    }

    private String parseParallelTime(String line) {
        String ret = null;
        Matcher m = PATERN_PARALLEL_TIME.matcher(line);
        if (m.find()) {
            ret = m.group(1);
        }
        return ret;
    }
}

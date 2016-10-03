/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.hn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
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

    private static final String PARAM_NAME_HULL_NUMBER = "number";
    private static final String PARAM_NAME_HULL_SET = "set";
    private static final String PARAM_NAME_CONVEX_HULL = "hs";
    private static final String PARAM_NAME_AUX_PROCESS = "aux";
    private static final String PARAM_NAME_TOTAL_TIME_MS = "tms";
    private static final String PARAM_NAME_PARTIAL_DERIVATED = "phs";

    private static final boolean verbose = true;
    private static final boolean breankOnFirst = true;

    private int INCLUDED = 2;
    private int NEIGHBOOR_COUNT_INCLUDED = 1;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("random")
    public UndirectedSparseGraphTO randomGraph(
            @QueryParam("nvertices") @DefaultValue("5") Integer nvertices,
            @QueryParam("minDegree") @DefaultValue("1") Integer minDegree,
            @QueryParam("maxDegree") @DefaultValue("1.5") Double maxDegree) {
//        UndirectedSparseGraphTO<Integer, Integer> graph = generateRandomGraphSimple(nvertices, minDegree, maxDegree);
        UndirectedSparseGraphTO<Integer, Integer> graph = generateRandomGraph(nvertices, minDegree, maxDegree);
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
            ProcessedHullSet caratheodoryNumberGraph = hsp3(graphRead, arr);
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
        response.put(PARAM_NAME_HULL_NUMBER, caratheodoryNumber);
        response.put(PARAM_NAME_HULL_SET, caratheodorySet);
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
            ProcessedHullSet caratheodoryNumberGraph = calcMaxCaratheodroyNumberGraph(graphRead);
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
        response.put(PARAM_NAME_HULL_NUMBER, caratheodoryNumber);
        response.put(PARAM_NAME_HULL_SET, caratheodorySet);
        response.put(PARAM_NAME_CONVEX_HULL, convexHull);
        response.put(PARAM_NAME_AUX_PROCESS, auxProcessor);
        response.put(PARAM_NAME_TOTAL_TIME_MS, totalTimeMillis);
        response.put(PARAM_NAME_PARTIAL_DERIVATED, partial);
        return response;
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

    private ProcessedHullSet calcMaxCaratheodroyNumberGraph(UndirectedSparseGraphTO<Integer, Integer> graph) {
        ProcessedHullSet processedCaratheodroySet = null;
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

    public ProcessedHullSet findCaratheodroySetBruteForce(UndirectedSparseGraphTO<Integer, Integer> graph, int currentSetSize) {
        ProcessedHullSet processedHullSet = null;
        if (graph == null || graph.getVertexCount() <= 0) {
            return processedHullSet;
        }
        Collection vertices = graph.getVertices();
        int veticesCount = vertices.size();
        Iterator<int[]> combinationsIterator = CombinatoricsUtils.combinationsIterator(graph.getVertexCount(), currentSetSize);
        while (combinationsIterator.hasNext()) {
            int[] currentSet = combinationsIterator.next();
            ProcessedHullSet hsp3g = hsp3(graph, currentSet);

//        return fecho;
            if (hsp3g != null) {
                processedHullSet = hsp3g;
                break;
            }
        }
        return processedHullSet;
    }

    public ProcessedHullSet hsp3(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] currentSet) {
        int currentSetSize = 0;
        ProcessedHullSet processedHullSet = null;
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
                    if (breankOnFirst && partial != null && !partial.isEmpty()) {
                        processedHullSet = new ProcessedHullSet();
                        processedHullSet.auxProcessor = aux;
                        processedHullSet.convexHull = hsp3g;
                        processedHullSet.caratheodorySet = hs;
                        processedHullSet.partial = calcDerivatedPartial(graph,
                                hsp3g, currentSet);
                        break;
                    }
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
            while (!mustBeIncluded.isEmpty()) {
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
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.hn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    private static final String PARAM_NAME_HULL_NUMBER = "number";
    private static final String PARAM_NAME_HULL_SET = "set";
    private static final String PARAM_NAME_CONVEX_HULL = "hs";
    private static final String PARAM_NAME_AUX_PROCESS = "aux";
    private int INCLUDED = 2;
    private int NEIGHBOOR_COUNT_INCLUDED = 1;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("random")
    public UndirectedSparseGraphTO randomGraph(
            @QueryParam("nvertices") @DefaultValue("5") Integer nvertices,
            @QueryParam("minDegree") @DefaultValue("1") Integer minDegree,
            @QueryParam("maxDegree") @DefaultValue("1") Integer maxDegree) {
//        UndirectedSparseGraphTO<Integer, Integer> graph = generateRandomGraphSimple(nvertices, minDegree, maxDegree);
        UndirectedSparseGraphTO<Integer, Integer> graph = generateRandomGraph(nvertices, minDegree, maxDegree);
        return graph;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("caratheodory")
    public Map<String, Object> calcCaratheodroyNumberGraph(String jsonGraph) {
        Integer caratheodoryNumber = -1;
        Integer[] caratheodorySet = null;
        Integer[] convexHull = null;
        int[] auxProcessor = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            BeanDeserializer bd = null;
            UndirectedSparseGraphTO<Integer, Integer> graphRead = mapper.readValue(jsonGraph, UndirectedSparseGraphTO.class);
            ProcessedHullSet caratheodoryNumberGraph = calcMinCaratheodroyNumberGraph(graphRead);
            if (caratheodoryNumberGraph != null
                    && !caratheodoryNumberGraph.caratheodorySet.isEmpty()) {
                caratheodoryNumber = caratheodoryNumberGraph.caratheodorySet.size();
                caratheodorySet = caratheodoryNumberGraph.caratheodorySet.toArray(new Integer[0]);
                auxProcessor = caratheodoryNumberGraph.auxProcessor;
                convexHull = caratheodoryNumberGraph.convexHull.toArray(new Integer[0]);
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
        return response;
    }

    private UndirectedSparseGraphTO<Integer, Integer> generateRandomGraph(Integer nvertices,
            Integer minDegree,
            Integer maxDegree) {
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
        int offset = maxDegree - minDegree;

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

    private ProcessedHullSet calcMinCaratheodroyNumberGraph(UndirectedSparseGraphTO<Integer, Integer> graph) {
        ProcessedHullSet processedCaratheodroySet = null;
        if (graph == null) {
            return processedCaratheodroySet;
        }
        int maxSizeSet = (graph.getVertexCount() + 1) / 2;
        int currentSize = 1;

        while (currentSize < maxSizeSet) {
            processedCaratheodroySet = findCaratheodroySetBruteForce(graph, currentSize);
            if (processedCaratheodroySet != null
                    && processedCaratheodroySet.caratheodorySet != null
                    && !processedCaratheodroySet.caratheodorySet.isEmpty()) {
                break;
            }
            currentSize++;
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
//            Set<Integer> hsp3g = hsp3(graph, currentSet);
//            Fecho hsp3
            Set<Integer> hsp3g = new HashSet<>();

            int[] aux = new int[graph.getVertexCount()];
            for (int i = 0; i < aux.length; i++) {
                aux[i] = 0;
            }

            Queue<Integer> mustBeIncluded = new ArrayDeque<>();
            for (Integer v : currentSet) {
                mustBeIncluded.add(v);
            }
            while (!mustBeIncluded.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                hsp3g.add(verti);
                aux[verti] = INCLUDED;
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
//        return fecho;
            if (hsp3g.size() == vertices.size()) {
                processedHullSet = new ProcessedHullSet();
                processedHullSet.auxProcessor = aux;
                processedHullSet.convexHull = hsp3g;
                processedHullSet.caratheodorySet = new HashSet<>(currentSetSize);
                for (int i : currentSet) {
                    processedHullSet.caratheodorySet.add(i);
                }
                break;
            }
        }
        return processedHullSet;
    }
}

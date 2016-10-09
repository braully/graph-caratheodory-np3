/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.hn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.braully.graph.generator.GraphGeneratorRandom;
import com.github.braully.graph.generator.IGraphGenerator;
import com.github.braully.graph.operation.IGraphOperation;
import edu.uci.ics.jung.graph.AbstractGraph;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.reflections.Reflections;

/**
 * REST Web Service
 *
 * @author strike
 */
@Path("graph")
public class GraphWS {

    private static final Logger log = Logger.getLogger(GraphWS.class.getSimpleName());

    private static final IGraphGenerator GRAPH_GENERATOR_DEFAULT = new GraphGeneratorRandom();
    private static final String NAME_PARAM_OUTPUT = "CONSOLE_USER_SESSION";

    public static final boolean verbose = false;
    public static final boolean breankOnFirst = true;

    @Context
    private HttpServletRequest request;

    private List<IGraphGenerator> generators = new ArrayList<>();

    private List<IGraphOperation> operators = new ArrayList<>();

    {
        Reflections reflections = new Reflections("com.github.braully.graph.generator");
        Set<Class<? extends IGraphGenerator>> classes = reflections.getSubTypesOf(IGraphGenerator.class);
        if (classes != null) {
            for (Class<? extends IGraphGenerator> cl : classes) {
                try {
                    generators.add(cl.newInstance());
                } catch (InstantiationException | IllegalAccessException ex) {
                    Logger.getLogger(GraphWS.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        reflections = new Reflections("com.github.braully.graph.operation");
        Set<Class<? extends IGraphOperation>> classesOperatio = reflections.getSubTypesOf(IGraphOperation.class);
        if (classes != null) {
            for (Class<? extends IGraphOperation> cl : classesOperatio) {
                try {
                    operators.add(cl.newInstance());
                } catch (InstantiationException | IllegalAccessException ex) {
                    Logger.getLogger(GraphWS.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("list-graph-operation")
    public List<Map.Entry<String, String>> listGraphOperation() {
        List<Map.Entry<String, String>> types = new ArrayList<>();
        if (operators != null) {
            for (IGraphOperation operator : operators) {
                types.add(new AbstractMap.SimpleEntry<String, String>(operator.getName(), operator.getDescription()));
            }
        }
        return types;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("list-graph-generator")
    public List<Map.Entry<String, String>> listGraphGenerator() {
        List<Map.Entry<String, String>> types = new ArrayList<>();
        if (generators != null) {
            for (IGraphGenerator generator : generators) {
                types.add(new AbstractMap.SimpleEntry<String, String>(generator.getName(), generator.getDescription()));
            }
        }
        return types;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("generate-graph")
    public UndirectedSparseGraphTO<Integer, Integer> generateGraph(
            @QueryParam("nvertices") @DefaultValue("5") Integer nvertices,
            @QueryParam("minDegree") @DefaultValue("1") Integer minDegree,
            @QueryParam("maxDegree") @DefaultValue("1.5") Double maxDegree,
            @QueryParam("typeGraph") @DefaultValue("random") String typeGraph) {

        AbstractGraph<Integer, Integer> graph = null;
        if (typeGraph != null) {
            for (IGraphGenerator generator : generators) {
                if (typeGraph.equalsIgnoreCase(generator.getName())) {
                    graph = generator.generateGraph(nvertices, minDegree, maxDegree);
                    break;
                }
            }
        }
        if (graph == null) {
            graph = GRAPH_GENERATOR_DEFAULT.generateGraph(nvertices, minDegree, maxDegree);
        }

        return (UndirectedSparseGraphTO) graph;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("operation")
    public Map<String, Object> operation(String jsonGraph) {
        Map<String, Object> result = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            UndirectedSparseGraphTO graph = mapper.readValue(jsonGraph, UndirectedSparseGraphTO.class);
            if (graph != null && operators != null && graph.getOperation() != null) {
                String operation = graph.getOperation();
                for (IGraphOperation graphOperation : operators) {
                    if (operation.equalsIgnoreCase(graphOperation.getName())) {
                        result = graphOperation.doOperation(graph);
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GraphWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public HttpSession getSession() {
        return this.request != null ? this.request.getSession(true) : null;
    }

    private BufferedReader getSessionOutputBufferdReader() {
        BufferedReader bf = new BufferedReader(new InputStreamReader(new ByteArrayInputStream("...".getBytes())));
        return bf;
    }

//    @GET
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("process-status")
    public Map<String, Object> processStatus() {
        Map<String, Object> map = new HashMap<>();
        List<String> lines = new ArrayList<>();
        BufferedReader bf = getSessionOutputBufferdReader();
        bf.lines().forEach((l) -> lines.add(l));
        map.put("processing", true);
        map.put("output", lines);
        return map;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("process-cancel")
    public Map<String, Object> cancelProcess() {
        return null;
    }

}

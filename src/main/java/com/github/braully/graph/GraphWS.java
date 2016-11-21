/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.braully.graph.generator.GraphGeneratorRandom;
import com.github.braully.graph.generator.IGraphGenerator;
import com.github.braully.graph.operation.IGraphOperation;
import edu.uci.ics.jung.graph.AbstractGraph;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.reflections.Reflections;

/**
 * REST Web Service -- Web Services Front end
 *
 * @author braully
 */
@Path("graph")
public class GraphWS {

    private static final Logger log = Logger.getLogger(GraphWS.class.getSimpleName());

    private static final IGraphGenerator GRAPH_GENERATOR_DEFAULT = new GraphGeneratorRandom();
    private static final String NAME_PARAM_OUTPUT = "CONSOLE_USER_SESSION";

    public static final boolean verbose = false;
    public static final boolean breankOnFirst = true;

    private static ExecuteOperation executeOperation = new ExecuteOperation();

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

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
                }
            }
            Collections.sort(generators, new Comparator<IGraphGenerator>() {
                @Override
                public int compare(IGraphGenerator t, IGraphGenerator t1) {
                    if (t != null && t1 != null) {
                        return t.getDescription().compareToIgnoreCase(t1.getDescription());
                    }
                    return 0;
                }
            });
        }

        reflections = new Reflections("com.github.braully.graph.operation");
        Set<Class<? extends IGraphOperation>> classesOperatio = reflections.getSubTypesOf(IGraphOperation.class);
        if (classes != null) {
            for (Class<? extends IGraphOperation> cl : classesOperatio) {
                try {
                    operators.add(cl.newInstance());
                } catch (InstantiationException | IllegalAccessException ex) {
                }
            }
            Collections.sort(operators, new Comparator<IGraphOperation>() {
                @Override
                public int compare(IGraphOperation t, IGraphOperation t1) {
                    if (t != null && t1 != null) {
                        return t.getName().compareToIgnoreCase(t1.getName());
                    }
                    return 0;
                }
            });
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("list-result")
    public List<DatabaseFacade.RecordResultGraph> listResults() {
        List<DatabaseFacade.RecordResultGraph> allResults = DatabaseFacade.getAllResults();
        return allResults;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("list-graph-operation")
    public List<Map.Entry<String, String>> listGraphOperation() {
        List<Map.Entry<String, String>> types = new ArrayList<>();
        if (operators != null) {
            for (IGraphOperation operator : operators) {
                types.add(new AbstractMap.SimpleEntry<String, String>(operator.getName(), operator.getTypeProblem()));
            }
        }
        return types;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("list-graph-generator")
    public List<Map.Entry<String, String[]>> listGraphGenerator() {
        List<Map.Entry<String, String[]>> types = new ArrayList<>();
        if (generators != null) {
            for (IGraphGenerator generator : generators) {
                types.add(new AbstractMap.SimpleEntry<String, String[]>(generator.getDescription(), generator.getParameters()));
            }
        }
        return types;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("generate-graph")
    public UndirectedSparseGraphTO<Integer, Integer> generateGraph(@Context UriInfo info) {
        MultivaluedMap<String, String> multiParams = info.getQueryParameters();
        Map<String, String> params = getTranslageParams(multiParams);
        String typeGraph = params.get("key");
        AbstractGraph<Integer, Integer> graph = null;
        if (typeGraph != null) {
            for (IGraphGenerator generator : generators) {
                if (typeGraph.equalsIgnoreCase(generator.getDescription())) {
                    graph = generator.generateGraph(params);
                    break;
                }
            }
        }
        if (graph == null) {
            graph = GRAPH_GENERATOR_DEFAULT.generateGraph(params);
        }

        return (UndirectedSparseGraphTO) graph;
    }

    Map<String, String> getTranslageParams(MultivaluedMap<String, String> multiParams) {
        Map<String, String> map = new HashMap<>();
        if (multiParams != null) {
            Set<String> keySet = multiParams.keySet();
            for (String key : keySet) {
                map.put(key, multiParams.getFirst(key));
            }
        }
        return map;
    }

    @POST
//    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("download-graph-csr")
    public void downloadGraphCsr(String jsonGraph) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            UndirectedSparseGraphTO graph = mapper.readValue(jsonGraph, UndirectedSparseGraphTO.class);
            if (graph != null) {
                response.setHeader("Content-disposition", "attachment; filename=" + "file.csr");
                response.setContentType("text/plain");
                PrintWriter writer = response.getWriter();
                UtilGraph.writerGraphToCsr(writer, graph);
                writer.flush();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Fail on dowload", e);
        }

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
            IGraphOperation operation = null;
            if (graph != null && operators != null && graph.getOperation() != null) {
                String strOperation = graph.getOperation();
                for (IGraphOperation graphOperation : operators) {
                    if (strOperation.equalsIgnoreCase(graphOperation.getName())) {
                        operation = graphOperation;
                        break;
                    }
                }

                synchronized (executeOperation) {
                    if (executeOperation.isProcessing()) {
                        throw new IllegalArgumentException("Processor busy (1-operantion in progress)");
                    }

                    if (operation != null) {
                        executeOperation = new ExecuteOperation();
                        executeOperation.setGraph(graph);
                        executeOperation.setGraphOperation(operation);
                        executeOperation.start();
//                        result = executeOperation.getResult();
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
//        BufferedReader bf = new BufferedReader(new InputStreamReader(new ByteArrayInputStream("...".getBytes())));
        BufferedReader bf = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(new byte[0])));
        return bf;
    }

//    @GET
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("process-status")
    public Map<String, Object> processStatus(Long lastTime) {
        Map<String, Object> map = new HashMap<>();
        List<String> lines = new ArrayList<>();
        List<LoggingEvent> loggingEvents = null;

        long last = 0;

        if (lastTime != null && lastTime > 0) {
            loggingEvents = WebConsoleAppender.getLoggingEvents(lastTime);
            last = lastTime;
        } else {
            loggingEvents = WebConsoleAppender.getLoggingEvents();
        }

        if (loggingEvents != null) {
            for (LoggingEvent e : loggingEvents) {
                Object message = e.getMessage();
                lines.add("" + message);
                if (e.getTimeStamp() > last) {
                    last = e.getTimeStamp();
                }
            }
        }

        map.put("processing", executeOperation.isProcessing());
        map.put("last", last);
        map.put("output", lines);
        map.put("result", executeOperation.getResult());
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

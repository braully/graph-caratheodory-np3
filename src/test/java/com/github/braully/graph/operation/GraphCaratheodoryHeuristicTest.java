/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.UtilGraph;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

/**
 * @author strike
 */
public class GraphCaratheodoryHeuristicTest extends TestCase {

    static File[] files = {new File("graph" + File.separator + "mft", "MTF_7-order-7-04.mat"),
            new File("graph" + File.separator + "mft", "MTF_14-order-14-21.mat"),
            new File("graph" + File.separator + "mft", "MTF_15-order-15-9720.mat"),
            new File("graph" + File.separator + "mft", "MTF_17-order-17-9999043451.mat"),
            new File("graph" + File.separator + "mft", "MTF_17-order-17-9999019890.mat"),
            new File("graph" + File.separator + "mft", "almhypo340.mat")
    };

    public GraphCaratheodoryHeuristicTest(String testName) {
        super(testName);
    }

    public void testDoOperation() throws FileNotFoundException, IOException {
        System.out.println("doOperation");

 /*           for (File file : files) {
            UndirectedSparseGraphTO<Integer, Integer> graphRead = UtilGraph.loadGraphAdjMatrix(new FileInputStream(file));
            GraphCaratheodoryHeuristic instance = new GraphCaratheodoryHeuristic();
            Set<Integer> buildMaxCaratheodorySet = instance.buildMaxCaratheodorySet(graphRead);
            assertNotNull(buildMaxCaratheodorySet);
            assertFalse(buildMaxCaratheodorySet.isEmpty());
            assertTrue(instance.isCaratheodorySet(graphRead, buildMaxCaratheodorySet));
            System.out.println(file.getName() + ": n=" + buildMaxCaratheodorySet.size() + " s=" + buildMaxCaratheodorySet);
        }*/
    }
}

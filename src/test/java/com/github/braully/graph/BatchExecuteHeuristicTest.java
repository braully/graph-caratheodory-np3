/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import java.io.File;
import junit.framework.TestCase;

/**
 *
 * @author strike
 */
public class BatchExecuteHeuristicTest extends TestCase {

    public void testProcessFile() throws Exception {
        System.out.println("processFile");
        File file = new File("graph" + File.separator + "mft", "MTF_17-order-17-9999019890.mat");
        BatchExecuteHeuristic.processFile(file);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.graph.util.Pair;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import tmp.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.genetics.*;
import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.RandomKey;

/**
 *
 * @author strike
 */
public class GACombPermutation {

    private static String fileDump = "/home/strike/.comb-moore-java-ga.txt";
    private static final long HOUR = 1000 * 60 * 60;

    // parameters for the GA
    private static final int DIMENSION = 50;
    private static final int POPULATION_SIZE = DIMENSION * 6;
    private static final int NUM_GENERATIONS = DIMENSION * 15;
    private static final double ELITISM_RATE = 0.2;
    private static final double CROSSOVER_RATE = 1;
    private static final double MUTATION_RATE = 0.08;
    private static final int TOURNAMENT_ARITY = 2;

    private static final UndirectedSparseGraphTO graph = new UndirectedSparseGraphTO();
    private static final UndirectedSparseGraphTO subgraph = new UndirectedSparseGraphTO();

    static {
//        graph.addEdgeC(0, 1).addEdgeC(1, 2).addEdgeC(2, 3).addEdgeC(3, 0);
//        subgraph.addEdgeC(0, 1).addEdgeC(1, 3).addEdgeC(2, 3).addEdgeC(3, 1);

        subgraph.addEdgeC(0, 6).addEdgeC(1, 7).addEdgeC(2, 8).addEdgeC(3, 9).addEdgeC(4, 10).addEdgeC(5, 11).addEdgeC(0, 12).addEdgeC(12, 42).addEdgeC(12, 7)
                .addEdgeC(6, 13).addEdgeC(49, 42).addEdgeC(13, 42).addEdgeC(13, 1).addEdgeC(0, 14).addEdgeC(14, 43).addEdgeC(14, 8).addEdgeC(6, 15).addEdgeC(49, 43)
                .addEdgeC(15, 43).addEdgeC(15, 2).addEdgeC(0, 16).addEdgeC(16, 44).addEdgeC(16, 9).addEdgeC(6, 17).addEdgeC(49, 44).addEdgeC(17, 44).addEdgeC(17, 3).addEdgeC(0, 18)
                .addEdgeC(18, 45).addEdgeC(18, 10).addEdgeC(6, 19).addEdgeC(49, 45).addEdgeC(19, 45).addEdgeC(19, 4).addEdgeC(0, 20).addEdgeC(20, 46).addEdgeC(20, 11).addEdgeC(6, 21)
                .addEdgeC(49, 46).addEdgeC(21, 46).addEdgeC(21, 5).addEdgeC(1, 22).addEdgeC(22, 44).addEdgeC(22, 8).addEdgeC(7, 23).addEdgeC(49, 44).addEdgeC(23, 44).addEdgeC(23, 2).addEdgeC(1, 24)
                .addEdgeC(24, 45).addEdgeC(24, 9).addEdgeC(7, 25).addEdgeC(49, 45).addEdgeC(25, 45).addEdgeC(25, 3).addEdgeC(1, 26).addEdgeC(26, 46).addEdgeC(26, 10).addEdgeC(7, 27).addEdgeC(49, 46)
                .addEdgeC(27, 46).addEdgeC(27, 4).addEdgeC(1, 28).addEdgeC(28, 43).addEdgeC(28, 11).addEdgeC(7, 29).addEdgeC(49, 43).addEdgeC(29, 43).addEdgeC(29, 5).addEdgeC(2, 30).addEdgeC(30, 46)
                .addEdgeC(30, 9).addEdgeC(8, 31).addEdgeC(49, 46).addEdgeC(31, 46).addEdgeC(31, 3).addEdgeC(2, 32).addEdgeC(32, 42).addEdgeC(32, 10).addEdgeC(8, 33).addEdgeC(49, 42).addEdgeC(33, 42)
                .addEdgeC(33, 4).addEdgeC(2, 34).addEdgeC(34, 45).addEdgeC(34, 11).addEdgeC(8, 35).addEdgeC(49, 45).addEdgeC(35, 45).addEdgeC(35, 5).addEdgeC(3, 36).addEdgeC(36, 43).addEdgeC(36, 10)
                .addEdgeC(9, 37).addEdgeC(49, 43).addEdgeC(37, 43).addEdgeC(37, 4).addEdgeC(3, 38).addEdgeC(38, 42).addEdgeC(38, 11).addEdgeC(9, 39).addEdgeC(49, 42).addEdgeC(39, 42).addEdgeC(39, 5)
                .addEdgeC(4, 40).addEdgeC(40, 44).addEdgeC(40, 11).addEdgeC(10, 41).addEdgeC(49, 44).addEdgeC(41, 44).addEdgeC(41, 5).addEdgeC(47, 49).addEdgeC(49, 48).addEdgeC(47, 0).addEdgeC(48, 6)
                .addEdgeC(47, 1).addEdgeC(48, 7).addEdgeC(47, 2).addEdgeC(48, 8).addEdgeC(47, 3).addEdgeC(48, 9).addEdgeC(47, 4).addEdgeC(48, 10).addEdgeC(47, 5).addEdgeC(48, 11);

        graph.addEdgeC(0, 1).addEdgeC(0, 2).addEdgeC(0, 3).addEdgeC(36, 40).addEdgeC(0, 4).addEdgeC(0, 5).addEdgeC(0, 6).addEdgeC(0, 7).addEdgeC(1, 9).addEdgeC(37, 43).addEdgeC(1, 11).addEdgeC(37, 49)
                .addEdgeC(1, 15).addEdgeC(1, 17).addEdgeC(1, 26).addEdgeC(1, 49).addEdgeC(2, 10).addEdgeC(2, 12).addEdgeC(38, 47).addEdgeC(2, 16).addEdgeC(2, 18).addEdgeC(2, 25).addEdgeC(2, 48)
                .addEdgeC(3, 13).addEdgeC(39, 44).addEdgeC(3, 14).addEdgeC(39, 48).addEdgeC(3, 19).addEdgeC(3, 20).addEdgeC(3, 27).addEdgeC(3, 28).addEdgeC(4, 21).addEdgeC(4, 22).addEdgeC(40, 46)
                .addEdgeC(4, 23).addEdgeC(4, 24).addEdgeC(4, 29).addEdgeC(4, 30).addEdgeC(5, 31).addEdgeC(5, 32).addEdgeC(41, 45).addEdgeC(5, 33).addEdgeC(5, 34).addEdgeC(5, 35).addEdgeC(5, 36)
                .addEdgeC(6, 37).addEdgeC(6, 38).addEdgeC(6, 39).addEdgeC(6, 40).addEdgeC(6, 41).addEdgeC(6, 42).addEdgeC(7, 8).addEdgeC(7, 43).addEdgeC(7, 44).addEdgeC(7, 45).addEdgeC(7, 46)
                .addEdgeC(7, 47).addEdgeC(8, 23).addEdgeC(8, 28).addEdgeC(8, 36).addEdgeC(8, 42).addEdgeC(8, 48).addEdgeC(8, 49).addEdgeC(9, 14).addEdgeC(9, 29).addEdgeC(9, 34).addEdgeC(9, 41)
                .addEdgeC(9, 47).addEdgeC(9, 48).addEdgeC(10, 14).addEdgeC(10, 23).addEdgeC(10, 26).addEdgeC(10, 32).addEdgeC(10, 38).addEdgeC(10, 46).addEdgeC(11, 24).addEdgeC(11, 25).addEdgeC(11, 28)
                .addEdgeC(11, 35).addEdgeC(11, 39).addEdgeC(11, 46).addEdgeC(12, 13).addEdgeC(12, 24).addEdgeC(12, 33).addEdgeC(12, 40).addEdgeC(12, 47).addEdgeC(12, 49).addEdgeC(13, 15).addEdgeC(13, 23)
                .addEdgeC(13, 34).addEdgeC(13, 39).addEdgeC(13, 43).addEdgeC(14, 24).addEdgeC(14, 36).addEdgeC(14, 37).addEdgeC(14, 44).addEdgeC(15, 16).addEdgeC(15, 30).addEdgeC(15, 36).addEdgeC(15, 38)
                .addEdgeC(15, 45).addEdgeC(16, 20).addEdgeC(16, 29).addEdgeC(16, 35).addEdgeC(16, 42).addEdgeC(16, 44).addEdgeC(17, 18).addEdgeC(17, 23).addEdgeC(17, 27).addEdgeC(17, 31).addEdgeC(17, 40)
                .addEdgeC(17, 44).addEdgeC(18, 22).addEdgeC(18, 28).addEdgeC(18, 34).addEdgeC(18, 37).addEdgeC(18, 45).addEdgeC(19, 21).addEdgeC(19, 26).addEdgeC(19, 35).addEdgeC(19, 40).addEdgeC(19, 45)
                .addEdgeC(19, 48).addEdgeC(20, 22).addEdgeC(20, 31).addEdgeC(20, 41).addEdgeC(20, 46).addEdgeC(20, 49).addEdgeC(21, 25).addEdgeC(21, 34).addEdgeC(21, 38).addEdgeC(21, 44).addEdgeC(21, 49)
                .addEdgeC(22, 26).addEdgeC(22, 36).addEdgeC(22, 39).addEdgeC(22, 47).addEdgeC(23, 35).addEdgeC(23, 41).addEdgeC(24, 31).addEdgeC(24, 42).addEdgeC(24, 45).addEdgeC(25, 27).addEdgeC(25, 36)
                .addEdgeC(25, 41).addEdgeC(25, 43).addEdgeC(26, 33).addEdgeC(26, 42).addEdgeC(26, 43).addEdgeC(27, 30).addEdgeC(27, 32).addEdgeC(27, 42).addEdgeC(27, 47).addEdgeC(28, 29).addEdgeC(28, 33)
                .addEdgeC(28, 38).addEdgeC(29, 32).addEdgeC(29, 40).addEdgeC(29, 43).addEdgeC(30, 33).addEdgeC(30, 37).addEdgeC(30, 46).addEdgeC(30, 48).addEdgeC(31, 38).addEdgeC(31, 43).addEdgeC(31, 48)
                .addEdgeC(32, 39).addEdgeC(32, 45).addEdgeC(32, 49).addEdgeC(33, 41).addEdgeC(33, 44).addEdgeC(34, 42).addEdgeC(34, 46).addEdgeC(35, 37).addEdgeC(35, 47);
    }

    // numbers from 0 to N-1
    private static List<Integer> sequence = new ArrayList<Integer>();

    static {
        for (int i = 0; i < DIMENSION; i++) {
            sequence.add(i);
        }
    }

    public static void main(String... args) {
        // to test a stochastic algorithm is hard, so this will rather be an usage example

        // initialize a new genetic algorithm
        GeneticAlgorithm ga = new GeneticAlgorithm(
                new OnePointCrossover<Integer>(),
                CROSSOVER_RATE,
                new RandomKeyMutation(),
                MUTATION_RATE,
                new TournamentSelection(TOURNAMENT_ARITY)
        );

        // initial population
        Population initial = randomPopulation(args);
        System.out.print("Graph e-");
        System.out.print(graph.getEdgeCount());
        System.out.print(" Subgraph e-");
        System.out.println(subgraph.getEdgeCount());

        System.out.println("Initial population:");
        System.out.println(initial.getFittestChromosome());
        long lastime = System.currentTimeMillis();

        // stopping conditions
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        // best initial chromosome
        Chromosome bestInitial = initial.getFittestChromosome();

        // run the algorithm
//        Population finalPopulation = ga.evolve(initial, stopCond);
        double bestfit = initial.getFittestChromosome().fitness();
        Population current = initial;
        int generationsEvolved = 0;
//        while (!stopCond.isSatisfied(current)) {
        while (bestfit != 0.0) {
            current = ga.nextGeneration(current);
            generationsEvolved++;
            Chromosome bestFinal = current.getFittestChromosome();
//            System.out.print(bestFinal);
            double atualfit = bestFinal.getFitness();
            if (atualfit > bestfit || System.currentTimeMillis() - lastime > HOUR) {
                lastime = System.currentTimeMillis();
                System.out.print(generationsEvolved);
                System.out.print("-");
                bestfit = atualfit;
                String strbest = bestFinal.toString() + "\n";
                dumpString(strbest);
                System.out.print(strbest);
                System.out.println();
            }
        }

        // best chromosome from the final population
        Chromosome bestFinal = current.getFittestChromosome();
        System.out.println("Best result:");
        System.out.println(bestFinal);

        // the only thing we can test is whether the final solution is not worse than the initial one
        // however, for some implementations of GA, this need not be true :)
//        Assert.assertTrue(bestFinal.compareTo(bestInitial) > 0);
        //System.out.println(bestInitial);
        //System.out.println(bestFinal);
    }

    /**
     * Initializes a random population
     */
    private static ElitisticListPopulation randomPopulation(String... args) {
        List<Chromosome> popList = new ArrayList<Chromosome>();
        int i = 0;
        if (args != null && args.length > 0) {
            if (args.length == DIMENSION || ((args = args[0].split(",")) != null && args.length == DIMENSION)) {
                List<Integer> start = new ArrayList<>();
                for (String str : args) {
                    start.add(Integer.parseInt(str.trim().replaceAll("\\D", "")));
                }
                Chromosome randChrom = new MinPermutations(RandomKey.inducedPermutation(sequence, start));
                popList.add(randChrom);
                i++;
            }
        }
        for (; i < POPULATION_SIZE; i++) {
            Chromosome randChrom = new MinPermutations(RandomKey.randomPermutation(DIMENSION));
            popList.add(randChrom);
        }
        return new ElitisticListPopulation(popList, popList.size(), ELITISM_RATE);
    }

    /**
     * Chromosomes representing a permutation of (0,1,2,...,DIMENSION-1).
     *
     * The goal is to sort the sequence.
     */
    private static class MinPermutations extends RandomKey<Integer> {

        public MinPermutations(List<Double> representation) {
            super(representation);
        }

//        public double fitness() {
//            int res = 0;
//            List<Integer> decoded = decode(sequence);
//            for (int i = 0; i < decoded.size(); i++) {
//                int value = (Integer) decoded.get(i);
//                if (value != i) {
//                    // bad position found
//                    res += Math.abs(value - i);
//                }
//            }
//            // the most fitted chromosome is the one with minimal error
//            // therefore we must return negative value
//            return -res;
//        }
        public double fitness() {
            int res = 0;
            List<Integer> remap = decode(sequence);

            List<Integer> vertices1 = (List<Integer>) graph.getVertices();
            Collection<Pair<Integer>> pairs = subgraph.getPairs();
            Collection<Pair<Integer>> thispairs = graph.getPairs();
            Iterator<Pair<Integer>> iterator = pairs.iterator();
            Pair<Integer> pair = null;

            while (iterator.hasNext()) {
                Pair<Integer> edge = iterator.next();
                Integer first = edge.getFirst();
                Integer second = edge.getSecond();
                if (remap != null) {
                    int indexOf = remap.indexOf(first);
                    first = vertices1.get(indexOf);
                    indexOf = remap.indexOf(second);
                    second = vertices1.get(indexOf);
                }
                pair = new Pair<Integer>(first, second);
                boolean contains = thispairs.contains(pair);
                if (!contains) {
                    pair = new Pair<Integer>(second, first);
                    contains = thispairs.contains(pair);
                }
                if (!contains) {
                    res++;
                }
            }
            // the most fitted chromosome is the one with minimal error
            // therefore we must return negative value
            return -res;
        }

        @Override
        public AbstractListChromosome<Double> newFixedLengthChromosome(List<Double> chromosomeRepresentation) {
            return new MinPermutations(chromosomeRepresentation);
        }
    }

    private static void dumpString(String strt) {
        try {
            new FileWriter(fileDump, true).append(strt).close();
        } catch (IOException ex) {
            Logger.getLogger(CombMooreGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void dumpArray(LinkedList<Integer> arr, String preset) {
        String strArra = "h-arr[" + arr.size() + "]: " + arr.toString() + "\n";
        try {
            new FileWriter(fileDump, true).append(strArra).close();
        } catch (IOException ex) {
            Logger.getLogger(CombMooreGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.genetics.*;
import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.RandomKey;
import static tmp.CombMooreGraph.mapInvalidPositions;

/**
 *
 * @author strike
 */
public class GACombGraphMoore {

    private static String fileDump = "/home/strike/.comb-moore-java-ga.txt";
    private static int max_length_file = 4000;
    private static final long HOUR = 1000 * 60 * 60 * 12;

    // parameters for the GA
    private static final int K = 57;
    private static final int KO = K - 2;
    private static final int LEN = ((KO + 1) * KO) / 2;
    private static final int DIMENSION = LEN;
    private static final int POPULATION_SIZE = 700;//Math.max(DIMENSION * 10, 200);
    private static final int NUM_GENERATIONS = DIMENSION * 15;
    private static final double ELITISM_RATE = 0.2;
    private static final double CROSSOVER_RATE = 1;
    private static final double MUTATION_RATE = 0.1;
    private static final int TOURNAMENT_ARITY = 2;
    private static final Map<Integer, List<Integer>> MAP_EXCLUDED_POSITIONS = Collections.unmodifiableMap(mapInvalidPositions(K));

    // numbers from 0 to N-1
    private static List<Integer> sequence = new ArrayList<Integer>();

    static {
        for (int i = 0; i < DIMENSION; i++) {
            sequence.add((i % KO));
        }
    }

    static {

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
//                System.out.print(generationsEvolved);
//                System.out.print("-");
                bestfit = atualfit;
                String strbest = generationsEvolved + "-f=" + atualfit + "-" + ((MinPermutations) bestFinal).decode(sequence).toString().replaceAll(" ", "") + "\n";
                dumpString(strbest);
                System.out.println(strbest);
                System.out.println(bestFinal);
//                System.out.println();
                lastime = System.currentTimeMillis();
            }
        }

        // best chromosome from the final population
        Chromosome bestFinal = current.getFittestChromosome();
        System.out.println("Best initial:");
        System.out.println(bestInitial);
        System.out.println(((MinPermutations) bestInitial).decode(sequence));
        System.out.println("Best result:");
        System.out.println(bestFinal);
        System.out.println(((MinPermutations) bestFinal).decode(sequence));

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
                    start.add(Integer.parseInt(str));
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

        public double fitness() {
            List<Integer> arr = decode(sequence);
            int res = 0;
            for (int i = 0; i < arr.size(); i++) {
                int pos = i;
                List<Integer> posExcl = MAP_EXCLUDED_POSITIONS.get(pos);
                for (int j = 0; j < posExcl.size(); j++) {
                    if (arr.get(i).equals(posExcl.get(j))) {
                        res++;
                    }
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
            FileWriter fileWriter = new FileWriter(fileDump, true);
            if (strt.length() > max_length_file) {
                int length = strt.length();
                for (int i = 0; i < length; i += max_length_file) {
                    fileWriter.append(strt.substring(i, Math.min(length, i + max_length_file))).append("\n");
                }
            } else {
                fileWriter.append(strt);
            }
            fileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(CombMooreGraph.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void dumpArray(LinkedList<Integer> arr, String preset) {
        String strArra = "h-arr[" + arr.size() + "]: " + arr.toString() + "\n";
        try {
            new FileWriter(fileDump, true).append(strArra).close();

        } catch (IOException ex) {
            Logger.getLogger(CombMooreGraph.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
}

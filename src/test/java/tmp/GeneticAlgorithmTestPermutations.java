/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tmp;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.genetics.*;
import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.RandomKey;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author strike
 */
public class GeneticAlgorithmTestPermutations {

    // parameters for the GA
    private static final int DIMENSION = 20;
    private static final int POPULATION_SIZE = 80;
    private static final int NUM_GENERATIONS = 200;
    private static final double ELITISM_RATE = 0.2;
    private static final double CROSSOVER_RATE = 1;
    private static final double MUTATION_RATE = 0.08;
    private static final int TOURNAMENT_ARITY = 2;

    // numbers from 0 to N-1
    private static List<Integer> sequence = new ArrayList<Integer>();

    static {
        for (int i = 0; i < DIMENSION; i++) {
            sequence.add(i);
        }
    }

    @Test
    public void test() {
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
        Population initial = randomPopulation();
        System.out.print("Initial population");
        System.out.println(initial.getFittestChromosome());

        // stopping conditions
        StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);

        // best initial chromosome
        Chromosome bestInitial = initial.getFittestChromosome();

        // run the algorithm
        Population finalPopulation = ga.evolve(initial, stopCond);

        // best chromosome from the final population
        Chromosome bestFinal = finalPopulation.getFittestChromosome();
        System.out.print("Best result: ");
        System.out.println(bestFinal);

        // the only thing we can test is whether the final solution is not worse than the initial one
        // however, for some implementations of GA, this need not be true :)
        Assert.assertTrue(bestFinal.compareTo(bestInitial) > 0);

        //System.out.println(bestInitial);
        //System.out.println(bestFinal);
    }

    /**
     * Initializes a random population
     */
    private static ElitisticListPopulation randomPopulation() {
        List<Chromosome> popList = new ArrayList<Chromosome>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
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
            int res = 0;
            List<Integer> decoded = decode(sequence);
            for (int i = 0; i < decoded.size(); i++) {
                int value = (Integer) decoded.get(i);
                if (value != i) {
                    // bad position found
                    res += Math.abs(value - i);
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
}

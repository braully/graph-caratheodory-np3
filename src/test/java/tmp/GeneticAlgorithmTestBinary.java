package tmp;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.BinaryChromosome;
import org.apache.commons.math3.genetics.BinaryMutation;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.FixedGenerationCount;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.OnePointCrossover;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.genetics.TournamentSelection;
import org.junit.Assert;
import org.junit.Test;

/**
 * This is also an example of usage.
 */
public class GeneticAlgorithmTestBinary {

    // parameters for the GA
    private static final int DIMENSION = 50;
    private static final int POPULATION_SIZE = 50;
    private static final int NUM_GENERATIONS = 50;
    private static final double ELITISM_RATE = 0.2;
    private static final double CROSSOVER_RATE = 1;
    private static final double MUTATION_RATE = 0.1;
    private static final int TOURNAMENT_ARITY = 2;

    @Test
    public void test() {
        // to test a stochastic algorithm is hard, so this will rather be an usage example

        // initialize a new genetic algorithm
        GeneticAlgorithm ga = new GeneticAlgorithm(
                new OnePointCrossover<Integer>(),
                CROSSOVER_RATE, // all selected chromosomes will be recombined (=crosssover)
                new BinaryMutation(),
                MUTATION_RATE,
                new TournamentSelection(TOURNAMENT_ARITY)
        );

        Assert.assertEquals(0, ga.getGenerationsEvolved());

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
        Assert.assertEquals(NUM_GENERATIONS, ga.getGenerationsEvolved());

    }

    /**
     * Initializes a random population.
     */
    private static ElitisticListPopulation randomPopulation() {
        List<Chromosome> popList = new LinkedList<>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            BinaryChromosome randChrom = new FindOnes(BinaryChromosome.randomBinaryRepresentation(DIMENSION));
            popList.add(randChrom);
        }
        return new ElitisticListPopulation(popList, popList.size(), ELITISM_RATE);
    }

    /**
     * Chromosomes represented by a binary chromosome.
     *
     * The goal is to set all bits (genes) to 1.
     */
    private static class FindOnes extends BinaryChromosome {

        public FindOnes(List<Integer> representation) {
            super(representation);
        }

        /**
         * Returns number of elements != 0
         */
        @Override
        public double fitness() {
            int num = 0;
            for (int val : this.getRepresentation()) {
                if (val != 0) {
                    num++;
                }
            }
            // number of elements >= 0
            return num;
        }

        @Override
        public AbstractListChromosome<Integer> newFixedLengthChromosome(List<Integer> chromosomeRepresentation) {
            return new FindOnes(chromosomeRepresentation);
        }

    }
}

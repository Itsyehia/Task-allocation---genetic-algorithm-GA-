/*
 Yehia Mohamed Youssef 20215051
 Nourhan Darwish 20216112
 Albert Maged 20216021
*/


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class GeneticAlgorithm {
    private static final double Pc = 0.5; // Crossover probability,between 0.4 and 0.7

    private static final Random random = new Random();

    public static List<List<Integer>> initializePopulation(int populationSize, int numberOfGenes) {
        List<List<Integer>> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            List<Integer> chromosome = new ArrayList<>();
            for (int j = 0; j < numberOfGenes; j++) {
                chromosome.add(random.nextInt(2)); // Randomly add 0 or 1
            }
            population.add(chromosome);
        }
        return population;
    }

    public static int calculateFitness(List<Integer> chromosome, List<Integer> taskTimes, int maxTimeLimit) {
        int process1Time = 0;
        int process2Time = 0;

        for (int i = 0; i < chromosome.size(); i++) {
            if (chromosome.get(i) == 1) {
                process1Time += taskTimes.get(i);
            } else {
                process2Time += taskTimes.get(i);
            }
        }

        if (process1Time > maxTimeLimit || process2Time > maxTimeLimit) {
            return Integer.MAX_VALUE; // Penalize infeasible solutions heavily
        }

        return Math.max(process1Time, process2Time);
    }

    public static List<List<Integer>> rouletteWheelSelection(List<List<Integer>> population, List<Integer> fitnessValues) {
        // List to store the selected individuals
        List<List<Integer>> selectedPopulation = new ArrayList<>();

        // Array to store selection probabilities for each individual
        double[] probabilities = new double[fitnessValues.size()];
        // Array to store cumulative probabilities for selecting ranges
        double[] cumulativeProbabilities = new double[fitnessValues.size()];

        // Calculate total fitness as the sum of 1/fitness (inverse fitness, minimize)
        double totalFitness = 0;
        for (int fitness : fitnessValues) {
            // If fitness is max, we skip it (0 probability), else we use 1/fitness
            totalFitness += (fitness == Integer.MAX_VALUE) ? 0 : 1.0 / fitness;
        }

        if (totalFitness == 0) { // No feasible solutions
            // Return the population as is, or handle accordingly
            return new ArrayList<>(population);
        }

        // Calculate individual probabilities for selection based on total fitness
        for (int i = 0; i < fitnessValues.size(); i++) {
            probabilities[i] = (fitnessValues.get(i) == Integer.MAX_VALUE ? 0 : 1.0 / fitnessValues.get(i)) / totalFitness;
        }

        // Calculate cumulative probabilities, used for defining ranges
        cumulativeProbabilities[0] = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            cumulativeProbabilities[i] = cumulativeProbabilities[i - 1] + probabilities[i];
        }

        // For each individual, select one based on their cumulative probability range
        for (int i = 0; i < population.size(); i++) {
            // Generate a random number between 0 and 1
            double rand = random.nextDouble();
            // Find the individual whose cumulative probability range contains 'rand'
            for (int j = 0; j < cumulativeProbabilities.length; j++) {
                if (rand <= cumulativeProbabilities[j]) {
                    // Add the selected individual to the new population
                    selectedPopulation.add(new ArrayList<>(population.get(j)));
                    break;
                }
            }
        }

        // Return the newly selected population
        return selectedPopulation;
    }

    public static List<List<Integer>> crossover(List<List<Integer>> selectedPopulation) {
        List<List<Integer>> newPopulation = new ArrayList<>();

        for (int i = 0; i < selectedPopulation.size(); i += 2) {
            List<Integer> parent1 = selectedPopulation.get(i);
            List<Integer> parent2 = selectedPopulation.get((i + 1) % selectedPopulation.size()); // Wrap around for odd sizes

            // Generate a random crossover point between 1 and L-1
            int chromosomeLength = parent1.size();

            // r1
            int crossoverPoint = random.nextInt(chromosomeLength - 1) + 1; // r1 in range [1, L-1]

            // Generate random number r2 to decide if crossover occurs
            double r2 = random.nextDouble();

            if (r2 <= Pc) {
                // Perform crossover
                List<Integer> child1 = new ArrayList<>(parent1.subList(0, crossoverPoint));
                List<Integer> child2 = new ArrayList<>(parent2.subList(0, crossoverPoint));

                child1.addAll(parent2.subList(crossoverPoint, chromosomeLength));
                child2.addAll(parent1.subList(crossoverPoint, chromosomeLength));

                newPopulation.add(child1);
                newPopulation.add(child2);
            } else {
                // No crossover, add parents as they are
                newPopulation.add(new ArrayList<>(parent1));
                newPopulation.add(new ArrayList<>(parent2));
            }
        }

        return newPopulation;
    }

    public static List<Integer> mutate(List<Integer> chromosome, double mutationRate) {
        for (int i = 0; i < chromosome.size(); i++) {
            // Generate random number ri in range [0, 1]
            double ri = random.nextDouble();

            // If ri is less than or equal to mutation rate, flip the bit
            if (ri <= mutationRate) {
                chromosome.set(i, chromosome.get(i) == 0 ? 1 : 0); // Flip bit
            }
        }
        return chromosome;
    }

    public static List<List<Integer>> elitismReplacement(List<List<Integer>> population, List<Integer> fitnessValues, int elitismCount) {
        // Create a new population that starts as a copy of the existing population
        List<List<Integer>> newPopulation = new ArrayList<>(population);

        // Loop to select the best individuals based on fitness values
        for (int i = 0; i < elitismCount; i++) {
            // Find the index of the individual with the lowest (best) fitness value
            int bestIndex = fitnessValues.indexOf(Collections.min(fitnessValues));

            // Place this best individual at the beginning of the new population (at position i)
            newPopulation.set(i, population.get(bestIndex));

            // Mark the selected individual's fitness as Integer.MAX_VALUE to avoid selecting it again
            fitnessValues.set(bestIndex, Integer.MAX_VALUE); // Temporarily mark as 'used'
        }

        // Return the new population with the elite members preserved
        return newPopulation;
    }

    public static void evolutionaryLoop(int testCaseIndex, List<List<Integer>> population, List<Integer> taskTimes, int maxTimeLimit, double mutationRate, int elitismCount) {
        boolean optimalFound = false;
        List<Integer> bestChromosome = null;
        int bestFitness = Integer.MAX_VALUE;
        int generation = 0;
        int maxGenerations = 1000; // Define a reasonable limit for generations

        // Calculate the optimal fitness target as half the total sum of task times
        int optimalFitness = taskTimes.stream().mapToInt(Integer::intValue).sum() / 2;

        while (!optimalFound && generation < maxGenerations) {
            List<Integer> fitnessValues = new ArrayList<>();
            for (List<Integer> chromosome : population) {
                int fitness = calculateFitness(chromosome, taskTimes, maxTimeLimit);
                fitnessValues.add(fitness);

                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestChromosome = new ArrayList<>(chromosome);
                }
            }

            optimalFound = fitnessValues.contains(bestFitness) && bestFitness <= maxTimeLimit;
            if (optimalFound) break;
            if (bestFitness == optimalFitness) { break; }
            // Selection
            List<List<Integer>> selectedPopulation = rouletteWheelSelection(population, fitnessValues);
            // Crossover
            List<List<Integer>> newPopulation = crossover(selectedPopulation);

            // Mutation
            for (int i = 0; i < newPopulation.size(); i++) {
                newPopulation.set(i, mutate(newPopulation.get(i), mutationRate));
            }

            // Elitism
            population = elitismReplacement(newPopulation, fitnessValues, elitismCount);
            generation++;
        }

        // Output the best solution details
        System.out.println("Test Case #" + testCaseIndex);
        if (bestFitness <= maxTimeLimit) {
            System.out.println("Best Fitness (Score): " + bestFitness);
            System.out.println("Best Chromosome: " + bestChromosome);

            // Split tasks between two processes based on the best chromosome
            List<Integer> process1Tasks = new ArrayList<>();
            List<Integer> process2Tasks = new ArrayList<>();
            int process1Time = 0;
            int process2Time = 0;

            for (int i = 0; i < bestChromosome.size(); i++) {
                if (bestChromosome.get(i) == 1) {
                    process1Tasks.add(taskTimes.get(i));
                    process1Time += taskTimes.get(i);
                } else {
                    process2Tasks.add(taskTimes.get(i));
                    process2Time += taskTimes.get(i);
                }
            }

            System.out.println("Process 1 Tasks: " + process1Tasks + " | Total Time: " + process1Time);
            System.out.println("Process 2 Tasks: " + process2Tasks + " | Total Time: " + process2Time);
        } else {
            System.out.println("No solution");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        String filePath = "input.txt";
        readAndProcessFile(filePath);
    }

    public static void readAndProcessFile(String filePath) {

        // Try-with-resources to automatically close BufferedReader after use
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            // Read the first line to get the number of test cases
            String line = br.readLine();
            if (line == null) { // Check if the file is empty
                System.out.println("File is empty.");
                return;
            }

            int testCases = Integer.parseInt(line.trim()); // Parse number of test cases
            if (testCases < 1) { // Ensure there's at least one test case
                System.out.println("Number of test cases must be at least 1.");
                return;
            }

            // Process each test case
            for (int i = 0; i < testCases; i++) {
                // Read the max time limit for the current test case
                line = br.readLine();
                if (line == null) {
                    System.out.println("Unexpected end of file.");
                    return;
                }
                int maxTimeLimit = Integer.parseInt(line.trim());

                // Read the number of tasks for the current test case
                line = br.readLine();
                if (line == null) {
                    System.out.println("Unexpected end of file.");
                    return;
                }
                int numberOfTasks = Integer.parseInt(line.trim());

                // Read each task time and store it in a list
                List<Integer> taskTimes = new ArrayList<>();
                for (int j = 0; j < numberOfTasks; j++) {
                    line = br.readLine();
                    if (line == null) {
                        System.out.println("Unexpected end of file.");
                        return;
                    }
                    int taskTime = Integer.parseInt(line.trim());
                    taskTimes.add(taskTime);
                }
                // Set evolutionary algorithm parameters
                int populationSize = 50;
                double mutationRate = 0.05;
                int elitismCount = 2;

                // Initialize the population with individuals (solutions) based on the number of tasks
                List<List<Integer>> population = initializePopulation(populationSize, numberOfTasks);

                evolutionaryLoop(i + 1, population, taskTimes, maxTimeLimit, mutationRate, elitismCount);
            }
        } catch (IOException e) {
            // Catch and report file reading errors
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            // Catch and report errors if numbers cannot be parsed
            System.err.println("Error parsing number: " + e.getMessage());
        }
    }
}

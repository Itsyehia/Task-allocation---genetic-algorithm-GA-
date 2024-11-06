import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class GeneticAlgorithm {

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
        List<List<Integer>> selectedPopulation = new ArrayList<>();
        double[] probabilities = new double[fitnessValues.size()];
        double[] cumulativeProbabilities = new double[fitnessValues.size()];

        double totalFitness = 0;
        for (int fitness : fitnessValues) {
            totalFitness += (fitness == Integer.MAX_VALUE) ? 0 : 1.0 / fitness;
        }

        for (int i = 0; i < fitnessValues.size(); i++) {
            probabilities[i] = (fitnessValues.get(i) == Integer.MAX_VALUE ? 0 : 1.0 / fitnessValues.get(i)) / totalFitness;
        }

        cumulativeProbabilities[0] = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            cumulativeProbabilities[i] = cumulativeProbabilities[i - 1] + probabilities[i];
        }

        for (int i = 0; i < population.size(); i++) {
            double rand = random.nextDouble();
            for (int j = 0; j < cumulativeProbabilities.length; j++) {
                if (rand <= cumulativeProbabilities[j]) {
                    selectedPopulation.add(new ArrayList<>(population.get(j)));
                    break;
                }
            }
        }

        return selectedPopulation;
    }

    public static List<List<Integer>> crossover(List<List<Integer>> selectedPopulation, int crossoverPoint) {
        List<List<Integer>> newPopulation = new ArrayList<>();

        for (int i = 0; i < selectedPopulation.size(); i += 2) {
            List<Integer> parent1 = selectedPopulation.get(i);
            List<Integer> parent2 = selectedPopulation.get((i + 1) % selectedPopulation.size()); // Wrap around for odd sizes

            List<Integer> child1 = new ArrayList<>(parent1.subList(0, crossoverPoint));
            List<Integer> child2 = new ArrayList<>(parent2.subList(0, crossoverPoint));

            child1.addAll(parent2.subList(crossoverPoint, parent2.size()));
            child2.addAll(parent1.subList(crossoverPoint, parent1.size()));

            newPopulation.add(child1);
            newPopulation.add(child2);
        }

        return newPopulation;
    }

    public static List<Integer> mutate(List<Integer> chromosome, double mutationRate) {
        for (int i = 0; i < chromosome.size(); i++) {
            if (random.nextDouble() < mutationRate) {
                chromosome.set(i, chromosome.get(i) == 0 ? 1 : 0); // Flip bit
            }
        }
        return chromosome;
    }

    public static List<List<Integer>> elitismReplacement(List<List<Integer>> population, List<Integer> fitnessValues, int elitismCount) {
        List<List<Integer>> newPopulation = new ArrayList<>(population);

        for (int i = 0; i < elitismCount; i++) {
            int bestIndex = fitnessValues.indexOf(Collections.min(fitnessValues));
            newPopulation.set(i, population.get(bestIndex));
            fitnessValues.set(bestIndex, Integer.MAX_VALUE); // Temporarily mark as 'used'
        }

        return newPopulation;
    }

    public static void evolutionaryLoop(int testCaseIndex, List<List<Integer>> population, List<Integer> taskTimes, int maxTimeLimit, double mutationRate, int elitismCount) {
        boolean optimalFound = false;
        List<Integer> bestChromosome = null;
        int bestFitness = Integer.MAX_VALUE;
        int generation = 0;
        int maxGenerations = 1000; // Define a reasonable limit for generations

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

            List<List<Integer>> selectedPopulation = rouletteWheelSelection(population, fitnessValues);
            List<List<Integer>> newPopulation = crossover(selectedPopulation, taskTimes.size() / 2);

            for (int i = 0; i < newPopulation.size(); i++) {
                newPopulation.set(i, mutate(newPopulation.get(i), mutationRate));
            }

            population = elitismReplacement(newPopulation, fitnessValues, elitismCount);
            generation++;
        }

        // Output the best solution details
        System.out.println("Test Case #" + testCaseIndex);
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
        System.out.println();
    }

    public static void main(String[] args) {
        String filePath = "input.txt"; // Change to your file path
        readAndProcessFile(filePath);
    }

    public static void readAndProcessFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            if (line == null) {
                System.out.println("File is empty.");
                return;
            }

            int testCases = Integer.parseInt(line.trim());
            if (testCases < 1) {
                System.out.println("Number of test cases must be at least 1.");
                return;
            }

            for (int i = 0; i < testCases; i++) {
                line = br.readLine();
                if (line == null) {
                    System.out.println("Unexpected end of file.");
                    return;
                }
                int maxTimeLimit = Integer.parseInt(line.trim());

                line = br.readLine();
                if (line == null) {
                    System.out.println("Unexpected end of file.");
                    return;
                }
                int numberOfTasks = Integer.parseInt(line.trim());

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

                int populationSize = 50;
                double mutationRate = 0.05;
                int elitismCount = 2;
                int optimalFitness = maxTimeLimit; // Define an optimal value as per problem requirements

                List<List<Integer>> population = initializePopulation(populationSize, numberOfTasks);
                evolutionaryLoop(i + 1, population, taskTimes, maxTimeLimit, mutationRate, elitismCount);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number: " + e.getMessage());
        }
    }
}
# Task Allocation Genetic Algorithm

## Overview

This project implements a **Genetic Algorithm (GA)** to solve the **Task Allocation Problem**. The goal is to efficiently allocate tasks between two processing cores, aiming to minimize the total execution time while ensuring that neither core exceeds a specified time limit. This genetic algorithm uses a binary, one-dimensional chromosome representation and incorporates selection, crossover, and mutation to evolve optimal solutions over generations.

## Problem Description

Given a list of tasks with specific execution times, the objective is to assign each task to one of two cores:
- **Core 1** and **Core 2** have their own task execution limits.
- The goal is to minimize the execution time of the core with the larger load while ensuring that neither core exceeds its maximum time limit.

### Input File Format

The input file specifies multiple test cases in the following format:

1. **First Line**: Number of test cases (minimum 1).
2. For each test case:
   - **Max time limit** for each core.
   - **Number of tasks** to allocate.
   - **Execution time for each task**.

#### Example Input

```plaintext
3
100
5
10
20
30
40
50
150
4
60
70
80
90
200
3
20
30
40
```

### Expected Output

For each test case, the program outputs:
- Test case index.
- The best solution found, including its evaluation score and chromosome representation.
- A breakdown of tasks assigned to each core with their respective total times.

## Algorithm Implementation

The genetic algorithm follows these specific configurations and steps:

1. **Chromosome Representation**: 
   - Each chromosome is a binary, one-dimensional array where:
     - `1` represents a task assigned to Core 1.
     - `0` represents a task assigned to Core 2.

2. **Population Size**:
   - Can be set to different values (e.g., 50, 100, 250) to observe effects on performance.

3. **Fitness Evaluation**:
   - The fitness function calculates the total time for each core.
   - Feasible solutions meet the max time limit for each core.
   - The chromosome is evaluated based on minimizing the higher core time between Core 1 and Core 2.

4. **Handling Infeasible Solutions**:
   - Infeasible solutions (those that exceed time constraints) are penalized or discarded to ensure valid allocations.

5. **Selection**:
   - **Roulette Wheel Selection** is used to choose parents for crossover, favoring chromosomes with higher fitness scores.

6. **Crossover**:
   - **One-Point Crossover** creates offspring by combining genes from two parent chromosomes at a random crossover point.

7. **Mutation**:
   - **Flip Bit Mutation** randomly inverts a gene in the chromosome to introduce variability.

8. **Elitism**:
   - Ensures that the best-performing chromosome is retained in the next generation.

## Running the Program

To execute the algorithm, provide an input file formatted as described above. The program outputs:
- The best solution for each test case.
- Task allocations for Core 1 and Core 2.
- Chromosome representation and fitness score.

### Example Output

```plaintext
Test Case 1:
Best Solution: Evaluation Score = 90
Chromosome: 1 0 1 0 1
Tasks for Core 1: [10, 30, 50] - Total Time: 90
Tasks for Core 2: [20, 40] - Total Time: 60
```

## Requirements

The program is implemented in java and requires no special libraries.

## Key Remarks

- This project leverages genetic algorithms to balance task allocation between two cores, exploring various evolutionary strategies to optimize performance.
- Experimenting with different population sizes and mutation rates is encouraged to observe their effects on the solution quality.
  
## Conclusion

This genetic algorithm effectively demonstrates how evolutionary techniques can address complex optimization problems like task allocation, ensuring efficient use of processing resources while meeting constraints.

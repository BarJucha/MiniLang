import gp.*;

import java.util.Random;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        double crossProb = 0.8;
        int tournamentPlayers = 100;
        int populationSize = 1000;
        Population population = new Population(populationSize);
        int maxDepth = 7;
        int maxGen = 100;

        population.generateRandomPrograms(maxDepth, 3, -1, 1, "E:\\Java\\Programy Java\\MiniLangv2\\src\\input_data.txt");

        evolve(maxGen, population, populationSize, crossProb, tournamentPlayers, maxDepth);

        Program bestFirstProgram = population.getBestProg();

        Population population1 = new Population(populationSize);

        population1.generateRandomProgramsFromProgram(bestFirstProgram);
    }
    public static void evolve(int maxGen, Population population, int populationSize, double crossProb, int tournamentPlayers, int maxDepth) {
        Random random = new Random();
        try (FileWriter fwBest = new FileWriter("results.csv", true);
             BufferedWriter bwBest = new BufferedWriter(fwBest);
             PrintWriter outBest = new PrintWriter(bwBest);

             FileWriter fwAvg = new FileWriter("avgResults.csv", true);
             BufferedWriter bwAvg = new BufferedWriter(fwAvg);
             PrintWriter outAvg = new PrintWriter(bwAvg)) {

            File fileBest = new File("results.csv");
            File fileAvg = new File("avgResults.csv");

            if (fileBest.length() == 0) {
                outBest.println("Generation,Best Fitness");
            }
            if (fileAvg.length() == 0) {
                outAvg.println("Generation,Average Fitness");
            }

            for (int i = 0; i < maxGen; i++) {
                population.calculateBest();
                double bestFit = population.getBestFit();
                double avgFit = population.getAvgFit();
                System.out.println("Generation: " + i + " Best fitness: " + bestFit + " Avg fitness: " + avgFit);
                System.out.println("Best program: " + population.getBestProg().tree.toString());

                outBest.println(i + "," + bestFit);

                outAvg.println(i + "," + avgFit);

                if (bestFit < 1) {
                    System.out.println("PROBLEM SOLVED!");
                    break;
                }

                for (int j = 0; j < populationSize; j++) {
                    if (random.nextDouble() < crossProb) {
                        population.crossover(tournamentPlayers);
                    } else {
                        population.mutate(tournamentPlayers, maxDepth + (i / 12));
                        //population.mutateTheBest(tournamentPlayers, maxDepth + (i/12));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

}
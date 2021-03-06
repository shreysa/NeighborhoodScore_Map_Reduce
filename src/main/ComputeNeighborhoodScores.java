/**
 * PDPMR 6240 Fall 2017
 * Assignment A0A1
 * Author : Shreysa Sharma
 */

package src.main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Computes the letter scores, neighborhood scores, accumulates these scores,
 * kNeighborhood scores and kneighborhoodMean scores and sends it to an output csv
 * file
 */

public class ComputeNeighborhoodScores {
    public static final int NUM_EXPECTED_CHARS = 26;
    public static final int ASCII_START_INDEX_LETTERS = 97;

    private HashMap<String, List<List<String>>> kNeighborhoods;
    private HashMap<String, Float> wordKNeighborhoodMeanScore;
    private Integer[] characterOccurances;
    private long totalCharacters;
    String outputFileName;

    ComputeNeighborhoodScores(String outputFileName) {
        totalCharacters = 0;
        this.outputFileName = outputFileName;
        kNeighborhoods = new HashMap<String, List<List<String>>>();
        wordKNeighborhoodMeanScore = new HashMap<String, Float>();
        characterOccurances = new Integer[NUM_EXPECTED_CHARS];
        for (int i = 0; i < NUM_EXPECTED_CHARS; i++) {
            characterOccurances[i] = 0;
        }
    }

    /**
     * This method computes the letter scores
     *
     * @return Returns an integer array that has scores for each alphabet
     */
    public Integer[] computeLetterScores() {
        Integer[] letterScore = new Integer[NUM_EXPECTED_CHARS];
        for (int i = 0; i < NUM_EXPECTED_CHARS; i++) {
            Float percentageOccurrence = (float)characterOccurances[i]/(float)totalCharacters * 100.0F;

            int scoreForLetter = 0;
            if (percentageOccurrence > 10.0) {
                scoreForLetter = 0;
            } else if (percentageOccurrence >= 8.0 && percentageOccurrence < 10.0) {
                scoreForLetter = 1;
            } else if (percentageOccurrence >= 6.0 && percentageOccurrence < 8.0) {
                scoreForLetter = 2;
            } else if (percentageOccurrence >= 4.0 && percentageOccurrence < 6.0) {
                scoreForLetter = 4;
            } else if (percentageOccurrence >= 2.0 && percentageOccurrence < 4.0) {
                scoreForLetter = 8;
            } else if (percentageOccurrence >= 1.0 && percentageOccurrence < 2.0) {
                scoreForLetter = 16;
            } else if (percentageOccurrence < 1.0) {
                scoreForLetter = 32;
            }

            letterScore[i] = scoreForLetter;
        }

        return letterScore;
    }

    /**
     * This method produces an output csv file that has the kNeighborhoodMean
     * scores
     */

    public void writeSortedFinalWordScores() throws IOException {
        TreeMap<String, Float> sorted = new TreeMap<String, Float>(wordKNeighborhoodMeanScore);
        Set<Map.Entry<String, Float>> mappings = sorted.entrySet();

        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));

        for(Map.Entry<String, Float> mapping : mappings){
            bw.write(mapping.getKey() + "," + mapping.getValue() + "\n");
        }

        bw.close();
    }

    /**
     * This method accumulates the letter and kneighborhood scores from various files
     *
     * @param charOccurances array and neighborhoods hashmap
     */

    void AddResults(Integer[] charOccurances, Map<String, List<List<String>>> neighborhoods ) {

        // Add letter occurances
        for (int i = 0; i < NUM_EXPECTED_CHARS; i++) {
            characterOccurances[i] += charOccurances[i];
            totalCharacters += charOccurances[i];
        }

        // Add neighborhoods
        for (Map.Entry<String, List<List<String>>> entry : neighborhoods.entrySet()) {
            String word = entry.getKey();
            List<List<String>> value = entry.getValue();

            if (kNeighborhoods.containsKey(word)) {
                List<List<String>> neighbors = kNeighborhoods.get(word);
                neighbors.addAll(value);
            } else {
                List<List<String>> neighbors = new ArrayList<List<String>>(value);
                kNeighborhoods.put(word, neighbors);
            }
        }
    }

    /**
     * This method computes KNeighborhoodMean score
     *
     * @param letterScore array
     */

    public void computeKNeighborMeans(Integer[] letterScore) {
        HashMap<String, Integer> wordScoreCache = new HashMap<String, Integer>();


        float kNeighborhoodMean = 0.0F;
        for (Map.Entry<String, List<List<String>>> entry : kNeighborhoods.entrySet()) {
            String word = entry.getKey();
            List<List<String>> value = entry.getValue();

            // Consider one neighborhood for word
            int entryNumber = 0;
            for (List<String> kNeighbors : value) {
                // compute neighborhood score
                int neighborhoodScore = 0;
                for (int i = 0; i < kNeighbors.size(); i++) {
                    String neighborWord = kNeighbors.get(i);

                    if (wordScoreCache.containsKey(neighborWord)) {
                        int score = wordScoreCache.get(neighborWord);
                        neighborhoodScore += score;
                    } else {
                        // compute score and cache it
                        int wordScore = 0;
                        for (char c: neighborWord.toCharArray()) {
                            wordScore += letterScore[(int)c - ASCII_START_INDEX_LETTERS];
                        }
                        neighborhoodScore += wordScore;
                        wordScoreCache.put(neighborWord, wordScore);
                    }
                }

                if (entryNumber == 0) {
                    kNeighborhoodMean = (float)neighborhoodScore;
                } else {
                    kNeighborhoodMean = (kNeighborhoodMean + neighborhoodScore) / 2.0F;
                }
                ++entryNumber;
            }

            wordKNeighborhoodMeanScore.put(word, kNeighborhoodMean);
        }
    }

    /**
     * This method calls all the other methods in this class in order to calculate the
     * various scores
     *
     */
    public void calculateKNeighbourhoodScores() throws IOException {
        Integer[] letterScore = computeLetterScores();
        computeKNeighborMeans(letterScore);
        writeSortedFinalWordScores();
    }


    /* Debug methods
     *
     */

    public void printLetterStats(Integer[] letterScore) {
        System.out.println("Total number of characters: " + totalCharacters);
        String header = "";
        String occurances = "";
        String percentage = "";
        String score = "";
        Float percentageTotal = 0.0F;
        for (int i = 0; i < NUM_EXPECTED_CHARS; i++) {
            header += "\t" + (char) (i + ASCII_START_INDEX_LETTERS);
            occurances += "\t" + characterOccurances[i];
            Float percentageOccurance = (float)characterOccurances[i]/(float)totalCharacters * 100.0F;
            Integer percentageOccuranceRounded = Math.round(percentageOccurance);
            percentage += "\t" + percentageOccuranceRounded;
            percentageTotal += percentageOccurance;
            score += "\t" + letterScore[i];
        }

         System.out.println("Percentage Total: " + percentageTotal);
         System.out.println(header);
         System.out.println(occurances);
         System.out.println(percentage);
         System.out.println(score);
    }

    public void printKNeighbors() {
        for (Map.Entry<String, List<List<String>>> entry : kNeighborhoods.entrySet()) {
            String word = entry.getKey();
            List<List<String>> value = entry.getValue();

            String neighborString = "";
            for (List<String> kNeighbors: value) {
                neighborString += "[";
                for (int i = 0; i < kNeighbors.size(); i++) {
                    neighborString += kNeighbors.get(i);
                    if (i < (kNeighbors.size() - 1))
                        neighborString += ", ";
                }
                neighborString += "], ";
            }

            System.out.println(word + " ↦ " + neighborString);
        }
    }

    public void printSortedFinalWordScores() {
        TreeMap<String, Float> sorted = new TreeMap<String, Float>(wordKNeighborhoodMeanScore);
        Set<Map.Entry<String, Float>> mappings = sorted.entrySet();

        for(Map.Entry<String, Float> mapping : mappings){
            System.out.println(mapping.getKey() + ": " + mapping.getValue());
        }
    }

}

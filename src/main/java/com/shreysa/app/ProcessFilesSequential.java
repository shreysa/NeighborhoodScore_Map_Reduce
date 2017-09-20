package com.shreysa.app;

import java.io.*;
import java.util.*;

public class ProcessFilesSequential {


    final int NUM_EXPECTED_CHARS;
    final int ASCII_START_INDEX_LETTERS;
    private Integer[] characterOccurances;

    ProgramArgs args;
    long totalCharacters;
    HashMap<String, List<List<String>>> kNeighborhoods;
    HashMap<String, Float> wordKNeighborhoodMeanScore;


    ProcessFilesSequential(ProgramArgs args) {
        this.args = args;
        kNeighborhoods = new HashMap<String, List<List<String>>>();
        wordKNeighborhoodMeanScore = new HashMap<String, Float>();
        NUM_EXPECTED_CHARS = 26;
        ASCII_START_INDEX_LETTERS = 97;
        characterOccurances = new Integer[NUM_EXPECTED_CHARS];
        for (int i = 0; i < NUM_EXPECTED_CHARS; i++) {
            characterOccurances[i] = 0;
        }
    }

    public void processFiles(File[] files) throws IOException {

        long startTimeFiles = System.currentTimeMillis();
        for (File f: files) {
            long startTimeFile = System.currentTimeMillis();

            if (!f.exists() || !f.canRead()) {
                System.err.format("Unable to read file %s. Check path and permissions.\n", f.getName());
            }

            BufferedReader br = new BufferedReader(new FileReader(f));

            List<String> fileWords = new ArrayList<String>();
            for (int k = 0; k < args.getkValue(); k++) {
                fileWords.add(null);
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] words = line.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
                for (String word : words) {
                    fileWords.add(word);
                }
            }

            br.close();

            for (int k = 0; k < args.getkValue(); k++) {
                fileWords.add(null);
            }

            processWords(fileWords);

            long endTimeFile = System.currentTimeMillis();

            // System.out.println("Processed file: " + f.getName() + " in " + (endTimeFile - startTimeFile) + "ms");
        }

        long endTimeFiles = System.currentTimeMillis();

        System.out.println("Processed all files in " + (endTimeFiles - startTimeFiles) + "ms");

        long startTimeScores = System.currentTimeMillis();
        Integer[] letterScore = computeLetterScores();

        computeKNeighborMeans(letterScore);
        long endTimeScores = System.currentTimeMillis();

        System.out.println("Computed word and neighbor scores in " + (endTimeScores - startTimeScores) + "ms");

        writeSortedFinalWordScores();
        printLetterStats(letterScore);
    }

    public void processWords(List<String> words) {
        int kval = args.getkValue();

        for (int i = kval; i < words.size() - kval; i++) {
            String word = words.get(i);
            List<String> kNeighbors = new ArrayList<String>();

            // Collect K-Neighbors
            for (int k = 0; k < args.getkValue(); k++) {
                int beforeIndex = i - (k + 1);
                int afterIndex = i + (k + 1);
                String beforeWord = words.get(beforeIndex);
                String afterWord = words.get(afterIndex);
                if (beforeWord != null) {
                    kNeighbors.add(beforeWord);
                }
                if (afterWord != null) {
                    kNeighbors.add(afterWord);
                }
            }

            // Add to hash map
            if (kNeighborhoods.containsKey(word)) {
                List<List<String>> value = kNeighborhoods.get(word);
                value.add(kNeighbors);
            } else {
                List<List<String>> value = new ArrayList<List<String>>();
                value.add(kNeighbors);
                kNeighborhoods.put(word, value);
            }


            // Collect character stats from word
            totalCharacters += word.length();
            for (char c : word.toCharArray()) {
                int asciiValue = (int) c;
                int index = (asciiValue - ASCII_START_INDEX_LETTERS);
              //  System.out.println("char: " + c + " asciiValue: " + c + " index: " + index);
                characterOccurances[index] += 1;
            }
        }
        words.clear();
    }

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

    // Sorting code derived from comments in:
    //   https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
    public void writeSortedFinalWordScores() throws IOException {
        TreeMap<String, Float> sorted = new TreeMap<String, Float>(wordKNeighborhoodMeanScore);
        Set<Map.Entry<String, Float>> mappings = sorted.entrySet();

        BufferedWriter bw = new BufferedWriter(new FileWriter("output.csv"));

        System.out.println("Saving neighborhood scores after sorting by keys to output.csv");
        for(Map.Entry<String, Float> mapping : mappings){
            bw.write(mapping.getKey() + "," + mapping.getValue() + "\n");
        }

        bw.close();
    }

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

}
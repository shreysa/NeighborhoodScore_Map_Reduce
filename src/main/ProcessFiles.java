/**
 * PDPMR 6240 Fall 2017
 * Assignment A0A1
 * Author : Shreysa Sharma
 */

package src.main;

import java.io.*;
import java.util.*;

/**
 * This class add processes all the files, gets character stats and
 * collects kNeighbors of the words in the files
 *
 */

public class ProcessFiles {
    static final int NUM_EXPECTED_CHARS = 26;
    static final int ASCII_START_INDEX_LETTERS = 97;
    public boolean isThreaded;
    private Integer[] characterOccurances;

    File[] files;
    int kval;
    long totalCharacters;
    HashMap<String, List<List<String>>> kNeighborhoods;
    HashMap<String, Float> wordKNeighborhoodMeanScore;

    ProcessFiles(File[] files, int kval) {
        this.files = files;
        this.kval = kval;
        kNeighborhoods = new HashMap<String, List<List<String>>>();
        wordKNeighborhoodMeanScore = new HashMap<String, Float>();
        characterOccurances = new Integer[NUM_EXPECTED_CHARS];
        for (int i = 0; i < NUM_EXPECTED_CHARS; i++) {
            characterOccurances[i] = 0;
        }
    }


    /**
     * This method gets character stats and collects kNeighbors from the words list
     *
     * @param Words List
     */
    public void processWords(List<String> words) {

        for (int i = this.kval; i < words.size() - this.kval; i++) {
            String word = words.get(i);
            List<String> kNeighbors = new ArrayList<String>();

            // Collect K-Neighbors
            for (int k = 0; k < this.kval; k++) {
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
                characterOccurances[index] += 1;
            }
        }

        words.clear();
    }

    /**
     * This method provides the characterOccurances array
     *
     * @return Returns an integer array that has scores for each alphabet
     */
   public Integer[] getCharacterOccurances() {
       return characterOccurances;
   }

    /**
     * This method provides the kNeighborhood for all the words in the files
     *
     * @return Returns a hashmap of kNeighborhoods of words
     */
    public HashMap<String, List<List<String>>> getkNeighborhoods() {
        return kNeighborhoods;
    }

    /**
     * This method inserts k nulls in the beginning and end of each file,
     * removes all punctuations and converts all the letters to lowercase
     * and then calls the processWords method
     *
     */

    public void processFiles() throws IOException {
        for (File f: files) {

            if (!f.exists() || !f.canRead()) {
                System.err.format("Unable to read file %s. Check path and permissions.\n", f.getName());
            }

            BufferedReader br = new BufferedReader(new FileReader(f));

            List<String> fileWords = new ArrayList<String>();
            for (int k = 0; k < this.kval; k++) {
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

            for (int k = 0; k < this.kval; k++) {
                fileWords.add(null);
            }

            processWords(fileWords);
        }
    }
}

package com.shreysa.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Application to process all files in the provided folder
 * compute the letter score, word score, k-neighborhood score
 * for each work and the mean of the k-neighborhood scores.
 */
public class App 
{

    /**
     * This method reads a file and returns a list of the contents
     * of each line.
     * @param fileName Name of the file to be processed
     * @return Returns List of Strings, each entry corresponds to a line in the file
     */
    public static List<String> readFile(String fileName) {
        File file = new File(fileName);

        if (!file.exists() || !file.canRead()) {
            System.err.println("ERROR: Unable to read file. Check file path and permissions.");
        }

        List<String> fileLines = new ArrayList<String>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            String line = null;
            while ((line = br.readLine()) != null) {
                fileLines.add(line);
            }
        } catch (Exception ex) {
            System.err.println("ERROR: Error wile reading file - " + ex.getMessage());
        }

        return fileLines;
    }

    public static void main( String[] args )
    {
        final String TEST_FILE = "./data/lorem.txt";
        List<String> fileContents = readFile(TEST_FILE);

        for (String line: fileContents) {
            System.out.println(line);
        }
    }
}

/** PDPMR 5240 Fall 2017 
Assignment A0A1 
Author : Shreysa Sharma 
*/

package com.shreysa.app;

import java.io.*;
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

    /**
     * This method gets all the files from the folder path with extension .txt
     * @param folderPath folder path from which files need to be processed
     * @return Returns Array of files, each entry corresponds to a file
     */
    public static File[] getFilesInDirectory(String folderPath) {
        File dir = new File(folderPath);
        File [] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt");
            }
        });
        return files;
    }
    
    public static void main( String[] args ) throws IOException
    {
         ProgramArgs options = new ProgramArgs(args);

        File[] files = getFilesInDirectory("./data/books");

        if (options.getNumThreads() == 1) {
            ProcessFilesSequential pr = new ProcessFilesSequential(options);
            pr.processFiles(files);
        } else {
        }
    }
}

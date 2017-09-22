/**
 * PDPMR 6240 Fall 2017
 * Assignment A0A1
 * Author : Shreysa Sharma
 */

package src.main;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Application to process all files in the provided folder
 * compute the letter score, word score, k-neighborhood score
 * for each work and the mean of the k-neighborhood scores.
 */
public class App {

    /**
     * This method reads a file and returns a list of the contents
     * of each line.
     *
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
     * This method gets all the files from the folder path with extension .txt.utf-8
     *
     * @param folderPath folder path from which files need to be processed
     * @return Returns Array of files, each entry corresponds to a file
     */
    public static File[] getFilesInDirectory(String folderPath) {
        File dir = new File(folderPath);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt.utf-8");
            }
        });
        return files;
    }

    /**
     * Entry point of program, runs sequential or parallel program with or without load balance
     * depending on the arguments passed.
     *
     * @param String [] args having path, k value and number of threads
     * @return void
     */

    public static void main(String[] args) throws IOException {
        final boolean NO_LOAD_BALANCE = true;
        ProgramArgs options = new ProgramArgs(args);
        File[] files = getFilesInDirectory(options.getFilePath());
        long startTimeFiles;
        List<ProcessFilesTask> tasks = new ArrayList<ProcessFilesTask>();
        //Sequential version
        if (options.getNumThreads() == 1) {
            startTimeFiles = System.currentTimeMillis();
            ProcessFiles pr = new ProcessFiles(files, options.getkValue());
            pr.processFiles();
            long endTimeFiles = System.currentTimeMillis();
            System.out.println("Processed all files in " + (endTimeFiles - startTimeFiles) + " ms");

            ComputeNeighborhoodScores scores = new ComputeNeighborhoodScores("output.csv");

            scores.AddResults(pr.getCharacterOccurances(), pr.getkNeighborhoods());
            long endTimeFilesAccumulate = System.currentTimeMillis();
            System.out.println("Accumulated results from files in " + (endTimeFilesAccumulate - endTimeFiles) + " ms");

            scores.calculateKNeighbourhoodScores();
            long endTimeFilesCompute = System.currentTimeMillis();
            System.out.println("Computed all files in " + (endTimeFilesCompute - endTimeFilesAccumulate) + " ms");


        } else {
            //Without any load balance
            if (NO_LOAD_BALANCE) {
                ExecutorService executor = Executors.newFixedThreadPool(options.getNumThreads());
                int segment = (files.length) / options.getNumThreads();
                startTimeFiles = System.currentTimeMillis();

                //Assigns files to threads
                for (int i = 0; i < options.getNumThreads(); i++) {
                    int startIndex = i * segment;
                    int endIndex = (i + 1) * segment;
                    if (endIndex > files.length) {
                        endIndex = files.length;
                    } else if ((i == (options.getNumThreads() - 1)) &&
                            (endIndex < files.length)) {
                        endIndex = files.length;
                    }

                    File[] filesForThread = Arrays.copyOfRange(files, startIndex, endIndex);

                    ProcessFilesTask worker = new ProcessFilesTask(filesForThread, options.getkValue());
                    executor.execute(worker);
                    tasks.add(worker);
                }

                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException ex) {
                    System.err.println("ERROR: " + ex.getMessage());
                }
                } else {
                // With load balance : takes the size of files and then distributes the work
                // amongst threads
                long totalLoad = 0;
                for (File f : files) {
                    totalLoad += f.length();
                }

                ExecutorService executor = Executors.newFixedThreadPool(options.getNumThreads());

                long workPerThread = (totalLoad) / options.getNumThreads();
                startTimeFiles = System.currentTimeMillis();
                int currentFile = 0;
                for (int i = 0; i < options.getNumThreads(); i++) {
                    int startIndex = currentFile;
                    long currentWorkForThread = 0;
                    for (; currentFile < files.length; currentFile++) {
                        currentWorkForThread += files[currentFile].length();
                        if (currentWorkForThread >= workPerThread) {
                            break;
                        }
                    }
                    int endIndex = currentFile;
                    if (endIndex > files.length || i == options.getNumThreads() - 1) {
                        endIndex = files.length;
                    }

                    File[] filesForThread = Arrays.copyOfRange(files, startIndex, endIndex);
                    ProcessFilesTask worker = new ProcessFilesTask(filesForThread, options.getkValue());
                    executor.execute(worker);
                    tasks.add(worker);
                }
                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException ex) {
                    System.err.println("ERROR: " + ex.getMessage());
                }

                }
            long endTimeFiles = System.currentTimeMillis();
            System.out.println("Processed all files in " + (endTimeFiles - startTimeFiles) + " ms");

            ComputeNeighborhoodScores scores = new ComputeNeighborhoodScores("output_threaded.csv");
            for (ProcessFilesTask task : tasks) {
                scores.AddResults(task.processor.getCharacterOccurances(), task.processor.getkNeighborhoods());
            }

            long endTimeFilesAccumulate = System.currentTimeMillis();
            System.out.println("Accumulated results from files in " + (endTimeFilesAccumulate - endTimeFiles) + " ms");

            scores.calculateKNeighbourhoodScores();
            long endTimeFilesCompute = System.currentTimeMillis();
            System.out.println("Computed all files in " + (endTimeFilesCompute - endTimeFilesAccumulate) + " ms");
     }
    }
}

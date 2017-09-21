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
     * This method gets all the files from the folder path with extension .txt
     *
     * @param folderPath folder path from which files need to be processed
     * @return Returns Array of files, each entry corresponds to a file
     */
    public static File[] getFilesInDirectory(String folderPath) {
        File dir = new File(folderPath);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt");
            }
        });
        return files;
    }

    public static void main(String[] args) throws IOException {
        final boolean NO_LOAD_BALANCE = true;
        ProgramArgs options = new ProgramArgs(args);

        File[] files = getFilesInDirectory("./data/books");

        if (options.getNumThreads() == 1) {
            ProcessFilesSequential pr = new ProcessFilesSequential(options);
            pr.processFiles(files);
        } else {
            if (NO_LOAD_BALANCE) {
                System.out.println("Processing data set on " + options.getNumThreads() + " threads.");
                ExecutorService executor = Executors.newFixedThreadPool(options.getNumThreads());
                List<ProcessFilesTask> tasks = new ArrayList<ProcessFilesTask>();
                int segment = (files.length) / options.getNumThreads();
                System.out.println("files length" + files.length);
                System.out.println("Number of files per thread " + segment);
                long startTimeFiles = System.currentTimeMillis();
                for (int i = 0; i < options.getNumThreads(); i++) {
                    int startIndex = i * segment;
                    int endIndex = (i + 1) * segment;
                    if (endIndex > files.length) {
                        endIndex = files.length;
                    } else if ((i == (options.getNumThreads() - 1)) &&
                            (endIndex < files.length)) {
                        endIndex = files.length;
                    }
                    System.out.println("Thread " + i + " start: " + startIndex + " endIndex: " + endIndex);

                    File[] filesForThread = Arrays.copyOfRange(files, startIndex, endIndex);
                    System.out.println("number of file to  be processed by thread" + i + " is " + filesForThread.length);
                    for (File file : filesForThread) {
                        System.out.println(file.getName() + " thread " + i);
                    }
                    ProcessFilesTask worker = new ProcessFilesTask(filesForThread, options.getkValue());
                    executor.execute(worker);
                    tasks.add(worker);
                }
                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                }

                long endTimeFiles = System.currentTimeMillis();
                System.out.println("Processed all files in " + (endTimeFiles - startTimeFiles) + "ms");

                ComputeNeighborhoodScores scores = new ComputeNeighborhoodScores();
                for (ProcessFilesTask task : tasks) {
                    scores.AddResults(task.processor.getCharacterOccurances(), task.processor.getkNeighborhoods());
                }
                scores.calculateKNeighbourhoodScores();
            } else {
                // With load balance
                long totalLoad = 0;
                for (File f : files) {
                    totalLoad += f.length();
                }

                System.out.println("Total \'load\' in files: " + totalLoad);

                System.out.println("Processing data set on " + options.getNumThreads() + " threads.");
                ExecutorService executor = Executors.newFixedThreadPool(options.getNumThreads());
                List<ProcessFilesTask> tasks = new ArrayList<ProcessFilesTask>();

                long workPerThread = (totalLoad) / options.getNumThreads();
                System.out.println("Work per thread " + workPerThread);
                long startTimeFiles = System.currentTimeMillis();
                int currentFile = 0;
                for (int i = 0; i < options.getNumThreads(); i++) {
                    int startIndex = currentFile + 1;
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
                    System.out.println("Thread " + i + " start: " + startIndex + " endIndex: " + endIndex + " work: " + currentWorkForThread);
                    File[] filesForThread = Arrays.copyOfRange(files, startIndex, endIndex);
                    ProcessFilesTask worker = new ProcessFilesTask(filesForThread, options.getkValue());
                    executor.execute(worker);
                    tasks.add(worker);
                }
                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                }

                long endTimeFiles = System.currentTimeMillis();
                System.out.println("Processed all files in " + (endTimeFiles - startTimeFiles) + "ms");

                ComputeNeighborhoodScores scores = new ComputeNeighborhoodScores();
                for (ProcessFilesTask task : tasks) {
                    scores.AddResults(task.processor.getCharacterOccurances(), task.processor.getkNeighborhoods());
                }
                scores.calculateKNeighbourhoodScores();
            }
        }
    }
}

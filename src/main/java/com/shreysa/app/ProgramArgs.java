/** PDPMR 5240 Fall 2017 
Assignment A0A1 
Author : Shreysa Sharma 
*/

package com.shreysa.app;


public class ProgramArgs {
    final String fileName;
    final int kValue;
    final int numThreads;


/**
     * The Constructor checks if the command line arguments are valid, 
     if yes then assigns the arguments passed to the class variables.
     * @param args The path of the directory from which files have to be picked, k value
                    and number of threads
     */
    ProgramArgs(String[] args) {
        String errorMessage;
        errorMessage = "Invalid arguments.\n" +
                "Usage:\n\t[Application] --path <file-path-and-name> --k <k-neighbourhood-value> --threads <num-threads>";
        if (args.length != 6) {
            System.err.println(errorMessage);
            System.exit(-1);
        }

        String fileName = "";
        int kval = 0;
        int nT = 1;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--path")) {
                fileName = args[i + 1];
                i += 1;
            } else if (args[i].equals("--k")) {
                kval = Integer.parseInt(args[i + 1]);
                i += 1;
            } else if (args[i].equals("--threads")) {
                nT = Integer.parseInt(args[i + 1]);
                i += 1;
            }
            else {
                System.err.println(errorMessage);
            }
        }

        this.fileName = fileName;
        this.kValue = kval;
        this.numThreads = nT;
    }


    /**
     * Getter for file name
     * @return returns the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Getter for k value
     * @return returns the kValue
     */
    public int getkValue() {
        return kValue;
    }

    /**
     * Getter for number of threadsvalue
     * @return returns the numThreads
     */
    public int getNumThreads() {
        return numThreads;
    }
}

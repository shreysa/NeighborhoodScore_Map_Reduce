/**
 * PDPMR 6240 Fall 2017
 * Assignment A0A1
 * Author : Shreysa Sharma
 */

package src.main;

import java.io.File;
import java.io.IOException;

// Reference from Professors notes posted on piazza
public class ProcessFilesTask implements  Runnable {
    ProcessFiles processor;

    ProcessFilesTask(File[] files, int kval) {
        this.processor = new ProcessFiles(files, kval);
    }

    @Override
    public void run() {
        try {
            this.processor.processFile();
        } catch (IOException ex) {
            System.err.println("IO exception: " + ex.getMessage());
            System.exit(-1);
        }
    }
}
#
# PDPMR 6240 Fall 2017
# Assignment A0A1 Makefile
# Author : Shreysa Sharma
#

JCC = javac
JVM = java

# Add debug symbols to the classes if DEBUG=1 during build
# eg : make build DEBUG=1
ifdef DEBUG
JFLAGS = -g
else
JFLAGS =
endif

# If no THREADS option is provided, use 4 threads by default
ifndef THREADS
THREADS = 4
endif

# If no KVALUE option is provided, use 4 as k value by default
ifndef KVALUE
KVALUE = 4
endif

# If no PATH option is provided, use ./data/books by default
ifndef PATH
PATH = ./input
endif

all: build serial parallel

default: build run

gzip: 
	gzip -q ./input/* 

gunzip:
	gunzip -q ./input/*

build:  App.class                    \
       	ProgramArgs.class            \
       	ProcessFilesTask.class       \
       	ProcessFiles.class           \
       	ComputeNeighborhoodScores.class

run: serial

serial: 
	$(JVM) -cp . src.main.App --path $(PATH) --k $(KVALUE) --threads 1

parallel: 
	$(JVM) -cp . src.main.App --path $(PATH) --k $(KVALUE) --threads $(THREADS)

App.class:  src/main/App.java
	$(JCC) $(JFLAGS) ./src/main/App.java

ProgramArgs.class: src/main/ProgramArgs.java
	$(JCC) $(JFLAGS) ./src/main/ProgramArgs.java

ProcessFilesTask.class: src/main/ProcessFilesTask.java
	$(JCC) $(JFLAGS) ./src/main/ProcessFilesTask.java

ProcessFiles.class: src/main/ProcessFiles.java
	$(JCC) $(JFLAGS) ./src/main/ProcessFiles.java

ComputeNeighborhoodScores.class: src/main/ComputeNeighborhoodScores.java
	$(JCC) $(JFLAGS) ./src/main/ComputeNeighborhoodScores.java

clean:
	rm ./src/main/*.class


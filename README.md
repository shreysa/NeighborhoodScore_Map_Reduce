## Assignment A0/A1 for CS6240
### Fall 2017
### Shreysa Sharma 

### Directory Structure 
`input` - Contains the input files used for the runs (taken from assignment task page).

`output` - Contains csv files with the averaged timings and speed up information. These files are used in the generation of reports.

`src` - Contains all the source files associated with the implementation. 

`Makefile` - Makefile to assist in building, running and managing the project.

`report.Rmd` - Report file in R-Markdown format.

`report.html` - HTML rendering of the above report.

### Instructions for building and running the program

1. Unzip the input files by running `make gunzip`.
2. Build the project by running `make build`.
3. Serial run: To run the sequential version of the program `eg. make serial PATH=./input KVALUE=4`.
4. Parallel run: To run the threaded version of the program `eg. make parallel PATH=./input KVALUE=4 THREADS=4`
5. The output of the serial run is stored in `output.csv` in the project folder. 
6. The output of the parallel run is stored in `output_threaded.csv` in the project folder.
7. `make clean` removes all the `*.class` files, useful for a clean build. 


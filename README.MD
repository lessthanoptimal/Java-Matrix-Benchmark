# Java Matrix Benchmark

Java Matrix Benchmark (JMatBench) is a tool used to evaluate the runtime performance, stability, and memory efficiency of 
linear algebra libraries at performing dense real operations.

*WARNING* The latest version is a major modification from the past and isn't fully functional yet

# Instructions

JMatBench is designed to be run as a command line application.  As much of the process has been automated as possible,
however a few libraries aren't on Maven central so you need to download their jars manually and place them in the 
libraries "libs/" directory.  Instructions for where to download the latest jar is typically provided in the
"libs/source.txt".  After those jars have been downloaded gradle will be able to compile the project.

1. Manual download jars for libaries which haven't been posted on Maven central
2. gradle createRuntimeJars
3. gradle benchmarkJar
4. java -jar benchmark.jar
5. Follow printed instructions for the benchmark you wish to run


# Adding Your Own Library

1. Create directory in external
2. copy "src" and "build.gradle" for another project into that directory
3. Modify the source code and create your own "TestSetInfo.txt" file using the application you modified.
4. Add the library to "external/DefaultTestSet.txt" if you wish
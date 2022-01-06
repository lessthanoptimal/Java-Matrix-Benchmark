# Java Matrix Benchmark

Java Matrix Benchmark (JMatBench) is a tool used to evaluate the runtime performance, stability, and memory efficiency of 
linear algebra libraries at performing dense real operations.

*WARNING* The latest version is a major modification from the past and isn't fully functional yet

# Instructions

JMatBench is designed to be run as a command line application.  As much of the process has been automated as possible,
however a few libraries aren't on Maven central so you need to download their jars manually and place them in the 
libraries "libs/" directory.  Instructions for where to download the latest jar is typically provided in the
"libs/source.txt".  After those jars have been downloaded gradle will be able to compile the project.

1. Install dependencies required by tested libraries
    ```bash
    sudo apt-get install libblas-dev liblapack-dev
    ``` 
2. Manual download jars for libraries which haven't been posted on Maven central
    * jeigen: See its readme.md
3. ./gradlew createRuntimeJars; ./gradlew benchmarkJar
4. java -jar benchmark.jar
5. Follow printed instructions for the benchmark you wish to run


# Settings Up Results E-Mailing

Create a file called "email_login.txt". DO NOT ADD TO GIT.
* First line is your login.
* Second line is your password. 
* Third line is the destination e-mail.

This is configured for gmail only right now. You need to turn on "Allow less secure apps" by going to
https://myaccount.google.com/security

# Adding Your Own Library

1. Create new directory in external
2. Copy "src" and "build.gradle" from another project in external into your directory
3. Modify the source code, build.gradle, and rename classes
4. Create your own "TestSetInfo.txt" file using <Your Project>CreateTestSetInfo.
5. Add the library to "external/DefaultTestSet.txt" if you wish it to be included by default
6. Add your library to settings.gradle so that Gradle will compile it


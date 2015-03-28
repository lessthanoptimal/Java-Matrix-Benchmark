/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
 *
 * This file is part of JMatrixBenchmark.
 *
 * JMatrixBenchmark is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * JMatrixBenchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JMatrixBenchmark.  If not, see <http://www.gnu.org/licenses/>.
 */

package jmbench.tools.memory;

import jmbench.impl.FactoryLibraryDescriptions;
import jmbench.impl.LibraryDescription;
import jmbench.tools.runtime.InputOutputGenerator;
import jmbench.tools.runtime.generator.*;
import jmbench.tools.stability.UtilXmlSerialization;
import jmbench.tools.version.PrintLibraryVersion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author Peter Abeles
 */
public class MemoryBenchmarkLibrary {

    Random rand;

    String libraryName;
    MemoryConfig config;

    MemoryBenchmarkTools tool = new MemoryBenchmarkTools();

    List<Task> activeTasks = new ArrayList<Task>();

    String directorySave;

    private PrintStream logStream = System.err;

    // how much overhead is involved in just launching a process
    // this is subtracted from the results in each library
    private long memoryOverhead;

    private LibraryDescription desc;

    public MemoryBenchmarkLibrary( MemoryConfig config ,
                                   LibraryDescription desc,
                                   String directorySave ,
                                   int matrixSize ,
                                   long memoryOverhead )
    {
        this.rand = new Random(config.seed);
        this.config = config;
        this.directorySave = directorySave;
        this.libraryName = desc.location.getPlotName();
        this.memoryOverhead = memoryOverhead;
        this.desc = desc;

        tool.setJars(desc.location.listOfJarFilePaths());
        tool.setVerbose(false);
        tool.sampleType = config.memorySampleType;

        if( config.add )
            addOperation(config, new AddGenerator(), "add", "C=A+B", libraryName, 0 , matrixSize);

        if( config.mult )
            addOperation(config, new MultGenerator(), "mult", "C=A*B", libraryName, 0 , matrixSize);

        if( config.multTransB )
            addOperation(config, new MultTranBGenerator(), "multTransB", "C=A*B^T", libraryName, 0 , matrixSize);

        if( config.solveLinear )
            addOperation(config, new SolveEqGenerator(), "solveExact", "solve m=n", libraryName, 0 , matrixSize);

        if( config.solveLS )
            addOperation(config, new SolveOverGenerator(), "solveOver", "solve m>n", libraryName,0 , matrixSize);

        if( config.invSymmPosDef )
            addOperation(config, new InvertSymmPosDefGenerator(), "invertSymmPosDef", "inv |A| > 1", libraryName,0 , matrixSize);

        if( config.svd )
            addOperation(config, new SvdGenerator(), "svd", "SVD", libraryName,0 , matrixSize/2 );

        if( config.eig )
            addOperation(config, new EigSymmGenerator(), "eigSymm", "Eigen", libraryName,0 , matrixSize);

        if( directorySave != null ) {
            setupOutputDirectory();
            setupLog();
        }

        // print the library's version to a file
        PrintLibraryVersion printVersion = new PrintLibraryVersion(directorySave);
        try {
            printVersion.printVersion(desc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void addOperation(MemoryConfig config, InputOutputGenerator gen ,
                              String nameOperation,
                              String displayName , String libraryName ,
                              int scale , int matrixSize ) {
        activeTasks.add( new Task(nameOperation,
                gen,displayName,libraryName,config.maxTestTimeMilli,scale,matrixSize));
    }

    public void process() {
        while( !activeTasks.isEmpty() ) {
            Task task = activeTasks.get( rand.nextInt(activeTasks.size()));

            System.out.println(libraryName+" operation "+task.results.displayName +"  remaining tasks "+activeTasks.size());

            boolean failed = false;

            for( int i = 0; i < config.numTrials && !failed ; i++ ) {
                if( !findMemory(task) ) {
                    System.out.println("Failed!");
                    logStream.println("FAILED: operation "+task.results.displayName);
                    failed = true;
                }
            }

            saveResults(task.results);

            activeTasks.remove(task);
        }

        logStream.close();
    }

    private void logMeasurement( Task task , long mem ) {
        if( mem > 0 ) {
            mem -= memoryOverhead;
            if( mem < 0 ) {
                logStream.println("Memory less than overhead!! "+mem+"  operation "+task.results.displayName);
            }
            task.results.results.add(mem);
        }
    }

    private void setupOutputDirectory() {
        File d = new File(directorySave);
        if( !d.exists() ) {
            if( !d.mkdirs() ) {
                throw new IllegalArgumentException("Failed to make output directory");
            }
        } else if( !d.isDirectory())
            throw new IllegalArgumentException("The output directory already exists and is not a directory");
    }

    /**
     * Sets out a file for recording errors.
     */
    private void setupLog() {
        try {
            logStream = new PrintStream(directorySave+"/log.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // For debugging purposes output the slave's classpath
        logStream.println("Current directory = "+new File(".").getAbsolutePath());
        logStream.println("Classpath:");
        logStream.println(tool.getClassPath());
        logStream.println();
        logStream.println("Overhead = "+memoryOverhead+" bytes");

        tool.setErrorStream(logStream);
    }

    private void saveResults( MemoryResults results  ) {
        if( directorySave == null )
            return;

        UtilXmlSerialization.serializeXml(results,directorySave+"/"+results.displayName +".xml");
    }

    /**
     * Adjusts the amount of memory allocated until it converges to a solution.  This is done
     * to take in account the garbage collector using up memory when it does not have to.
     */
    public boolean findMemory( Task task )
    {
        int iteration = 0;

        // measuring by giving it the maximum amount of memory
        long found = testMemory(task,config.memoryMaxMB);
        System.out.println(" iteration "+0+"  found "+(found/1024/1024)+" (MB)");

        // it failed
        if( tool.isFailed() || found <= 0 ) {
            task.results.numFailed++;
            return false;
        }

        while( true )  {
            long test = testMemory(task,found/1024/1024);
            if( !tool.failed )
                System.out.println(" iteration "+(++iteration)+"  found "+(test/1024/1024)+" (MB)");
            else
                System.out.println(" iteration "+(++iteration)+" stopped");

            // make sure the improvement is significant
            long tol = (long)(found * 0.01);

            if( test <= 0 || tool.failed || test >= found-tol ) {
                logMeasurement(task,found);
                return true;
            }
            
            found = test;

            if( iteration > 30 ) {
                System.out.println("Too many iterations!!");
                logStream.println("Too many iterations!");
                return false;
            }
        }
    }

    public long testMemory( Task task , long maxMemory )
    {
        if( maxMemory < config.memoryMinMB )
            return -1;

        tool.setFrozenDefaultTime(task.timeout);
        tool.setMemory(config.memoryMinMB,maxMemory);

        MemoryTest test = new MemoryTest();
        test.setup(desc.configure,desc.factoryRuntime,task.gen,task.nameOperation,1,task.matrixSize);
        test.setRandomSeed(config.seed);

        return tool.runTest(test);
    }

    private static class Task {
        String nameOperation;
        MemoryResults results;
        InputOutputGenerator gen;

        long timeout;

        int memoryScale;

        int matrixSize;

        public Task(String nameOperation ,
                    InputOutputGenerator gen,
                    String displayName , String nameLibrary ,
                    long time ,
                    int memoryScale ,
                    int matrixSize ) {
            this.nameOperation = nameOperation;
            this.gen = gen;
            this.timeout = time;
            results = new MemoryResults();
            results.displayName = displayName;
            results.nameLibrary = nameLibrary;
            this.memoryScale = memoryScale;
            this.matrixSize = matrixSize;
        }
    }

    public static void main( String []args ) {
        MemoryConfig config = MemoryConfig.createDefault();

        MemoryBenchmarkLibrary benchmark = new MemoryBenchmarkLibrary(config, FactoryLibraryDescriptions.createEJML(),null,1000,0);

        benchmark.process();
    }
}

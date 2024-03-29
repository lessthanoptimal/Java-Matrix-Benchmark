/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

package jmbench.tools.stability;

import jmbench.impl.LibraryDescription;
import jmbench.tools.BenchmarkConstants;
import jmbench.tools.BenchmarkTools;
import jmbench.tools.EvaluatorSlave;
import jmbench.tools.runtime.LibraryRuntimeInfo;
import jmbench.tools.stability.tests.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Peter Abeles
 */
public class StabilityBenchmarkLibrary {

    private String libraryName;
    private BenchmarkTools tools;

    private List<StabilityTestBase> operations;

    // used to write errors to
    private PrintStream logStream = System.err;
    private FatalError fatalError;

    private StabilityBenchmarkConfig config;

    private String directorySave;

    private int sizeMin;
    private int sizeMax;

    private int numSolve;
    private int numSvd;

    // how much memory was allocated to the slave
    private long slaveMemoryMegaBytes;

    boolean spawnChild = true;

    public StabilityBenchmarkLibrary(String outputDir,
                                     StabilityBenchmarkConfig config,
                                     LibraryDescription target,
                                     int sizeMin,
                                     int sizeMax,
                                     int numSolve,
                                     int numSvd) {

        this.directorySave = outputDir;


        tools = new BenchmarkTools(target.listOfJarFilePaths());
        tools.setOverrideMemory(config.memory);
        tools.setFrozenTimeMS(config.maxProcessingTime);

        if (directorySave != null) {
            setupOutputDirectory();
            setupLog();
        }

        this.config = config;
        this.libraryName = target.info.getNamePlot();

        this.sizeMin = sizeMin;
        this.sizeMax = sizeMax;

        this.numSolve = numSolve;
        this.numSvd = numSvd;

        File runtimeInfoFile = new File(this.directorySave, BenchmarkConstants.RUNTIME_INFO_NAME);
        if (!runtimeInfoFile.exists())
            tools.launch(LibraryRuntimeInfo.class, target.info.factory, runtimeInfoFile.getPath());

        createOperationsList(target.info.factory);

    }

    private void setupOutputDirectory() {
        File d = new File(directorySave);
        if (!d.exists()) {
            if (!d.mkdirs()) {
                throw new IllegalArgumentException("Failed to make output directory");
            }
        } else if (!d.isDirectory())
            throw new IllegalArgumentException("The output directory already exists and is not a directory");
    }

    /**
     * Sets out a file for recording errors.
     */
    private void setupLog() {
        try {
            logStream = new PrintStream(directorySave + "/log.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // For debugging purposes output the slave's classpath
        logStream.println("Current directory = " + new File(".").getAbsolutePath());
        logStream.println("Classpath:");
        logStream.println(tools.getClassPath());
    }

    private void createOperationsList(String library) {
        operations = new ArrayList<>();

        if (config.checkOverflow) {
            if (config.checkLinear)
                operations.add(new SolverOverflow(config.randomSeed,
                        library,
                        "solveExact",
                        numSolve / config.overFlowFactor,
                        config.breakingPoint, sizeMin, sizeMax, true, true));

            if (config.checkLS)
                operations.add(new SolverOverflow(config.randomSeed,
                        library,
                        "solveOver",
                        numSolve / config.overFlowFactor,
                        config.breakingPoint, sizeMin, sizeMax, false, true));

            if (config.checkSVD)
                operations.add(new SvdOverflow(config.randomSeed,
                        library,
                        "svd",
                        numSvd / config.overFlowFactor,
                        config.breakingPoint, sizeMin, sizeMax, true));

            if (config.checkEVD)
                operations.add(new EigSymmOverflow(config.randomSeed,
                        library,
                        "eigSymm",
                        numSvd / config.overFlowFactor,
                        config.breakingPoint, sizeMin, sizeMax, true));

            if (config.checkSymInv)
                operations.add(new InvSymmOverflow(config.randomSeed,
                        library,
                        "invertSymmPosDef",
                        numSolve / config.overFlowFactor,
                        config.breakingPoint, sizeMin, sizeMax, true));
        }

        if (config.checkUnderflow) {
            if (config.checkLinear)
                operations.add(new SolverOverflow(config.randomSeed,
                        library,
                        "solveExact",
                        numSolve / config.overFlowFactor,
                        config.breakingPoint, sizeMin, sizeMax, true, false));

            if (config.checkLS)
                operations.add(new SolverOverflow(config.randomSeed,
                        library,
                        "solveOver",
                        numSolve / config.overFlowFactor,
                        config.breakingPoint, sizeMin, sizeMax, false, false));

            if (config.checkSVD)
                operations.add(new SvdOverflow(config.randomSeed,
                        library,
                        "svd",
                        numSvd / config.overFlowFactor,
                        config.breakingPoint, sizeMin, sizeMax, false));

            if (config.checkEVD)
                operations.add(new EigSymmOverflow(config.randomSeed,
                        library,
                        "eigSymm",
                        numSvd / config.overFlowFactor,
                        config.breakingPoint, sizeMin, sizeMax, false));

            if (config.checkSymInv)
                operations.add(new InvSymmOverflow(config.randomSeed,
                        library,
                        "invertSymmPosDef",
                        numSolve / config.overFlowFactor,
                        config.breakingPoint, sizeMin, sizeMax, false));
        }

        if (config.checkNearlySingular) {
            if (config.checkLinear)
                operations.add(new SolverSingular(config.randomSeed,
                        library,
                        "solveExact",
                        numSolve / config.overFlowFactor,
                        config.breakingPoint, sizeMin, sizeMax, true));

            if (config.checkLS)
                operations.add(new SolverSingular(config.randomSeed,
                        library,
                        "solveOver",
                        numSolve / config.overFlowFactor,
                        config.breakingPoint, sizeMin, sizeMax, false));
        }
        if (config.checkAccuracy) {
            if (config.checkLinear)
                operations.add(new SolverAccuracy(config.randomSeed,
                        library,
                        "solveExact",
                        numSolve,
                        config.breakingPoint, sizeMin, sizeMax, true));

            if (config.checkLS)
                operations.add(new SolverAccuracy(config.randomSeed,
                        library,
                        "solveOver",
                        numSolve,
                        config.breakingPoint, sizeMin, sizeMax, false));

            if (config.checkSVD)
                operations.add(new SvdAccuracy(config.randomSeed,
                        library,
                        "svd",
                        numSvd,
                        sizeMin, sizeMax));

            if (config.checkEVD)
                operations.add(new EigSymmAccuracy(config.randomSeed,
                        library,
                        "eigSymm",
                        numSvd,
                        sizeMin, sizeMax));

            if (config.checkSymInv)
                operations.add(new InvSymmAccuracy(config.randomSeed,
                        library,
                        "invertSymmPosDef",
                        numSolve,
                        sizeMin, sizeMax));
        }
    }

    public void process() {
//        MatrixLibrary libInfo = library.getLibrary();

        for (StabilityTestBase op : operations) {

            System.out.println(libraryName + " :  Processing op: " + op.getTestName());

            StabilityTrialResults results = evaluateOperation(op);

            // if a fatal error occurred create some results so that this is marked
            if (fatalError != null) {
                results = new StabilityTrialResults();
                logStream.println("Had fatal error: " + op.getTestName() + " error = " + fatalError);
                results.fatalError = fatalError;
            }
            // add environmental information for debugging later on
            results.durationMilli = tools.getDurationMilli();
            results.libraryName = libraryName;
            results.benchmarkName = op.getTestName();
            results.memoryBytes = slaveMemoryMegaBytes;

            // save the results to a file
            saveResults(results, op.getFileName());

            // print the results to the screen
            if (fatalError == null)
                printResults(results);
        }

        if (directorySave != null) {
            logStream.close();
        }
    }

    private void printResults(StabilityTrialResults results) {
        System.out.println("    Median             = " + StabilityBenchmark.computePercent(results.breakingPoints, 0.5));
        System.out.println("    Finished           = " + results.getNumFinished());
        System.out.println("    Bad Answer         = " + results.getNumUncountable());
        System.out.println("    Large Error        = " + results.getNumLargeError());
        System.out.println("    Detected           = " + results.getNumGraceful());
        System.out.println("    Runtime Exception  = " + results.getNumUnexpectedException());
    }

    private void saveResults(StabilityTrialResults results, String opFileName) {
        if (directorySave == null)
            return;

        UtilXmlSerialization.serializeXml(results, directorySave + "/" + opFileName + ".xml");
    }

    public StabilityTrialResults evaluateOperation(StabilityTestBase e) {
        fatalError = null;
        e.setRandomSeed(config.randomSeed);

        EvaluatorSlave.Results results = spawnChild ? tools.runTest(e) : tools.runTestNoSpawn(e);
        slaveMemoryMegaBytes = tools.getAllocatedMemoryInMB();

        if (results == null) {
            logStream.println("*** WTF runTest returned null = " + e.getTestName());
            fatalError = FatalError.RETURNED_NULL;
        } else if (results.failed == EvaluatorSlave.FailReason.USER_REQUESTED) {
            logStream.println("    Slave was killed by the user/OS.  Stopping the benchmark.  op = " + e.getTestName());
            logStream.println("    error message: " + results.detailedError);
            System.out.println("  Slave was killed by the user/OS.  Stopping the benchmark.");
            System.out.println("    error message: " + results.detailedError);
            System.exit(0);
        } else if (results.failed == EvaluatorSlave.FailReason.OUT_OF_MEMORY) {
            System.out.println("  Not enough memory given to slave. ");
            logStream.println("Not enough memory for op.  " + e.getTestName() + " memory " + tools.getAllocatedMemoryInMB());
            fatalError = FatalError.OUT_OF_MEMORY;
        } else {
            if (results.failed != null) {
                fatalError = FatalError.MISC;
                if (results.failed == EvaluatorSlave.FailReason.TOO_SLOW) {
                    logStream.println("    Slave: Case too slow = " + e.getTestName());
                } else if (results.failed == EvaluatorSlave.FailReason.FROZEN) {
                    logStream.println("    Slave: Frozen = " + e.getTestName());
                    fatalError = FatalError.FROZE;
                } else {
                    logStream.println("    Slave: Case failed = " + results.failed + " op = " + e.getTestName() + " memory " + tools.getAllocatedMemoryInMB());
                    if (results.detailedError != null) {
                        logStream.println(results.detailedError);
                    }
                }
            }
        }

        // see if something very bad happened
        if (fatalError != null)
            return null;

        // collect all the results and return them
        StabilityTrialResults all = new StabilityTrialResults();

        StabilityTrialResults r = (StabilityTrialResults) results.getResults();
        all.addResults(r);

        return all;
    }
}

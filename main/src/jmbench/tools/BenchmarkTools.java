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

package jmbench.tools;

import jmbench.misc.JavaRuntimeLauncher;
import jmbench.tools.stability.UtilXmlSerialization;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author Peter Abeles
 */
// todo change random seed with each trial, optional
public class BenchmarkTools extends JavaRuntimeLauncher {

    // used to ID stale results
    int requestID= new Random().nextInt();
    // how many MC trials should the slave perform
    int numTrials = 1;
    // if not zero it will allocate this much memory (MB)
    long overrideMemory = 0;
    // amount of memory it always adds in megs
    long baseMemory = 10;
    // used to increase or decrease the added memory
    long memoryScale = 8;

    boolean verbose = true;

    PrintStream errorStream = System.err;

    public BenchmarkTools( int numTrials , long baseMemory , long memoryScale , List<String> pathJars ){
        super(pathJars);

        this.baseMemory = baseMemory;
        this.numTrials = numTrials;
        this.memoryScale = memoryScale;
    }


    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setErrorStream(PrintStream errorStream) {
        this.errorStream = errorStream;
    }

    public long getMemoryScale() {
        return memoryScale;
    }

    public void setMemoryScale(long memoryScale) {
        this.memoryScale = memoryScale;
    }


    public void setOverrideMemory(long overrideMemory) {
        this.overrideMemory = overrideMemory;
    }

    /**
     * Spawns a new java vm to run {@link EvaluatorSlave} which will compute the ops
     * per second for the specified test.
     *
     * @param test A description of which is to be tested by the slave
     * @return The results of the experiment.
     *
     */
    public EvaluatorSlave.Results runTest( EvaluationTest test ) {

        requestID++;

        // write out a file describing what the slave should process.
        UtilXmlSerialization.serializeXml(test, "case.xml");

        // compute required memory in mega bytes
        long allocatedMemory = overrideMemory > 0 ? overrideMemory : (test.getInputMemorySize()/1024/1024+baseMemory)*memoryScale;

        if(verbose)
            System.out.println("Memory = "+allocatedMemory+" MB");

        setMemoryInMB(allocatedMemory);

        switch(launch(EvaluatorSlave.class,"case.xml",Integer.toString(numTrials),Long.toString(requestID) ) )
        {
            case FROZEN:
                errorStream.println("BenchmarkTools: Slave froze and was killed");
                break;

            case RETURN_NOT_ZERO:
                errorStream.println("BenchmarkTools: Slave exited with non-zero value");
                break;

            case NORMAL:break;
        }

        // see if the user terminated the slave
        EvaluatorSlave.Results ret = UtilXmlSerialization.deserializeXml("slave_results.xml");
        if( ret == null || ret.getRequestID() != requestID ) {
            if( ret == null )
                errorStream.println("UtilXmlSerialization.deserializeXml returned null");
            else
                errorStream.println("ret.getRequestID() does not match");
            ret = null;
        }

        cleanup();
        return ret;
    }

    /**
     * Runs the tests but does not spawn a new processes to do so.  This is usefull for debugging
     * purposes.
     *
     * @param test A description of which is to be tested.
     * @return The results of the experiment.
     */
    public EvaluatorSlave.Results runTestNoSpawn( EvaluationTest test ) {
        requestID++;
        List<TestResults> results = new ArrayList<TestResults>();

        test.init();

        for( int i = 0; i < numTrials; i++ ) {
            test.setupTrial();
            TestResults tr = test.evaluate();
            results.add(tr);
        }

        EvaluatorSlave.Results slaveResults = new EvaluatorSlave.Results();

        slaveResults.results = results;
        slaveResults.requestID = requestID;

        return slaveResults;
    }


    /**
     * Delete temporary files that it created to pass information between the master and the slave.
     */
    private static void cleanup() {
        if( !new File("case.xml").delete() ) {
            System.out.println("Couldn't delete case.xml");
        }

        if( !new File("slave_results.xml").delete() ) {
            System.out.println("Couldn't delete slave_results.xml");
        }
    }
}

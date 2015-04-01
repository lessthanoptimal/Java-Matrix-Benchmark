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

package jmbench.tools;

import jmbench.tools.stability.UtilXmlSerialization;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


/**
 * @author Peter Abeles
 */
// todo change random seed with each trial, optional
public class BenchmarkTools {

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
    // jar that need to be added
    String extraJars = "";

    // if no estimated processing time is provided it should wait this long before
    // declaring the process to be frozen
    long frozenDefaultTime = 10*60*1000;

    // how much memory was allocated to the slave in megabytes
    long allocatedMemory;

    // how long the process ran for in milliseconds
    long durationMilli;

    boolean verbose = true;

    PrintStream errorStream = System.err;

    // arguments passed to slave jvm
    String []params;

    public BenchmarkTools(){}

    public BenchmarkTools( int numTrials , long baseMemory , long memoryScale , List<String> jarNames ){
        this.baseMemory = baseMemory;
        this.numTrials = numTrials;
        this.memoryScale = memoryScale;

        setJars(jarNames);
    }

    public void setJars(List<String> jarNames) {
        String sep = System.getProperty("path.separator");

        if( jarNames != null ) {
            extraJars = "";

            for( String s : jarNames ) {
                extraJars = extraJars + sep + s;
            }
        }
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

    public String getClassPath() {
        return System.getProperty("java.class.path")+extraJars;
    }

    public void setFrozenDefaultTime(long frozenDefaultTime) {
        this.frozenDefaultTime = frozenDefaultTime;
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

        params = setupJvmParam(test);

        if(verbose) {
            System.out.println("Test random seed = "+test.getRandomSeed());
        }

        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(params);

            if( System.in == pr.getInputStream() ) {
                System.out.println("Egads");
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

            // print the output from the slave
            boolean frozen = monitorSlave(test, pr, input, error);

            return processSlaveResults(frozen, pr, input, error);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
     * Writes out an xml file that tells the slave what to run and puts together the runtime
     * parameters that are passed on to it.
     */
    public String[] setupJvmParam(EvaluationTest test) {
        // write out a file describing what the slave should process.
        UtilXmlSerialization.serializeXml(test, "case.xml");

        // grab the current classpath and add some additional jars
        String classPath = getClassPath();
        String app = System.getProperty("java.home")+"/bin/java";

        // compute required memory in mega bytes
        allocatedMemory = overrideMemory > 0 ? overrideMemory : (test.getInputMemorySize()/1024/1024+baseMemory)*memoryScale;

        if(verbose)
            System.out.println("Memory = "+allocatedMemory+" MB");

        String []params = new String[10];
        params[0] = app;
        params[1] = "-server";
        params[2] = "-Xms"+allocatedMemory+"M";
        params[3] = "-Xmx"+allocatedMemory+"M";
        params[4] = "-classpath";
        params[5] = classPath;
        params[6] = "jmbench.tools.EvaluatorSlave";
        params[7] = "case.xml";
        params[8] = Integer.toString(numTrials);
        params[9] = Long.toString(requestID);
        return params;
    }

    /**
     * Prints out the standard out and error from the slave and checks its health.  Exits if
     * the slave has finished or is declared frozen.
     */
    private boolean monitorSlave(EvaluationTest test, Process pr,
                                 BufferedReader input, BufferedReader error)
            throws IOException, InterruptedException {

        // flush the input buffer
        System.in.skip(System.in.available());

        // If the total amount of time allocated to the slave exceeds the maximum number of trials multiplied
        // by the maximum runtime plus some fudge factor the slave is declared as frozen
        long mustBeFrozenTime = test.getMaximumRuntime() > 0 ?
                test.getMaximumRuntime()*(numTrials+2) : frozenDefaultTime;

        boolean frozen = false;

        long startTime = System.currentTimeMillis();
        long lastAliveMessage = startTime;
        for(;;) {
            while( System.in.available() > 0 ) {
                if( System.in.read() == 'q' ) {
                    System.out.println("User requested for the application to quit by pressing 'q'");
                    System.exit(0);
                }
            }

            printError(error);

            if( input.ready() ) {
                printInputBuffer(input);
            } else {
                Thread.sleep(500);
            }

            try {
                // exit value throws an exception is the process has yet to stop
                pr.exitValue();
                break;
            } catch( IllegalThreadStateException e) {
                // check to see if the process is frozen
                if(System.currentTimeMillis() - startTime > mustBeFrozenTime ) {
                    frozen = true;
                    break;
                }

                // let everyone know its still alive
                if( System.currentTimeMillis() - lastAliveMessage > 60000 ) {
                    System.out.println("\nMaster is still alive: "+new Date()+"  Press 'q' and enter to quit.");
                    lastAliveMessage = System.currentTimeMillis();
                }
            }
        }
        durationMilli = System.currentTimeMillis()-startTime;
        return frozen;
    }

    private void printError(BufferedReader error) throws IOException {
        while( error.ready() ) {
            int val = error.read();
            if( val < 0 ) break;

            System.out.print(Character.toChars(val));
        }
    }

    private void printInputBuffer(BufferedReader input) throws IOException {

        while( input.ready() ) {
            int val = input.read();
            if( val < 0 ) break;

            System.out.print(Character.toChars(val));
        }
    }

    /**
     * Cleans up after the slave and compiles the results that are returned.
     */
    private EvaluatorSlave.Results processSlaveResults(boolean frozen,
                                                       Process pr,
                                                       BufferedReader input, BufferedReader error)
            throws InterruptedException, IOException {
        EvaluatorSlave.Results ret;

        // flush whatever is left
        printInputBuffer(input);
        printError(error);

        // now look to see what happened
        if( !frozen ) {
            // see if it exited normally
            int exitVal = pr.waitFor();

            if( exitVal != 0 ) {
                errorStream.println("None 0 exit value returned by the slave. val = "+exitVal);

                // see if the user terminated the slave
                ret = UtilXmlSerialization.deserializeXml("slave_results.xml");
                if( ret == null || ret.getRequestID() != requestID ) {
                    ret = null;
                }
            } else {
                // read in the results from the slave
                ret = UtilXmlSerialization.deserializeXml("slave_results.xml");

                // make sure these results are not stale
                if( ret == null ) {
                    errorStream.println("exitVal = "+exitVal+"  Can't found slave_results.xml!");
                    ret = null;
                } else if( ret.getRequestID() != requestID ) {
                    errorStream.println("exitVal = "+exitVal+"  Stale request ID");
                    ret = null;
                }
            }
        } else {
            errorStream.println("BenchmarkTools: Killing a frozen slave.");
            System.out.println("BenchmarkTools: Killing a frozen slave.");
            // kill the frozen process
            pr.destroy();
            pr.waitFor();

            // report that there is no results because the slave froze
            System.out.println("Frozen slave is dead.");
            ret = new EvaluatorSlave.Results();
            ret.failed = EvaluatorSlave.FailReason.FROZEN;
        }
        
        // close the IO streams
        input.close();
        error.close();
        pr.getOutputStream().close();

        // delete temporary files
        cleanup();

        return ret;
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

    /**
     * Returns how long the most recent process took in milliseconds.
     * @return Runtime of the latest process in milliseconds.
     */
    public long getDurationMilli() {
        return durationMilli;
    }

    /**
     * Returns the number of megabytes allocated to the slave.
     *
     * @return Number of megabytes
     */
    public long getAllocatedMemory() {
        return allocatedMemory;
    }

    /**
     * Returns parameters passed to slave jvm
     *
     * @return jvm parameters
     */
    public String[] getParams() {
        return params;
    }
}

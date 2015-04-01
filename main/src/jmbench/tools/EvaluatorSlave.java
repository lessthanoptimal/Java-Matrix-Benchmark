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

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * EvaluatorSlave is a class that evaluates the performance of an algorithm for a specific
 * operation against random inputs of different sizes.  The results are output as an xml
 * file.
 * </p>
 * <p>
 * Processing can stop if: 1) All the trials have been processed.  2) A single trial takes too long.
 * 3) the total processing time is too long.  If the best processing time exceeds the maximum single
 * trial processing time then the results are zeroed and returned.  If the total processing time is
 * exceeded it returns the current best result as is.  The max processing time is a function of the
 * number of trials and desired durration.
 * </p>
 * <p>
 * Time bases stopping conditions are designed to make sure the evaluation tool can be run on a variety
 * of different hardware and not take too long.  If a single trial takes way too long probably all the
 * other trials will also take too long.  The max time is to prevent pathological cases where any single
 * trial doesn't take the max time, but running through everything just is too slow.
 * </p>
 *
 * @author Peter Abeles
 */
public class EvaluatorSlave {

    private static final boolean VERBOSE = false;

    private static long requestID;

    public static void main( String args[] ) throws FileNotFoundException {
        // catch control-c
//        install("INT");
//        install("TERM");

        // parse the input arguments
        if( args.length != 3 ) {
            throw new IllegalArgumentException("Unexpected number of arguments");
        }
        String fileName = args[0];
        int numTrials = Integer.parseInt(args[1]);
        requestID = Long.parseLong(args[2]);

        // load the plan
        EvaluationTest eval = UtilXmlSerialization.deserializeXml(fileName);

        if( eval == null ) {
            System.out.println("Can't deserialize input");
            writeOutFailure(requestID,FailReason.READ_CONFIG_FILE,null);
            return;
        }

        if( VERBOSE ) {
            System.out.println("numTrials = "+numTrials);
            eval.printInfo();
        }

        // evalute
        try {
            Results r = evaluationLoop(numTrials, eval);

            // save the results
            if( VERBOSE ) System.out.println("Slave done");
            r.requestID = requestID;
            UtilXmlSerialization.serializeXml(r,"slave_results.xml");
        } catch( Exception e ) {
            e.printStackTrace();
            String message = e.toString() +"\n";
            StackTraceElement[] stack = e.getStackTrace();
            for( StackTraceElement s : stack ) {
                message += s.toString()+"\n";
            }
            writeOutFailure(requestID,FailReason.MISC_EXCEPTION,message);
        } catch( OutOfMemoryError e ) {
            if( VERBOSE) System.out.println("OutOfMemoryError: Slave is out of memory!");
            writeOutFailure(requestID,FailReason.OUT_OF_MEMORY,null);
        }

        // by calling this exit function the slave will terminate even if a library is poorly
        // written and has a dangling thread.
        System.exit(0);
    }

//    public static MySignalHandler install(String signalName) {
//        Signal diagSignal = new Signal(signalName);
//        MySignalHandler diagHandler = new MySignalHandler();
//        diagHandler.oldHandler = Signal.handle(diagSignal,diagHandler);
//        return diagHandler;
//    }
//
//    /**
//     * Catches control-c signals and let's the master know what happened. If this is not done
//     * then some times when the used hits control-c the benchmark will think the slave failed and stop
//     * the benchmark there.
//     */
//    public static class MySignalHandler implements SignalHandler {
//
//        public SignalHandler oldHandler;
//
//        @Override
//        public void handle(Signal signal) {
//            System.out.println("Diagnostic Signal handler called for signal "+signal);
//            try {
//                // Output information for each thread
//                Thread[] threadArray = new Thread[Thread.activeCount()];
//                int numThreads = Thread.enumerate(threadArray);
//                System.out.println("Current threads:");
//                for (int i=0; i < numThreads; i++) {
//                    System.out.println("    "+threadArray[i]);
//                }
//                writeOutFailure(requestID,FailReason.USER_REQUESTED,"Caught signal: "+signal.getName());
//
//                // Chain back to previous handler, if one exists
//                if ( oldHandler != SIG_DFL && oldHandler != SIG_IGN ) {
//                    oldHandler.handle(signal);
//                }
//
//
//            } catch (Exception e) {
//                System.out.println("Signal handler failed, reason "+e);
//            }
//        }
//    }

    private static void writeOutFailure( long requestID , FailReason reason , String message ) throws FileNotFoundException {
        Results r = new Results();
        r.failed = reason;
        r.requestID = requestID;
        r.detailedError = message;
        UtilXmlSerialization.serializeXml(r,"slave_results.xml");
    }

    /**
     * Evaluate each algorithm several times and result the results.
     */
    private static Results evaluationLoop(int numTrials, EvaluationTest eval) {
        // make sure it is in the correct state
        eval.init();

        // How long does it allow each test to run for
        long maximumRuntime = eval.getMaximumRuntime();

        List<TestResults> results = new ArrayList<TestResults>();

        FailReason fail = null;

        for( int i = 0; i < numTrials; i++ ) {
            // create the matrix inputs for the algorithm.
            if( VERBOSE ) System.out.print("  Trial = "+i);
            eval.setupTrial();

            long before = System.currentTimeMillis();
            TestResults r = eval.evaluate();
            long after = System.currentTimeMillis();

            if( VERBOSE ) System.out.print("  results = "+r);
            results.add(r);

            if( maximumRuntime > -1 && (after-before) > maximumRuntime) {
                fail = FailReason.TOO_SLOW;
                // if a single trial takes too long then it is just stop
                if( VERBOSE )
                    System.out.println("\nSingle test too long: DT = "+(after-before)+" max = "+maximumRuntime);
                break;
            }


            if( VERBOSE ) System.out.println("  DT = "+(after-before));
        }

        Results r = new Results();
        r.results = results;
        r.failed = fail;

        return r;
    }

    /**
     * Returns a summary of the results from the test case just performed.
     */
    public static class Results implements Serializable
    {
        // the requestID that was passed to the slave.
        // this makes sure it is processing the correct results file and not a stale one
        public long requestID;

        // did the computation fail?
        public FailReason failed;

        // results from each trial
        public List<TestResults> results;

        public String detailedError;

        public List<TestResults> getResults() {
            return results;
        }

        public void setResults(List<TestResults> results) {
            this.results = results;
        }

        public FailReason getFailed() {
            return failed;
        }

        public void setFailed(FailReason failed) {
            this.failed = failed;
        }

        public long getRequestID() {
            return requestID;
        }

        public void setRequestID(long requestID) {
            this.requestID = requestID;
        }

        public String getDetailedError() {
            return detailedError;
        }

        public void setDetailedError(String detailedError) {
            this.detailedError = detailedError;
        }
    }

    public static enum FailReason
    {
        MISC_EXCEPTION,
        READ_CONFIG_FILE,
        TOO_SLOW,
        OUT_OF_MEMORY,
        FROZEN,
        /**
         * User requested shutdown.  Control-C
         */
        USER_REQUESTED
    }
}

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

import jmbench.tools.stability.UtilXmlSerialization;

import java.io.FileNotFoundException;
import java.io.Serializable;


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

    public static void main(String[] args) throws FileNotFoundException {
        // catch control-c
//        install("INT");
//        install("TERM");

        // parse the input arguments
        if( args.length != 2 ) {
            throw new IllegalArgumentException("Unexpected number of arguments. Got"+args.length);
        }
        String fileName = args[0];
        requestID = Long.parseLong(args[1]);

        // load the plan
        EvaluationTest eval = UtilXmlSerialization.deserializeXml(fileName);

        if( eval == null ) {
            System.out.println("Can't deserialize input");
            writeOutFailure(requestID,FailReason.READ_CONFIG_FILE,null);
            return;
        }

        if( VERBOSE ) {
            eval.printInfo();
        }

        // evalute
        try {
            Results r = evaluationLoop(eval);

            // save the results
            if( VERBOSE ) System.out.println("Slave done");
            r.requestID = requestID;
            UtilXmlSerialization.serializeXml(r,"slave_results.xml");
        } catch( Error e ) {
            if( e instanceof OutOfMemoryError ) {
                if( VERBOSE) System.out.println("OutOfMemoryError: Slave is out of memory!");
                writeOutFailure(requestID,FailReason.OUT_OF_MEMORY,null);
            } else {
                e.printStackTrace();
                String message = e.toString() + "\n";
                StackTraceElement[] stack = e.getStackTrace();
                for (StackTraceElement s : stack) {
                    message += s.toString() + "\n";
                }
                writeOutFailure(requestID, FailReason.MISC_EXCEPTION, message);
            }
        }

        // by calling this exit function the slave will terminate even if a library is poorly
        // written and has a dangling thread.
        System.exit(0);
    }


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
    private static Results evaluationLoop(EvaluationTest eval) {
        // make sure it is in the correct state
        eval.init();

        // create the matrix inputs for the algorithm.
        eval.setupTest();

        Results results = new Results();
        try {
            results.results = eval.evaluate();
            if( VERBOSE ) System.out.print("  results = "+results.results);
        } catch( RuntimeException e ) {
            e.printStackTrace();
            results.failed = FailReason.MISC_EXCEPTION;
        }

        return results;
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
        public TestResults results;

        public String detailedError;

        public TestResults getResults() {
            return results;
        }

        public void setResults(TestResults results) {
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

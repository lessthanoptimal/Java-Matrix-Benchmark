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

package jmbench.tools.runtime.evaluation;

import jmbench.tools.OutputError;
import jmbench.tools.runtime.RuntimeEvaluationMetrics;
import jmbench.tools.runtime.RuntimeMeasurement;
import jmbench.tools.runtime.RuntimeResults;

import java.io.File;


/**
 * Examines all the results in a directory looking for situations that merit additional attention.
 * It prints an error message whenever something goes wrong.
 *
 * @author Peter Abeles
 */
public class CheckForErrorsInResults {

    File directory;

    public CheckForErrorsInResults( String dir ) {
        directory = new File(dir);

        if( !directory.exists() ) {
            throw new IllegalArgumentException("Directory does not exist: "+dir);
        }

        if( !directory.isDirectory() ) {
            throw new IllegalArgumentException("Need to specify a directory");
        }
    }

    @SuppressWarnings({"unchecked"})
    public void examine() {
        String[] files = directory.list();

        for( String nameLevel0 : files ) {
            File level0 = new File(directory.getPath()+"/"+nameLevel0);

            if( level0.isDirectory() ) {
                String []files2 = level0.list();

                System.out.println("Examining "+level0);

                for( String name2 : files2 ) {
                    if( name2.contains(".csv") ) {

                        name2 = level0.getPath()+"/"+name2;

                        RuntimeResults r = RuntimeResultsCsvIO.read(new File(name2));

                        checkForExceptions(r);
                    }
                }
            }
        }
    }

    public void checkForExceptions( RuntimeResults r ) {
        int numNull = 0;
        int numLargeError = 0;
        int numNaN = 0;
        int numUnknown = 0;

        for( RuntimeEvaluationMetrics metrics : r.getMetrics() ) {
            // if it is null then it didn't have any results for that matrix size
            if( metrics == null ) {
                break;
            }
            for( RuntimeMeasurement rr : metrics.getRawResults() ) {
                if( rr.error == null ) {
                   numNull++;
                } else if( rr.error == OutputError.LARGE_ERROR ) {
                    numLargeError++;
                } else if( rr.error == OutputError.UNCOUNTABLE ) {
                    numNaN++;
                } else if( rr.error != OutputError.NO_ERROR ) {
                    numUnknown++;
                }
            }
        }

        if( numLargeError == 0 && numNaN == 0 && numUnknown == 0 ) {
//            if( numNull == 0 && numLargeError == 0 && numNaN == 0 && numUnknown == 0 ) {
            return;
        }

        System.out.print(r.getLibraryName()+" "+r.getOpName()+" ");

        if( numNull != 0 ) {
            System.out.printf("null %d",numNull);
        }
        if( numLargeError != 0 ) {
            System.out.printf("large error %d ",numLargeError);
        }
        if( numNaN != 0 ) {
            System.out.printf("NaN %d ",numNaN);
        }
        if( numUnknown != 0 ) {
            System.out.printf("unknown %d ",numUnknown);
        }
        System.out.println();
    }

    public static void printHelp() {
        System.out.println("This program runs through the logs and looks for any notable exceptions that occurred.");
        System.out.println("If any exceptions happened then a summary is printed out.");
        System.out.println();
        System.out.println("arguments: <results directory>");
        System.out.println();
        System.out.println("If the results directory is not specified then the most recent directory is used.");
    }

    public static void main( String args[] ) {
        String dir = args.length == 0 ? PlotRuntimeResults.findMostRecentDirectory() : args[0];

        CheckForErrorsInResults p = new CheckForErrorsInResults(dir);

        p.examine();
    }
}
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

package jmbench.tools.stability;

import jmbench.tools.TestResults;

import java.util.ArrayList;
import java.util.List;


/**
 * Summarizes results from a stability benchmark.
 *
 * @author Peter Abeles
 */
public class StabilityTrialResults implements TestResults {

    List<Double> breakingPoints = new ArrayList<Double>();

    // how many times the test was finished without any errors
    int numFinished;
    // Number of times the produced output had a very large error
    int numLargeError;
    //NUmber of times the output had a matrix with an uncountable element
    int numUncountable;
    // Number of times the operation signaled that it could fulfill the request.
    int numGraceful;
    // A runtime exception was thrown that was not expected.
    int numUnexpectedException;

    String libraryName;
    String benchmarkName;

    FatalError fatalError;

    List<ExceptionInfo> unexpectedExceptions = new ArrayList<ExceptionInfo>();

    // how long it took to finish the trial in milliseconds
    public long durationMilli;

    // how much memory was allocated to the process
    public long memoryBytes;// TODO rename to memoryMegabytes

    public StabilityTrialResults() {
    }

    public void addResults( StabilityTrialResults r ) {
        breakingPoints.addAll(r.breakingPoints);

        numFinished += r.numFinished;
        numLargeError += r.numLargeError;
        numUncountable += r.numUncountable;
        numGraceful += r.numGraceful;
        numUnexpectedException += r.numUnexpectedException;
        durationMilli += r.durationMilli;
        if( fatalError == null )
            fatalError = r.fatalError;

        if( r.memoryBytes > memoryBytes ) {
            memoryBytes = r.memoryBytes;
        }

        addUnexpectedExceptions(r);
    }

    public void addUnexpectedExceptions(StabilityTrialResults r) {
        for( ExceptionInfo s : r.unexpectedExceptions ) {
            int where = unexpectedExceptions.indexOf(s);

            if( where == -1 ) {
                unexpectedExceptions.add(s);
            } else {
                unexpectedExceptions.get(where).numTimesThrown += s.numTimesThrown;
            }
        }
    }

    public String toString() {
        if( breakingPoints.size() > 0 )
            return ""+breakingPoints.get(0);
        else
            return getClass().getSimpleName();
    }

    public List<Double> getBreakingPoints() {
        return breakingPoints;
    }

    public void setBreakingPoints(List<Double> breakingPoints) {
        this.breakingPoints = breakingPoints;
    }

    public int getNumFinished() {
        return numFinished;
    }

    public void setNumFinished(int numFinished) {
        this.numFinished = numFinished;
    }

    public int getNumLargeError() {
        return numLargeError;
    }

    public void setNumLargeError(int numLargeError) {
        this.numLargeError = numLargeError;
    }

    public int getNumUncountable() {
        return numUncountable;
    }

    public void setNumUncountable(int numUncountable) {
        this.numUncountable = numUncountable;
    }

    public int getNumGraceful() {
        return numGraceful;
    }

    public void setNumGraceful(int numGraceful) {
        this.numGraceful = numGraceful;
    }

    public int getNumUnexpectedException() {
        return numUnexpectedException;
    }

    public void setNumUnexpectedException(int numUnexpectedException) {
        this.numUnexpectedException = numUnexpectedException;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public String getBenchmarkName() {
        return benchmarkName;
    }

    public void setBenchmarkName(String benchmarkName) {
        this.benchmarkName = benchmarkName;
    }

    public List<ExceptionInfo> getUnexpectedExceptions() {
        return unexpectedExceptions;
    }

    public void setUnexpectedExceptions(List<ExceptionInfo> unexpectedExceptions) {
        this.unexpectedExceptions = unexpectedExceptions;
    }

    public FatalError getFatalError() {
        return fatalError;
    }

    public void setFatalError(FatalError fatalError) {
        this.fatalError = fatalError;
    }

    public long getDurationMilli() {
        return durationMilli;
    }

    public void setDurationMilli(long durationMilli) {
        this.durationMilli = durationMilli;
    }

    public long getMemoryBytes() {
        return memoryBytes;
    }

    public void setMemoryBytes(long memoryBytes) {
        this.memoryBytes = memoryBytes;
    }
}

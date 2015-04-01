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

import jmbench.impl.LibraryConfigure;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.EvaluationTest;
import jmbench.tools.OutputError;
import jmbench.tools.TestResults;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;


/**
 * @author Peter Abeles
 */
public abstract class StabilityTestBase extends EvaluationTest {

    protected Class<RuntimePerformanceFactory> classFactory;
    protected Class<LibraryConfigure> classConfigure;
    protected String nameOperation;
    protected int totalTrials;

    protected double breakingPoint;
    protected transient Random rand;

    protected transient RuntimePerformanceFactory factory;
    protected transient double foundResult;
    protected transient OutputError reason;
    protected transient StabilityTrialResults results;
    protected transient int numResults;

    protected StabilityTestBase(long randomSeed,
                                Class<LibraryConfigure> classConfigure,
                                Class<RuntimePerformanceFactory> classFactory,
                                String nameOperation,
                                int totalTrials,
                                double breakingPoint ) {
        super(randomSeed);
        this.classConfigure = classConfigure;
        this.classFactory = classFactory;
        this.nameOperation = nameOperation;
        this.totalTrials = totalTrials;
        this.breakingPoint = breakingPoint;
    }

    public StabilityTestBase(){}

    abstract public void performTest();

    /**
     * The full name of the test being performed.
     *
     * @return Name of the test being performed.
     */
    public abstract String getTestName();

    /**
     * Name of the file this test should be saved to.
     *
     * @return File name.
     */
    public abstract String getFileName();


    @Override
    public void init() {
        LibraryConfigure configure;
        try {
            factory = classFactory.newInstance();
            configure = classConfigure.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        rand = new Random(randomSeed);
        configure.runtimeConfigure();
    }

    @Override
    public void setupTrial() {
    }

    @Override
    public void printInfo() {
    }

    @Override
    public long getMaximumRuntime() {
        return -1;
    }

    @Override
    public TestResults evaluate() {
        results = new StabilityTrialResults();

        numResults = 0;

        try {
            performTest();
        } catch( FatalProblem ignore ){}

        return results;
    }

    protected void addUnexpectedException( Exception e ) {
        String name = e.getClass().getSimpleName();

        for( ExceptionInfo i : results.unexpectedExceptions ) {
            if( i.getShortName().compareTo(name) == 0 ) {
                i.numTimesThrown++;
                return;
            }
        }
        
        results.unexpectedExceptions.add(new ExceptionInfo(e));

    }

    protected int findMaxPow( double a ) {
        for( int i = 0; true; i++ ) {
            double p = Math.pow(a,i);
            if( Double.isInfinite(p) || p == 0)
                return i;
        }
    }

    protected void saveResults() {
        results.breakingPoints.add(foundResult);

        switch( reason ) {
            case NO_ERROR:
                results.numFinished++;
                break;

            case UNCOUNTABLE:
                results.numUncountable++;
                break;

            case LARGE_ERROR:
                results.numLargeError++;
                break;

            case UNEXPECTED_EXCEPTION:
                results.numUnexpectedException++;
                break;

            case DETECTED_FAILURE:
                results.numGraceful++;
                break;

            case NOT_SUPPORTED:
                results.fatalError = FatalError.UNSUPPORTED;
                throw new FatalProblem();

            default:
                throw new RuntimeException("Unknown reason: "+reason);
        }
        System.gc();
    }

    protected MatrixProcessorInterface createAlgorithm() {
        try {
            Method m = factory.getClass().getMethod(nameOperation);
            return (MatrixProcessorInterface)m.invoke(factory);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Thrown when something goes wrong and it needs to escape
     */
    protected static class FatalProblem extends RuntimeException {

    }

    public Class<RuntimePerformanceFactory> getClassFactory() {
        return classFactory;
    }

    public void setClassFactory(Class<RuntimePerformanceFactory> classFactory) {
        this.classFactory = classFactory;
    }

    public String getNameOperation() {
        return nameOperation;
    }

    public void setNameOperation(String nameOperation) {
        this.nameOperation = nameOperation;
    }

    public int getTotalTrials() {
        return totalTrials;
    }

    public void setTotalTrials(int totalTrials) {
        this.totalTrials = totalTrials;
    }

    public double getBreakingPoint() {
        return breakingPoint;
    }

    public void setBreakingPoint(double breakingPoint) {
        this.breakingPoint = breakingPoint;
    }
}

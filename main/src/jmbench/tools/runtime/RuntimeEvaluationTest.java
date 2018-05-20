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

package jmbench.tools.runtime;

import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.EvaluationTest;
import jmbench.tools.TestResults;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;


/**
 * @author Peter Abeles
 */
public class RuntimeEvaluationTest extends EvaluationTest {

    public static final double MAX_ERROR_THRESHOLD = 0.05;

    // how many trials have already been completed.  Used to determine which random seed is used
    private int completedTrials;

    private String nameAlgorithm;
    private int dimen;
    private String classFactory;
    private InputOutputGenerator generator;
    // how long it should try to run the tests for in milliseconds
    private long goalRuntimeMS;

    // If a call to evaluate exceeds this time limit then the set of tests is aborted
    private long maxEvaluationTimeMS;
    // randomly generated input matrices
    private volatile Random masterRand;

    private volatile BenchmarkMatrix inputs[];
    private volatile BenchmarkMatrix outputs[];
    private volatile RuntimePerformanceFactory factory;

    /**
     * Creates a new evaluation test.
     *
     * @param dimen How big the matrices are that are being processed.
     * @param nameAlgorithm The algorithm that is being processed.
     * @param generator Creates the inputs and expected outputs for the tested operation
     * @param goalRuntime  How long it wants to try to run the test for in milliseconds
     * @param maxEvaluationTime  How long it will let a test run for in milliseconds
     * @param randomSeed The random seed used for the tests.
     */
    public RuntimeEvaluationTest(int completedTrials,
                                 int dimen ,
                                 String classFactory,
                                 String nameAlgorithm ,
                                 InputOutputGenerator generator ,
                                 long goalRuntime, long maxEvaluationTime, long randomSeed )
    {
        super(randomSeed);
        this.completedTrials = completedTrials;
        this.dimen = dimen;
        this.classFactory = classFactory;
        this.nameAlgorithm = nameAlgorithm;
        this.generator = generator;
        this.goalRuntimeMS = goalRuntime;
        this.maxEvaluationTimeMS = maxEvaluationTime;
    }

    public RuntimeEvaluationTest(){}

    @Override
    public void printInfo() {

    }

    /**
     * The slave should call this function before anything else.
     */
    @Override
    public void init() {
        try {
            factory = (RuntimePerformanceFactory)Class.forName(classFactory).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        masterRand = new Random(randomSeed);
        for(int i = 0; i < completedTrials; i++ )
            masterRand.nextLong();
    }

    @Override
    public void setupTest()
    {
        Random rand = new Random(masterRand.nextLong());

        inputs = generator.createInputs(factory,rand,dimen);
        outputs = new BenchmarkMatrix[ generator.numOutputs() ];
    }

    /**
     * Returns how much memory the input matrices will require.
     *
     * @return Required memory in bytes
     */
    @Override
    public long getInputMemorySize() {
        return generator.getRequiredMemory(dimen);
    }

    /**
     * Computes the number of operations per second it takes to run the specified algorithm
     * with the inputs specified in {@link #setupTest()}.
     *
     * @return Number of operations per second.
     */
    @Override
    public TestResults evaluate()
    {
        int cycles = 0;
        // Number of trial's its estimated to meet the minimum time requirement
        long numTrials=1;

        // try to purge all temporary data that has yet to be clean up so that the GC won't run
        // while performance is being measured
        runGarbageCollector();

        MatrixProcessorInterface alg = createAlgorithm();

        // see if the operation isn't supported
        if( alg == null ) {
            return new RuntimeMeasurement(-1,-1);
        }

        // translate it to nanoseconds
        long goalDurationNS = this.goalRuntimeMS*1_000_000;

        // number of warm up trials it should shoot for
        int warmup = 4;

        while( true ) {
            // nano is more precise than the millisecond timer
            long elapsedTimeNS = alg.process(inputs, outputs, numTrials);

//            System.out.printf("SLAVE: elapsed_fraction=%4.2f warmup=%d\n",(elapsedTime/(double)goalDuration),warmup);

            if( elapsedTimeNS > goalDurationNS*0.9 ) {
                // The warm up is a fuzzy function of time and number of tests
                if( elapsedTimeNS > goalDurationNS*30 ) {
                    return compileResults((double)numTrials/(elapsedTimeNS/1e9));
                } else {
                    warmup = Math.max(0,warmup - (int)(elapsedTimeNS/(goalDurationNS*0.9)));
                }
            }

            // use a linear model to estimate the number of trials needed. The JVM might be optimizing the code
            // so this will be a bit chaotic in the beginning
            long oldNumTrials = numTrials;
                
            numTrials = (long)Math.ceil(goalDurationNS * (double)numTrials / (double)elapsedTimeNS);
            if( oldNumTrials > numTrials ) {
                numTrials = oldNumTrials;
            }
            runGarbageCollector();

            if( cycles++ > 20 ) {
                throw new RuntimeException("Exceeded the opsPerSecondMax cycles");
            }
        }
    }

    private void runGarbageCollector() {
        // try to get it to clean up some
        for( int i = 0; i < 5; i++ ) {
            System.gc();
            Thread.yield();
            System.gc();
            Thread.yield();
        }
    }

    /**
     * Generates the results based upon the computed opsPerSecond and the expected output.
     */
    private RuntimeMeasurement compileResults( double opsPerSecond )
    {

        return new RuntimeMeasurement(opsPerSecond,Runtime.getRuntime().totalMemory());
    }

    private MatrixProcessorInterface createAlgorithm() {
        try {
            Method m = factory.getClass().getMethod(nameAlgorithm);
            return (MatrixProcessorInterface)m.invoke(factory);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public String getClassFactory() {
        return classFactory;
    }

    public void setClassFactory(String classFactory) {
        this.classFactory = classFactory;
    }

    public String getNameAlgorithm() {
        return nameAlgorithm;
    }

    public void setNameAlgorithm(String nameAlgorithm) {
        this.nameAlgorithm = nameAlgorithm;
    }

    public int getDimen() {
        return dimen;
    }

    public void setDimen(int dimen) {
        this.dimen = dimen;
    }

    public InputOutputGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(InputOutputGenerator generator) {
        this.generator = generator;
    }

    public long getGoalRuntimeMS() {
        return goalRuntimeMS;
    }

    public void setGoalRuntimeMS(long goalRuntimeMS) {
        this.goalRuntimeMS = goalRuntimeMS;
    }

    public int getCompletedTrials() {
        return completedTrials;
    }

    public void setCompletedTrials(int completedTrials) {
        this.completedTrials = completedTrials;
    }

    @Override
    public long getMaximumEvaluateTime() {
        return maxEvaluationTimeMS;
    }

    public void setMaximumEvaluateTime(long maxRuntime) {
        this.maxEvaluationTimeMS = maxRuntime;
    }
}

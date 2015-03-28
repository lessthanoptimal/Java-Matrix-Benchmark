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

import jmbench.impl.LibraryConfigure;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.EvaluationTest;
import jmbench.tools.TestResults;
import jmbench.tools.runtime.InputOutputGenerator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;


/**
 * Performs the specified memory test.  If inputs are null that means its a test to see the what the overhead is
 *
 * @author Peter Abeles
 */
public class MemoryTest extends EvaluationTest {

    Class<RuntimePerformanceFactory> classFactory;
    Class<LibraryConfigure> classConfigure;
    InputOutputGenerator gen;
    String nameOperation;
    int N;
    int size;

    volatile RuntimePerformanceFactory factory;

    public void setup( Class<LibraryConfigure> classConfigure, Class<RuntimePerformanceFactory> classFactory ,
                       InputOutputGenerator gen ,
                       String nameOperation , int N , int size ) {
        this.classConfigure = classConfigure;
        this.classFactory = classFactory;
        this.gen = gen;
        this.nameOperation = nameOperation;
        this.N = N;
        this.size = size;
    }

    @Override
    public void init() {
        LibraryConfigure configure;
        if( classFactory != null ) {
            try {
                configure = classConfigure.newInstance();
                factory = classFactory.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            configure.runtimeConfigure();
        }
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
    public long getInputMemorySize() {
        return -1;
    }

    @Override
    public TestResults evaluate() {
        Random rand = new Random(randomSeed);

        BenchmarkMatrix []inputs = gen != null ? gen.createInputs(factory,rand,false,size) : null;

        double mod[] = null;

        if( gen != null ) {
            mod = new double[ inputs.length ];
            for( int i = 0; i < inputs.length; i++ ) {
                mod[i] = inputs[i].get(0,0);
            }
        }

        MatrixProcessorInterface operation = createAlgorithm();

        // see if the operation is supported
        if( operation == null ) {
            return new Results(-1);
        }

        long start = System.currentTimeMillis();
        // output is null since that might require creating new memory, which isn't strictly part of th test
        operation.process(inputs,null,N);
        long stop= System.currentTimeMillis();

        if( gen != null ) {
            for( int i = 0; i < inputs.length; i++ ) {
                if( mod[i] != inputs[i].get(0,0) )
                    throw new RuntimeException("Input modified! Input "+i);
            }
        }

        // pause it for a bit so if its sampling the max it has a time to catch it before
        // the application exits
        synchronized (this){
            try {
                wait(300);
            } catch (InterruptedException e) {
            }
        }

        return new Results(stop-start);
    }

    public static class Results implements TestResults
    {
        long elapsedTime;

        public Results(long elapsedTime) {
            this.elapsedTime = elapsedTime;
//            System.out.println(" Elapsed time  "+elapsedTime);
        }

        public Results() {
        }

        public long getElapsedTime() {
            return elapsedTime;
        }

        public void setElapsedTime(long elapsedTime) {
            this.elapsedTime = elapsedTime;
        }
    }

    private MatrixProcessorInterface createAlgorithm() {
        if( nameOperation == null ) {
            return new OverheadProcess();
        }

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

    public int getN() {
        return N;
    }

    public void setN(int n) {
        N = n;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public InputOutputGenerator getGen() {
        return gen;
    }

    public void setGen(InputOutputGenerator gen) {
        this.gen = gen;
    }
}

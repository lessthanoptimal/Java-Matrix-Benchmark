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

package jmbench.tools.stability.tests;

import jmbench.impl.LibraryConfigure;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.DetectedException;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.OutputError;
import jmbench.tools.stability.StabilityTestBase;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;


/**
 * @author Peter Abeles
 */
public abstract class OverflowTestBase extends StabilityTestBase
        implements BreakingPointBinarySearch.Processor
{
    protected boolean overflow;
    protected int minLength;
    protected int maxLength;

    protected volatile DenseMatrix64F A;
    protected volatile DenseMatrix64F Ascaled;
    private volatile double scaling;

    private volatile BreakingPointBinarySearch search;

    public OverflowTestBase(long randomSeed,
                            Class<LibraryConfigure> classConfigure , Class<RuntimePerformanceFactory> classFactory ,
                            String operation,
                       int totalTrials, double breakingPoint, int minLength, int maxLength,
                       boolean overflow)
    {
        super(randomSeed, classConfigure , classFactory , operation, totalTrials, breakingPoint);
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.overflow = overflow;
    }

    public OverflowTestBase(){}

    @Override
    public void performTest() {
        if( overflow ) {
            scaling = 10.0;
        } else {
            scaling = 0.1;
        }

        search = new BreakingPointBinarySearch(this);

        for( int i = 0; i < totalTrials; i++ ) {
            int m = rand.nextInt(maxLength-minLength)+minLength;
            int n = rand.nextInt(maxLength-minLength)+minLength;

            createMatrix(m,n);

            beakOperation();

            saveResults();
        }
    }

    protected abstract void createMatrix( int m , int n);

    private void beakOperation() {

        reason = OutputError.NO_ERROR;
        int where = search.findCriticalPoint(-1,findMaxPow(scaling));
        foundResult = Math.pow(scaling,where);
    }

    @Override
    public boolean check( int testPoint ) {
        double scale = Math.pow(scaling,testPoint);
        Ascaled.set(A);
        CommonOps.scale(scale,Ascaled);

        DenseMatrix64F inputs[] = new DenseMatrix64F[]{Ascaled};
        BenchmarkMatrix[] inputsB = new BenchmarkMatrix[inputs.length];
        BenchmarkMatrix[] outputB = new BenchmarkMatrix[getNumOutputs()];

        for( int i = 0; i < inputs.length; i++ ) {
            inputsB[i] = factory.convertToLib(inputs[i]);
        }

        MatrixProcessorInterface operation = createAlgorithm();

        if( operation == null ) {
            reason = OutputError.NOT_SUPPORTED;
            return false;
        }

        try {
            operation.process(inputsB,outputB,1);
        } catch( DetectedException e ) {
            reason = OutputError.DETECTED_FAILURE;
            return false;
        } catch( Exception e ) {
            addUnexpectedException(e);
            reason = OutputError.UNEXPECTED_EXCEPTION;
            return false;
        }

        DenseMatrix64F results[] = new DenseMatrix64F[outputB.length];
        for( int i = 0; i < results.length; i++ )
            results[i] = factory.convertToEjml(outputB[i]);

        for( DenseMatrix64F R : results ) {
            if( MatrixFeatures.hasUncountable(R)){
                reason = OutputError.UNCOUNTABLE;
                return false;
            }
        }

        return checkResults(results);
    }

    protected abstract boolean checkResults(DenseMatrix64F results[]);

    protected abstract int getNumOutputs();


    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }
}

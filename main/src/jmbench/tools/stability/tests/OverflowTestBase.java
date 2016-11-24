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

package jmbench.tools.stability.tests;

import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.DetectedException;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.matrix.RowMajorMatrix;
import jmbench.matrix.RowMajorOps;
import jmbench.tools.OutputError;
import jmbench.tools.stability.StabilityTestBase;


/**
 * @author Peter Abeles
 */
public abstract class OverflowTestBase extends StabilityTestBase
        implements BreakingPointBinarySearch.Processor
{
    protected boolean overflow;
    protected int minLength;
    protected int maxLength;

    protected volatile RowMajorMatrix A;
    protected volatile RowMajorMatrix Ascaled;
    private volatile double scaling;

    private volatile BreakingPointBinarySearch search;

    public OverflowTestBase(long randomSeed,
                            String classFactory ,
                            String operation,
                       int totalTrials, double breakingPoint, int minLength, int maxLength,
                       boolean overflow)
    {
        super(randomSeed, classFactory , operation, totalTrials, breakingPoint);
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
        RowMajorOps.scale(scale, Ascaled);

        RowMajorMatrix inputs[] = new RowMajorMatrix[]{Ascaled};
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

        RowMajorMatrix results[] = new RowMajorMatrix[outputB.length];
        for( int i = 0; i < results.length; i++ )
            results[i] = factory.convertToRowMajor(outputB[i]);

        for( RowMajorMatrix R : results ) {
            if( RowMajorOps.hasUncountable(R)){
                reason = OutputError.UNCOUNTABLE;
                return false;
            }
        }

        return checkResults(results);
    }

    protected abstract boolean checkResults(RowMajorMatrix results[]);

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

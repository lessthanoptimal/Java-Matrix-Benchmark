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
import jmbench.tools.stability.StabilityBenchmark;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;


/**
 * @author Peter Abeles
 */
public class SolverOverflow extends SolverCommon
        implements BreakingPointBinarySearch.Processor
{

    protected boolean overflow;

    private volatile BreakingPointBinarySearch search;
    private volatile DenseMatrix64F A_scale = new DenseMatrix64F(10,10);
    private volatile DenseMatrix64F b_scale = new DenseMatrix64F(10,1);
    private volatile DenseMatrix64F y = new DenseMatrix64F(10,1);

    private volatile double scaling;

    public SolverOverflow(long randomSeed,
                          Class<LibraryConfigure> classConfigure ,
                          Class<RuntimePerformanceFactory> classFactory ,
                          String nameOperation,
                          int totalTrials,
                          double breakingPoint ,
                          int minLength, int maxLength,
                          boolean linearSolver ,
                          boolean overflow )
    {
        super(randomSeed, classConfigure, classFactory, nameOperation, totalTrials, breakingPoint , minLength, maxLength, linearSolver);

        this.overflow = overflow;
    }

    public SolverOverflow(){}

    @Override
    public String getTestName() {
        if(overflow) {
            if( isLinearSolver )
                return "Linear Overflow";
            else
                return "Least Squares Overflow";
        } else {
            if( isLinearSolver )
                return "Linear Underflow";
            else
                return "Least Squares Underflow";
        }
    }

    @Override
    public String getFileName() {
        if(overflow) {
            if( isLinearSolver )
                return "LinearOverflow";
            else
                return "LeastSquaresOverflow";
        } else {
            if( isLinearSolver )
                return "LinearUnderflow";
            else
                return "LeastSquaresUnderflow";
        }
    }

    @Override
    public void performTest() {

        if(overflow) {
            scaling = 10.0;
        } else {
            scaling = 1.0/10.0;
        }

        search = new BreakingPointBinarySearch(this);

        for( int i = 0; i < totalTrials; i++ ) {
            int m,n;

//            long mem1 = Runtime.getRuntime().totalMemory() -
//                  Runtime.getRuntime().freeMemory();
//
//            System.out.println("Memory usage = "+mem1);

            if( isLinearSolver ) {
                m = n = rand.nextInt(maxLength-minLength)+minLength;
            } else {
                // least squares can handle over determined systems
                m = rand.nextInt(maxLength-minLength)+minLength;
                n = minLength;
                if( m > minLength )
                    n += rand.nextInt(m-minLength);
            }

            createMatrix(m,n,1);
            evaluateOverflowSolver(m,n);

            saveResults();
        }
    }

    private void evaluateOverflowSolver( int m , int n ) {
        // avoid declaring new memory, which is important when prcessing large matrices
        if( A_scale.data.length >= m*n ) {
            A_scale.reshape(m,n,false);
        } else {
            A_scale = new DenseMatrix64F(m,n);
        }
        if( b_scale.data.length >= m ) {
            b_scale.reshape(m,1,false);
            y.reshape(m,1,false);
        } else {
            b_scale = new DenseMatrix64F(m,1);
            y = new DenseMatrix64F(m,1);
        }

        reason = OutputError.NO_ERROR;
        int where = search.findCriticalPoint(0,findMaxPow(scaling));
        foundResult = Math.pow(scaling,where);
    }

    @Override
    public boolean check(int testPoint) {
//        System.out.println("check = "+testPoint);
        double scale = Math.pow(scaling,testPoint);

        CommonOps.scale(scale,A,A_scale);
        CommonOps.scale(scale,b,b_scale);

        BenchmarkMatrix[] inputsB = new BenchmarkMatrix[2];
        BenchmarkMatrix[] outputB = new BenchmarkMatrix[1];

        inputsB[0] = factory.convertToLib(A_scale);
        inputsB[1] = factory.convertToLib(b_scale);

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

        DenseMatrix64F x = results[0];

        if( MatrixFeatures.hasUncountable(x)) {
            reason = OutputError.UNCOUNTABLE;
            return false;
        }

        double error = StabilityBenchmark.residualErrorMetric(A_scale,x,b_scale);

        // all of these are considered large errors because earlier the solution it produced
        // was countable, it only become uncountable when computing the error.
        if( Double.isNaN(error) || Double.isInfinite(error) || error > breakingPoint) {
            reason = OutputError.LARGE_ERROR;
            return false;
        }

        System.gc();
        Thread.yield();
        return true;
    }

    public boolean isOverflow() {
        return overflow;
    }

    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }
}

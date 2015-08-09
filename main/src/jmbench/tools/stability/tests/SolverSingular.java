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
import jmbench.matrix.RowMajorMatrix;
import jmbench.matrix.RowMajorOps;
import jmbench.tools.OutputError;
import jmbench.tools.stability.StabilityBenchmark;


/**
 * @author Peter Abeles
 */
public class SolverSingular extends SolverCommon
        implements BreakingPointBinarySearch.Processor
{

    private volatile RowMajorMatrix U;
    private volatile RowMajorMatrix V;
    private volatile double []sv;
    private volatile double svMag = 1;
    private volatile int whichSV;
    private volatile BreakingPointBinarySearch search;

    public SolverSingular(long randomSeed,
                          Class<LibraryConfigure> classConfigure ,
                          Class<RuntimePerformanceFactory> classFactory, String nameOperation,
                          int totalTrials,
                          double breakingPoint ,
                          int minLength, int maxLength,
                          boolean linearSolver) {
        super(randomSeed, classConfigure , classFactory, nameOperation, totalTrials, breakingPoint , minLength, maxLength, linearSolver);
    }

    public SolverSingular() {

    }

    @Override
    public String getTestName() {
        if( isLinearSolver )
            return "Linear singular";
        else
            return "Least Squares singular";
    }

    @Override
    public String getFileName() {
        if( isLinearSolver )
            return "LinearSingular";
        else
            return "LeastSquaresSingular";  
    }

    @Override
    public void performTest() {

        search = new BreakingPointBinarySearch(this);

        for( int i = 0; i < totalTrials; i++ ) {
//            System.out.print("Trial "+i+"  \n");
            int m,n;

            if( isLinearSolver ) {
                m = n = rand.nextInt(maxLength-minLength)+minLength;
            } else {
                // least squares can handle over determined systems
                m = rand.nextInt(maxLength-minLength)+minLength;
                n = minLength;
                if( m > minLength )
                    n += rand.nextInt(m-minLength);
            }

            int min = Math.min(m,n);
            whichSV = rand.nextInt(min);

//            System.out.println("trail = "+i+"( "+m+" "+n+" )");

            evaluateNearlySingular(m,n);

            saveResults();
        }
    }

    private void evaluateNearlySingular(int m, int n) {
        U = RowMajorOps.createOrthogonal(m, m, rand);
        V = RowMajorOps.createOrthogonal(n,n,rand);

        int o = Math.min(m,n);

        sv = new double[o];
        for( int i = 0; i < o; i++ )
            sv[i] = svMag;

        A = createMatrix(U,V,sv);
        RowMajorMatrix x = RowMajorOps.createRandom(n,1,rand);
        b = new RowMajorMatrix(m,1);

        RowMajorOps.mult(A,x,b);

        reason = OutputError.NO_ERROR;
        int point = search.findCriticalPoint(-1,findMaxPow(0.9)+1);
        foundResult = Math.pow(0.9,point)*svMag;
    }

    @Override
    public boolean check(int testPoint) {
        sv[whichSV] = Math.pow(0.9,testPoint)*svMag;

        RowMajorMatrix A_adj = createMatrix(U,V,sv);

        BenchmarkMatrix[] inputsB = new BenchmarkMatrix[2];
        BenchmarkMatrix[] outputB = new BenchmarkMatrix[1];

        inputsB[0] = factory.convertToLib(A_adj);
        inputsB[1] = factory.convertToLib(b);

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

        RowMajorMatrix x = results[0];


        if(RowMajorOps.hasUncountable(x)) {
            reason = OutputError.UNCOUNTABLE;
            return false;
        }

        double residual = StabilityBenchmark.residualErrorMetric(A_adj,x,b);
//            System.out.println(residual);
        if( residual > breakingPoint ) {
            reason = OutputError.LARGE_ERROR;
            return false;
        }
        return true;
    }


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

    public boolean isLinearSolver() {
        return isLinearSolver;
    }

    public void setLinearSolver(boolean linearSolver) {
        isLinearSolver = linearSolver;
    }
}
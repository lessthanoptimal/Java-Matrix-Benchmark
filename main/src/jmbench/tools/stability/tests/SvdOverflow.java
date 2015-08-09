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
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.matrix.RowMajorMatrix;
import jmbench.matrix.RowMajorOps;
import jmbench.tools.BenchmarkToolsMasterApp;
import jmbench.tools.OutputError;
import jmbench.tools.stability.StabilityBenchmark;


/**
 * @author Peter Abeles
 */
public class SvdOverflow extends OverflowTestBase {

    private static final double svMag = 1;

    public SvdOverflow(long randomSeed,
                       Class<LibraryConfigure> classConfigure , Class<RuntimePerformanceFactory> classFactory,
                       String nameOperation, int totalTrials,
                       double breakingPoint, int minLength, int maxLength, boolean overflow) {
        super(randomSeed, classConfigure , classFactory, nameOperation, totalTrials, breakingPoint, minLength, maxLength, overflow);
    }

    public SvdOverflow(){}

    @Override
    protected void createMatrix( int m, int n ) {
//        System.out.println("Matrix size = ("+m+" , "+n+" )");
        RowMajorMatrix U = RowMajorOps.createOrthogonal(m, m, rand);
        RowMajorMatrix V = RowMajorOps.createOrthogonal(n,n,rand);

        int o = Math.min(m,n);

        // randomly generate singular values and put into ascending order
        double[] sv = new double[o];
        for( int i = 0; i < o; i++ )
        // perturb it from being exactly svMag since that is a pathological case for some
        // algorithms and not common in real world scenarios
            sv[i] = svMag+rand.nextDouble()* BenchmarkToolsMasterApp.SMALL_PERTURBATION;

        A = SolverCommon.createMatrix(U,V, sv);
        Ascaled = new RowMajorMatrix(m,n);
    }

    @Override
    protected int getNumOutputs() {
        return 3;
    }

    @Override
    protected boolean checkResults(RowMajorMatrix[] results) {
        RowMajorMatrix U = results[0];
        RowMajorMatrix S = results[1];
        RowMajorMatrix V = results[2];

        RowMajorMatrix US = new RowMajorMatrix(U.numRows,S.numCols);
        RowMajorMatrix V_tran = new RowMajorMatrix(V.numCols,V.numRows);

        RowMajorMatrix foundA = new RowMajorMatrix(A.numRows,A.numCols);

        RowMajorOps.transpose(V,V_tran);
        RowMajorOps.mult(U, S, US);
        RowMajorOps.mult(US,V_tran,foundA);

        double error = StabilityBenchmark.residualError(foundA,Ascaled);

        if( error > breakingPoint ) {
            reason = OutputError.LARGE_ERROR;
            return false;
        }

        return true;
    }

    @Override
    public String getTestName() {
        if( overflow)
            return "SVD Overflow";
        else
            return "SVD Underflow";
    }

    @Override
    public String getFileName() {
        if( overflow )
            return "SvdOverflow";
        else
            return "SvdUnderflow";
    }

    @Override
    public long getInputMemorySize() {
        return 8*maxLength*maxLength*10;
    }
}
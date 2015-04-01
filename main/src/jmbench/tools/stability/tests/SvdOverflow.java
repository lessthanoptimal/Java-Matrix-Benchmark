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
import jmbench.tools.BenchmarkToolsMasterApp;
import jmbench.tools.OutputError;
import jmbench.tools.stability.StabilityBenchmark;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;


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
        DenseMatrix64F U = RandomMatrices.createOrthogonal(m,m,rand);
        DenseMatrix64F V = RandomMatrices.createOrthogonal(n,n,rand);

        int o = Math.min(m,n);

        // randomly generate singular values and put into ascending order
        double[] sv = new double[o];
        for( int i = 0; i < o; i++ )
        // perturb it from being exactly svMag since that is a pathological case for some
        // algorithms and not common in real world scenarios
            sv[i] = svMag+rand.nextDouble()* BenchmarkToolsMasterApp.SMALL_PERTURBATION;

        A = SolverCommon.createMatrix(U,V, sv);
        Ascaled = new DenseMatrix64F(m,n);
    }

    @Override
    protected int getNumOutputs() {
        return 3;
    }

    @Override
    protected boolean checkResults(DenseMatrix64F[] results) {
        SimpleMatrix U = SimpleMatrix.wrap(results[0]);
        SimpleMatrix S = SimpleMatrix.wrap(results[1]);
        SimpleMatrix V = SimpleMatrix.wrap(results[2]);

        DenseMatrix64F foundA = U.mult(S).mult(V.transpose()).getMatrix();

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
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
import jmbench.tools.OutputError;
import jmbench.tools.stability.StabilityBenchmark;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;
import org.ejml.simple.SimpleMatrix;

import java.util.Arrays;


/**
 * @author Peter Abeles
 */
public class SvdAccuracy extends AccuracyTestBase {

    private static final double maxMag = 50;

    private volatile DenseMatrix64F A;
    private volatile double sv[];

    public SvdAccuracy(long randomSeed,
                       Class<LibraryConfigure> classConfigure ,
                       Class<RuntimePerformanceFactory> classFactory, String nameOperation,
                       int totalTrials, int minLength, int maxLength) {
        super(randomSeed, classConfigure , classFactory, nameOperation, totalTrials, minLength, maxLength);
    }

    public SvdAccuracy(){}


    @Override
    protected void createMatrix( int m, int n ) {
        int o = Math.min(m,n);
        int numS = rand.nextInt(o);

        while( numS == 0 ) {
            numS = rand.nextInt(o);
        }

//        System.out.println("Matrix size = ("+m+" , "+n+" )");
        DenseMatrix64F U = RandomMatrices.createOrthogonal(m,m,rand);
        DenseMatrix64F V = RandomMatrices.createOrthogonal(n,n,rand);

        // randomly generate singular values and put into ascending order
        sv = new double[o];
        for( int i = 0; i < numS; i++ )
            sv[i] = -rand.nextDouble()*maxMag;

        Arrays.sort(sv);
        for( int i = 0; i < numS; i++ )
            sv[i] = -sv[i];

        A = SolverCommon.createMatrix(U,V,sv);
    }

    @Override
    protected int getNumOutputs() {
        return 3;
    }

    @Override
    protected DenseMatrix64F[] createInputs() {
        return new DenseMatrix64F[]{A};
    }

    @Override
    protected void processResults(DenseMatrix64F[] inputs, DenseMatrix64F[] results) {
        SimpleMatrix U = SimpleMatrix.wrap(results[0]);
        SimpleMatrix S = SimpleMatrix.wrap(results[1]);
        SimpleMatrix V = SimpleMatrix.wrap(results[2]);

        if(MatrixFeatures.hasUncountable(U.getMatrix()) ||
                MatrixFeatures.hasUncountable(S.getMatrix()) ||
                MatrixFeatures.hasUncountable(V.getMatrix()) ) {
            reason = OutputError.UNCOUNTABLE;
            return;
        }

        DenseMatrix64F foundA = U.mult(S).mult(V.transpose()).getMatrix();

        foundResult = StabilityBenchmark.residualError(foundA,A);
    }

    @Override
    public String getTestName() {
        return "SVD Accuracy";
    }

    @Override
    public String getFileName() {
        return "SvdAccuracy";
    }

    @Override
    public long getInputMemorySize() {
        return 8*maxLength*maxLength*10;
    }
}

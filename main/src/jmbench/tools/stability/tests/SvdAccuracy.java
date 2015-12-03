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

import jmbench.matrix.RowMajorMatrix;
import jmbench.matrix.RowMajorOps;
import jmbench.tools.OutputError;
import jmbench.tools.stability.StabilityBenchmark;

import java.util.Arrays;


/**
 * @author Peter Abeles
 */
public class SvdAccuracy extends AccuracyTestBase {

    private static final double maxMag = 50;

    private volatile RowMajorMatrix A;
    private volatile double sv[];

    public SvdAccuracy(long randomSeed,
                       String classConfigure ,
                       String classFactory, String nameOperation,
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
        RowMajorMatrix U = RowMajorOps.createOrthogonal(m,m,rand);
        RowMajorMatrix V = RowMajorOps.createOrthogonal(n,n,rand);

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
    protected RowMajorMatrix[] createInputs() {
        return new RowMajorMatrix[]{A};
    }

    @Override
    protected void processResults(RowMajorMatrix[] inputs, RowMajorMatrix[] results) {
        RowMajorMatrix U = results[0];
        RowMajorMatrix S = results[1];
        RowMajorMatrix V = results[2];

        if(RowMajorOps.hasUncountable(U) || RowMajorOps.hasUncountable(S) || RowMajorOps.hasUncountable(V) ) {
            reason = OutputError.UNCOUNTABLE;
            return;
        }

        RowMajorMatrix US = new RowMajorMatrix(U.numRows,S.numCols);
        RowMajorMatrix V_tran = new RowMajorMatrix(V.numCols,V.numRows);

        RowMajorMatrix foundA = new RowMajorMatrix(A.numRows,A.numCols);

        RowMajorOps.transpose(V,V_tran);
        RowMajorOps.mult(U, S, US);
        RowMajorOps.mult(US,V_tran,foundA);

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

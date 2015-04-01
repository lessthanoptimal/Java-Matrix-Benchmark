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
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.RandomMatrices;


/**
 * Accuracy benchmark for inverting symmetric positive definite matrices.
 *
 * @author Peter Abeles
 */
public class InvSymmAccuracy extends AccuracyTestBase {

    protected volatile DenseMatrix64F A;
    protected volatile DenseMatrix64F I_found;
    protected volatile DenseMatrix64F I;

    public InvSymmAccuracy(long randomSeed,
                           Class<LibraryConfigure> classConfigure , Class<RuntimePerformanceFactory> classFactory,
                           String nameOperation,
                           int totalTrials, int minLength, int maxLength) {
        super(randomSeed, classConfigure , classFactory, nameOperation, totalTrials, minLength, maxLength);
    }

    public InvSymmAccuracy() {}

    @Override
    protected void createMatrix( int m , int n ) {
        A = RandomMatrices.createSymmPosDef(m,rand);

        I_found = new DenseMatrix64F(m,m);
        I = CommonOps.identity(m);
    }

    @Override
    protected DenseMatrix64F[] createInputs() {
        return  new DenseMatrix64F[]{A};
    }

    @Override
    protected int getNumOutputs() {
        return 1;
    }

    @Override
    protected void processResults(DenseMatrix64F[] inputs, DenseMatrix64F[] results) {
        DenseMatrix64F A_inv = results[0];

        if(MatrixFeatures.hasUncountable(A_inv) ) {
            reason = OutputError.UNCOUNTABLE;
            return;
        }

        CommonOps.mult(A,A_inv,I_found);

        foundResult = StabilityBenchmark.residualError(I_found,I);
    }

    @Override
    public String getTestName() {
        return "Invert Symmetric Pos. Def. Accuracy";
    }

    @Override
    public String getFileName() {
        return "InvSymmAccuracy";
    }

    @Override
    public long getInputMemorySize() {
        return 8*maxLength*maxLength*10;
    }
}
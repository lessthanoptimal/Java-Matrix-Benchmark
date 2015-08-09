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

import jmbench.impl.LibraryConfigure;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.matrix.RowMajorMatrix;
import jmbench.matrix.RowMajorOps;
import jmbench.tools.OutputError;
import jmbench.tools.stability.StabilityBenchmark;


/**
 * @author Peter Abeles
 */
public class EigSymmAccuracy extends AccuracyTestBase {

    protected volatile RowMajorMatrix A;
    protected volatile RowMajorMatrix L;
    protected volatile RowMajorMatrix R;

    public EigSymmAccuracy(long randomSeed,
                           Class<LibraryConfigure> classConfigure , Class<RuntimePerformanceFactory> classFactory,
                           String nameOperation,
                           int totalTrials, int minLength, int maxLength) {
        super(randomSeed, classConfigure , classFactory, nameOperation, totalTrials, minLength, maxLength);
    }

    public EigSymmAccuracy() {}

    @Override
    protected void createMatrix( int m , int n ) {
        A = RowMajorOps.createSymmetric(m, -1, 1, rand);

        L = new RowMajorMatrix(m,m);
        R = new RowMajorMatrix(m,m);
    }

    @Override
    protected RowMajorMatrix[] createInputs() {
        return new RowMajorMatrix[]{A};
    }

    @Override
    protected int getNumOutputs() {
        return 2;
    }

    @Override
    protected void processResults(RowMajorMatrix[] inputs, RowMajorMatrix[] results) {
        RowMajorMatrix D = results[0];
        RowMajorMatrix V = results[1];

        if(RowMajorOps.hasUncountable(D) ||
                RowMajorOps.hasUncountable(V)) {
            reason = OutputError.UNCOUNTABLE;
            return;
        }

        RowMajorOps.mult(A,V,L);
        RowMajorOps.mult(V,D,R);

        foundResult = StabilityBenchmark.residualError(L,R);
    }

    @Override
    public String getTestName() {
        return "Eigen Value Symmetric Accuracy";
    }

    @Override
    public String getFileName() {
        return "EigSymmAccuracy";
    }

    @Override
    public long getInputMemorySize() {
        return 8*maxLength*maxLength*10;
    }
}

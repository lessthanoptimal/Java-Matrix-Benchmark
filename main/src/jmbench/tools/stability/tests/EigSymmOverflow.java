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


/**
 * @author Peter Abeles
 */
public class EigSymmOverflow extends OverflowTestBase
{
    protected volatile RowMajorMatrix L;
    protected volatile RowMajorMatrix R;

    public EigSymmOverflow(long randomSeed,
                           String classConfigure , String classFactory,
                           String nameOperation, int totalTrials,
                           double breakingPoint, int minLength, int maxLength, boolean overflow) {
        super(randomSeed, classConfigure , classFactory, nameOperation, totalTrials, breakingPoint, minLength, maxLength, overflow);
    }

    public EigSymmOverflow(){}

    @Override
    protected void createMatrix( int m , int n ) {
        A = RowMajorOps.createSymmetric(m, -1, 1, rand);
        Ascaled = new RowMajorMatrix(m,m);

        L = new RowMajorMatrix(m,m);
        R = new RowMajorMatrix(m,m);
    }

    @Override
    protected int getNumOutputs() {
        return 2;
    }

    @Override
    protected boolean checkResults(RowMajorMatrix[] results) {
        RowMajorMatrix D = results[0];
        RowMajorMatrix V = results[1];

        RowMajorOps.mult(Ascaled,V,L);
        RowMajorOps.mult(V,D,R);

        double error = StabilityBenchmark.residualError(L,R);

        if( error > breakingPoint ) {
            reason = OutputError.LARGE_ERROR;
            return false;
        }

        return true;
    }

    @Override
    public String getTestName() {
        if( overflow )
            return "Eigenvalue Symmetric Overflow";
        else
            return "Eigenvalue Symmetric Underflow";
    }

    @Override
    public String getFileName() {
        if( overflow )
            return "EigSymmOverflow";
        else
            return "EigSymmUnderflow";
    }

    @Override
    public long getInputMemorySize() {
        return 8*maxLength*maxLength*10;
    }
}

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
 * Overflow benchmark for inverting symmetric positive definite matrices.
 *
 * @author Peter Abeles
 */
public class InvSymmOverflow extends OverflowTestBase {

    protected volatile RowMajorMatrix I_found;
    protected volatile RowMajorMatrix I;

    public InvSymmOverflow(long randomSeed, Class<LibraryConfigure> classConfigure ,
                           Class<RuntimePerformanceFactory> classFactory, String nameOperation, int totalTrials,
                           double breakingPoint, int minLength, int maxLength, boolean overflow) {
        super(randomSeed, classConfigure , classFactory , nameOperation, totalTrials, breakingPoint, minLength, maxLength, overflow);
    }

    public InvSymmOverflow() {
    }

    @Override
    protected void createMatrix(int m, int n) {
        A = RowMajorOps.createSymmPosDef(m, rand);
        Ascaled = new RowMajorMatrix(m,m);
        I_found = new RowMajorMatrix(m,m);
        I = RowMajorOps.identity(m);
    }

    @Override
    protected int getNumOutputs() {
        return 1;
    }

    @Override
    protected boolean checkResults(RowMajorMatrix[] results) {
        RowMajorMatrix A_inv = results[0];

        RowMajorOps.mult(Ascaled,A_inv,I_found);

        double error = StabilityBenchmark.residualError(I_found,I);

        if( error > breakingPoint ) {
            reason = OutputError.LARGE_ERROR;
            return false;
        }

        return true;
    }

    @Override
    public String getTestName() {
        if( overflow )
            return "Inverse Symmetric Overflow";
        else
            return "Inverse Symmetric Underflow";
    }

    @Override
    public String getFileName() {
        if( overflow )
            return "InvSymmOverflow";
        else
            return "InvSymmUnderflow";
    }

    @Override
    public long getInputMemorySize() {
        return 8*maxLength*maxLength*10;
    }
}

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
import org.ejml.ops.RandomMatrices;


/**
 * @author Peter Abeles
 */
public class EigSymmOverflow extends OverflowTestBase
{
    protected volatile DenseMatrix64F L;
    protected volatile DenseMatrix64F R;

    public EigSymmOverflow(long randomSeed,
                           Class<LibraryConfigure> classConfigure , Class<RuntimePerformanceFactory> factory,
                           String nameOperation, int totalTrials,
                           double breakingPoint, int minLength, int maxLength, boolean overflow) {
        super(randomSeed, classConfigure , factory, nameOperation, totalTrials, breakingPoint, minLength, maxLength, overflow);
    }

    public EigSymmOverflow(){}

    @Override
    protected void createMatrix( int m , int n ) {
        A = RandomMatrices.createSymmetric(m,-1,1,rand);
        Ascaled = new DenseMatrix64F(m,m);

        L = new DenseMatrix64F(m,m);
        R = new DenseMatrix64F(m,m);
    }

    @Override
    protected int getNumOutputs() {
        return 2;
    }

    @Override
    protected boolean checkResults(DenseMatrix64F[] results) {
        DenseMatrix64F D = results[0];
        DenseMatrix64F V = results[1];

        CommonOps.mult(Ascaled,V,L);
        CommonOps.mult(V,D,R);

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

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
import jmbench.tools.stability.StabilityTestBase;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.RandomMatrices;


/**
 * @author Peter Abeles
 */
public abstract class SolverCommon extends StabilityTestBase {

    protected int minLength;
    protected int maxLength;

    protected boolean isLinearSolver;

    protected transient DenseMatrix64F A,b;

    public SolverCommon(long randomSeed,
                        Class<LibraryConfigure> classConfigure ,
                        Class<RuntimePerformanceFactory> classFactory ,
                        String nameOperation,
                        int totalTrials,
                        double breakingPoint ,
                        int minLength, int maxLength,
                        boolean linearSolver) {
        super(randomSeed, classConfigure, classFactory , nameOperation, totalTrials, breakingPoint);
        this.minLength = minLength;
        this.maxLength = maxLength;
        isLinearSolver = linearSolver;
    }

    public SolverCommon() {
    }

    @Override
    public long getInputMemorySize() {
        return 8*maxLength*maxLength*10;
    }

    protected void createMatrix( int m, int n, double svMag ) {
//        System.out.println("Matrix size = ("+m+" , "+n+" )");
        DenseMatrix64F U = RandomMatrices.createOrthogonal(m,m,rand);
        DenseMatrix64F V = RandomMatrices.createOrthogonal(n,n,rand);

        int o = Math.min(m,n);

        double []sv = new double[o];
        for( int i = 0; i < o; i++ )
            sv[i] = svMag;

        A = createMatrix(U,V,sv);
        DenseMatrix64F x = RandomMatrices.createRandom(n,1,rand);
        b = new DenseMatrix64F(m,1);

        // make sure b is reasonable
        CommonOps.mult(A,x,b);
    }

    public static DenseMatrix64F createMatrix( DenseMatrix64F U , DenseMatrix64F V , double []sv ) {
        DenseMatrix64F S = CommonOps.diagR(U.numRows,V.numRows,sv);

        DenseMatrix64F tmp = new DenseMatrix64F(U.numRows,V.numRows);
        CommonOps.mult(U,S,tmp);
        CommonOps.multTransB(tmp,V,S);

        return S;
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

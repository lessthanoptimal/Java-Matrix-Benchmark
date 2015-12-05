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
import jmbench.tools.stability.StabilityTestBase;


/**
 * @author Peter Abeles
 */
public abstract class SolverCommon extends StabilityTestBase {

    protected int minLength;
    protected int maxLength;

    protected boolean isLinearSolver;

    protected transient RowMajorMatrix A,b;

    public SolverCommon(long randomSeed,
                        String classConfigure ,
                        String classFactory ,
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
        RowMajorMatrix U = RowMajorOps.createOrthogonal(m,m,rand);
        RowMajorMatrix V = RowMajorOps.createOrthogonal(n,n,rand);

        int o = Math.min(m,n);

        double []sv = new double[o];
        for( int i = 0; i < o; i++ )
            sv[i] = svMag;

        A = createMatrix(U,V,sv);
        RowMajorMatrix x = RowMajorOps.createRandom(n, 1, rand);
        b = new RowMajorMatrix(m,1);

        // make sure b is reasonable
        RowMajorOps.mult(A,x,b);
    }

    public static RowMajorMatrix createMatrix( RowMajorMatrix U , RowMajorMatrix V , double []sv ) {
        RowMajorMatrix S = RowMajorOps.diagR(U.numRows, V.numRows, sv);

        RowMajorMatrix tmp = new RowMajorMatrix(U.numRows,V.numRows);
        RowMajorOps.mult(U,S,tmp);
        RowMajorOps.multTransB(tmp,V,S);

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

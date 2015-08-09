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

package jmatbench.ejml;

import jmbench.interfaces.BenchmarkMatrix;
import jmbench.matrix.RowMajorMatrix;
import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;


/**
 * @author Peter Abeles
 */
public class EjmlSimpleBenchmarkMatrix implements BenchmarkMatrix {
    SimpleMatrix mat;

    public EjmlSimpleBenchmarkMatrix(SimpleMatrix mat) {
        this.mat = mat;
    }

    public EjmlSimpleBenchmarkMatrix(RowMajorMatrix rm ) {
        DenseMatrix64F mat = new DenseMatrix64F();
        mat.numCols = rm.numCols;
        mat.numRows = rm.numRows;
        mat.data    = rm.data;

        this.mat = SimpleMatrix.wrap(mat);
    }

    @Override
    public double get(int row, int col) {
        return mat.get(row,col);
    }

    @Override
    public void set(int row, int col, double value) {
        mat.set(row,col,value);
    }

    @Override
    public int numRows() {
        return mat.numRows();
    }

    @Override
    public int numCols() {
        return mat.numCols();
    }

    @Override
    public <T> T getOriginal() {
        return (T)mat;
    }
}

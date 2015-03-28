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

package jmbench.impl.wrapper;

import jmbench.interfaces.BenchmarkMatrix;
import org.ujmp.core.Matrix;


/**
 * @author Peter Abeles
 */
public class UjmpBenchmarkMatrix implements BenchmarkMatrix
{
    Matrix mat;

    public UjmpBenchmarkMatrix(Matrix mat) {
        this.mat = mat;
    }

    @Override
    public double get(int row, int col) {
        return mat.getAsDouble(row,col);
    }

    @Override
    public void set(int row, int col, double value) {
        mat.setAsDouble(value,row,col);
    }

    @Override
    public int numRows() {
        return (int)mat.getRowCount();
    }

    @Override
    public int numCols() {
        return (int)mat.getColumnCount();
    }

    @Override
    public <T> T getOriginal() {
        return (T)mat;
    }
}

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

package jmbench.nd4j;

import jmbench.interfaces.BenchmarkMatrix;
import org.nd4j.linalg.api.ndarray.INDArray;


/**
 * @author Peter Abeles
 */
public class Nd4JBenchmarkMatrix implements BenchmarkMatrix {

    INDArray mat;

    public Nd4JBenchmarkMatrix(INDArray mat) {
        this.mat = mat;
    }

    @Override
    public double get(int row, int col) {
        return mat.getDouble(row,col);
    }

    @Override
    public void set(int row, int col, double value) {
        mat.putScalar(row,col,value);
    }

    @Override
    public int numRows() {
        return (int)mat.shape()[0];
    }

    @Override
    public int numCols() {
        return (int)mat.shape()[1];
    }

    @Override
    public <T> T getOriginal() {
        return (T)mat;
    }
}

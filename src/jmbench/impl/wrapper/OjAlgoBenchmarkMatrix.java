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
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;

/**
 * @author Peter Abeles
 */
public class OjAlgoBenchmarkMatrix implements BenchmarkMatrix {

    MatrixStore<?> mat;

    public OjAlgoBenchmarkMatrix(final MatrixStore<?> mat) {

        super();

        this.mat = mat;
    }

    @Override
    public double get(final int row, final int col) {
        return mat.doubleValue(row, col);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOriginal() {
        return (T) mat;
    }

    @Override
    public int numCols() {
        return (int) mat.countColumns();
    }

    @Override
    public int numRows() {
        return (int) mat.countRows();
    }

    @Override
    public void set(final int row, final int col, final double value) {
        ((PhysicalStore<?>) mat).set(row, col, value);
    }
}

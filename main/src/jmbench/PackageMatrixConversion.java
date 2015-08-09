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

package jmbench;

import jmbench.matrix.RowMajorMatrix;

/**
 * @author Peter Abeles
 */
public class PackageMatrixConversion {

    public static double[][] convertToArray2D(final RowMajorMatrix orig) {
        final double[][] mat = new double[orig.numRows][orig.numCols];

        for (int i = 0; i < orig.numRows; i++) {
            for (int j = 0; j < orig.numCols; j++) {
                mat[i][j] = orig.get(i, j);
            }
        }

        return mat;
    }

}

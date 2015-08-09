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

package jmbench.tools.runtime.generator;

import jmbench.matrix.RowMajorMatrix;
import jmbench.matrix.RowMajorOps;
import jmbench.tools.OutputError;


/**
 * @author Peter Abeles
 */
public class ResultsChecking {



    public static OutputError checkResult( RowMajorMatrix found , RowMajorMatrix expected , double tol )
    {
        if( found == null ) {
            return OutputError.MISC;
        }

        // see if the found output has uncountable numbers in it
        if( RowMajorOps.hasUncountable(found)) {
            return OutputError.UNCOUNTABLE;
        }

        RowMajorMatrix residual = new RowMajorMatrix(expected.numRows,expected.numCols);

        RowMajorOps.subtract(found, expected, residual);

        double top = RowMajorOps.normF(residual);
        double bottom = RowMajorOps.normF(expected);

        if( bottom == 0 )
            return OutputError.ZERO_INPUT;

        if( top/bottom > tol ) {
            return OutputError.LARGE_ERROR;
        }

        return OutputError.NO_ERROR;
    }
}

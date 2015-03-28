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

package jmbench.tools.runtime.generator;

import jmbench.tools.OutputError;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;
import org.ejml.ops.MatrixFeatures;
import org.ejml.ops.NormOps;


/**
 * @author Peter Abeles
 */
public class ResultsChecking {



    public static OutputError checkResult( DenseMatrix64F found , DenseMatrix64F expected , double tol )
    {
        if( found == null ) {
            return OutputError.MISC;
        }

        // see if the found output has uncountable numbers in it
        if( MatrixFeatures.hasUncountable(found)) {
            return OutputError.UNCOUNTABLE;
        }

        DenseMatrix64F residual = new DenseMatrix64F(expected.numRows,expected.numCols);


        CommonOps.sub(found,expected,residual);

        double top = NormOps.normF(residual);
        double bottom = NormOps.normF(expected);

        if( bottom == 0 )
            return OutputError.ZERO_INPUT;

        if( top/bottom > tol ) {
            return OutputError.LARGE_ERROR;
        }

        return OutputError.NO_ERROR;
    }
}

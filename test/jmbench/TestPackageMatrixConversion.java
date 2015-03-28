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

import jmbench.impl.runtime.MtjAlgorithmFactory;
import jmbench.impl.runtime.OjAlgoAlgorithmFactory;
import no.uib.cipr.matrix.DenseMatrix;
import org.ejml.data.DenseMatrix64F;
import org.junit.Test;
import org.ojalgo.matrix.store.PhysicalStore;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestPackageMatrixConversion {

    @Test
    public void covertToOjalgo() {
        double[] d = new double[]{5 ,-2 ,-4 ,0.5, 0.1, 91, 8, 66, 1, -2, 10, -4, -0.2, 7, -4, 0.8};

        DenseMatrix64F orig = new DenseMatrix64F(2,8,true,d);
        PhysicalStore algo = OjAlgoAlgorithmFactory.convertToOjAlgo(orig);

        assertEquals(orig.getNumRows(),algo.getRowDim());
        assertEquals(orig.getNumCols(),algo.getColDim());

        for( int y = 0; y < orig.getNumRows(); y++ ) {
            for( int x = 0; x < orig.getNumCols(); x++ ) {
                assertEquals(orig.get(y,x),algo.get(y,x).doubleValue(),1e-10);
            }
        }
    }

    @Test
    public void covertToMtj() {
        double[] d = new double[]{5 ,-2 ,-4 ,0.5, 0.1, 91, 8, 66, 1, -2, 10, -4, -0.2, 7, -4, 0.8};

        DenseMatrix64F orig = new DenseMatrix64F(2,8,true,d);
        DenseMatrix mtj = MtjAlgorithmFactory.convertToMtj(orig);

        assertEquals(orig.getNumRows(),mtj.numRows());
        assertEquals(orig.getNumCols(),mtj.numColumns());

        for( int y = 0; y < orig.getNumRows(); y++ ) {
            for( int x = 0; x < orig.getNumCols(); x++ ) {
                assertEquals(orig.get(y,x),mtj.get(y,x),1e-10);
            }
        }
    }
}

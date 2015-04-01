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

package jmbench.interfaces;

import org.ejml.data.DenseMatrix64F;

import java.io.Serializable;


/**
 * Creates new matrices.
 *
 * @author Peter Abeles
 */
public interface MatrixGenerator extends Serializable {

    /**
     * Creates a new matrix.
     *
     * @param numRows Number of rows in the new matrix.
     * @param numCols Number of columns in the new matrix.
     * @return A new instance of the matrix.
     */
    public DenseMatrix64F createMatrix( int numRows , int numCols );

    /**
     * If a random matrix is created the seed for the RNG should be changed here.
     * 
     * @param randomSeed
     */
    void setSeed(long randomSeed);

    /**
     * How much memory will be needed to create this matrix in bytes.
     */
    public int getMemory( int numRows , int numCols );

}

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
 * <p>
 * An interface implemented for each benchmarked library that is used to measure
 * the library's runtime performance.
 * </p>
 * <p>
 * NOTE: Not all of these operations are currently being benchmarked in the publicly released
 * results.  It still is a good idea to implement them all.
 * </p>
 * @author Peter Abeles
 */
public interface RuntimePerformanceFactory extends LibraryFactory , MatrixFactory , Serializable  {

    /**
     * Cholesky decomposition
     */
    MatrixProcessorInterface chol();

    /**
     * LU decomposition
     */
    MatrixProcessorInterface lu();

    /**
     * Singular Value Decomposition
     */
    MatrixProcessorInterface svd();

    /**
     * QR Decomposition
     */
    MatrixProcessorInterface qr();

    /**
     * Eigenvalue Decomposition
     */
    MatrixProcessorInterface eigSymm();

    // should it test against asymmetric matrices?
//    MatrixProcessorInterface eigASymm();


    /**
     * Computes the determinant of a matrix.
     */
    MatrixProcessorInterface det();

    /**
     * Inverts a square matrix.
     */
    MatrixProcessorInterface invert();

    /**
     * Inverts a square positive definite matrix.
     */
    MatrixProcessorInterface invertSymmPosDef();

    /**
     * <p>
     * Matrix addition :<br>
     * <br>
     * C = A + B
     * </p>
     */
    MatrixProcessorInterface add();

    /**
     * <p>
     * Matrix multiplication :<br>
     * <br>
     * C = A*B
     * </p>
     */
    MatrixProcessorInterface mult();

    /**
     * <p>
     * Matrix multiplication where B is transposed:<br>
     * <br>
     * C = A*B^T
     * </p>
     */
    MatrixProcessorInterface multTransB();

    /**
     * <p>
     * Multiplies each element in the matrix by a constant value.<br>
     * <br>
     * b<sub>i,j</sub> = &gamma;a<sub>i,j</sub>
     * </p>
     */
    MatrixProcessorInterface scale();

    /**
     * Solve a system with square input matrix:<br>
     * <br>
     * A*X = B<br>
     * <br>
     * where A is an m by m matrix.
     */
    MatrixProcessorInterface solveExact();

    /**
     * Solve a system with a "tall" input matrix:<br>
     * <br>
     * A*X = B<br>
     * <br>
     * where A is an m by n matrix and m > n.
     */
    MatrixProcessorInterface solveOver();

    /**
     * Matrix transpose
     */
    MatrixProcessorInterface transpose();

    BenchmarkMatrix convertToLib( DenseMatrix64F input );

    DenseMatrix64F convertToEjml( BenchmarkMatrix input );
}

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

package jmbench.tools.runtime;

import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.MatrixFactory;
import jmbench.tools.OutputError;

import java.util.Random;


/**
 * Each operation that is evaluated in the runtime performance benchmark implements this function.
 * It provides random inputs for each library to process and produces a set of expected outputs.
 *
 * By testing the output it can verify that the algorithms are performing the specified operation
 * correctly.
 *
 * @author Peter Abeles
 */
public interface InputOutputGenerator {

    /**
     * Creates a set of inputs for each library to process.
     *
     * @param factory Used to create input matrices.
     * @param rand Random number generator used to create the matrices.
     * @param checkResults If the results will be checked.  If false less memory is needed.
     * @param size how big the matrices should be.
     *
     * @return List of input matrices.
     */
    public BenchmarkMatrix[] createInputs( MatrixFactory factory , Random rand , 
                                           boolean checkResults , int size );
    
    /**
     * Checks to see if anything is wrong with the computed output.
     *
     * @param output Output from the operation.
     * @param tol Tolerance that is uses in deciding of it is close enough to the expected result.
     * @return
     */
    public OutputError checkResults( BenchmarkMatrix[] output , double tol );


    /**
     * An estimate of how much memory it should take to perform the operation in bytes.
     *
     * @return Amount of memory in bytes.
     */
    public long getRequiredMemory( int matrixSize );

    /**
     * How many matrices can be expected on output
     * @return
     */
    public int numOutputs();
}

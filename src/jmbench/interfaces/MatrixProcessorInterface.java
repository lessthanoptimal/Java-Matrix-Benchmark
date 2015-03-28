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

/**
 * <p>
 * Generic interface for benchmarking elapsed time for a matrix operation.
 * Each operation should be performed 'numTrial' times and the timing is performed inside this function.
 * Inside of each iteration there should be no caching of past results.  Only numerically stable
 * solvers should be used.  The same operations used here will be used in the stability benchmark.
 * </p>
 *
 * <p>
 * Upon exit the input and output matrices are (optionally) checked.  If the output matrices deviate
 * too much from the expected value/properties then a fault is recorded.  If input matrices have been
 * modified then a fault is recorded.
 * </p>
 * 
 * @author Peter Abeles
 */
public interface MatrixProcessorInterface {

    /**
     * Measures the amount of time it takes to complete an operation 'numTrials' times.
     *
     * @param inputs Input matrices. MUST NOT BE MODIFIED.
     * @param outputs Storage array for output matrices.  used to sanity check results.
     *                If null then don't create output.
     * @param numTrials How many times the operation should be performed.
     * @return Elapsed time in nanoseconds.  If a failure was detected and gracefully handled then -1 is returned.
     */
    public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials);

}
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

import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.MatrixFactory;
import jmbench.matrix.RowMajorMatrix;
import jmbench.matrix.RowMajorOps;
import jmbench.tools.OutputError;
import jmbench.tools.runtime.InputOutputGenerator;

import java.util.Random;

import static jmbench.misc.RandomizeMatrices.convertToBm;
import static jmbench.misc.RandomizeMatrices.randomize;


/**
 * @author Peter Abeles
 */
public class SolveOverGenerator implements InputOutputGenerator {
    RowMajorMatrix X;

    @Override
    public BenchmarkMatrix[] createInputs( MatrixFactory factory , Random rand ,
                                           boolean checkResults , int size ) {
        BenchmarkMatrix[] inputs = new  BenchmarkMatrix[2];

        inputs[0] = factory.create(3*size,size);  // A
        inputs[1] = factory.create(3*size,1);     // B

        randomize(inputs[0],-1,1,rand);

        // make sure an exact solution exists
        RowMajorMatrix A = new RowMajorMatrix(inputs[0]);
        RowMajorMatrix B = new RowMajorMatrix(3*size,1);
        RowMajorMatrix X = RowMajorOps.createRandom(size, 1, -1, 1, rand);

        RowMajorOps.mult(A,X,B);

        convertToBm(B,inputs[1]);

        if( checkResults ) {
            this.X = X;
        }

        return inputs;
    }

    @Override
    public OutputError checkResults(BenchmarkMatrix[] output, double tol) {
        RowMajorMatrix o = new RowMajorMatrix(output[0]);

        return ResultsChecking.checkResult(o,X,tol);
    }

    @Override
    public int numOutputs() {
        return 1;
    }

    @Override
    public long getRequiredMemory( int matrixSize ) {
        return 8L*matrixSize*matrixSize*10L;
    }
}
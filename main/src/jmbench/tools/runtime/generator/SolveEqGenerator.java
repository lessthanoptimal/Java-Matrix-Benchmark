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

import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.MatrixFactory;
import jmbench.tools.OutputError;
import jmbench.tools.runtime.InputOutputGenerator;
import jmbench.tools.stability.StabilityBenchmark;
import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;

import static jmbench.misc.RandomizeMatrices.convertToEjml;
import static jmbench.misc.RandomizeMatrices.randomize;


/**
 * @author Peter Abeles
 */
public class SolveEqGenerator implements InputOutputGenerator {

    DenseMatrix64F A;
    DenseMatrix64F B;

    @Override
    public BenchmarkMatrix[] createInputs( MatrixFactory factory , Random rand ,
                                           boolean checkResults , int size ) {
        BenchmarkMatrix[] inputs = new  BenchmarkMatrix[2];

        inputs[0] = factory.create(size,size);
        inputs[1] = factory.create(size,1);

        randomize(inputs[0],-1,1,rand);
        randomize(inputs[1],-1,1,rand);

        if( checkResults ) {
            A = convertToEjml(inputs[0]);
            B = convertToEjml(inputs[1]);
        }

        return inputs;
    }

    @Override
    public OutputError checkResults(BenchmarkMatrix[] output, double tol) {
        if( output[0] == null ) {
            return OutputError.MISC;
        }

        SimpleMatrix X = SimpleMatrix.wrap(convertToEjml(output[0]));

        if( X.hasUncountable() ) {
            return OutputError.UNCOUNTABLE;
        }

        SimpleMatrix B_found = SimpleMatrix.wrap(A).mult(X);


        double error = StabilityBenchmark.residualError(B_found.getMatrix(),B);
        if( error > tol ) {
//            P.print();
            return OutputError.LARGE_ERROR;
        }

        return OutputError.NO_ERROR;
    }

    @Override
    public int numOutputs() {
        return 1;
    }

    @Override
    public long getRequiredMemory( int matrixSize ) {
        return 8L*matrixSize*matrixSize*6L;
    }
}
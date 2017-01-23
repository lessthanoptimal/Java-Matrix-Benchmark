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
import jmbench.tools.runtime.InputOutputGenerator;

import java.util.Random;

import static jmbench.misc.RandomizeMatrices.randomize;


/**
 * @author Peter Abeles
 */
public class QrGenerator implements InputOutputGenerator {

    @Override
    public BenchmarkMatrix[] createInputs(MatrixFactory factory, Random rand,
                                          int size) {
        BenchmarkMatrix[] inputs = new  BenchmarkMatrix[1];

        inputs[0] = factory.create(size,size);

        randomize(inputs[0],-1,1,rand);

        return inputs;
    }

    @Override
    public int numOutputs() {
        return 2;
    }

    @Override
    public long getRequiredMemory( int matrixSize ) {
        return 8L*matrixSize*matrixSize*6L;
    }
}
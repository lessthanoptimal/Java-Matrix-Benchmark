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

package jmbench.misc;

import jmbench.interfaces.MatrixGenerator;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.RandomMatrices;

import java.util.Random;


/**
 * Creates random positive definite symetric square matrices.
 *
 * @author Peter Abeles
 */
public class PosDefSymGenerator implements MatrixGenerator
{
    private Random rand;

    public PosDefSymGenerator( long seed ) {
        rand = new Random(seed);
    }

    public PosDefSymGenerator(){}

    @Override
    public DenseMatrix64F createMatrix(int numRows, int numCols) {
        if( numRows != numCols ) {
            throw new RuntimeException("Must be square");
        }

        return RandomMatrices.createSymmPosDef(numRows,rand);
    }

    @Override
    public void setSeed(long randomSeed) {
        rand.setSeed(randomSeed);
    }

    @Override
    public int getMemory(int numRows, int numCols) {
        return numRows*numRows*8+50;
    }

    public Random getRand() {
        return rand;
    }

    public void setRand(Random rand) {
        this.rand = rand;
    }
}

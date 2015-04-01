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
 * @author Peter Abeles
 */
public class RandomMatrixGenerator implements MatrixGenerator {
    private Random rand;

    private double scaleRows =1;
    private double scaleCols =1;

    public RandomMatrixGenerator( long seed ) {
        rand = new Random(seed);
    }

    public RandomMatrixGenerator( long seed , double scaleRows, double scaleCols) {
        rand = new Random(seed);
        this.scaleRows = scaleRows;
        this.scaleCols = scaleCols;
    }

    public RandomMatrixGenerator(){
        rand = new Random();
    }

    @Override
    public DenseMatrix64F createMatrix(int numRows, int numCols) {
        return RandomMatrices.createRandom((int)(numRows* scaleRows),(int)(numCols* scaleCols),rand);
    }

    @Override
    public void setSeed(long randomSeed) {
        rand.setSeed(randomSeed);
    }

    @Override
    public int getMemory(int numRows, int numCols) {
        return (int)(scaleRows*scaleCols*numRows*numCols*8)+50;
    }

    public Random getRand() {
        return rand;
    }

    public void setRand(Random rand) {
        this.rand = rand;
    }

    public double getScaleRows() {
        return scaleRows;
    }

    public void setScaleRows(double scaleRows) {
        this.scaleRows = scaleRows;
    }

    public double getScaleCols() {
        return scaleCols;
    }

    public void setScaleCols(double scaleCols) {
        this.scaleCols = scaleCols;
    }
}

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

package jmbench.misc;

import jmbench.interfaces.BenchmarkMatrix;
import jmbench.matrix.RowMajorMatrix;
import jmbench.matrix.RowMajorOps;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class RandomizeMatrices {

    public static void randomize( BenchmarkMatrix input , double min , double max , Random rand ) {

        double range = max-min;

        for( int i = 0; i < input.numRows(); i++ ) {
            for( int j = 0; j < input.numCols(); j++ ) {
                input.set(i,j,rand.nextDouble()*range+min);
            }
        }
    }

    public static void symmetric( BenchmarkMatrix input , double min , double max , Random rand ) {

        double range = max-min;

        for( int i = 0; i < input.numRows(); i++ ) {
            for( int j = i; j < input.numCols(); j++ ) {
                double v = rand.nextDouble()*range+min;
                input.set(i,j,v);
                input.set(j,i,v);
            }
        }
    }

    public static void symmPosDef( BenchmarkMatrix input , Random rand ) {

        RowMajorMatrix spd = RowMajorOps.createSymmPosDef(input.numRows(), rand);

        for( int i = 0; i < input.numRows(); i++ ) {
            for( int j = 0; j < input.numCols(); j++ ) {
                input.set(i,j,spd.get(i,j));
            }
        }
    }


    public static void convertToBm( RowMajorMatrix orig , BenchmarkMatrix ret ) {

        for( int i = 0; i < orig.numRows; i++ ) {
            for( int j = 0; j < orig.numCols; j++ ) {
                ret.set(i,j, orig.get(i,j) );
            }
        }
    }
}

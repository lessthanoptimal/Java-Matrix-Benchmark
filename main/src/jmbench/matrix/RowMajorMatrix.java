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

package jmbench.matrix;

import jmbench.interfaces.BenchmarkMatrix;

/**
 * Very simple matrix format.  Single array row-major.
 *
 * @author Peter Abeles
 */
public class RowMajorMatrix {
	public double data[];
	public int numRows,numCols;

	public RowMajorMatrix( int numRows , int numCols ) {
		this.numRows = numRows;
		this.numCols = numCols;
		data = new double[numCols*numRows];
	}

	public RowMajorMatrix( BenchmarkMatrix orig ) {
		this( orig.numRows() , orig.numCols() );
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				data[getIndex(row,col)] = orig.get(row,col);
			}
		}
	}

	public double get( int row , int col ) {
		return data[row*numCols+col];
	}

	public void set( int row , int col , double value ) {
		data[row*numCols+col] = value;
	}

	public double get( int i ) {
		return data[i];
	}

	public int getNumRows() {
		return numRows;
	}

	public int getNumCols() {
		return numCols;
	}

	public int getNumElements() {
		return data.length;
	}

	public void reshape( int numRows , int numCols ) {
		if( data.length < numCols*numRows ) {
			data = new double[ numCols*numRows ];
		}
		this.numRows = numRows;
		this.numCols = numCols;
	}

	public void set(RowMajorMatrix a) {
		reshape(a.numRows,a.numCols);
		System.arraycopy(a.data,0,data,0,a.getNumElements());
	}

	public int getIndex(int row, int col) {
		return row * numCols + col;
	}
	
	public void add(int row, int col, double value) {
		data[row*numCols+col] += value;
	}
}

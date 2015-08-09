package jmbench.matrix;

import jmbench.interfaces.BenchmarkMatrix;

/**
 * @author Peter Abeles
 */
public class RowMajorBenchmarkMatrix implements BenchmarkMatrix {

	RowMajorMatrix matrix;

	public RowMajorBenchmarkMatrix(RowMajorMatrix matrix) {
		this.matrix = matrix;
	}

	@Override
	public double get(int row, int col) {
		return matrix.get(row,col);
	}

	@Override
	public void set(int row, int col, double value) {
		matrix.set(row,col,value);
	}

	@Override
	public int numRows() {
		return matrix.numRows;
	}

	@Override
	public int numCols() {
		return matrix.numCols;
	}

	@Override
	public <T> T getOriginal() {
		return (T)matrix;
	}
}

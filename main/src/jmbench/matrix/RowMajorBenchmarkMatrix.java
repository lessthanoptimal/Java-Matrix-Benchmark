package jmbench.matrix;

import jmbench.interfaces.BenchmarkMatrix;

/**
 * @author Peter Abeles
 */
public class RowMajorBenchmarkMatrix implements BenchmarkMatrix {

    RowMajorMatrix A;

    public RowMajorBenchmarkMatrix(RowMajorMatrix a) {
        A = a;
    }

    @Override
    public double get(int row, int col) {
        return A.get(row,col);
    }

    @Override
    public void set(int row, int col, double value) {
        A.set(row,col,value);
    }

    @Override
    public int numRows() {
        return A.numRows;
    }

    @Override
    public int numCols() {
        return A.numCols;
    }

    @Override
    public <T> T getOriginal() {
        return (T)A;
    }
}

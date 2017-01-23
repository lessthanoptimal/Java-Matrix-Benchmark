package jmatbench.jeigen;

import jeigen.DenseMatrix;
import jmbench.interfaces.BenchmarkMatrix;

/**
 * @author Peter Abeles
 */
public class JeigenBenchmarkMatrix implements BenchmarkMatrix {

    DenseMatrix matrix;

    public JeigenBenchmarkMatrix(DenseMatrix matrix) {
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
        return matrix.rows;
    }

    @Override
    public int numCols() {
        return matrix.cols;
    }

    @Override
    public <T> T getOriginal() {
        return (T)matrix;
    }
}

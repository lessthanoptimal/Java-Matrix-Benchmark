package jmbench.interfaces;

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

	public double get( int row , int col ) {
		return data[row*numCols+col];
	}

	public void set( int row , int col , double value ) {
		data[row*numCols+col] = value;
	}
}

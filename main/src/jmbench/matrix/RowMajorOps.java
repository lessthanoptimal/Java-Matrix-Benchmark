package jmbench.matrix;

import java.util.Arrays;

/**
 * @author Peter Abeles
 */
public class RowMajorOps {
    public static boolean hasUncountable( RowMajorMatrix m )
    {
        int length = m.getNumElements();

        for( int i = 0; i < length; i++ ) {
            double a = m.data[i];
            if( Double.isNaN(a) || Double.isInfinite(a))
                return true;
        }
        return false;
    }

    public static void scale( double alpha , RowMajorMatrix a )
    {
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            a.data[i] *= alpha;
        }
    }

    public static void subtract(RowMajorMatrix a, RowMajorMatrix b, RowMajorMatrix c)
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows ) {
            throw new IllegalArgumentException("The 'a' and 'b' matrices do not have compatible dimensions");
        }

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.data[i] = a.data[i] - b.data[i];
        }
    }

    public static double elementMaxAbs( RowMajorMatrix a ) {
        final int size = a.getNumElements();

        double max = 0;
        for( int i = 0; i < size; i++ ) {
            double val = Math.abs(a.get( i ));
            if( val > max ) {
                max = val;
            }
        }

        return max;
    }

    public static double normF( RowMajorMatrix a ) {
        double total = 0;

        double scale = elementMaxAbs(a);

        if( scale == 0.0 )
            return 0.0;

        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            double val = a.get(i)/scale;
            total += val*val;
        }

        return scale*Math.sqrt(total);
    }

    public static void transpose( RowMajorMatrix A, RowMajorMatrix A_tran)
    {
        int index = 0;
        for( int i = 0; i < A_tran.numRows; i++ ) {
            int index2 = i;

            int end = index + A_tran.numCols;
            while( index < end ) {
                A_tran.data[index++ ] = A.data[ index2 ];
                index2 += A.numCols;
            }
        }
    }

    public static RowMajorMatrix pivotMatrix(RowMajorMatrix ret, int pivots[], int numPivots, boolean transposed ) {

        if( ret == null ) {
            ret = new RowMajorMatrix(numPivots, numPivots);
        } else {
            if( ret.numCols != numPivots || ret.numRows != numPivots )
                throw new IllegalArgumentException("Unexpected matrix dimension");
            RowMajorOps.fill(ret, 0);
        }

        if( transposed ) {
            for( int i = 0; i < numPivots; i++ ) {
                ret.set(pivots[i],i,1);
            }
        } else {
            for( int i = 0; i < numPivots; i++ ) {
                ret.set(i,pivots[i],1);
            }
        }

        return ret;
    }

    public static RowMajorMatrix diagR( int numRows , int numCols , double ...diagEl )
    {
        RowMajorMatrix ret = new RowMajorMatrix(numRows,numCols);

        int o = Math.min(numRows,numCols);

        for( int i = 0; i < o; i++ ) {
            ret.set(i,i,diagEl[i]);
        }

        return ret;
    }

    private static void fill(RowMajorMatrix matrix, double value) {
        Arrays.fill(matrix.data,value);
    }

    public static RowMajorMatrix diag(double[] d) {
        RowMajorMatrix ret = new RowMajorMatrix(d.length,d.length);

        for( int i = 0; i < ret.numCols; i++ ) {
            ret.set(i, i, d[i]);
        }
        return ret;
    }

    public static void mult( RowMajorMatrix a , RowMajorMatrix b , RowMajorMatrix c )
    {
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new RuntimeException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new RuntimeException("The results matrix does not have the desired dimensions");
        }

        if( a.numCols == 0 || a.numRows == 0 ) {
            fill(c, 0);
            return;
        }
        double valA;
        int indexCbase= 0;
        int endOfKLoop = b.numRows*b.numCols;

        for( int i = 0; i < a.numRows; i++ ) {
            int indexA = i*a.numCols;

            // need to assign c.data to a value initially
            int indexB = 0;
            int indexC = indexCbase;
            int end = indexB + b.numCols;

            valA = a.get(indexA++);

            while( indexB < end ) {
                c.data[indexC++] = valA*b.get(indexB++);
            }

            // now add to it
            while( indexB != endOfKLoop ) { // k loop
                indexC = indexCbase;
                end = indexB + b.numCols;

                valA = a.get(indexA++);

                while( indexB < end ) { // j loop
                    c.data[indexC++] = valA*b.get(indexB++);
                }
            }
            indexCbase += c.numCols;
        }
    }
}

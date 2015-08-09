package jmbench.matrix;

import java.util.Arrays;
import java.util.Random;

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

    public static RowMajorMatrix createSymmetric(int m , double min, double max, Random rand) {
        RowMajorMatrix matrix = new RowMajorMatrix(m,m);
        createSymmetric(matrix,min,max,rand);
        return matrix;
    }

    public static void createSymmetric(RowMajorMatrix A, double min, double max, Random rand) {
        if( A.numRows != A.numCols )
            throw new IllegalArgumentException("A must be a square matrix");

        double range = max-min;

        int length = A.numRows;

        for( int i = 0; i < length; i++ ) {
            for( int j = i; j < length; j++ ) {
                double val = rand.nextDouble()*range + min;
                A.set(i,j,val);
                A.set(j,i,val);
            }
        }
    }

    public static void fill(RowMajorMatrix a, double value)
    {
        Arrays.fill(a.data, 0, a.getNumElements(), value);
    }


    public static RowMajorMatrix mult( RowMajorMatrix a , RowMajorMatrix b , RowMajorMatrix c )
    {
        if( c == null ) {
            c = new RowMajorMatrix(a.numRows,b.numCols);
        }
        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numRows ) {
            throw new RuntimeException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numCols != c.numCols ) {
            throw new RuntimeException("The results matrix does not have the desired dimensions");
        }

        if( a.numCols == 0 || a.numRows == 0 ) {
            RowMajorOps.fill(c,0);
            return c;
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
                    c.data[indexC++] += valA*b.get(indexB++);
                }
            }
            indexCbase += c.numCols;
        }

        return c;
    }

    public static RowMajorMatrix multTransB( RowMajorMatrix a , RowMajorMatrix b , RowMajorMatrix c )
    {
        if( c == null ) {
            c = new RowMajorMatrix(a.numRows,b.numRows);
        }

        if( a == c || b == c )
            throw new IllegalArgumentException("Neither 'a' or 'b' can be the same matrix as 'c'");
        else if( a.numCols != b.numCols ) {
            throw new RuntimeException("The 'a' and 'b' matrices do not have compatible dimensions");
        } else if( a.numRows != c.numRows || b.numRows != c.numCols ) {
            throw new RuntimeException("The results matrix does not have the desired dimensions");
        }

        int cIndex = 0;
        int aIndexStart = 0;

        for( int xA = 0; xA < a.numRows; xA++ ) {
            int end = aIndexStart + b.numCols;
            int indexB = 0;
            for( int xB = 0; xB < b.numRows; xB++ ) {
                int indexA = aIndexStart;

                double total = 0;

                while( indexA<end ) {
                    total += a.get(indexA++) * b.get(indexB++);
                }

                c.data[cIndex++] = total;
            }
            aIndexStart += a.numCols;
        }

        return c;
    }

    public static RowMajorMatrix createSymmPosDef(int width, Random rand) {
        // This is not formally proven to work.  It just seems to work.
        RowMajorMatrix a = new RowMajorMatrix(width,1);
        RowMajorMatrix a_tran = new RowMajorMatrix(1,width);
        RowMajorMatrix b = new RowMajorMatrix(width,width);

        for( int i = 0; i < width; i++ ) {
            a.set(i,0,rand.nextDouble());
        }
        a_tran.data = a.data;

        RowMajorOps.mult(a, a_tran, b);

        for( int i = 0; i < width; i++ ) {
            b.data[b.getIndex(i,i)] += 1;
        }

        return b;
    }

    public static RowMajorMatrix identity(int m) {
        RowMajorMatrix I = new RowMajorMatrix(m,m);
        for (int i = 0; i < m; i++) {
            I.data[I.getIndex(i,i)] = 1;
        }
        return I;
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

    public static RowMajorMatrix createRandom(int row, int col, double min , double max, Random rand) {
        RowMajorMatrix m = new RowMajorMatrix(row,col);
        setRandom(m,min,max,rand);
        return m;
    }

    public static RowMajorMatrix createRandom(int row, int col, Random rand) {
        RowMajorMatrix m = new RowMajorMatrix(row,col);
        setRandom(m,-1,1,rand);
        return m;
    }

    public static void setRandom( RowMajorMatrix mat , double min , double max , Random rand )
    {
        double d[] = mat.data;
        int size = mat.getNumElements();

        double r = max-min;

        for( int i = 0; i < size; i++ ) {
            d[i] = r*rand.nextDouble()+min;
        }
    }

    public static RowMajorMatrix createOrthogonal( int numRows , int numCols , Random rand ) {
        if( numRows < numCols ) {
            throw new IllegalArgumentException("The number of rows must be more than or equal to the number of columns");
        }

        RowMajorMatrix u[] = createSpan(numRows, numCols, rand);

        RowMajorMatrix ret = new RowMajorMatrix(numRows,numCols);
        for( int i = 0; i < numCols; i++ ) {
            setSubMatrix(u[i], ret, 0, 0, 0, i, numRows, 1);
        }

        return ret;
    }

    public static RowMajorMatrix[] createSpan( int dimen, int numVectors , Random rand ) {
        if( dimen < numVectors )
            throw new IllegalArgumentException("The number of vectors must be less than or equal to the dimension");

        RowMajorMatrix u[] = new RowMajorMatrix[numVectors];

        u[0] = createRandom(dimen,1,-1,1,rand);
        normalizeF(u[0]);

        for( int i = 1; i < numVectors; i++ ) {
//            System.out.println(" i = "+i);
            RowMajorMatrix a = new RowMajorMatrix(dimen,1);
            RowMajorMatrix r=null;

            for( int j = 0; j < i; j++ ) {
//                System.out.println("j = "+j);
                if( j == 0 )
                    r = createRandom(dimen,1,-1,1,rand);

                // find a vector that is normal to vector j
                // u[i] = (1/2)*(r + Q[j]*r)
                a.set(r);
                householder(-2.0, u[j], r, a);
                RowMajorOps.add(r, a, a);
                RowMajorOps.scale(0.5,a);

//                UtilEjml.print(a);

                RowMajorMatrix t = a;
                a = r;
                r = t;

                // normalize it so it doesn't get too small
                double val = RowMajorOps.normF(r);
                if( val == 0 || Double.isNaN(val) || Double.isInfinite(val))
                    throw new RuntimeException("Failed sanity check");
                RowMajorOps.divide(r,val);
            }

            u[i] = r;
        }

        return u;
    }

    public static void householder( double gamma,
                                    RowMajorMatrix u ,
                                    RowMajorMatrix x , RowMajorMatrix y )
    {
        int n = u.getNumElements();

        double sum = 0;
        for( int i = 0; i < n; i++ ) {
            sum += u.get(i)*x.get(i);
        }
        for( int i = 0; i < n; i++ ) {
            y.data[i] = x.get(i) + gamma*u.get(i)*sum;
        }
    }

    public static void add( final RowMajorMatrix a , final RowMajorMatrix b , final RowMajorMatrix c )
    {
        if( a.numCols != b.numCols || a.numRows != b.numRows
                || a.numCols != c.numCols || a.numRows != c.numRows ) {
            throw new IllegalArgumentException("The matrices are not all the same dimension.");
        }

        final int length = a.getNumElements();

        for( int i = 0; i < length; i++ ) {
            c.data[i] = a.get(i)+b.get(i);
        }
    }

    public static void divide( RowMajorMatrix a , double alpha)
    {
        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            a.data[i] /= alpha;
        }
    }

    public static void normalizeF( RowMajorMatrix A ) {
        double val = normF(A);

        if( val == 0 )
            return;

        int size = A.getNumElements();

        for( int i = 0; i < size; i++) {
            A.data[i] /= val;
        }
    }

    public static void setSubMatrix( RowMajorMatrix src , RowMajorMatrix dst ,
                                     int srcRow , int srcCol , int dstRow , int dstCol ,
                                     int numSubRows, int numSubCols )
    {
        for( int i = 0; i < numSubRows; i++ ) {
            for( int j = 0; j < numSubCols; j++ ) {
                double val = src.get(i+srcRow,j+srcCol);
                dst.set(i+dstRow,j+dstCol,val);
            }
        }
    }

    public static void scale( double alpha , RowMajorMatrix a , RowMajorMatrix b)
    {
        if( a.numRows != b.numRows || a.numCols != b.numCols )
            throw new IllegalArgumentException("Matrices must have the same shape");

        final int size = a.getNumElements();

        for( int i = 0; i < size; i++ ) {
            b.data[i] = a.data[i]*alpha;
        }
    }

    public static RowMajorMatrix transpose( RowMajorMatrix A, RowMajorMatrix A_tran)
    {
        if( A_tran == null ) {
            A_tran = new RowMajorMatrix(A.numCols,A.numRows);
        }
        int index = 0;
        for( int i = 0; i < A_tran.numRows; i++ ) {
            int index2 = i;

            int end = index + A_tran.numCols;
            while( index < end ) {
                A_tran.data[index++ ] = A.data[ index2 ];
                index2 += A.numCols;
            }
        }
        return A_tran;
    }

    public static boolean isInverse( RowMajorMatrix a , RowMajorMatrix b , double tol ) {
        if( a.numRows != b.numRows || a.numCols != b.numCols ) {
            return false;
        }

        int numRows = a.numRows;
        int numCols = a.numCols;

        for( int i = 0; i < numRows; i++ ) {
            for( int j = 0; j < numCols; j++ ) {
                double total = 0;
                for( int k = 0; k < numCols; k++ ) {
                    total += a.get(i,k)*b.get(k,j);
                }

                if( i == j ) {
                    if( !(Math.abs(total-1) <= tol) )
                        return false;
                } else if( !(Math.abs(total) <= tol) )
                    return false;
            }
        }

        return true;
    }

    public static RowMajorMatrix diag( double ...diagEl )
    {
        return diag(null,diagEl.length,diagEl);
    }

    /**
     * @see #diag(double...)
     */
    public static RowMajorMatrix diag( RowMajorMatrix ret , int width , double ...diagEl )
    {
        if( ret == null ) {
            ret = new RowMajorMatrix(width,width);
        } else {
            if( ret.numRows != width || ret.numCols != width )
                throw new IllegalArgumentException("Unexpected matrix size");

            RowMajorOps.fill(ret, 0);
        }

        for( int i = 0; i < width; i++ ) {
            ret.set(i, i, diagEl[i]);
        }

        return ret;
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

}

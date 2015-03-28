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

package jmbench.impl.runtime;

import jmbench.impl.wrapper.CommonsMathBenchmarkMatrix;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.DetectedException;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.runtime.generator.ScaleGenerator;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.linear.*;
import org.ejml.data.DenseMatrix64F;


/**
 * @author Peter Abeles
 */
public class CommonsMathAlgorithmFactory implements RuntimePerformanceFactory {

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        return wrap(MatrixUtils.createRealMatrix(numRows,numCols));
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new CommonsMathBenchmarkMatrix( (RealMatrix)matrix );
    }

    @Override
    public MatrixProcessorInterface chol() {
        return new Chol();
    }

    public static class Chol implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();

            RealMatrix L = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    CholeskyDecomposition chol = new CholeskyDecomposition(matA);
                    L = chol.getL();
                } catch( NonSymmetricMatrixException e ) {
                    throw new DetectedException(e);
                } catch( NonPositiveDefiniteMatrixException e ) {
                    throw new DetectedException(e);
                }
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(L);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface lu() {
        return new LU();
    }

    public static class LU implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();

            RealMatrix L = null;
            RealMatrix U = null;
            RealMatrix P = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                LUDecomposition LU = new LUDecomposition(matA);
                L = LU.getL();
                U = LU.getU();
                P = LU.getP();
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(L);
                outputs[1] = new CommonsMathBenchmarkMatrix(U);
                outputs[2] = new CommonsMathBenchmarkMatrix(P);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface svd() {
        return new SVD();
    }

    public static class SVD implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();

            RealMatrix U = null;
            RealMatrix S = null;
            RealMatrix V = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                SingularValueDecomposition svd = new SingularValueDecomposition(matA);
                // need to call this functions so that it performs the full decomposition
                U = svd.getU();
                S = svd.getS();
                V = svd.getV();
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(U);
                outputs[1] = new CommonsMathBenchmarkMatrix(S);
                outputs[2] = new CommonsMathBenchmarkMatrix(V);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface eigSymm() {
        return new Eig();
    }

    public static class Eig implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();

            RealMatrix V = null;
            RealMatrix D = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    EigenDecomposition eig = new EigenDecomposition(matA);
                    // need to do this so that it computes the complete eigen vector
                    V = eig.getV();
                    D = eig.getD();
                } catch( MaxCountExceededException e ) {
                    throw new DetectedException(e);
                } catch( MathArithmeticException e ) {
                    throw new DetectedException(e);
                }
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(D);
                outputs[1] = new CommonsMathBenchmarkMatrix(V);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface qr() {
        return new QR();
    }

    public static class QR implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();

            RealMatrix Q = null;
            RealMatrix R = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                QRDecomposition decomp = new QRDecomposition(matA);

                Q = decomp.getQ();
                R = decomp.getR();

            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(Q);
                outputs[1] = new CommonsMathBenchmarkMatrix(R);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface det() {
        return new Det();
    }

    public static class Det implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();

            long prev = System.nanoTime();

            // LU decompose is a bit of a mess because of all the depreciated stuff everywhere
            // I believe this is the way the designers want you to do it
            for( long i = 0; i < numTrials; i++ ) {
                LUDecomposition lu = new LUDecomposition(matA);
                lu.getDeterminant();
            }

            return System.nanoTime()-prev;
        }
    }

    @Override
    public MatrixProcessorInterface invert() {
        return new Inv();
    }

    public static class Inv implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();

            RealMatrix result = null;
            long prev = System.nanoTime();

            // LU decompose is a bit of a mess because of all the depreciated stuff everywhere
            // I believe this is the way the designers want you to do it
            for( long i = 0; i < numTrials; i++ ) {
                try {
                    LUDecomposition lu = new LUDecomposition(matA);
                    result = lu.getSolver().getInverse();
                } catch( SingularMatrixException e ) {
                    throw new DetectedException(e);
                }
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface invertSymmPosDef() {
        return new InvSymmPosDef();
    }

    public static class InvSymmPosDef implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();

            RealMatrix result = null;
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    CholeskyDecomposition chol = new CholeskyDecomposition(matA);
                    result = chol.getSolver().getInverse();
                } catch( NonSymmetricMatrixException e ) {
                    throw new DetectedException(e);
                } catch( NonPositiveDefiniteMatrixException e ) {
                    throw new DetectedException(e);
                } catch( SingularMatrixException e ) {
                    throw new DetectedException(e);
                }
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface add() {
        return new Add();
    }

    public static class Add implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();
            RealMatrix matB = inputs[1].getOriginal();

            RealMatrix result = null;
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.add(matB);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface mult() {
        return new Mult();
    }

    public static class Mult implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();
            RealMatrix matB = inputs[1].getOriginal();

            RealMatrix result = null;
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.multiply(matB);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface multTransB() {
        return new MulTranB();
    }

    public static class MulTranB implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();
            RealMatrix matB = inputs[1].getOriginal();

            RealMatrix result = null;
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.multiply(matB.transpose());
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface scale() {
        return new Scale();
    }

    public static class Scale implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();

            RealMatrix result = null;
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.scalarMultiply(ScaleGenerator.SCALE);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface solveExact() {
        return new SolveExact();
    }

    @Override
    public MatrixProcessorInterface solveOver() {
        return new SolveOver();
    }

    public static class SolveExact implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();
            RealMatrix matB = inputs[1].getOriginal();

            RealMatrix result = null;
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                LUDecomposition lu = new LUDecomposition(matA);
                result = lu.getSolver().solve(matB);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    public static class SolveOver implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();
            RealMatrix matB = inputs[1].getOriginal();

            RealMatrix result = null;
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                QRDecomposition qr = new QRDecomposition(matA);
                result = qr.getSolver().solve(matB);
            }
            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface transpose() {
        return new Transpose();
    }

    public static class Transpose implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            RealMatrix matA = inputs[0].getOriginal();

            RealMatrix result = null;
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.transpose();
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new CommonsMathBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public BenchmarkMatrix convertToLib(DenseMatrix64F input) {
        return new CommonsMathBenchmarkMatrix(convertToBlockReal(input));
    }

    @Override
    public DenseMatrix64F convertToEjml(BenchmarkMatrix input) {
        RealMatrix mat = input.getOriginal();
        return realToEjml(mat);
    }

    /**
     * Converts BenchmarkMatrix used in EML into a RealMatrix found in commons-math.
     *
     * @param orig A BenchmarkMatrix in EML
     * @return A RealMatrix in CommonsMath
     */
    public static BlockRealMatrix convertToBlockReal( DenseMatrix64F orig )
    {
        double [][]mat = new double[ orig.numRows ][ orig.numCols ];

        for( int i = 0; i < orig.numRows; i++ ) {
            for( int j = 0; j < orig.numCols; j++ ) {
                mat[i][j] = orig.get(i,j);
            }
        }

        return new BlockRealMatrix(mat);
    }

    /**
     * Uses MatrixUtils.createRealMatrix() to declare the matrix.  This function
     * creates a different matrix depending on size.
     *
     * @param orig A BenchmarkMatrix in EML
     * @return A RealMatrix in CommonsMath
     */
    public static RealMatrix convertToReal( DenseMatrix64F orig )
    {
        double [][]mat = new double[ orig.numRows ][ orig.numCols ];

        for( int i = 0; i < orig.numRows; i++ ) {
            for( int j = 0; j < orig.numCols; j++ ) {
                mat[i][j] = orig.get(i,j);
            }
        }

        return MatrixUtils.createRealMatrix(mat);
    }

    public static DenseMatrix64F realToEjml( RealMatrix orig )
    {
        if( orig == null )
            return null;

        DenseMatrix64F ret = new DenseMatrix64F(orig.getRowDimension(),orig.getColumnDimension());

        for( int i = 0; i < ret.numRows; i++ ) {
            for( int j = 0; j < ret.numCols; j++ ) {
                ret.set(i,j, orig.getEntry(i,j));
            }
        }

        return ret;
    }
}
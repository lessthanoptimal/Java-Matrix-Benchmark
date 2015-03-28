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

import jmbench.impl.wrapper.EjmlBenchmarkMatrix;
import jmbench.impl.wrapper.MtjBenchmarkMatrix;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.DetectedException;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.runtime.generator.ScaleGenerator;
import no.uib.cipr.matrix.*;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;


/**
 * @author Peter Abeles
 */
public class MtjAlgorithmFactory implements RuntimePerformanceFactory {

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        return wrap(new DenseMatrix(numRows,numCols));
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new MtjBenchmarkMatrix((DenseMatrix)matrix);
    }

    @Override
    public MatrixProcessorInterface chol() {
        return new Chol();
    }

    public static class Chol implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix matA = inputs[0].getOriginal();

            DenseCholesky cholesky = new DenseCholesky(matA.numRows(),false);
            LowerSPDDenseMatrix uspd = new LowerSPDDenseMatrix(matA);

            LowerTriangDenseMatrix L = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                // the input matrix is over written
                uspd.set(matA);
                if( !cholesky.factor(uspd).isSPD() ) {
                    throw new DetectedException("Is not SPD");
                }

                L = cholesky.getL();
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new MtjBenchmarkMatrix(L);
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
            DenseMatrix matA = inputs[0].getOriginal();

            DenseLU lu = new DenseLU(matA.numRows(),matA.numColumns());
            DenseMatrix tmp = new DenseMatrix(matA);

            LowerTriangDenseMatrix L = null;
            UpperTriangDenseMatrix U = null;
            int pivots[] = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                // the input matrix is over written
                tmp.set(matA);
                lu.factor(tmp);

                L = lu.getL();
                U = lu.getU();
                pivots = lu.getPivots();
            }

            long elapsedTime = System.nanoTime()-prev;

            if( outputs != null ) {
                // I believe that MTJ is generating some buggy row pivots since they go outside
                // the matrix bounds

                outputs[0] = new MtjBenchmarkMatrix(L);
                outputs[1] = new MtjBenchmarkMatrix(U);
//            outputs[2] = SpecializedOps.pivotMatrix(null, pivots, pivots.length);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface svd() {
        return new MySVD();
    }

    public static class MySVD implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix matA = inputs[0].getOriginal();

            no.uib.cipr.matrix.SVD svd = new no.uib.cipr.matrix.SVD(matA.numRows(),matA.numColumns());
            DenseMatrix tmp = new DenseMatrix(matA);

            DenseMatrix U = null;
            double[] S = null;
            DenseMatrix Vt = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    // the input matrix is over written
                    tmp.set(matA);
                    SVD s = svd.factor(tmp);
                    U = s.getU();
                    S = s.getS();
                    Vt = s.getVt();
                } catch (NotConvergedException e) {
                    throw new RuntimeException(e);
                }
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                int m = matA.numRows();
                int n = matA.numColumns();

                outputs[0] = new MtjBenchmarkMatrix(U);
                outputs[1] = new EjmlBenchmarkMatrix(CommonOps.diagR(m, n, S));
                outputs[2] = new MtjBenchmarkMatrix(Vt.transpose());
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
            DenseMatrix matA = inputs[0].getOriginal();

            DenseMatrix V = null;
            double []D = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                try {
                    // the input matrix is over written
                    SymmDenseEVD e = SymmDenseEVD.factorize(matA);
                    V = e.getEigenvectors();
                    D = e.getEigenvalues();
                } catch (NotConvergedException e) {
                    throw new RuntimeException(e);
                }
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(CommonOps.diag(D));
                outputs[1] = new MtjBenchmarkMatrix(V);
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
            DenseMatrix matA = inputs[0].getOriginal();

            no.uib.cipr.matrix.QR qr = new no.uib.cipr.matrix.QR(matA.numRows(),matA.numColumns());
            DenseMatrix tmp = new DenseMatrix(matA);

            DenseMatrix Q = null;
            UpperTriangDenseMatrix R = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                // the input matrix is over written
                tmp.set(matA);
                qr.factor(tmp);

                Q = qr.getQ();
                R = qr.getR();
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new MtjBenchmarkMatrix(Q);
                outputs[1] = new MtjBenchmarkMatrix(R);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface det() {
        return null;
    }

    @Override
    public MatrixProcessorInterface invert() {
        return new Inv();
    }

    public static class Inv implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix matA = inputs[0].getOriginal();

            DenseMatrix I = Matrices.identity(matA.numColumns());
            DenseMatrix inv = new DenseMatrix(matA.numColumns(),matA.numColumns());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.solve(I,inv);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new MtjBenchmarkMatrix(inv);
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
            DenseMatrix matA = inputs[0].getOriginal();

            DenseCholesky cholesky = new DenseCholesky(matA.numRows(),false);
            LowerSPDDenseMatrix uspd = new LowerSPDDenseMatrix(matA);

            DenseMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                // the input matrix is over written
                uspd.set(matA);
                if( !cholesky.factor(uspd).isSPD() ) {
                    throw new RuntimeException("Is not SPD");
                }

                result = cholesky.solve(Matrices.identity(matA.numColumns()));
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new MtjBenchmarkMatrix(result);
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
            DenseMatrix matA = inputs[0].getOriginal();
            DenseMatrix matB = inputs[1].getOriginal();

            DenseMatrix result = new DenseMatrix(matA.numRows(),matB.numColumns());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                // in-place operator
                result.set(matA);
                result.add(matB);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new MtjBenchmarkMatrix(result);
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
            DenseMatrix matA = inputs[0].getOriginal();
            DenseMatrix matB = inputs[1].getOriginal();

            DenseMatrix result = new DenseMatrix(matA.numRows(),matB.numColumns());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.mult(matB,result);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new MtjBenchmarkMatrix(result);
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
            DenseMatrix matA = inputs[0].getOriginal();
            DenseMatrix matB = inputs[1].getOriginal();

            DenseMatrix result = new DenseMatrix(matA.numColumns(),matB.numColumns());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.transBmult(matB,result);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new MtjBenchmarkMatrix(result);
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
            DenseMatrix matA = inputs[0].getOriginal();

            DenseMatrix mod = new DenseMatrix(matA.numRows(),matA.numColumns());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                // in-place operator
                mod.set(matA);
                mod.scale(ScaleGenerator.SCALE);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new MtjBenchmarkMatrix(mod);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface solveExact() {
        return new Solve();
    }

    @Override
    public MatrixProcessorInterface solveOver() {
        return new Solve();
    }

    public static class Solve implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix matA = inputs[0].getOriginal();
            DenseMatrix matB = inputs[1].getOriginal();

            DenseMatrix result = new DenseMatrix(matA.numColumns(),matB.numColumns());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.solve(matB,result);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new MtjBenchmarkMatrix(result);
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
            DenseMatrix matA = inputs[0].getOriginal();
            DenseMatrix result = new DenseMatrix(matA.numColumns(),matA.numRows());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.transpose(result);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new MtjBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public BenchmarkMatrix convertToLib(DenseMatrix64F input) {
        return new MtjBenchmarkMatrix(convertToMtj(input));
    }

    @Override
    public DenseMatrix64F convertToEjml(BenchmarkMatrix input) {
        if( input.getOriginal() instanceof DenseMatrix64F ) {
            return (DenseMatrix64F)input.getOriginal();
        } else {
            DenseMatrix orig = input.getOriginal();
            return mtjToEjml(orig);
        }
    }

    /**
     * Converts a BenchmarkMatrix in EML into a DenseMatrix in MTJ
     *
     * @param orig A BenchmarkMatrix in EML
     * @return A DenseMatrix in MTJ
     */
    public static DenseMatrix convertToMtj( DenseMatrix64F orig )
    {
        DenseMatrix ret = new DenseMatrix(orig.getNumRows(),orig.getNumCols());

        // MTJ's format is the transpose of this format
        DenseMatrix64F temp = new DenseMatrix64F();
        temp.numRows = orig.numCols;
        temp.numCols = orig.numRows;
        temp.data = ret.getData();

        CommonOps.transpose(orig,temp);

        return ret;
    }

    public static DenseMatrix64F mtjToEjml( AbstractMatrix orig )
    {
        if( orig == null )
            return null;

        DenseMatrix64F ret = new DenseMatrix64F(orig.numRows(),orig.numColumns());

        for( int i = 0; i < ret.numRows; i++ ) {
            for( int j = 0; j < ret.numCols; j++ ) {
                ret.set(i,j,orig.get(i,j));
            }
        }

        return ret;
    }
}
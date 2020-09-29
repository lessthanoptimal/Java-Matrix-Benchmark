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

package jmbench.nd4j;

import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.DetectedException;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.matrix.RowMajorBenchmarkMatrix;
import jmbench.matrix.RowMajorMatrix;
import jmbench.matrix.RowMajorOps;
import jmbench.tools.BenchmarkConstants;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.transforms.Cholesky;
import org.nd4j.linalg.factory.Nd4j;


/**
 * @author Peter Abeles
 */
public class Nd4JAlgorithmFactory implements RuntimePerformanceFactory {

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        return wrap(Nd4j.zeros(numRows, numCols));
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new Nd4JBenchmarkMatrix((INDArray)matrix);
    }

    @Override
    public MatrixProcessorInterface chol() {
        return new Chol();
    }

    public static class Chol implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            INDArray matA = inputs[0].getOriginal();

            Cholesky cholesky = new Cholesky(matA);
            LowerSPDINDArray uspd = new LowerSPDINDArray(matA);

            INDArray L = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                cholesky.
                // the input matrix is over written
                uspd.set(matA);
                if( !cholesky.factor(uspd).isSPD() ) {
                    throw new DetectedException("Is not SPD");
                }

                L = cholesky.getL();
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new Nd4JBenchmarkMatrix(L);
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
            INDArray matA = inputs[0].getOriginal();

            DenseLU lu = new DenseLU(matA.numRows(),matA.numColumns());
            INDArray tmp = new INDArray(matA);

            INDArray L = null;
            INDArray U = null;
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

                outputs[0] = new Nd4JBenchmarkMatrix(L);
                outputs[1] = new Nd4JBenchmarkMatrix(U);
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
            INDArray matA = inputs[0].getOriginal();

            no.uib.cipr.matrix.SVD svd = new no.uib.cipr.matrix.SVD(matA.numRows(),matA.numColumns());
            INDArray tmp = new INDArray(matA);

            INDArray U = null;
            double[] S = null;
            INDArray Vt = null;

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

                outputs[0] = new Nd4JBenchmarkMatrix(U);
                outputs[1] = new RowMajorBenchmarkMatrix(RowMajorOps.diagR(m, n, S));
                outputs[2] = new Nd4JBenchmarkMatrix(Vt.transpose());
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
            INDArray matA = inputs[0].getOriginal();

            INDArray V = null;
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
                outputs[0] = new RowMajorBenchmarkMatrix(RowMajorOps.diag(D));
                outputs[1] = new Nd4JBenchmarkMatrix(V);
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
            INDArray matA = inputs[0].getOriginal();

            no.uib.cipr.matrix.QR qr = new no.uib.cipr.matrix.QR(matA.numRows(),matA.numColumns());
            INDArray tmp = new INDArray(matA);

            INDArray Q = null;
            INDArray R = null;

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
                outputs[0] = new Nd4JBenchmarkMatrix(Q);
                outputs[1] = new Nd4JBenchmarkMatrix(R);
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
            INDArray matA = inputs[0].getOriginal();

            INDArray I = Matrices.identity(matA.numColumns());
            INDArray inv = new INDArray(matA.numColumns(),matA.numColumns());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.solve(I,inv);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new Nd4JBenchmarkMatrix(inv);
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
            INDArray matA = inputs[0].getOriginal();

            DenseCholesky cholesky = new DenseCholesky(matA.numRows(),false);
            LowerSPDINDArray uspd = new LowerSPDINDArray(matA);

            INDArray result = null;

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
                outputs[0] = new Nd4JBenchmarkMatrix(result);
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
            INDArray matA = inputs[0].getOriginal();
            INDArray matB = inputs[1].getOriginal();

            INDArray result = null;

            long prev = System.nanoTime();
            for( long i = 0; i < numTrials; i++ ) {
                result = matA.add(matB);
            }
            long elapsedTime = System.nanoTime()-prev;

            if( outputs != null ) {
                outputs[0] = new Nd4JBenchmarkMatrix(result);
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
            INDArray matA = inputs[0].getOriginal();
            INDArray matB = inputs[1].getOriginal();

            INDArray result = null;

            long prev = System.nanoTime();
            for( long i = 0; i < numTrials; i++ ) {
                result = matA.mmul(matB);
            }
            long elapsedTime = System.nanoTime()-prev;

            if( outputs != null ) {
                outputs[0] = new Nd4JBenchmarkMatrix(result);
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
            INDArray matA = inputs[0].getOriginal();
            INDArray matB = inputs[1].getOriginal();

            INDArray result = null;

            long prev = System.nanoTime();
            for( long i = 0; i < numTrials; i++ ) {
                result = matA.mmul(matB);
            }
            long elapsedTime = System.nanoTime()-prev;
            
            if( outputs != null ) {
                outputs[0] = new Nd4JBenchmarkMatrix(result);
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
            INDArray matA = inputs[0].getOriginal();

            INDArray mod = null;

            long prev = System.nanoTime();
            for( long i = 0; i < numTrials; i++ ) {
                mod = matA.mul(BenchmarkConstants.SCALE);
            }
            long elapsedTime = System.nanoTime()-prev;
            
            if( outputs != null ) {
                outputs[0] = new Nd4JBenchmarkMatrix(mod);
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
            INDArray matA = inputs[0].getOriginal();
            INDArray matB = inputs[1].getOriginal();

            INDArray result = new INDArray(matA.numColumns(),matB.numColumns());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.solve(matB,result);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new Nd4JBenchmarkMatrix(result);
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
            INDArray matA = inputs[0].getOriginal();
            INDArray result = null;

            long prev = System.nanoTime();
            for( long i = 0; i < numTrials; i++ ) {
                result = matA.transpose();
            }
            long elapsedTime = System.nanoTime()-prev;

            if( outputs != null ) {
                outputs[0] = new Nd4JBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public BenchmarkMatrix convertToLib(RowMajorMatrix input) {
        return new Nd4JBenchmarkMatrix(convertToND4J(input));
    }

    @Override
    public RowMajorMatrix convertToRowMajor(BenchmarkMatrix input) {
        INDArray orig = input.getOriginal();
        return nd4jToRowMajor(orig);
    }

    @Override
    public String getLibraryVersion() {
        return "1.0.2";
    }

    @Override
    public boolean isNative() {
        return false;
    }

    /**
     * Converts a BenchmarkMatrix in EML into a INDArray in MTJ
     *
     * @param orig A BenchmarkMatrix in EML
     * @return A INDArray in MTJ
     */
    public static INDArray convertToND4J(RowMajorMatrix orig )
    {
        INDArray ret = Nd4j.zeros(orig.getNumRows(), orig.getNumCols());

        for (int row = 0; row < orig.numRows; row++) {
            for (int col = 0; col < orig.numCols; col++) {
                ret.putScalar(row,col, orig.get(row,col));
            }
        }

        return ret;
    }

    public static RowMajorMatrix nd4jToRowMajor(INDArray orig)
    {
        if( orig == null )
            return null;

        RowMajorMatrix ret = new RowMajorMatrix((int)orig.shape()[0],(int)orig.shape()[1]);

        for( int i = 0; i < ret.numRows; i++ ) {
            for( int j = 0; j < ret.numCols; j++ ) {
                ret.set(i,j,orig.getDouble(i,j));
            }
        }

        return ret;
    }
}
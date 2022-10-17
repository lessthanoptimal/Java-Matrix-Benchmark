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

package jmatbench.hipparchus;

import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.DetectedException;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.matrix.RowMajorMatrix;
import jmbench.tools.BenchmarkConstants;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.linear.*;


public class HipparchusAlgorithmFactory implements RuntimePerformanceFactory {

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        return wrap(MatrixUtils.createRealMatrix(numRows, numCols));
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new HipparchusBenchmarkMatrix((RealMatrix) matrix);
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

            for (long i = 0; i < numTrials; i++) {
                try {
                    CholeskyDecomposition chol = new CholeskyDecomposition(matA);
                    L = chol.getL();
                } catch (MathIllegalArgumentException e) {
                    throw new DetectedException(e);
                }
            }

            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(L);
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

            for (long i = 0; i < numTrials; i++) {
                LUDecomposition LU = new LUDecomposition(matA);
                L = LU.getL();
                U = LU.getU();
                P = LU.getP();
            }

            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(L);
                outputs[1] = new HipparchusBenchmarkMatrix(U);
                outputs[2] = new HipparchusBenchmarkMatrix(P);
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

            for (long i = 0; i < numTrials; i++) {
                SingularValueDecomposition svd = new SingularValueDecomposition(matA);
                // need to call this functions so that it performs the full decomposition
                U = svd.getU();
                S = svd.getS();
                V = svd.getV();
            }

            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(U);
                outputs[1] = new HipparchusBenchmarkMatrix(S);
                outputs[2] = new HipparchusBenchmarkMatrix(V);
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

            for (long i = 0; i < numTrials; i++) {
                EigenDecomposition eig = new EigenDecomposition(matA);
                // need to do this so that it computes the complete eigen vector
                V = eig.getV();
                D = eig.getD();
            }

            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(D);
                outputs[1] = new HipparchusBenchmarkMatrix(V);
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

            for (long i = 0; i < numTrials; i++) {
                QRDecomposition decomp = new QRDecomposition(matA);

                Q = decomp.getQ();
                R = decomp.getR();

            }

            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(Q);
                outputs[1] = new HipparchusBenchmarkMatrix(R);
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
            for (long i = 0; i < numTrials; i++) {
                LUDecomposition lu = new LUDecomposition(matA);
                lu.getDeterminant();
            }

            return System.nanoTime() - prev;
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
            for (long i = 0; i < numTrials; i++) {
                LUDecomposition lu = new LUDecomposition(matA);
                result = lu.getSolver().getInverse();
            }

            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(result);
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

            for (long i = 0; i < numTrials; i++) {
                CholeskyDecomposition chol = new CholeskyDecomposition(matA);
                result = chol.getSolver().getInverse();
            }

            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(result);
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

            for (long i = 0; i < numTrials; i++) {
                result = matA.add(matB);
            }

            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(result);
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

            for (long i = 0; i < numTrials; i++) {
                result = matA.multiply(matB);
            }

            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(result);
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

            for (long i = 0; i < numTrials; i++) {
                result = matA.multiply(matB.transpose());
            }

            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(result);
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

            for (long i = 0; i < numTrials; i++) {
                result = matA.scalarMultiply(BenchmarkConstants.SCALE);
            }

            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(result);
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

            for (long i = 0; i < numTrials; i++) {
                LUDecomposition lu = new LUDecomposition(matA);
                result = lu.getSolver().solve(matB);
            }

            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(result);
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

            for (long i = 0; i < numTrials; i++) {
                QRDecomposition qr = new QRDecomposition(matA);
                result = qr.getSolver().solve(matB);
            }
            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(result);
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

            for (long i = 0; i < numTrials; i++) {
                result = matA.transpose();
            }

            long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new HipparchusBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public BenchmarkMatrix convertToLib(RowMajorMatrix input) {
        return new HipparchusBenchmarkMatrix(convertToBlockReal(input));
    }

    @Override
    public RowMajorMatrix convertToRowMajor(BenchmarkMatrix input) {
        RealMatrix mat = input.getOriginal();
        return realToCommon(mat);
    }

    @Override
    public String getLibraryVersion() {
        return "3.2";
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public String getSourceHash() {
        return "";
    }

    /**
     * Converts BenchmarkMatrix used in EML into a RealMatrix found in commons-math.
     *
     * @param orig A BenchmarkMatrix in EML
     * @return A RealMatrix in CommonsMath
     */
    public static BlockRealMatrix convertToBlockReal(RowMajorMatrix orig) {
        double[][] mat = new double[orig.numRows][orig.numCols];

        for (int i = 0; i < orig.numRows; i++) {
            for (int j = 0; j < orig.numCols; j++) {
                mat[i][j] = orig.get(i, j);
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
    public static RealMatrix convertToReal(RowMajorMatrix orig) {
        double[][] mat = new double[orig.numRows][orig.numCols];

        for (int i = 0; i < orig.numRows; i++) {
            for (int j = 0; j < orig.numCols; j++) {
                mat[i][j] = orig.get(i, j);
            }
        }

        return MatrixUtils.createRealMatrix(mat);
    }

    public static RowMajorMatrix realToCommon(RealMatrix orig) {
        if (orig == null)
            return null;

        RowMajorMatrix ret = new RowMajorMatrix(orig.getRowDimension(), orig.getColumnDimension());

        for (int i = 0; i < ret.numRows; i++) {
            for (int j = 0; j < ret.numCols; j++) {
                ret.set(i, j, orig.getEntry(i, j));
            }
        }

        return ret;
    }
}
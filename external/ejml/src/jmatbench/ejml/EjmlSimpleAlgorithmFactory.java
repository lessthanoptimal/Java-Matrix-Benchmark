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

package jmatbench.ejml;

import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.matrix.RowMajorMatrix;
import jmbench.tools.BenchmarkConstants;
import org.ejml.UtilEjml;
import org.ejml.dense.row.EigenOps_DDRM;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;


/**
 * @author Peter Abeles
 */
public class EjmlSimpleAlgorithmFactory implements RuntimePerformanceFactory {

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        return wrap( new SimpleMatrix(numRows,numCols));
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new EjmlSimpleBenchmarkMatrix((SimpleMatrix)matrix);
    }

    @Override
    public MatrixProcessorInterface chol() {
        return null;
    }

    @Override
    public MatrixProcessorInterface lu() {
        return null;
    }

    @Override
    public MatrixProcessorInterface svd() {
        return new SVD();
    }

    public static class SVD implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();

            SimpleMatrix U=null,W=null,V=null;
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                SimpleSVD<SimpleMatrix> s = matA.svd();
                U=s.getU();
                W=s.getW();
                V=s.getV();
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlSimpleBenchmarkMatrix(U);
                outputs[1] = new EjmlSimpleBenchmarkMatrix(W);
                outputs[2] = new EjmlSimpleBenchmarkMatrix(V);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface eigSymm() {
        return new MyEig();
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public static class MyEig implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();

            SimpleEVD evd = null;
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                evd = matA.eig();
                evd.getEigenvalue(0);
                evd.getEigenVector(0);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(EigenOps_DDRM.createMatrixD((EigenDecomposition_F64)evd.getEVD()));
                outputs[1] = new EjmlBenchmarkMatrix(EigenOps_DDRM.createMatrixV((EigenDecomposition_F64)evd.getEVD()));
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface qr() {
        return null;
    }

    @Override
    public MatrixProcessorInterface det() {
        return new Det();
    }

    public static class Det implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.determinant();
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
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.invert();
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new EjmlSimpleBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface invertSymmPosDef() {
        return null;
    }

    @Override
    public MatrixProcessorInterface add() {
        return new Add();
    }

    public static class Add implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix matB = inputs[1].getOriginal();
            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.plus(matB);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new EjmlSimpleBenchmarkMatrix(result);
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
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix matB = inputs[1].getOriginal();
            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                 result = matA.mult(matB);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new EjmlSimpleBenchmarkMatrix(result);
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
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix matB = inputs[1].getOriginal();
            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.mult(matB.transpose());
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new EjmlSimpleBenchmarkMatrix(result);
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
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.scale(BenchmarkConstants.SCALE);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new EjmlSimpleBenchmarkMatrix(result);
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
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix matB = inputs[1].getOriginal();

            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.solve(matB);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new EjmlSimpleBenchmarkMatrix(result);
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
            SimpleMatrix matA = inputs[0].getOriginal();
            SimpleMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.transpose();
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new EjmlSimpleBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public BenchmarkMatrix convertToLib(RowMajorMatrix input) {
        return new EjmlBenchmarkMatrix(input);
    }

    @Override
    public RowMajorMatrix convertToRowMajor(BenchmarkMatrix input) {
        SimpleMatrix m = input.getOriginal();

        RowMajorMatrix out = new RowMajorMatrix(1,1);
        out.data    = m.getDDRM().data;
        out.numCols = m.getDDRM().numCols;
        out.numRows = m.getDDRM().numRows;

        return out;
    }

    @Override
    public String getLibraryVersion() {
        return UtilEjml.VERSION;
    }

    @Override
    public boolean isNative() {
        return false;
    }
}
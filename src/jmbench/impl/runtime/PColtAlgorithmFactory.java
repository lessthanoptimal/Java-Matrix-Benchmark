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

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.decomposition.*;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import jmbench.impl.wrapper.EjmlBenchmarkMatrix;
import jmbench.impl.wrapper.PColtBenchmarkMatrix;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.DetectedException;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.runtime.generator.ScaleGenerator;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.SpecializedOps;


/**
 * @author Peter Abeles
 */
public class PColtAlgorithmFactory implements RuntimePerformanceFactory {

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        return wrap(new DenseDoubleMatrix2D(numRows,numCols));
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new PColtBenchmarkMatrix((DoubleMatrix2D)matrix);
    }

    @Override
    public MatrixProcessorInterface chol() {
        return new Chol();
    }

    // DenseDoubleAlgebra
    public static class Chol implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DoubleMatrix2D matA = inputs[0].getOriginal();

            DenseDoubleAlgebra alg = new DenseDoubleAlgebra();

            DoubleMatrix2D L = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                // can't decompose a matrix with the same decomposition algorithm
                DenseDoubleCholeskyDecomposition chol = alg.chol(matA);

                L = chol.getL();
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new PColtBenchmarkMatrix(L);
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
            DoubleMatrix2D matA = inputs[0].getOriginal();

            // the recommended way I think would be using Algebra, but this might allow
            // reuse of data
            DenseDoubleLUDecompositionQuick decomp = new DenseDoubleLUDecompositionQuick();
            DoubleMatrix2D tmp = createMatrix(matA.rows(),matA.columns());

            DoubleMatrix2D L = null;
            DoubleMatrix2D U = null;
            int[] pivot = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                // input matrix is overwritten
                tmp.assign(matA);
                decomp.decompose(tmp);
                if( !decomp.isNonsingular() )
                    throw new DetectedException("LU decomposition failed");

                L = decomp.getL();
                U = decomp.getU();
                pivot = decomp.getPivot();
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new PColtBenchmarkMatrix(L);
                outputs[1] = new PColtBenchmarkMatrix(U);
                outputs[2] = new EjmlBenchmarkMatrix(SpecializedOps.pivotMatrix(null, pivot, pivot.length, false));
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
            DoubleMatrix2D matA = inputs[0].getOriginal();

            DenseDoubleAlgebra alg = new DenseDoubleAlgebra();

            DoubleMatrix2D U = null;
            DoubleMatrix2D S = null;
            DoubleMatrix2D V = null;

            long prev = System.nanoTime();

            // There are two SVD algorithms. Piotr Wendykier said this one is faster.
            for( long i = 0; i < numTrials; i++ ) {
                DenseDoubleSingularValueDecomposition s = alg.svd(matA);
                U = s.getU();
                S = s.getS();
                V = s.getV();
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new PColtBenchmarkMatrix(U);
                outputs[1] = new PColtBenchmarkMatrix(S);
                outputs[2] = new PColtBenchmarkMatrix(V);
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
            DoubleMatrix2D matA = inputs[0].getOriginal();

            DenseDoubleAlgebra alg = new DenseDoubleAlgebra();

            DoubleMatrix2D D = null;
            DoubleMatrix2D V = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                DenseDoubleEigenvalueDecomposition e = alg.eig(matA);
                D = e.getD();
                V = e.getV();
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new PColtBenchmarkMatrix(D);
                outputs[1] = new PColtBenchmarkMatrix(V);
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
            DoubleMatrix2D matA = inputs[0].getOriginal();

            DenseDoubleAlgebra alg = new DenseDoubleAlgebra();

            DoubleMatrix2D Q = null;
            DoubleMatrix2D R = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                DenseDoubleQRDecomposition decomp = alg.qr(matA);

                Q = decomp.getQ(true);
                R = decomp.getR(true);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new PColtBenchmarkMatrix(Q);
                outputs[1] = new PColtBenchmarkMatrix(R);
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
            DoubleMatrix2D matA = inputs[0].getOriginal();

            DenseDoubleAlgebra alg = new DenseDoubleAlgebra();

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                alg.det(matA);
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
            DoubleMatrix2D matA = inputs[0].getOriginal();

            DenseDoubleAlgebra alg = new DenseDoubleAlgebra();

            DoubleMatrix2D result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = alg.inverse(matA);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new PColtBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface invertSymmPosDef() {
        return new InvSymmPosDef();
    }

    // DenseDoubleAlgebra
    public static class InvSymmPosDef implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DoubleMatrix2D matA = inputs[0].getOriginal();

            DenseDoubleAlgebra alg = new DenseDoubleAlgebra();

            DoubleMatrix2D result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                // can't decompose a matrix with the same decomposition algorithm
                DenseDoubleCholeskyDecomposition chol = alg.chol(matA);

                result = DoubleFactory2D.dense.identity(matA.rows());
                chol.solve(result);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new PColtBenchmarkMatrix(result);
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
            DoubleMatrix2D matA = inputs[0].getOriginal();
            DoubleMatrix2D matB = inputs[1].getOriginal();

            DoubleMatrix2D result = createMatrix(matA.rows(),matA.columns());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                // in-place operator
                result.assign(matA);
                result.assign(matB, DoubleFunctions.plus);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new PColtBenchmarkMatrix(result);
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
            DoubleMatrix2D matA = inputs[0].getOriginal();
            DoubleMatrix2D matB = inputs[1].getOriginal();

            DenseDoubleAlgebra alg = new DenseDoubleAlgebra();
            DoubleMatrix2D result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = alg.mult(matA,matB);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new PColtBenchmarkMatrix(result);
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
            DoubleMatrix2D matA = inputs[0].getOriginal();
            DoubleMatrix2D matB = inputs[1].getOriginal();

            DoubleMatrix2D result = createMatrix(matA.columns(),matB.columns());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.zMult(matB, result, 1, 0, false, true);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new PColtBenchmarkMatrix(result);
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
            DoubleMatrix2D matA = inputs[0].getOriginal();

            DoubleMatrix2D result = createMatrix(matA.rows(),matA.columns());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                // in-place operator
                result.assign(matA);
                result.assign(DoubleFunctions.mult(ScaleGenerator.SCALE));
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new PColtBenchmarkMatrix(result);
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
            DoubleMatrix2D matA = inputs[0].getOriginal();
            DoubleMatrix2D matB = inputs[1].getOriginal();

            DenseDoubleAlgebra alg = new DenseDoubleAlgebra();
            DoubleMatrix2D result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = alg.solve(matA,matB);
            }

            long elapsedTime = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new PColtBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface transpose() {
        // yep it just marks it as transposed
        return null;
    }

    public static DenseDoubleMatrix2D createMatrix( int numRows , int numCols ) {
        // this matrix type is used at the suggestion of Piotr Wendykier
        return new DenseDoubleMatrix2D( numRows , numCols );
    }

    @Override
    public BenchmarkMatrix convertToLib(DenseMatrix64F input) {
        return new PColtBenchmarkMatrix(convertToParallelColt(input));
    }

    @Override
    public DenseMatrix64F convertToEjml(BenchmarkMatrix input) {
        cern.colt.matrix.tdouble.DoubleMatrix2D orig = input.getOriginal();
        return parallelColtToEjml(orig);
    }

    public static cern.colt.matrix.tdouble.DoubleMatrix2D convertToParallelColt( DenseMatrix64F orig )
    {
        DenseDoubleMatrix2D mat = createMatrix(orig.numRows,orig.numCols);

        for( int i = 0; i < orig.numRows; i++ ) {
            for( int j = 0; j < orig.numCols; j++ ) {
                mat.set(i,j,orig.get(i,j));
            }
        }

        return mat;
    }

    public static DenseMatrix64F parallelColtToEjml( cern.colt.matrix.tdouble.DoubleMatrix2D orig )
    {
        if( orig == null )
            return null;

        DenseMatrix64F mat = new DenseMatrix64F(orig.rows(),orig.columns());

        for( int i = 0; i < mat.numRows; i++ ) {
            for( int j = 0; j < mat.numCols; j++ ) {
                mat.set(i,j,orig.get(i,j));
            }
        }

        return mat;
    }

}
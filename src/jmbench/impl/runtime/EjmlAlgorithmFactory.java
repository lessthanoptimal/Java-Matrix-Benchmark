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
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.DetectedException;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.runtime.generator.ScaleGenerator;
import org.ejml.alg.dense.linsol.LinearSolverSafe;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.*;
import org.ejml.ops.CommonOps;
import org.ejml.ops.CovarianceOps;
import org.ejml.ops.EigenOps;


/**
 * @author Peter Abeles
 */
public class EjmlAlgorithmFactory implements RuntimePerformanceFactory {


    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new EjmlBenchmarkMatrix((DenseMatrix64F)matrix);
    }

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        DenseMatrix64F A = new DenseMatrix64F(numRows,numCols);
        return wrap(A);
    }

    @Override
    public MatrixProcessorInterface chol() {
        return new Chol();
    }

    public static class Chol implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix64F matA = inputs[0].getOriginal();

            CholeskyDecomposition<DenseMatrix64F> chol = DecompositionFactory.chol(matA.numRows, true);

            DenseMatrix64F L = new DenseMatrix64F(matA.numRows,matA.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !DecompositionFactory.decomposeSafe(chol,matA) ) {
                    throw new DetectedException("Decomposition failed");
                }
                chol.getT(L);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(L);
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
            DenseMatrix64F matA = inputs[0].getOriginal();

            LUDecomposition<DenseMatrix64F> lu = DecompositionFactory.lu(matA.numRows,matA.numCols);

            DenseMatrix64F L = new DenseMatrix64F(matA.numRows,matA.numCols);
            DenseMatrix64F U = new DenseMatrix64F(matA.numRows,matA.numCols);
            DenseMatrix64F P = new DenseMatrix64F(matA.numRows,matA.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !DecompositionFactory.decomposeSafe(lu,matA) )
                    throw new DetectedException("Decomposition failed");

                lu.getLower(L);
                lu.getUpper(U);
                lu.getPivot(P);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(L);
                outputs[1] = new EjmlBenchmarkMatrix(U);
                outputs[2] = new EjmlBenchmarkMatrix(P);
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
            DenseMatrix64F matA = inputs[0].getOriginal();

            SingularValueDecomposition<DenseMatrix64F> svd = DecompositionFactory.svd(matA.numRows,matA.numCols,true,true,false);

            DenseMatrix64F U = null;
            DenseMatrix64F S = null;
            DenseMatrix64F V = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !DecompositionFactory.decomposeSafe(svd,matA) )
                    throw new DetectedException("Decomposition failed");
                U = svd.getU(null, false);
                S = svd.getW(S);
                V = svd.getV(null, false);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(U);
                outputs[1] = new EjmlBenchmarkMatrix(S);
                outputs[2] = new EjmlBenchmarkMatrix(V);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface eigSymm() {
        return new MyEig();
    }

    public static class MyEig implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix64F matA = inputs[0].getOriginal();

            EigenDecomposition<DenseMatrix64F> eig = DecompositionFactory.eig(matA.numCols, true, true);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !DecompositionFactory.decomposeSafe(eig,matA) )
                    throw new DetectedException("Decomposition failed");
                // this isn't necessary since eigenvalues and eigenvectors are always computed
                eig.getEigenvalue(0);
                eig.getEigenVector(0);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(EigenOps.createMatrixD(eig));
                outputs[1] = new EjmlBenchmarkMatrix(EigenOps.createMatrixV(eig));
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
            DenseMatrix64F matA = inputs[0].getOriginal();

            QRDecomposition<DenseMatrix64F> qr = DecompositionFactory.qr(matA.numRows,matA.numCols);
            DenseMatrix64F Q = null;
            DenseMatrix64F R = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !DecompositionFactory.decomposeSafe(qr,matA) )
                    throw new DetectedException("Decomposition failed");

                Q = qr.getQ(null,true);
                R = qr.getR(null,true);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(Q);
                outputs[1] = new EjmlBenchmarkMatrix(R);
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
            DenseMatrix64F matA = inputs[0].getOriginal();

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CommonOps.det(matA);
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
            DenseMatrix64F matA = inputs[0].getOriginal();

            DenseMatrix64F result = new DenseMatrix64F(matA.numRows,matA.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !CommonOps.invert(matA,result) )
                    throw new DetectedException("Inversion failed");
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(result);
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
            DenseMatrix64F matA = inputs[0].getOriginal();

            DenseMatrix64F result = new DenseMatrix64F(matA.numRows,matA.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !CovarianceOps.invert(matA,result) )
                    throw new RuntimeException("Inversion failed");
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(result);
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
            DenseMatrix64F matA = inputs[0].getOriginal();
            DenseMatrix64F matB = inputs[1].getOriginal();

            DenseMatrix64F result = new DenseMatrix64F(matA);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CommonOps.add(matA,matB,result);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(result);
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
            DenseMatrix64F matA = inputs[0].getOriginal();
            DenseMatrix64F matB = inputs[1].getOriginal();

            DenseMatrix64F result = new DenseMatrix64F(matA.numRows,matB.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CommonOps.mult(matA,matB,result);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(result);
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
            DenseMatrix64F matA = inputs[0].getOriginal();
            DenseMatrix64F matB = inputs[1].getOriginal();

            DenseMatrix64F result = new DenseMatrix64F(matA.numCols,matB.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CommonOps.multTransB(matA,matB,result);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(result);
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
            DenseMatrix64F matA = inputs[0].getOriginal();

            DenseMatrix64F result = new DenseMatrix64F(matA.numRows,matA.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CommonOps.scale(ScaleGenerator.SCALE,matA,result);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface solveExact() {
        return new SolveExact();
    }
    public static class SolveExact implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix64F matA = inputs[0].getOriginal();
            DenseMatrix64F matB = inputs[1].getOriginal();

            DenseMatrix64F result = new DenseMatrix64F(matA.numCols,matB.numCols);

            LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.linear(matA.numRows);
            // make sure the input is not modified
            solver = new LinearSolverSafe<DenseMatrix64F>(solver);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !solver.setA(matA) )
                    throw new DetectedException("Bad A");

                solver.solve(matB,result);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public MatrixProcessorInterface solveOver() {
        return new SolveOver();
    }

    public static class SolveOver implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix64F matA = inputs[0].getOriginal();
            DenseMatrix64F matB = inputs[1].getOriginal();

            DenseMatrix64F result = new DenseMatrix64F(matA.numCols,matB.numCols);

            LinearSolver<DenseMatrix64F> solver = LinearSolverFactory.leastSquares(matA.numRows,matA.numCols);

            // make sure the input is not modified
            solver = new LinearSolverSafe<DenseMatrix64F>(solver);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !solver.setA(matA) )
                    throw new DetectedException("Bad A");

                solver.solve(matB,result);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(result);
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
            DenseMatrix64F matA = inputs[0].getOriginal();

            DenseMatrix64F result = new DenseMatrix64F(matA.numCols,matA.numRows);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CommonOps.transpose(matA,result);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    @Override
    public BenchmarkMatrix convertToLib(DenseMatrix64F input) {
        return new EjmlBenchmarkMatrix(input);
    }

    @Override
    public DenseMatrix64F convertToEjml(BenchmarkMatrix input) {
        return (DenseMatrix64F)input.getOriginal();
    }
}

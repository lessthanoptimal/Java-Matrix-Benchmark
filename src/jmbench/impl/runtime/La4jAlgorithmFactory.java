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

import jmbench.impl.wrapper.La4jBenchmarkMatrix;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.runtime.generator.ScaleGenerator;
import org.ejml.data.DenseMatrix64F;
import org.la4j.LinearAlgebra;
import org.la4j.decomposition.MatrixDecompositor;
import org.la4j.inversion.MatrixInverter;
import org.la4j.linear.LinearSystemSolver;
import org.la4j.matrix.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.Vector;

/**
 * Wrapper around la4j
 *
 * @author Peter Abels
 * @author Vladimir Kostyukov
 */
public class La4jAlgorithmFactory implements RuntimePerformanceFactory {
    @Override
    public MatrixProcessorInterface chol() {
        return new Chol();
    }

    public static class Chol implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix a = inputs[0].getOriginal();

            Matrix L = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                MatrixDecompositor decompositor = a.withDecompositor(LinearAlgebra.CHOLESKY);
                Matrix l[] = decompositor.decompose();
                L = l[0];
            }

            long elapsed = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new La4jBenchmarkMatrix(L);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface lu() {
        return new LU();
    }

    public static class LU implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix a = inputs[0].getOriginal();

            Matrix L = null;
            Matrix U = null;
            Matrix P = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                MatrixDecompositor decompositor = a.withDecompositor(LinearAlgebra.LU);
                Matrix[] lup = decompositor.decompose();
                L = lup[0];
                U = lup[1];
                P = lup[2];
            }

            long elapsed = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new La4jBenchmarkMatrix(L);
                outputs[1] = new La4jBenchmarkMatrix(U);
                outputs[2] = new La4jBenchmarkMatrix(P);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface svd() {
        return new SVD();
    }

    public static class SVD implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix a = inputs[0].getOriginal();

            Matrix U = null;
            Matrix S = null;
            Matrix V = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                MatrixDecompositor decompositor = a.withDecompositor(LinearAlgebra.SVD);
                Matrix usv[] = decompositor.decompose();
                U = usv[0];
                S = usv[1];
                V = usv[2];
            }

            long elapsed = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new La4jBenchmarkMatrix(U);
                outputs[1] = new La4jBenchmarkMatrix(S);
                outputs[2] = new La4jBenchmarkMatrix(V);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface qr() {
        return new QR();
    }

    public static class QR implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix a = inputs[0].getOriginal();

            Matrix Q = null;
            Matrix R = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                MatrixDecompositor decompositor = a.withDecompositor(LinearAlgebra.QR);
                Matrix qr[] = decompositor.decompose();
                Q = qr[0];
                R = qr[1];
            }

            long elapsed = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new La4jBenchmarkMatrix(Q);
                outputs[1] = new La4jBenchmarkMatrix(R);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface eigSymm() {
        return new Eig();
    }

    public static class Eig implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix a = inputs[0].getOriginal();

            Matrix D = null;
            Matrix V = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                MatrixDecompositor decompositor = a.withDecompositor(LinearAlgebra.EIGEN);
                Matrix vd[] = decompositor.decompose();
                V = vd[0];
                D = vd[1];
            }

            long elapsed = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new La4jBenchmarkMatrix(D);
                outputs[1] = new La4jBenchmarkMatrix(V);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface det() {
        return new Det();
    }

    public static class Det implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix a = inputs[0].getOriginal();

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                a.determinant();
            }

            return System.nanoTime() - prev;
        }
    }
    @Override
    public MatrixProcessorInterface invert() {
        return new SmartInverse();
    }

    @Override
    public MatrixProcessorInterface invertSymmPosDef() {
        return new SmartInverse();
    }


    public static class SmartInverse implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix a = inputs[0].getOriginal();

            Matrix A = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                MatrixInverter inverter = a.withInverter(LinearAlgebra.INVERTER);
                A = inverter.inverse();
            }

            long elapsed = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new La4jBenchmarkMatrix(A);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface add() {
        return new Add();
    }

    public static class Add implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix a = inputs[0].getOriginal();
            Matrix b = inputs[1].getOriginal();

            Matrix C = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                C = a.add(b);
            }

            long elapsed = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new La4jBenchmarkMatrix(C);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface mult() {
        return new Mult();
    }

    public static class Mult implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix a = inputs[0].getOriginal();
            Matrix b = inputs[1].getOriginal();

            long prev = System.nanoTime();

            Matrix C = null;

            for( long i = 0; i < numTrials; i++ ) {
                C = a.multiply(b);
            }

            long elapsed = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new La4jBenchmarkMatrix(C);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface multTransB() {
        return new MulTranB();
    }

    public static class MulTranB implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix a = inputs[0].getOriginal();
            Matrix b = inputs[1].getOriginal();

            Matrix C = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                C = a.multiply(b.transpose());
            }

            long elapsed = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new La4jBenchmarkMatrix(C);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface scale() {
        return new Scale();
    }

    public static class Scale implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix a = inputs[0].getOriginal();

            Matrix B = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                B = a.multiply(ScaleGenerator.SCALE);
            }

            long elapsed = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new La4jBenchmarkMatrix(B);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface solveExact() {
        return new SmartSolve();
    }

    @Override
    public MatrixProcessorInterface solveOver() {
        return new SmartSolve();
    }

    public static class SmartSolve implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix a = inputs[0].getOriginal();
            Vector b = La4jBenchmarkMatrix.toVector((Matrix) inputs[1].getOriginal());

            Vector X = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                LinearSystemSolver solver = a.withSolver(LinearAlgebra.SOLVER);
                X = solver.solve(b);
            }

            long elapsed = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new La4jBenchmarkMatrix(La4jBenchmarkMatrix.toMatrix(X));
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface transpose() {
        return new Transpose();
    }

    public static class Transpose implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            Matrix a = inputs[0].getOriginal();

            Matrix B = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                B = a.transpose();
            }

            long elapsed = System.nanoTime()- prev;
            if( outputs != null ) {
                outputs[0] = new La4jBenchmarkMatrix(B);
            }
            return elapsed;
        }
    }

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        return new La4jBenchmarkMatrix(new Basic2DMatrix(numRows, numCols));
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new La4jBenchmarkMatrix((Matrix)matrix);
    }

    @Override
    public BenchmarkMatrix convertToLib(DenseMatrix64F input) {
        return new La4jBenchmarkMatrix(ejmlToLa4j(input));
    }

    @Override
    public DenseMatrix64F convertToEjml(BenchmarkMatrix input) {
        Matrix orig = input.getOriginal();
        return la4jToEjml(orig);
    }
    
    public static Matrix ejmlToLa4j( DenseMatrix64F orig ) {
        Matrix m = new Basic2DMatrix(orig.numRows, orig.numCols);

        for( int i = 0; i < orig.numRows; i++ ) {
            for( int j = 0; j < orig.numCols; j++ ) {
                m.set(i,j, orig.get(i,j));
            }
        }

        return m;
    }

    public static DenseMatrix64F la4jToEjml( Matrix orig ) {
        DenseMatrix64F m = new DenseMatrix64F(orig.rows(),orig.columns());

        for( int i = 0; i < m.numRows; i++ ) {
            for( int j = 0; j < m.numCols; j++ ) {
                m.set(i,j, orig.get(i,j));
            }
        }

        return m;
    }
}

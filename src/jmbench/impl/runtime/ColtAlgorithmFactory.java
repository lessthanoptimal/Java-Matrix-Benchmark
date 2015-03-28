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

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.*;
import jmbench.impl.wrapper.ColtBenchmarkMatrix;
import jmbench.impl.wrapper.EjmlBenchmarkMatrix;
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
public class ColtAlgorithmFactory implements RuntimePerformanceFactory {

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        DenseDoubleMatrix2D mat = new DenseDoubleMatrix2D(numRows,numCols);

        return wrap(mat);
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new ColtBenchmarkMatrix((DenseDoubleMatrix2D)matrix);
    }

    @Override
    public MatrixProcessorInterface chol() {
        return new Chol();
    }

    public static class Chol implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseDoubleMatrix2D matA = inputs[0].getOriginal();

            DoubleMatrix2D L = null;
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CholeskyDecomposition chol = new CholeskyDecomposition(matA);

                if( !chol.isSymmetricPositiveDefinite() ) {
                    throw new DetectedException("Is not SPD");
                }

                L = chol.getL();
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new ColtBenchmarkMatrix(L);
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
            DenseDoubleMatrix2D matA = inputs[0].getOriginal();

            LUDecompositionQuick lu = new LUDecompositionQuick();
            DenseDoubleMatrix2D tmp = new DenseDoubleMatrix2D(matA.rows(),matA.columns());

            DoubleMatrix2D L = null;
            DoubleMatrix2D U = null;
            int pivot[] = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                tmp.assign(matA);
                lu.decompose(tmp);

                L = lu.getL();
                U = lu.getU();
                pivot = lu.getPivot();

                if( !lu.isNonsingular() )
                    throw new DetectedException("Singular matrix");
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new ColtBenchmarkMatrix(L);
                outputs[1] = new ColtBenchmarkMatrix(U);
                outputs[2] = new EjmlBenchmarkMatrix(SpecializedOps.pivotMatrix(null, pivot, pivot.length, false));
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
            DenseDoubleMatrix2D matA = inputs[0].getOriginal();

            DoubleMatrix2D U = null;
            DoubleMatrix2D S = null;
            DoubleMatrix2D V = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                SingularValueDecomposition s = new SingularValueDecomposition(matA);
                U = s.getU();
                S = s.getS();
                V = s.getV();
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new ColtBenchmarkMatrix(U);
                outputs[1] = new ColtBenchmarkMatrix(S);
                outputs[2] = new ColtBenchmarkMatrix(V);
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
            DenseDoubleMatrix2D matA = inputs[0].getOriginal();

            DoubleMatrix2D D = null;
            DoubleMatrix2D V = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                EigenvalueDecomposition eig = new EigenvalueDecomposition(matA);

                D = eig.getD();
                V = eig.getV();
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new ColtBenchmarkMatrix(D);
                outputs[1] = new ColtBenchmarkMatrix(V);
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
            DenseDoubleMatrix2D matA = inputs[0].getOriginal();

            DoubleMatrix2D Q = null;
            DoubleMatrix2D R = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                QRDecomposition qr = new QRDecomposition(matA);

                Q = qr.getQ();
                R = qr.getR();
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new ColtBenchmarkMatrix(Q);
                outputs[1] = new ColtBenchmarkMatrix(R);
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
            DenseDoubleMatrix2D matA = inputs[0].getOriginal();

            Algebra alg = new Algebra();

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
            DenseDoubleMatrix2D matA = inputs[0].getOriginal();

            Algebra alg = new Algebra();

            DoubleMatrix2D result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = alg.inverse(matA);
            }

            long elapsed = System.nanoTime()-prev;

            if( outputs != null ) {
                outputs[0] = new ColtBenchmarkMatrix(result);
            }

            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface invertSymmPosDef() {
        return new InvSymmPosDef();
    }

    public static class InvSymmPosDef implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseDoubleMatrix2D matA = inputs[0].getOriginal();

            DoubleMatrix2D result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CholeskyDecomposition chol = new CholeskyDecomposition(matA);

                result = chol.solve(DoubleFactory2D.dense.identity(matA.rows()));
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new ColtBenchmarkMatrix(result);
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
            DenseDoubleMatrix2D matA = inputs[0].getOriginal();
            DenseDoubleMatrix2D matB = inputs[1].getOriginal();

            DoubleMatrix2D result = new DenseDoubleMatrix2D(matA.rows(),matA.columns());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                // In-place operation here
                result.assign(matA);
                result.assign(matB, cern.jet.math.Functions.plus);
            }

            long elapsed = System.nanoTime()-prev;

            if( outputs != null ) {
                outputs[0] = new ColtBenchmarkMatrix(result);
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
            DenseDoubleMatrix2D matA = inputs[0].getOriginal();
            DenseDoubleMatrix2D matB = inputs[1].getOriginal();

            Algebra alg = new Algebra();
            DoubleMatrix2D result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = alg.mult(matA,matB);
            }

            long elapsed = System.nanoTime()-prev;

            if( outputs != null ) {
                outputs[0] = new ColtBenchmarkMatrix(result);
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
            DenseDoubleMatrix2D matA = inputs[0].getOriginal();
            DenseDoubleMatrix2D matB = inputs[1].getOriginal();

            DoubleMatrix2D result = new DenseDoubleMatrix2D(matA.columns(),matB.columns());
            
            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                matA.zMult(matB, result, 1, 0, false, true);
            }

            long elapsed = System.nanoTime()-prev;

            if( outputs != null ) {
                outputs[0] = new ColtBenchmarkMatrix(result);
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
            DenseDoubleMatrix2D matA = inputs[0].getOriginal();

            DoubleMatrix2D result = new DenseDoubleMatrix2D(matA.rows(),matA.columns());

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                // in-place operator
                result.assign(matA);
                result.assign(cern.jet.math.Functions.mult(ScaleGenerator.SCALE));
            }

            long elapsed = System.nanoTime()-prev;

            if( outputs != null ) {
                outputs[0] = new ColtBenchmarkMatrix(result);
            }

            return elapsed;
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
            DenseDoubleMatrix2D matA = inputs[0].getOriginal();
            DenseDoubleMatrix2D matB = inputs[1].getOriginal();

            Algebra alg = new Algebra();
            DoubleMatrix2D result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = alg.solve(matA,matB);
            }

            if( outputs != null ) {
                outputs[0] = new ColtBenchmarkMatrix(result);
            }

            return System.nanoTime()-prev;
        }
    }

    @Override
    public MatrixProcessorInterface transpose() {
        // yep this is one of "those" libraries that just flags the matrix as being transposed
        return null;
    }

    @Override
    public BenchmarkMatrix convertToLib(DenseMatrix64F input) {
        return new ColtBenchmarkMatrix(convertToColt(input));
    }

    @Override
    public DenseMatrix64F convertToEjml(BenchmarkMatrix input) {
        DenseDoubleMatrix2D mat = input.getOriginal();
        return coltToEjml(mat);
    }

    public static DenseDoubleMatrix2D convertToColt( DenseMatrix64F orig )
    {
        DenseDoubleMatrix2D mat = new DenseDoubleMatrix2D(orig.numRows,orig.numCols);

        for( int i = 0; i < orig.numRows; i++ ) {
            for( int j = 0; j < orig.numCols; j++ ) {
                mat.set(i,j,orig.get(i,j));
            }
        }

        return mat;
    }

    public static DenseMatrix64F coltToEjml( DoubleMatrix2D orig )
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
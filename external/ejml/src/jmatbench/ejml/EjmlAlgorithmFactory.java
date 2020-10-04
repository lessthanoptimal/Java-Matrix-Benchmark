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
import jmbench.interfaces.DetectedException;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.matrix.RowMajorMatrix;
import jmbench.tools.BenchmarkConstants;
import org.ejml.EjmlVersion;
import org.ejml.LinearSolverSafe;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.CovarianceOps_DDRM;
import org.ejml.dense.row.EigenOps_DDRM;
import org.ejml.dense.row.factory.DecompositionFactory_DDRM;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.decomposition.*;
import org.ejml.interfaces.linsol.LinearSolverDense;


/**
 * @author Peter Abeles
 */
public class EjmlAlgorithmFactory implements RuntimePerformanceFactory {


    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new EjmlBenchmarkMatrix((DMatrixRMaj)matrix);
    }

    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        DMatrixRMaj A = new DMatrixRMaj(numRows,numCols);
        return wrap(A);
    }

    @Override
    public MatrixProcessorInterface chol() {
        return new Chol();
    }

    public static class Chol implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DMatrixRMaj matA = inputs[0].getOriginal();

            CholeskyDecomposition_F64<DMatrixRMaj> chol = DecompositionFactory_DDRM.chol(matA.numRows, true);

            DMatrixRMaj L = new DMatrixRMaj(matA.numRows,matA.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !DecompositionFactory_DDRM.decomposeSafe(chol,matA) ) {
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
            DMatrixRMaj matA = inputs[0].getOriginal();

            LUDecomposition_F64<DMatrixRMaj> lu = DecompositionFactory_DDRM.lu(matA.numRows,matA.numCols);

            DMatrixRMaj L = new DMatrixRMaj(matA.numRows,matA.numCols);
            DMatrixRMaj U = new DMatrixRMaj(matA.numRows,matA.numCols);
            DMatrixRMaj P = new DMatrixRMaj(matA.numRows,matA.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !DecompositionFactory_DDRM.decomposeSafe(lu,matA) )
                    throw new DetectedException("Decomposition failed");

                lu.getLower(L);
                lu.getUpper(U);
                lu.getRowPivot(P);
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
            DMatrixRMaj matA = inputs[0].getOriginal();

            SingularValueDecomposition_F64<DMatrixRMaj> svd = DecompositionFactory_DDRM.svd(matA.numRows,matA.numCols,true,true,false);

            DMatrixRMaj U = null;
            DMatrixRMaj S = null;
            DMatrixRMaj V = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !DecompositionFactory_DDRM.decomposeSafe(svd,matA) )
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
            DMatrixRMaj matA = inputs[0].getOriginal();

            EigenDecomposition_F64<DMatrixRMaj> eig = DecompositionFactory_DDRM.eig(matA.numCols, true, true);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !DecompositionFactory_DDRM.decomposeSafe(eig,matA) )
                    throw new DetectedException("Decomposition failed");
                // this isn't necessary since eigenvalues and eigenvectors are always computed
                eig.getEigenvalue(0);
                eig.getEigenVector(0);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(EigenOps_DDRM.createMatrixD(eig));
                outputs[1] = new EjmlBenchmarkMatrix(EigenOps_DDRM.createMatrixV(eig));
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
            DMatrixRMaj matA = inputs[0].getOriginal();

            QRDecomposition<DMatrixRMaj> qr = DecompositionFactory_DDRM.qr(matA.numRows,matA.numCols);
            DMatrixRMaj Q = null;
            DMatrixRMaj R = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !DecompositionFactory_DDRM.decomposeSafe(qr,matA) )
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
            DMatrixRMaj matA = inputs[0].getOriginal();

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CommonOps_DDRM.det(matA);
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
            DMatrixRMaj matA = inputs[0].getOriginal();

            DMatrixRMaj result = new DMatrixRMaj(matA.numRows,matA.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !CommonOps_DDRM.invert(matA,result) )
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
            DMatrixRMaj matA = inputs[0].getOriginal();

            DMatrixRMaj result = new DMatrixRMaj(matA.numRows,matA.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                if( !CovarianceOps_DDRM.invert(matA,result) )
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
            DMatrixRMaj matA = inputs[0].getOriginal();
            DMatrixRMaj matB = inputs[1].getOriginal();

            DMatrixRMaj result = new DMatrixRMaj(matA);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CommonOps_DDRM.add(matA,matB,result);
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
            DMatrixRMaj matA = inputs[0].getOriginal();
            DMatrixRMaj matB = inputs[1].getOriginal();

            DMatrixRMaj result = new DMatrixRMaj(matA.numRows,matB.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CommonOps_DDRM.mult(matA,matB,result);
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
            DMatrixRMaj matA = inputs[0].getOriginal();
            DMatrixRMaj matB = inputs[1].getOriginal();

            DMatrixRMaj result = new DMatrixRMaj(matA.numCols,matB.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CommonOps_DDRM.multTransB(matA,matB,result);
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
            DMatrixRMaj matA = inputs[0].getOriginal();

            DMatrixRMaj result = new DMatrixRMaj(matA.numRows,matA.numCols);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CommonOps_DDRM.scale(BenchmarkConstants.SCALE,matA,result);
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
            DMatrixRMaj matA = inputs[0].getOriginal();
            DMatrixRMaj matB = inputs[1].getOriginal();

            DMatrixRMaj result = new DMatrixRMaj(matA.numCols,matB.numCols);

            LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.linear(matA.numRows);
            // make sure the input is not modified
            solver = new LinearSolverSafe<>(solver);

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
            DMatrixRMaj matA = inputs[0].getOriginal();
            DMatrixRMaj matB = inputs[1].getOriginal();

            DMatrixRMaj result = new DMatrixRMaj(matA.numCols,matB.numCols);

            LinearSolverDense<DMatrixRMaj> solver = LinearSolverFactory_DDRM.leastSquares(matA.numRows,matA.numCols);

            // make sure the input is not modified
            solver = new LinearSolverSafe<>(solver);

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
            DMatrixRMaj matA = inputs[0].getOriginal();

            DMatrixRMaj result = new DMatrixRMaj(matA.numCols,matA.numRows);

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                CommonOps_DDRM.transpose(matA,result);
            }

            long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new EjmlBenchmarkMatrix(result);
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
        DMatrixRMaj m = ((EjmlBenchmarkMatrix)input).mat;

        RowMajorMatrix out = new RowMajorMatrix(1,1);
        out.data = m.data;
        out.numCols = m.numCols;
        out.numRows = m.numRows;

        return out;
    }

    @Override
    public String getLibraryVersion() {
        return EjmlVersion.VERSION;
    }

    @Override
    public String getSourceHash() {
        return EjmlVersion.GIT_SHA;
    }

    @Override
    public boolean isNative() {
        return false;
    }
}

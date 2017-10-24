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

package jmatbench.ojalgo;

import jmbench.PackageMatrixConversion;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.DetectedException;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.matrix.RowMajorBenchmarkMatrix;
import jmbench.matrix.RowMajorMatrix;
import jmbench.matrix.RowMajorOps;
import jmbench.tools.runtime.generator.ScaleGenerator;
import org.ojalgo.OjAlgoUtils;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.function.PrimitiveFunction;
import org.ojalgo.matrix.decomposition.*;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.task.DeterminantTask;
import org.ojalgo.matrix.task.InverterTask;
import org.ojalgo.matrix.task.SolverTask;

/**
 * @author Peter Abeles
 * @author Anders Peterson (apete)
 */
@SuppressWarnings({ "unchecked" })
public class OjAlgoAlgorithmFactory implements RuntimePerformanceFactory {

    public static class OpAdd implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();
            final MatrixStore<Double> matB = inputs[1].getOriginal();

            final PrimitiveDenseStore result = FACTORY.copy(matA);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                result.fillMatching(matA, PrimitiveFunction.ADD, matB);
            }

            final long elapsedTime = System.nanoTime() - prev;
            if (outputs != null) {
                outputs[0] = new OjAlgoBenchmarkMatrix(result);
            }
            return elapsedTime;
        }
    }

    public static class OpChol implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();

            MatrixStore<Double> L = null;
            final Cholesky<Double> chol = Cholesky.make(matA);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                if (!chol.compute(matA)) {
                    throw new DetectedException("Decomposition failed");
                }
                L = chol.getL();
            }

            final long elapsedTime = System.nanoTime() - prev;

            if (outputs != null) {
                outputs[0] = new OjAlgoBenchmarkMatrix(L);
            }

            return elapsedTime;
        }
    }

    public static class OpDet implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();

            final DeterminantTask<Double> tmpTask = DeterminantTask.PRIMITIVE.make(matA, false, false);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                tmpTask.calculateDeterminant(matA);
            }

            return System.nanoTime() - prev;
        }
    }

    public static class OpEigSymm implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();

            MatrixStore<Double> D = null;
            MatrixStore<Double> V = null;
            final Eigenvalue<Double> eig = Eigenvalue.make(matA);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                if (!eig.decompose(matA)) {
                    throw new DetectedException("Decomposition failed");
                }
                D = eig.getD();
                V = eig.getV();
            }

            final long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new OjAlgoBenchmarkMatrix(D);
                outputs[1] = new OjAlgoBenchmarkMatrix(V);
            }
            return elapsedTime;
        }
    }

    public static class OpInvert implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();
            MatrixStore<Double> result = null;

            final InverterTask<Double> tmpInverter = InverterTask.PRIMITIVE.make(matA, false, false);
            final PhysicalStore<Double> tmpAlloc = tmpInverter.preallocate(matA);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                try {
                    result = tmpInverter.invert(matA, tmpAlloc);
                } catch (final RecoverableCondition ex) {
                    throw new DetectedException(ex);
                }
            }

            final long elapsedTime = System.nanoTime() - prev;
            if( outputs != null )
                outputs[0] = new OjAlgoBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    public static class OpInvertSymmPosDef implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();
            MatrixStore<Double> inverse = null;

            final InverterTask<Double> tmpInverter = InverterTask.PRIMITIVE.make(matA, true, true);
            final PhysicalStore<Double> tmpAlloc = tmpInverter.preallocate(matA);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                try {
                    inverse = tmpInverter.invert(matA, tmpAlloc);
                } catch (final RecoverableCondition ex) {
                    throw new DetectedException(ex);
                }
            }

            final long elapsedTime = System.nanoTime() - prev;
            if( outputs != null )
                outputs[0] = new OjAlgoBenchmarkMatrix(inverse.transpose());
            return elapsedTime;
        }
    }

    public static class OpLu implements MatrixProcessorInterface {

        // TODO change to what Anders said
        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();

            MatrixStore<Double> L = null;
            MatrixStore<Double> U = null;
            int pivot[] = null;
            final LU<Double> lu = LU.make(matA);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                if (!lu.compute(matA)) {
                    throw new DetectedException("Decomposition failed");
                }

                L = lu.getL();
                U = lu.getU();
                pivot = lu.getPivotOrder();
            }

            final long elapsedTime = System.nanoTime() - prev;

            if( outputs != null ) {
                outputs[0] = new OjAlgoBenchmarkMatrix(L);
                outputs[1] = new OjAlgoBenchmarkMatrix(U);
                outputs[2] = new RowMajorBenchmarkMatrix(RowMajorOps.pivotMatrix(null, pivot, pivot.length, false));
            }
            return elapsedTime;
        }
    }

    public static class OpMult implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();
            final MatrixStore<Double> matB = inputs[1].getOriginal();

            final PrimitiveDenseStore result = FACTORY.makeZero(matA.countRows(), matB.countColumns());

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                result.fillByMultiplying(matA, matB);
            }

            final long elapsedTime = System.nanoTime() - prev;
            if( outputs != null )
                outputs[0] = new OjAlgoBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    public static class OpMultTransB implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();
            final MatrixStore<Double> matB = inputs[1].getOriginal();

            final PrimitiveDenseStore result = FACTORY.makeZero(matA.countRows(), matB.countRows());

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                result.fillByMultiplying(matA, matB.transpose());
            }

            final long elapsedTime = System.nanoTime() - prev;
            if( outputs != null )
                outputs[0] = new OjAlgoBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    public static class OpQr implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();

            MatrixStore<Double> Q = null;
            MatrixStore<Double> R = null;
            final QR<Double> qr = QR.make(matA);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                if (!qr.compute(matA)) {
                    throw new DetectedException("Decomposition failed");
                }
                Q = qr.getQ();
                R = qr.getR();
            }

            final long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new OjAlgoBenchmarkMatrix(Q);
                outputs[1] = new OjAlgoBenchmarkMatrix(R);
            }
            return elapsedTime;
        }
    }

    public static class OpScale implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();

            final Double tmpArg = ScaleGenerator.SCALE;

            final PrimitiveDenseStore result = FACTORY.copy(matA);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                result.fillMatching(matA, PrimitiveFunction.MULTIPLY, tmpArg);
            }

            final long elapsedTime = System.nanoTime() - prev;
            if( outputs != null )
                outputs[0] = new OjAlgoBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    public static class OpSolveExact implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final PrimitiveDenseStore matA = inputs[0].getOriginal();
            final PrimitiveDenseStore matB = inputs[1].getOriginal();
            MatrixStore<Double> result = null;

            final SolverTask<Double> tmpSolver = SolverTask.PRIMITIVE.make(matA, matB, false, false);
            final PhysicalStore<Double> tmpAlloc = tmpSolver.preallocate(matA, matB);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                try {
                    result = tmpSolver.solve(matA, matB, tmpAlloc);
                } catch (final RecoverableCondition ex) {
                    throw new DetectedException(ex);
                }
            }

            final long elapsedTime = System.nanoTime() - prev;
            if( outputs != null )
                outputs[0] = new OjAlgoBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    public static class OpSolveOver implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();
            final MatrixStore<Double> matB = inputs[1].getOriginal();
            MatrixStore<Double> result = null;

            final SolverTask<Double> tmpSolver = SolverTask.PRIMITIVE.make(matA, matB, false, false);
            final PhysicalStore<Double> tmpAlloc = tmpSolver.preallocate(matA, matB);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                try {
                    result = tmpSolver.solve(matA, matB, tmpAlloc);
                } catch (final RecoverableCondition ex) {
                    throw new DetectedException(ex);
                }
            }

            final long elapsedTime = System.nanoTime() - prev;
            if( outputs != null )
                outputs[0] = new OjAlgoBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    public static class OpSvd implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();

            MatrixStore<Double> U = null;
            MatrixStore<Double> S = null;
            MatrixStore<Double> V = null;

            final SingularValue<Double> svd = SingularValue.make(matA);

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                if (!svd.compute(matA)) {
                    throw new DetectedException("Decomposition failed");
                }
                U = svd.getQ1();
                S = svd.getD();
                V = svd.getQ2();
            }

            final long elapsedTime = System.nanoTime() - prev;
            if( outputs != null ) {
                outputs[0] = new OjAlgoBenchmarkMatrix(U);
                outputs[1] = new OjAlgoBenchmarkMatrix(S);
                outputs[2] = new OjAlgoBenchmarkMatrix(V);
            }
            return elapsedTime;
        }
    }

    public static class OpTranspose implements MatrixProcessorInterface {

        public long process(final BenchmarkMatrix[] inputs, final BenchmarkMatrix[] outputs, final long numTrials) {

            final MatrixStore<Double> matA = inputs[0].getOriginal();

            final PrimitiveDenseStore result = FACTORY.makeZero(matA.countColumns(), matA.countRows());

            final long prev = System.nanoTime();

            for (long i = 0; i < numTrials; i++) {
                result.fillMatching(matA.transpose());
            }

            final long elapsedTime = System.nanoTime() - prev;
            if( outputs != null )
                outputs[0] = new OjAlgoBenchmarkMatrix(result);
            return elapsedTime;
        }
    }

    static final PhysicalStore.Factory<Double, PrimitiveDenseStore> FACTORY = PrimitiveDenseStore.FACTORY;

    public static PrimitiveDenseStore convertToOjAlgo(final RowMajorMatrix orig) {

        final double[][] raw = PackageMatrixConversion.convertToArray2D(orig);

        return FACTORY.rows(raw);
    }

    public static RowMajorMatrix ojAlgoToRowMajor(final MatrixStore<?> orig) {
        if (orig == null) {
            return null;
        }

        final RowMajorMatrix ret = new RowMajorMatrix((int) orig.countRows(), (int) orig.countColumns());

        for (int i = 0; i < ret.numRows; i++) {
            for (int j = 0; j < ret.numCols; j++) {
                ret.set(i, j, orig.doubleValue(i, j));
            }
        }

        return ret;
    }

    @Override
    public MatrixProcessorInterface add() {
        return new OpAdd();
    }

    @Override
    public MatrixProcessorInterface chol() {
        return new OpChol();
    }

    @Override
    public RowMajorMatrix convertToRowMajor(final BenchmarkMatrix input) {
        final MatrixStore<Double> mat = input.getOriginal();
        return OjAlgoAlgorithmFactory.ojAlgoToRowMajor(mat);
    }

    @Override
    public BenchmarkMatrix convertToLib(final RowMajorMatrix input) {
        return new OjAlgoBenchmarkMatrix(OjAlgoAlgorithmFactory.convertToOjAlgo(input));
    }

    @Override
    public BenchmarkMatrix create(final int numRows, final int numCols) {
        return this.wrap(FACTORY.makeZero(numRows, numCols));
    }

    @Override
    public MatrixProcessorInterface det() {
        return new OpDet();
    }

    @Override
    public MatrixProcessorInterface eigSymm() {
        return new OpEigSymm();
    }

    @Override
    public MatrixProcessorInterface invert() {
        return new OpInvert();
    }

    @Override
    public MatrixProcessorInterface invertSymmPosDef() {
        return new OpInvertSymmPosDef();
    }

    @Override
    public MatrixProcessorInterface lu() {
        return new OpLu();
    }

    @Override
    public MatrixProcessorInterface mult() {
        return new OpMult();
    }

    @Override
    public MatrixProcessorInterface multTransB() {
        return new OpMultTransB();
    }

    @Override
    public MatrixProcessorInterface qr() {
        return new OpQr();
    }

    @Override
    public MatrixProcessorInterface scale() {
        return new OpScale();
    }

    @Override
    public MatrixProcessorInterface solveExact() {
        return new OpSolveExact();
    }

    @Override
    public MatrixProcessorInterface solveOver() {
        return new OpSolveOver();
    }

    @Override
    public MatrixProcessorInterface svd() {
        return new OpSvd();
    }

    @Override
    public MatrixProcessorInterface transpose() {
        return new OpTranspose();
    }

    @Override
    public BenchmarkMatrix wrap(final Object matrix) {
        return new OjAlgoBenchmarkMatrix((MatrixStore<?>) matrix);
    }

    public String getLibraryVersion() {
        return OjAlgoUtils.getVersion();
    }

    public boolean isNative() {
        return false;
    }
}

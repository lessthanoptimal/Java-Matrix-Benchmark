package jmatbench.jeigen;

import jeigen.DenseMatrix;
import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.matrix.RowMajorMatrix;
import jmbench.tools.BenchmarkConstants;

/**
 * @author Peter Abeles
 */
// TODO fix solve solveOver()  crashes hard
    // TODO fix SVD
public class JeigenAlgorithmFactory implements RuntimePerformanceFactory {
    @Override
    public BenchmarkMatrix create(int numRows, int numCols) {
        return wrap( new DenseMatrix(numRows, numCols));
    }

    @Override
    public BenchmarkMatrix wrap(Object matrix) {
        return new JeigenBenchmarkMatrix((DenseMatrix)matrix);
    }

    @Override
    public MatrixProcessorInterface chol() {
        throw new RuntimeException("Operation not supported"); // TODO see if supported in future
    }


    @Override
    public MatrixProcessorInterface lu() {
        throw new RuntimeException("Operation not supported"); // TODO see if supported in future
    }

    @Override
    public MatrixProcessorInterface svd() { // TODO see if supported in future
        return new SVD();
    }

    public static class SVD implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix matA = inputs[0].getOriginal();

            DenseMatrix.SvdResult results = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                results = matA.svd();
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new JeigenBenchmarkMatrix(results.U);
                outputs[1] = new JeigenBenchmarkMatrix(results.S);
                outputs[2] = new JeigenBenchmarkMatrix(results.V);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface qr() {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public MatrixProcessorInterface eigSymm() { // TODO see if supported in future
        return new Eig();
    }

    public static class Eig implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix matA = inputs[0].getOriginal();

            DenseMatrix.EigenResult results = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                results = matA.eig();
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new JeigenBenchmarkMatrix(results.vectors.real());
                outputs[1] = new JeigenBenchmarkMatrix(results.values.real());
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface det() { // TODO see if supported in future
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public MatrixProcessorInterface invert() {
        return new Inv();
    }

    public static class Inv implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix matA = inputs[0].getOriginal();

            DenseMatrix I = DenseMatrix.eye(matA.cols);

            DenseMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.ldltSolve(I);
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new JeigenBenchmarkMatrix(result);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface invertSymmPosDef() {
        return new Inv(); // TODO is there a specialized operation for this?!
    }

    @Override
    public MatrixProcessorInterface add() { // TODO see if supported in future
        return new Add();
    }

    public static class Add implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix matA = inputs[0].getOriginal();
            DenseMatrix matB = inputs[1].getOriginal();

            DenseMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.add(matB);
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new JeigenBenchmarkMatrix(result);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface mult() { // TODO see if supported in future
        return new Mult();
    }

    public static class Mult implements MatrixProcessorInterface {
        @Override
        public long process(BenchmarkMatrix[] inputs, BenchmarkMatrix[] outputs, long numTrials) {
            DenseMatrix matA = inputs[0].getOriginal();
            DenseMatrix matB = inputs[1].getOriginal();

            long prev = System.nanoTime();

            DenseMatrix result = null;

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.mul(matB);
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new JeigenBenchmarkMatrix(result);
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
            DenseMatrix matA = inputs[0].getOriginal();
            DenseMatrix matB = inputs[1].getOriginal();

            DenseMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.mul(matB.t());
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new JeigenBenchmarkMatrix(result);
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
            DenseMatrix matA = inputs[0].getOriginal();

            DenseMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.mul(BenchmarkConstants.SCALE);
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new JeigenBenchmarkMatrix(result);
            }
            return elapsed;
        }
    }

    @Override
    public MatrixProcessorInterface solveExact() {
        return new Solve(); // TODO no specialized?
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

            DenseMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.ldltSolve(matB);
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new JeigenBenchmarkMatrix(result);
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
            DenseMatrix matA = inputs[0].getOriginal();

            DenseMatrix result = null;

            long prev = System.nanoTime();

            for( long i = 0; i < numTrials; i++ ) {
                result = matA.t();
            }

            long elapsed = System.nanoTime()-prev;
            if( outputs != null ) {
                outputs[0] = new JeigenBenchmarkMatrix(result);
            }
            return elapsed;
        }
    }

    @Override
    public BenchmarkMatrix convertToLib(RowMajorMatrix orig) {
        DenseMatrix ret = new DenseMatrix(orig.getNumRows(),orig.getNumCols());

        for( int i = 0; i < orig.numRows; i++ ) {
            for( int j = 0; j < orig.numCols; j++ ) {
                ret.set(i,j,orig.get(i,j));
            }
        }

        return new JeigenBenchmarkMatrix(ret);
    }

    @Override
    public RowMajorMatrix convertToRowMajor(BenchmarkMatrix orig) {
        if( orig == null )
            return null;

        DenseMatrix A = orig.getOriginal();

        RowMajorMatrix ret = new RowMajorMatrix(orig.numRows(),orig.numCols());
        System.arraycopy(A.getValues(),0,ret.data,0,ret.data.length);
        return ret;
    }

    @Override
    public String getLibraryVersion() {
        return "v1.2";
    }

    @Override
    public boolean isNative() {
        return true;
    }
}

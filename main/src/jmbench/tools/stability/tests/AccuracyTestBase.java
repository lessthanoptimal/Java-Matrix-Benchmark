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

package jmbench.tools.stability.tests;

import jmbench.interfaces.BenchmarkMatrix;
import jmbench.interfaces.DetectedException;
import jmbench.interfaces.MatrixProcessorInterface;
import jmbench.matrix.RowMajorMatrix;
import jmbench.tools.OutputError;
import jmbench.tools.stability.StabilityTestBase;


/**
 * Base class for accuracy benchmark.  Several different random matrices are generated and processed.
 * The results are checked and an accuracy metric computed.
 *
 * @author Peter Abeles
 */
public abstract class AccuracyTestBase extends StabilityTestBase {

    protected int minLength;
    protected int maxLength;

    public AccuracyTestBase(long randomSeed,
                            String classFactory, String nameOperation,
                            int totalTrials,
                            int minLength, int maxLength)
    {
        super(randomSeed , classFactory , nameOperation, totalTrials, 0);
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    protected AccuracyTestBase(){}

    @Override
    public void performTest() {
        for( int i = 0; i < totalTrials; i++ ) {
//            System.out.print("Trial "+i+"  ");
            int m = rand.nextInt(maxLength-minLength)+minLength;
            int n = rand.nextInt(maxLength-minLength)+minLength;

            createMatrix(m,n);
            evaluateTestCase();

            saveResults();
        }
    }

    protected void evaluateTestCase() {
        reason = OutputError.NO_ERROR;
        foundResult = Double.NaN;

        RowMajorMatrix inputs[] = createInputs();
  
        BenchmarkMatrix[] inputsB = new BenchmarkMatrix[inputs.length];
        BenchmarkMatrix[] outputB = new BenchmarkMatrix[getNumOutputs()];

        for( int i = 0; i < inputs.length; i++ ) {
            inputsB[i] = factory.convertToLib(inputs[i]);
        }

        MatrixProcessorInterface operation = createAlgorithm();

        if( operation == null ) {
            reason = OutputError.NOT_SUPPORTED;
            return;
        }

        try {
            operation.process(inputsB,outputB,1);
        } catch( DetectedException e ) {
            reason = OutputError.DETECTED_FAILURE;
            return;    
        } catch( Exception e ) {
            addUnexpectedException(e);
            reason = OutputError.UNEXPECTED_EXCEPTION;
            return;
        }

        RowMajorMatrix results[] = new RowMajorMatrix[outputB.length];
        for( int i = 0; i < results.length; i++ )
            results[i] = factory.convertToRowMajor(outputB[i]);

        processResults( inputs , results );
    }

    protected abstract void createMatrix( int m , int n );

    protected abstract RowMajorMatrix[] createInputs();
    
    protected abstract int getNumOutputs();

    protected abstract void processResults(RowMajorMatrix[] inputs, RowMajorMatrix[] results);

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}

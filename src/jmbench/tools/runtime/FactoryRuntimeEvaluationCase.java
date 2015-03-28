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

package jmbench.tools.runtime;

import jmbench.impl.LibraryConfigure;
import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.runtime.generator.*;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Peter Abeles
 */
public class FactoryRuntimeEvaluationCase {
    Class<LibraryConfigure> configure;
    Class<RuntimePerformanceFactory> factory;

    RuntimeBenchmarkConfig config;

    public FactoryRuntimeEvaluationCase( Class<LibraryConfigure> configure ,
                                         Class<RuntimePerformanceFactory> factory ,
                                         RuntimeBenchmarkConfig config ) {
        this.configure = configure;
        this.factory = factory;
        this.config = config;
    }

    public List<RuntimeEvaluationCase> createCases() {

        List<RuntimeEvaluationCase> ret = new ArrayList<RuntimeEvaluationCase>();


        if( config.mult ) ret.add( createMatrixMult(configure,factory));

        if( config.add ) ret.add( createMatrixAdd(configure,factory));

        if( config.transposeSquare ) ret.add( createTransposeSquare(configure,factory));

        if( config.transposeTall ) ret.add( createTransposeTall(configure,factory));

        if( config.transposeWide ) ret.add( createTransposeWide(configure,factory));

        if( config.scale ) ret.add( createScale(configure,factory));

        if( config.det ) ret.add( createDeterminant(configure,factory));

        if( config.invert ) ret.add( createInvert(configure,factory));

        if( config.invertSymmPosDef ) ret.add( createInvertSymmPosDef(configure,factory));

        if( config.svd ) ret.add( createSVD(configure,factory));

        if( config.chol ) ret.add( createCholesky(configure,factory));

        if( config.multTransB ) ret.add( createMultTranB(configure,factory));

        if( config.solveExact ) ret.add( createSolveEq(configure,factory));

        if( config.solveOver ) ret.add( createSolveOver(configure,factory));

        if( config.qr ) ret.add( createQR(configure,factory));

        if( config.lu ) ret.add( createLU(configure,factory));

        if( config.eigSymm ) ret.add( createEigSymm(configure,factory));

        return ret;
    }


    public RuntimeEvaluationCase createMatrixMult( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new MultGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Mult c=a*b","mult",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createMatrixAdd( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new AddGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Add c=a+b","add",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createTransposeSquare( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new TransposeSquareGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Transpose Square: b=a^T","transpose",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createTransposeTall( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new TransposeTallGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Transpose Tall: b=a^T","tranTall",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createTransposeWide( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new TransposeWideGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Transpose Wide: b=a^T","tranWide",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createScale( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new ScaleGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Scale b=alpha*a","scale",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createDeterminant( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new DeterminantGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Determinant","det",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createInvert( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new InvertGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Invert b=inv(a)","invert",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createInvertSymmPosDef( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new InvertSymmPosDefGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Invert Symm b=inv(a)","invertSymmPosDef",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createSVD( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new SvdGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("SVD","svd",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createCholesky( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new CholeskyGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Cholesky","chol",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createMultTranB( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new MultTranBGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Mult c=a*b^T","multTransB",matDimen,configure,
                factory,generator);
    }

    /**
     * The number of unknowns matches the number of equations.
     */
    public RuntimeEvaluationCase createSolveEq( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new SolveEqGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Solve m=n","solveExact",matDimen,configure,
                factory,generator);
    }

    /**
     * See how well it can solve an overdetermined system.
     */
    public RuntimeEvaluationCase createSolveOver( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new SolveOverGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Solve m>n","solveOver",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createQR( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new QrGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("QR","qr",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createLU( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new LuGenerator();

        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("LU","lu",matDimen,configure,
                factory,generator);
    }

    public RuntimeEvaluationCase createEigSymm( Class<LibraryConfigure> configure , Class<RuntimePerformanceFactory> factory ) {

        InputOutputGenerator generator = new EigSymmGenerator();
        int matDimen[] = createDimenList(config.minMatrixSize, config.maxMatrixSize);

        return new RuntimeEvaluationCase("Eigen for Symm Matrices","eigSymm",matDimen,configure,
                factory,generator);
    }

    private static int[] createDimenList( int min , int max ) {
        List<Integer> a = new ArrayList<Integer>();

        int val = min;

        int dec = 1;

        while( (val / (dec*10)) >= 1 ) {
            dec *= 10;
        }

        a.add(val);

        while(  val < max ) {
            if( val < 5 ) {
                val++;
            } else {
                int w = val/dec;
                switch( w ) {
                    case 1:
                        val = 2*dec;
                        break;

                    case 2:
                        val = 5*dec;
                        break;

                    case 5:
                        val = 10*dec;
                        dec *= 10;
                        break;

                    default:
                        if( w < 5 ) {
                            val = 5*dec;
                        } else {
                            val = 10*dec;
                            dec *= 10;
                        }
                }
            }

            a.add(val);
        }

        if( a.get(a.size()-1) != max ) {
            a.remove(a.size()-1);
            a.add(max);
        }

        int ret[] = new int[ a.size() ];
        for( int i = 0; i < ret.length; i++ ) {
            ret[i] = a.get(i);
        }
        return ret;
    }

}

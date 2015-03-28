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

import java.io.Serializable;


/**
 * @author Peter Abeles
 */
public class RuntimeEvaluationCase implements Serializable {
    // the operation that this is evaluating
    private String opName;
    // the different matrix sizes that can be evaluated
    private int dimens[];
    // configures the library's runtime environment
    private Class<LibraryConfigure> classConfigure;
    // used to create perform tests
    private Class<RuntimePerformanceFactory> classFactory;
    // list of algorithms it it can run
    private String nameAlgorithm;
    // what creates the matrices it processes
    private InputOutputGenerator generator;

    private volatile RuntimeEvaluationTest theTest = new RuntimeEvaluationTest();

    public RuntimeEvaluationCase( String opName , String nameAlgorithm , int dimens[] ,
                                  Class<LibraryConfigure> classConfigure ,
                                  Class<RuntimePerformanceFactory> classFactory ,
                                  InputOutputGenerator generator )
    {
        this.opName = opName;
        this.dimens = dimens.clone();
        this.classConfigure = classConfigure;
        this.classFactory = classFactory;
        this.nameAlgorithm = nameAlgorithm;
        this.generator = generator;
    }

    public RuntimeEvaluationCase(){}


    public RuntimeEvaluationTest createTest( int numTrials , int dimenIndex , long duration , long maxRuntime ,
                                             boolean sanityCheck ) {
        theTest.setNumTrials(numTrials);
        theTest.setDimen(dimens[dimenIndex]);
        theTest.setNameAlgorithm(nameAlgorithm);
        theTest.setClassConfigure(classConfigure);
        theTest.setClassFactory(classFactory);
        theTest.setGenerator(generator);
        theTest.setGoalRuntime(duration);
        theTest.setMaximumRuntime(maxRuntime);
        theTest.setSanityCheck(sanityCheck);

        return theTest;
    }

    public String getNameAlgorithm() {
        return nameAlgorithm;
    }

    public void setNameAlgorithm(String nameAlgorithm) {
        this.nameAlgorithm = nameAlgorithm;
    }

    public Class<RuntimePerformanceFactory> getClassFactory() {
        return classFactory;
    }

    public void setClassFactory(Class<RuntimePerformanceFactory> classFactory) {
        this.classFactory = classFactory;
    }

    public String getOpName() {
        return opName;
    }

    public void setOpName(String opName) {
        this.opName = opName;
    }

    public int[] getDimens() {
        return dimens;
    }

    public void setDimens(int[] dimens) {
        this.dimens = dimens;
    }

    public InputOutputGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(InputOutputGenerator generator) {
        this.generator = generator;
    }

    public Class<LibraryConfigure> getClassConfigure() {
        return classConfigure;
    }

    public void setClassConfigure(Class<LibraryConfigure> classConfigure) {
        this.classConfigure = classConfigure;
    }
}

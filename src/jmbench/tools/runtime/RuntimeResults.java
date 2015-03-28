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

import java.io.Serializable;


/**
 * Collection of all the runtime performance results for a single operation in a library across
 * all matrix sizes.
 *
 * @author Peter Abeles
 */
public class RuntimeResults implements Serializable {
    // operation these results are from
    public String opName;
    // The library's name
    public String libraryName;
    // size of the input matrices
    public int matDimen[];
    // results by matrix size
    public RuntimeEvaluationMetrics metrics[];

    // if true it finished computing all the results for this operation
    public boolean complete;

    public RuntimeResults( String opName , String libraryName ,
                             int matDimen[] ,  RuntimeEvaluationMetrics metrics[] )
    {
        this.opName = opName;
        this.libraryName = libraryName;

        this.metrics = metrics;
        this.matDimen = matDimen;
    }

    public RuntimeResults(){}

    public String getOpName() {
        return opName;
    }

    public void setOpName(String opName) {
        this.opName = opName;
    }

    public int[] getMatDimen() {
        return matDimen;
    }

    public void setMatDimen(int[] matDimen) {
        this.matDimen = matDimen;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public RuntimeEvaluationMetrics[] getMetrics() {
        return metrics;
    }

    public void setMetrics(RuntimeEvaluationMetrics[] metrics) {
        this.metrics = metrics;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}

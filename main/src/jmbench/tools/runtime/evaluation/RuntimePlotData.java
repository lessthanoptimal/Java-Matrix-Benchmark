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

package jmbench.tools.runtime.evaluation;

import java.util.ArrayList;
import java.util.List;


/**
 * Data structure that contains information about a runtime performance plot.
 *
 * @author Peter Abeles
 */
public class RuntimePlotData {

    // The name of the plot.  Typically this is the operation being evaluated
    public String plotName;
    // size of the matrices evaluated (x-axis)
    public int matrixSize[];

    // results for each library
    public List<SourceResults> libraries = new ArrayList<SourceResults>();

    public RuntimePlotData( int matrixSize[] ) {
        this.matrixSize = matrixSize;
    }

    /**
     * Returns the best score across all libraries for this matrix size
     *
     * @param matrixIndex which matrix.
     * @return the best score
     */
    public double findBest( int matrixIndex ) {
        double best = -1;

        for( SourceResults s : libraries ) {
            double v = s.getResult(matrixIndex);

            if( !Double.isNaN(v) && best < v ) {
                best = v;
            }
        }

        return best;
    }

    /**
     * Returns how many different matrix sizes have results.
     */
    public int getNumMatrices() {
        int max = 0;

        for( SourceResults s : libraries ) {
            for( int i = max; i < matrixSize.length; i++ ) {
                if( Double.isNaN(s.getResult(i)))
                    break;
                max = i;
            }
        }

        return max;
    }

    public void addLibrary( String label , double[] results , int plotLineType ) {
        SourceResults s = new SourceResults();
        s.plotLineType = plotLineType;
        s.label = label;
        s.results = results;

        this.libraries.add(s);
    }

    public int findLibrary(String refLib) {
        int index = 0;
        for( SourceResults s : libraries) {
            if( s.label.compareTo(refLib) == 0)
                return index;
            index++;
        }

        return -1;
    }

    public static class SourceResults
    {
        int plotLineType;
        String label;
        double[] results;

        public double getResult( int index ) {
            if( results.length <= index )
                return Double.NaN;

            return results[index];
        }
    }
}

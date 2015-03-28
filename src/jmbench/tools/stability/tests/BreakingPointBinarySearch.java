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

package jmbench.tools.stability.tests;


/**
 * @author Peter Abeles
 */
public class BreakingPointBinarySearch {

    Processor processor;

    public BreakingPointBinarySearch( Processor processor ) {
        this.processor = processor;
    }

    public int findCriticalPoint( int lower , int upper ) {
        int testPoint = 0;

//        int numIterations = 0;
        while( true ) {
//            numIterations++;
            if( processor.check(testPoint) ) {
                lower = testPoint;
            } else {
                upper = testPoint;
            }

//            System.out.println("testPoint "+testPoint);
            if( upper == -1 ) {
                testPoint = (lower+1)*2;
            } else if( lower == -1 ) {
                break;
            } else if( upper-lower <= 1 ) {
                break;
            } else {
                testPoint = (upper+lower)/2;
            }
        }

//        System.out.println("Iterations = "+numIterations);
        return upper;
    }

    public interface Processor
    {
        boolean check( int testPoint );
    }
}

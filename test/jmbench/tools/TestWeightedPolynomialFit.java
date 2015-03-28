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

package jmbench.tools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * @author Peter Abeles
 */
public class TestWeightedPolynomialFit {

    @Test
    public void linearPerfectUnweighted() {
        int N = 10;
        double slope = 2;
        double offset  = 1.5;

        WeightedPolynomialFit alg = new WeightedPolynomialFit(3);

        for( int i = 0; i < N; i++ ) {
            alg.add(1,i,offset+i*slope);
        }

        double[] coef = alg.compute();

        assertEquals(offset,coef[0],1e-8);
        assertEquals(slope,coef[1],1e-8);
    }

    @Test
    public void linearPerfectWeighted() {
        int N = 10;
        double slope = 2;
        double offset  = 1.5;

        WeightedPolynomialFit alg = new WeightedPolynomialFit(3);

        for( int i = 0; i < N; i++ ) {
            alg.add(Math.cos(i)+0.1,i,offset+i*slope);
        }

        double[] coef = alg.compute();

        assertEquals(offset,coef[0],1e-8);
        assertEquals(slope,coef[1],1e-8);
    }
}

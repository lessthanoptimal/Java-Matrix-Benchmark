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

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.util.ArrayList;
import java.util.List;


/**
 * Computes a best fit polynomial for the weighted set of data
 *
 * @author Peter Abeles
 */
public class WeightedPolynomialFit {

    int dof;
    List<Data> data = new ArrayList<Data>();


    public WeightedPolynomialFit( int dof ) {
        this.dof = dof;
    }

    public void add( double weight , double x , double y ) {
        Data d = new Data();
        d.w = weight;
        d.x = x;
        d.y = y;

        data.add(d);
    }

    // not totally sure the weighting is done right
    public double[] compute() {
        double total = 0;

        for( Data d : data ) {
            total += d.w;
        }

        DenseMatrix64F A = new DenseMatrix64F(data.size(),dof);
        DenseMatrix64F x = new DenseMatrix64F(dof,1);
        DenseMatrix64F y = new DenseMatrix64F(data.size(),1);

        for( int i = 0; i < data.size(); i++ ) {
            Data d = data.get(i);

            double w = d.w/total;

            A.set(i,0,w);
            for( int j = 1; j < dof; j++ ) {
                double val = Math.pow(d.x,j)*w;
                A.set(i,j,val);
            }

            y.set(i,0,d.y*w);
        }

        CommonOps.solve(A,y,x);

        return x.data;
    }

    private static class Data
    {
        double w;
        double x;
        double y;
    }
}

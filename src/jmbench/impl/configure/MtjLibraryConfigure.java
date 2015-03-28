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

package jmbench.impl.configure;

import com.github.fommil.netlib.ARPACK;
import com.github.fommil.netlib.BLAS;
import com.github.fommil.netlib.LAPACK;
import jmbench.impl.LibraryConfigure;

/**
 * Configuration to force MTJ to use pure java code
 *
 * @author Peter Abeles
 */
public class MtjLibraryConfigure implements LibraryConfigure {
    @Override
    public void runtimeConfigure() {

        System.setProperty("java.util.logging.config.file","logging.properties");
        System.setProperty("com.github.fommil.netlib.BLAS","com.github.fommil.netlib.F2jBLAS");
        System.setProperty("com.github.fommil.netlib.LAPACK","com.github.fommil.netlib.F2jLAPACK");
        System.setProperty("com.github.fommil.netlib.ARPACK","com.github.fommil.netlib.F2jARPACK");

        BLAS.getInstance();
        LAPACK.getInstance();
        ARPACK.getInstance();
    }

    @Override
    public String[] getJreFlags() {
        return new String[0];
    }
}

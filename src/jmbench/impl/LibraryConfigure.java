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

package jmbench.impl;

import java.io.Serializable;

/**
 * Interface which allows the library to configure itself at runtime and how the JRE operates
 *
 * @author Peter Abeles
 */
public interface LibraryConfigure extends Serializable {

    /**
     * Called once when the benchmark thread starts to configure the library at runtime
     */
    public void runtimeConfigure();

    /**
     * Returns runtime flags which are to be passed to the JRE
     */
    public String[] getJreFlags();
}

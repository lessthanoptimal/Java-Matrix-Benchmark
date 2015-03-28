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

import jmbench.interfaces.RuntimePerformanceFactory;

/**
 * Contains all the information needed to run a benchmark for a particular library.
 *
 * @author Peter Abeles
 */
public class LibraryDescription {
    /** Information on the library's name and location */
    public LibraryLocation location;
    /** Specifies how to configure the library at runtime */
    public Class<LibraryConfigure> configure;
    /** Creates functions for runtime benchmark */
    public Class<RuntimePerformanceFactory> factoryRuntime;
    /** Used to extract the libraries version */
    public Class<LibraryVersion> version;


}

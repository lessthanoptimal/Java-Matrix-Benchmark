/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
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

package jmbench.misc;

import jmbench.interfaces.RuntimePerformanceFactory;

/**
 * Slave which is run in a new JVM.  Loads a LibraryStringInfo specified in the first arguments, creates a factory,
 * then prints its version string to standard out.
 *
 * @author Peter Abeles
 */
public class VersionSlave {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        Class factoryClass = Class.forName(args[0]);
        RuntimePerformanceFactory factory = (RuntimePerformanceFactory)factoryClass.newInstance();

        System.out.println(factory.getLibraryVersion());
    }
}

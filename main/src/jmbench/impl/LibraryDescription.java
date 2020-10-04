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

package jmbench.impl;

import jmbench.tools.BenchmarkConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class LibraryDescription implements Comparable<LibraryDescription> {
    public LibraryStringInfo info;
    public String directory;

    public List<String> listOfJarFilePaths () {
        File files[] = new File("external/"+directory+"/"+BenchmarkConstants.RUNTIME_LIBS).listFiles();
        List<String> jars = new ArrayList<String>();
        for( File f : files ) {
            if( f.getName().endsWith(".jar")) {
                jars.add(f.getAbsolutePath());
            }
        }
        return jars;
    }


    @Override
    public int compareTo(LibraryDescription o) {
        return directory.compareTo(o.directory);
    }
}

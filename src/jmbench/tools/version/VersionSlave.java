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

package jmbench.tools.version;

import jmbench.impl.LibraryVersion;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

/**
 * Prints version information to a file
 *
 * @author Peter Abeles
 */
public class VersionSlave implements Serializable {


    public static void main( String args[] )
            throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, FileNotFoundException
    {
        Class<LibraryVersion> classVersion = (Class)Class.forName(args[0]);
        String libraryName = args[1];

        LibraryVersion version = classVersion.newInstance();

        PrintStream out = new PrintStream(new FileOutputStream(libraryName+"version.txt"));

        out.println("version: " + version.getVersionString());
        out.println("release date: " + version.getReleaseDate());

        out.close();
    }
}

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

import jmbench.impl.LibraryDescription;
import jmbench.impl.LibraryManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Gets the version of a library by launching a separate JVM to avoid corruption
 *
 * @author Peter Abeles
 */
public class LibraryVersionExtractor extends JavaRuntimeLauncher {

    PrintStream errorStream = System.err;

    LibraryDescription library;

    // string which contains the version
    String version;

    public LibraryVersionExtractor(LibraryDescription library) {
        super(library.listOfJarFilePaths());

        this.library = library;
    }

    /**
     * Returns the version string for the library.  If it fails then an error string is returned and stuff printed
     * to the error stream
     * @return Version string
     */
    public String getVersion() {
        version = "";
        switch( launch(VersionSlave.class,library.info.getFactory()) ) {
            case NORMAL:
                return version;
            default:
                errorStream.println("Failed to get version information for "+library.directory);
                return "Version Failed";
        }
    }

    /**
     * Feed standard out into a string.
     */
    @Override
    protected void printToStream(BufferedReader input, PrintStream stream) throws IOException {
        while( input.ready() ) {
            int val = input.read();
            if( val < 0 ) break;

            version += new String(Character.toChars(val));
        }
    }

    public void setErrorStream(PrintStream errorStream) {
        this.errorStream = errorStream;
    }

    public static void main(String[] args) {
        LibraryManager manager = new LibraryManager();
        LibraryDescription target = manager.lookup("ejml");
        LibraryVersionExtractor app = new LibraryVersionExtractor(target);
        System.out.println(target.info.getNameFull()+": version = " + app.getVersion());
    }
}

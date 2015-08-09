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

import jmbench.tools.MiscTools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Examines the external directory and reads all the libraries.  Then configures them.
 *
 * @author Peter Abeles
 */
public class LibraryManager {

    List<LibraryDescription> all;


    /**
     * Loads all the libraries in the external directory.
     */
    public LibraryManager() {
        all = new ArrayList<LibraryDescription>();

        File dirExternal = new File("external");

        if( !dirExternal.exists() )
            throw new RuntimeException("Can't find external directory");
        if( !dirExternal.isDirectory())
            throw new RuntimeException("external is not a directory!");

        File[] files = dirExternal.listFiles();

        for( File f : files ) {
            if( !f.isDirectory() )
                continue;

            File settings = new File(f,"TestSetInfo.txt");
            if( settings.exists() ) {
                all.addAll(parseDescription(settings));
            }
        }

        assignPlotStuff();
    }

    private void assignPlotStuff() {
        // sort into alphabetical order
    }

    protected List<LibraryDescription> parseDescription(File file) {
        List<LibraryStringInfo> listString = MiscTools.loadLibraryInfo(file);

        List<LibraryDescription> out = new ArrayList<LibraryDescription>();

        for( LibraryStringInfo info : listString ) {
            LibraryDescription desc = new LibraryDescription();

            desc.directory = file.getParent();
            desc.directory = desc.directory.substring("external/".length(),desc.directory.length());
            desc.info = info;

            out.add( desc );
        }

        return out;
    }

    /**
     * Reads a list of libraries that are to be tested from a file and loads their information
     *
     * @param targetFile
     */
    public List<LibraryDescription> loadTargetFile( String targetFile ) {
        return null;
    }

    public LibraryDescription lookup( String name ) {
        for (int i = 0; i < all.size(); i++) {
            LibraryStringInfo l = all.get(i).info;

            if( l.nameFull.compareToIgnoreCase(name) == 0 )
                return all.get(i);
            else if( l.nameShort.compareToIgnoreCase(name) == 0 )
                return all.get(i);
            else if( l.namePlot.compareToIgnoreCase(name) == 0 )
                return all.get(i);
        }
        return null;
    }

    public void printAllNames() {
        for (int i = 0; i < all.size(); i++) {
            LibraryStringInfo l = all.get(i).info;

            System.out.println("Library "+l.nameFull+"  in directory "+all.get(i).directory);
        }
    }

    public List<LibraryDescription> getDefaults() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("external/DefaultTestSet.txt"));
            String line = reader.readLine();

            List<LibraryDescription> defaults = new ArrayList<>();

            while( line != null ) {
                LibraryDescription found = lookup(line);
                if( found != null ) {
                    defaults.add(found);
                } else {
                    throw new RuntimeException("Couldn't find default! "+line );
                }

                line = reader.readLine();
            }

            return defaults;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public List<LibraryDescription> getAll() {
        return all;
    }
}

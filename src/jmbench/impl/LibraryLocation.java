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

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * Information on a linear algebra library, such as; its name, where its located, and how to load it.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class LibraryLocation implements Serializable {
    public static final LibraryLocation EJML = new LibraryLocation("EJML","ejml","ejml", false,0);
    public static final LibraryLocation JAMA = new LibraryLocation("JAMA","jama","jama", false, 1);
    public static final LibraryLocation MTJ = new LibraryLocation("MTJ","mtj","mtj", false, 2);
    public static final LibraryLocation MTJ_NATIVE = new LibraryLocation("MTJ-N","mtj","mtj-native", true, 3);
    public static final LibraryLocation SEJML = new LibraryLocation("SEJML","sejml","sejml", false, 4);
    public static final LibraryLocation CM = new LibraryLocation("CommMath","commons-math", "commons-math", false, 5);
    public static final LibraryLocation OJALGO = new LibraryLocation("ojAlgo","ojalgo","ojalgo", false, 6);
    public static final LibraryLocation COLT = new LibraryLocation("Colt","colt","colt", false, 7);
    public static final LibraryLocation PCOLT = new LibraryLocation("PColt","parallelcolt","parallelcolt", false, 8);
    public static final LibraryLocation JBLAS = new LibraryLocation("JBLAS","jblas","jblas", true, 9);
    public static final LibraryLocation UJMP = new LibraryLocation("UJMP","ujmp","ujmp", false, 10);
    public static final LibraryLocation UJMP_NATIVE = new LibraryLocation("UJMP-N","ujmp","ujmp-native", true, 11);
    public static final LibraryLocation LA4J = new LibraryLocation("la4j","la4j","la4j", false,12);

    public String plotName;
    // directory that it loads its libraries from
    public String libraryDirName;
    // directory the results are saved into
    public String saveDirName;
    // if the library is native or not
    public boolean nativeCode;

    // when plotted what color and stroke should be used
    public int plotLineType;

    public LibraryLocation(String plotName, String libraryDirName, String saveDirName,
                           boolean nativeCode, int plotLineType)
    {
        this.plotName = plotName;
        this.libraryDirName = libraryDirName;
        this.saveDirName = saveDirName;
        this.nativeCode = nativeCode;
        this.plotLineType = plotLineType;
    }

    public LibraryLocation() {

    }

    /**
     * Returns a list of absolute paths to the library's jar files.
     */
    public List<String> listOfJarFilePaths() {
        List<String> jarNames = new ArrayList<String>();

        File rootDir = new File("lib/"+ libraryDirName);

        File files[] = rootDir.listFiles();
        if( files == null)
            return null;

        for( File f : files ) {
            if( !f.isFile() ) continue;

            String n = f.getName();
            if( n.contains("-doc.") || n.contains("-src."))
                continue;

            if( n.contains(".jar") || n.contains(".zip")) {
                jarNames.add(rootDir.getAbsolutePath()+"/"+n);
            }
        }
        return jarNames;
    }

    public static LibraryLocation lookup( String libraryPlotName ) {
        Field[] fields = LibraryLocation.class.getFields();

        for( Field f : fields ) {
            if( LibraryLocation.class.isAssignableFrom(f.getType())) {
                try {
                    LibraryLocation l = (LibraryLocation)f.get(null);

                    if( l.plotName.compareToIgnoreCase(libraryPlotName) == 0 )
                        return l;
                } catch (IllegalAccessException e) {

                }
            }
        }

        return null;
    }

    public String getPlotName() {
        return plotName;
    }

    public void setPlotName(String plotName) {
        this.plotName = plotName;
    }

    public String getLibraryDirName() {
        return libraryDirName;
    }

    public void setLibraryDirName(String libraryDirName) {
        this.libraryDirName = libraryDirName;
    }

    public int getPlotLineType() {
        return plotLineType;
    }

    public void setPlotLineType(int plotLineType) {
        this.plotLineType = plotLineType;
    }

    public boolean isNativeCode() {
        return nativeCode;
    }

    public void setNativeCode(boolean nativeCode) {
        this.nativeCode = nativeCode;
    }

    public String getSaveDirName() {
        return saveDirName;
    }

    public void setSaveDirName(String saveDirName) {
        this.saveDirName = saveDirName;
    }
}

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

package jmatbench.ujmp;


import jmbench.impl.LibraryStringInfo;
import jmbench.tools.MiscTools;

import java.io.IOException;

/**
 * @author Peter Abeles
 */
public class UjmpCreateTestSetInfo {
    public static void main(String[] args) throws IOException {
        LibraryStringInfo infoA = new LibraryStringInfo();
        infoA.factory = UjmpAlgorithmFactory.class.getName();
        infoA.nameFull = "Universal Java Matrix Package";
        infoA.nameShort = "UJMP";
        infoA.namePlot = "UJMP";

        LibraryStringInfo infoB = new LibraryStringInfo();
        infoB.factory = UjmpAlgorithmFactoryNative.class.getName();
        infoB.nameFull = "Universal Java Matrix Package - Native";
        infoB.nameShort = "UJMP-N";
        infoB.namePlot = "UJMP-N";

        MiscTools.saveLibraryInfo("ujmp", infoA, infoB);
    }
}

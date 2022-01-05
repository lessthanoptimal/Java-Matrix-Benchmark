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

package jmbench.nd4j;


import jmbench.impl.LibraryStringInfo;
import jmbench.tools.MiscTools;

import java.io.IOException;

/**
 * @author Peter Abeles
 */
public class Nd4JCreateTestSetInfo {
    public static void main(String[] args) throws IOException {
        LibraryStringInfo infoA = new LibraryStringInfo();
        infoA.factory = Nd4JAlgorithmFactory.class.getName();
        infoA.nameFull = "ND4J";
        infoA.nameShort = "ND4J";
        infoA.namePlot = "ND4J";

        MiscTools.saveLibraryInfo("nd4j", infoA);
    }
}

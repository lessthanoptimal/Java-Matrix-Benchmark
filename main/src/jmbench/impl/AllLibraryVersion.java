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

import org.ejml.UtilEjml;
import org.la4j.LinearAlgebra;
import org.ojalgo.OjAlgoUtils;

/**
 *
 * A single place containing version information on all the supported libraries
 *
 * @author Peter Abeles
 */
public class AllLibraryVersion {

    public static class Colt implements LibraryVersion {

        @Override
        public String getVersionString() {
            return "1.2";
        }

        @Override
        public String getReleaseDate() {
            return "";
        }
    }

    public static class COMMONS implements LibraryVersion {

        @Override
        public String getVersionString() {
            return "3.2";
        }

        @Override
        public String getReleaseDate() {
            return "2013-04-03";
        }
    }

    public static class PColt implements LibraryVersion {

        @Override
        public String getVersionString() {
            return "0.9.4";
        }

        @Override
        public String getReleaseDate() {
            return "2010-03-20";
        }
    }

    public static class JBLAS implements LibraryVersion {

        @Override
        public String getVersionString() {
            return "1.2.3";
        }

        @Override
        public String getReleaseDate() {
            return "2013-02-13";
        }
    }

    public static class JAMA implements LibraryVersion {

        @Override
        public String getVersionString() {
            return "1.0.3";
        }

        @Override
        public String getReleaseDate() {
            return "2012-11-09";
        }
    }

    public static class EJML implements LibraryVersion {

        @Override
        public String getVersionString() {
            return UtilEjml.VERSION;
        }

        @Override
        public String getReleaseDate() {
            return "2013-06-21";
        }
    }

    public static class MTJ implements LibraryVersion {

        @Override
        public String getVersionString() {
            return "1.0";
        }

        @Override
        public String getReleaseDate() {
            return "2013-10";
        }
    }


    public static class OJALGO implements LibraryVersion {

        @Override
        public String getVersionString() {
            return OjAlgoUtils.getVersion();
        }

        @Override
        public String getReleaseDate() {
            return OjAlgoUtils.getDate();
        }
    }

    public static class UJMP implements LibraryVersion {

        @Override
        public String getVersionString() {
            return org.ujmp.core.UJMP.UJMPVERSION;
        }

        @Override
        public String getReleaseDate() {
            return "2010-06-22";
        }
    }

    public static class LA4J implements LibraryVersion {

        @Override
        public String getVersionString() {
            return LinearAlgebra.VERSION;
        }

        @Override
        public String getReleaseDate() {
            return LinearAlgebra.DATE;
        }
    }

}

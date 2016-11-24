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

package jmbench.tools.runtime;

import jmbench.interfaces.RuntimePerformanceFactory;
import jmbench.tools.stability.UtilXmlSerialization;

import java.io.Serializable;

/**
 * Contains information about the library which was determined at runtime
 *
 * @author Peter Abeles
 */
public class LibraryRuntimeInfo implements Serializable {
    public String version;
    public boolean isNative;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isNative() {
        return isNative;
    }

    public void setNative(boolean aNative) {
        this.isNative = aNative;
    }

    public static void main(String[] args) {
        String factoryName = args[0];
        String path = args[1];

        try {
            RuntimePerformanceFactory factory = (RuntimePerformanceFactory)Class.forName(factoryName).newInstance();

            System.out.println("LibraryRuntimeInfo factory "+factoryName);
            System.out.println("                   native "+factory.isNative());


            LibraryRuntimeInfo info = new LibraryRuntimeInfo();
            info.isNative = factory.isNative();
            info.version = factory.getLibraryVersion();

            UtilXmlSerialization.serializeXml(info,path);
        } catch (Exception e) {
            System.err.println("Reflection failed to load "+factoryName);
            System.exit(-1);
        }
    }
}

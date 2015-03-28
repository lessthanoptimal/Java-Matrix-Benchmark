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

import jmbench.impl.configure.*;
import jmbench.impl.runtime.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates descriptions of each library for testing purposes
 *
 * @author Peter Abeles
 */
public class FactoryLibraryDescriptions {

    public static List<LibraryDescription> createDefault() {
        List<LibraryDescription> list = new ArrayList<LibraryDescription>();

        list.add(createColt());
        list.add(createCommonsMath());
        list.add(createEJML());
        list.add(createJAMA());
        list.add(createJBlas());
        list.add(createLa4j());
        list.add(createMtj());
        list.add(createMtj_Native());
        list.add(createOjAlgo());
        list.add(createPColt());
//        list.add(createSejml());
        list.add(createUJMP());
//        list.add(createUJMP_Native()); // The last version posted online does not contain native binaries

        return list;
    }

    public static LibraryDescription find( String name ) {
        List<LibraryDescription> list = createDefault();

        for( LibraryDescription l : list ) {
            if( l.location.getPlotName().compareTo(name) == 0  )
                return l;
        }
        return null;
    }

    public static void printAllNames() {
        List<LibraryDescription> list = createDefault();

        for( LibraryDescription l : list ) {
            System.out.println(l.location.getPlotName());
        }
    }

    public static LibraryDescription createColt() {
        LibraryDescription ret = new LibraryDescription();

        ret.configure = (Class)DoNothingSpecialConfigure.class;
        ret.factoryRuntime = (Class)ColtAlgorithmFactory.class;
        ret.location = LibraryLocation.COLT;
        ret.version = (Class)AllLibraryVersion.Colt.class;

        return ret;
    }

    public static LibraryDescription createCommonsMath() {
        LibraryDescription ret = new LibraryDescription();

        ret.configure = (Class)DoNothingSpecialConfigure.class;
        ret.factoryRuntime = (Class) CommonsMathAlgorithmFactory.class;
        ret.location = LibraryLocation.CM;
        ret.version = (Class)AllLibraryVersion.COMMONS.class;

        return ret;
    }

    public static LibraryDescription createEJML() {
        LibraryDescription ret = new LibraryDescription();

        ret.configure = (Class)DoNothingSpecialConfigure.class;
        ret.factoryRuntime = (Class) EjmlAlgorithmFactory.class;
        ret.location = LibraryLocation.EJML;
        ret.version = (Class)AllLibraryVersion.EJML.class;

        return ret;
    }

    public static LibraryDescription createJAMA() {
        LibraryDescription ret = new LibraryDescription();

        ret.configure = (Class)DoNothingSpecialConfigure.class;
        ret.factoryRuntime = (Class) JamaAlgorithmFactory.class;
        ret.location = LibraryLocation.JAMA;
        ret.version = (Class)AllLibraryVersion.JAMA.class;

        return ret;
    }

    public static LibraryDescription createJBlas() {
        LibraryDescription ret = new LibraryDescription();

        ret.configure = (Class)DoNothingSpecialConfigure.class;
        ret.factoryRuntime = (Class)JBlasAlgorithmFactory.class;
        ret.location = LibraryLocation.JBLAS;
        ret.version = (Class)AllLibraryVersion.JBLAS.class;

        return ret;
    }

    public static LibraryDescription createLa4j() {
        LibraryDescription ret = new LibraryDescription();

        ret.configure = (Class)DoNothingSpecialConfigure.class;
        ret.factoryRuntime = (Class)La4jAlgorithmFactory.class;
        ret.location = LibraryLocation.LA4J;
        ret.version = (Class)AllLibraryVersion.LA4J.class;

        return ret;
    }

    public static LibraryDescription createMtj() {
        LibraryDescription ret = new LibraryDescription();

        ret.configure = (Class)MtjLibraryConfigure.class;
        ret.factoryRuntime = (Class)MtjAlgorithmFactory.class;
        ret.location = LibraryLocation.MTJ;
        ret.version = (Class)AllLibraryVersion.MTJ.class;

        return ret;
    }

    public static LibraryDescription createMtj_Native() {
        LibraryDescription ret = new LibraryDescription();

        ret.configure = (Class)MtjNativeLibraryConfigure.class;
        ret.factoryRuntime = (Class)MtjAlgorithmFactory.class;
        ret.location = LibraryLocation.MTJ_NATIVE;
        ret.version = (Class)AllLibraryVersion.MTJ.class;

        return ret;
    }

    public static LibraryDescription createOjAlgo() {
        LibraryDescription ret = new LibraryDescription();

        ret.configure = (Class)DoNothingSpecialConfigure.class;
        ret.factoryRuntime = (Class)OjAlgoAlgorithmFactory.class;
        ret.location = LibraryLocation.OJALGO;
        ret.version = (Class)AllLibraryVersion.OJALGO.class;

        return ret;
    }

    public static LibraryDescription createPColt() {
        LibraryDescription ret = new LibraryDescription();

        ret.configure = (Class)DoNothingSpecialConfigure.class;
        ret.factoryRuntime = (Class)PColtAlgorithmFactory.class;
        ret.location = LibraryLocation.PCOLT;
        ret.version = (Class)AllLibraryVersion.PColt.class;

        return ret;
    }

    public static LibraryDescription createSejml() {
        LibraryDescription ret = new LibraryDescription();

        ret.configure = (Class)DoNothingSpecialConfigure.class;
        ret.factoryRuntime = (Class)SejmlAlgorithmFactory.class;
        ret.location = LibraryLocation.SEJML;
        ret.version = (Class)AllLibraryVersion.EJML.class;

        return ret;
    }

    public static LibraryDescription createUJMP() {
        LibraryDescription ret = new LibraryDescription();

        ret.configure = (Class)UjmpLibraryConfigure.class;
        ret.factoryRuntime = (Class)UjmpAlgorithmFactory.class;
        ret.location = LibraryLocation.UJMP;
        ret.version = (Class)AllLibraryVersion.UJMP.class;

        return ret;
    }

    public static LibraryDescription createUJMP_Native() {
        LibraryDescription ret = new LibraryDescription();

        ret.configure = (Class)UjmpNativeLibraryConfigure.class;
        ret.factoryRuntime = (Class)UjmpAlgorithmFactory.class;
        ret.location = LibraryLocation.UJMP_NATIVE;
        ret.version = (Class)AllLibraryVersion.UJMP.class;

        return ret;
    }
}

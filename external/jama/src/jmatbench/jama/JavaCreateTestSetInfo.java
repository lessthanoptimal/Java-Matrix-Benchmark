package jmatbench.jama;


import jmbench.impl.LibraryStringInfo;
import jmbench.tools.MiscTools;

import java.io.IOException;

/**
 * @author Peter Abeles
 */
public class JavaCreateTestSetInfo {
    public static void main(String[] args) throws IOException {
        LibraryStringInfo info = new LibraryStringInfo();
        info.factory = JamaAlgorithmFactory.class.getName();
        info.nameFull = "Jama";
        info.nameShort = "Jama";
        info.namePlot = "Jama";

        MiscTools.saveLibraryInfo("jama", info);
    }
}

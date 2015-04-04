package jmatbench.colt;

import jmbench.libraries.LibraryStringInfo;
import jmbench.libraries.LibraryTools;

import java.io.IOException;

/**
 * @author Peter Abeles
 */
public class ColtCreateTestSetInfo {
    public static void main(String[] args) throws IOException {
        LibraryStringInfo info = new LibraryStringInfo();
        info.factory = ColtAlgorithmFactory.class.getName();
        info.nameFull = "Colt";
        info.nameShort = "Colt";
        info.namePlot = "Colt";

        LibraryTools.saveTests("colt",info);
    }
}

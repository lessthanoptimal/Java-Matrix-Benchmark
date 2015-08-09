package jmatbench.colt;

import jmbench.impl.DoNothingSpecialConfigure;
import jmbench.impl.LibraryStringInfo;
import jmbench.tools.MiscTools;

import java.io.IOException;

/**
 * @author Peter Abeles
 */
public class ColtCreateTestSetFile {
    public static void main(String[] args) throws IOException {
        LibraryStringInfo info = new LibraryStringInfo();
        info.factory = ColtAlgorithmFactory.class.getName();
        info.configure = DoNothingSpecialConfigure.class.getName();
        info.nameFull = "Colt";
        info.nameShort = "Colt";
        info.namePlot = "Colt";

        MiscTools.saveTests(info);
    }
}

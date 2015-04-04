package jmatbench.commonsmath;

import jmbench.libraries.LibraryStringInfo;
import jmbench.libraries.LibraryTools;

import java.io.IOException;

/**
 * @author Peter Abeles
 */
public class CommonsMathCreateTestSetInfo {
    public static void main(String[] args) throws IOException {
        LibraryStringInfo info = new LibraryStringInfo();
        info.factory = CommonsMathAlgorithmFactory.class.getName();
        info.nameFull = "Commons-Math";
        info.nameShort = "Commons";
        info.namePlot = "Commons";

        LibraryTools.saveTests("commons-math",info);
    }
}

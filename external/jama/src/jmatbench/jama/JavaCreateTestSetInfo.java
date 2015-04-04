package jmatbench.jama;

import jmbench.libraries.LibraryStringInfo;
import jmbench.libraries.LibraryTools;

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

        LibraryTools.saveTests("jama",info);
    }
}

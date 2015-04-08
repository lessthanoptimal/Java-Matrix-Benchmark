package jmatbench.colt;

import jmbench.benchmark.BenchmarkTools;
import jmbench.libraries.LibraryStringInfo;

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

        BenchmarkTools.saveLibraryInfo("colt", info);
    }
}

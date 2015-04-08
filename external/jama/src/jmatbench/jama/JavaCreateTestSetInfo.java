package jmatbench.jama;

import jmbench.benchmark.BenchmarkTools;
import jmbench.libraries.LibraryStringInfo;

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

        BenchmarkTools.saveLibraryInfo("jama", info);
    }
}

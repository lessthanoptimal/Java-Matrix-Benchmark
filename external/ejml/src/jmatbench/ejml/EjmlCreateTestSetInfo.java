package jmatbench.ejml;

import jmbench.benchmark.BenchmarkTools;
import jmbench.libraries.LibraryStringInfo;

import java.io.IOException;

/**
 * @author Peter Abeles
 */
public class EjmlCreateTestSetInfo {
    public static void main(String[] args) throws IOException {
        LibraryStringInfo infoA = new LibraryStringInfo();
        infoA.factory = EjmlAlgorithmFactory.class.getName();
        infoA.nameFull = "Efficient Java Matrix Library";
        infoA.nameShort = "EJML";
        infoA.namePlot = "EJML";

        LibraryStringInfo infoB = new LibraryStringInfo();
        infoB.factory = EjmlSimpleAlgorithmFactory.class.getName();
        infoB.nameFull = "EJML-SimpleMatrix";
        infoB.nameShort = "SimpleMatrix";
        infoB.namePlot = "SM";

        BenchmarkTools.saveLibraryInfo("ejml", infoA, infoB);
    }
}

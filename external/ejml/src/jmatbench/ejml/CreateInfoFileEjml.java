package jmatbench.ejml;

import jmbench.impl.DoNothingSpecialConfigure;
import jmbench.impl.LibraryStringInfo;
import jmbench.tools.MiscTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class CreateInfoFileEjml {
    public static void main(String[] args) throws IOException {
        LibraryStringInfo commonOps = new LibraryStringInfo();

        commonOps.factory = EjmlAlgorithmFactory.class.getName();
        commonOps.configure = DoNothingSpecialConfigure.class.getName();
        commonOps.nameFull = "Efficient Java Matrix Library: CommonOps";
        commonOps.nameShort = "EJML";
        commonOps.namePlot = "EJML";

        LibraryStringInfo simple = new LibraryStringInfo();

        simple.factory = EjmlSimpleAlgorithmFactory.class.getName();
        simple.configure = DoNothingSpecialConfigure.class.getName();
        simple.nameFull = "Efficient Java Matrix Library: SimpleMatrix";
        simple.nameShort = "SimpleMatrix";
        simple.namePlot = "Simple";

        List<LibraryStringInfo> info = new ArrayList<LibraryStringInfo>();
        info.add( commonOps);
        info.add(simple);

        MiscTools.saveTests(info);
    }
}

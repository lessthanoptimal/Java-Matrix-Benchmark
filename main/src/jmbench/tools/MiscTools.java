package jmbench.tools;

import com.thoughtworks.xstream.XStream;
import jmbench.impl.LibraryStringInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class MiscTools {
    public static void saveTests( LibraryStringInfo test ) throws IOException {

        List<LibraryStringInfo> tests = new ArrayList<LibraryStringInfo>();
        tests.add(test);
        saveTests(tests);
    }

    public static void saveTests( List<LibraryStringInfo> tests ) throws IOException {
        XStream xstream = new XStream();
        String string = xstream.toXML(tests);

        BufferedWriter out = new BufferedWriter(new FileWriter("TestSetInfo.txt"));
        out.write(string);
        out.close();
    }

    public static List<LibraryStringInfo> loadTests( File file ) {
        XStream xstream = new XStream();
        return (List<LibraryStringInfo>)xstream.fromXML(file);
    }
}

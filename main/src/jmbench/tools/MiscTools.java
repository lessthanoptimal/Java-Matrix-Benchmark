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

    public static void saveLibraryInfo(String directory, List<LibraryStringInfo> tests) throws IOException {
        XStream xstream = new XStream();
        String string = xstream.toXML(tests);

        File f = new File("external/"+directory+"/TestSetInfo.txt");
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
        out.write(string);
        out.close();
    }

    public static void saveLibraryInfo(String directory, LibraryStringInfo... tests) throws IOException {
        List<LibraryStringInfo> list = new ArrayList<>();
        for( LibraryStringInfo info : tests ) {
            list.add(info);
        }
        saveLibraryInfo(directory,list);
    }

    public static List<LibraryStringInfo> loadLibraryInfo(File file) {
        XStream xstream = new XStream();
        return (List<LibraryStringInfo>)xstream.fromXML(file);
    }
}

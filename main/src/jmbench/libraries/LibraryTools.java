package jmbench.libraries;

import com.thoughtworks.xstream.XStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class LibraryTools {

    public static void saveTests( String directory , LibraryStringInfo... tests ) throws IOException {
        List<LibraryStringInfo> all = new ArrayList<>();

        for( LibraryStringInfo l : tests ) {
            all.add(l);
        }

        saveTests(directory,all);
    }

    public static void saveTests( String directory , List<LibraryStringInfo> tests ) throws IOException {
        XStream xstream = new XStream();
        String string = xstream.toXML(tests);

        BufferedWriter out = new BufferedWriter(new FileWriter("external/"+directory+"/TestSetInfo.txt"));
        out.write(string);
        out.close();
    }

    public static List<LibraryStringInfo> loadTests( File file ) {
        XStream xstream = new XStream();
        return (List<LibraryStringInfo>)xstream.fromXML(file);
    }
}

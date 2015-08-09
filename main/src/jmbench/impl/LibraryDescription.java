package jmbench.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class LibraryDescription {
    public LibraryStringInfo info;
    public String directory;

    public List<String> listOfJarFilePaths () {
        File files[] = new File("external/"+directory+"/libraries").listFiles();
        List<String> jars = new ArrayList<String>();
        for( File f : files ) {
            if( f.getName().endsWith(".jar")) {
                jars.add(f.getAbsolutePath());
            }
        }
        return jars;
    }
}

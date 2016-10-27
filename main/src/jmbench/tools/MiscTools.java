/*
 * Copyright (c) 2009-2015, Peter Abeles. All Rights Reserved.
 *
 * This file is part of JMatrixBenchmark.
 *
 * JMatrixBenchmark is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * JMatrixBenchmark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JMatrixBenchmark.  If not, see <http://www.gnu.org/licenses/>.
 */

package jmbench.tools;

import com.thoughtworks.xstream.XStream;
import jmbench.impl.LibraryStringInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class MiscTools {

    public static String selectDirectoryName() {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy-HH:mm:ss");
        Date today = Calendar.getInstance().getTime();

        return"results/"+df.format(today);
    }

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

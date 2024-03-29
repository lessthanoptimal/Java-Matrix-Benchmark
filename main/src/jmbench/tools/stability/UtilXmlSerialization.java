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

package jmbench.tools.stability;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import jmbench.tools.MiscTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Peter Abeles
 */
public class UtilXmlSerialization {
    public static void serializeXml(Object o, String fileName) {
        XStream xstream = MiscTools.createXStream(new DomDriver());

        try (FileOutputStream stream = new FileOutputStream(fileName)) {
            xstream.toXML(o, stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserializeXml(String fileName) {
        File file = new File(fileName);
        if (!file.exists())
            return null;

        XStream xstream = MiscTools.createXStream(new DomDriver());
        xstream.addPermission(AnyTypePermission.ANY);

        try (FileReader reader = new FileReader(file)) {
            return (T) xstream.fromXML(reader);
        } catch (IOException e) {
            return null;
        }
    }
}

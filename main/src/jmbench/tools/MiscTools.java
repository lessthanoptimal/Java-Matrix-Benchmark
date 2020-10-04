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
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import jmbench.impl.LibraryStringInfo;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Peter Abeles
 */
public class MiscTools {

    public static String selectDirectoryName( String benchmarkName ) {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy-HH_mm_ss");
        Date today = Calendar.getInstance().getTime();

        return new File("results",benchmarkName+"_"+df.format(today)).getPath();
    }

    public static long parseTime( String message ) {
        long multiplier = 1;
        int truncate = 0;
        if( message.endsWith("ms"))
            truncate = 2;
        else if ( message.endsWith("s")) {
            multiplier = 1000;
            truncate = 1;
        } else if ( message.endsWith("m")) {
            multiplier = 60*1000;
            truncate = 1;
        }
        if( truncate > 0 )
            message = message.substring(0, message.length()-truncate);
        return Long.parseLong(message)*multiplier;
    }

    public static long parseMemoryMB( String message ) {
        long divisor = 1;
        long multiplier = 1;
        int truncate = 0;

        message = message.toLowerCase();

        if( message.endsWith("mb")) {
            truncate = 2;
        } else if( message.endsWith("m")) {
            truncate = 1;
        } else if ( message.endsWith("gb")) {
            multiplier = 1024;
            truncate = 2;
        } else if ( message.endsWith("g")) {
            multiplier = 1024;
            truncate = 1;
        } else if ( message.endsWith("b")) {
            divisor = 1024*1024;
            truncate = 1;
        }
        if( truncate > 0 )
            message = message.substring(0, message.length()-truncate);
        return Long.parseLong(message)*multiplier/divisor;
    }

    public static String stringTimeArgumentHelp() {
        return "Maximum time on a single test. <value><unit> Units: ms = milliseconds, s = seconds, m = minutes.  Default is ms";
    }

    public static String milliToHuman( long milliseconds ) {
        long second = (milliseconds / 1000) % 60;
        long minute = (milliseconds / (1000 * 60)) % 60;
        long hour = (milliseconds / (1000 * 60 * 60)) % 24;
        long days = milliseconds / (1000 * 60 * 60 * 24);

        return String.format("%03d:%02d:%02d:%02d (days:hrs:min:sec)", days, hour, minute, second);
    }

    public static void saveLibraryInfo(String directory, List<LibraryStringInfo> tests) throws IOException {
        XStream xstream = createXStream(null);
        String string = xstream.toXML(tests);

        File f = new File("external/"+directory+"/TestSetInfo.txt");
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
        out.write(string);
        out.close();
    }

    public static XStream createXStream(HierarchicalStreamDriver driver) {
        XStream xstream = driver == null ? new XStream() : new XStream(driver);
        xstream.addPermission(NoTypePermission.NONE);
        xstream.addPermission(NullPermission.NULL);
        xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
        xstream.allowTypeHierarchy(Collection.class);
        xstream.allowTypesByWildcard(new String[] {
                "jmbench.**"
        });
        return xstream;
    }

    public static void saveLibraryInfo(String directory, LibraryStringInfo... tests) throws IOException {
        List<LibraryStringInfo> list = new ArrayList<>();
        for( LibraryStringInfo info : tests ) {
            list.add(info);
        }
        saveLibraryInfo(directory,list);
    }

    public static List<LibraryStringInfo> loadLibraryInfo(File file) {
        XStream xstream = createXStream(null);
        return (List<LibraryStringInfo>)xstream.fromXML(file);
    }

    public static void sendFinishedEmail( String benchmark , long startTime ) {
        File f = new File("email_login.txt");
        try {
            if (f.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(f));

                String emailUsername = reader.readLine();
                String emailPassword = reader.readLine();
                String emailDestination = reader.readLine();

                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class",
                        "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.port", "465");

                Session session = Session.getDefaultInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(emailUsername, emailPassword);
                            }
                        });

                long elapsedTime = System.currentTimeMillis()-startTime;

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(emailUsername + "@gmail.com"));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestination));
                message.setSubject("Java Matrix Benchmark Completed: " + benchmark);
                message.setText(benchmark+" has finished.\n\n"+
                        "Start Date "+new Date(startTime)+"\n\n"+
                        "Elapsed Time: "+MiscTools.milliToHuman(elapsedTime));

                Transport.send(message);

                System.out.println("Sent summary to " + emailDestination);
            } else {
                System.out.println("\n\n*** email_login.txt doesn't exist ***");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Something went wrong when trying to e-mail");
        }
    }
}

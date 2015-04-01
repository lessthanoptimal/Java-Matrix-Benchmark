/*
 * Copyright (c) 2009-2011, Peter Abeles. All Rights Reserved.
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

package jmbench.tools.version;

import jmbench.impl.LibraryDescription;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Saves version information for a library to a file.  A new JVM needs to be launched because the libraries jars
 * aren't loaded otherwise.
 *
 * @author Peter Abeles
 */
public class PrintLibraryVersion {

    // where the results be saved to
    private String directorySave;

    public PrintLibraryVersion(String directorySave) {
        this.directorySave = directorySave;
    }

    public void printVersion( LibraryDescription desc ) throws IOException, InterruptedException {

        String[] params = setupSlave(desc);

        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec(params);

        BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

        monitorSlave(pr,input,error);

        printError(error);
        printInputBuffer(input);

        int exitVal = pr.waitFor();
        if( exitVal != 0 ) {
            System.out.println("Version slave return value "+exitVal);
        }
    }

    private boolean monitorSlave( Process pr, BufferedReader input, BufferedReader error)
            throws IOException, InterruptedException {

        int mustBeFrozenTime = 2000;

        // flush the input buffer
        System.in.skip(System.in.available());

        boolean frozen = false;

        long startTime = System.currentTimeMillis();
        long lastAliveMessage = startTime;
        for(;;) {
            while( System.in.available() > 0 ) {
                if( System.in.read() == 'q' ) {
                    System.out.println("User requested for the application to quit by pressing 'q'");
                    System.exit(0);
                }
            }

            printError(error);

            if( input.ready() ) {
                printInputBuffer(input);
            } else {
                Thread.sleep(500);
            }

            try {
                // exit value throws an exception is the process has yet to stop
                pr.exitValue();
                break;
            } catch( IllegalThreadStateException e) {
                // check to see if the process is frozen
                if(System.currentTimeMillis() - startTime > mustBeFrozenTime ) {
                    frozen = true;
                    break;
                }

                // let everyone know its still alive
                if( System.currentTimeMillis() - lastAliveMessage > 60000 ) {
//                    System.out.println("\nMaster is still alive: "+new Date()+"  Press 'q' and enter to quit.");
                    lastAliveMessage = System.currentTimeMillis();
                }
            }
        }

        return frozen;
    }

    private String[] setupSlave(LibraryDescription desc) {

        // grab the current classpath and add some additional jars
        String classPath = getClassPath(desc.location.listOfJarFilePaths());
        String app = System.getProperty("java.home")+"/bin/java";

        String params[] = new String[7];
        params[0] = app;
        params[1] = "-server";
        params[2] = "-classpath";
        params[3] = classPath;
        params[4] = VersionSlave.class.getName()+"";
        params[5] = desc.version.getName();
        params[6] = directorySave+"/";
        return params;
    }

    private void printError(BufferedReader error) throws IOException {
        while( error.ready() ) {
            int val = error.read();
            if( val < 0 ) break;

            System.out.print(Character.toChars(val));
        }
    }

    private void printInputBuffer(BufferedReader input) throws IOException {

        while( input.ready() ) {
            int val = input.read();
            if( val < 0 ) break;

            System.out.print(Character.toChars(val));
        }
    }

    public String getClassPath(List<String> jarNames) {
        String extraJars = System.getProperty("java.class.path");

        String sep = System.getProperty("path.separator");

        if( jarNames != null ) {
            for( String s : jarNames ) {
                extraJars = extraJars + sep + s;
            }
        }
        return extraJars;
    }


}

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

package jmbench.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

/**
 * Class for launching JVMs.  Monitors the status and kills frozen threads.  Keeps track of execution time and
 * sets up class path.  Uses the existing one and the list of libraries in external/<library>/runtimeLibs
 *
 * @author Peter Abeles
 */
public class JavaRuntimeLauncher {

    private String classPath;
    // amount of memory allocated to the JVM
    private long memoryInMB = 200;
    // if the process doesn't finish in this number of milliesconds it's considered frozen and killed
    private long frozenTime = 60*1000;

    // amount of time it actually took to execute in milliseconds
    private long durationMilli;

    // save for future debugging
    private String[] jvmArgs;

    /**
     * Constructor.  Configures which library it is to be launching a class from/related to
     * @param pathJars List of paths to all the jars
     */
    public JavaRuntimeLauncher( List<String> pathJars ) {

        String sep = System.getProperty("path.separator");

        if( pathJars != null ) {
            classPath = "";

            for( String s : pathJars ) {
                classPath = classPath + sep + s;
            }
        }
    }

    /**
     * Specifies the amount of time the process has to complete.  After which it is considered frozen and
     * will be killed
     * @param frozenTime time in milliseconds
     */
    public void setFrozenTime(long frozenTime) {
        this.frozenTime = frozenTime;
    }

    /**
     * Specifies the amount of memory the process will be allocated in megabytes
     * @param memoryInMB megabytes
     */
    public void setMemoryInMB(long memoryInMB) {
        this.memoryInMB = memoryInMB;
    }

    /**
     * Returns how long the operation took to complete. In milliseconds
     */
    public long getDurationMilli() {
        return durationMilli;
    }

    /**
     * Launches the class with the provided arguments.
     * @param mainClass Class
     * @param args it's arguments
     * @return true if successful or false if it ended on error
     */
    public Exit launch( Class mainClass , String ...args ) {

        jvmArgs = configureArguments(mainClass,args);

        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(jvmArgs);

            // If it exits too quickly it might not get any error messages if it crashes right away
            // so the work around is to sleep
            Thread.sleep(500);

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

            // print the output from the slave
            if( !monitorSlave(pr, input, error) )
                return Exit.FROZEN;

            if( pr.exitValue() != 0 ) {
                return Exit.RETURN_NOT_ZERO;
            } else {
                return Exit.NORMAL;
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prints out the standard out and error from the slave and checks its health.  Exits if
     * the slave has finished or is declared frozen.
     *
     * @return true if successful or false if it was forced to kill the slave because it was frozen
     */
    private boolean monitorSlave(Process pr,
                                 BufferedReader input, BufferedReader error)
            throws IOException, InterruptedException {

        // flush the input buffer
        System.in.skip(System.in.available());

        // If the total amount of time allocated to the slave exceeds the maximum number of trials multiplied
        // by the maximum runtime plus some fudge factor the slave is declared as frozen

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
                if(System.currentTimeMillis() - startTime > frozenTime ) {
                    pr.destroy(); // kill the process
                    frozen = true;
                    break;
                }

                // let everyone know its still alive
                if( System.currentTimeMillis() - lastAliveMessage > 60000 ) {
                    System.out.println("\nMaster is still alive: "+new Date()+"  Press 'q' and enter to quit.");
                    lastAliveMessage = System.currentTimeMillis();
                }
            }
        }
        durationMilli = System.currentTimeMillis()-startTime;
        return !frozen;
    }

    protected void printError(BufferedReader error) throws IOException {
        while( error.ready() ) {
            int val = error.read();
            if( val < 0 ) break;

            System.out.print(Character.toChars(val));
        }
    }

    protected void printInputBuffer(BufferedReader input) throws IOException {

        while( input.ready() ) {
            int val = input.read();
            if( val < 0 ) break;

            System.out.print(Character.toChars(val));
        }
    }

    private String[] configureArguments( Class mainClass , String ...args ) {
        String out[] = new String[7+args.length];

        String app = System.getProperty("java.home")+"/bin/java";

        out[0] = app;
        out[1] = "-server";
        out[2] = "-Xms"+memoryInMB+"M";
        out[3] = "-Xmx"+memoryInMB+"M";
        out[4] = "-classpath";
        out[5] = classPath;
        out[6] = mainClass.getName();
        for (int i = 0; i < args.length; i++) {
            out[7+i] = args[i];
        }
        return out;
    }

    public String getClassPath() {
        return classPath;
    }

    public long getAllocatedMemoryInMB() {
        return memoryInMB;
    }

    public long getFrozenTime() {
        return frozenTime;
    }

    public String[] getArguments() {
        return jvmArgs;
    }

    public enum Exit
    {
        /**
         * Exited normally.
         */
        NORMAL,
        /**
         * Did not finish in the required amount of time
         */
        FROZEN,
        /**
         * exited with a non zero return value
         */
        RETURN_NOT_ZERO,
    }
}

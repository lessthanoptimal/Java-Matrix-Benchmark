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
import java.io.PrintStream;
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
    private long frozenTimeMS = 60*1000;

    // amount of time it actually took to execute in milliseconds
    private long durationMilli;

    // save for future debugging
    private String[] jvmArgs;

    // optional message which describes the task being performed
    private String messageTask = "";

    public PrintStream errorStream = System.err;

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

    public void setErrorStream(PrintStream errorStream) {
        this.errorStream = errorStream;
    }

    /**
     * Specifies the amount of time the process has to complete.  After which it is considered frozen and
     * will be killed
     * @param frozenTime time in milliseconds
     */
    public void setFrozenTimeMS(long frozenTime) {
        this.frozenTimeMS = frozenTime;
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
            return monitorSlave(pr, input, error);
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
    private Exit monitorSlave(Process pr,
                                 BufferedReader input, BufferedReader error)
            throws IOException, InterruptedException {

        // flush the input buffer
        System.in.skip(System.in.available());

        // If the total amount of time allocated to the slave exceeds the maximum number of trials multiplied
        // by the maximum runtime plus some fudge factor the slave is declared as frozen

        Exit exit = Exit.NORMAL;

        long startTime = System.currentTimeMillis();
        long lastAliveMessage = startTime;
        for(;;) {
            while( System.in.available() > 0 ) {
                if( System.in.read() == 'q' ) {
                    System.out.println("User requested for the application to quit by pressing 'q'");
                    System.exit(0);
                }
            }

            printToStream(error, errorStream);
            if( input.ready() ) {
                printToStream(input, System.out);
            } else {
                Thread.sleep(500);
            }

            try {
                // exit value throws an exception is the process has yet to stop
                if( pr.exitValue() != 0 )
                    exit = Exit.RETURN_NOT_ZERO;
                break;
            } catch( IllegalThreadStateException e) {
                long ellapsedTime = System.currentTimeMillis() - startTime;

                // check to see if the process is frozen
                if(ellapsedTime > frozenTimeMS) {
                    // use destroyForcibly() because it might kill the child subprocess too
                    // not that isn't ensured
                    long giveUpTime = System.currentTimeMillis()+20_000;
                    boolean success = false;
                    while( System.currentTimeMillis() < giveUpTime && !success ) {
                        if( !pr.destroyForcibly().isAlive() ) {
                            success = true;
                        } else {
                            printToStream(error, errorStream);
                            printToStream(input, System.out);
                            Thread.sleep(500);
                        }
                    }

                    if( success )
                        exit = Exit.FROZEN;
                    else
                        exit = Exit.FROZEN_WILL_NOT_DIE;
                    break;
                }

                // let everyone know its still alive
                if( System.currentTimeMillis() - lastAliveMessage > 300000 ) {
                    int percent = (int)(100*(ellapsedTime/(double) frozenTimeMS));
                    System.out.println("\nMaster is still alive: "+new Date()+"  Press 'q' and enter to quit. "+percent+"%");
                    if( messageTask != null && messageTask.length() > 0 )
                        System.out.println(messageTask);
                    lastAliveMessage = System.currentTimeMillis();
                }
            }
        }
        durationMilli = System.currentTimeMillis()-startTime;
        return exit;
    }

    protected void printToStream(BufferedReader reader, PrintStream stream) throws IOException
    {
        while( reader.ready() ) {
            int val = reader.read();
            if( val <= 0 ) break;
            stream.print(Character.toChars(val));
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

    public long getFrozenTimeMS() {
        return frozenTimeMS;
    }

    public String[] getArguments() {
        return jvmArgs;
    }

    public void setMessageTask(String messageTask) {
        this.messageTask = messageTask;
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
        /**
         * Can't kill process. Subprocess won't die.
         */
        FROZEN_WILL_NOT_DIE
    }
}

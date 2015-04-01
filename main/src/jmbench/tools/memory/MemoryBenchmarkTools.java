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

package jmbench.tools.memory;

import jmbench.tools.EvaluationTest;
import jmbench.tools.EvaluatorSlave;
import jmbench.tools.TestResults;
import jmbench.tools.stability.UtilXmlSerialization;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Random;


/**
 * Launches test processes for memory benchmark
 *
 * @author Peter Abeles
 */
// TODO use proc/status
    // todo implement child pause
    // todo make it selectable if it should check proc or use ps
public class MemoryBenchmarkTools {

    // how often the size is sampled
    long samplePeriod = 20;

    // how long the child process should pause for at the end
    // this allows for the peak memory usage to be read before it dies
    // time is specified in milliseconds.
    long childPauseTimeMS = samplePeriod*3;


    // used to ID stale results
    int requestID= new Random().nextInt();

    // if not zero it will allocate this much memory (MB)
    long memoryMin = 0;
    long memoryMax = 0;

    // jar that need to be added
    String extraJars = "";

    // if no estimated processing time is provided it should wait this long before
    // declaring the process to be frozen
    long frozenDefaultTime = 10*60*1000;

    // how long the process ran for in milliseconds
    long durationMilli;

    long memoryUsage;

    // if the test failed or not
    boolean failed;
    // did it fail because it froze?
    boolean froze;

    boolean verbose = true;

    PrintStream errorStream = System.err;

    // arguments passed to slave jvm
    String []params;

    MemoryConfig.SampleType sampleType;

    public MemoryBenchmarkTools(){}

    public MemoryBenchmarkTools( List<String> jarNames ){
        setJars(jarNames);
    }

    public void setJars(List<String> jarNames) {
        String sep = System.getProperty("path.separator");

        if( jarNames != null ) {
            extraJars = "";

            for( String s : jarNames ) {
                extraJars = extraJars + sep + s;
            }
        }
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setErrorStream(PrintStream errorStream) {
        this.errorStream = errorStream;
    }
    public String getClassPath() {
        return System.getProperty("java.class.path")+extraJars;
    }

    public void setFrozenDefaultTime(long frozenDefaultTime) {
        this.frozenDefaultTime = frozenDefaultTime;
    }

    public void setMemory(long min , long max) {
        this.memoryMax = max;
        this.memoryMin = min;
    }

    /**
     *
     * @param test
     * @return Amount of memory used in bytes.
     */
    public long runTest( MemoryTest test ) {

        requestID++;
        failed = false;
        froze = false;

        String[] params = setupSlave(test);

        if(verbose) {
            System.out.println("Test random seed = "+test.getRandomSeed());
        }

        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(params);

            long processID = getProcessID("EvaluatorSlave");

            if( processID < 0 ) {
                System.out.println("Get Process ID failed");
                errorStream.println("  get process ID failed.");
                return -1;
            }

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

            // print the output from the slave
            froze = monitorSlave2(test, pr, input, error,processID);

            cleanUp(froze, pr, input, error, test.getNameOperation());

            return memoryUsage;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prints out the standard out and error from the slave and checks its health.  Exits if
     * the slave has finished or is declared frozen.
     */
    private boolean monitorSlave2(EvaluationTest test, Process pr,
                                 BufferedReader input, BufferedReader error ,
                                 long PID )
            throws IOException, InterruptedException {

        boolean frozen = false;

        long startTime = System.currentTimeMillis();
        long lastAliveMessage = startTime;

        memoryUsage = 0;

        for(;;) {

            long mem = getMemoryForPid(PID);
            if( mem > memoryUsage )
                memoryUsage = mem;

            printError(error);

            if( input.ready() ) {
                printInputBuffer(input);
            } else {
                Thread.sleep(samplePeriod);
            }

            try {
                // exit value throws an exception is the process has yet to stop
                pr.exitValue();
                break;
            } catch( IllegalThreadStateException e) {
                // check to see if the process is frozen
                if(System.currentTimeMillis() - startTime > frozenDefaultTime ) {
                    frozen = true;
                    break;
                }

                // let everyone know its still alive
                if( System.currentTimeMillis() - lastAliveMessage > 60000 ) {
                    System.out.println("\nMaster is still alive: "+new Date());
                    lastAliveMessage = System.currentTimeMillis();
                }
            }
        }
        durationMilli = System.currentTimeMillis()-startTime;
        return frozen;
    }

    public long getMemoryForPid( long PID ) {
        switch( sampleType ) {
            case PROC:
                return getMemoryForPid_PROC(PID);

            case PS:
                return getMemoryForPid_PS(PID);

            default:
                throw new RuntimeException("Unknown sample type "+sampleType);
        }

    }

    public static long getMemoryForPid_PS( long PID ) {
        StringBuffer buff = new StringBuffer();
        String textPID = ""+PID;

        try {
            Process pr = Runtime.getRuntime().exec("ps -eo pid,rss");

            if (!getProcessInput(pr, buff))
                return -1;

            // find the ID
            StringBuffer word = new StringBuffer(50);
            boolean found = false;

            String text = buff.toString();

            int wordIndex = 0;

            for( int i = 0; i < text.length(); i++ ) {
                char c = text.charAt(i);

                if( c == ' ' || c == '\n') {
                    boolean newWord = word.length() != 0;

                    if( found ) {
                       if( newWord ) {
                           return Long.parseLong(word.toString())*1024; // RSS is in kilobytes, convert to bytes
                       }
                    } else {
                        if( newWord && wordIndex == 0 && word.toString().compareTo(textPID) == 0 ) {
                            found = true;
                        }
                        word.delete(0,word.length());
                    }
                    if( newWord ) {
                        if( c == '\n' )
                            wordIndex = 0;
                        else
                            wordIndex++;
                    }
                } else {
                    word.append(c);
                }
            }
            return -1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static long getMemoryForPid_PROC( long PID ) {
        String textPID = ""+PID;

        BufferedReader input = null;
        long size = -1;
        try {
            input = new BufferedReader(new FileReader("/proc/"+textPID+"/status"));

            while( true ) {
                String line = input.readLine();

                if( line == null )
                    break;

                if( line.length() > 10 && line.charAt(0) == 'V' && line.charAt(1) == 'm' &&
                        line.charAt(2) == 'H' && line.charAt(3) == 'W' && line.charAt(4) == 'M' &&
                        line.charAt(5) == ':') {
                    int begin = 6;
                    int end = line.length()-1;
                    for( ; begin < end; begin++ ) {
                        char c = line.charAt(begin);
                        if( c != ' ' && c != '\t')
                            break;
                    }
                    for( end = begin+1; end < line.length(); end++ ) {
                        char c = line.charAt(end);
                        if( c == ' ' || c == '\t' || c == '\n')
                            break;
                    }
                    String sizeStr = line.substring(begin,end);
                    size = Long.decode(sizeStr)*1024;
                    break;
                }
            }

            input.close();
        } catch (FileNotFoundException e) {
            return -1;
        } catch (IOException e) {
            return -1;
        }

        return size;
    }

    /**
     * Uses jps to get a list of java processes.  It then looks for the first process
     * that is the specified class and returns its PID.
     *
     * @param name The java class that was launched.
     * @return The process PID or -1 if it wasn't found.
     */
    public static long getProcessID( String name ) {

        String params[] = new String[]{"jps"};

        StringBuffer buff = new StringBuffer();


        long foundID = -1;

        try {
            Process pr = Runtime.getRuntime().exec(params);
            if (!getProcessInput(pr, buff))
                return -1;

            // find the ID
            String prev = null;
            String curr = null;

            String text = buff.toString();

            for( int i = 0; i < text.length(); i++ ) {
                char c = text.charAt(i);

                if( c == ' ' || c == '\n') {
                    if( curr != null ) {
                        // see if it found the process it was looking for
                        if( curr.compareTo(name) == 0 ) {
                            if( foundID != -1 )
                                throw new RuntimeException("Multiple instances of process "+name);
                            foundID = Long.parseLong(prev);
                        }

                        prev = curr;
                        curr = null;
                    }
                } else {
                   if( curr == null ) {
                       curr = Character.toString(c);
                   } else {
                       curr += c;
                   }
                }
            }
            return foundID;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean getProcessInput( Process pr, StringBuffer buff) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();

        BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));

        boolean worked = true;

        for(;;) {
            if( input.ready() ) {
                while( input.ready() ) {
                    buff.append((char)input.read());
                }
            } else {
                Thread.sleep(50);
            }

            try {
                // exit value throws an exception is the process has yet to stop
                pr.exitValue();
                pr.waitFor();
                break;
            } catch( IllegalThreadStateException e) {
                // check to see if the process is frozen
                if(System.currentTimeMillis() - startTime > 500 ) {
                    pr.destroy();
                    pr.waitFor();
                    worked = false;
                    break;
                }
            }
        }

        // clean up
        while( input.ready() ) {
            buff.append((char)input.read());
        }
        pr.getOutputStream().close();
        pr.getErrorStream().close();
        input.close();
        return worked;
    }


    /**
     * Writes out an xml file that tells the slave what to run and puts together the runtime
     * parameters that are passed on to it.
     */
    private String[] setupSlave(EvaluationTest test) {
        // write out a file describing what the slave should process.
        UtilXmlSerialization.serializeXml(test,"case.xml");

        // grab the current classpath and add some additional jars
        String classPath = getClassPath();
        String app = System.getProperty("java.home")+"/bin/java";

        if(verbose)
            System.out.println("Memory = "+memoryMax+" MB");

        params = new String[10];
        params[0] = app;
        params[1] = "-server";
        params[2] = "-Xms"+memoryMin+"M";
        params[3] = "-Xmx"+memoryMax+"M";
        params[4] = "-classpath";
        params[5] = classPath;
        params[6] = "jmbench.tools.EvaluatorSlave";
        params[7] = "case.xml";
        params[8] = Integer.toString(1);
        params[9] = Long.toString(requestID);
        return params;
    }

    private void printError(BufferedReader error) throws IOException {
        while( error.ready() ) {
            int val = error.read();
            if( val < 0 ) break;

            char []message = Character.toChars(val);
            System.out.print(message);
            errorStream.print(message);
        }
    }

    private void printInputBuffer(BufferedReader input) throws IOException {

        while( input.ready() ) {
            int val = input.read();
            if( val < 0 ) break;

            System.out.print(Character.toChars(val));
        }
    }

    /**
     * Cleans up after the slave and compiles the results that are returned.
     */
    private void cleanUp(boolean frozen,
                         Process pr,
                         BufferedReader input, BufferedReader error, String nameOp )
            throws InterruptedException, IOException {

        // flush whatever is left
        printInputBuffer(input);
        printError(error);

        // now look to see what happened
        if( !frozen ) {
            // see if it exited normally
            int exitVal = pr.waitFor();

            if( exitVal != 0 ) {
                errorStream.println("None 0 exit value returned by the slave. val = "+exitVal);
                failed = true;
            } else {
                EvaluatorSlave.Results results = UtilXmlSerialization.deserializeXml("slave_results.xml");

                // make sure these results are not stale
                if( results == null ) {
                    errorStream.println("Can't find slave_results.xml");
                    failed = true;
                } if( results.getRequestID() != requestID ) {
                    errorStream.println("Stale request ID");
                    failed = true;
                } else if( results.failed != null ) {
                    if( results.failed == EvaluatorSlave.FailReason.USER_REQUESTED ) {
                        errorStream.println("    Slave was killed by the user/OS.  Stopping the benchmark.");
                        errorStream.println("    error message: "+results.detailedError);
                        System.out.println("  Slave was killed by the user/OS.  Stopping the benchmark.");
                        System.out.println("    error message: "+results.detailedError);
                        System.exit(0);
                    } else if( results.failed != EvaluatorSlave.FailReason.OUT_OF_MEMORY ) {
                        // don't log out of memory errors since they happen intentionally a lot
                        errorStream.println("Failed! op = "+nameOp+" reason "+results.failed);
                        errorStream.println(results.detailedError);
                        System.out.println("Failed! op = "+nameOp+" reason "+results.failed);
                        System.out.println(results.detailedError);
                    }
                    failed = true;
                }  else {
                    // See if the slave caught an error.  Typically this will be the operation isn't supported or
                    // sanity check failed
                    for( TestResults tr : results.getResults() ) {
                        MemoryTest.Results rm = (MemoryTest.Results)tr;

                        if( rm.elapsedTime < 0 ) {
                            String message = "    Case failed: Operation Not Supported: "+nameOp;
                            errorStream.println(message);
                            System.out.println(message);
                            memoryUsage = -1;
                            break;
                        }
                    }
                }
            }
        } else {
            errorStream.println("BenchmarkTools: Killing a frozen slave.");
            System.out.println("BenchmarkTools: Killing a frozen slave.");
            // kill the frozen process
            pr.destroy();
            pr.waitFor();

            // report that there is no results because the slave froze
            System.out.println("Frozen slave is dead.");
            failed = true;
        }
        
        // close the IO streams
        input.close();
        error.close();
        pr.getOutputStream().close();

        // delete temporary files
        cleanup();
    }

    /**
     * Delete temporary files that it created to pass information between the master and the slave.
     */
    private static void cleanup() {
        if( !new File("case.xml").delete() ) {
            System.out.println("Couldn't delete case.xml");
        }

        if( !new File("slave_results.xml").delete() ) {
            System.out.println("Couldn't delete slave_results.xml");
        }
    }

    /**
     * Returns how long the most recent process took in milliseconds.
     * @return Runtime of the latest process in milliseconds.
     */
    public long getDurationMilli() {
        return durationMilli;
    }


    /**
     * Returns parameters passed to slave jvm
     *
     * @return jvm parameters
     */
    public String[] getParams() {
        return params;
    }

    public boolean isFailed() {
        return failed;
    }
}

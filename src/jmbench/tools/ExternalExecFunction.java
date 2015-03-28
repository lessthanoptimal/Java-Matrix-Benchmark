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

package jmbench.tools;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Launches a new VM to call a function and returns the result.  Useful when a jar should not be loaded
 * in the current runtime.
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class ExternalExecFunction {

    Random rand = new Random();

    Class owner;
    List<String> jarNames;

    public ExternalExecFunction(Class owner, List<String> jarNames) {
        this.owner = owner;
        this.jarNames = jarNames;
    }

    public <T> T call( String functionName ) {
        try {
            String fileName = "temp"+rand.nextInt(50000);

            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(createParams(functionName,fileName));

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

            if( !monitorSlave(pr,input,error) )
                throw new RuntimeException("Slave failed.");

            return (T)readResult(fileName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T readResult(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fileStream = new FileInputStream(fileName);
        ObjectInputStream objStream = new ObjectInputStream(fileStream);

        T obj = (T)objStream.readObject();

        objStream.close();

        File f = new File(fileName);
        if( !f.delete() )
            System.out.println(getClass().getSimpleName()+": Can't delete temp file.");

        return obj;
    }


    /**
     * Prints out the standard out and error from the slave and checks its health.  Exits if
     * the slave has finished or is declared frozen.
     *
     * @rewturn true of successful
     */
    private boolean monitorSlave(Process pr, BufferedReader input, BufferedReader error)
            throws IOException, InterruptedException {

        long mustBeFrozenTime = 5000;

        boolean frozen = false;

        long startTime = System.currentTimeMillis();
        long lastAliveMessage = startTime;
        for(;;) {

            printBuffer(error);

            if( input.ready() ) {
                printBuffer(input);
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
                    System.out.println("\nMaster is still alive: "+new Date());
                    lastAliveMessage = System.currentTimeMillis();
                }
            }
        }

        boolean errorFlag = frozen;

        // clean up
        printBuffer(input);
        printBuffer(error);
        input.close();
        error.close();
        pr.getOutputStream().close();

        // see if it exited normally
        if( pr.waitFor() != 0 ) {
            System.out.println("Did not exit normally");
            errorFlag = true;
        }

        return !errorFlag;
    }

    private void printBuffer(BufferedReader input) throws IOException {

        while( input.ready() ) {
            int val = input.read();
            if( val < 0 ) break;

            System.out.print(Character.toChars(val));
        }
    }

    protected String[] createParams( String functionName , String fileName  ) {
        String app = System.getProperty("java.home")+"/bin/java";

        String[] params = new String[8];
        params[0] = app;
        params[1] = "-server";
        params[2] = "-classpath";
        params[3] = getClassPath();
        params[4] = getClass().getName();
        params[5] = owner.getName();
        params[6] = functionName;
        params[7] = fileName;

        return params;
    }

    protected String getClassPath() {
        String sep = System.getProperty("path.separator");

        String extraJars = "";

        if( jarNames != null ) {

            for( String s : jarNames ) {
                extraJars = extraJars + sep + s;
            }
        }

        String ret = System.getProperty("java.class.path")+extraJars;

//        System.out.println("Class Path = "+ret);

        return ret;
    }

    public static void main( String args[] ) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, IOException {
        String className = args[0];
        String functionName = args[1];
        String fileName = args[2];


        Class c = Class.forName(className);

        Method m = c.getMethod(functionName);

        // call the function
        Object ret = m.invoke(c.newInstance());

        // save the function to a file
        FileOutputStream fileStream = new FileOutputStream(fileName);
        ObjectOutputStream objStream = new ObjectOutputStream(fileStream);

        objStream.writeObject(ret);

        objStream.close();
    }

}

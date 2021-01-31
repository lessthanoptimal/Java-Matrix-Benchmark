package jmbench.tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Reads system information using command line arguments
 *
 * @author Peter Abeles
 */
public class SystemSpecificInfo {

    /**
     * Retrieves information the CPU that it's running on using /proc/cpuinfo. Works on Ubuntu
     */
    public static String readCpu() {

        try {
            String text = command("cat","/proc/cpuinfo");
            for (String line : text.split("\n")) {
                if (line.startsWith("model name")) {
                    return line.split(":\\s")[1];
                }
            }
        } catch( RuntimeException e ) {
            System.err.println("Parsing /proc/cpuinfo failed. "+e.getMessage());
        }
        return "Unknown";
    }

    /**
     * Gets a string that describes the OS it's running on
     * @return
     */
    public static String readOSVersion() {
        if( SystemUtils.IS_OS_LINUX ) {
            return readUbuntuVersion();
        } else {
            return SystemUtils.OS_NAME;
        }
    }

    public static String readUbuntuVersion() {
        String text = command("lsb_release", "-a");

        if( text == null )
            return System.getProperty("os.name");
        String[] words = text.split("\n");
        for( String w : words ) {
            if( w.startsWith("Description:")) {
                String a[] = w.split(":\t");
                if( a.length < 2 )
                    continue;
                return a[1];
            }
        }
        return null;
    }

    /**
     * Reads in system load. See {@link #parseProcLoadAvg(String)} for description of output
     */
    public static double[] lookupSystemLoad() {
        try {
            String text = command("cat","/proc/loadavg");
            return parseProcLoadAvg(text);
        } catch( RuntimeException e ) {
            System.err.println("Reading /proc/loadavg failed. "+e.getMessage());
        }
        return new double[]{Double.NaN,Double.NaN,Double.NaN};
    }

    /**
     * Parses text from /proc/loadavg  [0] = average 1 minute, [1] = 5 minutes, [2] = 15 minutes
     */
    public static double[] parseProcLoadAvg( String text ) {
        String words[] = text.split("\\s+");
        if( words.length != 5 )
            throw new RuntimeException("Unexpected length. "+ StringUtils.abbreviate(text,30));
        double[] load = new double[3];
        load[0] = Double.parseDouble(words[0]);
        load[1] = Double.parseDouble(words[1]);
        load[2] = Double.parseDouble(words[2]);
        return load;
    }

    public static String command( String... arguments ) {
        Runtime rt = Runtime.getRuntime();

        String message = "";
        try {
            Process proc = rt.exec(arguments);

            BufferedReader stdin = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stderr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String s;
            while ((s = stdin.readLine()) != null) {
                message += s +"\n";
            }

            stdin.close();
            stderr.close();
        } catch( IOException e ) {
            System.err.println("Calling "+arguments[0]+" failed: "+e.getMessage());
            return null;
        }
        return message;
    }
}

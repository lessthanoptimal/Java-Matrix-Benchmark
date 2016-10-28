package jmbench.tools.stability;

import java.io.File;

import static jmbench.tools.runtime.evaluation.PlotRuntimeResults.findMostRecentDirectory;

/**
 * @author Peter Abeles
 */
public class DisplayStability {

    enum OutputType {
        HTML,
        TEXT
    }

    public static void printHelp() {
        System.out.println("Generate Stability Benchmark Results");
        System.out.println("  --Format=<type>          |  HTML or TEXT");
        System.out.println("  --Size=<size>            |  small or large results");
        System.out.println("  --Directory=<dir>        |  To specify which directory it should launch");
        System.out.println();
        System.out.println("If no options are specified then a default configuration will be used.");

    }

    public static void main(String[] args) {
        OutputType outputType = OutputType.HTML;
        boolean failed = false;
        boolean dataSmall = true;

        String inputDirectory = null;

        System.out.println("** Parsing Command Line **");
        System.out.println();
        for( int i = 0; i < args.length; i++ ) {
            String splits[] = args[i].split("=");

            String flag = splits[0];

            if (flag.length() < 2 || flag.charAt(0) != '-' || flag.charAt(0) != '-') {
                failed = true;
                break;
            }

            flag = flag.substring(2);

            if( flag.compareTo("Format") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}

                if( splits[1].compareToIgnoreCase("HTML") == 0) {
                    outputType = OutputType.HTML;
                } else if( splits[1].compareToIgnoreCase("TEXT") == 0) {
                    outputType = OutputType.TEXT;
                } else {
                    System.err.println("Unknown format: "+splits[1]);
                    failed = true;
                }
            } else if( flag.compareTo("Size") == 0 ) {
                if (splits.length != 2) {
                    failed = true;
                    break;
                }

                if (splits[1].compareToIgnoreCase("small") == 0) {
                    dataSmall = true;
                } else if (splits[1].compareToIgnoreCase("large") == 0) {
                    dataSmall = false;
                } else {
                    System.err.println("Unknown data size: " + splits[1]);
                    failed = true;
                    break;
                }
            } else if( flag.compareToIgnoreCase("directory") == 0 ) {
                inputDirectory = splits[1];
            } else {
                printHelp();
                return;
            }
        }

        if( failed ) {
            printHelp();
            return;
        }

        if( inputDirectory == null )
            inputDirectory = findMostRecentDirectory();

        if( dataSmall ) {
            inputDirectory = new File(inputDirectory,"small").getAbsolutePath();
        } else {
            inputDirectory = new File(inputDirectory,"large").getAbsolutePath();
        }
        System.out.println("Generating tables");
        System.out.println("   type "+outputType);
        System.out.println("   location "+inputDirectory);
        System.out.println();
        System.out.println();

        switch( outputType ) {
            case HTML:
                new GenerateHtmlTables(inputDirectory).plot();
                break;

            case TEXT:
                new GenerateTextTables(inputDirectory).plot();
                break;
        }


    }
}

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

package jmbench.tools.runtime;

import jmbench.impl.LibraryDescription;
import jmbench.impl.LibraryManager;
import jmbench.tools.MiscTools;
import jmbench.tools.SystemInfo;
import jmbench.tools.stability.UtilXmlSerialization;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;


/**
 * <p>
 * RuntimeBenchmarkMaster runs a series of benchmark tests against a set of linear algebra libraries.
 * These libraries are evaluated using {@link RuntimeBenchmarkLibrary}.  The results are
 * put into a directory that is in the results directory whose name is set to the integer value
 * returned by System.currentTimeMillis().  In addition information on the system the test was run on
 * and error logs are all saved.
 * </p>
 * 
 * @author Peter Abeles
 */
public class RuntimeBenchmark {

    // where should the results be saved to
    private String directorySave;

    public RuntimeBenchmark() {
        directorySave = MiscTools.selectDirectoryName("runtime");
    }

    public RuntimeBenchmark(String directory ) {
        this.directorySave = directory;
    }

    /**
     * Perform the benchmark tests against all the different algortihms
     */
    public void performBenchmark( RuntimeBenchmarkConfig config ) {

        saveSystemInfo(config);

        long startTime = System.currentTimeMillis();
        processLibraries(config.getTargets(),config);
        long elapsedTime = System.currentTimeMillis()-startTime;
        System.out.println("Elapsed time "+MiscTools.milliToHuman(elapsedTime)+"\n");

        MiscTools.sendFinishedEmail("Runtime",startTime);
    }

    private void processLibraries( List<LibraryDescription> libs, RuntimeBenchmarkConfig config ) {


        for( LibraryDescription desc : libs ) {

            String libOutputDir = directorySave+"/"+desc.info.outputDirectory();

            // save the description so that where this came from can be easily extracted
            String outputFile = libOutputDir+".xml";
            UtilXmlSerialization.serializeXml(desc,outputFile);

            RuntimeBenchmarkLibrary benchmark = new RuntimeBenchmarkLibrary(libOutputDir,desc,config);

            try {
                benchmark.performBenchmark();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Collects information of the system that this is being run on.  Allows for a better understanding
     * of the results.  Not all relevant information can be gathered since this is java.
     *
     * The benchmark config is also saved here.
     */
    private void saveSystemInfo(RuntimeBenchmarkConfig config) {
        SystemInfo info = new SystemInfo();
        info.grabCurrentInfo();

        File dir = new File(directorySave);
        if( !dir.exists() ) {
            if( !dir.mkdirs() ) {
                throw new IllegalArgumentException("Can't make directories to save results.");
            }
        }

        UtilXmlSerialization.serializeXml(info,directorySave+"/info.xml");
        UtilXmlSerialization.serializeXml(config,directorySave+"/config.xml");
    }

    public static void printHelp() {
        System.out.println("The runtime benchmark works by measuring the ops/second for a specific operation given");
        System.out.println("an input matrix of a specific size. A single measurement is done using a test. A test");
        System.out.println("has a minimum time and a maximum time. A single test is contrained to last for a minimum");
        System.out.println("amount of time. If that time is not meet by a single trial then the number of trials is");
        System.out.println("increased until it does. The JVM might also be optimizing the code at the same time making");
        System.out.println("a single trial faster. This is taken in account. A slow operation might have no warm up");
        System.out.println("period because the amount of time it takes");
        System.out.println();
        System.out.println("Summary of steps:");
        System.out.println("1) Launch a new JVM to run tests inside of");
        System.out.println("2) Determine number of trials needed to meet minimum time (this is the warm up)");
        System.out.println("3) Run the test and compute ops/second");
        System.out.println("4) Exist the slave JVM, record performance, select next op to test");
        System.out.println();
        System.out.println("If a single test exceeds the maximum test time the benchmark will end");
        System.out.println("Otherwise the benchmark will end when the maximum matrix size exceeds a user specifid value");
        System.out.println();
        System.out.println("The following options are valid for runtime benchmark:");
        System.out.println("  --Config=<file>           |  Configure using the specified xml file.");
        System.out.println("  --Size=min:max            |  Test matrices from the specified minimum size to the specified maximum size.");
        System.out.println("  --Quick                   |  Generate results much faster by sacrificing accuracy/stability of the results.");
        System.out.println("  --Library=<lib>           |  To run a specific library only.  --Library=? will print a list");
        System.out.println("                            |  Use a comma to specify multiple libraries, e.g. 'ejml,ojalgo'");
        System.out.println("  --Seed=<number>           |  used to set the random seed to the specified value.");
        System.out.println("  --MinTestTime=<ms>        |  The minimum amount of time spent in a single test.  Typical is 3000 ms.");
        System.out.println("  --MaxTestTime=<time|unit> |  "+MiscTools.stringTimeArgumentHelp());
        System.out.println("  --Resume=<directory>      |  It will resume an unfinished benchmark at the specified directory.");
        System.out.println("  --Memory=<size|unit>      |  Sets the amount of memory allocated.  Default is MB. Recognizes suffixes for m,mb,g,gb,b.");
        System.out.println("                            |  as large as possible with out exceeding the amount of physical memory on the system.");
        System.out.println("                            |  specified since the dynamic algorithm will slow down the benchmark and has some known issues.");
        System.out.println();
        System.out.println("The only option which must be specified is \"FixedMemory\".  If no other options are specified " +
                "then a default configuration will be used and the results" +
                "will be saved to a directory in results with the name of the current system time in milliseconds.");
        System.out.println();
        System.out.println("Example: java -jar benchmark.jar runtime --Size=2:40000 --MaxTime=10m --Memory=25600");
    }

    public static void main( String args[] ) {
        if( args.length == 0 ) {
            printHelp();
            System.exit(0);
            return;
        }

        LibraryManager manager = new LibraryManager();

        boolean memorySpecified = false;
        boolean failed = false;
        boolean configFileSpecified = false;

        RuntimeBenchmarkConfig config = RuntimeBenchmarkConfig.createAllConfig(manager.getDefaults());

        System.out.println("** Parsing Command Line **");
        System.out.println();
        for( int i = 0; i < args.length; i++ ) {
            String splits[] = args[i].split("=");

            String flag = splits[0];

            if( flag.length() < 2 || flag.charAt(0) != '-' || flag.charAt(0) != '-') {
                System.out.println("Failed to understand "+args[i]);
                failed = true;
                break;
            }

            flag = flag.substring(2);

            if( flag.compareTo("Config") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                configFileSpecified = true;
                System.out.println("Loading config: "+splits[1]);
                config = UtilXmlSerialization.deserializeXml(splits[1]);
                if( config == null )
                    throw new RuntimeException("Failed to load configuration");
            } else if( flag.compareTo("Size") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                String rangeStr[] = splits[1].split(":");
                if( rangeStr.length != 2 ) {failed = true; break;}
                config.minMatrixSize = Integer.parseInt(rangeStr[0]);
                config.maxMatrixSize = Integer.parseInt(rangeStr[1]);
                System.out.println("Set min/max matrix size to: "+config.minMatrixSize+" "+config.maxMatrixSize);
            } else if( flag.compareTo("Quick") == 0 ) {
                if( i != 0 ) {
                    System.out.println("*** Quick must be the first argument specified ***");
                    failed = true; break;
                }
                if( splits.length != 1 ) {failed = true; break;}
                config.totalTests = 2;
                config.minimumTimePerTestMS = 1000;
                System.out.println("Using quick and dirty config.");
            } else if( flag.compareTo("Library") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                String[] libs = splits[1].split(",");

                config.targets.clear();

                for (int j = 0; j < libs.length; j++) {
                    LibraryDescription match = manager.lookup(libs[j]);
                    if( match == null ) {
                        failed = true;
                        manager.printAllNames();
                        break;
                    }
                    config.targets.add(match);
                }
            } else if( flag.compareTo("Seed") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                config.seed = Long.parseLong(splits[1]);
                System.out.println("Random seed set to "+config.seed);
            } else if( flag.compareTo("MinTestTime") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                config.minimumTimePerTestMS = Integer.parseInt(splits[1]);
                System.out.println("Minimum time per test set to "+config.minimumTimePerTestMS +" (ms).");
            } else if( flag.compareTo("MaxTestTime") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                config.maximumTimePerTrialMS = (int)MiscTools.parseTime(splits[1]);
                System.out.println("Maximum time per test set to "+config.maximumTimePerTrialMS +" (ms).");
            }else if( flag.compareTo("Resume") == 0 ) {
                if( splits.length != 2 || args.length != 1 ) {failed = true; break;}
                System.out.println("Resuming a benchmark in dir "+splits[1]);
                RuntimeBenchmark master = new RuntimeBenchmark(splits[1]);
                config = UtilXmlSerialization.deserializeXml(splits[1]+"/config.xml");
                master.performBenchmark(config);
                return;
            } else if( flag.compareTo("Memory") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                memorySpecified = true;
                config.memoryMB = (int)MiscTools.parseMemoryMB(splits[1]);
                if( config.memoryMB <= 0 )
                    System.out.println("Memory must be set to a value greater than zero");
                else
                    System.out.println("Memory used in each test will be "+config.memoryMB +" (MB).");
            } else {
                System.out.println("Unknown flag: "+flag);
                printHelp();
                failed = true;
                break;
            }
        }

        double hours = (config.maximumTimePerTrialMS*config.totalTests)/1000.0/60.0/60.0;

        System.out.println("\n** Done parsing command line **\n");

        if( failed ) {
            System.out.println("Unable to parse command.  Try --Help");
        } else {
            System.out.println();
            System.out.printf("Maximum time to complete a single benchmark for an operation/matrix size is %.3f hrs\n",hours);

            System.out.println();
            System.out.println(" To safely quit the benchmark process 'q' and enter.");
            System.out.println();

            if( !configFileSpecified && !memorySpecified ) {
                System.out.println("The amount of memory must be specified using \"--Memory=<MB>\"!");
            } else {
                RuntimeBenchmark master = new RuntimeBenchmark();
                master.performBenchmark(config);
            }
        }
    }
}

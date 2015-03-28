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

import jmbench.impl.FactoryLibraryDescriptions;
import jmbench.impl.LibraryDescription;
import jmbench.tools.SystemInfo;
import jmbench.tools.stability.UtilXmlSerialization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;


/**
 * @author Peter Abeles
 */
public class MemoryBenchmark {

    String directorySave;

    public MemoryBenchmark() {
        directorySave = "results/"+System.currentTimeMillis();
    }

    public MemoryBenchmark( String directory ) {
        this.directorySave = directory;
    }

    public void performBenchmark( MemoryConfig config ) {
        System.out.println("Setting up results directory");
        File dir = new File(directorySave);
        if( !dir.exists() ) {
            if( !dir.mkdir() ) {
                throw new IllegalArgumentException("Can't make directories to save results.");
            }
        }

        SystemInfo info = new SystemInfo();
        info.grabCurrentInfo();

        UtilXmlSerialization.serializeXml(info,directorySave+"/info.xml");
        UtilXmlSerialization.serializeXml(config,directorySave+"/config.xml");

        long startTime = System.currentTimeMillis();

        // save the description of each library
        saveLibraryDescriptions(directorySave,config.libraries);

        System.out.print("Computing overhead ");
        long overhead = new DetermineOverhead(config,10).computeOverhead();
        System.out.println(overhead/1024+" KB");

        processLibraries(config.libraries,config,overhead);

        long stopTime = System.currentTimeMillis();

        System.out.println("Finished Benchmark");
        System.out.println("  elapsed time "+(stopTime-startTime)+" (ms) "+((stopTime-startTime)/(60*60*1000.0))+" hrs");
    }

    private void processLibraries( List<LibraryDescription> libs, MemoryConfig config , long overhead ) {

        for( int i = 0; i < config.matrixSizes.length; i++ ) {

            int size = config.matrixSizes[i];
            System.out.println("************ Starting size "+size);

            File f = new File(directorySave+"/"+size);
            f.mkdirs();
            saveMatrixSize(directorySave+"/"+size+"/size.txt",size);

            for(  LibraryDescription desc : libs ) {
                // run the benchmark
                String libOutputDir = directorySave+"/"+size+"/"+desc.location.getSaveDirName();

                MemoryBenchmarkLibrary bench = new MemoryBenchmarkLibrary(config,desc,libOutputDir,size,overhead);

                bench.process();

                System.out.println("Finished Library Benchmark");
                System.out.println();
            }
        }
    }

    private void saveMatrixSize( String fileName , int size ) {
        try {
            PrintStream out = new PrintStream(fileName);
            out.println(size);
            out.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Save the description so that where this came from can be easily extracted
     */
    public static void saveLibraryDescriptions( String directorySave , List<LibraryDescription> libs )
    {
        for( LibraryDescription desc : libs ) {

            String outputFile = directorySave+"/"+desc.location.getSaveDirName()+".xml";
            UtilXmlSerialization.serializeXml(desc,outputFile);
        }
    }

    public static void printHelp() {
        System.out.println("The following options are valid for memory benchmark:");
        System.out.println("  --Config=<file>          |  Configure using the specified xml file.");
        System.out.println("  --Library=<lib>          |  To run a specific library only.  --Library=? will print a list");
    }

    public static void main( String args[] ) throws IOException, InterruptedException {
        MemoryBenchmark master = new MemoryBenchmark();

        boolean failed = false;
        MemoryConfig config = MemoryConfig.createDefault();

        System.out.println("** Parsing Command Line **");
        System.out.println();
        for( int i = 0; i < args.length; i++ ) {
            String splits[] = args[i].split("=");

            String flag = splits[0];

            flag = flag.substring(2);

            if( flag.compareTo("Config") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                System.out.println("Loading config: "+splits[1]);
                config = UtilXmlSerialization.deserializeXml(splits[1]);
            } else if( flag.compareTo("Library") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                LibraryDescription match = FactoryLibraryDescriptions.find(splits[1]);
                if( match == null ) {
                    failed = true;
                    System.out.println("Can't find library.  See list below:");
                    FactoryLibraryDescriptions.printAllNames();
                    break;
                }
                config.libraries.clear();
                config.libraries.add(match);
            } else {
                failed = true;
            }
        }
        if( failed ) {
            printHelp();
        } else {
            master.performBenchmark(config);
        }
    }
}
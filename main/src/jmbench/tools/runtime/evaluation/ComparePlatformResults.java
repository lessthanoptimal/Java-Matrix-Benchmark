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

package jmbench.tools.runtime.evaluation;

import jmbench.tools.runtime.RuntimeEvaluationMetrics;
import jmbench.tools.runtime.RuntimeResults;

import java.io.File;
import java.util.*;

/**
 * Compares results that are contained in two different results directory.  Typically this will be used to compare
 * results from the same library on different platforms or from different versions.  Results are saved to the
 * "plots" directory.
 *
 * @author Peter Abeles
 */
public class ComparePlatformResults {

    // should it display results to the screen
    private static boolean displayResults = true;

    // only plot results more than this size
    private int minMatrixSize = 0;
    // it will only plot results which are of this size or less
    private int maxMatrixSize = 0;

    // which results it will plot
    List<Platform> platforms = new ArrayList<Platform>();

    File outputDirectory = new File(".");

    /**
     * Adds a new platform to compare.
     *
     * @param resultsDir Where the results are stored.
     * @param libraryDir Name of the directory containing the targeted library.
     * @param plotName What should the plot be named.
     */
    public void addPlatform( String resultsDir , String libraryDir , String plotName )  {
        Platform p = new Platform();
        p.resultsDir = resultsDir;
        p.libraryDir = libraryDir;
        p.plotName = plotName;

        platforms.add(p);
    }

    /**
     * Plot the results.
     *
     * @param whichMetric The metric which will be plotted. See {@link RuntimeEvaluationMetrics}.
     */
    public void plot(int whichMetric) {

        List<List<XMLResults>> l = loadResults();
        Map<String,Operation> m = resortByOperation(l);
        convertToPlotData(m,whichMetric);
    }

    /**
     * Loads results from XML files
     */
    private List<List<XMLResults>> loadResults()
    {
        List<List<XMLResults>> ret = new ArrayList<List<XMLResults>>();

        for( Platform p : platforms ) {

            String platformResultsDir = p.resultsDir+"/"+p.libraryDir;

            File platformDir = new File(platformResultsDir);

            if( !platformDir.exists() ) {
                throw new RuntimeException("Results for "+p.libraryDir+" do not exist in "+p.resultsDir);
            }

            List<XMLResults> opResults = new ArrayList<XMLResults>();

            File[] files = platformDir.listFiles();

            for( File f : files ) {
                String fileName = f.getName();

                if( fileName.contains(".csv")) {
                    // extract the operation name
                    String stripName = fileName.substring(0,fileName.length()-4);

                    XMLResults r = new XMLResults();
                    r.fileName = stripName;
                    r.results = RuntimeResultsCsvIO.read(new File(f.getAbsolutePath()));

                    opResults.add(r);
                }
            }

            ret.add( opResults );
        }

        return ret;
    }

    /**
     * Resorts the result from the platform -> operation order it was read in at to operation -> platform.
     */
    private Map<String,Operation> resortByOperation( List<List<XMLResults>> results )
    {
        Map<String,Operation> ret = new HashMap<String,Operation>();

        for( int i = 0; i < platforms.size(); i++ ) {
            Platform p = platforms.get(i);


            for( XMLResults r : results.get(i) ) {
                Operation o;

                if( ret.containsKey(r.fileName)) {
                    o = ret.get(r.fileName);
                } else {
                    o = new Operation();
                    o.fileName = r.fileName;
                    o.plotName = r.results.getOpName();
                    ret.put(o.fileName,o);
                }
                o.platforms.add(p);
                o.results.add(r.results);
            }
        }

        return ret;
    }

    /**
     * Converts the results into RuntimePlotData and displays the result.
     * 
     * @param resultsByOps Structure containing the results sorted by operation.
     * @param whichMetric The type of metric which is to be plotted.
     */
    private void convertToPlotData(  Map<String,Operation> resultsByOps , int whichMetric )
    {
        for( Operation o : resultsByOps.values() ) {
            int[] sizes = getMatrixSizes(o);

            RuntimePlotData plotData = new RuntimePlotData(sizes);
            plotData.plotName = o.fileName;

            for( int index = 0; index < o.platforms.size(); index++ ) {
                Platform p = o.platforms.get(index);
                RuntimeResults r = o.results.get(index);

                // If a matrix size has no data then its value will be NaN
                double results[] = new double[ sizes.length ];
                for( int i = 0; i < results.length; i++ )
                    results[i] = Double.NaN;


                for( int i = 0; i < r.getMetrics().length; i++ ) {
                    RuntimeEvaluationMetrics m = r.getMetrics()[i];
                    if( m == null )
                        continue;

                    int matrixSize = r.getMatDimen()[i];

                    int indexSize = findMatchIndex( matrixSize , sizes );

                    results[indexSize] = m.getMetric(whichMetric);
                }

                plotData.addLibrary(p.plotName,results,index);
            }

            // generate and save the plot
            createPlots(plotData,o.fileName,o.plotName);
        }
    }

    private int findMatchIndex(int matrixSize, int[] sizes) {
        for( int i = 0; i < sizes.length; i++ ) {
            if( matrixSize == sizes[i])
                return i;
        }

        throw new RuntimeException("Bug in code.  matrix size is not found in size array");
    }

    private int[] getMatrixSizes( Operation o )
    {
        List<Integer> sizes = new ArrayList<Integer>();

        for( RuntimeResults r : o.results ) {
            for( int size : r.matDimen ) {
                if( !sizes.contains(size) )
                    sizes.add(size);
            }
        }

        Collections.sort(sizes);

        int[] ret = new int[ sizes.size() ];
        for( int i = 0; i < ret.length; i++ ) {
            ret[i] = sizes.get(i);
        }

        return ret;
    }

    /**
     * Creates relative runtime and absolute runtime plots.  Relative runtime plots are displayed and both types
     * are saved to disk as pdf files.
     */
    public void createPlots( RuntimePlotData plotData , String fileName , String plotName) {

        PlotRuntimeResults.truncatePlotData(minMatrixSize,maxMatrixSize,plotData);

        String fileNameRel = outputDirectory.getPath()+"/plots/relative/"+fileName;
        String fileNameAbs = outputDirectory.getPath()+"/plots/absolute/"+fileName;

        RuntimeResultPlotter.Reference refType = RuntimeResultPlotter.Reference.MAX;

        RuntimeResultPlotter.relativePlots(plotData, refType,null,fileNameRel,plotName,true,displayResults);
        RuntimeResultPlotter.absolutePlots(plotData, fileNameAbs,plotName,true,false);
    }

    private static class XMLResults
    {
        String fileName;
        RuntimeResults results;
    }

    private static class Operation
    {
        String fileName;
        String plotName;
        List<Platform> platforms = new ArrayList<Platform>();
        List<RuntimeResults> results = new ArrayList<RuntimeResults>();
    }

    private static class Platform
    {
        String resultsDir;
        String libraryDir;
        public String plotName;
    }

    public static void printHelp() {
        System.out.println("Compares results from the same library across different results directories.  This allows "+
                "performance to be compared from one type of computer to another or different versions against each other."+
        "  Both relative and absolute runtime plots are saved to a 'plots' directory in the current directory.");
        System.out.println();
        System.out.println("--Metric=<?>                 : Changes the metric that is plotted.");
        System.out.println("                             : MAX,MIN,STDEV,MEDIAN,MEAN");
        System.out.println("--Display=<true|false>       : If true some results will be displayed.");
        System.out.println("--Size=min:max               : Only plot data from matrix size min to max inclusive.");
        System.out.println("--Library=<name>             : The library's name (name of its directory) which is being compared across platforms. *Must be specified*");
        System.out.println("--Input=<directory>          : Directory containing results.");
        System.out.println("--InputName=<name>           : Name used in plots for an input.  Specified in the same order as Input.");
        System.out.println();
        System.out.println("Example:");
        System.out.println("java -jar build/jar/benchmark_app.jar  comparePlatforms --Library=ejml --Input=results/Q9400_2010_08 --InputName=Q9400 --Input=results/PentiumM_2010_08 --InputName=PentiumM");
    }

    public static void parseInput( String args[] ) {
        ComparePlatformResults app = new ComparePlatformResults();

        int metric = RuntimeEvaluationMetrics.METRIC_MAX;
        boolean displayResults = true;

        boolean failed = false;

        String targetLibrary = null;
        int maxSize = 0;
        int minSize = 0;
        List<String> inputDirectories = new ArrayList<String>();
        List<String> names = new ArrayList<String>();

        for( int i = 0; i < args.length; i++ ) {
            String splits[] = args[i].split("=");

            String flag = splits[0];

            if( flag.length() < 2 || flag.charAt(0) != '-' || flag.charAt(0) != '-') {
                failed = true;
                break;
            }

            flag = flag.substring(2);

            if( flag.compareTo("Library") == 0 ) {
                targetLibrary = splits[1];
            } else if( flag.compareTo("Metric") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                if( splits[1].compareToIgnoreCase("MAX") == 0 ) {
                    metric = RuntimeEvaluationMetrics.METRIC_MAX;
                } else if( splits[1].compareToIgnoreCase("MIN") == 0 ) {
                    metric = RuntimeEvaluationMetrics.METRIC_MIN;
                } else if( splits[1].compareToIgnoreCase("STDEV") == 0 ) {
                    metric = RuntimeEvaluationMetrics.METRIC_STDEV;
                } else if( splits[1].compareToIgnoreCase("MEDIAN") == 0 ) {
                    metric = RuntimeEvaluationMetrics.METRIC_MEDIAN;
                } else if( splits[1].compareToIgnoreCase("MEAN") == 0 ) {
                    metric = RuntimeEvaluationMetrics.METRIC_MEAN;
                } else {
                    throw new RuntimeException("Unknown metric: "+splits[1]);
                }
            } else if( flag.compareTo("Size") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                String rangeStr[] = splits[1].split(":");
                if( rangeStr.length != 2 ) {failed = true; break;}
                minSize = Integer.parseInt(rangeStr[0]);
                maxSize = Integer.parseInt(rangeStr[1]);
                System.out.println("Set plot min/max matrix size to: "+minSize+" "+maxSize);
            } else if( flag.compareTo("Display") ==0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                displayResults = Boolean.parseBoolean(splits[1]);
                System.out.println("Display = "+displayResults);
            } else if( flag.compareTo("Input") == 0 ) {
                inputDirectories.add(splits[1]);
            } else if( flag.compareTo("InputName") == 0 ) {
                names.add(splits[1]);
            } else {
                System.out.println("Unknown flag: "+flag);
                failed = true;
                break;
            }
        }

        if( !failed && targetLibrary == null ) {
            System.out.println("A target library must be specified");
            failed = true;
        }
        if( !failed && inputDirectories.size() != names.size() ) {
            System.out.println("For each input a name must be specified.");
            failed = true;
        }
        if( !failed ){
            for( int i = 0; i < inputDirectories.size(); i++ ) {
                String dir = inputDirectories.get(i);
                String name = names.get(i);

                app.addPlatform(dir,targetLibrary,name);
            }

            app.minMatrixSize = minSize;
            app.maxMatrixSize = maxSize;
            ComparePlatformResults.displayResults = displayResults;

            app.plot(metric);
        } else {
            printHelp();
        }
     }

    public static void main( String args[] ) {
//        ComparePlatformResultsXml app = new ComparePlatformResultsXml();
//
//        app.addPlatform("results/PentiumM_2010_08",MatrixLibrary.EJML.libraryDirName,"Pentium-M");
//        app.addPlatform("results/Q9400_2010_08",MatrixLibrary.EJML.libraryDirName,"Q9400");
//        app.addPlatform("results/runtime_xeon_2010_08", MatrixLibrary.EJML.libraryDirName,"Xeon");
//
//        app.plot(RuntimeEvaluationMetrics.METRIC_MIN);

        parseInput(args);
    }
}

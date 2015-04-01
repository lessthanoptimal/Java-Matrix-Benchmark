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

import jmbench.impl.LibraryDescription;
import jmbench.impl.LibraryLocation;
import jmbench.tools.runtime.RuntimeEvaluationMetrics;
import jmbench.tools.runtime.RuntimeResults;
import jmbench.tools.stability.UtilXmlSerialization;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Creates plots for all the results in a directory from raw csv files.  Plots are saved
 * int the plots directory.
 *
 * @author Peter Abeles
 */
public class PlotRuntimeResults {

    File directory;

    // should it include native libraries while plotting results
    boolean plotNativeLibraries = true;

    // should it display results to the screen
    public static boolean displayResults = true;

    // only plot results more than this size
    int minMatrixSize = 0;
    // it will only plot results which are of this size or less
    int maxMatrixSize = 0;

    // should the summary plot weight the results based on how long an operation takes?
    boolean weightedSummary=true;

    /**
     * Plots results from XML contained in the specified directory.
     *
     * @param dir Directory containing the results.
     */
    public PlotRuntimeResults( String dir ) {
        directory = new File(dir);

        if( !directory.exists() ) {
            throw new IllegalArgumentException("Directory does not exist.");
        }

        if( !directory.isDirectory() ) {
            throw new IllegalArgumentException("Need to specify a directory.");
        }
    }

    @SuppressWarnings({"unchecked"})
    public void plot(int whichMetric) {
        String[] files = directory.list();

        Map<String, List> opMap = new HashMap<String,List>();

        for( String nameLevel0 : files ) {
            File level0 = new File(directory.getPath()+"/"+nameLevel0);

            if( level0.isDirectory() ) {
                if( level0.getName().compareTo("plots") == 0 )
                    continue;

                // see if it should include this library in the results or not
                if( !checkIncludeLibrary(level0.getAbsolutePath()))
                    continue;

                String []files2 = level0.list();

                for( String name2 : files2 ) {
                    if( name2.contains(".csv") ) {

                        String stripName = name2.substring(0,name2.length()-4);
                        name2 = level0.getPath()+"/"+name2;

                        RuntimeResults r = RuntimeResultsCsvIO.read(new File(name2));

                        List l;
                        if( opMap.containsKey(stripName) ) {
                            l = opMap.get(stripName);
                        } else {
                            l = new ArrayList();
                            opMap.put(stripName,l);
                        }
                        l.add(r);
                    }
                }
            }

        }

        createPlots(minMatrixSize,maxMatrixSize,directory,whichMetric, opMap, weightedSummary);
    }

    public static void createPlots( int minMatrixSize , int maxMatrixSize ,
                                    File outputDirectory , int whichMetric,
                                    Map<String, List> opMap , boolean weightedSummary ) {
        List<RuntimePlotData> allResults = new ArrayList<RuntimePlotData>();

        RuntimeResultPlotter.Reference refType = RuntimeResultPlotter.Reference.MAX;

        for( String key : opMap.keySet() ) {
            List<RuntimeResults> l = opMap.get(key);

            RuntimePlotData plotData = convertToPlotData(l,whichMetric);
            allResults.add( plotData );

            truncatePlotData(minMatrixSize,maxMatrixSize,plotData);

            String fileNameVar = outputDirectory.getPath()+"/plots/variability/"+key;
            String fileNameRel = outputDirectory.getPath()+"/plots/relative/"+key;
            String fileNameAbs = outputDirectory.getPath()+"/plots/absolute/"+key;


            RuntimeResultPlotter.variabilityPlots(l, fileNameVar,true,false);
            RuntimeResultPlotter.relativePlots(plotData, refType,null,fileNameRel,plotData.plotName,true,displayResults);
            RuntimeResultPlotter.absolutePlots(plotData, fileNameAbs,plotData.plotName,true,false);
        }

        RuntimeResultPlotter.summaryPlots(allResults,refType,weightedSummary,outputDirectory.getPath()+"/plots",true,displayResults);
        RuntimeResultPlotter.summaryAreaPlot(allResults,refType,outputDirectory.getPath()+"/plots",true,displayResults);
    }

    /**
     * Removes data outside of the requested min and max matrix size
     */
    public static void truncatePlotData( int minMatrixSize , int maxMatrixSize , RuntimePlotData data ) {
        if( maxMatrixSize == 0 )
            return;

        // find the new max matrix index
        int off = -1;
        int N = 0;
        for( ; N < data.matrixSize.length; N++ ) {
            if( off < 0 && data.matrixSize[N] >=  minMatrixSize ) {
                off = N;
            }
            if( data.matrixSize[N] > maxMatrixSize )
                break;
        }

        // truncate matrix size
        int matrixSize[] = new int[N-off];
        System.arraycopy(data.matrixSize,off,matrixSize,0,N-off);
        data.matrixSize = matrixSize;

//        for( int i = 0; i < data.results.size(); i++ ) {
        for( RuntimePlotData.SourceResults s : data.libraries) {
            double []r = s.results;
            double []trunc = new double[N-off];

            System.arraycopy(r,off,trunc,0,N-off);
            s.results = trunc;
        }
    }

    public static RuntimePlotData convertToPlotData( List<RuntimeResults> results , int whichMetric ) {
        RuntimeResults a = results.get(0);

        RuntimePlotData ret = new RuntimePlotData(a.matDimen);

        ret.plotName = a.getOpName();

        for( int i = 0; i < results.size(); i++ ) {
            a = results.get(i);

            double r[] = new double[ a.matDimen.length ];

            for( int j = 0; j < r.length; j++ ) {
                RuntimeEvaluationMetrics m = a.getMetrics()[j];
                if( m != null )
                    r[j] = m.getMetric(whichMetric);
                else
                    r[j] = Double.NaN;
            }

            LibraryLocation lib = LibraryLocation.lookup(a.getLibraryName());
            ret.addLibrary(lib.getPlotName(),r,lib.getPlotLineType());
        }

        return ret;
    }

    /**
     * Checks to see if the results from this library are being filters out or not
     *
     * @param pathDir Path to results directory
     * @return true if it should be included
     */
    private boolean checkIncludeLibrary(String pathDir) {
        LibraryDescription target = UtilXmlSerialization.deserializeXml(pathDir + ".xml");

        if( target == null ) {
            // no library info associated with this directory so its probably not a results directory
            return false;
        }

        return !(target.location.isNativeCode() && !plotNativeLibraries);
    }

    /**
     * Returns the path to the most recently modified directory in results.
     */
    public static String findMostRecentDirectory() {
        File baseDir = new File("results");

        if( !baseDir.exists() ) {
            throw new RuntimeException("Can't find results directory.  Try running one level up from it.");
        }

        File bestDir = null;
        long bestTime = 0;

        for( File f : baseDir.listFiles() ) {
            if( f.isDirectory() ) {
                long time = f.lastModified();
                if( time > bestTime ) {
                    bestDir = f;
                    bestTime = time;
                }
            }
        }

        if( bestDir == null ) {
            throw new RuntimeException("Can't find any directories in results/");
        }

        return bestDir.getPath();
    }

    public static void printHelp() {
        System.out.println("Creates plots from raw XML file results.  The plots can be generated from " +
                "different statistical metric and filtered based on library features.");
        System.out.println();
        System.out.println("--PlotNative=<true|false>      : Turns plotting results from native libraries on and off.");
        System.out.println("--Metric=<?>                   : Changes the metric that is plotted.");
        System.out.println("                               : MAX,MIN,STDEV,MEDIAN,MEAN");
        System.out.println("--Display=<true|false>         : If true some results will be displayed.");
        System.out.println("--Size=min:max                 : Only plot data from matrix size min to max inclusive.");
        System.out.println("--WeightedSummary=<true|false> : Should the summary chart weight operations more if they take longer?");
        System.out.println();
        System.out.println("The last argument is the directory that contains the results.  If this is not specified");
        System.out.println("then the most recently modified directory is used.");
    }

    public static void main( String args[] ) {
        String inputDirectory = null;
        boolean plotNative = true;
        int metric = RuntimeEvaluationMetrics.METRIC_MAX;
        boolean displayResults = true;

        boolean failed = false;

        int maxSize = 0;
        int minSize = 0;

        boolean weightedSummary = true;

        for( int i = 0; i < args.length; i++ ) {
            String splits[] = args[i].split("=");

            String flag = splits[0];

            if( flag.length() < 2 || flag.charAt(0) != '-' || flag.charAt(0) != '-') {
                inputDirectory = args[i];
                break;
            }

            flag = flag.substring(2);

            if( flag.compareTo("PlotNative") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                plotNative = Boolean.parseBoolean(splits[1]);
                System.out.println("PlotNative = "+plotNative);
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
            } else if( flag.compareTo("WeightedSummary") == 0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                weightedSummary = Boolean.parseBoolean(splits[1]);
                System.out.println("WeightedSummary = "+weightedSummary);
            } else if( flag.compareTo("Display") ==0 ) {
                if( splits.length != 2 ) {failed = true; break;}
                displayResults = Boolean.parseBoolean(splits[1]);
                System.out.println("Display = "+displayResults);
            } else {
                System.out.println("Unknown flag: "+flag);
                failed = true;
                break;
            }
        }

        if( failed ) {
            printHelp();
            throw new RuntimeException("Parsing arguments failed");
        }

        if( inputDirectory == null )
            inputDirectory = findMostRecentDirectory();

        System.out.println("Parsing "+inputDirectory);

        PlotRuntimeResults p = new PlotRuntimeResults(inputDirectory);

        p.plotNativeLibraries = plotNative;
        p.displayResults = displayResults;
        p.minMatrixSize = minSize;
        p.maxMatrixSize = maxSize;
        p.weightedSummary = weightedSummary;
        p.plot(metric);
    }
}

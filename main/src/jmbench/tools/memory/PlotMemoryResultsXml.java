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

import jmbench.plots.MemoryRelativeBarPlot;
import jmbench.tools.runtime.evaluation.PlotRuntimeResults;
import jmbench.tools.stability.UtilXmlSerialization;

import java.io.*;
import java.util.*;


/**
 * @author Peter Abeles
 */
public class PlotMemoryResultsXml {

    int plotWidth = 1100;
    int plotHeight = 300;

    File directory;
    private static final boolean displayResults = true;
    private static final boolean plotFailed = false;

    // specifies which metric is used to compare libraries.  1.0 = max 0.0 = min
    double scoreFrac = 0.0;
    MemoryConfig config;

    public PlotMemoryResultsXml( String dir ) {
        System.out.println("Reading "+dir);
        directory = new File(dir);

        if( !directory.exists() ) {
            throw new IllegalArgumentException("Directory does not exist.");
        }

        if( !directory.isDirectory() ) {
            throw new IllegalArgumentException("Need to specify a directory.");
        }
    }

    public void plot() {
        config = UtilXmlSerialization.deserializeXml(directory.getAbsolutePath()+"/../config.xml");
        if( config == null )
            throw new RuntimeException("Couldn't load saved benchmark config file");

        Map<String, List<MemoryResults>> opMap = parseResults();

        int size = readSize();
        plotResults(opMap,size);
    }

    private int readSize() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(directory.getAbsolutePath()+"/size.txt"));
            String s = reader.readLine();
            reader.close();
            return Integer.parseInt(s);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, List<MemoryResults>> parseResults() {
        String[] files = directory.list();

        Map<String, List<MemoryResults>> opMap = new HashMap<String,List<MemoryResults>>();

        for( String nameLevel0 : files ) {
            File level0 = new File(directory.getPath()+"/"+nameLevel0);

            if( level0.isDirectory() ) {

                String []files2 = level0.list();

                for( String name2 : files2 ) {
                    if( name2.contains(".xml") ) {

                        String stripName = name2.substring(0,name2.length()-4);
                        name2 = level0.getPath()+"/"+name2;

                        MemoryResults r;
                        try {
                            r = UtilXmlSerialization.deserializeXml(name2);
                        } catch( ClassCastException e ) {
                            System.out.println("Couldn't deserialize "+name2);
                            continue;
                        }

                        System.out.printf("%10.10s  %12s ",nameLevel0,r.getDisplayName());
                        r.printStatistics();

                        List<MemoryResults> l;
                        if( opMap.containsKey(stripName) ) {
                            l = opMap.get(stripName);
                        } else {
                            l = new ArrayList<MemoryResults>();
                            opMap.put(stripName,l);
                        }
                        l.add(r);
                    }
                }
            }

        }
        return opMap;
    }

    private void plotResults( Map<String, List<MemoryResults>> opMap , int matrixSize ) {
        MemoryRelativeBarPlot plot = new MemoryRelativeBarPlot("Library Memory Usage: Size "+matrixSize);

        // Sort the keys to ensure the order is the same each time
        List<String> keys = new ArrayList<String>(opMap.keySet());
        Collections.sort(keys);

        for( String key : keys ) {
            List<MemoryResults> l = opMap.get(key);

            // put into alphabetical order
            Collections.sort(l,new CompareByLibName());

            MemoryPlotData plotData = convertToPlotData(l);

            int N = plotData.libNames.size();

            for( int i = 0; i < N; i++ ) {
                plot.addResult(key,plotData.libNames.get(i),plotData.memory.get(i));
            }
        }

        plot.displayWindow(plotWidth,plotHeight);
        plot.savePDF(directory.getPath()+"/plot_memory.pdf",plotWidth,plotHeight);
    }

    private static class CompareByLibName implements Comparator<MemoryResults>
    {

        @Override
        public int compare(MemoryResults o1, MemoryResults o2) {
            return o1.getNameLibrary().compareTo(o2.getNameLibrary());
        }
    }

    private MemoryPlotData convertToPlotData( List<MemoryResults> l ) {
        long max = 0;

        for( MemoryResults m : l ) {
            if( !m.results.isEmpty() && (plotFailed || m.numFailed == 0) ) {
                long d = m.getScore(scoreFrac);
                if( d < Long.MAX_VALUE && max < d )
                    max = d;
            }
        }

        MemoryPlotData data = new MemoryPlotData();

        for( int i = 0; i < l.size(); i++ ) {
            MemoryResults m = l.get(i);

            if( !m.results.isEmpty() && (plotFailed || m.numFailed == 0) ) {

                long val = m.getScore(scoreFrac);

                if( val == Long.MAX_VALUE || val == 0)
                    continue;

                data.libNames.add( m.getNameLibrary() );
                data.memory.add( (double)val/(double)max );
            }
        }

        return data;
    }

    public static void main( String args[] ) {

        String dir;

        if( args.length == 0 ) {
            dir = PlotRuntimeResults.findMostRecentDirectory();
        } else {
            dir = args[0];
        }

        //dir = "results/memory_2010_04";

        PlotMemoryResultsXml plotter = new PlotMemoryResultsXml(dir+"/1000");
        plotter.plot();
        plotter = new PlotMemoryResultsXml(dir+"/2000");
        plotter.plot();
        plotter = new PlotMemoryResultsXml(dir+"/3000");
        plotter.plot();
    }
}

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

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Read in CSV files and plot the results.
 *
 * @author Peter Abeles
 */
@Deprecated
public class PlotRuntimeResultsCsv {

    File directory;

    public PlotRuntimeResultsCsv( String dir ) {
        directory = new File(dir);

        if( !directory.exists() ) {
            throw new IllegalArgumentException("Directory does not exist.");
        }

        if( !directory.isDirectory() ) {
            throw new IllegalArgumentException("Need to specify a directory.");
        }
    }

    @SuppressWarnings({"unchecked"})
    public void plot() throws IOException {
        String[] files = directory.list();

        Map<String, List> opMap = new HashMap<String,List>();

        for( String nameLevel0 : files ) {
            if( !nameLevel0.contains(".csv"))
                continue;

            String opName = nameLevel0.split("[.]")[0];

            System.out.println("processing op: "+opName);

            BufferedReader r =
                        new BufferedReader(new InputStreamReader(new FileInputStream(directory.getAbsolutePath()+"/"+nameLevel0)));

            // read in the results
            RuntimePlotData plotData = readResults(r);

            // create the plots
            savePlots(opName,plotData);
        }
    }

    private void savePlots( String plotName , RuntimePlotData data ) {
        String fileNameRel = directory.getPath()+"/plots/relative/"+plotName;
        String fileNameAbs = directory.getPath()+"/plots/absolute/"+plotName;

        RuntimeResultPlotter.Reference refType = RuntimeResultPlotter.Reference.MAX;
        RuntimeResultPlotter.relativePlots(data, refType,null,fileNameRel,plotName,true,true);
        RuntimeResultPlotter.absolutePlots(data, fileNameAbs,plotName,true,false);
    }

    /**
     * Creates the results in for a particular file ignoring comments.
     */
    private RuntimePlotData readResults( BufferedReader r ) throws IOException {
        // read the file in as a series of string arrays
        String[] libs = readLine(r);
        String[] libIds = readLine(r);

        List<String[]> data = new ArrayList<String[]>();

        for( String[] a = readLine(r); a != null; a = readLine(r) ) {
            data.add(a);
        }

        int matrixSize[] = new int[ data.size() ];
        for( int i = 0; i < matrixSize.length; i++ ) {
            matrixSize[i] = Integer.parseInt(data.get(i)[0]);
        }

        // TODO check to see if this is totally hosed after some changes in October
        // convert results from strings to numbers
        RuntimePlotData ret = new RuntimePlotData(matrixSize);

        for( int i = 0; i < libs.length-1; i++ ) {

            double results[] = new double[ matrixSize.length ];

            for( int indexResults = 0; indexResults < matrixSize.length; indexResults++ ) {
                String[] d = data.get(i);

                results[indexResults] = Double.parseDouble(d[indexResults+1]);
            }

            ret.addLibrary(libs[i+1],results,Integer.parseInt(libIds[i]));
        }



        return ret;
    }

    /**
     * Reads a line from the file and splits it into substrings based upon whitespace.
     * Lines that begin with the comment character are ignored.  Relative runtime plots are displayed
     * on the screen and both relative runtime and absolute results are saved in the plots directory.
     */
    public static String[] readLine( BufferedReader r ) throws IOException {
        while( true ) {
            String line = r.readLine();
            if( line == null )
                return null;
            if( line.length() == 0 )
                continue;
            
            if( line.charAt(0) == '#')
                continue;

            String[] raw = line.split("\\s");

            // strip away empty strings
            int count = 0;
            for( int i = 0; i < raw.length; i++ ) {
                if( raw[i].length() > 0 )
                    count++;
            }

            String[] ret = new String[count];

            int c = 0;
            for( int i = 0; i < raw.length; i++ ) {
                if( raw[i].length() > 0 )
                    ret[c++] = raw[i];
            }

            return ret;
        }
    }

    public static void main( String args[] ) throws IOException {

        String inputDirectory = args.length == 0 ? PlotRuntimeResults.findMostRecentDirectory() : args[0];

        PlotRuntimeResultsCsv p = new PlotRuntimeResultsCsv(inputDirectory);

        p.plot();
    }
}
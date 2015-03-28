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

import jmbench.impl.LibraryLocation;
import jmbench.tools.runtime.RuntimeEvaluationMetrics;
import jmbench.tools.runtime.RuntimeResults;
import jmbench.tools.stability.UtilXmlSerialization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Converts results from xml format into a CSV (comma space value) format. '#' is a comment character
 * and tabs \\t denote each data element.
 *
 * @author Peter Abeles
 */
@Deprecated
public class ConvertRuntimeResultsXmlToCSV {

    File directory;

    public ConvertRuntimeResultsXmlToCSV( String dir ) {
        directory = new File(dir);

        if( !directory.exists() ) {
            throw new IllegalArgumentException("Directory does not exist");
        }

        if( !directory.isDirectory() ) {
            throw new IllegalArgumentException("Need to specify a directory");
        }
    }

    /**
     * Reads in the results from serialized XML files and converts them into a stripped down CSV format.
     *
     * @param whichMetric Which performance metric is being used.
     */
    @SuppressWarnings({"unchecked"})
    public void convert(int whichMetric) {
        String[] files = directory.list();

        Map<String, List> opMap = new HashMap<String,List>();

        for( String nameLevel0 : files ) {
            File level0 = new File(directory.getPath()+"/"+nameLevel0);

            if( level0.isDirectory() ) {
                String []files2 = level0.list();

                for( String name2 : files2 ) {
                    if( name2.contains(".xml") ) {

                        String stripName = name2.substring(0,name2.length()-4);
                        name2 = level0.getPath()+"/"+name2;

                        RuntimeResults r = UtilXmlSerialization.deserializeXml(name2);

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
        for( String key : opMap.keySet() ) {
            List<RuntimeResults> l = opMap.get(key);

            String fileName = directory.getPath()+"/"+key+".csv";
            System.out.println("Writing file: "+fileName);
            try {
                File temp = new File(fileName);
                if( temp.exists() )
                    if( !temp.delete() )
                        throw new RuntimeException("Can't delete old file. "+fileName);

                PrintStream fileStream = new PrintStream(new FileOutputStream(fileName));
                printResults(fileStream,l,whichMetric,key);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Saves the results into a tab separated file.
     *
     * @param fileStream
     * @param results
     * @param metricType
     * @param opName
     */
    private void printResults( PrintStream fileStream,
                               List<RuntimeResults> results ,
                               int metricType ,
                               String opName) {


        fileStream.println("# Operation: "+opName);
        fileStream.println("# Metric:    "+metricType);
        
        // save all the names of each library
        fileStream.print("size\t");
        for( RuntimeResults r : results ) {
            fileStream.print("\t"+r.getLibraryName());
        }
        fileStream.println();

        // save the library number so that the plots use the same color line when plotting
        fileStream.print("\t");
        for( RuntimeResults r : results ) {
            LibraryLocation lib = LibraryLocation.lookup(r.getLibraryName());
            fileStream.print("\t"+lib.getPlotLineType());
        }
        fileStream.println();

        int matrixSize[] = findMaxMatrixSize(results);

        for( int indexMatrix = 0; indexMatrix < matrixSize.length; indexMatrix++ ) {
            int s = matrixSize[indexMatrix];

            // print the size of this matrix
            fileStream.print(s);

            // print results for each library at this size
            for( RuntimeResults r : results ) {
//                fileStream.print(r.getLibrary());

                // sanity check
                if( r.getMatDimen()[indexMatrix] != s ) {
                    throw new RuntimeException("Matrix size miss match");
                }

                RuntimeEvaluationMetrics m[] = r.getMetrics();

                if( m.length > indexMatrix && m[indexMatrix] != null )
                    fileStream.print("\t"+m[indexMatrix].getMetric(metricType));
                else
                    fileStream.print("\t"+Double.NaN);

            }
            fileStream.println();
        }
        fileStream.println();

    }

    private int[] findMaxMatrixSize( List<RuntimeResults> results) {
        int max = 0;
        int arrayMax[] = null;

        for( RuntimeResults r : results ) {
            int d[] = r.matDimen;

            if( d.length > max ) {
                max = d.length;
                arrayMax = d;
            }
        }

        return arrayMax;
    }

    public static void main( String args[] ) {

        String dir = args.length == 0 ? PlotRuntimeResults.findMostRecentDirectory() : args[0];

        ConvertRuntimeResultsXmlToCSV p = new ConvertRuntimeResultsXmlToCSV(dir);

        p.convert(RuntimeEvaluationMetrics.METRIC_MAX);

        System.out.println("Done");
    }
}
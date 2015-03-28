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

package jmbench.tools.stability;

import jmbench.impl.LibraryLocation;

import java.io.File;
import java.util.*;


/**
 * Creates plots for all the results in a directory
 *
 * @author Peter Abeles
 */
public abstract class TablesCommon {

    File directory;

    public TablesCommon( String dir ) {
        directory = new File(dir);

        if( !directory.exists() ) {
            throw new IllegalArgumentException("Directory does not exist");
        }

        if( !directory.isDirectory() ) {
            throw new IllegalArgumentException("Need to specify a directory");
        }

//        System.out.println("Reading results from: "+dir);
//        System.out.println();
    }

    @SuppressWarnings({"unchecked"})
    public void plot() {
//        System.out.println("loading files: ");
        String[] files = directory.list();

        Map<String, List> opMap = new HashMap<String,List>();

        for( String nameLevel0 : files ) {
            File level0 = new File(directory.getPath()+"/"+nameLevel0);

            if( level0.isDirectory() ) {
                String []files2 = level0.list();
//                System.out.println(level0);

                for( String name2 : files2 ) {
                    if( name2.contains(".xml") ) {
//                        System.out.print(name2+" ");

                        String stripName = name2.substring(0,name2.length()-4);
                        name2 = level0.getPath()+"/"+name2;

                        StabilityTrialResults r = UtilXmlSerialization.deserializeXml(name2);

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
//            System.out.println();

        }

        printTables(opMap);

    }

    protected abstract void printTables( Map<String,List> opMap );

    protected List<String> getLibraryNames( Map<String, List> opMap ) {
        List<String> names = new ArrayList<String>();

        Set<Map.Entry<String,List>> entries = opMap.entrySet();

        for( Map.Entry<String,List> e : entries ) {
            List<StabilityTrialResults> l = e.getValue();

            for( StabilityTrialResults s : l ) {
                LibraryLocation lib = LibraryLocation.lookup(s.getLibraryName());
                
                if( !names.contains(lib.getPlotName()) )
                    names.add(lib.getPlotName());
            }
        }

        // sort the names to ensure the order is consistent and not arbitrary
        Collections.sort(names);

        return names;
    }

    protected Data findByName(List<StabilityTrialResults> l , String name )
    {
        for( StabilityTrialResults s : l ) {
            LibraryLocation lib = LibraryLocation.lookup(s.getLibraryName());

            if( name.compareTo(lib.getPlotName()) == 0 ) {
                Data d = new Data();

                List<Double> breaking = s.getBreakingPoints();
                int totalTrials = breaking.size();

                d.fatalError = s.fatalError;
                if( d.fatalError == null ) {
                    d.per50 = StabilityBenchmark.computePercent(breaking,0.5);
                    d.per10 = StabilityBenchmark.computePercent(breaking,0.10);
                    d.per90 = StabilityBenchmark.computePercent(breaking,0.90);
                    d.fracUncount = (double)(s.numUncountable)/(double)totalTrials;
                    d.fracUnexpected = (double)(s.numUnexpectedException)/(double)totalTrials;
                    d.fracLargeError = (double)(s.numLargeError)/(double)totalTrials;
                    d.fracDetected = (double)(s.numGraceful)/(double)totalTrials;
                }

                return d;
            }
        }
        return null;
    }



    public static class Data
    {
        double per50;
        double per10;
        double per90;
        double fracUncount;
        double fracUnexpected;
        double fracDetected;
        public double fracLargeError;
        public FatalError fatalError;
    }

}
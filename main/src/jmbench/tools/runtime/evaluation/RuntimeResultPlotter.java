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
import jmbench.plots.OperationsVersusSizePlot;
import jmbench.plots.OverallRelativeAreaPlot;
import jmbench.plots.SummaryWhiskerPlot;
import jmbench.tools.runtime.RuntimeEvaluationMetrics;
import jmbench.tools.runtime.RuntimeResults;

import java.util.*;


/**
 * Generates different plots of the results.
 *
 * @author Peter Abeles
 */
public class RuntimeResultPlotter {

    public static void summaryPlots( List<RuntimePlotData> allResults , Reference referenceType , boolean weighted ,
                                     String outputDirectory ,
                                     boolean savePDF ,
                                     boolean showWindow ) {

        Map<String,List<OverallData>> overallResults = new HashMap<String,List<OverallData>>();

        // find the number of matrices sizes tested
        int numMatrices = 0;
        for( RuntimePlotData opResults : allResults ) {
            if( opResults.matrixSize.length > numMatrices )
                numMatrices = opResults.matrixSize.length;
        }

        // find the relative speed of each operation for each matrix size so that they can be weighted
        double slowestOperationByMatrix[] = new double[ numMatrices ];
        for( RuntimePlotData opResults : allResults ) {
            for( int i = 0; i < numMatrices; i++ ) {
                double bestSpeed = opResults.findBest(i);

                if( Double.isNaN(bestSpeed) )
                    continue;

                // convert from ops/sec to sec/op
                bestSpeed = 1.0/bestSpeed;

                if( bestSpeed > slowestOperationByMatrix[i] )
                    slowestOperationByMatrix[i] = bestSpeed;
            }
        }

        for( RuntimePlotData opResults : allResults ) {
            int numMatrixSizes = opResults.matrixSize.length;

            // find the performance for each matrix size that each library will
            // be compared against
            double refValue[] = new double[ numMatrixSizes ];
            computeReferenceValues(opResults, referenceType, -1, numMatrixSizes, refValue);

            // find the largest matrix with results in it
            int maxValidMatrix = opResults.getNumMatrices();

            for( RuntimePlotData.SourceResults r : opResults.libraries ) {
                List<OverallData> libOverall;

                if( !overallResults.containsKey(r.label)) {
                    libOverall = new ArrayList<OverallData>();
                    overallResults.put(r.label,libOverall);
                } else {
                    libOverall = overallResults.get(r.label);
                }

                for( int i = 0; i <= maxValidMatrix; i++ ) {
                    // the weight is determined by how slow this operation is relative to the slowest
                    double weight = (1.0/refValue[i])/slowestOperationByMatrix[i];

                    double a = r.getResult(i);
                    if( !Double.isNaN(a) && a >= 0 ) {
                        // its relative ranking compared to other libraries in this operation
                        double score = a/refValue[i];
                        libOverall.add(new OverallData(weight,score,i));
                    } else {
                        libOverall.add(new OverallData(weight,0.0,i));
                    }
                }
            }
        }

        // If set to one results will not be weighted
        int maxSamples = weighted ? 100 : 1;

        String title = "Summary of Runtime Performance";
        String subtitle = weighted ? "Weighted by Operation Time" : null;

        SummaryWhiskerPlot plot = new SummaryWhiskerPlot(title,subtitle);

        // sort the names so that they appear in a consistent order
        List<String> orderedNames = new ArrayList<String>();
        orderedNames.addAll( overallResults.keySet());
        Collections.sort(orderedNames);

        for( String libName : orderedNames ) {
            List<OverallData> libOverall = overallResults.get(libName);

            plot.addLibrary(libName,
                    addSample(libOverall,0,numMatrices,maxSamples),
                    addSample(libOverall,numMatrices-5,numMatrices,maxSamples),
                    addSample(libOverall,0,5,maxSamples));
        }

        if( showWindow )
            plot.displayWindow(1000,450);

        if( savePDF )
            plot.savePDF(outputDirectory+"/summary_bar.pdf",1000,450);
    }

    public static void summaryAreaPlot( List<RuntimePlotData> allResults , Reference referenceType ,
                                        String outputDirectory ,
                                        boolean savePDF ,
                                        boolean showWindow ) {

        Map<String,OverallSizeData> overallResults = new HashMap<String,OverallSizeData>();

        // find the number of matrices sizes tested
        int numMatrices = 0;
        int sizes[] = null;
        for( RuntimePlotData opResults : allResults ) {
            if( opResults.matrixSize.length > numMatrices ) {
                numMatrices = opResults.matrixSize.length;
                sizes = opResults.matrixSize;
            }
        }

        // find the relative speed of each operation for each matrix size so that they can be weighted
        double slowestOperationByMatrix[] = new double[ numMatrices ];
        for( RuntimePlotData opResults : allResults ) {
            for( int i = 0; i < numMatrices; i++ ) {
                double bestSpeed = opResults.findBest(i);

                if( Double.isNaN(bestSpeed) )
                    continue;

                // convert from ops/sec to sec/op
                bestSpeed = 1.0/bestSpeed;

                if( bestSpeed > slowestOperationByMatrix[i] )
                    slowestOperationByMatrix[i] = bestSpeed;
            }
        }

        for( RuntimePlotData opResults : allResults ) {
            int numMatrixSizes = opResults.matrixSize.length;

            // find the performance for each matrix size that each library will
            // be compared against
            double refValue[] = new double[ numMatrixSizes ];
            computeReferenceValues(opResults, referenceType, -1, numMatrixSizes, refValue);

            for( RuntimePlotData.SourceResults r : opResults.libraries ) {
                OverallSizeData libOverall;

                if( !overallResults.containsKey(r.label)) {
                    libOverall = new OverallSizeData(sizes.length);
                    libOverall.plotLineType = r.plotLineType;
                    overallResults.put(r.label,libOverall);
                } else {
                    libOverall = overallResults.get(r.label);
                }

                for( int i = 0; i < numMatrixSizes; i++ ) {
                    // the weight is determined by how slow this operation is relative to the slowest
                    double weight = (1.0/refValue[i])/slowestOperationByMatrix[i];

                    double a = r.getResult(i);

                    if( !Double.isNaN(a) && a >= 0 ) {
                        // its relative ranking compared to other libraries in this operation
                        double score = a/refValue[i];
                        libOverall.scoreWeighted[i] += score*weight;
                        libOverall.weights[i] += weight;
                    } else {
                        // If an operation could not be completed penalize it by setting its score to zero
                        libOverall.weights[i] += weight;
                    }
                }
            }
        }

        String title = "Summary of Runtime Performance";
        OperationsVersusSizePlot plotLine = new OperationsVersusSizePlot(title,"Relative Average Speed");
        plotLine.setSubTitle("Weighted by Operation Speed. Larger is Better.");
        plotLine.setLogScale(false, true);
        plotLine.setRange(0, 1.1);
        OverallRelativeAreaPlot plotArea = new OverallRelativeAreaPlot(title,sizes);

        // sort the names so that they appear in a consistent order
        List<String> orderedNames = new ArrayList<String>();
        orderedNames.addAll( overallResults.keySet());
        Collections.sort(orderedNames);

        double total[] = new double[ sizes.length ];
        for( String libName : orderedNames ) {
            OverallSizeData libOverall = overallResults.get(libName);

            for( int i = 0; i < sizes.length; i++ ) {
                double w = libOverall.weights[i];
                if( w > 0 ) {
                    total[i] += libOverall.scoreWeighted[i]/w;
                }
            }
        }

        for( String libName : orderedNames ) {
            OverallSizeData libOverall = overallResults.get(libName);

            double scoresLine[] = new double[ sizes.length ];
            double scoresArea[] = new double[ sizes.length ];

            for( int i = 0; i < sizes.length; i++ ) {
                double w = libOverall.weights[i];
                if( w > 0 ) {
                    scoresLine[i] = libOverall.scoreWeighted[i]/w;
                    scoresArea[i] = libOverall.scoreWeighted[i]/(w*total[i]);
                } else {
                    scoresLine[i] = Double.NaN;
                    scoresArea[i] = Double.NaN;
                }
            }

            plotLine.addResults(sizes, scoresLine, libName, sizes.length, libOverall.plotLineType);
            plotArea.addLibrary(libName, libOverall.plotLineType, scoresArea);
        }

        if( showWindow ) {
            plotLine.displayWindow(650, 450);
            plotArea.displayWindow(700, 450);
        }

        try {Thread.sleep(500);} catch (InterruptedException e) {}

        if( savePDF ) {
            plotLine.savePDF(outputDirectory + "/summary_size.pdf", 650, 450);
            plotArea.savePDF(outputDirectory + "/summary_stacked_area.pdf", 700, 450);
        }
    }


    /**
     * For each result it will add the score a number of times depending upon its weight.
     *
     * @param results benchmark results across all the trials.
     * @param minIndex Only consider matrices that are this size or more.
     * @param maxIndex Only consider matrices that are less than this size.
     * @param maxSamples The maximum number of samples that can be added per result.
     * @return  List containing weighted results.
     */
    private static List<Double> addSample( List<OverallData> results , int minIndex , int maxIndex , int maxSamples ) {

        List<Double> ret = new ArrayList<Double>();

        for( OverallData d : results ) {
            if( d.matrixSize < minIndex || d.matrixSize >= maxIndex )
                continue;

            int num = (int)Math.ceil(d.weight*maxSamples);

            for( int i = 0; i < num; i++ ) {
                ret.add(d.score);
            }
        }

        return ret;
    }

    private static class OverallData
    {
        double weight;
        double score;
        int matrixSize;

        private OverallData(double weight, double score, int matrixSize) {
            this.weight = weight;
            this.score = score;
            this.matrixSize = matrixSize;
        }
    }

    private static class OverallSizeData
    {
        double weights[];
        double scoreWeighted[];
        int plotLineType;

        public OverallSizeData( int numSizes ) {
            weights = new double[ numSizes ];
            scoreWeighted = new double[ numSizes ];
        }
    }

    public static void variabilityPlots( List<RuntimeResults> data ,
                                         String fileName ,
                                         boolean savePDF ,
                                         boolean showWindow )
    {
        String opName = data.get(0).getOpName();
        OperationsVersusSizePlot splot = new OperationsVersusSizePlot(opName,"Ops/Sec Range (%)");

        splot.setLogScale(false,true);
        splot.setRange(0.0,0.4);

        int numMatrixSizes = getNumMatrices(data);

        double results[] = new double[ numMatrixSizes ];
        int matDimen[] = new int[ numMatrixSizes ];

        if( fileName == null ) {
            fileName = opName;
        }

        for( int i = 0; i < numMatrixSizes; i++ ){
            matDimen[i] = getMatrixSize(data,i);
        }

        for( RuntimeResults ops : data ) {
            RuntimeEvaluationMetrics[]metrics = ops.metrics;
            int n = ops.getMatDimen().length;

            for( int i = 0; i < numMatrixSizes; i++ ) {
                if( i < n && metrics[i] != null && metrics[i].getRawResults().size() > 5 ) {
//                    double max = 1.0/metrics[i].getMin();
//                    double min = 1.0/metrics[i].getMax();
                    double max = metrics[i].getMax();
                    double min = metrics[i].getMin();
                    results[i] = (max-min)/max;
//                    results[i] = metrics[i].getStdev()/metrics[i].getMean();
                } else {
                    results[i] = Double.NaN;
                }
            }

            LibraryLocation lib = LibraryLocation.lookup(ops.getLibraryName());
            splot.addResults(matDimen,results,lib.getPlotName(),numMatrixSizes,
                    lib.getPlotLineType());
        }

        if( savePDF )
            splot.savePDF(fileName+".pdf",600,500);
        if( showWindow )
            splot.displayWindow(600, 500);
    }

    public static void absolutePlots( RuntimePlotData data ,
                                      String fileName ,
                                      String opName,
                                      boolean savePDF ,
                                      boolean showWindow )
    {
        OperationsVersusSizePlot splot = new OperationsVersusSizePlot(opName,"Time Per Op (s)");

        splot.setLogScale(true,true);

        int numMatrixSizes = data.matrixSize.length;

        double results[] = new double[ numMatrixSizes ];
        int matDimen[] = data.matrixSize;

        if( fileName == null ) {
            fileName = opName;
        }

        for( RuntimePlotData.SourceResults s : data.libraries) {
            boolean allInvalid = true;
            for( int i = 0; i < numMatrixSizes; i++ ) {
                double libResult = s.getResult(i);
                
                if( !Double.isNaN(libResult) && libResult > 0 ) {
                    allInvalid = false;
                    results[i] = 1.0/libResult;
                } else {
                    results[i] = Double.NaN;
                }
            }

            if( !allInvalid )
                splot.addResults(matDimen,results,s.label,numMatrixSizes,
                        s.plotLineType);
        }

        if( savePDF )
            splot.savePDF(fileName+".pdf",600,500);
        if( showWindow )
            splot.displayWindow(600, 500);
    }

    public static void relativePlots( RuntimePlotData data ,
                                      Reference referenceType ,
                                      String refLib ,
                                      String fileName ,
                                      String opName ,
                                      boolean savePDF ,
                                      boolean showWindow )
    {
        int refIndex = refLib == null ? -1 : data.findLibrary(refLib);


        OperationsVersusSizePlot splot = new OperationsVersusSizePlot(opName,"Relative Performance");

        splot.setLogScale(true,true);
        splot.setRange(0.01,2);

        int numMatrixSizes = data.matrixSize.length;

        double results[] = new double[ numMatrixSizes ];
        double refValue[] = new double[ numMatrixSizes ];
        int matDimen[] = data.matrixSize;

        if( fileName == null ) {
            fileName = opName;
        }

        computeReferenceValues(data, referenceType, refIndex, numMatrixSizes, refValue);

        for( RuntimePlotData.SourceResults s : data.libraries) {
            for( int i = 0; i < numMatrixSizes; i++ ) {
                double libResult = s.getResult(i);

                if( !Double.isNaN(libResult) ) {
                    results[i] = libResult/refValue[i];
                } else {
                    results[i] = Double.NaN;
                }
            }

            splot.addResults(matDimen,results,s.label,numMatrixSizes, s.plotLineType);
        }
        
        if( savePDF )
            splot.savePDF(fileName+".pdf",500,350);
        if( showWindow )
            splot.displayWindow(500, 350);
    }

    private static void computeReferenceValues(RuntimePlotData data,
                                               Reference referenceType,
                                               int refIndex,
                                               int numMatrixSizes,
                                               double[] refValue) {
        if( referenceType == Reference.NONE ) {
            for( int i = 0; i <numMatrixSizes; i++ ) {
                refValue[i] = 1.0;
            }
        } else {
            for( int i = 0; i <numMatrixSizes; i++ ) {
                refValue[i] = getReferenceValue(data,refIndex,i,referenceType);
//                System.out.println("i = "+refValue[i]);
            }
//            System.out.println();
        }
    }

    private static int getNumMatrices( List<RuntimeResults> data ) {
        int max = 0;

        for( RuntimeResults d : data ) {
            int sizes[] = d.getMatDimen();

            if( sizes.length > max )
                max = sizes.length;
        }

        return max;
    }

    private static int getMatrixSize( List<RuntimeResults> data , int index)
    {
        for( RuntimeResults d : data ) {
            int sizes[] = d.getMatDimen();

            if( sizes.length > index ) {
                return sizes[index];
            }
        }

        throw new RuntimeException("Couldn't find a match");
    }

    private static double getReferenceValue( RuntimePlotData data ,
                                             int refIndex ,
                                             int matrixSize ,
                                             Reference referenceType )
    {
        if( referenceType == Reference.LIBRARY ) {
            return data.libraries.get(refIndex).results[matrixSize];
        }

        List<Double> results = new ArrayList<Double>();

        // get results from each library at this matrix size
        for( int i = 0; i < data.libraries.size(); i++ ) {
            double r = data.libraries.get(i).getResult(matrixSize);

            if( Double.isNaN(r) || Double.isInfinite(r)) {
                continue;
            }

            results.add(r);
        }

        if( results.size() == 0 )
            return Double.NaN;

        switch( referenceType ) {
            case MEAN:
                double total = 0;
                for( double d : results )
                    total += d;
                return total / results.size();

            case MEDIAN:
                Collections.sort(results);
                return results.get(results.size()/2);

            case MAX:
                Collections.sort(results);
                return results.get(results.size()-1);
        }

        throw new RuntimeException("Unknown reference type");
    }

    public static enum Reference
    {
        NONE,
        LIBRARY,
        MEAN,
        MEDIAN,
        MAX
    }
}

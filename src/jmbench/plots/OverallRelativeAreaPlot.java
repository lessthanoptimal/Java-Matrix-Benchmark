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

package jmbench.plots;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;


/**
* @author Peter Abeles
*/
public class OverallRelativeAreaPlot {

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    JFreeChart chart;
    CategoryPlot plot;

    String sizeNames[];

    public OverallRelativeAreaPlot(String title , int sizes[] ) {
        // createAreaChart

        chart = ChartFactory.createStackedAreaChart(
                title,      // chart title
                "Size",                // domain axis label
                "Relative Average Speed",                   // range axis label
                dataset,                   // data
                PlotOrientation.VERTICAL,  // orientation
                true,                      // include legend
                true,
                false
        );

        chart.addSubtitle(new TextTitle("Weighted by Operation Speed. Larger is Better.",new Font("SansSerif", Font.ITALIC, 12)));


        plot = chart.getCategoryPlot();

        sizeNames = new String[sizes.length];
        for( int i = 0; i < sizes.length; i++ ) {
            sizeNames[i] = Integer.toString(sizes[i]);
        }

        plot.setRangeGridlinePaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);

        plot.setDomainGridlinesVisible(true);
    }

    int numDataSets = 0;
    public void addLibrary( String name , int seriesIndex , double[] results )
    {
        for( int i = 0; i < sizeNames.length; i++ ) {
            double v = results[i];
            if(!Double.isNaN(v))
                dataset.addValue(v, name, sizeNames[i]);
        }

        StackedAreaRenderer renderer = (StackedAreaRenderer)plot.getRenderer();

        float STROKE_SIZE = 5f;

//        renderer.setSeriesPaint(numDataSets, OperationsVersusSizePlot.getColor(seriesIndex));
        renderer.setSeriesStroke(numDataSets,OperationsVersusSizePlot.getStroke(seriesIndex, STROKE_SIZE,STROKE_SIZE*2));

        numDataSets++;
    }

    public void displayWindow(int width, int height) {

        ChartFrame window = new ChartFrame(chart.getTitle().getText(),chart);

        window.setMinimumSize(new Dimension(width,height));
        window.setPreferredSize(window.getMinimumSize());
        window.setVisible(true);
    }

    public void savePDF( String fileName , int width , int height ) {
        UtilPlotPdf.saveAsPdf(chart,fileName,width,height);
    }

    public static void main( String args[] ) {

        int sizes[] = new int[]{1,2,5,10,20,50,100};

        OverallRelativeAreaPlot plot = new OverallRelativeAreaPlot("Overall Performance",sizes);

        plot.addLibrary("ejml", 0, new double[]{0.1, 0.5, 0.7, 0.8, 1.0, 0.99, 0.8});
        plot.addLibrary("foo", 1,new double[]{1.0, 0.8, 0.65, 0.6, 0.5, 0.4, 0.1});

        plot.displayWindow(400,300);
    }
}

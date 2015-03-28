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
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author Peter Abeles
 */
public class SummaryWhiskerPlot {

    DefaultBoxAndWhiskerCategoryDataset dataSet
            = new DefaultBoxAndWhiskerCategoryDataset();

    String title;
    String subtitle;

    public SummaryWhiskerPlot(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public void addLibrary( String name , List<Double> overall ,
                            List<Double> large , List<Double> small )
    {
        dataSet.add(overall,"All Sizes",name);
        dataSet.add(large,"Only Large",name);
        dataSet.add(small,"Only Small",name);
    }

    public JFreeChart createChart() {
        JFreeChart chart = ChartFactory.createBoxAndWhiskerChart(
                title, "Matrix Libraries", "Relative Performance", dataSet,
                true);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setDomainGridlinesVisible(true);
        plot.setBackgroundPaint(new Color(230,230,230));
        plot.setDomainGridlinePaint(new Color(50,50,50,50));
        plot.setDomainGridlineStroke(new BasicStroke(78f));

        chart.getTitle().setFont(new Font("Times New Roman", Font.BOLD, 24));

        String foo = "( Higher is Better )";
        if( subtitle != null )
            foo += "      ( "+subtitle+" )";

        chart.addSubtitle(new TextTitle(foo,new Font("SansSerif", Font.ITALIC, 12)));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());


        return chart;
    }

    public void displayWindow(int width, int height) {

        JFreeChart chart = createChart();

        ChartFrame window = new ChartFrame(chart.getTitle().getText(),chart);

        window.setMinimumSize(new Dimension(width,height));
        window.setPreferredSize(window.getMinimumSize());
        window.setVisible(true);
    }

    public void saveJpeg( String fileName , int width , int height ) {
        UtilPlotPdf.saveAsJpeg(createChart(),fileName,width,height,0.95);
    }

    public void savePDF( String fileName , int width , int height ) {
        UtilPlotPdf.saveAsPdf(createChart(),fileName,width,height);
    }

    public static void main( String args[] ) {
        Random rand = new Random(2344);

        SummaryWhiskerPlot plot = new SummaryWhiskerPlot("Test Summary","Weighted by Operation Time");

        for( int i = 0; i < 3; i++ ) {
            List<Double> overall = new ArrayList<Double>();
            List<Double> large = new ArrayList<Double>();
            List<Double> small = new ArrayList<Double>();

            for( int j = 0; j < 50; j++ ) {
                overall.add( rand.nextDouble() );
                large.add( rand.nextDouble() );
                small.add( rand.nextDouble() );
            }

            plot.addLibrary("Lib "+i,overall,large,small);
        }

        plot.displayWindow(600,350);
    }

}

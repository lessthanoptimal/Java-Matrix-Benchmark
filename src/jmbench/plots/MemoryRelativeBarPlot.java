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
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;


/**
 * @author Peter Abeles
 */
public class MemoryRelativeBarPlot {

    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    CategoryPlot plot;

    JFreeChart chart;

    public MemoryRelativeBarPlot( String title ) {
        chart = ChartFactory.createBarChart(
                title,       // chart title
                "Operation",               // domain axis label
                "Relative Memory",                  // range axis label
                dataset,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips?
                false                     // URLs?
        );
        chart.addSubtitle(new TextTitle("(Smaller is Better)",new Font("SansSerif", Font.ITALIC, 12)));

        plot();
    }

    public void addResult( String operation , String library , double relativeMemory ) {
        dataset.addValue(relativeMemory, library, operation);
    }

    private void plot() {
        CategoryPlot plot = chart.getCategoryPlot();
//        plot.setDomainGridlinesVisible(true);
//        plot.setRangeCrosshairVisible(true);
//        plot.setRangeCrosshairPaint(Color.blue);

        plot.setRangeGridlinePaint(Color.BLACK);

        // set the range axis to display integers only...
//        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

//        CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis();
//        domainAxis.setTickMarkPaint(Color.BLACK);

        plot.setBackgroundPaint(new Color(230,230,230));

        // disable bar outlines...
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(true);
        renderer.setShadowVisible(false);
        renderer.setBaseOutlinePaint(Color.BLACK);
//        renderer.setBaseOutlineStroke(new BasicStroke(2));

//        CategoryAxis domainAxis = plot.getDomainAxis();
//        domainAxis.setCategoryLabelPositions(
//                CategoryLabelPositions.createUpRotationLabelPositions(
//                        Math.PI / 6.0));
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
       MemoryRelativeBarPlot plot = new MemoryRelativeBarPlot("Library Memory Usage");

        plot.addResult("add","ejml",0.4);
        plot.addResult("add","poop",0.1);
        plot.addResult("add","dude",0.8);
        plot.addResult("add","ert",1);

        plot.addResult("mult","ejml",0.4);
        plot.addResult("mult","poop",0.1);
        plot.addResult("mult","dude",0.8);
        plot.addResult("mult","ert",1);

        plot.displayWindow(400,300);
    }
}

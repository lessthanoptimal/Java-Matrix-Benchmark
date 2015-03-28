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
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: pja
 * Date: Dec 9, 2008
 * Time: 10:35:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class OperationsVersusSizePlot
{
    float STROKE_SIZE = 3;
    JFreeChart chart;
    // how many data sets have been added
    int numDataSets;
    XYPlot plot;

    java.util.List<String> dataNames = new ArrayList<String>();

    public OperationsVersusSizePlot( String title, String ylabel)
    {
        chart = ChartFactory.createXYLineChart(title,
                "Matrix Size",
                ylabel,
                null,
                PlotOrientation.VERTICAL,
                true , false , false  );

//        chart.removeLegend();
        plot = (XYPlot) chart.getPlot();

        plot.setBackgroundPaint(Color.WHITE);

//        final NumberAxis rangeAxis = new LogarithmicAxis(ylabel);
//        plot.setRangeAxis(rangeAxis);

        // one of the numbers is getting cropped.  this will make it fully visible
        chart.setPadding(new RectangleInsets(5,0,0,5));

    }

    public void setSubTitle( String title ) {
        chart.addSubtitle(new TextTitle(title,new Font("SansSerif", Font.ITALIC, 12)));
    }

    public void setRange( double min , double max ) {
        NumberAxis axis = (NumberAxis)plot.getRangeAxis();
        axis.setAutoRange(false);
        axis.setRange(min,max);
    }
    
    public void setLogScale(boolean range, boolean domain) {

        if( domain ) {
            NumberAxis axis = (NumberAxis)plot.getDomainAxis();
            axis = new LogarithmicAxis(axis.getLabel());
            plot.setDomainAxis(axis);
        }

        if( range ) {
            NumberAxis axis = (NumberAxis)plot.getRangeAxis();
            axis = new LogarithmicAxis(axis.getLabel());
            plot.setRangeAxis(axis);
        }
    }

    public void displayWindow(int width, int height) {

        ChartFrame window = new ChartFrame(chart.getTitle().getText(),chart);

        window.setMinimumSize(new Dimension(width,height));
        window.setPreferredSize(window.getMinimumSize());
        window.setVisible(true);
    }

    public void setAxisYTicks( double units , boolean isInteger ) {
        NumberAxis axis = (NumberAxis)plot.getRangeAxis();
        if( isInteger )
            axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        axis.setTickUnit( new NumberTickUnit(units) );
    }

    public void saveJpeg( String fileName , int width , int height ) {
        UtilPlotPdf.saveAsJpeg(chart,fileName,width,height,0.95);
    }

    public void savePDF( String fileName , int width , int height ) {
        UtilPlotPdf.saveAsPdf(chart,fileName,width,height);
    }

    public void addResults( int size[] , double opsPerSecond[], String name , int length , int seriesIndex ) {
        double conv_x[] = new double[ size.length ];

        for( int i = 0; i < size.length; i++ ) {
            conv_x[i] = size[i];
        }

        _addErrors(conv_x,opsPerSecond,length,name,seriesIndex);
    }

    private void _addErrors( double x[] , double y[], int max , String name , int seriesIndex ) {
        XYSeries series = createXY( x , y , max , name );

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        renderer.setBaseShapesVisible(false);
        renderer.setDrawSeriesLineAsPath(true);
        renderer.setSeriesPaint(0, getColor(seriesIndex));
        renderer.setSeriesStroke(0,getStroke(seriesIndex, STROKE_SIZE,STROKE_SIZE*2));

        plot.setRenderer(numDataSets, renderer);
        plot.setDataset(numDataSets,new XYSeriesCollection(series));

        numDataSets++;

        dataNames.add(name);
    }

    public static XYSeries createXY( double x[], double y[] , int max , String name )
    {
        if( x.length < max || y.length < max )
            throw new IllegalArgumentException("The must have at least max elements");

        XYSeries series = new XYSeries(name,false,false);
        for( int i = 0; i < max; i++ ) {
            series.add(x[i],y[i]);
        }
        return series;
    }

    public static Color getColor( int num ) {
        switch( num ) {
            case 0:
                return new Color(0f,0f,1f,0.6f);

            case 1:
                return new Color(0.5f,0.7f,0f,0.6f);

            case 2:
                return Color.BLACK;

            case 3:
                return Color.RED;

            case 4:
                return Color.CYAN;

            case 5:
                return Color.ORANGE;

            case 6:
                return Color.GREEN;

            case 7:
                return Color.lightGray;

            case 8:
                return Color.blue;

            case 9:
                return new Color(255, 0, 255,190); //magenta with translucent

            case 10:
                return new Color(230, 150, 0); // darker orange

            case 11:
                return new Color(255, 0, 255,190);

            case 12:
                return new Color(120,255,0);
            
            default:
                throw new RuntimeException("add some more");
        }
    }

    public static Stroke getStroke(int num, float size, float patternSize ) {
        switch( num ) {
            case 0:
                return SimpleStrokeFactory.createStroke("-",size,patternSize);

            case 1:
                return SimpleStrokeFactory.createStroke("-",size,patternSize);

            case 2:
                return SimpleStrokeFactory.createStroke("-.",size,patternSize);

            case 3:
                return SimpleStrokeFactory.createStroke(".",size,patternSize);

            case 4:
                return SimpleStrokeFactory.createStroke("-",size,patternSize);

            case 5:
                return SimpleStrokeFactory.createStroke("-",size,patternSize);

            case 6:
                return SimpleStrokeFactory.createStroke("-",size,patternSize);

            case 7:
                return SimpleStrokeFactory.createStroke(".",size,patternSize);

            case 8:
                return SimpleStrokeFactory.createStroke("-.",size,patternSize);

            case 9:
                return SimpleStrokeFactory.createStroke("-",size,patternSize);

            case 10:
                return SimpleStrokeFactory.createStroke(".",size,patternSize);

            case 11:
                return SimpleStrokeFactory.createStroke("-.",size,patternSize);

            case 12:
                return SimpleStrokeFactory.createStroke("--",size,patternSize);

            default:
                throw new RuntimeException("add some more: "+num);
        }
    }
}
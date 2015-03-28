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

import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.LegendTitle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Provides an easy mechanism for saving plots as PDF documents.
 */
public class UtilPlotPdf {

    public static void saveAsPdf(JFreeChart chart, String FILENAME , int width, int height) {
        File parent = new File(new File(FILENAME).getParent());
        if( !parent.exists() )  {
            if( !parent.mkdirs() )
                throw new RuntimeException("Can't make directory path");
        }

        Document document = new Document(new Rectangle(width, height));
        try {
            FileOutputStream file = new FileOutputStream(FILENAME);
            PdfWriter writer = PdfWriter.getInstance(document, file);
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(width, height);
            Graphics2D g2d = tp.createGraphics(width, height, new DefaultFontMapper());
            Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);
            chart.draw(g2d, r2d);
            g2d.dispose();
            cb.addTemplate(tp, 0, 0);
            document.close();
            g2d.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveAsPdf(LegendTitle legend, String FILENAME , int width, int height) {
        Document document = new Document(new Rectangle(width, height));
        try {
            FileOutputStream file = new FileOutputStream(FILENAME);
            PdfWriter writer = PdfWriter.getInstance(document, file);
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(width, height);
            Graphics2D g2d = tp.createGraphics(width, height, new DefaultFontMapper());
            Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);
            legend.draw(g2d, r2d);
            g2d.dispose();
            cb.addTemplate(tp, 0, 0);
            document.close();
            g2d.dispose();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveAsJpeg( JFreeChart chart, String fileName , int width, int height , double quality )
    {
        BufferedImage img = draw( chart, width, height );

        try {
            ImageIO.write(img,"jpg",new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    protected static BufferedImage draw(JFreeChart chart, int width, int height)
    {
        BufferedImage img =
                new BufferedImage(width , height,
                        BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();


        chart.draw(g2, new Rectangle2D.Double(0, 0, width, height));

        g2.dispose();
        return img;
    }
}
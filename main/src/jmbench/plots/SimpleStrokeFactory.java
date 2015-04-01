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

import java.awt.*;

/**
 * A class which simplifies creating strokes for drawing lines.  It is inspired by
 * the syntax used to set the line patterns in Matlab.
 */
public class SimpleStrokeFactory
{
    /**
     * The pattern that the stroke has is specified using the 'desc' parameter.  It supports
     * some of the strokes that matlab uses in its plot function.  The width of the line
     * is specified by the 'size' paramter.
     *
     * "-" will draw a solid line
     * "--" will draw a line with two long dashes
     * "." will draw a line with dots
     * "-." will draw a line with dashes and dots.  This can be increased to form any pattern of
     *      dashes and dots.
     *
     * @param desc The pattern which will be drawn on the line
     * @param size The width of the line in pixels.
     * @return  The stroke with the specified features.
     */
    public static Stroke createStroke( String desc , float size ) {
        // handle the line strokes
        if( isBasicLine(desc)) {
            return createBasicStroke(desc,size,size);
        } else {
            return createShapeStroke(desc,size);
        }
    }

    public static Stroke createStroke( String desc , float size , float patternSize ) {
        // handle the line strokes
        if( isBasicLine(desc)) {
            return createBasicStroke(desc,size,patternSize);
        } else {
            return createShapeStroke(desc,size);
        }
    }

    private static Stroke createShapeStroke(String desc, float size) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private static Stroke createBasicStroke(String desc, float size , float patternSize ) {
        if( desc.length() == 1 && desc.charAt(0) == '-') {
                return new BasicStroke(size*0.7f);
        }


        float[] dashPattern = new float[ desc.length()*2 ];

        for( int i = 0; i < desc.length(); i++ ) {
            if( desc.charAt(i) == '-')
                dashPattern[i*2] = patternSize*1.2f;
            else
                dashPattern[i*2] = patternSize*0.4f;

            dashPattern[i*2+1] = patternSize*0.6f;
        }

        return new BasicStroke(size, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1,
                dashPattern, 0);
    }


    /**
     * If the stroke can be described by a basic stroke then this returns true.
     */
    private static boolean isBasicLine( String desc ) {
        for( int i = 0; i < desc.length(); i++ ) {
            char c = desc.charAt(i);
            if( c != '-' && c != '.' )
                return false;
        }
        return true;
    }

}
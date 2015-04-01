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

import java.io.Serializable;


/**
 * @author Peter Abeles
 */
public class ExceptionInfo implements Serializable, Comparable<ExceptionInfo> {
    public String shortName;

    // number of times an exception with this short name was called
    public int numTimesThrown;

    // stack trace from one instance of this exception
    public String[] stackTrace;

    public ExceptionInfo(){}

    public ExceptionInfo( Exception e ) {
        shortName = e.getClass().getSimpleName();

        StackTraceElement[] ste = e.getStackTrace();

        stackTrace = new String[ ste.length ];

        for( int i = 0; i < ste.length; i++  ) {
            StackTraceElement t = ste[i];
            stackTrace[i] = t.toString();
        }

        numTimesThrown = 1;
    }


    @Override
    public int compareTo(ExceptionInfo o) {
        return shortName.compareTo(o.shortName);
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String[] getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String[] stackTrace) {
        this.stackTrace = stackTrace;
    }

    public int getNumTimesThrown() {
        return numTimesThrown;
    }

    public void setNumTimesThrown(int numTimesThrown) {
        this.numTimesThrown = numTimesThrown;
    }
}

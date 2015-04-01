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

package jmbench.tools.memory;

import jmbench.tools.EvaluatorSlave;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author Peter Abeles
 */
public class MemoryResults implements Serializable {
    String nameLibrary;
    String versionLibrary;
    String displayName;

    EvaluatorSlave.FailReason error;
    String errorMessage;

    List<Long> results = new ArrayList<Long>();
    int numFailed;

    public MemoryResults() {
        Collections.sort(results);
    }

    public void printStatistics() {
        if( results.size() == 0 ) {
            System.out.println("No data to print!");
            return;
        }
        Collections.sort(results);

        long min = results.get(0);
        long max = results.get(results.size()-1);
        System.out.printf(" min %6.2f max %6.2f  spread %5.2f\n",(min/1024/1024.0),(max/1024/1024.0),(100.0*(max-min)/min));
    }

    public long getScore( double frac ) {
        Collections.sort(results);

        int index = (int)(frac*results.size());
        if( index >= 1 )
            index = results.size()-1;

        return results.get( index );
    }

    public String getNameLibrary() {
        return nameLibrary;
    }

    public void setNameLibrary(String nameLibrary) {
        this.nameLibrary = nameLibrary;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getVersionLibrary() {
        return versionLibrary;
    }

    public void setVersionLibrary(String versionLibrary) {
        this.versionLibrary = versionLibrary;
    }

    public EvaluatorSlave.FailReason getError() {
        return error;
    }

    public void setError(EvaluatorSlave.FailReason error) {
        this.error = error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<Long> getResults() {
        return results;
    }

    public void setResults(List<Long> results) {
        this.results = results;
    }

    public int getNumFailed() {
        return numFailed;
    }

    public void setNumFailed(int numFailed) {
        this.numFailed = numFailed;
    }
}

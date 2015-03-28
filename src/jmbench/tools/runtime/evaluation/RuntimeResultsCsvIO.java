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

import jmbench.tools.OutputError;
import jmbench.tools.runtime.RuntimeEvaluationMetrics;
import jmbench.tools.runtime.RuntimeMeasurement;
import jmbench.tools.runtime.RuntimeResults;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts {@link RuntimeResults} to and from a very simple CSV format.  Replaces the old XML format that kept on
 * getting screwed up each time refactoring was done.
 *
 * @author Peter Abeles
 */
public class RuntimeResultsCsvIO {

    public static RuntimeResults read( File file ){

        try {
            FileInputStream stream = new FileInputStream(file);
            RuntimeResults ret = read( stream );
            stream.close();
            return ret;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static RuntimeResults read( InputStream input ){
        try{
            RuntimeResults ret = new RuntimeResults();
            ret.libraryName = readString(input);
            ret.opName = readString(input);
            ret.complete = readBoolean(input);
            ret.matDimen = new int[ readInt(input) ];
            ret.metrics = new RuntimeEvaluationMetrics[ret.matDimen.length];

            for( int i = 0; i < ret.matDimen.length; i++ ) {
                ret.matDimen[i] = readInt(input);


                int numResults = readInt(input);
                if( numResults > 0 ) {
                    RuntimeEvaluationMetrics e = ret.metrics[i] = new RuntimeEvaluationMetrics();
                    e.rawResults = new ArrayList<RuntimeMeasurement>();

                    for( int j = 0; j < numResults; j++ ) {
                        double opsPerSec = readDouble(input);
                        String faultName = readString(input);

                        OutputError error = faultName.compareTo("null") == 0 ? null : OutputError.valueOf(faultName);

                        e.rawResults.add(new RuntimeMeasurement(opsPerSec,0,error));
                    }

                    e.computeStatistics();
                }
            }

            return ret;
        } catch( IOException e ) {
            throw new RuntimeException(e);
        }
    }


    private static String readString( InputStream input ) throws IOException {
        char c = readEmptySapce(input);

        checkChar('\"',c);

        String ret = "";
        while( true ) {
            c = (char)input.read();
            if( c == '\"')
                break;

            ret += c;
        }

        return ret;
    }

    private static String readToken( InputStream input ) throws IOException {
        char c = readEmptySapce(input);

        String ret = c+"";
        while( true ) {
            c = (char)input.read();
            if( isEmpty(c) )
                break;

            ret += c;
        }

        return ret;
    }

    private static int readInt( InputStream input ) throws IOException {
        String s = readToken(input);

        return Integer.parseInt(s);
    }

    private static boolean readBoolean( InputStream input ) throws IOException {
        String s = readToken(input);

        return Boolean.parseBoolean(s);
    }

    private static double readDouble( InputStream input ) throws IOException {
        String s = readToken(input);

        return Double.parseDouble(s);
    }

    private static char readEmptySapce( InputStream input ) throws IOException {
        char c = (char)input.read();

        while( isEmpty(c) )
            c = (char)input.read();
        return c;
    }

    private static boolean isEmpty( char c ) {
        return c == ' ' || c == '\t' || c == '\n';
    }

    private static void checkChar( char expected , char found ) {
        if( expected != found )
            throw new RuntimeException("Unexpected character");
    }

    public static void write( RuntimeResults results , String fileName) {
        try {
            PrintStream output = new PrintStream(fileName);
            write(results,output);
            output.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write( RuntimeResults results , PrintStream output ) {

        writeHeader(results,output);
        int matDimen[] = results.matDimen;
        for( int i = 0; i < matDimen.length; i++ ) {
            output.print(matDimen[i]);
            output.print('\t');
            if( results.metrics[i] == null ) {
                output.print(-1);
                output.print('\n');
            } else {
                List<RuntimeMeasurement> raw = results.metrics[i].rawResults;
                output.print(raw.size());
                output.print('\n');
                for (RuntimeMeasurement m : raw) {
                    output.print(m.getOpsPerSec());
                    output.print('\t');
                    if (m.getError() == null)
                        out(output,"null");
                    else
                        out(output,m.getError().toString());
                    output.print('\n');
                }
            }
        }
    }

    protected static void writeHeader(RuntimeResults results , PrintStream output) {
        out(output,results.getLibraryName());
        output.print('\t');
        out(output,results.getOpName());
        output.print('\t');
        output.print(results.isComplete());
        output.print('\t');
        output.print(results.matDimen.length);
        output.print('\n');
    }

    protected static void out( PrintStream output , String text ) {
        output.print("\"");
        output.print(text);
        output.print("\"");
    }
}

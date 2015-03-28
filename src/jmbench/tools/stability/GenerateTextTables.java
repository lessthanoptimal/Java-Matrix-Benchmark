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

import java.util.List;
import java.util.Map;


/**
 * @author Peter Abeles
 */
public class GenerateTextTables extends TablesCommon {
    public GenerateTextTables(String dir) {
        super(dir);
    }

    @Override
    protected void printTables(Map<String, List> opMap) {
        System.out.println("== Linear Solve ==\n");
        printSolvingLinear(opMap);
        System.out.println("\n== Least Squares Solve ==");
        System.out.println();
        printSolvingLeastSquares(opMap);
        System.out.println("\n== Symmetric Semi-Positive Definite Inverse ==");
        System.out.println();
        printSymmInvert(opMap);
        System.out.println("\n== Singular Value Decomposition ==");
        System.out.println();
        printSvd(opMap);
        System.out.println("\n== Symmetric Eigenvalue Decomposition ==");
        System.out.println();
        printSymmEig(opMap);
    }

    private void printSolvingLinear( Map<String, List> opMap ) {
        List<String> names = getLibraryNames(opMap);

        List<StabilityTrialResults> lsOverflow = opMap.get("LinearOverflow");
        List<StabilityTrialResults> lsAccuracy = opMap.get("LinearAccuracy");
        List<StabilityTrialResults> lsSingular = opMap.get("LinearSingular");

        printTableHeader("Overflow","Accuracy","Nearly Singular");

        for( String n : names ) {
            Data over = findByName(lsOverflow,n);
            Data accuracy = findByName(lsAccuracy,n);
            Data singular = findByName(lsSingular,n);

            System.out.printf("%15s",n);
            printSolveHTML(over);
            printSolveHTML(accuracy);
            printSolveHTML(singular);

            System.out.print("\n");
        }
    }

    private void printSymmInvert( Map<String, List> opMap ) {
        printOverflowAccuracy(opMap,"InvSymmOverflow","InvSymmAccuracy");
    }

    private void printSvd( Map<String, List> opMap ) {
        printOverflowAccuracy(opMap,"SvdOverflow","SvdAccuracy");
    }

    private void printSymmEig( Map<String, List> opMap ) {
        printOverflowAccuracy(opMap,"EigSymmOverflow","EigSymmAccuracy");
    }

    private void printOverflowAccuracy( Map<String, List> opMap ,
                                        String nameOver , String nameAccuracy  ) {
        List<String> names = getLibraryNames(opMap);

        List<StabilityTrialResults> overflow = opMap.get(nameOver);
        List<StabilityTrialResults> accuracy = opMap.get(nameAccuracy);

        printTableHeader("Overflow","Accuracy");

        for( String n : names ) {


            Data over = findByName(overflow,n);
            Data singular = findByName(accuracy,n);

            System.out.printf("%15s",n);
            printSolveHTML(over);
            printSolveHTML(singular);

            System.out.print("\n");
        }
    }

    private void printTableHeader( String ...titles ) {
        System.out.printf("%15s","");

        for( String s : titles ) {
            System.out.printf("|                       %20s                                  |",s);
        }
        System.out.println("|");

        System.out.printf("%15s","");
        for( int i = 0; i < titles.length; i++ ) {
            System.out.print("| Fatal |    Median   | Uncountable |  Exception  | Large Error |  Detected   |");
        }
        System.out.print("\n");
    }

    private void printSolvingLeastSquares( Map<String, List> opMap ) {
        List<String> names = getLibraryNames(opMap);

        List<StabilityTrialResults> llsOverflow = opMap.get("LeastSquaresOverflow");
        List<StabilityTrialResults> llsAccuracy = opMap.get("LeastSquaresAccuracy");
        List<StabilityTrialResults> llsSingular = opMap.get("LeastSquaresSingular");

        printTableHeader("Overflow","Accuracy","Nearly Singular");


        for( String n : names ) {

            Data over = findByName(llsOverflow,n);
            Data accuracy = findByName(llsAccuracy,n);
            Data singular = findByName(llsSingular,n);

            System.out.printf("%15s",n);
            printSolveHTML(over);
            printSolveHTML(accuracy);
            printSolveHTML(singular);

            System.out.print("\n");
        }
    }

    private void printSolveHTML( Data d ) {
        if( d == null ) {
            System.out.printf("|%77s|","");
        } else {
            printFatalError(d);

            printHtmlElement(d.per50," %8.1e  ");
            printHtmlElement(d.fracUncount);
            printHtmlElement(d.fracUnexpected);
            printHtmlElement(d.fracLargeError);
            printHtmlElement(d.fracDetected);
            System.out.printf("|");

//            System.out.printf("<TD>%8.1e</TD> <TD> %6.3f </TD><TD> %6.3f </TD><TD> %6.3f </TD><TD> %6.3f</TD>",d.median,d.fracUncount,d.fracUnexpected,d.fracLargeError,d.fracDetected);
        }
    }

    private void printFatalError(Data d) {
        if( d.fatalError == null ) {
            System.out.print("|       ");
        } else {
            switch( d.fatalError ) {
                case MISC:
                case RETURNED_NULL:
                    System.out.print("|   ?   ");
                    break;

                case FROZE:
                    System.out.print("| FROZE ");
                    break;

                case OUT_OF_MEMORY:
                    System.out.print("|  MEM  ");
                    break;
                
                default:
                    throw new RuntimeException("Unknown error.  Add to list: "+d.fatalError);
            }
        }
    }

    private void printHtmlElement( double val ) {
        printHtmlElement(val*100,"  %5.1f%%   ");
    }

    private void printHtmlElement( double val , String precision ) {
        if( val == 0 )
            System.out.print("|             ");
        else
            System.out.printf("| "+precision+" ",val);
    }

    public static void main( String args[] ) {
        GenerateTextTables p = new GenerateTextTables("results/stability_2013_10/small");

        p.plot();
    }
}

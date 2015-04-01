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
 * Creates plots for all the results in a directory
 *
 * @author Peter Abeles
 */
@SuppressWarnings({"unchecked"})
public class GenerateHtmlTables extends TablesCommon {
    public GenerateHtmlTables(String dir) {
        super(dir);
    }

    @Override
    protected void printTables(Map<String, List> opMap) {
        System.out.println("== Linear Solve ==\n");
        printSolvingLinear(opMap);
        System.out.println("\n== Least Squares Solve ==");
        System.out.println();
        printSolvingLeastSquares(opMap);
        System.out.println("\n== Symmetric Positive Definite Inverse ==");
        System.out.println();
        printSymmInverse(opMap);
        System.out.println("\n== Singular Value Decomposition ==");
        System.out.println();
        printSvd(opMap);
        System.out.println("\n== Symmetric Eigenvalue Decomposition ==");
        System.out.println();
        printSymmEig(opMap);
    }

    private void printSolvingLinear( Map<String, List> opMap ) {
        printSolving(opMap,"LinearOverflow","LinearUnderflow","LinearAccuracy","LinearSingular");
    }

    private void printSolving( Map<String, List> opMap ,
                               String nameOverflow , String nameUnderflow ,
                               String nameAccuracy , String nameSingular ) {
        List<String> names = getLibraryNames(opMap);

        List<StabilityTrialResults> lsOverflow = opMap.get(nameOverflow);
        List<StabilityTrialResults> lsUnderflow = opMap.get(nameUnderflow);
        List<StabilityTrialResults> lsAccuracy = opMap.get(nameAccuracy);
        List<StabilityTrialResults> lsSingular = opMap.get(nameSingular);

        printTableHeader(new Info("Overflow",false),new Info("Underflow",false));

        for( String n : names ) {
            Data over = findByName(lsOverflow,n);
            Data under = findByName(lsUnderflow,n);

            System.out.printf("<TR><TH>%s</TH>",n);
            printSolveHTML(over);
            printSolveHTML(under);

            System.out.println("</TR>");
        }
        System.out.println("</TABLE>");

        printTableHeader(new Info("Accuracy",true),new Info("Nearly Singular",false));
        for( String n : names ) {
            Data accuracy = findByName(lsAccuracy,n);
            Data singular = findByName(lsSingular,n);

            System.out.printf("<TR><TH>%s</TH>",n);
            printSolveHTML(accuracy);
            printSolveHTML(singular);

            System.out.println("</TR>");
        }
        System.out.println("</TABLE>");
    }

    private void printSymmInverse( Map<String, List> opMap ) {
        printDecomposition(opMap,"InvSymmOverflow","InvSymmUnderflow","InvSymmAccuracy");
    }

    private void printSvd( Map<String, List> opMap ) {
        printDecomposition(opMap,"SvdOverflow","SvdUnderflow","SvdAccuracy");
    }

    private void printDecomposition( Map<String, List> opMap ,
                                     String nameOverflow , String nameUnderflow ,
                                     String nameAccuracy )
    {
        List<String> names = getLibraryNames(opMap);

        List<StabilityTrialResults> overflow = opMap.get(nameOverflow);
        List<StabilityTrialResults> underflow = opMap.get(nameUnderflow);
        List<StabilityTrialResults> accuracy = opMap.get(nameAccuracy);

        printTableHeader(new Info("Accuracy",true));
        for( String n : names ) {
            Data acc = findByName(accuracy,n);

            System.out.printf("<TR><TH>%s</TH>",n);
            printSolveHTML(acc);

            System.out.println("</TR>");
        }
        System.out.println("</TABLE>");

        printTableHeader(new Info("Overflow",false),new Info("Underflow",false));
        for( String n : names ) {
            Data over = findByName(overflow,n);
            Data under = findByName(underflow,n);

            System.out.printf("<TR><TH>%s</TH>",n);
            printSolveHTML(over);
            printSolveHTML(under);

            System.out.println("</TR>");
        }
        System.out.println("</TABLE>");
    }

    private void printSymmEig( Map<String, List> opMap ) {
        printDecomposition(opMap,"EigSymmOverflow","EigSymmUnderflow","EigSymmAccuracy");
    }

    private void printTableHeader( Info ...titles ) {



        System.out.print("<TABLE border=\"1\" cellpadding=\"5\">\n");
        System.out.printf("<TR><TH></TH>");

        for( Info s : titles ) {
            System.out.printf("<TH colspan=\"8\"> %s </TH>",s.name);
        }
        System.out.println("</TR>\n");

        System.out.print("<TR><TH/>");

        for (Info info : titles) {
            String percentNames = info.accuracy ? "Unexpected Error" : "Stopping Condition";
            String metricName = info.accuracy ? "Accuracy" : "Scaling Factor";
            System.out.printf("<TH colspan=\"3\"> %s </TH><TH colspan=\"5\"> %s </TH>", metricName, percentNames);
        }
        System.out.print("</TR>\n");

        System.out.print("<TR><TH/>");

        for (Info title : titles) {
            System.out.print("<TH>Metric 10%</TH><TH>Metric 50%</TH><TH>Metric 90%</TH><TH>No Error</TH><TH>Uncountable</TH><TH>Exception</TH><TH>Large Error</TH><TH>Detected</TH>");
        }
        System.out.print("</TR>\n");
    }

    private void printSolvingLeastSquares( Map<String, List> opMap ) {
        printSolving(opMap,"LeastSquaresOverflow","LeastSquaresUnderflow",
                "LeastSquaresAccuracy","LeastSquaresSingular");

    }

    private void printSolveHTML( Data d ) {
        if( d == null ) {
            System.out.print("<TD colspan=\"8\"> Not Supported </TD>");
        } else {
            if( d.fatalError != null ) {
                printFatalError(d);
            } else {
                double noError = 1.0 - d.fracUncount - d.fracUnexpected - d.fracLargeError - d.fracDetected;
                printHtmlElement(d.per10,"%8.1e");
                printHtmlElement(d.per50,"%8.1e");
                printHtmlElement(d.per90,"%8.1e");
                printHtmlElement(noError);
                printHtmlElement(d.fracUncount);
                printHtmlElement(d.fracUnexpected);
                printHtmlElement(d.fracLargeError);
                printHtmlElement(d.fracDetected);
            }

//            System.out.printf("<TD>%8.1e</TD> <TD> %6.3f </TD><TD> %6.3f </TD><TD> %6.3f </TD><TD> %6.3f</TD>",d.median,d.fracUncount,d.fracUnexpected,d.fracLargeError,d.fracDetected);
        }
    }

    private void printFatalError(Data d) {
        String reason;

        switch( d.fatalError ) {
            case UNSUPPORTED:
                reason = "UNSUPPORTED";
                break;

            case MISC:
                reason = "MISC";
                break;

            case RETURNED_NULL:
                reason = "RETURNED NULL";
                break;

            case FROZE:
                reason = "TIMED OUT";
                break;

            case OUT_OF_MEMORY:
                reason = "OUT OF MEMORY";
                break;

            default:
                throw new RuntimeException("Unknown error.  Add to list. "+d.fatalError);
        }

        System.out.printf("<TH colspan=\"8\"> %s </TH>",reason);
    }

    private void printHtmlElement( double val ) {
        printHtmlElement(val*100,"% 5.1f%%");
    }

    private void printHtmlElement( double val , String precision ) {
        if( val == 0 )
            System.out.print("<TD></TD>");
        else
            System.out.printf("<TD>"+precision+"</TD>",val);
    }

    public static class Info
    {
        String name;
        boolean accuracy;

        public Info(String name, boolean accuracy) {
            this.name = name;
            this.accuracy = accuracy;
        }
    }

    public static void main( String args[] ) {

        String base = "results/stability_2013_10";

        System.out.println("= Small Matrices =");
        System.out.println();
        GenerateHtmlTables p = new GenerateHtmlTables(base+"/small");
        p.plot();
//        System.out.println();
//        System.out.println("= Medium Matrices =");
//        System.out.println();
//        p = new GenerateHtmlTables(base+"/medium");
//        p.plot();
        System.out.println();
        System.out.println("= Large Matrices =");
        System.out.println();
        p = new GenerateHtmlTables(base+"/large");
        p.plot();

    }
}
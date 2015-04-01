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

package jmbench.tools;

import jmbench.tools.memory.MemoryBenchmark;
import jmbench.tools.memory.PlotMemoryResultsXml;
import jmbench.tools.runtime.RuntimeBenchmarkMaster;
import jmbench.tools.runtime.evaluation.CheckForErrorsInResults;
import jmbench.tools.runtime.evaluation.ComparePlatformResults;
import jmbench.tools.runtime.evaluation.PlotRuntimeResults;
import jmbench.tools.stability.StabilityBenchmark;

import java.io.IOException;


/**
 * An application for running all the benchmark related tools
 *
 * @author Peter Abeles
 */
public class BenchmarkToolsMasterApp {

    public static double SMALL_PERTURBATION = 1e-8;

    public static void printHelp() {
        System.out.println("A tool must be specified:");
        System.out.println();
        System.out.println("  java -jar benchmark_app.jar <tool>");
        System.out.println();
        System.out.println("Where tool is one of the following keywords: ");
        System.out.println("  stability          Runs the stability benchmark.");
        System.out.println("  runtime            Runs the runtime benchmark.");
        System.out.println("  memory             Runs the memory benchmark.");
        System.out.println("  checkRuntime       Outputs the runtime sanity check results.");
        System.out.println("  plotRuntime        Generates plots from runtime results.");
        System.out.println("  plotMemory         Generates a plot from memory benchmark results.");
        System.out.println("  compareRuntime   Compares runtime performance for a single library across different platforms.");
        System.out.println("  displayStability   Prints out tables showing stability results.");
        System.out.println();
        System.out.println("For example to run the runtime benchmark type:");
        System.out.println("  java -jar benchmark_app.jar runtime");
        System.out.println();
        System.out.println("Additional help on commandline arguments for each tool can be obtained by typing:");
        System.out.println("  java -jar <tool> help");
        System.out.println();
        System.exit(0);
    }

    public static void main( String args[] ) throws IOException, InterruptedException {

        if( args.length == 0 ) {
            printHelp();
        }

        String tool = args[0];

        String[] pruned = new String[args.length-1];
        System.arraycopy(args,1,pruned,0,pruned.length);

        if( tool.compareToIgnoreCase("runtime") == 0) {
            RuntimeBenchmarkMaster.main(pruned);
        } else if( tool.compareToIgnoreCase("stability") == 0) {
            StabilityBenchmark.main(pruned);
        } else if( tool.compareToIgnoreCase("memory") == 0 ) {
            MemoryBenchmark.main(pruned);
        } else if( tool.compareToIgnoreCase("checkRuntime") == 0) {
            CheckForErrorsInResults.main(pruned);
        } else if( tool.compareToIgnoreCase("plotRuntime") == 0) {
            PlotRuntimeResults.main(pruned);
        } else if( tool.compareToIgnoreCase("plotMemory") == 0 ) {
            PlotMemoryResultsXml.main(pruned);
        } else if( tool.compareToIgnoreCase("compareRuntime") == 0 ) {
            ComparePlatformResults.main(pruned);
        } else if( tool.compareToIgnoreCase("displayStability") == 0) {
            System.out.println("Not supported yet "+tool);
        } else {
            System.out.println("Unknown tool '"+args[0]+"'");
        }
    }

}

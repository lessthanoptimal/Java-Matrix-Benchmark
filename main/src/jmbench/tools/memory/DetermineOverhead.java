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

/**
 * Computes the memory overhead in just launching a process.
 *
 * @author Peter Abeles
 */
public class DetermineOverhead {

    MemoryConfig config;
    MemoryBenchmarkTools tool = new MemoryBenchmarkTools();

    int numTrials;

    public DetermineOverhead(MemoryConfig config , int numTrials ) {
        this.numTrials = numTrials;
        this.config = config;
        tool.setVerbose(false);
        tool.sampleType = config.memorySampleType;
    }

    public long computeOverhead() {
        long total = 0;

        for( int i = 0; i < numTrials; i++ ) {
            System.out.print("*");
            long time = performTest();

            if( time <= 0 )
                throw new RuntimeException("Overhead test failed!");

            total += time;
        }

        System.out.print("  ");
        return total / numTrials;
    }

    private long performTest() {
        tool.setFrozenDefaultTime(60*1000);
        tool.setMemory(config.memoryMinMB,config.memoryMaxMB);

        MemoryTest test = new MemoryTest();
        test.setup(null,null,null,null,1,0);
        test.setRandomSeed(config.seed);

        return tool.runTest(test);
    }
}

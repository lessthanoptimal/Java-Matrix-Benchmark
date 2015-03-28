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

import java.io.Serializable;


/**
 * @author Peter Abeles
 */
public abstract class EvaluationTest implements Serializable {
    // a random seed that the random matrix generators should be set to on init
    protected long randomSeed;

    /**
     * Creates a new evaluation test.
     *
     * @param randomSeed The random seed used for the tests.
     */
    public EvaluationTest( long randomSeed )
    {
        this.randomSeed = randomSeed;
    }

    public EvaluationTest(){}

    public long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    /**
     * Called only once after a new instance has been created.
     */
    public abstract void init();

    /**
     * Called before a new trial is started.  This would be a good point to create new random inputs.
     */
    public abstract void setupTrial();

    /**
     * Prints information about what it is doing.
     */
    public abstract void printInfo();

    /**
     * Returns how long this test is allowed to run for.  If there is no maximum runtime then
     * -1 is returned.
     */
    public abstract long getMaximumRuntime();

    /**
     * Returns how much memory it is expected to use up in bytes.
     */
    public abstract long getInputMemorySize();

    /**
     * Performs the evaluation process.
     *
     * @return the results of the test.
     */
    public abstract TestResults evaluate();
}

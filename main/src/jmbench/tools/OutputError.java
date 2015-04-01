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


/**
 * Indicates if an unexpected result was generated from an operation.
 *
 * @author Peter Abeles
 */
public enum OutputError {
    /**
     * The operation isn't supported by the library
     */
    NOT_SUPPORTED,
    /**
     * Its solution was outside of tolerance
     */
    LARGE_ERROR,
    /**
     * The answer it produced has uncountable numbers
     */
    UNCOUNTABLE,
    /**
     * Detected that it was failing and gave up
     */
    DETECTED_FAILURE,
    /**
     * It threw some runtime exception
     */
    UNEXPECTED_EXCEPTION,
    /**
     * A zero matrix is the input making it impossible to compute the error
     */
    ZERO_INPUT,
    /**
     * The test finished without error
     */
    NO_ERROR,
    /**
     * Something bad happened
     */
    MISC
}

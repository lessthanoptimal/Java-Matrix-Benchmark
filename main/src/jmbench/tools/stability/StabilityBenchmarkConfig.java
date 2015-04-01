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

import jmbench.impl.FactoryLibraryDescriptions;
import jmbench.impl.LibraryDescription;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Peter Abeles
 */
public class StabilityBenchmarkConfig implements Serializable {

    public long randomSeed;
    public long maxProcessingTime;

    // how much memory it will add to the operation requested memory
    public long baseMemory;
    // how much operation requested memory will be scaled
    public long scaleMemory;

    // at which point is the error considered too large
    public double breakingPoint;

    // the number of trials for determining the breaking point
    // is this factor less than the base number of trials
    public int overFlowFactor;

    // size of matrices in each size group
    public int smallSizeMin;
    public int smallSizeMax;

    public int mediumSizeMin;
    public int mediumSizeMax;

    public int largeSizeMin;
    public int largeSizeMax;

    // number of trials it will perform for solve benchmarks
    public int trialsSmallSolve;
    public int trialsMediumSolve;
    public int trialsLargeSolve;

    // number of trials for SVD benchmarks
    public int trialsSmallSvd;
    public int trialsMediumSvd;
    public int trialsLargeSvd;

    // which tests it should run
    public boolean checkAccuracy;
    public boolean checkOverflow;
    public boolean checkUnderflow;
    public boolean checkNearlySingular;
    public boolean checkLinear;
    public boolean checkLS;
    public boolean checkSVD;
    public boolean checkEVD;
    public boolean checkSymInv;

    // which libraries are to be evaluated
    public List<LibraryDescription> targets = new ArrayList<LibraryDescription>();

    public static StabilityBenchmarkConfig createDefault() {
        StabilityBenchmarkConfig config = new StabilityBenchmarkConfig();

        config.randomSeed = 0xdeadbeef;//new Random().nextLong();
        config.maxProcessingTime = 15*60*1000;

        config.baseMemory = 200;
        config.scaleMemory = 2;

        // some what arbitrary.  selected so that it will work on small and large
        // matrices with a generous cushion.  really should be a function of matrix size.
        config.breakingPoint = 1e-9;

        int off = 4;
        config.overFlowFactor = off;

        config.smallSizeMin = 2;
        config.smallSizeMax = 10;

        config.mediumSizeMin = 100;
        config.mediumSizeMax = 200;

        config.largeSizeMin = 500;
        config.largeSizeMax = 600;

        config.trialsSmallSolve = 1000*off;
        config.trialsMediumSolve = 50*off;
        config.trialsLargeSolve = 12*off;

        config.trialsSmallSvd = 600*off;
        config.trialsMediumSvd = 18*off;
        config.trialsLargeSvd = 7*off;

        config.targets = FactoryLibraryDescriptions.createDefault();

        config.checkAccuracy = true;
        config.checkOverflow = true;
        config.checkUnderflow = true;
        config.checkNearlySingular = true;
        config.checkLinear = true;
        config.checkLS = true;
        config.checkSVD = true;
        config.checkEVD = true;
        config.checkSymInv = true;

        return config;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public long getMaxProcessingTime() {
        return maxProcessingTime;
    }

    public void setMaxProcessingTime(long maxProcessingTime) {
        this.maxProcessingTime = maxProcessingTime;
    }

    public int getSmallSizeMin() {
        return smallSizeMin;
    }

    public void setSmallSizeMin(int smallSizeMin) {
        this.smallSizeMin = smallSizeMin;
    }

    public int getSmallSizeMax() {
        return smallSizeMax;
    }

    public void setSmallSizeMax(int smallSizeMax) {
        this.smallSizeMax = smallSizeMax;
    }

    public int getMediumSizeMin() {
        return mediumSizeMin;
    }

    public void setMediumSizeMin(int mediumSizeMin) {
        this.mediumSizeMin = mediumSizeMin;
    }

    public int getMediumSizeMax() {
        return mediumSizeMax;
    }

    public void setMediumSizeMax(int mediumSizeMax) {
        this.mediumSizeMax = mediumSizeMax;
    }

    public int getLargeSizeMin() {
        return largeSizeMin;
    }

    public void setLargeSizeMin(int largeSizeMin) {
        this.largeSizeMin = largeSizeMin;
    }

    public int getLargeSizeMax() {
        return largeSizeMax;
    }

    public void setLargeSizeMax(int largeSizeMax) {
        this.largeSizeMax = largeSizeMax;
    }

    public int getTrialsSmallSolve() {
        return trialsSmallSolve;
    }

    public void setTrialsSmallSolve(int trialsSmallSolve) {
        this.trialsSmallSolve = trialsSmallSolve;
    }

    public int getTrialsMediumSolve() {
        return trialsMediumSolve;
    }

    public void setTrialsMediumSolve(int trialsMediumSolve) {
        this.trialsMediumSolve = trialsMediumSolve;
    }

    public int getTrialsLargeSolve() {
        return trialsLargeSolve;
    }

    public void setTrialsLargeSolve(int trialsLargeSolve) {
        this.trialsLargeSolve = trialsLargeSolve;
    }

    public List<LibraryDescription> getTargets() {
        return targets;
    }

    public void setTargets(List<LibraryDescription> targets) {
        this.targets = targets;
    }

    public double getBreakingPoint() {
        return breakingPoint;
    }

    public void setBreakingPoint(double breakingPoint) {
        this.breakingPoint = breakingPoint;
    }

    public int getTrialsSmallSvd() {
        return trialsSmallSvd;
    }

    public void setTrialsSmallSvd(int trialsSmallSvd) {
        this.trialsSmallSvd = trialsSmallSvd;
    }

    public int getTrialsMediumSvd() {
        return trialsMediumSvd;
    }

    public void setTrialsMediumSvd(int trialsMediumSvd) {
        this.trialsMediumSvd = trialsMediumSvd;
    }

    public int getTrialsLargeSvd() {
        return trialsLargeSvd;
    }

    public void setTrialsLargeSvd(int trialsLargeSvd) {
        this.trialsLargeSvd = trialsLargeSvd;
    }

    public int getOverFlowFactor() {
        return overFlowFactor;
    }

    public void setOverFlowFactor(int overFlowFactor) {
        this.overFlowFactor = overFlowFactor;
    }

    public long getBaseMemory() {
        return baseMemory;
    }

    public void setBaseMemory(long baseMemory) {
        this.baseMemory = baseMemory;
    }

    public long getScaleMemory() {
        return scaleMemory;
    }

    public void setScaleMemory(long scaleMemory) {
        this.scaleMemory = scaleMemory;
    }

    public boolean isCheckAccuracy() {
        return checkAccuracy;
    }

    public void setCheckAccuracy(boolean checkAccuracy) {
        this.checkAccuracy = checkAccuracy;
    }

    public boolean isCheckOverflow() {
        return checkOverflow;
    }

    public void setCheckOverflow(boolean checkOverflow) {
        this.checkOverflow = checkOverflow;
    }

    public boolean isCheckUnderflow() {
        return checkUnderflow;
    }

    public void setCheckUnderflow(boolean checkUnderflow) {
        this.checkUnderflow = checkUnderflow;
    }

    public boolean isCheckNearlySingular() {
        return checkNearlySingular;
    }

    public void setCheckNearlySingular(boolean checkNearlySingular) {
        this.checkNearlySingular = checkNearlySingular;
    }

    public boolean isCheckLinear() {
        return checkLinear;
    }

    public void setCheckLinear(boolean checkLinear) {
        this.checkLinear = checkLinear;
    }

    public boolean isCheckLS() {
        return checkLS;
    }

    public void setCheckLS(boolean checkLS) {
        this.checkLS = checkLS;
    }

    public boolean isCheckSVD() {
        return checkSVD;
    }

    public void setCheckSVD(boolean checkSVD) {
        this.checkSVD = checkSVD;
    }

    public boolean isCheckEVD() {
        return checkEVD;
    }

    public void setCheckEVD(boolean checkEVD) {
        this.checkEVD = checkEVD;
    }

    public boolean isCheckSymInv() {
        return checkSymInv;
    }

    public void setCheckSymInv(boolean checkSymInv) {
        this.checkSymInv = checkSymInv;
    }
}

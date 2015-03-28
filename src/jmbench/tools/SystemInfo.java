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
 * Contains information on the system that was running
 *
 * @author Peter Abeles
 */
public class SystemInfo implements Serializable {

    // The start time according to System.currentTimeMillis()
    private long time;

    // various stuff collected about the system its running on
    private String javaVersion;
    private String javaVendor;
    private String javaHome;
    private String javaVmName;
    private String javaVmVersion;
    private String osArch;
    private String osName;
    private String osVersion;

    // how many cores/CPUs in the system
    private int numCPU;
    // what ever totalMemory says there is
    private long memory;

    public SystemInfo() {
    }

    public void grabCurrentInfo() {
        time = System.currentTimeMillis();

        javaVersion = System.getProperty("java.version");
        javaVendor = System.getProperty("java.vendor");
        javaVmName = System.getProperty("java.vm.name");
        javaVmVersion = System.getProperty("java.vm.version");
        osArch = System.getProperty("os.arch");
        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
        javaHome = System.getProperty("java.home");
        numCPU = Runtime.getRuntime().availableProcessors();
        memory = Runtime.getRuntime().totalMemory();

    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getJavaVendor() {
        return javaVendor;
    }

    public void setJavaVendor(String javaVendor) {
        this.javaVendor = javaVendor;
    }

    public String getOsArch() {
        return osArch;
    }

    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public int getNumCPU() {
        return numCPU;
    }

    public void setNumCPU(int numCPU) {
        this.numCPU = numCPU;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(long memory) {
        this.memory = memory;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getJavaVmName() {
        return javaVmName;
    }

    public void setJavaVmName(String javaVmName) {
        this.javaVmName = javaVmName;
    }

    public String getJavaVmVersion() {
        return javaVmVersion;
    }

    public void setJavaVmVersion(String javaVmVersion) {
        this.javaVmVersion = javaVmVersion;
    }
}

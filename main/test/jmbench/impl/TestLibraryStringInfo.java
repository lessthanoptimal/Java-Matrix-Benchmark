package jmbench.impl;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestLibraryStringInfo {
    @Test
    public void outputDirectory() {
        LibraryStringInfo info = new LibraryStringInfo();
        info.nameShort = "Foo:Bar-Now";
        String found = info.outputDirectory();
        assertTrue("Foo_Bar_Now".equals(found));
    }
}
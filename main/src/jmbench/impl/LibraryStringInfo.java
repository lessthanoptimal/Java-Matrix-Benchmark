package jmbench.impl;

import jmbench.interfaces.RuntimePerformanceFactory;

import java.io.Serializable;

/**
 * Describes which classes are to be used for a benchmarked library as well as how it will be refered to.
 *
 * @author Peter Abeles
 */
public class LibraryStringInfo implements Serializable{
    /**
     * Class path to the algorithm factory
     */
    public String factory;
    /**
     * Class path to the configuration class
     */
    public String configure;
    public String nameFull;
    public String nameShort;
    public String namePlot;

    public Class<LibraryConfigure> getLibraryConfigure() {
        try {
            return  (Class)Class.forName(configure);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Class<RuntimePerformanceFactory> getFactoryConfigure() {
        try {
            return  (Class)Class.forName(factory);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getConfigure() {
        return configure;
    }

    public void setConfigure(String configure) {
        this.configure = configure;
    }

    public String getNameFull() {
        return nameFull;
    }

    public void setNameFull(String nameFull) {
        this.nameFull = nameFull;
    }

    public String getNameShort() {
        return nameShort;
    }

    public void setNameShort(String nameShort) {
        this.nameShort = nameShort;
    }

    public String getNamePlot() {
        return namePlot;
    }

    public void setNamePlot(String namePlot) {
        this.namePlot = namePlot;
    }
}

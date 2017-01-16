package jmatbench.ujmp;

import org.ujmp.core.util.UJMPSettings;

/**
 * @author Peter Abeles
 */
public class UjmpAlgorithmFactoryNative extends UjmpAlgorithmFactory {
    @Override
    public void init() {
        UJMPSettings.getInstance().setUseJBlas(true);
    }
}

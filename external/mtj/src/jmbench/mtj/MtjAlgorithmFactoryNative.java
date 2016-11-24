package jmbench.mtj;

import com.github.fommil.netlib.ARPACK;
import com.github.fommil.netlib.BLAS;
import com.github.fommil.netlib.LAPACK;

/**
 * @author Peter Abeles
 */
public class MtjAlgorithmFactoryNative extends MtjAlgorithmFactory {
    @Override
    public void init() {
        BLAS.getInstance();
        LAPACK.getInstance();
        ARPACK.getInstance();
    }

    @Override
    public boolean isNative() {
        return true;
    }
}

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
        System.out.println("BLAS = "+BLAS.getInstance().getClass().getName());
        System.out.println("LAPACK = "+LAPACK.getInstance().getClass().getName());
        System.out.println("ARPACK = "+ARPACK.getInstance().getClass().getName());
    }

    @Override
    public boolean isNative() {
        return true;
    }
}

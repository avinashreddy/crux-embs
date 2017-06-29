package com.matrix.common;

import rapture.kernel.DecisionApiImplWrapper;
import rapture.kernel.Kernel;
import rapture.kernel.StructuredApiImplWrapper;

/**
 * A wrapper around the Rapture Kernel({@link Kernel}). Using this class makes mocking for unit testing easier and nothing more.
 * So, if you do not intend to mock the Kernel methods and instead choose to bootstrap the Kernel in tests, then no need to use this
 * class.
 *
 * This class should do nothing but delegate to Kernel. All methods in this class must map one-on-one to methods in Kernel
 * and each method should have only a single line of cde that invokes the corresponding Kernel method.
 *
 * If you need a Kernel method that is not in this class, feel free to add it.
 *
 * @see Kernel
 *
 */
public class KernelServices {

    public static DecisionApiImplWrapper getDecision() {
        return Kernel.getDecision();
    }

    public static StructuredApiImplWrapper getStructured() {
        return Kernel.getStructured();
    }

}

package jsci.maths.vector.impl;

import jsci.maths.vector.Algorithms;
import jsci.maths.vector.AlgorithmsFactory;
import jsci.util.array.DoubleArray1D;

/**
 *
 * @author Mark
 */
public final class DoubleAlgorithmsFactory<E extends DoubleArray1D> implements AlgorithmsFactory<Double,E> {
    private final DoubleAlgorithms<E> algorithms;

    public DoubleAlgorithmsFactory() {
        algorithms = new DoubleAlgorithms<E>();
    }
    public Algorithms<Double,E> createAlgorithms(E array) {
        return algorithms;
    }
}

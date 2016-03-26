package jsci.maths.vector.impl;

import jsci.maths.vector.*;
import jsci.maths.structure.Ring;
import jsci.util.array.Array1D;

/**
 *
 * @author Mark
 */
public class RingAlgorithmsFactory<T,E extends Array1D<T>> implements AlgorithmsFactory<T,E> {
    private final RingAlgorithms<T,E> algorithms;

    public RingAlgorithmsFactory(Ring<T> r) {
        this.algorithms = new RingAlgorithms<T,E>(r);
    }

    public Algorithms<T,E> createAlgorithms(E array) {
        return algorithms;
    }
}

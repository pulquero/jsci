package jsci.maths.matrix.impl;

import jsci.maths.matrix.*;
import jsci.maths.structure.Ring;
import jsci.util.array.Array1D;
import jsci.util.array.Array2D;

/**
 *
 * @author Mark
 */
public class RingAlgorithmsFactory<T,E extends Array2D<T>,F extends Array1D<T>> implements AlgorithmsFactory<T,E,F> {
    private final RingAlgorithms<T,E,F> algorithms;
    private final jsci.maths.vector.impl.RingAlgorithmsFactory<T,F> vectorAlgorithmsFactory;

    public RingAlgorithmsFactory(Ring<T> r) {
        this.algorithms = new RingAlgorithms<T,E,F>(r);
        this.vectorAlgorithmsFactory = new jsci.maths.vector.impl.RingAlgorithmsFactory<T, F>(r);
    }

    public Algorithms<T,E,F> createAlgorithms(E array) {
        return algorithms;
    }

    public jsci.maths.vector.AlgorithmsFactory<T,F> getVectorAlgorithmsFactory() {
        return vectorAlgorithmsFactory;
    }
}

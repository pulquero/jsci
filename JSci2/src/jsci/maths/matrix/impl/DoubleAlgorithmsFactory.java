package jsci.maths.matrix.impl;

import jsci.maths.matrix.Algorithms;
import jsci.maths.matrix.AlgorithmsFactory;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.DoubleArray2D;

/**
 *
 * @author Mark
 */
public final class DoubleAlgorithmsFactory<E extends DoubleArray2D,F extends DoubleArray1D> implements AlgorithmsFactory<Double,E,F> {
    private final DoubleAlgorithms<E,F> algorithms;
    private final jsci.maths.vector.impl.DoubleAlgorithmsFactory<F> vectorAlgorithmsFactory;

    public DoubleAlgorithmsFactory() {
        this.algorithms = new DoubleAlgorithms<E,F>();
        this.vectorAlgorithmsFactory = new jsci.maths.vector.impl.DoubleAlgorithmsFactory<F>();
    }

    public Algorithms<Double,E,F> createAlgorithms(E array) {
        return algorithms;
    }

    public jsci.maths.vector.AlgorithmsFactory<Double,F> getVectorAlgorithmsFactory() {
        return vectorAlgorithmsFactory;
    }
}

package jsci.maths.vector;

import jsci.util.array.Array1D;

/**
 *
 * @author Mark
 */
public interface AlgorithmsFactory<T,E extends Array1D<T>> {
    Algorithms<T,E> createAlgorithms(E array);
}

package jsci.maths.matrix;

import jsci.util.array.Array1D;
import jsci.util.array.Array2D;

/**
 *
 * @author Mark
 */
public interface AlgorithmsFactory<T,E extends Array2D<T>,F extends Array1D<T>> {
    Algorithms<T,E,F> createAlgorithms(E array);
    jsci.maths.vector.AlgorithmsFactory<T,F> getVectorAlgorithmsFactory();
}

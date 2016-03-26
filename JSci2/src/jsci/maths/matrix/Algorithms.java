package jsci.maths.matrix;

import jsci.util.array.Array1D;
import jsci.util.array.Array2D;

/**
 *
 * @author Mark
 */
public interface Algorithms<T,E extends Array2D<T>,F extends Array1D<T>> {
    T trace(E a);
    E transpose(E a);
    E add(E a, E b);
    E subtract(E a, E b);
    E multiply(E a, E b);
    E scalarMultiply(E a, T r);
    F act(E a, F v);
}

package jsci.maths.vector;

import jsci.util.array.Array1D;

/**
 *
 * @author Mark
 */
public interface Algorithms<T,E extends Array1D<T>> {
    E add(E a, E b);
    E scalarMultiply(E a, T r);
}

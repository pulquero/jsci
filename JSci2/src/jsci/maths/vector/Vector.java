package jsci.maths.vector;

import jsci.maths.vector.impl.RingAlgorithmsFactory;
import jsci.maths.structure.Ring;
import jsci.util.array.Array1D;

/**
 *
 * @author Mark
 */
public class Vector<T,E extends Array1D<T>> {
    protected final AlgorithmsFactory<T,E> factory;
    protected final Algorithms<T,E> algorithms;
    protected final E array;

    public Vector(E array, AlgorithmsFactory<T,E> factory) {
        this.array = array;
        this.factory = factory;
        this.algorithms = factory.createAlgorithms(array);
    }
    public Vector(E array, Ring<T> ring) {
        this(array, new RingAlgorithmsFactory<T,E>(ring));
    }

    public E getArray1D() {
        return array;
    }

    public final T get(int i) {
        return array.get(i);
    }
    public final void set(int i, T r) {
        array.set(i, r);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Vector))
            return false;
        @SuppressWarnings("unchecked")
        Vector<T,?> v = (Vector<T,?>) o;
        return equals(v, 0.0);
    }
    public boolean equals(Vector<T,?> v, double tol) {
        return array.contentEquals(v.array, tol);
    }
    @Override
    public String toString() {
        return array.toString();
    }

    public final int dimension() {
        return array.size();
    }

    public Vector<T,E> add(Vector<T,E> v) {
        E arr = algorithms.add(array, v.array);
        return new Vector<T,E>(arr, factory);
    }

    public Vector<T,E> scalarMultiply(T r) {
        E arr = algorithms.scalarMultiply(array, r);
        return new Vector<T,E>(arr, factory);
    }
}

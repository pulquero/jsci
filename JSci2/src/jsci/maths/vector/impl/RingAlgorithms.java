package jsci.maths.vector.impl;

import jsci.maths.vector.*;
import jsci.maths.structure.Ring;
import jsci.util.array.Array1D;

/**
 *
 * @author Mark
 */
public class RingAlgorithms<T,E extends Array1D<T>> implements Algorithms<T,E> {
    private final Ring<T> ring;

    public RingAlgorithms(Ring<T> r) {
        this.ring = r;
    }
    public E add(E a, E b) {
        @SuppressWarnings("unchecked") // create() returns same dynamic type
        E arr = (E) a.create(a.size());
        for(int i=0; i<a.size(); i++) {
            arr.set(i, ring.add(a.get(i), b.get(i)));
        }
        return arr;
    }

    public E scalarMultiply(E a, T r) {
        @SuppressWarnings("unchecked") // create() returns same dynamic type
        E arr = (E) a.create(a.size());
        for(int i=0; i<a.size(); i++) {
            arr.set(i, ring.multiply(a.get(i), r));
        }
        return arr;
    }
}

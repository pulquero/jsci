package jsci.maths.matrix.impl;

import jsci.maths.matrix.*;
import jsci.maths.structure.Ring;
import jsci.util.array.Array1D;
import jsci.util.array.Array2D;

/**
 *
 * @author Mark
 */
public class RingAlgorithms<T,E extends Array2D<T>,F extends Array1D<T>> implements Algorithms<T,E,F> {
    private final Ring<T> ring;

    public RingAlgorithms(Ring<T> r) {
        this.ring = r;
    }
    public T trace(E a) {
        int n = Math.min(a.rows(), a.columns());
        T tr = ring.zero();
        for(int i=0; i<n; i++) {
            tr = ring.add(tr, a.get(i,i));
        }
        return tr;
    }
    public E transpose(E a) {
        @SuppressWarnings("unchecked") // create() returns same dynamic type
        E arr = (E) a.create(a.columns(), a.rows());
        for(int i=0; i<a.rows(); i++) {
            for(int j=0; j<a.columns(); j++) {
                arr.set(i, j, a.get(j,i));
            }
        }
        return arr;
    }
    public E add(E a, E b) {
        @SuppressWarnings("unchecked") // create() returns same dynamic type
        E arr = (E) a.create(a.rows(), a.columns());
        for(int i=0; i<a.rows(); i++) {
            for(int j=0; j<a.columns(); j++) {
                arr.set(i, j, ring.add(a.get(i,j), b.get(i,j)));
            }
        }
        return arr;
    }
    public E subtract(E a, E b) {
        @SuppressWarnings("unchecked") // create() returns same dynamic type
        E arr = (E) a.create(a.rows(), a.columns());
        for(int i=0; i<a.rows(); i++) {
            for(int j=0; j<a.columns(); j++) {
                arr.set(i, j, ring.subtract(a.get(i,j), b.get(i,j)));
            }
        }
        return arr;
    }
    public E multiply(E a, E b) {
        @SuppressWarnings("unchecked") // create() returns same dynamic type
        E arr = (E) a.create(a.rows(), b.columns());
        for(int i=0; i<a.rows(); i++) {
            for(int j=0; j<b.columns(); j++) {
                T tmp = ring.zero();
                for(int k=0; k<a.columns(); k++) {
                    tmp = ring.add(tmp, ring.multiply(a.get(i, k), b.get(k, j)));
                }
                arr.set(i, j, tmp);
            }
        }
        return arr;
    }

    public F act(E a, F v) {
        @SuppressWarnings("unchecked") // create() returns same dynamic type
        F arr = (F) v.create(v.size());
        for(int i=0; i<a.rows(); i++) {
            T tmp = ring.zero();
            for(int k=0; k<a.columns(); k++) {
                tmp = ring.add(tmp, ring.multiply(a.get(i, k), v.get(k)));
            }
            arr.set(i, tmp);
        }
        return arr;
    }

    public E scalarMultiply(E a, T r) {
        @SuppressWarnings("unchecked") // create() returns same dynamic type
        E arr = (E) a.create(a.rows(), a.columns());
        for(int i=0; i<a.rows(); i++) {
            for(int j=0; j<a.columns(); j++) {
                arr.set(i, j, ring.multiply(a.get(i,j), r));
            }
        }
        return arr;
    }
}

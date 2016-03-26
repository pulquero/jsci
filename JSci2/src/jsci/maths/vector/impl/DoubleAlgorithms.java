package jsci.maths.vector.impl;

import jsci.maths.vector.Algorithms;
import jsci.util.array.DoubleArray1D;

/**
 *
 * @author Mark
 */
public class DoubleAlgorithms<E extends DoubleArray1D> implements Algorithms<Double,E> {

    public E add(E a, E b) {
        @SuppressWarnings("unchecked") // create() returns same dynamic type
        E arr = (E) a.create(a.size());
        for(int i=0; i<a.size(); i++) {
            arr.setDouble(i, a.getDouble(i) + b.getDouble(i));
        }
        return arr;
    }
    public E scalarMultiply(E a, Double x) {
        @SuppressWarnings("unchecked") // create() returns same dynamic type
        E arr = (E) a.create(a.size());
        for(int i=0; i<a.size(); i++) {
            arr.setDouble(i, a.getDouble(i) * x);
        }
        return arr;
    }
}

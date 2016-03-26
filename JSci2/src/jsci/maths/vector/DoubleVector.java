package jsci.maths.vector;

import jsci.maths.vector.impl.DoubleAlgorithmsFactory;
import jsci.util.array.DoubleArray1D;

/**
 *
 * @author Mark
 */
public final class DoubleVector<E extends DoubleArray1D> extends Vector<Double,E> {
    public DoubleVector(E array, AlgorithmsFactory<Double,E> factory) {
        super(array, factory);
    }
    public DoubleVector(E array) {
        this(array, new DoubleAlgorithmsFactory<E>());
    }

    @Override
    public E getArray1D() {
        return array;
    }

    public double getDouble(int i) {
        return array.getDouble(i);
    }
    public void setDouble(int i, double x) {
        array.setDouble(i, x);
    }

    public DoubleVector<E> add(DoubleVector<E> v) {
        E arr = algorithms.add(array, v.array);
        return new DoubleVector<E>(arr, factory);
    }

    public DoubleVector<E> scalarMultiply(double x) {
        E arr = algorithms.scalarMultiply(array, x);
        return new DoubleVector<E>(arr, factory);
    }
}

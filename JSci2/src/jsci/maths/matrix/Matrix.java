package jsci.maths.matrix;

import jsci.maths.matrix.impl.RingAlgorithmsFactory;
import jsci.maths.structure.Ring;
import jsci.maths.vector.Vector;
import jsci.util.array.Array1D;
import jsci.util.array.Array2D;

/**
 *
 * @author Mark
 */
public class Matrix<T,E extends Array2D<T>,F extends Array1D<T>> {
    protected final AlgorithmsFactory<T,E,F> factory;
    protected final Algorithms<T,E,F> algorithms;
    protected final E array;

    public Matrix(E array, AlgorithmsFactory<T,E,F> factory) {
        this.array = array;
        this.factory = factory;
        this.algorithms = factory.createAlgorithms(array);
    }
    public Matrix(E array, Ring<T> ring) {
        this(array, new RingAlgorithmsFactory<T,E,F>(ring));
    }

    public E getArray2D() {
        return array;
    }

    public final T get(int i, int j) {
        return array.get(i, j);
    }
    public final void set(int i, int j, T r) {
        array.set(i,j, r);
    }
    public final int rows() {
        return array.rows();
    }
    public final int columns() {
        return array.columns();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Matrix))
            return false;
        @SuppressWarnings("unchecked")
        Matrix<T,E,F> m = (Matrix<T,E,F>) o;
        return equals(m, 0.0);
    }
    public boolean equals(Matrix<T,?,?> m, double tol) {
        return array.contentEquals(m.array, tol);
    }
    @Override
    public String toString() {
        return array.toString();
    }

    public T trace() {
        return algorithms.trace(array);
    }

    public Matrix<T,E,F> transpose() {
        E arr = algorithms.transpose(array);
        return new Matrix<T,E,F>(arr, factory);
    }

    public Matrix<T,E,F> add(Matrix<T,E,F> m) {
        E arr = algorithms.add(array, m.array);
        return new Matrix<T,E,F>(arr, factory);
    }

    public Matrix<T,E,F> subtract(Matrix<T,E,F> m) {
        E arr = algorithms.subtract(array, m.array);
        return new Matrix<T,E,F>(arr, factory);
    }

    public Matrix<T,E,F> multiply(Matrix<T,E,F> m) {
        E arr = algorithms.multiply(array, m.array);
        return new Matrix<T,E,F>(arr, factory);
    }

    public Matrix<T,E,F> scalarMultiply(T r) {
        E arr = algorithms.scalarMultiply(array, r);
        return new Matrix<T,E,F>(arr, factory);
    }

    public Vector<T,F> act(Vector<T,F> v) {
        F arr = algorithms.act(array, v.getArray1D());
        jsci.maths.vector.AlgorithmsFactory<T,F> vectorAlgoFactory = factory.getVectorAlgorithmsFactory();
        return new Vector<T,F>(arr, vectorAlgoFactory);
    }
}

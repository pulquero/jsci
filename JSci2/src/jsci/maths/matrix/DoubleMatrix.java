package jsci.maths.matrix;

import jsci.maths.matrix.impl.DoubleAlgorithmsFactory;
import jsci.maths.vector.DoubleVector;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.DoubleArray2D;

/**
 *
 * @author Mark
 */
public final class DoubleMatrix<E extends DoubleArray2D,F extends DoubleArray1D> extends Matrix<Double,E,F> {
    public DoubleMatrix(E array, AlgorithmsFactory<Double,E,F> factory) {
        super(array, factory);
    }

    public static DoubleMatrix<DoubleArray2D,DoubleArray1D> create(DoubleArray2D array) {
        return new DoubleMatrix<DoubleArray2D,DoubleArray1D>(array, new DoubleAlgorithmsFactory<DoubleArray2D,DoubleArray1D>());
    }

    @Override
    public E getArray2D() {
        return array;
    }

    public double getDouble(int i, int j) {
        return array.getDouble(i, j);
    }
    public void setDouble(int i, int j, double x) {
        array.setDouble(i,j, x);
    }

    public DoubleMatrix<E,F> transpose() {
        E arr = algorithms.transpose(array);
        return new DoubleMatrix<E,F>(arr, factory);
    }

    public DoubleMatrix<E,F> add(DoubleMatrix<E,F> m) {
        E arr = algorithms.add(array, m.array);
        return new DoubleMatrix<E,F>(arr, factory);
    }

    public DoubleMatrix<E,F> subtract(DoubleMatrix<E,F> m) {
        E arr = algorithms.subtract(array, m.array);
        return new DoubleMatrix<E,F>(arr, factory);
    }

    public DoubleMatrix<E,F> multiply(DoubleMatrix<E,F> m) {
        E arr = algorithms.multiply(array, m.array);
        return new DoubleMatrix<E,F>(arr, factory);
    }

    public DoubleMatrix<E,F> scalarMultiply(double x) {
        E arr = algorithms.scalarMultiply(array, x);
        return new DoubleMatrix<E,F>(arr, factory);
    }

    public DoubleVector<F> act(DoubleVector<F> v) {
        F arr = algorithms.act(array, v.getArray1D());
        jsci.maths.vector.AlgorithmsFactory<Double,F> vectorAlgoFactory = factory.getVectorAlgorithmsFactory();
        return new DoubleVector<F>(arr, vectorAlgoFactory);
    }
}

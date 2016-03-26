package jsci.maths.vector.impl;

import jsci.maths.matrix.impl.AlgorithmsDenseDoubleArray2D;
import jsci.util.array.DenseDoubleArray1D;

/**
 *
 * @author Mark
 */
public class AlgorithmsDenseDoubleArray1D extends DenseDoubleArray1D implements AlgorithmsDoubleArray1D {
    public AlgorithmsDenseDoubleArray1D(int size) {
        super(size);
    }
    public AlgorithmsDenseDoubleArray1D(double[] array) {
        super(array);
    }

    @Override
    public AlgorithmsDenseDoubleArray1D create(int size) {
        return new AlgorithmsDenseDoubleArray1D(size);
    }

    @Override
    public AlgorithmsDoubleArray1D add(AlgorithmsDoubleArray1D a, AlgorithmsDoubleArray1D b) {
        return a.add(b);
    }

    @Override
    public AlgorithmsDoubleArray1D add(AlgorithmsDoubleArray1D b) {
        return b.addOp(this);
    }

    @Override
    public AlgorithmsDenseDoubleArray1D addOp(AlgorithmsDenseDoubleArray1D a) {
        double[] arr = new double[size()];
        for(int i=0; i<arr.length; i++) {
            arr[i] = a.array[i] + this.array[i];
        }
        return new AlgorithmsDenseDoubleArray1D(arr);
    }

    public AlgorithmsDoubleArray1D scalarMultiply(AlgorithmsDoubleArray1D a, Double x) {
        return a.scalarMultiply(x);
    }
    public AlgorithmsDoubleArray1D scalarMultiply(double x) {
        double[] arr = new double[size()];
        for(int i=0; i<size(); i++) {
            arr[i] = array[i] * x;
        }
        return new AlgorithmsDenseDoubleArray1D(arr);
    }

    public AlgorithmsDenseDoubleArray1D actOp(AlgorithmsDenseDoubleArray2D a) {
        double[] arr = new double[size()];
        for(int i=0; i<a.rows(); i++) {
            double tmp = 0.0;
            for(int k=0; k<a.columns(); k++) {
                tmp += a.getDouble(i, k)*array[k];
            }
            arr[i] = tmp;
        }
        return new AlgorithmsDenseDoubleArray1D(arr);
    }
}

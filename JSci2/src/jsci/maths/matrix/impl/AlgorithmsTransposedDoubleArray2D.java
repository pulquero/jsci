package jsci.maths.matrix.impl;

import jsci.maths.vector.impl.AlgorithmsDoubleArray1D;
import jsci.util.array.TransposedDoubleArray2D;

/**
 *
 * @author Mark
 */
public class AlgorithmsTransposedDoubleArray2D<E extends AlgorithmsDoubleArray2D> extends TransposedDoubleArray2D<E> implements AlgorithmsDoubleArray2D {
    public AlgorithmsTransposedDoubleArray2D(E arr) {
        super(arr);
    }

    @Override
    public AlgorithmsTransposedDoubleArray2D create(int rows, int cols) {
        return new AlgorithmsTransposedDoubleArray2D(array.create(cols, rows));
    }

    @Override
    public Double trace(AlgorithmsDoubleArray2D a) {
        return a.trace();
    }

    @Override
    public double trace() {
        return array.trace();
    }

    @Override
    public AlgorithmsDoubleArray2D transpose(AlgorithmsDoubleArray2D a) {
        return a.transpose();
    }

    @Override
    public E transpose() {
        return array;
    }

    @Override
    public AlgorithmsDoubleArray2D add(AlgorithmsDoubleArray2D a, AlgorithmsDoubleArray2D b) {
        return a.add(b);
    }

    @Override
    public AlgorithmsDoubleArray2D add(AlgorithmsDoubleArray2D b) {
        return array.add(b.transpose()).transpose();
    }

    @Override
    public AlgorithmsDenseDoubleArray2D addOp(AlgorithmsDenseDoubleArray2D a) {
        double[][] arr = new double[rows()][columns()];
        for(int i=0; i<arr.length; i++) {
            for(int j=0; j<arr[0].length; j++) {
                arr[i][j] = a.getDouble(i, j) + this.array.getDouble(j, i);
            }
        }
        return new AlgorithmsDenseDoubleArray2D(arr);
    }


    @Override
    public AlgorithmsDoubleArray2D subtract(AlgorithmsDoubleArray2D a, AlgorithmsDoubleArray2D b) {
        return a.subtract(b);
    }

    @Override
    public AlgorithmsDoubleArray2D subtract(AlgorithmsDoubleArray2D b) {
        return array.subtract(b.transpose()).transpose();
    }

    @Override
    public AlgorithmsDenseDoubleArray2D subtractOp(AlgorithmsDenseDoubleArray2D a) {
        double[][] arr = new double[rows()][columns()];
        for(int i=0; i<arr.length; i++) {
            for(int j=0; j<arr[0].length; j++) {
                arr[i][j] = a.getDouble(i, j) - this.array.getDouble(j, i);
            }
        }
        return new AlgorithmsDenseDoubleArray2D(arr);
    }

    @Override
    public AlgorithmsDoubleArray2D multiply(AlgorithmsDoubleArray2D a, AlgorithmsDoubleArray2D b) {
        return a.multiply(b);
    }

    @Override
    public AlgorithmsDoubleArray2D multiply(AlgorithmsDoubleArray2D b) {
        return b.transpose().multiply(array).transpose();
    }

    @Override
    public AlgorithmsDenseDoubleArray2D multiplyOp(AlgorithmsDenseDoubleArray2D a) {
        double[][] arr = new double[a.rows()][columns()];
        for(int i=0; i<a.rows(); i++) {
            for(int j=0; j<columns(); j++) {
                double tmp = 0.0;
                for(int k=0; k<a.columns(); k++) {
                    tmp += a.getDouble(i, k)*this.array.getDouble(j, k);
                }
                arr[i][j] = tmp;
            }
        }
        return new AlgorithmsDenseDoubleArray2D(arr);
    }

    @Override
    public AlgorithmsDoubleArray1D act(AlgorithmsDoubleArray2D a, AlgorithmsDoubleArray1D v) {
        return a.act(v);
    }

    @Override
    public AlgorithmsDoubleArray1D act(AlgorithmsDoubleArray1D v) {
        AlgorithmsDoubleArray1D w = v.create(v.size());
        for(int i=0; i<rows(); i++) {
            double tmp = 0.0;
            for(int k=0; k<columns(); k++) {
                tmp += getDouble(k, i)*v.getDouble(k);
            }
            w.setDouble(i, tmp);
        }
        return w;
    }

    @Override
    public AlgorithmsDoubleArray2D scalarMultiply(AlgorithmsDoubleArray2D a, Double r) {
        return a.scalarMultiply(r);
    }

    @Override
    public AlgorithmsDoubleArray2D scalarMultiply(double x) {
        return array.scalarMultiply(x).transpose();
    }

}

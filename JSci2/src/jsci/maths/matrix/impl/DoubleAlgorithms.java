package jsci.maths.matrix.impl;

import java.util.HashMap;
import java.util.Map;
import jsci.maths.matrix.Algorithms;
import jsci.maths.vector.impl.AlgorithmsDoubleArray1D;
import jsci.util.array.*;

/**
 *
 * @author Mark
 */
public class DoubleAlgorithms<E extends DoubleArray2D,F extends DoubleArray1D> implements Algorithms<Double,E,F> {
    private final Map<String,String> shapesAdditive = new HashMap<String,String>();
    private final Map<String,String> shapesMultiplicative = new HashMap<String,String>();

    public DoubleAlgorithms() {
        addShapes(DiagonalDoubleArray2D.class, DiagonalDoubleArray2D.class, DiagonalDoubleArray2D.class, DiagonalDoubleArray2D.class);

        addShapes(TridiagonalDoubleArray2D.class, DiagonalDoubleArray2D.class, TridiagonalDoubleArray2D.class, TridiagonalDoubleArray2D.class);
        addShapes(DiagonalDoubleArray2D.class, TridiagonalDoubleArray2D.class, TridiagonalDoubleArray2D.class, TridiagonalDoubleArray2D.class);
        addShapes(TridiagonalDoubleArray2D.class, TridiagonalDoubleArray2D.class, TridiagonalDoubleArray2D.class, DenseDoubleArray2D.class);

        addShapes(LowerTriangularDoubleArray2D.class, LowerTriangularDoubleArray2D.class, LowerTriangularDoubleArray2D.class, LowerTriangularDoubleArray2D.class);
    }
    private void addShapes(Class a, Class b, Class additive, Class multiplicative) {
        String key = a.getName() + b.getName();
        shapesAdditive.put(key, additive.getName());
        shapesMultiplicative.put(key, multiplicative.getName());
    }
    private String getAdditiveShape(E a, E b) {
        String key = a.getClass().getName() + b.getClass().getName();
        return shapesAdditive.get(key);
    }
    private String getMultiplicativeShape(E a, E b) {
        String key = a.getClass().getName() + b.getClass().getName();
        return shapesMultiplicative.get(key);
    }

    public Double trace(E a) {
        if(a instanceof AlgorithmsDoubleArray2D)
            return ((AlgorithmsDoubleArray2D)a).trace();
        else
            return traceImpl(a);
    }
    private double traceImpl(E a) {
        int n = Math.min(a.rows(), a.columns());
        double tr = 0.0;
        for(int i=0; i<n; i++) {
            tr += a.getDouble(i,i);
        }
        return tr;
    }

    public E transpose(E a) {
        if(a instanceof AlgorithmsDoubleArray2D)
            return (E) ((AlgorithmsDoubleArray2D)a).transpose();
        else
            return transposeImpl(a);
    }
    private E transposeImpl(E a) {
        return (E) new TransposedDoubleArray2D<E>(a);
    }

    private E createAdditive(E a, E b) {
        String clsName = getAdditiveShape(a, b);
        if(a.getClass().getName().equals(clsName)) {
            return (E) a.create(a.rows(), a.columns());
        } else if(b.getClass().getName().equals(clsName)) {
            return (E) b.create(b.rows(), b.columns());
        } else {
            return (E) new DenseDoubleArray2D(a.rows(), a.columns());
        }
    }

    private E createMultiplicative(E a, E b) {
        String clsName = getMultiplicativeShape(a, b);
        if(a.getClass().getName().equals(clsName)) {
            return (E) a.create(a.rows(), b.columns());
        } else if(b.getClass().getName().equals(clsName)) {
            return (E) b.create(a.rows(), b.columns());
        } else {
            return (E) new DenseDoubleArray2D(a.rows(), b.columns());
        }
    }

    public E add(E a, E b) {
        if(a instanceof AlgorithmsDoubleArray2D && b instanceof AlgorithmsDoubleArray2D)
            return (E) ((AlgorithmsDoubleArray2D)a).add((AlgorithmsDoubleArray2D) b);
        else
            return addImpl(a, b);
    }
    private E addImpl(E a, E b) {
        E arr = createAdditive(a,b);
        for(int i=0; i<a.rows(); i++) {
            for(int j=0; j<a.columns(); j++) {
                double v = a.getDouble(i,j) + b.getDouble(i, j);
                arr.setDouble(i,j, v);
            }
        }
        return arr;
    }
    public E subtract(E a, E b) {
        if(a instanceof AlgorithmsDoubleArray2D && b instanceof AlgorithmsDoubleArray2D)
            return (E) ((AlgorithmsDoubleArray2D)a).subtract((AlgorithmsDoubleArray2D) b);
        else
            return subtractImpl(a, b);
    }
    private E subtractImpl(E a, E b) {
        E arr = createAdditive(a,b);
        for(int i=0; i<a.rows(); i++) {
            for(int j=0; j<a.columns(); j++) {
                double v = a.getDouble(i,j) - b.getDouble(i, j);
                arr.setDouble(i,j, v);
            }
        }
        return arr;
    }

    public E multiply(E a, E b) {
        if(a instanceof AlgorithmsDoubleArray2D && b instanceof AlgorithmsDoubleArray2D)
            return (E) ((AlgorithmsDoubleArray2D)a).multiply((AlgorithmsDoubleArray2D) b);
        else
            return multiplyImpl(a, b);
    }
    private E multiplyImpl(E a, E b) {
        E arr = createMultiplicative(a,b);
        for(int i=0; i<a.rows(); i++) {
            for(int j=0; j<b.columns(); j++) {
                double tmp = 0.0;
                for(int k=0; k<a.columns(); k++) {
                    tmp += a.getDouble(i, k)*b.getDouble(k, j);
                }
                arr.setDouble(i, j, tmp);
            }
        }
        return arr;
    }


    public F act(E a, F v) {
        if(a instanceof AlgorithmsDoubleArray2D && v instanceof AlgorithmsDoubleArray1D)
            return (F) ((AlgorithmsDoubleArray2D)a).act((AlgorithmsDoubleArray1D) v);
        else
            return actImpl(a, v);
    }
    private F actImpl(E a, F v) {
        @SuppressWarnings("unchecked") // create() returns same dynamic type
        F arr = (F) v.create(v.size());
        for(int i=0; i<a.rows(); i++) {
            double tmp = 0.0;
            for(int k=0; k<a.columns(); k++) {
                tmp += a.getDouble(i, k)*v.getDouble(k);
            }
            arr.setDouble(i, tmp);
        }
        return arr;
    }

    public E scalarMultiply(E a, Double x) {
        return scalarMultiply(a, x);
    }
    public E scalarMultiply(E a, double x) {
        if(a instanceof AlgorithmsDoubleArray2D)
            return (E) ((AlgorithmsDoubleArray2D)a).scalarMultiply(x);
        else
            return scalarMultiplyImpl(a, x);
    }
    private E scalarMultiplyImpl(E a, double x) {
        @SuppressWarnings("unchecked") // create() returns same dynamic type
        E arr = (E) a.create(a.rows(), a.columns());
        for(int i=0; i<a.rows(); i++) {
            for(int j=0; j<a.columns(); j++) {
                arr.setDouble(i,j, a.getDouble(i,j) * x);
            }
        }
        return arr;
    }
}

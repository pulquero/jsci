package jsci.maths.matrix.impl;

import jsci.maths.matrix.*;
import jsci.maths.vector.impl.AlgorithmsDoubleArray1D;
import jsci.util.array.DoubleArray2D;

/**
 *
 * @author Mark
 */
public interface AlgorithmsDoubleArray2D extends DoubleArray2D, Algorithms<Double,AlgorithmsDoubleArray2D,AlgorithmsDoubleArray1D> {
    @Override
    AlgorithmsDoubleArray2D create(int rows, int cols);
    double trace();
    AlgorithmsDoubleArray2D transpose();

    AlgorithmsDoubleArray2D add(AlgorithmsDoubleArray2D a);
    AlgorithmsDoubleArray2D addOp(AlgorithmsDenseDoubleArray2D b);

    AlgorithmsDoubleArray2D subtract(AlgorithmsDoubleArray2D a);
    AlgorithmsDoubleArray2D subtractOp(AlgorithmsDenseDoubleArray2D b);

    AlgorithmsDoubleArray2D multiply(AlgorithmsDoubleArray2D a);
    AlgorithmsDoubleArray2D multiplyOp(AlgorithmsDenseDoubleArray2D b);

    AlgorithmsDoubleArray1D act(AlgorithmsDoubleArray1D a);

    AlgorithmsDoubleArray2D scalarMultiply(double x);
}

package jsci.maths.vector.impl;

import jsci.maths.matrix.impl.AlgorithmsDenseDoubleArray2D;
import jsci.maths.vector.Algorithms;
import jsci.util.array.DoubleArray1D;

/**
 *
 * @author Mark
 */
public interface AlgorithmsDoubleArray1D extends DoubleArray1D, Algorithms<Double,AlgorithmsDoubleArray1D> {
    @Override
    AlgorithmsDoubleArray1D create(int size);

    AlgorithmsDoubleArray1D add(AlgorithmsDoubleArray1D a);
    AlgorithmsDenseDoubleArray1D addOp(AlgorithmsDenseDoubleArray1D a);

    AlgorithmsDoubleArray1D scalarMultiply(double x);

    AlgorithmsDenseDoubleArray1D actOp(AlgorithmsDenseDoubleArray2D a);
}

package jsci.util.array;

/**
 *
 * @author Mark
 */
public interface DoubleArray1D extends Array1D<Double> {
    @Override
    DoubleArray1D create(int size);
    double getDouble(int i);
    void setDouble(int i, double x);
}

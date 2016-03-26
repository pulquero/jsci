package jsci.util.array;

/**
 *
 * @author Mark
 */
public interface DoubleArray2D extends Array2D<Double> {
    @Override
    DoubleArray2D create(int rows, int cols);
    double getDouble(int i, int j);
    void setDouble(int i, int j, double x);
}

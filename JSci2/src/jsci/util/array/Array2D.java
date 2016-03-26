package jsci.util.array;

/**
 *
 * @author Mark
 */
public interface Array2D<T> {
    Array2D<T> create(int rows, int cols);
    T get(int i, int j);
    void set(int i, int j, T obj);
    int rows();
    int columns();
    boolean contentEquals(Array2D<T> arr, double tol);
}

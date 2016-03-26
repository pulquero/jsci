package jsci.util.array;

/**
 *
 * @author Mark
 */
public interface Array1D<T> {
    Array1D<T> create(int size);
    T get(int i);
    void set(int i, T obj);
    int size();
    boolean contentEquals(Array1D<T> arr, double tol);
}

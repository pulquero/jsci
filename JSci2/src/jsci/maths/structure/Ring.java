package jsci.maths.structure;

/**
 *
 * @author Mark
 */
public interface Ring<T> {
    T zero();
    T unit();
    T add(T a, T b);
    T subtract(T a, T b);
    T multiply(T a, T b);
}

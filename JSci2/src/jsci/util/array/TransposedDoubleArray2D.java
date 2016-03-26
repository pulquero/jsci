package jsci.util.array;

/**
 *
 * @author Mark
 */
public class TransposedDoubleArray2D<E extends DoubleArray2D> extends AbstractDoubleArray2D {
    protected final E array;

    public TransposedDoubleArray2D(E array) {
        this.array = array;
    }
    public double getDouble(int i, int j) {
        return array.getDouble(j, i);
    }
    public void setDouble(int i, int j, double x) {
        array.setDouble(j, i, x);
    }

    @Override
    public TransposedDoubleArray2D<E> create(int rows, int cols) {
        return new TransposedDoubleArray2D(array.create(cols,rows));
    }

    public int rows() {
        return array.columns();
    }

    public int columns() {
        return array.rows();
    }
}

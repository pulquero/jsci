package jsci.util.array;

/**
 *
 * @author Mark
 */
public class DenseDoubleArray1D extends AbstractDoubleArray1D {
    protected final double[] array;

    public DenseDoubleArray1D(int size) {
        array = new double[size];
    }
    /**
     * Wraps an existing array.
     * @param array
     */
    public DenseDoubleArray1D(double[] array) {
        this.array = array;
    }

    @Override
    public DenseDoubleArray1D create(int size) {
        return new DenseDoubleArray1D(size);
    }

    public final double getDouble(int i) {
        return array[i];
    }

    public final void setDouble(int i, double x) {
        array[i] = x;
    }

    public final int size() {
        return array.length;
    }

    @Override
    public boolean contentEquals(AbstractDoubleArray1D arr, double tol) {
        return arr.contentEqualsOp(this, tol);
    }

    @Override
    protected boolean contentEqualsOp(DenseDoubleArray1D arr, double tol) {
        for(int i=0; i<size(); i++) {
            if(Math.abs(arr.array[i]-array[i]) > tol)
                return false;
        }
        return true;
    }
}

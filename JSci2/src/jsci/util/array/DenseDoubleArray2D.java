package jsci.util.array;

/**
 *
 * @author Mark
 */
public class DenseDoubleArray2D extends AbstractDoubleArray2D {
    protected final double[][] array;

    public DenseDoubleArray2D(int rows, int cols) {
        array = new double[rows][cols];
    }
    /**
     * Wraps an existing array.
     * @param array
     */
    public DenseDoubleArray2D(double[][] array) {
        this.array = array;
    }

    @Override
    public DenseDoubleArray2D create(int rows, int cols) {
        return new DenseDoubleArray2D(rows, cols);
    }

    public final double getDouble(int i, int j) {
        return array[i][j];
    }

    public final void setDouble(int i, int j, double x) {
        array[i][j] = x;
    }

    public final int rows() {
        return array.length;
    }

    public final int columns() {
        return array[0].length;
    }

    @Override
    public boolean contentEquals(AbstractDoubleArray2D arr, double tol) {
        return arr.contentEqualsOp(this, tol);
    }

    @Override
    protected boolean contentEqualsOp(DenseDoubleArray2D arr, double tol) {
        for(int i=0; i<rows(); i++) {
            for(int j=0; j<columns(); j++) {
                if(Math.abs(arr.array[i][j]-array[i][j]) > tol)
                    return false;
            }
        }
        return true;
    }
}

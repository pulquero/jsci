package jsci.util.array;

/**
 *
 * @author Mark
 */
public class DiagonalDoubleArray2D extends AbstractDoubleArray2D {
    private final DoubleArray1D array;

    public DiagonalDoubleArray2D(DoubleArray1D array) {
        this.array = array;
    }

    /**
    * Constructs a matrix from an array.
    * Any non-diagonal elements in the array are ignored.
    */
    public DiagonalDoubleArray2D(double[][] arr) {
        int size = Math.min(arr.length, arr[0].length);
        double[] diag = new double[size];
        for(int i=0; i<size; i++)
            diag[i] = arr[i][i];
        array = new DenseDoubleArray1D(diag);
    }

    public double getDouble(int i, int j) {
        if(i == j)
            return array.getDouble(i);
        else
            return 0.0;
    }
    public void setDouble(int i, int j, double x) {
        if(i == j)
            array.setDouble(i, x);
        else if(x != 0.0)
            throw new Array2DStoreException(i, j, x);
    }

    @Override
    public DiagonalDoubleArray2D create(int rows, int cols) {
        return new DiagonalDoubleArray2D(array.create(rows));
    }

    public int rows() {
        return array.size();
    }

    public int columns() {
        return array.size();
    }
}

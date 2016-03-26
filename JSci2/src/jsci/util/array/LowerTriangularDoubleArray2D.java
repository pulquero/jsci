package jsci.util.array;

/**
 *
 * @author Mark
 */
public class LowerTriangularDoubleArray2D extends AbstractDoubleArray2D {
    protected final double[][] array;
    protected final boolean strict;

    public LowerTriangularDoubleArray2D(int rows, int cols, boolean strict) {
        array = new double[rows][];
        for(int i=0; i<rows; i++) {
            int len = (strict ? i : i+1);
            array[i] = new double[len];
        }
        this.strict = strict;
    }
    /**
     * Wraps an existing array.
     * @param arr
     * @param strict
     */
    public LowerTriangularDoubleArray2D(double[][] arr, boolean strict) {
        array = arr;
        this.strict = strict;
    }
    public LowerTriangularDoubleArray2D(double[][] arr) {
        this(arr, false);
    }

    public LowerTriangularDoubleArray2D create(int rows, int cols) {
        return new LowerTriangularDoubleArray2D(rows, cols, strict);
    }

    public double getDouble(int i, int j) {
        boolean isNonZero = (strict ? j<i : j<=i);
        if(isNonZero)
            return array[i][j];
        else
            return 0.0;
    }

    public void setDouble(int i, int j, double x) {
        boolean isNonZero = (strict ? j<i : j<=i);
        if(isNonZero)
            array[i][j] = x;
        else if(x != 0.0)
            throw new Array2DStoreException(i, j, x);
    }

    public int rows() {
        return array.length;
    }

    public int columns() {
        int lastRowLen = array[array.length-1].length;
        return (strict ? lastRowLen+1 : lastRowLen);
    }

}

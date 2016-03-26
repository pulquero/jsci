package jsci.util.array;

/**
 *
 * @author Mark
 */
public class TridiagonalDoubleArray2D extends AbstractDoubleArray2D {
        /**
        * Tridiagonal data.
        */
        protected final double ldiag[];
        protected final double diag[];
        protected final double udiag[];

        public TridiagonalDoubleArray2D(final int size) {
            ldiag=new double[size];
            diag=new double[size];
            udiag=new double[size];
        }

        /**
        * Constructs a matrix from an array.
        * Any non-tridiagonal elements in the array are ignored.
        */
        public TridiagonalDoubleArray2D(final double array[][]) {
                this(array.length);
                diag[0]=array[0][0];
                udiag[0]=array[0][1];
                int i=1;
                for(;i<array.length-1;i++) {
                        ldiag[i]=array[i][i-1];
                        diag[i]=array[i][i];
                        udiag[i]=array[i][i+1];
                }
                ldiag[i]=array[i][i-1];
                diag[i]=array[i][i];
        }

        @Override
    public TridiagonalDoubleArray2D create(int rows, int cols) {
        return new TridiagonalDoubleArray2D(rows);
    }

    public double getDouble(int i, int j) {
            if(j == i-1)
                    return ldiag[i];
            else if(j == i)
                    return diag[i];
            else if(j == i+1)
                    return udiag[i];
            else
                    return 0.0;
    }

    public void setDouble(int i, int j, double x) {
            if(j == i-1)
                    ldiag[i] = x;
            else if(j == i)
                    diag[i] = x;
            else if(j == i+1)
                    udiag[i] = x;
            else if(x != 0.0)
                    throw new Array2DStoreException(i, j, x);
    }

    public int rows() {
        return diag.length;
    }

    public int columns() {
        return diag.length;
    }

}

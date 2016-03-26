package jsci.util.array;

/**
 *
 * @author Mark
 */
public abstract class AbstractDoubleArray2D implements DoubleArray2D {
    public final Double get(int i, int j) {
        return Double.valueOf(getDouble(i,j));
    }
    public final void set(int i, int j, Double obj) {
        setDouble(i,j, obj.doubleValue());
    }


    public final boolean contentEquals(Array2D<Double> arr, double tol) {
        if(arr instanceof DoubleArray2D) {
            return contentEquals((DoubleArray2D)arr, tol);
        } else {
            for(int i=0; i<rows(); i++) {
                for(int j=0; j<columns(); j++) {
                    if(Math.abs(getDouble(i,j) - arr.get(i, j).doubleValue()) > tol)
                        return false;
                }
            }
            return true;
        }
    }

    public final boolean contentEquals(DoubleArray2D arr, double tol) {
        if(arr instanceof AbstractDoubleArray2D) {
            return contentEquals((AbstractDoubleArray2D)arr, tol);
        } else {
            return contentEqualsImpl(arr, tol);
        }
    }

    private boolean contentEqualsImpl(DoubleArray2D arr, double tol) {
        for(int i=0; i<rows(); i++) {
            for(int j=0; j<columns(); j++) {
                if(Math.abs(getDouble(i,j) - arr.getDouble(i, j)) > tol)
                    return false;
            }
        }
        return true;
    }

    public boolean contentEquals(AbstractDoubleArray2D arr, double tol) {
        return arr.contentEqualsOp(this, tol);
    }

    protected boolean contentEqualsOp(AbstractDoubleArray2D arr, double tol) {
        return arr.contentEqualsImpl(this, tol);
    }

    protected boolean contentEqualsOp(DenseDoubleArray2D arr, double tol) {
        for(int i=0; i<rows(); i++) {
            for(int j=0; j<columns(); j++) {
                if(Math.abs(arr.array[i][j] - getDouble(i,j)) > tol)
                    return false;
            }
        }
        return true;
    }

    @Override
        public String toString() {
                final StringBuilder buf=new StringBuilder(5*rows()*columns());
                String newline = "";
                for(int i=0;i<rows();i++) {
                        buf.append(newline);
                        String sep = "";
                        for(int j=0;j<columns();j++) {
                                buf.append(sep);
                                buf.append(getDouble(i,j));
                                sep = " ";
                        }
                        newline = "\n";
                }
                return buf.toString();
        }
}

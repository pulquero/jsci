package jsci.util.array;

/**
 *
 * @author Mark
 */
public abstract class AbstractDoubleArray1D implements DoubleArray1D {
    public final Double get(int i) {
        return Double.valueOf(getDouble(i));
    }
    public final void set(int i, Double obj) {
        setDouble(i, obj.doubleValue());
    }


    public final boolean contentEquals(Array1D<Double> arr, double tol) {
        if(arr instanceof DoubleArray1D) {
            return contentEquals((DoubleArray1D)arr, tol);
        } else {
            for(int i=0; i<size(); i++) {
                if(Math.abs(getDouble(i)-arr.get(i).doubleValue()) > tol)
                    return false;
            }
            return true;
        }
    }

    public final boolean contentEquals(DoubleArray1D arr, double tol) {
        if(arr instanceof AbstractDoubleArray1D) {
            return contentEquals((AbstractDoubleArray1D)arr, tol);
        } else {
            return contentEqualsImpl(arr, tol);
        }
    }

    private boolean contentEqualsImpl(DoubleArray1D arr, double tol) {
        for(int i=0; i<size(); i++) {
            if(Math.abs(getDouble(i)-arr.getDouble(i)) > tol)
                return false;
        }
        return true;
    }

    public boolean contentEquals(AbstractDoubleArray1D arr, double tol) {
        return arr.contentEqualsOp(this, tol);
    }

    protected boolean contentEqualsOp(AbstractDoubleArray1D arr, double tol) {
        return arr.contentEqualsImpl(this, tol);
    }

    protected boolean contentEqualsOp(DenseDoubleArray1D arr, double tol) {
        for(int i=0; i<size(); i++) {
            if(Math.abs(arr.array[i]-get(i)) > tol)
                return false;
        }
        return true;
    }

    @Override
        public String toString() {
                final StringBuilder buf=new StringBuilder(5*size());
                String sep = "";
                for(int i=0;i<size();i++) {
                        buf.append(sep);
                        buf.append(getDouble(i));
                        sep = " ";
                }
                return buf.toString();
        }
}

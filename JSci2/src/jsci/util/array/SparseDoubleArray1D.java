package jsci.util.array;

/**
 *
 * @author Mark
 */
public class SparseDoubleArray1D extends AbstractDoubleArray1D {
        private final int size;
        private final double zeroTol;
        private double vector[];
        /**
        * Sparse indexing data.
        * Contains the component positions of each element,
        * e.g. <code>pos[n]</code> is the component position
        * of the <code>n</code>th element
        * (the <code>pos[n]</code>th component is stored at index <code>n</code>).
        */
        private int pos[];
        private int count = 0;
        /**
         * Amount by which to increase the capacity.
         */
        private final int capacityIncrement;

        public SparseDoubleArray1D(int size) {
            this(size, 0.0, 1);
        }
        public SparseDoubleArray1D(final int dim, double zeroTol, int capacityIncrement) {
            this.size = dim;
                vector=new double[0];
                pos=new int[0];
                this.zeroTol = zeroTol;
            this.capacityIncrement = capacityIncrement;
        }
        public SparseDoubleArray1D(double[] array) {
            this(array, 0.0, 1);
        }
        /**
        * Constructs a vector from an array.
        */
        public SparseDoubleArray1D(double[] array, double zeroTol, int capacityIncrement) {
            this.size = array.length;
                int n=0;
                for(int i=0;i<size;i++) {
                        if(Math.abs(array[i]) > zeroTol)
                                n++;
                }
                count = n;
                vector=new double[n];
                pos=new int[n];
                n=0;
                for(int i=0;i<size ;i++) {
                        if(Math.abs(array[i]) > zeroTol) {
                                vector[n]=array[i];
                                pos[n]=i;
                                n++;
                        }
                }
                this.zeroTol = zeroTol;
                this.capacityIncrement = capacityIncrement;
        }

    @Override
    public SparseDoubleArray1D create(int size) {
        return new SparseDoubleArray1D(size, zeroTol, capacityIncrement);
    }

    public double getDouble(int n) {
            int k = 0;
            while(k<count && pos[k]<n) {
                k++;
            }
            if(k<count && pos[k]==n)
                return vector[k];
            else
                return 0.0;
    }

    public void setDouble(int n, double x) {
            int k = 0;
            while(k<count && pos[k]<n) {
                k++;
            }
            if(k<count && pos[k]==n) {
                if(Math.abs(x) <= zeroTol) {
                    shrink(k);
                } else {
                    // overwrite
                    vector[k] = x;
                }
            } else if(Math.abs(x) > zeroTol) {
                expand(k);
                vector[k] = x;
                pos[k] = n;
            }
    }

    private void shrink(int k) {
        int start = k+1;
        int len = count-start;
        System.arraycopy(vector,start,vector,k,len);
        System.arraycopy(pos,start,pos,k,len);
        count--;
    }
    private void expand(int k) {
        if(count == vector.length) {
            // increase capacity
            double oldVector[] = vector;
            int oldPos[] = pos;
            vector=new double[oldVector.length+capacityIncrement];
            pos=new int[oldPos.length+capacityIncrement];
            System.arraycopy(oldVector,0,vector,0,k);
            System.arraycopy(oldPos,0,pos,0,k);
            System.arraycopy(oldVector,k,vector,k+1,oldVector.length-k);
            System.arraycopy(oldPos,k,pos,k+1,oldPos.length-k);
        } else {
            System.arraycopy(vector,k,vector,k+1,count-k);
            System.arraycopy(pos,k,pos,k+1,count-k);
        }
        count++;
    }

    public int size() {
        return size;
    }

    public int getElementCount() {
        return count;
    }
}

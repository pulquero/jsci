package jsci.util.array;

/**
 *
 * @author Mark
 */
public class SparseDoubleArray2D extends AbstractDoubleArray2D {
    protected final int numRows;
    protected final int numCols;
    protected final double zeroTol;
        /**
        * Matrix elements.
        */
        protected double elements[];
        /**
        * Sparse indexing data.
        * Contains the column positions of each element,
        * e.g. <code>colPos[n]</code> is the column position
        * of the <code>n</code>th element.
        */
        protected int colPos[];
        /**
        * Sparse indexing data.
        * Contains the indices of the start of each row,
        * e.g. <code>rows[i]</code> is the index
        * where the <code>i</code>th row starts.
        */
        protected final int rows[];
        /**
         * Amount by which to increase the capacity.
         */
        protected final int capacityIncrement;

        public SparseDoubleArray2D(final int rowCount, final int colCount) {
            this(rowCount, colCount, 0.0, 1);
        }
        public SparseDoubleArray2D(final int rowCount, final int colCount, double zeroTol, int capacityIncrement) {
            numRows = rowCount;
            numCols = colCount;
                elements=new double[0];
                colPos=new int[0];
                rows=new int[numRows+1];
                this.zeroTol = zeroTol;
            this.capacityIncrement = capacityIncrement;
        }
        public SparseDoubleArray2D(double[][] array) {
            this(array, 0.0, 1);
        }
        public SparseDoubleArray2D(final double[][] array, double zeroTol, int capacityIncrement) {
            numRows = array.length;
            numCols = array[0].length;
                rows=new int[numRows+1];
                int n=0;
                for(int i=0;i<numRows;i++) {
                        for(int j=0;j<numCols;j++) {
                                if(Math.abs(array[i][j])>zeroTol)
                                        n++;
                        }
                }
                elements=new double[n];
                colPos=new int[n];
                n=0;
                for(int i=0;i<numRows;i++) {
                        rows[i]=n;
                        for(int j=0;j<numCols;j++) {
                                if(Math.abs(array[i][j])>zeroTol) {
                                        elements[n]=array[i][j];
                                        colPos[n]=j;
                                        n++;
                                }
                        }
                }
                rows[numRows]=n;
                this.zeroTol = zeroTol;
            this.capacityIncrement = capacityIncrement;
        }

        @Override
    public SparseDoubleArray2D create(int rows, int cols) {
        return new SparseDoubleArray2D(rows, cols, zeroTol, capacityIncrement);
    }

    public double getDouble(int i, int j) {
        int p = rows[i];
        int endOfRow = rows[i+1];
        while(p<endOfRow && colPos[p]<j) {
            p++;
        }
        if(p<endOfRow && colPos[p]==j)
            return elements[p];
        else
            return 0.0;
    }

    public void setDouble(int i, int j, double x) {
        int p = rows[i];
        int endOfRow = rows[i+1];
        while(p<endOfRow && colPos[p]<j) {
            p++;
        }
        if(p<endOfRow && colPos[p] == j) {
             if(Math.abs(x) <= zeroTol) {
                shrink(i, p, 1);
            } else {
                // overwrite
                elements[p]=x;
            }
        } else if(Math.abs(x) > zeroTol) {
            expand(i, p, 1);
            elements[p]=x;
            colPos[p]=j;
        }
    }

    public void addTo(int i, int j, double delta) {
        int p = rows[i];
        int endOfRow = rows[i+1];
        while(p<endOfRow && colPos[p]<j) {
            p++;
        }
        if(p<endOfRow && colPos[p] == j) {
            double newValue = elements[p] + delta;
            if(Math.abs(newValue) <= zeroTol) {
                shrink(i, p, 1);
            } else {
                // overwrite
                elements[p]=newValue;
            }
        } else if(Math.abs(delta) > zeroTol) {
            expand(i, p, 1);
            elements[p]=delta;
            colPos[p]=j;
        }
    }

    /**
     * Removes elements.
     * @param i row
     * @param p deletion index
     * @param numElems number of elements to delete
     */
    protected void shrink(int i, int p, int numElems) {
        int start = p+numElems;
        int len = rows[numRows] - start;
        System.arraycopy(elements, start,elements,p, len);
        System.arraycopy(colPos, start,colPos,p, len);
        for(int k=i+1;k<rows.length;k++) {
            rows[k]-=numElems;
        }
    }
    /**
     * Inserts elements.
     * Expands the storage if necessary.
     * @param i row
     * @param p insertion index
     * @param numElems number of elements to insert
     */
    protected void expand(int i, int p, int numElems) {
        int count = rows[numRows];
        if(count+numElems > elements.length) {
            // increase capacity
            final double oldMatrix[]=elements;
            final int oldColPos[]=colPos;
            int diff = count+numElems-elements.length;
            int r = diff/capacityIncrement;
            if(diff % capacityIncrement > 0)
                r++;
            int newLen = oldMatrix.length+r*capacityIncrement;
            elements=new double[newLen];
            colPos=new int[newLen];
            System.arraycopy(oldMatrix,0,elements,0,p);
            System.arraycopy(oldColPos,0,colPos,0,p);
            System.arraycopy(oldMatrix,p,elements,p+numElems,count-p);
            System.arraycopy(oldColPos,p,colPos,p+numElems,count-p);
        } else {
            System.arraycopy(elements,p,elements,p+numElems,count-p);
            System.arraycopy(colPos,p,colPos,p+numElems,count-p);
        }
        for(int k=i+1;k<rows.length;k++) {
            rows[k]+=numElems;
        }
    }

    public double[] getSubRow(int row, int startCol, int endCol) {
        double[] arr = new double[endCol-startCol];
        int p=rows[row];
        int endOfRow = rows[row+1];
        while(p<endOfRow && colPos[p]<startCol) {
            p++;
        }
        if(p<endOfRow && colPos[p] >= startCol) {
            while(p<endOfRow && colPos[p]<endCol) {
                arr[colPos[p]-startCol] = elements[p];
                p++;
            }
        }
        return arr;
    }

    public void setSubRow(int row, int startCol, double[] arr) {
        int p=rows[row];
        int endOfRow = rows[row+1];
        while(p<endOfRow && colPos[p]<startCol) {
            p++;
        }
        int startIndex = p;
        int oldCount = 0;
        if(p<endOfRow && colPos[p] >= startCol) {
            int endCol = startCol + arr.length;
            while(p<endOfRow && colPos[p]<endCol) {
                oldCount++;
                p++;
            }
        }

        int newCount = 0;
        for(int i=0; i<arr.length; i++) {
             if(Math.abs(arr[i]) > zeroTol)
                newCount++;
        }

        int diff = newCount-oldCount;
        if(diff > 0)
            expand(row, startIndex, newCount-oldCount);
        else if(diff < 0)
            shrink(row, startIndex, -diff);

        p = startIndex;
        for(int i=0; i<arr.length; i++) {
            if(Math.abs(arr[i]) > zeroTol) {
                elements[p] = arr[i];
                colPos[p] = startCol+i;
                p++;
            }
        }
    }

    public int rows() {
        return numRows;
    }

    public int columns() {
        return numCols;
    }

    public int getElementCount() {
        return rows[numRows];
    }
}

package jsci.maths.matrix.impl;

import jsci.maths.MaximumIterationsExceededException;
import jsci.maths.linalg.EigenDecomposition;
import jsci.maths.vector.impl.AlgorithmsDoubleArray1D;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.SparseDoubleArray2D;

/**
 *
 * @author Mark
 */
public class AlgorithmsSparseDoubleArray2D extends SparseDoubleArray2D implements AlgorithmsDoubleArray2D, EigenDecomposition.Algorithms {
    public AlgorithmsSparseDoubleArray2D(int rows, int cols) {
        super(rows, cols);
    }
    public AlgorithmsSparseDoubleArray2D(final int rowCount, final int colCount, double zeroTol, int capacityIncrement) {
        super(rowCount, colCount, zeroTol, capacityIncrement);
    }
    public AlgorithmsSparseDoubleArray2D(double[][] array) {
        super(array);
    }
    public AlgorithmsSparseDoubleArray2D(final double[][] array, double zeroTol, int capacityIncrement) {
        super(array, zeroTol, capacityIncrement);
    }

    @Override
    public AlgorithmsSparseDoubleArray2D create(int rows, int cols) {
        return new AlgorithmsSparseDoubleArray2D(rows, cols);
    }

    public Double trace(AlgorithmsDoubleArray2D a) {
        return a.trace();
    }
    public double trace() {
        int n = Math.min(rows(), columns());
        double tr = 0.0;
        for(int i=0; i<n; i++) {
            tr += getDouble(i,i);
        }
        return tr;
    }

    public AlgorithmsDoubleArray2D transpose(AlgorithmsDoubleArray2D a) {
        return a.transpose();
    }

    public AlgorithmsTransposedDoubleArray2D<AlgorithmsSparseDoubleArray2D> transpose() {
        return new AlgorithmsTransposedDoubleArray2D<AlgorithmsSparseDoubleArray2D>(this);
    }

    @Override
    public AlgorithmsDoubleArray2D add(AlgorithmsDoubleArray2D a, AlgorithmsDoubleArray2D b) {
        return a.add(b);
    }

    @Override
    public AlgorithmsDoubleArray2D add(AlgorithmsDoubleArray2D b) {
        AlgorithmsDoubleArray2D arr = b.create(rows(), columns());
        for(int i=0; i<arr.rows(); i++) {
            for(int j=0; j<columns(); j++) {
                arr.setDouble(i, j, b.getDouble(i, j));
            }
            for(int j=rows[i]; j<rows[i+1]; j++) {
                arr.setDouble(i, colPos[j], elements[j] + arr.getDouble(i, colPos[j]));
            }
        }
        return arr;
    }

    @Override
    public AlgorithmsDenseDoubleArray2D addOp(AlgorithmsDenseDoubleArray2D a) {
        double[][] arr = new double[rows()][columns()];
        a.copyTo(arr);
        for(int i=0; i<arr.length; i++) {
            for(int j=rows[i]; j<rows[i+1]; j++) {
                arr[i][colPos[j]] += elements[j];
            }
        }
        return new AlgorithmsDenseDoubleArray2D(arr);
    }

    @Override
    public AlgorithmsDoubleArray2D subtract(AlgorithmsDoubleArray2D a, AlgorithmsDoubleArray2D b) {
        return a.subtract(b);
    }

    @Override
    public AlgorithmsDoubleArray2D subtract(AlgorithmsDoubleArray2D b) {
        AlgorithmsDoubleArray2D arr = b.create(rows(), columns());
        for(int i=0; i<arr.rows(); i++) {
            for(int j=0; j<columns(); j++) {
                arr.setDouble(i, j, -b.getDouble(i, j));
            }
            for(int j=rows[i]; j<rows[i+1]; j++) {
                arr.setDouble(i, colPos[j], elements[j] + arr.getDouble(i, colPos[j]));
            }
        }
        return arr;
    }

    @Override
    public AlgorithmsDenseDoubleArray2D subtractOp(AlgorithmsDenseDoubleArray2D a) {
        double[][] arr = new double[rows()][columns()];
        a.copyTo(arr);
        for(int i=0; i<arr.length; i++) {
            for(int j=rows[i]; j<rows[i+1]; j++) {
                arr[i][colPos[j]] -= elements[j];
            }
        }
        return new AlgorithmsDenseDoubleArray2D(arr);
    }

    @Override
    public AlgorithmsDoubleArray2D multiply(AlgorithmsDoubleArray2D a, AlgorithmsDoubleArray2D b) {
        return a.multiply(b);
    }

    @Override
    public AlgorithmsDoubleArray2D multiply(AlgorithmsDoubleArray2D b) {
        AlgorithmsDoubleArray2D arr = b.create(rows(), b.columns());
        for(int i=0; i<arr.rows(); i++) {
            for(int j=0; j<arr.columns(); j++) {
                double tmp = 0.0;
                for(int k=rows[i]; k<rows[i+1]; k++) {
                    tmp += elements[k]*b.getDouble(colPos[k], j);
                }
                arr.setDouble(i, j, tmp);
            }
        }
        return arr;
    }

    public AlgorithmsDenseDoubleArray2D multiplyOp(AlgorithmsDenseDoubleArray2D a) {
        double[][] arr = new double[a.rows()][columns()];
        for(int i=0; i<arr.length; i++) {
            for(int j=0; j<arr[0].length; j++) {
                double tmp = 0.0;
                for(int k=rows[i]; k<rows[i+1]; k++) {
                    tmp += a.getDouble(j, colPos[k]) * elements[k];
                }
                arr[i][j] = tmp;
            }
        }

        for(int i=0; i<a.rows(); i++) {
            for(int j=0; j<columns(); j++) {
                double tmp = 0.0;
                for(int k=0; k<a.columns(); k++) {
                    tmp += a.getDouble(i, k)*getDouble(k, j);
                }
                arr[i][j] = tmp;
            }
        }

        return new AlgorithmsDenseDoubleArray2D(arr);
    }

    public AlgorithmsDoubleArray1D act(AlgorithmsDoubleArray2D a, AlgorithmsDoubleArray1D v) {
        return a.act(v);
    }
    public AlgorithmsDoubleArray1D act(AlgorithmsDoubleArray1D v) {
        AlgorithmsDoubleArray1D arr = v.create(v.size());
        for(int i=0;i<numRows;i++) {
                double tmp = 0.0;
                for(int j=rows[i];j<rows[i+1];j++) {
                        tmp += elements[j]*v.getDouble(colPos[j]);
                }
                arr.setDouble(i, tmp);
        }
        return arr;
    }

    @Override
    public AlgorithmsDoubleArray2D scalarMultiply(AlgorithmsDoubleArray2D a, Double x) {
        return a.scalarMultiply(x);
    }

    @Override
    public AlgorithmsSparseDoubleArray2D scalarMultiply(double x) {
        AlgorithmsSparseDoubleArray2D arr = new AlgorithmsSparseDoubleArray2D(rows(), columns());
        arr.elements = new double[elements.length];
        arr.colPos = new int[colPos.length];
        System.arraycopy(colPos,0, arr.colPos,0,colPos.length);
        System.arraycopy(rows,0, arr.rows,0,rows.length);
        for(int i=0; i<rows[numRows]; i++) {
            arr.elements[i] = elements[i] * x;
        }
        return arr;
    }

    public void reductionIteration(int i, DoubleArray1D offdiag) {
                final int l=i-1;
                if(l>0) {
                        double scale=0.0;
                        int k;
                        for(k=rows[i]; k<rows[i+1] && colPos[k]<=l; k++) {
                                scale+=Math.abs(elements[k]);
                        }
                        if(scale==0.0) {
                                double tmp = 0.0;
                                if(colPos[--k] == l)
                                    tmp = elements[k];
                                offdiag.setDouble(i, tmp);
                        } else {
                                double h=0.0;
                                for(k=rows[i]; k<rows[i+1] && colPos[k]<=l; k++) {
                                        double tmp = elements[k]/scale;
                                        elements[k] = tmp;
                                        h+=tmp*tmp;
                                }
                                double f = 0.0;
                                if(colPos[--k] == l)
                                    f = elements[k];
                                double g=(f>=0.0?-Math.sqrt(h):Math.sqrt(h));
                                offdiag.setDouble(i, scale*g);
                                h-=f*g;
                                setDouble(i, l, f-g);
                                f=0.0;
                                for(int j=0;j<=l;j++) {
                                        g=0.0;
                                        for(k=rows[i]; k<rows[i+1] && colPos[k]<=j; k++)
                                                g+=getDouble(j, colPos[k])*elements[k];
                                        double arrayij = 0.0;
                                        if(colPos[k-1] == j)
                                            arrayij = elements[k-1];
                                        for(; k<rows[i+1] && colPos[k]<=l; k++)
                                                g+=getDouble(colPos[k], j)*elements[k];
                                        final double tmp = g/h;
                                        offdiag.setDouble(j, tmp);
                                        f+=tmp*arrayij;
                                }
                                final double hh=f/(h+h);
                                for(int j=0;j<=l;j++) {
                                        f=getDouble(i, j);
                                        g=offdiag.getDouble(j)-hh*f;
                                        offdiag.setDouble(j, g);
                                        double[] subrow = getSubRow(j, 0, j+1);
                                        for(k=0;k<=j;k++)
                                                subrow[k] -= f*offdiag.getDouble(k)+g*getDouble(i, k);
                                        setSubRow(j, 0, subrow);
                                }
                        }
                } else {
                        offdiag.setDouble(i, getDouble(i, l));
                }
                shrinkRowToColumn(i, i);
    }

    private void shrinkRowToColumn(int row, int col) {
        int startOfRow = rows[row];
        int endOfRow = rows[row+1];
        int p=startOfRow;
        while(p<endOfRow && colPos[p]<col) {
            p++;
        }
        if(p<endOfRow && colPos[p]==col) {
            double value = elements[p];
            shrink(row, startOfRow+1, endOfRow-startOfRow-1);
            elements[startOfRow] = value;
            colPos[startOfRow] = col;
        } else {
            shrink(row, startOfRow, endOfRow-startOfRow);
        }
    }

    public void eigenvalueIteration(int l, DoubleArray1D offdiag, int maxIters) {
                final int nm1 = numRows-1;
                int iteration=0;
                int m;
                do {
                        for(m=l;m<nm1;m++) {
                                double dd=Math.abs(getDouble(m,m))+Math.abs(getDouble(m+1, m+1));
                                if(Math.abs(offdiag.getDouble(m))+dd==dd)
                                        break;
                        }
                        if(m!=l) {
                                if(iteration++ == maxIters)
                                        throw new MaximumIterationsExceededException("No convergence after "+maxIters+" iterations.", new Object[] {this, offdiag});
                                final double arrayll = getDouble(l, l);
                                final double offdiagl = offdiag.getDouble(l);
                                double g=(getDouble(l+1,l+1) - arrayll)/(2.0*offdiagl);
                                double r=Math.sqrt(g*g+1.0);
                                g=getDouble(m,m) - arrayll +offdiagl/(g+(g<0.0?-Math.abs(r):Math.abs(r)));
                                double s=1.0;
                                double c=1.0;
                                double p=0.0;
                                for(int i=m-1;i>=l;i--) {
                                        final double offdiagi = offdiag.getDouble(i);
                                        double f=s*offdiagi;
                                        double b=c*offdiagi;
                                        if(Math.abs(f)>=Math.abs(g)) {
                                                c=g/f;
                                                r=Math.sqrt(c*c+1.0);
                                                offdiag.setDouble(i+1, f*r);
                                                s=1.0/r;
                                                c*=s;
                                        } else {
                                                s=f/g;
                                                r=Math.sqrt(s*s+1.0);
                                                offdiag.setDouble(i+1, g*r);
                                                c=1.0/r;
                                                s*=c;
                                        }
                                        g=getDouble(i+1, i+1)-p;
                                        r=(getDouble(i,i)-g)*s+2.0*c*b;
                                        p=s*r;
                                        setDouble(i+1, i+1, g+p);
                                        g=c*r-b;
                                }
                                addTo(l,l, -p);
                                offdiag.setDouble(l, g);
                                offdiag.setDouble(m, 0.0);
                        }
                } while(m!=l);
    }
}

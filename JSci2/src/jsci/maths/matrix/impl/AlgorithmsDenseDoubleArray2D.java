package jsci.maths.matrix.impl;

import jsci.maths.MaximumIterationsExceededException;
import jsci.maths.linalg.EigenDecomposition;
import jsci.maths.vector.impl.AlgorithmsDoubleArray1D;
import jsci.util.array.DenseDoubleArray2D;
import jsci.util.array.DoubleArray1D;

/**
 *
 * @author Mark
 */
public class AlgorithmsDenseDoubleArray2D extends DenseDoubleArray2D implements AlgorithmsDoubleArray2D, EigenDecomposition.Algorithms {
    public AlgorithmsDenseDoubleArray2D(int rows, int cols) {
        super(rows, cols);
    }
    public AlgorithmsDenseDoubleArray2D(double[][] array) {
        super(array);
    }

    @Override
    public AlgorithmsDenseDoubleArray2D create(int rows, int cols) {
        return new AlgorithmsDenseDoubleArray2D(rows, cols);
    }

    public void copyTo(double[][] arr) {
        for(int i=0; i<array.length; i++) {
            System.arraycopy(this.array[i], 0, arr[i], 0, this.array[i].length);
        }
    }

    public Double trace(AlgorithmsDoubleArray2D a) {
        return a.trace();
    }
    public double trace() {
        int n = Math.min(rows(), columns());
        double tr = 0.0;
        for(int i=0; i<n; i++) {
            tr += array[i][i];
        }
        return tr;
    }

    public AlgorithmsDoubleArray2D transpose(AlgorithmsDoubleArray2D a) {
        return a.transpose();
    }

    public AlgorithmsTransposedDoubleArray2D transpose() {
        return new AlgorithmsTransposedDoubleArray2D(this);
    }

    @Override
    public AlgorithmsDoubleArray2D add(AlgorithmsDoubleArray2D a, AlgorithmsDoubleArray2D b) {
        return a.add(b);
    }

    @Override
    public AlgorithmsDoubleArray2D add(AlgorithmsDoubleArray2D b) {
        return b.addOp(this);
    }

    @Override
    public AlgorithmsDenseDoubleArray2D addOp(AlgorithmsDenseDoubleArray2D a) {
        double[][] arr = new double[rows()][columns()];
        for(int i=0; i<arr.length; i++) {
            for(int j=0; j<arr[0].length; j++) {
                arr[i][j] = a.array[i][j] + this.array[i][j];
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
        return b.subtractOp(this);
    }

    @Override
    public AlgorithmsDenseDoubleArray2D subtractOp(AlgorithmsDenseDoubleArray2D a) {
        double[][] arr = new double[rows()][columns()];
        for(int i=0; i<arr.length; i++) {
            for(int j=0; j<arr[0].length; j++) {
                arr[i][j] = a.array[i][j] - this.array[i][j];
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
        return b.multiplyOp(this);
    }

    public AlgorithmsDenseDoubleArray2D multiplyOp(AlgorithmsDenseDoubleArray2D a) {
        double[][] arr = new double[a.rows()][columns()];
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
        return v.actOp(this);
    }

    @Override
    public AlgorithmsDoubleArray2D scalarMultiply(AlgorithmsDoubleArray2D a, Double x) {
        return a.scalarMultiply(x);
    }

    @Override
    public AlgorithmsDenseDoubleArray2D scalarMultiply(double x) {
        double[][] arr = new double[rows()][columns()];
        for(int i=0; i<arr.length; i++) {
            for(int j=0; j<arr[0].length; j++) {
                arr[i][j] = array[i][j] * x;
            }
        }
        return new AlgorithmsDenseDoubleArray2D(arr);
    }

    public void reductionIteration(int i, DoubleArray1D offdiag) {
                final int l=i-1;
                if(l>0) {
                        double scale=0.0;
                        for(int k=0;k<=l;k++) {
                                scale+=Math.abs(array[i][k]);
                        }
                        if(scale==0.0) {
                                offdiag.setDouble(i, array[i][l]);
                        } else {
                                double h=0.0;
                                for(int k=0;k<=l;k++) {
                                        double tmp = array[i][k]/scale;
                                        array[i][k] = tmp;
                                        h+=tmp*tmp;
                                }
                                double f=array[i][l];
                                double g=(f>=0.0?-Math.sqrt(h):Math.sqrt(h));
                                offdiag.setDouble(i, scale*g);
                                h-=f*g;
                                array[i][l] = f-g;
                                f=0.0;
                                for(int j=0;j<=l;j++) {
                                        g=0.0;
                                        for(int k=0;k<=j;k++)
                                                g+=array[j][k]*array[i][k];
                                        for(int k=j+1;k<=l;k++)
                                                g+=array[k][j]*array[i][k];
                                        final double tmp = g/h;
                                        offdiag.setDouble(j, tmp);
                                        f+=tmp*array[i][j];
                                }
                                final double hh=f/(h+h);
                                for(int j=0;j<=l;j++) {
                                        f=array[i][j];
                                        g=offdiag.getDouble(j)-hh*f;
                                        offdiag.setDouble(j, g);
                                        for(int k=0;k<=j;k++)
                                                array[j][k] -= f*offdiag.getDouble(k) + g*array[i][k];
                                }
                        }
                } else {
                        offdiag.setDouble(i, array[i][l]);
                }
    }

    public void eigenvalueIteration(int l, DoubleArray1D offdiag, int maxIters) {
                final int nm1 = array.length-1;
                int iteration=0;
                int m;
                do {
                        for(m=l;m<nm1;m++) {
                                double dd=Math.abs(array[m][m])+Math.abs(array[m+1][m+1]);
                                if(Math.abs(offdiag.getDouble(m))+dd==dd)
                                        break;
                        }
                        if(m!=l) {
                                if(iteration++ == maxIters)
                                        throw new MaximumIterationsExceededException("No convergence after "+maxIters+" iterations.", new Object[] {this, offdiag});
                                final double offdiagl = offdiag.getDouble(l);
                                double g=(array[l+1][l+1] - array[l][l])/(2.0*offdiagl);
                                double r=Math.sqrt(g*g+1.0);
                                g=array[m][m] - array[l][l] +offdiagl/(g+(g<0.0?-Math.abs(r):Math.abs(r)));
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
                                        g=array[i+1][i+1]-p;
                                        r=(array[i][i]-g)*s+2.0*c*b;
                                        p=s*r;
                                        array[i+1][i+1] = g+p;
                                        g=c*r-b;
                                }
                                array[l][l] -= p;
                                offdiag.setDouble(l, g);
                                offdiag.setDouble(m, 0.0);
                        }
                } while(m!=l);
    }
}

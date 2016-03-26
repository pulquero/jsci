package jsci.maths.linalg;

import jsci.maths.matrix.DoubleMatrix;
import jsci.maths.matrix.impl.AlgorithmsDenseDoubleArray2D;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.DoubleArray2D;

/**
 *
 * @author Mark
 */
public class LUDecomposition {
    private int[] pivot;
    private DoubleMatrix<DoubleArray2D,DoubleArray1D> L;
    private DoubleMatrix<DoubleArray2D,DoubleArray1D> U;

    public LUDecomposition(DoubleMatrix<? extends DoubleArray2D,? extends DoubleArray1D> matrix) {
        luDecompose(matrix);
    }

    /*
    * Returns an array of length <code>rows()+1</code> containing the pivot information.
    * The last array element will contain the parity.
     */
    public int[] getPivot() {
        return pivot;
    }
    public DoubleMatrix<DoubleArray2D,DoubleArray1D> getL() {
        return L;
    }
    public DoubleMatrix<DoubleArray2D,DoubleArray1D> getU() {
        return U;
    }
    public double det() {
        int n = U.rows();
        double det = 1.0;
        for(int i=0; i<n; i++)
                det *= U.getDouble(i,i);
        return det*pivot[n];
    }

        /**
        * Returns the LU decomposition of this matrix.
        * @return an array with [0] containing the L-matrix
        * and [1] containing the U-matrix.
        * @jsci.planetmath LUDecomposition
        */
        private void luDecompose(DoubleMatrix<? extends DoubleArray2D,? extends DoubleArray1D> matrix) {
                final int N=matrix.rows();
                final double arrayU[][]=new double[N][N];
                pivot=new int[N+1];
                for(int i=0;i<N;i++) {
                        pivot[i]=i;
                }
                pivot[N]=1;
        // LU decomposition to arrayU
                for(int j=0;j<N;j++) {
                        for(int i=0;i<j;i++) {
                                double tmp= matrix.getDouble(pivot[i],j);
                                for(int k=0;k<i;k++)
                                        tmp-=arrayU[i][k]*arrayU[k][j];
                                arrayU[i][j]=tmp;
                        }
                        double max=0.0;
                        int pivotrow=j;
                        for(int i=j;i<N;i++) {
                                double tmp= matrix.getDouble(pivot[i],j);
                                for(int k=0;k<j;k++)
                                        tmp-=arrayU[i][k]*arrayU[k][j];
                                arrayU[i][j]=tmp;
                        // while we're here search for a pivot for arrayU[j][j]
                                tmp=Math.abs(tmp);
                                if(tmp>max) {
                                        max=tmp;
                                        pivotrow=i;
                                }
                        }
                // swap row j with pivotrow
                        if(pivotrow!=j) {
                                double[] tmprow = arrayU[j];
                                arrayU[j] = arrayU[pivotrow];
                                arrayU[pivotrow] = tmprow;
                                int k=pivot[j];
                                pivot[j]=pivot[pivotrow];
                                pivot[pivotrow]=k;
                                // update parity
                                pivot[N]=-pivot[N];
                        }
                // divide by pivot
                        double tmp=arrayU[j][j];
                        for(int i=j+1;i<N;i++)
                                arrayU[i][j]/=tmp;
                }
                // move lower triangular part to arrayL
                final double arrayL[][]=new double[N][N];
                for(int j=0;j<N;j++) {
                        arrayL[j][j]=1.0;
                        for(int i=j+1;i<N;i++) {
                                arrayL[i][j]=arrayU[i][j];
                                arrayU[i][j]=0.0;
                        }
                }
                L=DoubleMatrix.create(new AlgorithmsDenseDoubleArray2D(arrayL));
                U=DoubleMatrix.create(new AlgorithmsDenseDoubleArray2D(arrayU));
        }

        public DoubleMatrix inverse() {
                final int N=L.rows();
                final double arrayL[][]=new double[N][N];
                final double arrayU[][]=new double[N][N];
                arrayL[0][0]=1.0/L.getDouble(0,0);
                arrayU[0][0]=1.0/U.getDouble(0,0);
                for(int i=1;i<N;i++) {
                        arrayL[i][i]=1.0/L.getDouble(i,i);
                        arrayU[i][i]=1.0/U.getDouble(i,i);
                }
                for(int i=0;i<N-1;i++) {
                        for(int j=i+1;j<N;j++) {
                                double tmpL=0.0, tmpU=0.0;
                                for(int k=i;k<j;k++) {
                                        tmpL-=L.getDouble(j,k)*arrayL[k][i];
                                        tmpU-=arrayU[i][k]*U.getDouble(k,j);
                                }
                                arrayL[j][i]=tmpL/L.getDouble(j,j);
                                arrayU[i][j]=tmpU/U.getDouble(j,j);
                        }
                }
                // matrix multiply arrayU x arrayL
                final double inv[][]=new double[N][N];
                for(int i=0;i<N;i++) {
                        for(int j=0;j<i;j++) {
                                for(int k=i;k<N;k++)
                                        inv[i][pivot[j]]+=arrayU[i][k]*arrayL[k][j];
                        }
                        for(int j=i;j<N;j++) {
                                for(int k=j;k<N;k++)
                                        inv[i][pivot[j]]+=arrayU[i][k]*arrayL[k][j];
                        }
                }
                return DoubleMatrix.create(new AlgorithmsDenseDoubleArray2D(inv));
        }
}

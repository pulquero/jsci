package jsci.maths.linalg;

import jsci.maths.matrix.DoubleMatrix;
import jsci.maths.matrix.impl.AlgorithmsDenseDoubleArray2D;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.DoubleArray2D;

/**
 *
 * @author Mark
 */
public class CholeskyDecomposition {
    private DoubleMatrix<DoubleArray2D,DoubleArray1D> L;
    private DoubleMatrix<DoubleArray2D,DoubleArray1D> U;

    public CholeskyDecomposition(DoubleMatrix<? extends DoubleArray2D,? extends DoubleArray1D> matrix) {
        choleskyDecompose(matrix);
    }

    public DoubleMatrix<DoubleArray2D,DoubleArray1D> getL() {
        return L;
    }
    public DoubleMatrix<DoubleArray2D,DoubleArray1D> getU() {
        if(U == null)
            U = L.transpose();
        return U;
    }
    public double det() {
        int n = L.rows();
        double det = 1.0;
        for(int i=0; i<n; i++)
                det *= L.getDouble(i,i);
        return det*det;
    }

        /**
        * Returns the Cholesky decomposition of this matrix.
        * Matrix must be symmetric and positive definite.
        * @return an array with [0] containing the L-matrix and [1] containing the U-matrix.
        * @jsci.planetmath CholeskyDecomposition
        */
        private void choleskyDecompose(DoubleMatrix<? extends DoubleArray2D,? extends DoubleArray1D> matrix) {
                final int N=matrix.rows();
                final double arrayL[][]=new double[N][N];
                double tmp=Math.sqrt(matrix.getDouble(0,0));
                arrayL[0][0]=tmp;
                for(int i=1;i<N;i++)
                        arrayL[i][0]=matrix.getDouble(i,0)/tmp;
                for(int j=1;j<N;j++) {
                        tmp=matrix.getDouble(j,j);
                        for(int i=0;i<j;i++)
                                tmp-=arrayL[j][i]*arrayL[j][i];
                        arrayL[j][j]=Math.sqrt(tmp);
                        for(int i=j+1;i<N;i++) {
                                tmp=matrix.getDouble(i,j);
                                for(int k=0;k<i;k++)
                                        tmp-=arrayL[j][k]*arrayL[i][k];
                                arrayL[i][j]=tmp/arrayL[j][j];
                        }
                }
                L = DoubleMatrix.create(new AlgorithmsDenseDoubleArray2D(arrayL));
        }


        public DoubleMatrix inverse() {
                final int N=L.rows();
                final double arrayL[][]=new double[N][N];
                arrayL[0][0]=1.0/L.getDouble(0,0);
                for(int i=1;i<N;i++) {
                        arrayL[i][i]=1.0/L.getDouble(i,i);
                }
                for(int i=0;i<N-1;i++) {
                        for(int j=i+1;j<N;j++) {
                                double tmpL=0.0, tmpU=0.0;
                                for(int k=i;k<j;k++) {
                                        tmpL-=L.getDouble(j,k)*arrayL[k][i];
                                }
                                arrayL[j][i]=tmpL/L.getDouble(j,j);
                        }
                }
                // matrix multiply arrayU x arrayL
                final double inv[][]=new double[N][N];
                for(int i=0;i<N;i++) {
                        for(int j=0;j<N;j++) {
                                for(int k=i;k<N;k++)
                                        inv[i][j]+=arrayL[k][i]*arrayL[k][j];
                        }
                }
                return DoubleMatrix.create(new AlgorithmsDenseDoubleArray2D(inv));
        }
}

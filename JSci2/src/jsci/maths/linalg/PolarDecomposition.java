package jsci.maths.linalg;

import jsci.maths.matrix.DoubleMatrix;
import jsci.maths.matrix.impl.AlgorithmsDenseDoubleArray2D;
import jsci.maths.vector.DoubleVector;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.DoubleArray2D;

/**
 *
 * @author Mark
 */
public class PolarDecomposition {
    private DoubleMatrix<DoubleArray2D,DoubleArray1D> U;
    private DoubleMatrix<DoubleArray2D,DoubleArray1D> P;

    public PolarDecomposition(DoubleMatrix<? extends DoubleArray2D,? extends DoubleArray1D> matrix) {
        polarDecompose(matrix);
    }

    public DoubleMatrix<DoubleArray2D,DoubleArray1D> getU() {
        return U;
    }
    public DoubleMatrix<DoubleArray2D,DoubleArray1D> getP() {
        return P;
    }

        /**
        * Returns the polar decomposition of this matrix.
        */
        private void polarDecompose(DoubleMatrix<? extends DoubleArray2D,? extends DoubleArray1D> matrix) {
                final int N=matrix.rows();
                EigenDecomposition decomp = new EigenDecomposition(matrix, true);
                final DoubleVector evec[]=decomp.getEigenvectors();
                double eval[] = decomp.getEigenvalues();
                final double tmpa[][]=new double[N][N];
                final double tmpm[][]=new double[N][N];
                double abs;
                for(int i=0;i<N;i++) {
                        abs=Math.abs(eval[i]);
                        tmpa[i][0]=eval[i]*evec[i].getDouble(0)/abs;
                        tmpm[i][0]=abs*evec[i].getDouble(0);
                        for(int j=1;j<N;j++) {
                                tmpa[i][j]=eval[i]*evec[i].getDouble(j)/abs;
                                tmpm[i][j]=abs*evec[i].getDouble(j);
                        }
                }
                final double arg[][]=new double[N][N];
                final double mod[][]=new double[N][N];
                for(int i=0;i<N;i++) {
                        for(int j=0;j<N;j++) {
                                arg[i][j]=evec[0].getDouble(i)*tmpa[0][j];
                                mod[i][j]=evec[0].getDouble(i)*tmpm[0][j];
                                for(int k=1;k<N;k++) {
                                        arg[i][j]+=evec[k].getDouble(i)*tmpa[k][j];
                                        mod[i][j]+=evec[k].getDouble(i)*tmpm[k][j];
                                }
                        }
                }
                U = DoubleMatrix.create(new AlgorithmsDenseDoubleArray2D(arg));
                P = DoubleMatrix.create(new AlgorithmsDenseDoubleArray2D(mod));
        }
}

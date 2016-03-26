package jsci.maths.linalg;

import jsci.maths.matrix.DoubleMatrix;
import jsci.maths.matrix.impl.AlgorithmsDenseDoubleArray2D;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.DoubleArray2D;

/**
 *
 * @author Mark
 */
public class QRDecomposition {
    private DoubleMatrix<DoubleArray2D,DoubleArray1D> Q;
    private DoubleMatrix<DoubleArray2D,DoubleArray1D> R;

    public QRDecomposition(DoubleMatrix<? extends DoubleArray2D,? extends DoubleArray1D> matrix) {
        qrDecompose(matrix);
    }

    public DoubleMatrix<DoubleArray2D,DoubleArray1D> getQ() {
        return Q;
    }
    public DoubleMatrix<DoubleArray2D,DoubleArray1D> getR() {
        return R;
    }

        /**
        * Returns the QR decomposition of this matrix.
        * Based on the code from <a href="http://math.nist.gov/javanumerics/jama/">JAMA</a> (public domain).
        * @return an array with [0] containing the Q-matrix and [1] containing the R-matrix.
        * @jsci.planetmath QRDecomposition
        */
        private void qrDecompose(DoubleMatrix<? extends DoubleArray2D,? extends DoubleArray1D> matrix) {
                final int N=matrix.rows();
                final double array[][]=new double[N][N];
                // copy matrix
                for(int i=0;i<N;i++) {
                        array[i][0]=matrix.getDouble(i,0);
                        for(int j=1;j<N;j++)
                                array[i][j]=matrix.getDouble(i,j);
                }

                final double arrayQ[][]=new double[N][N];
                final double arrayR[][]=new double[N][N];
                for(int k=0; k<N; k++) {
                        // compute l2-norm of kth column
                        double normSqr = 0.0;
                        for(int i=k; i<N; i++)
                                normSqr += array[i][k]*array[i][k];
                        double norm = Math.sqrt(normSqr);
                        if(norm != 0.0) {
                                // form kth Householder vector
                                if(array[k][k] < 0.0)
                                        norm = -norm;
                                for(int i=k; i<N; i++)
                                        array[i][k] /= norm;
                                array[k][k] += 1.0;
                                // apply transformation to remaining columns
                                for(int j=k+1; j<N; j++) {
                                        double s=array[k][k]*array[k][j];
                                        for(int i=k+1; i<N; i++)
                                                s += array[i][k]*array[i][j];
                                        s /= array[k][k];
                                        for(int i=k; i<N; i++)
                                                array[i][j] -= s*array[i][k];
                                }
                        }
                        arrayR[k][k] = -norm;
                }
                for(int k=N-1; k>=0; k--) {
                        arrayQ[k][k] = 1.0;
                        for(int j=k; j<N; j++) {
                                if(array[k][k] != 0.0) {
                                        double s = array[k][k]*arrayQ[k][j];
                                        for(int i=k+1; i<N; i++)
                                                s += array[i][k]*arrayQ[i][j];
                                        s /= array[k][k];
                                        for(int i=k; i<N; i++)
                                                arrayQ[i][j] -= s*array[i][k];
                                }
                        }
                }
                for(int i=0; i<N; i++) {
                        for(int j=i+1; j<N; j++)
                                arrayR[i][j] = array[i][j];
                }
                Q = DoubleMatrix.create(new AlgorithmsDenseDoubleArray2D(arrayQ));
                R = DoubleMatrix.create(new AlgorithmsDenseDoubleArray2D(arrayR));
        }
}

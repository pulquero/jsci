package jsci.maths.linalg;

import jsci.maths.MaximumIterationsExceededException;
import jsci.maths.matrix.DoubleMatrix;
import jsci.maths.vector.DoubleVector;
import jsci.util.array.DenseDoubleArray1D;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.DoubleArray2D;

/**
 *
 * @author Mark
 */
public class EigenDecomposition {
    private static final int DEFAULT_MAX_ITERS = 250;

    private final int maxIters;
    private final double[] eigenvalues;
    private DoubleVector[] eigenvectors;

    public EigenDecomposition(DoubleMatrix<? extends DoubleArray2D,? extends DoubleArray1D> matrix, boolean eigenvectors) throws MaximumIterationsExceededException {
        this(matrix, eigenvectors, DEFAULT_MAX_ITERS);
    }
    public EigenDecomposition(DoubleMatrix<? extends DoubleArray2D,? extends DoubleArray1D> matrix, boolean eigenvectors, int maxIters) throws MaximumIterationsExceededException {
        this.maxIters = maxIters;
        if(eigenvectors) {
            eigenvalues = eigenvectorSolveSymmetric(matrix);
        } else {
            eigenvalues = eigenvalueSolveSymmetric(matrix);
        }
    }

    public double[] getEigenvalues() {
        return eigenvalues;
    }
    public DoubleVector[] getEigenvectors() {
        return eigenvectors;
    }

        /**
        * This method finds the eigenvalues of a symmetric square matrix.
        * The matrix is reduced to tridiagonal form and then the QL method is applied.
        * It is based on the NETLIB algol/fortran procedure tred1/tql1 by Bowdler, Martin, Reinsch and Wilkinson.
        * @param matrix a double symmetric square matrix.
        * @return an array containing the eigenvalues.
        * @exception MaximumIterationsExceededException If it takes too many iterations to determine an eigenvalue.
        */
        private double[] eigenvalueSolveSymmetric(final DoubleMatrix matrix) throws MaximumIterationsExceededException {
                final int n=matrix.rows();
                final double eigenvalue[]=new double[n];
                final double offdiag[]=new double[n];
                final double array[][]=new double[n][n];
                for(int i=0;i<n;i++) {
                        for(int j=0;j<n;j++) {
                                array[i][j]=matrix.getDouble(i,j);
                        }
                }
                reduceSymmetric1_SquareToTridiagonal(array,eigenvalue,offdiag);
                System.arraycopy(offdiag,1,offdiag,0,n-1);
                offdiag[n-1]=0.0;
                eigenvalueSolveSymmetricTridiagonalMatrix(eigenvalue,offdiag);
                return eigenvalue;
        }
        /**
        * This method finds the eigenvalues and eigenvectors of a symmetric square matrix.
        * The matrix is reduced to tridiagonal form and then the QL method is applied.
        * It is based on the NETLIB algol/fortran procedure tred2/tql2 by Bowdler, Martin, Reinsch and Wilkinson.
        * @param matrix a double symmetric square matrix.
        * All eigenvectors will be orthogonal.
        * @return an array containing the eigenvalues.
        * @exception MaximumIterationsExceededException If it takes too many iterations to determine an eigenvalue.
        */
        private double[] eigenvectorSolveSymmetric(final DoubleMatrix matrix) throws MaximumIterationsExceededException {
                final int n=matrix.rows();
                final double eigenvalue[]=new double[n];
                final double offdiag[]=new double[n];
                final double transf[][]=new double[n][n];
                for(int i=0;i<n;i++) {
                        for(int j=0;j<n;j++) {
                                transf[i][j]=matrix.getDouble(i,j);
                        }
                }
                reduceSymmetric2_SquareToTridiagonal(transf,eigenvalue,offdiag);
                System.arraycopy(offdiag,1,offdiag,0,n-1);
                offdiag[n-1]=0.0;
                eigenvectorSolveSymmetricTridiagonalMatrix(eigenvalue,offdiag,transf);
                eigenvectors = new DoubleVector[n];
                for(int i=0;i<n;i++) {
                        DoubleVector<DenseDoubleArray1D> evec = new DoubleVector<DenseDoubleArray1D>(new DenseDoubleArray1D(n));
                        for(int j=0; j<n; j++)
                                evec.setDouble(j, transf[i][j]);
                        eigenvectors[i] = evec;
                }
                return eigenvalue;
        }

        /**
        * Internal NETLIB tql1 routine.
        * @param diag output eigenvalues.
        * @author Richard Cannings
        */
        private void eigenvalueSolveSymmetricTridiagonalMatrix(final double diag[],final double offdiag[]) throws MaximumIterationsExceededException {
                final int n=diag.length;
                final int nm1=n-1;
                for(int l=0;l<n;l++) {
                        int iteration=0;
                        int m;
                        do {
                                for(m=l;m<nm1;m++) {
                                        double dd=Math.abs(diag[m])+Math.abs(diag[m+1]);
                                        if(Math.abs(offdiag[m])+dd==dd)
                                                break;
                                }
                                if(m!=l) {
                                        if(iteration++ == maxIters)
                                                throw new MaximumIterationsExceededException("No convergence after "+maxIters+" iterations.", new double[][] {diag, offdiag});
                                        double g=(diag[l+1]-diag[l])/(2.0*offdiag[l]);
                                        double r=Math.sqrt(g*g+1.0);
                                        g=diag[m]-diag[l]+offdiag[l]/(g+(g<0.0?-Math.abs(r):Math.abs(r)));
                                        double s=1.0;
                                        double c=1.0;
                                        double p=0.0;
                                        for(int i=m-1;i>=l;i--) {
                                                double f=s*offdiag[i];
                                                double b=c*offdiag[i];
                                                if(Math.abs(f)>=Math.abs(g)) {
                                                        c=g/f;
                                                        r=Math.sqrt(c*c+1.0);
                                                        offdiag[i+1]=f*r;
                                                        s=1.0/r;
                                                        c*=s;
                                                } else {
                                                        s=f/g;
                                                        r=Math.sqrt(s*s+1.0);
                                                        offdiag[i+1]=g*r;
                                                        c=1.0/r;
                                                        s*=c;
                                                }
                                                g=diag[i+1]-p;
                                                r=(diag[i]-g)*s+2.0*c*b;
                                                p=s*r;
                                                diag[i+1]=g+p;
                                                g=c*r-b;
                                        }
                                        diag[l] -= p;
                                        offdiag[l]=g;
                                        offdiag[m]=0.0;
                                }
                        } while(m!=l);
                }
        }
        /**
        * Internal NETLIB tql2 routine.
        * @param diag output eigenvalues.
        * @param transf output eigenvectors.
        * @author Richard Cannings
        */
        private void eigenvectorSolveSymmetricTridiagonalMatrix(final double diag[],final double offdiag[],final double transf[][]) throws MaximumIterationsExceededException {
                final int n=diag.length;
                final int nm1=n-1;
                for(int l=0;l<n;l++) {
                        int iteration=0;
                        int m;
                        do {
                                for(m=l;m<nm1;m++) {
                                        double dd=Math.abs(diag[m])+Math.abs(diag[m+1]);
                                        if(Math.abs(offdiag[m])+dd==dd)
                                                break;
                                }
                                if(m!=l) {
                                        if(iteration++ == maxIters)
                                                throw new MaximumIterationsExceededException("No convergence after "+maxIters+" iterations.", new Object[] {diag, offdiag, transf});
                                        double g=(diag[l+1]-diag[l])/(2.0*offdiag[l]);
                                        double r=Math.sqrt(g*g+1.0);
                                        g=diag[m]-diag[l]+offdiag[l]/(g+(g<0.0?-Math.abs(r):Math.abs(r)));
                                        double s=1.0;
                                        double c=1.0;
                                        double p=0.0;
                                        for(int i=m-1;i>=l;i--) {
                                                double f=s*offdiag[i];
                                                double b=c*offdiag[i];
                                                if(Math.abs(f)>=Math.abs(g)) {
                                                        c=g/f;
                                                        r=Math.sqrt(c*c+1.0);
                                                        offdiag[i+1]=f*r;
                                                        s=1.0/r;
                                                        c*=s;
                                                } else {
                                                        s=f/g;
                                                        r=Math.sqrt(s*s+1.0);
                                                        offdiag[i+1]=g*r;
                                                        c=1.0/r;
                                                        s*=c;
                                                }
                                                g=diag[i+1]-p;
                                                r=(diag[i]-g)*s+2.0*c*b;
                                                p=s*r;
                                                diag[i+1]=g+p;
                                                g=c*r-b;
                                                for(int k=0;k<n;k++) {
                                                        f = transf[i+1][k];
                                                        transf[i+1][k] = s*transf[i][k]+c*f;
                                                        transf[i][k] = c*transf[i][k]-s*f;
                                                }
                                        }
                                        diag[l] -= p;
                                        offdiag[l]=g;
                                        offdiag[m]=0.0;
                                }
                        } while(m!=l);
                }
        }

        /**
        * Internal NETLIB tred1 routine.
        * @author Richard Cannings
        */
        private void reduceSymmetric1_SquareToTridiagonal(final double matrix[][],final double diag[],final double offdiag[]) {
                final int n=diag.length;
                for(int i=n-1;i>0;i--) {
                        final int l=i-1;
                        if(l>0) {
                                double scale=0.0;
                                for(int k=0;k<=l;k++) {
                                        scale+=Math.abs(matrix[i][k]);
                                }
                                if(scale==0.0) {
                                        offdiag[i]=matrix[i][l];
                                } else {
                                        double h=0.0;
                                        for(int k=0;k<=l;k++) {
                                                double tmp = matrix[i][k]/scale;
                                                matrix[i][k]=tmp;
                                                h+=tmp*tmp;
                                        }
                                        double f=matrix[i][l];
                                        double g=(f>=0.0?-Math.sqrt(h):Math.sqrt(h));
                                        offdiag[i]=scale*g;
                                        h-=f*g;
                                        matrix[i][l]=f-g;
                                        f=0.0;
                                        for(int j=0;j<=l;j++) {
                                                g=0.0;
                                                for(int k=0;k<=j;k++)
                                                        g+=matrix[j][k]*matrix[i][k];
                                                for(int k=j+1;k<=l;k++)
                                                        g+=matrix[k][j]*matrix[i][k];
                                                final double tmp = g/h;
                                                offdiag[j]=tmp;
                                                f+=tmp*matrix[i][j];
                                        }
                                        final double hh=f/(h+h);
                                        for(int j=0;j<=l;j++) {
                                                f=matrix[i][j];
                                                offdiag[j]=g=offdiag[j]-hh*f;
                                                for(int k=0;k<=j;k++)
                                                        matrix[j][k]-=f*offdiag[k]+g*matrix[i][k];
                                        }
                                }
                        } else {
                                offdiag[i]=matrix[i][l];
                        }
                }
                offdiag[0]=0.0;
                for(int i=0;i<n;i++)
                        diag[i]=matrix[i][i];
        }
        /**
        * Internal NETLIB tred2 routine.
        * @param matrix output orthogonal transformations.
        * @author Richard Cannings
        */
        private void reduceSymmetric2_SquareToTridiagonal(final double matrix[][],final double diag[],final double offdiag[]) {
                final int n=diag.length;
                for(int i=n-1;i>0;i--) {
                        final int l=i-1;
                        double h=0.0;
                        if(l>0) {
                                double scale=0.0;
                                for(int k=0;k<=l;k++)
                                        scale+=Math.abs(matrix[k][i]);
                                if(scale==0.0) {
                                        offdiag[i]=matrix[l][i];
                                } else {
                                        for(int k=0;k<=l;k++) {
                                                double tmp = matrix[k][i]/scale;
                                                matrix[k][i]=tmp;
                                                h+=tmp*tmp;
                                        }
                                        double f=matrix[l][i];
                                        double g=(f>=0.0?-Math.sqrt(h):Math.sqrt(h));
                                        offdiag[i]=scale*g;
                                        h-=f*g;
                                        matrix[l][i]=f-g;
                                        f=0.0;
                                        for(int j=0;j<=l;j++) {
                                                matrix[i][j]=matrix[j][i]/h;
                                                g=0.0;
                                                for(int k=0;k<=j;k++)
                                                        g+=matrix[k][j]*matrix[k][i];
                                                for(int k=j+1;k<=l;k++)
                                                        g+=matrix[j][k]*matrix[k][i];
                                                final double tmp = g/h;
                                                offdiag[j]=tmp;
                                                f+=tmp*matrix[j][i];
                                        }
                                        final double hh=f/(h+h);
                                        for(int j=0;j<=l;j++) {
                                                f=matrix[j][i];
                                                offdiag[j]=g=offdiag[j]-hh*f;
                                                for(int k=0;k<=j;k++)
                                                        matrix[k][j]-=f*offdiag[k]+g*matrix[k][i];
                                        }
                                }
                        } else {
                                offdiag[i]=matrix[l][i];
                        }
                        diag[i]=h;
                }
                offdiag[0]=0.0;
                for(int i=0;i<n;i++) {
                        final int l=i-1;
                        if(diag[i] != 0.0) {
                                for(int j=0;j<=l;j++) {
                                        double g=0.0;
                                        for(int k=0;k<=l;k++)
                                                g+=matrix[k][i]*matrix[j][k];
                                        for(int k=0;k<=l;k++)
                                                matrix[j][k]-=g*matrix[i][k];
                                }
                        }
                        diag[i]=matrix[i][i];
                        matrix[i][i]=1.0;
                        for(int j=0;j<=l;j++)
                                matrix[j][i]=matrix[i][j]=0.0;
                }
        }

        
    /**
     * Reduces a symmetric matrix to a tridiagonal matrix in-place.
     * Resumable/optimised method for finding the eigenvalues of a (large) symmetric matrix.
     * @param array a symmetric square array.
     * @param offdiag an array to hold the off-diagonal elements.
     * @param start 0 < start <= n-1
     * @param end 0 <= end < start
     */
    public static void reduceSymmetricToTridiagonal(DoubleArray2D array, DoubleArray1D offdiag, int start, int end) throws MaximumIterationsExceededException {
            if(array instanceof Algorithms) {
                for(int i=start; i>end; i--) {
                    ((Algorithms)array).reductionIteration(i,offdiag);
                }
            } else {
                for(int i=start; i>end; i--) {
                    reductionIteration(i,array,offdiag);
                }
            }
            if(end == 0) {
                offdiag.setDouble(0, 0.0);
            }
    }
    /**
     * Diagonalizes a symmetric tridiagonal matrix in-place.
     * Resumable/optimised method for finding the eigenvalues of a (large) symmetric matrix.
     * @param array an array processed by reduceSymmetricToTridiagonal().
     * @param offdiag an array processed by reduceSymmetricToTridiagonal().
     * @param start 0 <= start < matrix.rows()
     * @param end start < end <= matrix.rows()
     */
    public static void diagonalizeSymmetricTridiagonal(DoubleArray2D array, DoubleArray1D offdiag, int start, int end, int maxIters) throws MaximumIterationsExceededException {
            if(start == 0) {
                final int n=offdiag.size();
                for(int i=1; i<n; i++) {
                    offdiag.setDouble(i-1, offdiag.getDouble(i));
                }
                offdiag.setDouble(n-1, 0.0);
            }
            if(array instanceof Algorithms) {
                for(int l=start; l<end; l++) {
                    ((Algorithms)array).eigenvalueIteration(l,offdiag,maxIters);
                }
            } else {
                for(int l=start; l<end; l++) {
                    eigenvalueIteration(l,array,offdiag,maxIters);
                }
            }
    }

        /**
         * In-place version.
         */
        private static void eigenvalueIteration(int l, DoubleArray2D array, final DoubleArray1D offdiag, final int maxIters) throws MaximumIterationsExceededException {
                final int nm1 = array.rows()-1;
                int iteration=0;
                int m;
                do {
                        for(m=l;m<nm1;m++) {
                                double dd=Math.abs(array.getDouble(m, m))+Math.abs(array.getDouble(m+1, m+1));
                                if(Math.abs(offdiag.getDouble(m))+dd==dd)
                                        break;
                        }
                        if(m!=l) {
                                if(iteration++ == maxIters)
                                        throw new MaximumIterationsExceededException("No convergence after "+maxIters+" iterations.", new Object[] {array, offdiag});
                                final double arrayll = array.getDouble(l, l);
                                final double offdiagl = offdiag.getDouble(l);
                                double g=(array.getDouble(l+1, l+1)-arrayll)/(2.0*offdiagl);
                                double r=Math.sqrt(g*g+1.0);
                                g=array.getDouble(m, m)-arrayll+offdiagl/(g+(g<0.0?-Math.abs(r):Math.abs(r)));
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
                                        g=array.getDouble(i+1, i+1)-p;
                                        r=(array.getDouble(i, i)-g)*s+2.0*c*b;
                                        p=s*r;
                                        array.setDouble(i+1, i+1, g+p);
                                        g=c*r-b;
                                }
                                array.setDouble(l, l, array.getDouble(l, l)-p);
                                offdiag.setDouble(l, g);
                                offdiag.setDouble(m, 0.0);
                        }
                } while(m!=l);
        }
        /**
         * In-place version.
         */
        private static void reductionIteration(int i, DoubleArray2D array, final DoubleArray1D offdiag) {
                final int l=i-1;
                if(l>0) {
                        double scale=0.0;
                        for(int k=0;k<=l;k++) {
                                scale+=Math.abs(array.getDouble(i,k));
                        }
                        if(scale==0.0) {
                                offdiag.setDouble(i, array.getDouble(i, l));
                        } else {
                                double h=0.0;
                                for(int k=0;k<=l;k++) {
                                        double tmp = array.getDouble(i, k)/scale;
                                        array.setDouble(i, k, tmp);
                                        h+=tmp*tmp;
                                }
                                double f=array.getDouble(i, l);
                                double g=(f>=0.0?-Math.sqrt(h):Math.sqrt(h));
                                offdiag.setDouble(i, scale*g);
                                h-=f*g;
                                array.setDouble(i, l, f-g);
                                f=0.0;
                                for(int j=0;j<=l;j++) {
                                        g=0.0;
                                        for(int k=0;k<=j;k++)
                                                g+=array.getDouble(j, k)*array.getDouble(i, k);
                                        for(int k=j+1;k<=l;k++)
                                                g+=array.getDouble(k, j)*array.getDouble(i, k);
                                        final double tmp = g/h;
                                        offdiag.setDouble(j, tmp);
                                        f+=tmp*array.getDouble(i, j);
                                }
                                final double hh=f/(h+h);
                                for(int j=0;j<=l;j++) {
                                        f=array.getDouble(i, j);
                                        g=offdiag.getDouble(j)-hh*f;
                                        offdiag.setDouble(j, g);
                                        for(int k=0;k<=j;k++)
                                                array.setDouble(j,k, array.getDouble(j, k) - (f*offdiag.getDouble(k) + g*array.getDouble(i, k)));
                                }
                        }
                } else {
                        offdiag.setDouble(i, array.getDouble(i, l));
                }
        }

        public interface Algorithms {
            void reductionIteration(int i, DoubleArray1D offdiag);
            void eigenvalueIteration(int i, DoubleArray1D offdiag, int maxIters);
        }
}

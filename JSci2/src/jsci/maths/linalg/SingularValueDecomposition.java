package jsci.maths.linalg;

import jsci.maths.matrix.DoubleMatrix;
import jsci.maths.matrix.impl.AlgorithmsDenseDoubleArray2D;
import jsci.util.array.DenseDoubleArray1D;
import jsci.util.array.DiagonalDoubleArray2D;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.DoubleArray2D;

/**
 *
 * @author Mark
 */
public class SingularValueDecomposition {
    private DoubleMatrix<DoubleArray2D,DoubleArray1D> U;
    private DoubleMatrix<DoubleArray2D,DoubleArray1D> S;
    private DoubleMatrix<DoubleArray2D,DoubleArray1D> V;

    public SingularValueDecomposition(DoubleMatrix<? extends DoubleArray2D,? extends DoubleArray1D> matrix) {
        singularValueDecompose(matrix);
    }

    public DoubleMatrix<DoubleArray2D,DoubleArray1D> getU() {
        return U;
    }
    public DoubleMatrix<DoubleArray2D,DoubleArray1D> getS() {
        return S;
    }
    public DoubleMatrix<DoubleArray2D,DoubleArray1D> getV() {
        return V;
    }

        /**
        * Returns the singular value decomposition of this matrix.
        * Based on the code from <a href="http://math.nist.gov/javanumerics/jama/">JAMA</a> (public domain).
        * @return an array with [0] containing the U-matrix, [1] containing the S-matrix and [2] containing the V-matrix.
        * @jsci.planetmath SingularValueDecomposition
        */
        public void singularValueDecompose(DoubleMatrix<? extends DoubleArray2D,? extends DoubleArray1D> matrix) {
                final int N=matrix.rows();
                final int Nm1=N-1;
                final double array[][]=new double[N][N];
                final double arrayU[][]=new double[N][N];
                final double arrayS[]=new double[N];
                final double arrayV[][]=new double[N][N];
                final double e[]=new double[N];
                final double work[]=new double[N];
                // copy matrix
                for(int i=0;i<N;i++) {
                        array[i][0]=matrix.getDouble(i,0);
                        for(int j=1;j<N;j++)
                                array[i][j]=matrix.getDouble(i,j);
                }
                // reduce matrix to bidiagonal form
                for(int k=0;k<Nm1;k++) {
                        // compute the transformation for the kth column
                        // compute l2-norm of kth column
                        arrayS[k]=array[k][k];
                        for(int i=k+1;i<N;i++)
                                arrayS[k]=Math.hypot(arrayS[k],array[i][k]);
                        if(arrayS[k]!=0.0) {
                                if(array[k][k]<0.0)
                                        arrayS[k]=-arrayS[k];
                                for(int i=k;i<N;i++)
                                        array[i][k]/=arrayS[k];
                                array[k][k]+=1.0;
                        }
                        arrayS[k]=-arrayS[k];
                        for(int j=k+1;j<N;j++) {
                                if(arrayS[k]!=0.0) {
                                        // apply the transformation
                                        double t=array[k][k]*array[k][j];
                                        for(int i=k+1; i<N; i++)
                                                t+=array[i][k]*array[i][j];
                                        t /= array[k][k];
                                        for(int i=k; i<N; i++)
                                                array[i][j] -= t*array[i][k];
                                }
                                e[j]=array[k][j];
                        }
                        for(int i=k;i<N;i++)
                                arrayU[i][k]=array[i][k];
                        if(k<N-2) {
                                // compute the kth row transformation
                                // compute l2-norm of kth column
                                e[k]=e[k+1];
                                for(int i=k+2;i<N;i++)
                                        e[k]=Math.hypot(e[k],e[i]);
                                if(e[k]!=0.0) {
                                        if(e[k+1]<0.0)
                                                e[k]=-e[k];
                                        for(int i=k+1;i<N;i++)
                                                e[i]/=e[k];
                                        e[k+1]+=1.0;
                                }
                                e[k]=-e[k];
                                if(e[k]!=0.0) {
                                        // apply the transformation
                                        for(int i=k+1;i<N;i++) {
                                                work[i]=0.0;
                                                for(int j=k+1;j<N;j++)
                                                        work[i]+=e[j]*array[i][j];
                                        }
                                        for(int j=k+1;j<N;j++) {
                                                double t = e[j]/e[k+1];
                                                for(int i=k+1;i<N;i++)
                                                        array[i][j] -= t*work[i];
                                        }
                                }
                                for(int i=k+1;i<N;i++)
                                        arrayV[i][k]=e[i];
                        }
                }
                // setup the final bidiagonal matrix of order p
                int p=N;
                arrayS[Nm1]=array[Nm1][Nm1];
                e[N-2]=array[N-2][Nm1];
                e[Nm1]=0.0;
                arrayU[Nm1][Nm1]=1.0;
                for(int k=N-2;k>=0;k--) {
                        if(arrayS[k]!=0.0) {
                                for(int j=k+1;j<N;j++) {
                                        double t=arrayU[k][k]*arrayU[k][j];
                                        for(int i=k+1;i<N;i++)
                                                t+=arrayU[i][k]*arrayU[i][j];
                                        t /= arrayU[k][k];
                                        for(int i=k;i<N;i++)
                                                arrayU[i][j] -= t*arrayU[i][k];
                                }
                                for(int i=k;i<N;i++)
                                        arrayU[i][k]=-arrayU[i][k];
                                arrayU[k][k]+=1.0;
                                for(int i=0;i<k-1;i++)
                                        arrayU[i][k]=0.0;
                        } else {
                                for(int i=0;i<N;i++)
                                        arrayU[i][k]=0.0;
                                arrayU[k][k]=1.0;
                        }
                }
                for(int k=Nm1;k>=0;k--) {
                        if(k<N-2 && e[k]!=0.0) {
                                for(int j=k+1;j<N;j++) {
                                        double t=arrayV[k+1][k]*arrayV[k+1][j];
                                        for(int i=k+2;i<N;i++)
                                                t+=arrayV[i][k]*arrayV[i][j];
                                        t /= arrayV[k+1][k];
                                        for(int i=k+1;i<N;i++)
                                                arrayV[i][j] -= t*arrayV[i][k];
                                }
                        }
                        for(int i=0;i<N;i++)
                                arrayV[i][k]=0.0;
                        arrayV[k][k]=1.0;
                }
                final double eps=Math.pow(2.0,-52.0);
                int iter=0;
                while(p>0) {
                        int k,action;
                        // action = 1 if arrayS[p] and e[k-1] are negligible and k<p
                        // action = 2 if arrayS[k] is negligible and k<p
                        // action = 3 if e[k-1] is negligible, k<p, and arrayS[k], ..., arrayS[p] are not negligible (QR step)
                        // action = 4 if e[p-1] is negligible (convergence)
                        for(k=p-2;k>=-1;k--) {
                                if(k==-1)
                                        break;
                                if(Math.abs(e[k])<=eps*(Math.abs(arrayS[k])+Math.abs(arrayS[k+1]))) {
                                        e[k]=0.0;
                                        break;
                                }
                        }
                        if(k==p-2) {
                                action=4;
                        } else {
                                int ks;
                                for(ks=p-1;ks>=k;ks--) {
                                        if(ks==k)
                                                break;
                                        double t=(ks!=p ? Math.abs(e[ks]) : 0.0)+(ks!=k+1 ? Math.abs(e[ks-1]) : 0.0);
                                        if(Math.abs(arrayS[ks])<=eps*t) {
                                                arrayS[ks]=0.0;
                                                break;
                                        }
                                }
                                if(ks==k) {
                                        action=3;
                                } else if(ks==p-1) {
                                        action=1;
                                } else {
                                        action=2;
                                        k=ks;
                                }
                        }
                        k++;
                        switch(action) {
                                // deflate negligible arrayS[p]
                                case 1: {
                                        double f=e[p-2];
                                        e[p-2]=0.0;
                                        for(int j=p-2;j>=k;j--) {
                                                double t=Math.hypot(arrayS[j],f);
                                                final double cs=arrayS[j]/t;
                                                final double sn=f/t;
                                                arrayS[j]=t;
                                                if(j!=k) {
                                                        f=-sn*e[j-1];
                                                        e[j-1]*=cs;
                                                }
                                                for(int i=0;i<N;i++) {
                                                        t=cs*arrayV[i][j]+sn*arrayV[i][p-1];
                                                        arrayV[i][p-1]=-sn*arrayV[i][j]+cs*arrayV[i][p-1];
                                                        arrayV[i][j]=t;
                                                }
                                        }
                                        } break;
                                // split at negligible arrayS[k]
                                case 2: {
                                        double f=e[k-1];
                                        e[k-1]=0.0;
                                        for(int j=k;j<p;j++) {
                                                double t=Math.hypot(arrayS[j],f);
                                                final double cs=arrayS[j]/t;
                                                final double sn=f/t;
                                                arrayS[j]=t;
                                                f=-sn*e[j];
                                                e[j]*=cs;
                                                for(int i=0;i<N;i++) {
                                                        t=cs*arrayU[i][j]+sn*arrayU[i][k-1];
                                                        arrayU[i][k-1]=-sn*arrayU[i][j]+cs*arrayU[i][k-1];
                                                        arrayU[i][j]=t;
                                                }
                                        }
                                        } break;
                                // perform one QR step
                                case 3: {
                                        // calculate the shift
                                        final double scale=Math.max(Math.max(Math.max(Math.max(
                                                Math.abs(arrayS[p-1]),Math.abs(arrayS[p-2])),Math.abs(e[p-2])),
                                                Math.abs(arrayS[k])),Math.abs(e[k]));
                                        double sp=arrayS[p-1]/scale;
                                        double spm1=arrayS[p-2]/scale;
                                        double epm1=e[p-2]/scale;
                                        double sk=arrayS[k]/scale;
                                        double ek=e[k]/scale;
                                        double b=((spm1+sp)*(spm1-sp)+epm1*epm1)/2.0;
                                        double c=(sp*epm1)*(sp*epm1);
                                        double shift=0.0;
                                        if(b!=0.0 || c!=0.0) {
                                                shift=Math.sqrt(b*b+c);
                                                if(b<0.0)
                                                        shift=-shift;
                                                shift=c/(b+shift);
                                        }
                                        double f=(sk+sp)*(sk-sp)+shift;
                                        double g=sk*ek;
                                        // chase zeros
                                        for(int j=k;j<p-1;j++) {
                                                double t=Math.hypot(f,g);
                                                double cs=f/t;
                                                double sn=g/t;
                                                if(j!=k)
                                                        e[j-1]=t;
                                                f=cs*arrayS[j]+sn*e[j];
                                                e[j]=cs*e[j]-sn*arrayS[j];
                                                g=sn*arrayS[j+1];
                                                arrayS[j+1]*=cs;
                                                for(int i=0;i<N;i++) {
                                                        t=cs*arrayV[i][j]+sn*arrayV[i][j+1];
                                                        arrayV[i][j+1]=-sn*arrayV[i][j]+cs*arrayV[i][j+1];
                                                        arrayV[i][j]=t;
                                                }
                                                t=Math.hypot(f,g);
                                                cs=f/t;
                                                sn=g/t;
                                                arrayS[j]=t;
                                                f=cs*e[j]+sn*arrayS[j+1];
                                                arrayS[j+1]=-sn*e[j]+cs*arrayS[j+1];
                                                g=sn*e[j+1];
                                                e[j+1]*=cs;
                                                if(j<Nm1) {
                                                        for(int i=0;i<N;i++) {
                                                                t=cs*arrayU[i][j]+sn*arrayU[i][j+1];
                                                                arrayU[i][j+1]=-sn*arrayU[i][j]+cs*arrayU[i][j+1];
                                                                arrayU[i][j]=t;
                                                        }
                                                }
                                        }
                                        e[p-2]=f;
                                        iter++;
                                        } break;
                                // convergence
                                case 4: {
                                        // make the singular values positive
                                        if(arrayS[k]<=0.0) {
                                                arrayS[k]=-arrayS[k];
                                                for(int i=0;i<p;i++)
                                                        arrayV[i][k]=-arrayV[i][k];
                                        }
                                        // order the singular values
                                        while(k<p-1) {
                                                if(arrayS[k]>=arrayS[k+1])
                                                        break;
                                                double tmp=arrayS[k];
                                                arrayS[k]=arrayS[k+1];
                                                arrayS[k+1]=tmp;
                                                if(k<Nm1) {
                                                        for(int i=0;i<N;i++) {
                                                                tmp=arrayU[i][k+1];
                                                                arrayU[i][k+1]=arrayU[i][k];
                                                                arrayU[i][k]=tmp;
                                                                tmp=arrayV[i][k+1];
                                                                arrayV[i][k+1]=arrayV[i][k];
                                                                arrayV[i][k]=tmp;
                                                        }
                                                }
                                                k++;
                                        }
                                        iter=0;
                                        p--;
                                        } break;
                        }
                }
                U = DoubleMatrix.create(new AlgorithmsDenseDoubleArray2D(arrayU));
                S = DoubleMatrix.create(new DiagonalDoubleArray2D(new DenseDoubleArray1D(arrayS)));
                V = DoubleMatrix.create(new AlgorithmsDenseDoubleArray2D(arrayV));
        }
}

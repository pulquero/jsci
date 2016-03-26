package JSci.maths;

import JSci.maths.matrices.DoubleSquareMatrix;
import JSci.maths.vectors.AbstractDoubleVector;
import JSci.maths.vectors.DoubleVector;

/**
* This class implements the Karhunen-Loeve expansion.
* @author Daniel Lemire
*/
public final class KarhunenLoeve {
	double[][] data;

	/**
	 * @param v an array where [i][j] is the jth component of the ith vector.
	*/
	public KarhunenLoeve(double[][] v) {
		setData(v);
	}
        public double[][] getProductMatrix() {
                return(getProductMatrix(data));
        }
        private static double[][] vectorToSquare (double[] v) {
                double[][] ans=new double[v.length][v.length];
                for(int k=0;k<v.length;k++) {
                        for(int l=0;l<v.length;l++) {
                                ans[l][k]=v[k]*v[l];
                        }
                }
                return(ans);
        }
        private static void add(double[][] a, double c, double[][] b) {
                for(int k=0;k<a.length;k++) {
                        for(int l=0;l<a[k].length;l++) {
                                a[k][l]+=b[k][l]*c;
                        }
                }
        }
	/**
	 * @param v an array where [i][j] is the jth component of the ith vector.
	 */
	public static double[][] getProductMatrix (double[][] v) {
		double[][] ans=new double[v[0].length][v[0].length];
                for(int k=0;k<v.length;k++) {
                        add(ans,1.0/v.length,vectorToSquare(v[k]));
                }
		return(ans);
	}
	public static double[][] getProductMatrix (double[] v) {
		return(vectorToSquare(v));
	}
	/**
	* Careful: doesn't generate a copy.
	*/
	public double[][] getData() {
		return(data);
	}
	/**
	* Careful: doesn't generate a copy.
	* @param v an array where [i][j] is the jth component of the ith vector.
	*/
	public void setData(double[][] v) {
		data=v;
	}
	/**
	* Returns the eigenvectors ordered by the norm of the eigenvalues
        * (from max to min).
        * @exception MaximumIterationsExceededException if it can't compute
        * the eigenvectors within the limited number of iterations allowed.
	*/
	public AbstractDoubleVector[] getEigenvectors() throws MaximumIterationsExceededException {
                double[][] test=getProductMatrix(data);
		DoubleSquareMatrix alpha=new DoubleSquareMatrix(test);
                DoubleVector[] beta=new DoubleVector[data[0].length];
                double[] eigen=LinearMath.eigenSolveSymmetric(alpha,beta);
                tri(eigen,beta);
		return beta;
	}
	/**
	* Returns the eigenvectors ordered by the norm of the eigenvalues
        * (from max to min).
	* @return an array where [i][j] is the jth component of the ith eigenvector.
        * @exception MaximumIterationsExceededException if it can't compute
        * the eigenvectors within the limited number of iterations allowed.
	*/
	public double[][] getEigenvectorArrays() throws MaximumIterationsExceededException {
                AbstractDoubleVector[] beta=getEigenvectors();
                double[][] ans=new double[beta.length][beta[0].dimension()];
                for(int k=0;k<beta.length;k++) {
                        for(int l=0;l<beta[k].dimension();l++) {
                                ans[k][l]=beta[k].getComponent(l);
                        }
                }
                return(ans);
	}
	/** sort (french) */
	private static void tri(double[] v, AbstractDoubleVector[] mat) {
 		double temp;
		AbstractDoubleVector arraytemp;
		boolean doitTrier=true;
		while(doitTrier) {
			doitTrier=false;
			for(int k=0;k<v.length-1;k++) {
				if(v[k]<v[k+1]) {
					temp=v[k+1];
                                        v[k+1]=v[k];
                                        v[k]=temp;
                                        doitTrier=true;
					arraytemp=mat[k+1];
					mat[k+1]=mat[k];
					mat[k]=arraytemp;
				}
			}
		}
	}
}

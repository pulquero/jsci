
package JSci.maths.wavelet.cdf2_4;

import JSci.maths.wavelet.*;


/******************************************
* Cohen-Daubechies-Feauveau
* with N=2 and
* Ntilde=4 adapted to the interval
* by Deslauriers-Dubuc-Lemire
* @author Daniel Lemire
*****************************************/
public final class DualWavelet2_4 extends MultiscaleFunction  implements Cloneable {
	private int n0;
	private int k;
	private static CDF2_4 cdf=new CDF2_4();
   /*****************************************
  * Check if another object is equal to this
  * DualWavelet2_4 object
  ******************************************/
    public boolean equals(Object a) {
    if((a!=null) && (a instanceof DualWavelet2_4))  {
      DualWavelet2_4 iv=(DualWavelet2_4)a;
      return (this.dimension(0)==iv.dimension(0)) && (this.position()==iv.position());
    }
    return false;
  }

  /*******************************
  * Return a String representation
  * of the object
  ********************************/
  public String toString() {
    String ans=new String("[n0=");
    ans.concat(Integer.toString(n0));
    ans.concat("][k=");
    ans.concat(Integer.toString(k));
    ans.concat("]");
    return(ans);
  }
	/****************************************
  * This method is used to compute
  * how the number of scaling functions
  * changes from on scale to the other.
  * Basically, if you have k scaling
  * function and a Filter of type t, you'll
  * have 2*k+t scaling functions at the
  * next scale (dyadic case).
  * Notice that this method assumes
  * that one is working with the dyadic
  * grid while the method "previousDimension"
  * define in the interface "Filter" doesn't.
	******************************************/
	public int getFilterType () {
			return(cdf.filtretype);
	}
	public DualWavelet2_4 (int N0, int K) {
		setParameters(N0,K);
	}
	public DualWavelet2_4 () {
	}
  /**********************************************
  * Set the parameters for this object
  * @param N0 number of scaling function on the
  *   scale of this object
  * @param K position or number of this object
  * @exception IllegalScalingException if N0 is not
  *   large enough
  ***********************************************/
	public void setParameters(int N0, int K) {
		if(N0<cdf.minlength) {
			throw new IllegalScalingException(N0,cdf.minlength);
		}
		n0=N0;
		k=K;
	}
  /********************************************
  * Return a copy of this object
  *********************************************/
	public Object clone() {
    DualWavelet2_4 s = (DualWavelet2_4)super.clone();
    s.n0=n0;
    s.k=k;
    return(s);
	}
	/************************************************
  * Return as an array the sampled values
  * of the function
  * @param j number of iterations
	*************************************************/
	public double[] evaluate ( int j) {
		return(cdf.evalWavelet (n0, k, j));
	}
  /****************************************************
  * Given that the wavelet is written in terms of
  * a scale containing dimension() scaling functions and
  * going jfin scales ahead (iterating jfin times),
  * tells you how many scaling functions you'll need.
  * @param jfin number of iterations
  ******************************************************/
	public int dimension(int jfin) {
		return(Cascades.dimension(n0,jfin+1));
	}
  /****************************************************
  * Number of scaling functions at scale where this
  * wavelet belongs.
  *****************************************************/
	public int dimension() {
		return(Cascades.dimension(n0,1));
	}
  /****************************************
  * Tells you what is the number of this
  * wavelet. Wavelets are numbered from left
  * to right with the one at the left
  * boundary being noted 0.
  *****************************************/
	public int position() {
		return(k);
	}
}

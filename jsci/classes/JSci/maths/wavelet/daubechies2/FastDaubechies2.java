package JSci.maths.wavelet.daubechies2;

import JSci.maths.wavelet.FWT;

/************************
* This is a very fast implementation of the
* Fast Wavelet Transform. It uses in-place computations
* for less memory usage. Data length should be
* a power of 2 a be at least of length 4.
* Handles boundaries by assuming periodicity.
* Ideal for image processing or processing large
* amount of data. Uses floats for more performance.
* Safety is minimal, so be careful!
* @author Daniel Lemire
*************************/
public final class FastDaubechies2 extends FWT  {
  static final private float root3 =  (float)(Math.sqrt(3.0));
  static final private float normalizer = (float)(Math.pow(2d,-.5d));
  static final float[] scale = {(1f+(root3))*normalizer/4f,(3f+(root3))*normalizer/4f,(3f+(-1*(root3)))*normalizer/4f,(1f+(-1*(root3)))*normalizer/4f};
  static final float[] wavelet = {-(1f+(-1*(root3)))*normalizer/4f,(3f+(-1*(root3)))*normalizer/4f,-(3f+(root3))*normalizer/4f,(1f+(root3))*normalizer/4f};


  public FastDaubechies2() {
  }

  public static void transform (float[] v,int last) {
    float[] ans=new float[last];
    final int half=last/2;
    try {
      for(int k=0;/*k<half*/;k++) {
          ans[k+half]=v[(2*k+0)]*wavelet[0]+v[(2*k+1)]*wavelet[1]+v[(2*k+2)]*wavelet[2]+v[(2*k+3)]*wavelet[3];
          ans[k]=v[(2*k+0)]*scale[0]+v[(2*k+1)]*scale[1]+v[(2*k+2)]*scale[2]+v[(2*k+3)]*scale[3];
      }
    } catch (IndexOutOfBoundsException e) {}
    ans[last-1]=v[last-2]*wavelet[0]+v[last-1]*wavelet[1]+v[0]*wavelet[2]+v[1]*wavelet[3];
    ans[half-1]=v[last-2]*scale[0]+v[last-1]*scale[1]+v[0]*scale[2]+v[1]*scale[3];

    System.arraycopy(ans,0,v,0,last);
  }

  public void transform (float[] v) {
    int last;
    for (last=v.length;last>4;last/=2) {
      transform(v,last);
    }
    if(last!=4)
      System.err.println("Careful! this should be a power of 2 : "+v.length);
  }

  public void invTransform(float[] v) {
    int last;
    for (last=4;2*last<=v.length;last*=2) {
      invTransform(v,last);
    }
    if(last!=v.length)
      System.err.println("Careful! this should be a power of 2 : "+v.length);

  }

  public static void invTransform (float[] v, int last) {
    int ResultingLength=2*last;
    float[] ans=new float[ResultingLength];
    try {
      for(int k=0;/*k<last*/;k++) {
          ans[(2*k+3)]+=scale[3]*v[k]+wavelet[3]*v[k+last] ;
          ans[(2*k+2)]+=scale[2]*v[k]+wavelet[2]*v[k+last] ;
          ans[(2*k+1)]+=scale[1]*v[k]+wavelet[1]*v[k+last] ;
          ans[(2*k+0)]+=scale[0]*v[k]+wavelet[0]*v[k+last] ;
      }
    } catch (IndexOutOfBoundsException e) {}
    ans[ResultingLength-2]+=scale[0]*v[last-1]+wavelet[0]*v[ResultingLength-1] ;
    ans[ResultingLength-1]+=scale[1]*v[last-1]+wavelet[1]*v[ResultingLength-1] ;
    ans[0]+=scale[2]*v[last-1]+wavelet[2]*v[ResultingLength-1] ;
    ans[1]+=scale[3]*v[last-1]+wavelet[3]*v[ResultingLength-1] ;
    System.arraycopy(ans,0,v,0,ans.length);
  }
}


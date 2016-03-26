package com.cflex.util.lpSolve;

/**
* mat_alloc :The sparse matrix
* @author Rodrigo Almeida Gonçalves
*/
public class LpMatrec
{
   int rowNr;
   double value;
   public LpMatrec(int r, double v)
   {
      rowNr = r;
      value = v;
   }
}

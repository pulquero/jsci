package com.cflex.util.lpSolve;

/**
* @author Rodrigo Almeida Gonçalves
*/
public class LpColumn
{
   int row;
   double value;
   LpColumn next;
   public int getRow()
   {
      return row;
   }
}

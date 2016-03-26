package com.cflex.util.lpSolve;

/**
* @author Rodrigo Almeida Gonçalves
*/
public class LpHashElement
{
   String name;
   LpHashElement next;
   LpColumn col;
   LpBound bound;
   int must_be_int;
   int index; /* for row and column name hash tables */

   public LpHashElement(String name, int row, double value)
   {
      this.name = name;
      col = new LpColumn();
      col.row = row;
      col.value = value;

      bound = new LpBound();
   }
}



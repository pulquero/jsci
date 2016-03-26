package com.cflex.util.lpSolve;

import java.util.*;
import java.lang.*;

/**
* Packs a double value for reference.
* This is the Java equivalent of a C pointer to a double.
* @author Rodrigo Almeida Gonçalves
*/
class DoubleReference
{
   double value;
   public DoubleReference(double v)
   {
      value = v;
   }
}

/**
* @author Rodrigo Almeida Gonçalves
*/
public class LpSolver extends Solver implements LpConstant
{

   /* Globals used by solver */
   int JustInverted;
   int Status;
   int DoIter;
   int DoInvert;
   int Break_bb;
   double Extrad;
   double Trej;

   int Level;

   public LpSolver(LpModel model)
   {
      super(model);
   }

   final double myRound(double val, double eps) throws Exception
   {
      return (Math.abs(val) < eps) ? 0 : val;
   }

   /**
   * Determine Begin and End of last Eta matrix.
   * calculate theta = 1/ Eta_value[Diagonal element]
   * multiply all coefficients in last matrix with -theta
   * JustInverted = FALSE
   */
   private void addEtaColumn(LpModel lp) throws Exception
   {
      int i, j, k;
      double theta;
      j = lp.etaColEnd[lp.etaSize];
      lp.etaSize++;
      k = lp.etaColEnd[lp.etaSize] - 1;
      theta = 1 / (double)lp.etaValue[k];
      lp.etaValue[k] = theta;
      for(i = j;i < k;i++)
         lp.etaValue[i] *= -theta;
      JustInverted = FALSE;
   }

   /**
   * Main idea: take one column from original matrix and call ftran
   *
   * init
   * ...
   * pcol[i] = 0  for all i
   *
   * If variable   ( This means not surplus/slack variable )
   * copy column coefficients into pcol[]
   * Actualise pcol[0] -= Extrad*f
   * else          (surplus/slack variable )
   * This column is column from identity matrix
   * pcol[varin] = 1 or -1
   *
   * ftran(1, Etasize, pcol)
   */
   public void setPivotColumn(LpModel lp, int lower, int varin, double[] pcol) throws Exception
   {
      int i, colnr;

      for(i = 0;i <= lp.rows;i++)
         pcol[i] = 0;
      if(lower!=FALSE)
      {
         if(varin > lp.rows)
         {
            colnr = varin - lp.rows;
            for(i = lp.colEnd[colnr - 1];i < lp.colEnd[colnr];i++)
               pcol[lp.mat[i].rowNr] = lp.mat[i].value;
            pcol[0] -= Extrad;
         }
         else
            pcol[varin] = 1;
      }
      else
      { /* !lower */
         if(varin > lp.rows)
         {
            colnr = varin - lp.rows;
            for(i = lp.colEnd[colnr - 1];i < lp.colEnd[colnr];i++)
               pcol[lp.mat[i].rowNr] = -lp.mat[i].value;
            pcol[0] += Extrad;
         }
         else
            pcol[varin] = -1;
      }
      lp.ftran(pcol);
   } /* setpivcol */

   /**
   * set varin
   * elnr = wk = Eta_colEnd[Eta_size]  ( next free element )
   * Eta_size++
   * ( Eta_size = number of matrices in Eta file )
   * ( Eta_colEnd = End of one matrix           )
   *
   * Do something if Extrad <> 0          ( I do not know what to do
   * and what is Extrad  )
   * For all coefficients in column
   * set rowNr
   * If Objective_row and Extrad
   * Eta_value[Eta_colEnd[Eta_size -1]] += Mat[j].value
   * else if k <> rowNr
   * Eta_rowNr[elnr] = k
   * Eta_value[elnr] = Mat[j].value
   * elnr++
   * else
   * piv = Mat[j].value
   *
   * ( Last action: Write element on diagonal )
   * insert rowNr and 1/piv
   *
   * theta = rhs[rowNr] / piv
   * Rhs[rowNr] = theta
   *
   * For all coefficients of last eta matrix without diagonal element
   * Rhs[Eta_rowNr[i]] -= theta*Eta_value[i]
   *
   * ( set administration data for Basis )
   * varout =
   * Bas[rowNr] = varin
   * Basis[varout] = FALSE
   * Basis[varin] = TRUE
   *
   * For all coefficients of last eta matrix without diagonal element
   * Eta_value[i] /= -piv
   *
   * ( update Eta_colEnd )
   * Eta_colEnd[Eta_size] = elnr
   */
   private void minorIteration(LpModel lp, int colnr, int rowNr) throws Exception
   {
      int i, j, k, wk, varin, varout, elnr;
      double piv = 0, theta;
      varin = colnr + lp.rows;
      elnr = lp.etaColEnd[lp.etaSize];
      wk = elnr;
      lp.etaSize++;
      if(Extrad != 0)
      {
         lp.etaRowNr[elnr] = 0;
         lp.etaValue[elnr] = -Extrad;
         elnr++;
         if(elnr >= lp.etaAlloc)
            lp.resizeEta(elnr);
      }
      for(j = lp.colEnd[colnr - 1];j < lp.colEnd[colnr];j++)
      {
         k = lp.mat[j].rowNr;
         if(k == 0 && Extrad != 0)
            lp.etaValue[lp.etaColEnd[lp.etaSize - 1]] += lp.mat[j].value;
         else
            if(k != rowNr)
            {
               lp.etaRowNr[elnr] = k;
               lp.etaValue[elnr] = lp.mat[j].value;
               elnr++;
               if(elnr >= lp.etaAlloc)
                  lp.resizeEta(elnr);
            }
            else
               piv = lp.mat[j].value;
      }
      lp.etaRowNr[elnr] = rowNr;
      lp.etaValue[elnr] = 1 / piv;
      theta = lp.rhs[rowNr] / piv;
      lp.rhs[rowNr] = theta;
      for(i = wk;i < elnr;i++)
         lp.rhs[lp.etaRowNr[i]] -= theta * lp.etaValue[i];
      varout = lp.bas[rowNr];
      lp.bas[rowNr] = varin;
      lp.basis[varout] = FALSE;
      lp.basis[varin] = TRUE;
      for(i = wk;i < elnr;i++)
         lp.etaValue[i] /= -piv;
      lp.etaColEnd[lp.etaSize] = elnr + 1;
   } /* minoriteration */

   /**
   * Error test
   * Find for last matrix in etafile the begin and end
   * for all coefficients in last eta matrix without diagonal coefficient
   * calculate rhs[etaRowNr] -= theta * etavalue[i]
   * rhs[rowNr] = theta
   *
   * varout = bas[rowNr]
   * Bas[rowNr] = varin
   * Basis[varout] = FALSE
   * Basis[varin]  = TRUE
   */
   private void rhsMinColumn(LpModel lp, double theta, int rowNr, int varin) throws Exception
   {
      int i, j, k, varout;
      double f;
      if(rowNr > lp.rows + 1)
         throw (new Exception("Error: rhsmincol called with rowNr: " + rowNr + ", rows: " + lp.rows + "." + "This indicates numerical instability\n"));
      j = lp.etaColEnd[lp.etaSize];
      k = lp.etaColEnd[lp.etaSize + 1];
      for(i = j;i < k;i++)
      {
         f = lp.rhs[lp.etaRowNr[i]] - theta * lp.etaValue[i];
         f = myRound(f, lp.epsb);
         lp.rhs[lp.etaRowNr[i]] = f;
      }
      lp.rhs[rowNr] = theta;
      varout = lp.bas[rowNr];
      lp.bas[rowNr] = varin;
      lp.basis[varout] = FALSE;
      lp.basis[varin] = TRUE;
   } /* rhsmincol */

   /**
   * Basis has to be valid
   * set_globals
   * if etaValid = FALSE
   * invert
   * initialise array with 0
   * set rc[0] = 1
   * btran(rc)
   * For all columns
   * if variable not in basis AND upper bound > 0
   * rc[column] = SUM (over all elements in Column) mat.value * rc[row]
   * round all values
   */
   void getReducedCosts(LpModel lp, double[] rc) throws Exception
   {
      int varnr, i, j;
      double f;
      if(!(lp.basisValid != FALSE))
         throw (new Exception("Not a valid basis in get_reduced_costs"));
      if(!(lp.etaValid != FALSE))
         invert(lp);
      for(i = 1;i <= lp.sum;i++)
         rc[i] = 0;
      rc[0] = 1;
      lp.btran(rc);
      for(i = 1;i <= lp.columns;i++)
      {
         varnr = lp.rows + i;
         if(!(lp.basis[varnr] != FALSE))
            if(lp.upperBound[varnr] > 0)
            {
               f = 0;
               for(j = lp.colEnd[i - 1];j < lp.colEnd[i];j++)
                  f += rc[lp.mat[j].rowNr] * lp.mat[j].value;
               rc[varnr] = f;
            }
      }
      for(i = 1;i <= lp.sum;i++)
         rc[i] = myRound(rc[i], lp.epsd);
   }


   /**
   * allocate
   *
   * +---+---+---+---+---+---+
   * | 0 |   |   |   |   |   | rownum
   * +---+---+---+---+---+---+
   * ^
   * |
   * Rows+1
   *
   * +---+---+---+---+---+---+
   * |   |   |   |   |   |   | col
   * +---+---+---+---+---+---+
   *
   * +---+---+---+---+---+---+
   * |   |   |   |   |   |   | row
   * +---+---+---+---+---+---+
   *
   * +---+---+---+---+---+---+
   * |   |   |   |   |   |   | pcol               REAL pivot column??
   * +---+---+---+---+---+---+
   *
   * +---+---+---+---+---+---+
   * |TRU|   |   |   |   |   | frow               short
   * +---+---+---+---+---+---+
   *
   * +---+---+---+---+---+---+---+---+
   * |FAL|   |   |   |   |   |   |   | fcol       short
   * +---+---+---+---+---+---+---+---+
   * ^
   * |
   * columns+1
   *
   * +---+---+---+---+---+---+---+---+
   * | 0 |   |   |   |   |   |   |   | colnum   ( count number of
   * +---+---+---+---+---+---+---+---+             Coefficients which
   * appear in basis matrix )
   *
   * change frow and fcol depending on Bas[i]
   * frow = FALSE if Bas[i] <= Rows
   * fcol = TRUE  if Bas[i] >  Rows
   *
   *
   * set
   * Bas[i] = i   for all i
   * Basis[i] = TRUE   for all slack variables
   * FALSE  for all other variables
   * Rhs[i] = Rh[i]   for all i    ( initialise original Rhs )
   *
   * Correct Rhs for all Variables on upper bound
   * Correct Rhs for all slack variables on upper bound (if necessary)
   *
   * Etasize =0
   * v = 0
   * rowNr = 0
   * Num_inv = 0
   * numit = 0
   *
   * Look for rows with only one Coefficient  (while)
   * if found
   * look for column of coefficient
   * set     fcol[colnr - 1] = FALSE  ( This col is no longer
   * in Basis )
   * colnum[colnr] = 0
   * correct rownum counter
   * set frow[row_num] = FALSE    ( This row is no longer
   * in Basis )
   * minorIteration(colnr, rowNr)
   *
   * Look for columns with only one Coefficient  (while)
   * if found
   *
   * set frow[row_num] = FALSE    ( This row is no longer
   * in Basis )
   * rownum[] = 0
   * update column[]
   * numit++                ( counter how many iterations to do )
   * ( at end )
   * col[numit] = colnr     ( replaces minoriteration. But this )
   * ( is done later and we need arrays  )
   * row[numit] = rowNr    ( col and row therefore             )
   *
   * ( real invertation )
   * for all columns   (From beginning to end )
   * if fcol
   * set fcol[] = FALSE
   * setpivcol ( Lower[Rows + j] , Rows + j, pcol)
   * Loop through all the rows to find coefficient with
   * frow[rowNr] && pcol[rowNr]
   * ( Interpretation:
   * Look for first coefficient in (partly inverted)
   * Basis matrix which is nonzero and use it for pivot.)
   *
   * ( comparison for pcol is dangerous, but ok after
   * rounding )
   *
   * Error conditions
   *
   * ( Now we know pivot element )
   *
   * frow[rowNr] = FALSE
   * condenseColumn(rowNr, pcol)
   * rhsMinColumn(theta, rowNr, Rows + j)
   * addEtaColumn()
   *
   * For all stored actions   ( compare numit )
   * set colnr / varin
   * rowNr
   * init pcol with 0
   * set pcol[]
   * actualize pcol[0]
   * condenseColumn(rowNr, pcol)
   * rhsMinColumn(theta, rowNr, varin)
   * addEtaColumn()
   *
   * Round Rhs
   * print info
   * Justinverted = TRUE
   * Doinvert = FALSE
   */
   void invert(LpModel lp) throws Exception
   {
      int i, j, v, wk, numit, varnr, rowNr, colnr, varin;
      double theta;
      double[] pcol;
      int[] frow;
      int[] fcol;
      int rownum[], col[], row[];
      int[] colnum;
      if(lp.printAtInvert!=FALSE)
         print("Start Invert iter " + lp.iter + " etaSize " + lp.etaSize + " rhs[0] " + ((double)-lp.rhs[0]) + " \n");

      //CALLOC(rownum, lp->rows + 1);
      rownum = new int[lp.rows + 1];

      //CALLOC(col, lp->rows + 1);
      col = new int[lp.rows + 1];

      //CALLOC(row, lp->rows + 1);
      row = new int[lp.rows + 1];

      //CALLOC(pcol, lp->rows + 1);
      pcol = new double[lp.rows + 1];

      //CALLOC(fcol, lp->columns + 1);
      fcol = new int[lp.columns + 1];
      for(i = 0;i <= lp.columns;i++) /** It was <   changed by R.*/
         fcol[i] = FALSE;

      //CALLOC(frow, lp->rows + 1);
      frow = new int[lp.rows + 1];
      for(i = 0;i <= lp.rows;i++)
         frow[i] = TRUE;
      for(i = 0;i <= lp.rows;i++)
         if(lp.bas[i] > lp.rows)
            fcol[lp.bas[i] - lp.rows - 1] = TRUE;
         else
            frow[lp.bas[i]] = FALSE;


      //CALLOC(colnum, lp->columns + 1);
      colnum = new int[lp.columns + 1];
      for(i = 0;i <= lp.columns;i++)
         colnum[i] = 0;
      for(i = 1;i <= lp.rows;i++)
         if(frow[i] != FALSE)
            for(j = lp.rowEnd[i - 1] + 1;j <= lp.rowEnd[i];j++)
            {
               wk = lp.colNo[j];
               if(fcol[wk - 1] != FALSE)
               {
                  colnum[wk]++;
                  rownum[i - 1]++;
               }
            }
      for(i = 1;i <= lp.rows;i++)
         lp.bas[i] = i;
      for(i = 1;i <= lp.rows;i++)
         lp.basis[i] = TRUE;
      for(i = 1;i <= lp.columns;i++)
         lp.basis[i + lp.rows] = FALSE;
      for(i = 0;i <= lp.rows;i++)
         lp.rhs[i] = lp.rh[i];
      for(i = 1;i <= lp.columns;i++)
      {
         varnr = lp.rows + i;
         if(!(lp.lower[varnr]!=FALSE))
         {
            theta = lp.upperBound[varnr];
            for(j = lp.colEnd[i - 1];j < lp.colEnd[i];j++)
               lp.rhs[lp.mat[j].rowNr] -= theta * lp.mat[j].value;
         }
      }
      for(i = 1;i <= lp.rows;i++)
         if(!(lp.lower[i] != FALSE))
            lp.rhs[i] -= lp.upperBound[i];
      lp.etaSize = 0;
      v = 0;
      rowNr = 0;
      lp.numInv = 0;
      numit = 0;
      while(v < lp.rows)
      {
         rowNr++;
         if(rowNr > lp.rows)
            rowNr = 1;
         v++;
         if(rownum[rowNr - 1] == 1)
            if(frow[rowNr] != FALSE)
            {
               v = 0;
               j = lp.rowEnd[rowNr - 1] + 1;
               while(!(fcol[lp.colNo[j] - 1] != FALSE))
                  j++;
               colnr = lp.colNo[j];
               fcol[colnr - 1] = FALSE;
               colnum[colnr] = 0;
               for(j = lp.colEnd[colnr - 1];j < lp.colEnd[colnr];j++)
                  if(frow[lp.mat[j].rowNr] != FALSE)
                     rownum[lp.mat[j].rowNr - 1]--;
               frow[rowNr] = FALSE;
               minorIteration(lp, colnr, rowNr);
            }
      }
      v = 0;
      colnr = 0;
      while(v < lp.columns)
      {
         colnr++;
         if(colnr > lp.columns)
            colnr = 1;
         v++;
         if(colnum[colnr] == 1)
            if(fcol[colnr - 1]!=FALSE)
            {
               v = 0;
               j = lp.colEnd[colnr - 1] + 1;
               while(!(frow[lp.mat[j - 1].rowNr]!=FALSE))
                  j++;
               rowNr = lp.mat[j - 1].rowNr;
               frow[rowNr] = FALSE;
               rownum[rowNr - 1] = 0;
               for(j = lp.rowEnd[rowNr - 1] + 1;j <= lp.rowEnd[rowNr];j++)
                  if(fcol[lp.colNo[j] - 1]!=FALSE)
                     colnum[lp.colNo[j]]--;
               fcol[colnr - 1] = FALSE;
               numit++;
               col[numit - 1] = colnr;
               row[numit - 1] = rowNr;
            }
      }
      for(j = 1;j <= lp.columns;j++)
         if(fcol[j - 1] != FALSE)
         {
            fcol[j - 1] = FALSE;
            setPivotColumn(lp, lp.lower[lp.rows + j], j + lp.rows, pcol);
            rowNr = 1;
            while((rowNr <= lp.rows) && (!((frow[rowNr] != FALSE) && (pcol[rowNr] != FALSE))))
               rowNr++;

            /* if(rowNr == lp.rows + 1) */
            if(rowNr > lp.rows) /* problems! */
               throw (new Exception("Inverting failed"));
            frow[rowNr] = FALSE;
            lp.condenseColumn(rowNr, pcol);
            theta = lp.rhs[rowNr] / (double)pcol[rowNr];
            rhsMinColumn(lp, theta, rowNr, lp.rows + j);
            addEtaColumn(lp);
         }
      for(i = numit - 1;i >= 0;i--)
      {
         colnr = col[i];
         rowNr = row[i];
         varin = colnr + lp.rows;
         for(j = 0;j <= lp.rows;j++)
            pcol[j] = 0;
         for(j = lp.colEnd[colnr - 1];j < lp.colEnd[colnr];j++)
            pcol[lp.mat[j].rowNr] = lp.mat[j].value;
         pcol[0] -= Extrad;
         lp.condenseColumn(rowNr, pcol);
         theta = lp.rhs[rowNr] / (double)pcol[rowNr];
         rhsMinColumn(lp, theta, rowNr, varin);
         addEtaColumn(lp);
      }
      for(i = 1;i <= lp.rows;i++)
         lp.rhs[i] = myRound(lp.rhs[i], lp.epsb);
      if(lp.printAtInvert!=FALSE)
         print("End Invert                etaSize " + lp.etaSize + " rhs[0] " + ((double)-lp.rhs[0]) + "\n");
      JustInverted = TRUE;
      DoInvert = FALSE;
   } /* invert */

   /** Each, colprimal - rowprimal and rowdual - coldual form a couple
   * btran( 1,0,0,0,0,0,...)
   * update result depending on variables at upper bound
   *
   * look for variable with negative reduced costs
   *
   * ========================
   * More detailed:
   *
   * init *colnr = 0
   * dpiv = set to a small negative number.
   * if  NOT minit
   * drow = 1,0,0,0,0......
   * btran(drow)
   * For variables at upper bound we have to calculate the
   * reduced cost differently:
   * multiply each coefficient in column with reduced cost of row=
   * slackvariable and sum. This is new reduced cost.
   * round reduced costs "drow"
   * Look for variable which has upper bound Greater than Zero, which
   * is nonbasic.
   * Perhaps correct sign of reduced costs of variables
   * at upper bound.
   * take variable with most negative reduced costs.
   * save reduced costs in dpiv and
   * col_nr in *col_nr
   * print trace info
   * if *col_nr == 0
   * set some variables, that indicate that we are optimal.
   * return True, if *col_nr > 0
   */
   private int columnPrimal(LpModel lp, DoubleReference colnr, int minit, double[] drow) throws Exception
   {
      int varnr, i, j;
      double f, dpiv;
      dpiv = -lp.epsd;
      colnr.value = 0;
      if(!(minit != FALSE))
      {
         for(i = 1;i <= lp.sum;i++)
            drow[i] = 0;
         drow[0] = 1;
         lp.btran(drow);
         for(i = 1;i <= lp.columns;i++)
         {
            varnr = lp.rows + i;
            if(!(lp.basis[varnr] != FALSE))
               if(lp.upperBound[varnr] > 0)
               {
                  f = 0;
                  for(j = lp.colEnd[i - 1];j < lp.colEnd[i];j++)
                     f += drow[lp.mat[j].rowNr] * lp.mat[j].value;
                  drow[varnr] = f;
               }
         }
         for(i = 1;i <= lp.sum;i++)
            drow[i] = myRound(drow[i], lp.epsd);
      }
      for(i = 1;i <= lp.sum;i++)
         if(!(lp.basis[i] != FALSE))
            if(lp.upperBound[i] > 0)
            {
               if(lp.lower[i] != FALSE)
                  f = drow[i];
               else
                  f = -drow[i];
               if(f < dpiv)
               {
                  dpiv = f;
                  colnr.value = i;
               }
            }
      if(lp.trace != FALSE)
      {
         if(colnr.value > 0)
            println("col_prim:" + colnr.value + ", reduced cost: " + ((double)dpiv));
         else
            println("col_prim: no negative reduced costs found, optimality!");
      }
      if(colnr.value == 0)
      {
         DoIter = FALSE;
         DoInvert = FALSE;
         Status = OPTIMAL;
      }
      return (colnr.value > 0 ? (int)1 : (int)0);
   } /* colprim */

   /**
   * search for good candidate in a column for pivot
   * First look for big entries
   * Second ( this means first failed ) look also for smaller entries
   * Warning numerical instability
   *
   * Determine UNBOUNDED
   * Perhaps shift variable to its upper bound
   *
   * Aim: determin valid pivot element
   *
   * print some info
   *
   * Return true, if we had been successful finding a pivot element.
   *
   * ========================
   * More detailed:
   *
   * init *rowNr = 0
   * *theta = infinity
   * loop through all the rows
   * qout = maximal steplength = *thetha
   * *rowNr = number of that row.
   * ( At first look only for Steps which are not calculated with
   * very small divisors. If no such steps found, take also
   * small divisors in consideration )
   * Perhaps we found numerical problems. Print warning in this case
   *
   * If we did not find a limiting row, we are perhaps unbounded.
   * (upperbound on that variable = infinity)
   * The case that we have an upper bound is treated separately
   *
   * print some trace info
   *
   * return (*rowNr > 0)
   */
   private int rowPrimal(LpModel lp, int colnr, DoubleReference rowNr, DoubleReference theta, double[] pcol) throws Exception
   {
      int i;
      double f=0, quot;
      rowNr.value = 0;
      theta.value = lp.infinite;

      for(i = 1;i <= lp.rows;i++)
      {
         f = pcol[i];
         if(f != 0)
         {
            if(Math.abs(f) < Trej)
               lp.debugPrint(Level,"pivot " + ((double)f) + " rejected, too small (limit " + ((double)Trej) + ")\n");
            else
            { /* pivot alright */
               quot = 2 * lp.infinite;
               if(f > 0)
                  quot = lp.rhs[i] / (double)f;
               else
                  if(lp.upperBound[lp.bas[i]] < lp.infinite)
                     quot = (lp.rhs[i] - lp.upperBound[lp.bas[i]]) / (double)f;
               quot = myRound(quot, lp.epsel);
               if(quot < theta.value)
               {
                  theta.value = quot;
                  rowNr.value = i;
               }
            }
         }
      }
      if(rowNr.value == 0)
         for(i = 1;i <= lp.rows;i++)
         {
            f = pcol[i];
            if(f != 0)
            {
               quot = 2 * lp.infinite;
               if(f > 0)
                  quot = lp.rhs[i] / (double)f;
               else
                  if(lp.upperBound[lp.bas[i]] < lp.infinite)
                     quot = (lp.rhs[i] - lp.upperBound[lp.bas[i]]) / (double)f;
               quot = myRound(quot, lp.epsel);
               if(quot < theta.value)
               {
                  theta.value = quot;
                  rowNr.value = i;
               }
            }
         }
      if(theta.value < 0)
      {
         System.err.println("Warning: Numerical instability, qout = " + theta.value);
         System.err.println("pcol[" + rowNr.value + "] = " + f + ", rhs[" + rowNr.value + "] = " + lp.rhs[(int)rowNr.value] + " , upperBound = " + lp.upperBound[lp.bas[(int)rowNr.value]]);
      }
      if(rowNr.value == 0)
      {
         if(lp.upperBound[colnr] == lp.infinite)
         {
            DoIter = FALSE;
            DoInvert = FALSE;
            Status = UNBOUNDED;
         }
         else
         {
            i = 1;
            while(i <= lp.rows && pcol[i] >= 0)
               i++;
            if(i > lp.rows)
            { /* empty column with upperbound! */
               lp.lower[colnr] = FALSE;
               lp.rhs[0] += lp.upperBound[colnr] * pcol[0];
               DoIter = FALSE;
               DoInvert = FALSE;
            }
            else
               if(pcol[i] < 0)
                  rowNr.value = i;
         }
      }
      if(rowNr.value > 0)
         DoIter = TRUE;
      if(lp.trace!=FALSE)
         println("row_prim:" + rowNr.value + ", pivot element:" + pcol[(int)rowNr.value]);
      return ((rowNr.value > 0) ? (int)1 : (int)0);
   } /* rowprim */

   /**
   * Look for infeasibilities.
   *
   * init *rowNr = 0
   * minrhs = a little bit negative
   *
   * loop through all the rows
   * if we find a variable which is not zero, but has to be
   * then we break this loop. *rowNr = i
   *
   * calculate distance between rhs[i] and upperbound[i]
   * take smaller one
   *
   * |-------|----------------|
   * 0      rhs[i]          upperbound[i]
   * =g
   *
   * minrhs is smallest g
   * *rowNr is corresponding rownumber.
   *
   * print some trace info
   * return (*rowNr > 0)
   */
   private int rowDual(LpModel lp, DoubleReference rowNr) throws Exception
   {
      int i;
      double f, g, minrhs;
      int artifs;
      rowNr.value = 0;
      minrhs = -lp.epsb;
      i = 0;
      artifs = FALSE;
      while(i < lp.rows && !(artifs != FALSE))
      {
         i++;
         f = lp.upperBound[lp.bas[i]];
         if(f == 0 && (lp.rhs[i] != 0))
         {
            artifs = TRUE;
            rowNr.value = i;
         }
         else
         {
            if(lp.rhs[i] < f - lp.rhs[i])
               g = lp.rhs[i];
            else
               g = f - lp.rhs[i];
            if(g < minrhs)
            {
               minrhs = g;
               rowNr.value = i;
            }
         }
      }
      if(lp.trace != FALSE)
      {
         if(rowNr.value > 0)
         {
            println("row_dual:" + rowNr.value + ", rhs of selected row:           " + lp.rhs[(int)rowNr.value]);
            if(lp.upperBound[lp.bas[(int)rowNr.value]] < lp.infinite)
               println("               upper bound of basis variable:    " + lp.upperBound[lp.bas[(int)rowNr.value]]);
         }
         else
            println("row_dual: no infeasibilities found\n");
      }
      return (rowNr.value > 0 ? (int)1 : (int)0);
   } /* rowdual */

   /**
   * Looks also for a candidate for pivot.
   */
   private int columnDual(LpModel lp, int rowNr, DoubleReference colnr, int minit, double[] prow, double[] drow) throws Exception
   {
      int i, j, k, r, varnr, rowp[], row;
      double theta, quot, pivot, d, f, g, valuep[], value;
      DoIter = FALSE;
      if(!(minit!=FALSE))
      {
         for(i = 0;i <= lp.rows;i++)
         {
            prow[i] = 0;
            drow[i] = 0;
         }
         drow[0] = 1;
         prow[rowNr] = 1;
         for(i = lp.etaSize;i >= 1;i--)
         {
            d = 0;
            f = 0;
            r = lp.etaRowNr[lp.etaColEnd[i] - 1];
            for(j = lp.etaColEnd[i - 1];j < lp.etaColEnd[i];j++)
            {

               /* this is one of the loops where the program consumes a lot of CPU
               time */
               f += prow[lp.etaRowNr[j]] * lp.etaValue[j];
               d += drow[lp.etaRowNr[j]] * lp.etaValue[j];
            }
            f = myRound(f, lp.epsel);
            prow[r] = f;
            d = myRound(d, lp.epsel);
            drow[r] = d;
         }
         for(i = 1;i <= lp.columns;i++)
         {
            varnr = lp.rows + i;
            if(!(lp.basis[varnr] != FALSE))
            {
               d = -Extrad * drow[0];
               f = 0;
               for(j = lp.colEnd[i - 1];j < lp.colEnd[i];j++)
               {
                  d = d + drow[lp.mat[j].rowNr] * lp.mat[j].value;
                  f = f + prow[lp.mat[j].rowNr] * lp.mat[j].value;
               }
               f = myRound(f, lp.epsel);
               prow[varnr] = f;
               d = myRound(d, lp.epsd);
               drow[varnr] = d;
            }
         }
      }
      if(lp.rhs[rowNr] > lp.upperBound[lp.bas[rowNr]])
         g = -1;
      else
         g = 1;
      pivot = 0;
      colnr.value = 0;
      theta = lp.infinite;
      for(i = 1;i <= lp.sum;i++)
      {
         if(lp.lower[i]!=FALSE)
            d = prow[i] * g;
         else
            d = -prow[i] * g;
         if((d < 0) && (!(lp.basis[i] != FALSE)) && (lp.upperBound[i] > 0))
         {
            if(lp.lower[i]!=FALSE)
               quot = -drow[i] / (double)d;
            else
               quot = drow[i] / (double)d;
            if(quot < theta)
            {
               theta = quot;
               pivot = d;
               colnr.value = i;
            }
            else
               if((quot == theta) && (Math.abs(d) > Math.abs(pivot)))
               {
                  pivot = d;
                  colnr.value = i;
               }
         }
      }
      if(lp.trace != FALSE)
         println("col_dual:" + colnr.value + ", pivot element:  " + prow[(int)colnr.value]);
      if(colnr.value > 0)
         DoIter = TRUE;
      return (colnr.value > 0 ? (int)1 : (int)0);
   } /* coldual */

   /**
   * Execute one iteration.
   */
   void iteration(LpModel lp, int rowNr, int varin, DoubleReference theta, double up, DoubleReference minit, DoubleReference low, int primal) throws Exception
   {
      int i, k, varout;
      double f;
      double pivot;
      lp.iter++;

      //----------------------------
      //Java version only...
      if (viewer!=null)
         viewer.stepUpdate(lp.iter);
      if (state == STATE_4_PAUSED)
      {
         if (viewer!=null)
            viewer.stateChanged();
         wait();
      }
      //----------------------------

      minit.value = theta.value > (up + lp.epsb) ? 1 : 0;
      if(minit.value != 0)
      {
         theta.value = up;

         //(*low) = !(*low);
         low.value = low.value == 0 ? 1 : 0;
      }
      k = lp.etaColEnd[lp.etaSize + 1];
      pivot = lp.etaValue[k - 1];
      for(i = lp.etaColEnd[lp.etaSize];i < k;i++)
      {
         f = lp.rhs[lp.etaRowNr[i]] - theta.value * lp.etaValue[i];
         f = myRound(f, lp.epsb);
         lp.rhs[lp.etaRowNr[i]] = f;
      }
      if(!(minit.value != FALSE))
      {
         lp.rhs[rowNr] = theta.value;
         varout = lp.bas[rowNr];
         lp.bas[rowNr] = varin;
         lp.basis[varout] = FALSE;
         lp.basis[varin] = TRUE;
         if((primal!=FALSE) && pivot < 0)
            lp.lower[varout] = FALSE;
         if(!(low.value != FALSE) && up < lp.infinite)
         {
            low.value = TRUE;
            lp.rhs[rowNr] = up - lp.rhs[rowNr];
            for(i = lp.etaColEnd[lp.etaSize];i < k;i++)
               lp.etaValue[i] = -lp.etaValue[i];
         }
         addEtaColumn(lp);
         lp.numInv++;
      }
      if(lp.trace != FALSE)
      {
         println("Theta = " + theta.value + " ");
         if(minit.value != 0)
         {
            if(lp.lower[varin] == FALSE)
               print("Iteration:" + lp.iter + ", variable" + varin + " changed from 0 to its upper bound of " + lp.upperBound[varin] + "\n");
            else
               print("Iteration:" + lp.iter + ", variable" + varin + " changed its upper bound of " + lp.upperBound[varin] + " to 0\n");
         }
         else
            print("Iteration:" + lp.iter + ", variable" + varin + " entered basis at:" + lp.rhs[rowNr] + "\n");
         if(primal == 0)
         {
            f = 0;
            for(i = 1;i <= lp.rows;i++)
               if(lp.rhs[i] < 0)
                  f -= lp.rhs[i];
               else
                  if(lp.rhs[i] > lp.upperBound[lp.bas[i]])
                     f += lp.rhs[i] - lp.upperBound[lp.bas[i]];
            println("feasibility gap of this basis:" + (double)f);
         }
         else
            println("objective function value of this feasible basis: " + lp.rhs[0]);
      }
   } /* iteration */

   void presolve(LpModel lp)
   {
     print("Entering presolve\n");
   }

   /**
   * First check if right hand side is positive everywhere
   * and smaller than possible upper bound of this row.
   * In this case we start with a feasible basis.
   *
   * ATTENTION:
   * If we want to use solveLp() directly, skipping
   * solve() and milpsolve() we have to be very careful.
   * e.g. solve() sets the global variables!!!
   */
   private int solveLp(LpModel lp) throws Exception
   {
      int i, j, varnr;
      double f = 0, theta = 0;
      int primal;
      double[] drow, prow, Pcol;
      int minit;
      int colnr=-10000; //it is initialized with a dummy value to detect if it is used without being initilized /** By R. */
      int rowNr=-10000; //it is initialized with a dummy value to detect if it is used without being initilized /** By R. */
      int[] test;
      DoubleReference ref1, ref2, ref3;
      int flag;
      ref1 = new DoubleReference(0.0);
      ref2 = new DoubleReference(0.0);
      ref3 = new DoubleReference(0.0);
      if(lp.doPresolve != FALSE)
         presolve(lp);

      //  CALLOC(drow, lp->sum + 1);
      drow = new double[lp.sum + 1];
      //  CALLOC(prow, lp->sum + 1);
      prow = new double[lp.sum + 1];
      //  CALLOC(Pcol, lp->rows + 1);
      Pcol = new double[lp.rows + 1];
      //  CALLOC(test, lp->sum +1);
      test = new int[lp.sum + 1];

      lp.iter = 0;
      minit = FALSE;
      Status = RUNNING;
      DoInvert = FALSE;
      DoIter = FALSE;
      for(i = 1, primal = TRUE; (i <= lp.rows) && (primal!=FALSE) ;i++)
         primal = ((lp.rhs[i] >= 0) && (lp.rhs[i] <= lp.upperBound[lp.bas[i]])) ? TRUE : FALSE;
      if(lp.trace != FALSE)
      {
         if(primal != FALSE)
            println("Start at feasible basis");
         else
            println("Start at infeasible basis");
      }
      if(!(primal != FALSE))
      {
         drow[0] = 1;
         for(i = 1;i <= lp.rows;i++)
            drow[i] = 0;

         /* fix according to Joerg Herbers */
         lp.btran(drow);
         Extrad = 0;
         for(i = 1;i <= lp.columns;i++)
         {
            varnr = lp.rows + i;
            drow[varnr] = 0;
            for(j = lp.colEnd[i - 1];j < lp.colEnd[i];j++)
               if(drow[lp.mat[j].rowNr] != 0)
                  drow[varnr] += drow[lp.mat[j].rowNr] * lp.mat[j].value;
            if(drow[varnr] < Extrad)
               Extrad = drow[varnr];
         }
      }
      else
         Extrad = 0;
      if(lp.trace!=FALSE)
         println("Extrad = " + ((double)Extrad));
      minit = FALSE;
      while(Status == RUNNING)
      {
         DoIter = FALSE;
         DoInvert = FALSE;
         if(primal != FALSE)
         {
            colnr = 0; //At this point this variable has not been initialized but it does not matters... It is an output variavel passed as reference /* by R. */
            ref1.value = colnr;
            flag = columnPrimal(lp, ref1, minit, drow);
            colnr = (int) ref1.value;
            if(flag != FALSE)
            {
               setPivotColumn(lp, lp.lower[colnr], colnr, Pcol);

               rowNr = 0; //At this point this variable has not been initialized but it does not matters... It is an output variavel passed as reference /* by R. */
               theta = 0; //At this point this variable has not been initialized but it does not matters... It is an output variavel passed as reference /* by R. */
               ref1.value = rowNr;
               ref2.value = theta;
               flag = rowPrimal(lp, colnr, ref1, ref2, Pcol);
               rowNr = (int) ref1.value;
               theta = ref2.value;
               if(flag != FALSE)
                  lp.condenseColumn(rowNr, Pcol);
            }
         }
         else /* not primal */
         {
            if(!(minit != FALSE))
            {
               rowNr = 0; //At this point this variable has not been initialized but it does not matters... It is an output variavel passed as reference /* by R. */
               ref1.value = rowNr;
               rowDual(lp, ref1);
               rowNr = (int) ref1.value;
            }
            if(rowNr > 0)
            {
               colnr = 0; //At this point this variable has not been initialized but it does not matters... It is an output variavel passed as reference /* by R. */
               ref1.value = colnr;
               flag = columnDual(lp, rowNr, ref1, minit, prow, drow);
               colnr = (int) ref1.value;
               if(flag != FALSE)
               {
                  setPivotColumn(lp, lp.lower[colnr], colnr, Pcol);

                  /* getting div by zero here. Catch it and try to recover */
                  if(Pcol[rowNr] == 0)
                  {
                     println("An attempt was made to divide by zero (Pcol[" + rowNr + "])");
                     println("This indicates numerical instability");
                     DoIter = FALSE;
                     if(!(JustInverted!=FALSE))
                     {
                        println("Trying to recover. Reinverting Eta");
                        DoInvert = TRUE;
                     }
                     else
                     {
                        println("Can't reinvert, failure");
                        Status = FAILURE;
                     }
                  }
                  else
                  {
                     lp.condenseColumn(rowNr, Pcol);
                     f = lp.rhs[rowNr] - lp.upperBound[lp.bas[rowNr]];
                     if(f > 0)
                     {
                        theta = f / (double)Pcol[rowNr];
                        if(theta <= lp.upperBound[colnr])
//                           lp.lower[lp.bas[rowNr]] = !lp.lower[lp.bas[rowNr]];
                           lp.lower[lp.bas[rowNr]] = TRUE-lp.lower[lp.bas[rowNr]];
                     }
                     else /* f <= 0 */
                        theta = lp.rhs[rowNr] / (double)Pcol[rowNr];
                  }
               }
               else
                  Status = INFEASIBLE;
            }
            else
            {
               primal = TRUE;
               DoIter = FALSE;
               Extrad = 0;
               DoInvert = TRUE;
            }
         }
         if(DoIter != FALSE)
         {
            ref1.value = theta;
            ref2.value = minit;
            ref3.value = lp.lower[colnr];
            iteration(lp, rowNr, colnr, ref1, lp.upperBound[colnr], ref2, ref3, primal);
            theta = ref1.value;
            minit = (int) ref2.value;
            lp.lower[colnr] = (int) ref3.value;
         }
         if(lp.numInv >= lp.maxNumInv)
            DoInvert = TRUE;
         if(DoInvert!=FALSE)
         {
            if(lp.printAtInvert!=FALSE)
               println("Inverting: Primal = " + primal);
            invert(lp);
         }
      }
      lp.totalIter += lp.iter;

      //  free(drow);

      //  free(prow);

      //  free(Pcol);

      //  free(test); I love when we do not to do that in Java :-)
      return (Status);
   } /* solvelp */

   /**
   * Simple routine, checks, if a REAL value is integer
   */
   int isInt(LpModel lp, int i) throws Exception
   {
      double value, error;
      value = lp.solution[i];
      error = value - (double) Math.floor((double)value);
      if(error < lp.epsilon)
         return (TRUE);
      if(error > (1 - lp.epsilon))
         return (TRUE);
      return (FALSE);
   } /* isInt */

   /**
   * The routine does exactly, what its name says.
   * There are two parts, with and without scaling.
   *
   *
   * First set all variables to their lower bounds.
   * Then set all basis variables to their true values, i.e.
   * the right hand side is added to the lower bound.
   * (The reason is that all variables have been transformed
   * to have lower bound zero) ## Autor fragen!!
   * Finally set the non basic variables, which are not at
   * their lower bound to their upper bound.
   * Calculate values of the slack variables of a row.
   */
   private void constructSolution(LpModel lp) throws Exception
   {
      int i, j, basi;
      double f;

      /* zero all results of rows */
      Arrays.fill(lp.solution, 0);
      lp.solution[0] = -lp.origRh[0];
      if(lp.scalingUsed!=FALSE)
      {
         lp.solution[0] /= lp.scale[0];
         for(i = lp.rows + 1;i <= lp.sum;i++)
            lp.solution[i] = lp.lowerBound[i] * lp.scale[i];
         for(i = 1;i <= lp.rows;i++)
         {
            basi = lp.bas[i];
            if(basi > lp.rows)
               lp.solution[basi] += lp.rhs[i] * lp.scale[basi];
         }
         for(i = lp.rows + 1;i <= lp.sum;i++)
            if(!(lp.basis[i] != FALSE) && !(lp.lower[i] != FALSE))
               lp.solution[i] += lp.upperBound[i] * lp.scale[i];
         for(j = 1;j <= lp.columns;j++)
         {
            f = lp.solution[lp.rows + j];
            if(f != 0)
               for(i = lp.colEnd[j - 1];i < lp.colEnd[j];i++)
                  lp.solution[lp.mat[i].rowNr] += (f / lp.scale[lp.rows + j]) * (lp.mat[i].value / lp.scale[lp.mat[i].rowNr]);
         }
         for(i = 0;i <= lp.rows;i++)
         {
            if(Math.abs(lp.solution[i]) < lp.epsb)
               lp.solution[i] = 0;
            else
               if(lp.changedSign[i]!=FALSE)
                  lp.solution[i] = -lp.solution[i];
         }
      }
      else
      { /* no scaling */
         for(i = lp.rows + 1;i <= lp.sum;i++)
            lp.solution[i] = lp.lowerBound[i];
         for(i = 1;i <= lp.rows;i++)
         {
            basi = lp.bas[i];
            if(basi > lp.rows)
               lp.solution[basi] += lp.rhs[i];
         }
         for(i = lp.rows + 1;i <= lp.sum;i++)
            if(!(lp.basis[i] != FALSE) && !(lp.lower[i] != FALSE))
               lp.solution[i] += lp.upperBound[i];
         for(j = 1;j <= lp.columns;j++)
         {
            f = lp.solution[lp.rows + j];
            if(f != 0)
               for(i = lp.colEnd[j - 1];i < lp.colEnd[j];i++)
                  lp.solution[lp.mat[i].rowNr] += f * lp.mat[i].value;
         }
         for(i = 0;i <= lp.rows;i++)
         {
            if(Math.abs(lp.solution[i]) < lp.epsb)
               lp.solution[i] = 0;
            else
               if(lp.changedSign[i]!=FALSE)
                  lp.solution[i] = -lp.solution[i];
         }
      }
   } /* construct_solution */

   /**
   * In fact calculate the reduced costs of the slack variables
   * and correct values.
   */
   private void calculateDuals(LpModel lp) throws Exception
   {
      int i;

      /* initialize */
      lp.duals[0] = 1;
      for(i = 1;i <= lp.rows;i++)
         lp.duals[i] = 0;
      lp.btran(lp.duals);
      if(lp.scalingUsed != FALSE)
         for(i = 1;i <= lp.rows;i++)
            lp.duals[i] *= lp.scale[i] / lp.scale[0];

      /* the dual values are the reduced costs of the slacks */

      /* When the slack is at its upper bound, change the sign. */
      for(i = 1;i <= lp.rows;i++)
      {
         if(lp.basis[i] != FALSE)
            lp.duals[i] = 0;

         /* added a test if variable is different from 0 because sometime you get
         -0 and this is different from 0 on for example INTEL processors (ie 0
         != -0 on INTEL !) PN */
         else
            if((lp.changedSign[0] == lp.changedSign[i]) && (lp.duals[i]!=FALSE))
               lp.duals[i] = -lp.duals[i];
      }
   } /* calculate_duals */
   private void checkIfLess(double x, double y, double value)
   {
      if(x >= y)
      {
         viewer.errorMessage("Error: new upper or lower bound is not more restrictive\n");
         viewer.errorMessage("bound 1: " + ((double)x) + ", bound 2: " + ((double)y) + ", value: " + ((double)value));

      /* exit(EXIT_FAILURE); */
      }
   }
   private void checkSolution(LpModel lp, double[] upperBound, double[] lowerBound) throws Exception
   {
      int i;

      /* check if all solution values are within the bounds, but allow some margin
      for numerical errors */
      double CHECK_EPS = 1e-2;
      if(lp.columnsScaled != FALSE)
         for(i = lp.rows + 1;i <= lp.sum;i++)
         {
            if(lp.solution[i] < lowerBound[i] * lp.scale[i] - CHECK_EPS)
            {
               viewer.errorMessage("Error: variable " + (i - lp.rows) + " (" + (lp.colName[i - lp.rows]) + ") has a solution (" + ((double)lp.solution[i]) + ") smaller than its lower bound (" + ((double)lowerBound[i] * lp.scale[i]) + ")");

            /* abort(); */
            }
            if(lp.solution[i] > upperBound[i] * lp.scale[i] + CHECK_EPS)
            {
               viewer.errorMessage("Error: variable " + (i - lp.rows) + " (" + lp.colName[i - lp.rows] + ") has a solution (" + ((double)lp.solution[i]) + ") larger than its upper bound (" + ((double)upperBound[i] * lp.scale[i]) + ")\n");

            /* abort(); */
            }
         }
      else /* columns not scaled */
         for(i = lp.rows + 1;i <= lp.sum;i++)
         {
            if(lp.solution[i] < lowerBound[i] - CHECK_EPS)
            {
               viewer.errorMessage("Error: variable " + (i - lp.rows) + " (" + (lp.colName[i - lp.rows]) + ") has a solution (" + ((double)lp.solution[i]) + ") smaller than its lower bound (" + ((double)lowerBound[i]) + ")");

            /* abort(); */
            }
            if(lp.solution[i] > upperBound[i] + CHECK_EPS)
            {
               viewer.errorMessage("Error: variable " + (i - lp.rows) + " (" + (lp.colName[i - lp.rows]) + ") has a solution (" + ((double)lp.solution[i]) + ") larger than its upper bound (" + ((double)upperBound[i]) + ")");

            /* abort(); */
            }
         }
   } /* check_solution */

   /**
   * First of all: copy the arrays upperBound and lowerBound
   * to the pointers of Upbo and Lowbo. (Memory
   * is allocated for these arrays. Pointers point
   * to lp.upperBound and lp.lowerBound)
   * (size of memory is updated, if new columns are
   * added.)
   * These arrays came from solve() as ORIGINAL
   * bounds. Therefore no shifting of transformed bounds
   * necessary in lpkit.c if solve() is called.
   *
   * if (lp.antiDegen)
   * disturb lower and upper bound a little bit.
   * if (!lp.etaValid)
   * shift lower bounds to zero. This means:
   * Orig_lowerBound   ... unchanged
   * Orig_upperBound    ... unchanged
   * lowerBound        ... unchanged (implicit in code = 0)
   * upperBound         ... mainly upperBound_old - lowerBound.
   *
   * solveLp()
   *
   * if (lp.antiDegen)
   * restore upperBound, lowerBound, Orig_rh and solve again.
   *
   * if (OPTIMAL solution of LP)
   * check, if we can cutoff branch with LP value.
   * look for noninteger variable (look for first
   * or look random)
   * if (noninteger variables)
   * setup two new problems.
   * Malloc new memory
   * memcpy the data
   * solve problems recursively (Floor_first/ceiling_irst)
   * set return values
   * else
   * (all required values are int)
   * check, if better solution found.
   * (Yes)
   * memcpy data
   * perhaps break B+B
   *
   *
   * Recursive Function. Pure depth first search.
   * No easily accessible nodelist, because of depth
   * first search. (Also less active nodes)
   * Branching on first noninteger variablen or
   * on a randomly selected variable.
   * Avoid inverting if possible.
   */
   private int milpsolve(LpModel lp, double[] upperBound, double[] lowerBound, int[] sbasis, int[] slower, int[] sbas, int recursive) throws Exception
   {
      int i, j, failure, is_worse;
      int notint = Integer.MIN_VALUE;
      double theta, tmpreal;
      Random rdm = new Random();
      if(Break_bb != FALSE)
         return (BREAK_BB);
      Level++;
      lp.totalNodes++;
      if(Level > lp.maxLevel)
         lp.maxLevel = Level;
      lp.debugPrint(Level,"starting solve");

      /* make fresh copies of upperBound, lowerBound, rh as solving changes them */

      //memcpy(lp.upperBound,  upperBound,    (lp.sum + 1)  * sizeof(REAL));
      System.arraycopy(upperBound, 0, lp.upperBound, 0, lp.sum + 1);

      //memcpy(lp.lowerBound, lowerBound,   (lp.sum + 1)  * sizeof(REAL));
      System.arraycopy(lowerBound, 0, lp.lowerBound, 0, lp.sum + 1);

      //memcpy(lp.rh,    lp.origRh, (lp.rows + 1) * sizeof(REAL));
      System.arraycopy(lp.origRh, 0, lp.rh, 0, lp.rows + 1);

      /* make shure we do not do memcpy(lp.basis, lp.basis ...) ! */
      if(recursive != FALSE)
      {

         //memcpy(lp.basis, sbasis,  (lp.sum + 1)  * sizeof(short));
         System.arraycopy(sbasis, 0, lp.basis, 0, lp.sum + 1);

         //memcpy(lp.lower, slower,  (lp.sum + 1)  * sizeof(short));
         System.arraycopy(slower, 0, lp.lower, 0, lp.sum + 1);

         //memcpy(lp.bas,   sbas,    (lp.rows + 1) * sizeof(int));
         System.arraycopy(sbas, 0, lp.bas, 0, lp.rows + 1);
      }
      if(lp.antiDegen != FALSE)
      { /* randomly disturb bounds */
         for(i = 1;i <= lp.columns;i++)
         {
            tmpreal = rdm.nextDouble() * 0.001;
            ;
            if(tmpreal > lp.epsb)
               lp.lowerBound[i + lp.rows] -= tmpreal;
            tmpreal = rdm.nextDouble() * 0.001;
            ;
            if(tmpreal > lp.epsb)
               lp.upperBound[i + lp.rows] += tmpreal;
         }
         lp.etaValid = FALSE;
      }
      if(!(lp.etaValid != FALSE))
      {

         /* transform to all lower bounds to zero */
         for(i = 1;i <= lp.columns;i++)
            if((theta = lp.lowerBound[lp.rows + i]) != 0)
            {
               if(lp.upperBound[lp.rows + i] < lp.infinite)
                  lp.upperBound[lp.rows + i] -= theta;
               for(j = lp.colEnd[i - 1];j < lp.colEnd[i];j++)
                  lp.rh[lp.mat[j].rowNr] -= theta * lp.mat[j].value;
            }
         invert(lp);
         lp.etaValid = TRUE;
      }
      failure = solveLp(lp);
      if((lp.antiDegen != FALSE) && (failure == OPTIMAL))
      {

         /* restore to original problem, solve again starting from the basis found
         for the disturbed problem */

         /* restore original problem */

         //memcpy(lp.upperBound,  upperBound,        (lp.sum + 1)  * sizeof(REAL));
         System.arraycopy(upperBound, 0, lp.upperBound, 0, lp.sum + 1);

         //memcpy(lp.lowerBound, lowerBound,       (lp.sum + 1)  * sizeof(REAL));
         System.arraycopy(lowerBound, 0, lp.lowerBound, 0, lp.sum + 1);

         //memcpy(lp.rh,    lp.origRh, (lp.rows + 1) * sizeof(REAL));
         System.arraycopy(lp.origRh, 0, lp.rh, 0, lp.rows + 1);

         /* transform to all lower bounds zero */
         for(i = 1;i <= lp.columns;i++)
            if((theta = lp.lowerBound[lp.rows + i]) != 0)
            {
               if(lp.upperBound[lp.rows + i] < lp.infinite)
                  lp.upperBound[lp.rows + i] -= theta;
               for(j = lp.colEnd[i - 1];j < lp.colEnd[i];j++)
                  lp.rh[lp.mat[j].rowNr] -= theta * lp.mat[j].value;
            }
         invert(lp);
         lp.etaValid = TRUE;
         failure = solveLp(lp); /* and solve again */
      }
      if(failure != OPTIMAL)
         lp.debugPrint(Level, "this problem has no solution, it is "+((failure == UNBOUNDED) ? "unbounded" : "infeasible"));
      if(failure == INFEASIBLE && (lp.verbose != FALSE))
         print("level" + Level + " INF\n");
      if(failure == OPTIMAL)
      { /* there is a good solution */
         constructSolution(lp);

         /* because of reports of solution > upperBound */

         /* check_solution(lp, upperBound, lowerBound); get too many hits ?? */
         lp.debugPrint(Level,"a solution was found");
         lp.debugPrintSolution(Level);

         /* if this solution is worse than the best sofar, this branch must die */

         /* if we can only have integer OF values, we might consider requiring to
         be at least 1 better than the best sofar, MB */
         if(lp.maximise!=FALSE)
            is_worse = lp.solution[0] <= lp.bestSolution[0] ? TRUE : FALSE;
         else /* minimising! */
            is_worse = lp.solution[0] >= lp.bestSolution[0] ? TRUE : FALSE;
         if(is_worse!=FALSE)
         {
            if(lp.verbose != FALSE)
               println("level " + Level + " OPT NOB value " + lp.solution[0] + " bound " + lp.bestSolution[0]);
            lp.debugPrint(Level,"but it was worse than the best sofar, discarded");
            Level--;
            return (MILP_FAIL);
         }

         /* check if solution contains enough ints */
         if(lp.bbRule == FIRST_NI)
         {
            for(notint = 0,i = lp.rows + 1;i <= lp.sum && notint == 0;i++)
            {
               if((lp.mustBeInt[i] != FALSE) && !(isInt(lp, i) != FALSE))
               {
                  if(lowerBound[i] == upperBound[i])
                  { /* this var is already fixed */
                     System.err.println("Warning: integer var " + (i - lp.rows) + " is already fixed at " + lowerBound[i] + ", but has non-integer value " + lp.solution[i]);
                     System.err.println("Perhaps the -e option should be used");
                  }
                  else
                     notint = i;
               }
            }
         }
         if(lp.bbRule == RAND_NI)
         {
            int nr_not_int, select_not_int;
            nr_not_int = 0;
            for(i = lp.rows + 1;i <= lp.sum;i++)
               if((lp.mustBeInt[i] != FALSE) && !(isInt(lp, i) != FALSE))
                  nr_not_int++;
            if(nr_not_int == 0)
               notint = 0;
            else
            {
               select_not_int = (rdm.nextInt() % nr_not_int) + 1;
               i = lp.rows + 1;
               while(select_not_int > 0)
               {
                  if((lp.mustBeInt[i] != FALSE) && !(isInt(lp, i) != FALSE))
                     select_not_int--;
                  i++;
               }
               notint = i - 1;
            }
         }
         if (notint == Integer.MIN_VALUE)
            throw(new Exception("notint is not being initialized!"));
         if(lp.verbose != FALSE)
         {
            if(notint != FALSE)
               println("level " + Level + " OPT     value " + lp.solution[0]);
            else
               println("level " + Level + " OPT INT value " + lp.solution[0]);
         }
         if(notint != FALSE)
         { /* there is at least one value not yet int */

            /* set up two new problems */
            double[] new_upperBound, new_lowerBound;
            double new_bound;
            int[] new_lower, new_basis;
            int[] new_bas;
            int resone, restwo;

            /* allocate room for them */

            //MALLOC(new_upperBound,  lp.sum + 1);
            new_upperBound = new double[lp.sum + 1];

            //MALLOC(new_lowerBound, lp.sum + 1);
            new_lowerBound = new double[lp.sum + 1];

            //MALLOC(new_lower, lp.sum + 1);
            new_lower = new int[lp.sum + 1];

            //MALLOC(new_basis, lp.sum + 1);
            new_basis = new int[lp.sum + 1];

            //MALLOC(new_bas,   lp.rows + 1);
            new_bas = new int[lp.rows + 1];

            //memcpy(new_upperBound,  upperBound,      (lp.sum + 1)  * sizeof(REAL));
            System.arraycopy(upperBound, 0, new_upperBound, 0, lp.sum + 1);

            //memcpy(new_lowerBound, lowerBound,     (lp.sum + 1)  * sizeof(REAL));
            System.arraycopy(lowerBound, 0, new_lowerBound, 0, lp.sum + 1);

            //memcpy(new_lower, lp.lower, (lp.sum + 1)  * sizeof(short));
            System.arraycopy(lp.lower, 0, new_lower, 0, lp.sum + 1);

            //memcpy(new_basis, lp.basis, (lp.sum + 1)  * sizeof(short));
            System.arraycopy(lp.basis, 0, new_basis, 0, lp.sum + 1);

            //memcpy(new_bas,   lp.bas,   (lp.rows + 1) * sizeof(int));
            System.arraycopy(lp.bas, 0, new_bas, 0, lp.rows + 1);
            if(lp.namesUsed != FALSE)
               lp.debugPrint(Level,"not enough ints. Selecting var "+(lp.colName[notint - lp.rows])+", val: "+((double)lp.solution[notint]));
            else
               lp.debugPrint(Level,"not enough ints. Selecting Var ["+notint+"], val: "+((double)lp.solution[notint]));
            lp.debugPrint(Level,"current bounds:\n");
            lp.debugPrintBounds(Level, upperBound, lowerBound);
            if(lp.floorFirst != FALSE)
            {
               new_bound = Math.ceil(lp.solution[notint]) - 1;

               /* this bound might conflict */
               if(new_bound < lowerBound[notint])
               {
                  lp.debugPrint(Level,"New upper bound value "+((double)new_bound)+" conflicts with old lower bound "+((double)lowerBound[notint])+"\n");
                  resone = MILP_FAIL;
               }
               else
               { /* bound feasible */
                  checkIfLess(new_bound, upperBound[notint], lp.solution[notint]);
                  new_upperBound[notint] = new_bound;
                  lp.debugPrint(Level,"starting first subproblem with bounds:");
                  lp.debugPrintBounds(Level, new_upperBound, lowerBound);
                  lp.etaValid = FALSE;
                  resone = milpsolve(lp, new_upperBound, lowerBound, new_basis, new_lower, new_bas, TRUE);
                  lp.etaValid = FALSE;
               }
               new_bound += 1;
               if(new_bound > upperBound[notint])
               {
                  lp.debugPrint(Level, "New lower bound value "+((double)new_bound)+" conflicts with old upper bound "+((double)upperBound[notint])+"\n");
                  restwo = MILP_FAIL;
               }
               else
               { /* bound feasible */
                  checkIfLess(lowerBound[notint], new_bound, lp.solution[notint]);
                  new_lowerBound[notint] = new_bound;
                  lp.debugPrint(Level, "starting second subproblem with bounds:");
                  lp.debugPrintBounds(Level, upperBound, new_lowerBound);
                  lp.etaValid = FALSE;
                  restwo = milpsolve(lp, upperBound, new_lowerBound, new_basis, new_lower, new_bas, TRUE);
                  lp.etaValid = FALSE;
               }
            }
            else
            { /* take ceiling first */
               new_bound = Math.ceil(lp.solution[notint]);

               /* this bound might conflict */
               if(new_bound > upperBound[notint])
               {
                  lp.debugPrint(Level, "New lower bound value "+((double)new_bound)+" conflicts with old upper bound "+((double)upperBound[notint])+"\n");
                  resone = MILP_FAIL;
               }
               else
               { /* bound feasible */
                  checkIfLess(lowerBound[notint], new_bound, lp.solution[notint]);
                  new_lowerBound[notint] = new_bound;
                  lp.debugPrint(Level, "starting first subproblem with bounds:");
                  lp.debugPrintBounds(Level, upperBound, new_lowerBound);
                  lp.etaValid = FALSE;
                  resone = milpsolve(lp, upperBound, new_lowerBound, new_basis, new_lower, new_bas, TRUE);
                  lp.etaValid = FALSE;
               }
               new_bound -= 1;
               if(new_bound < lowerBound[notint])
               {
                  lp.debugPrint(Level, "New upper bound value "+((double)new_bound)+" conflicts with old lower bound "+((double)lowerBound[notint])+"\n");
                  restwo = MILP_FAIL;
               }
               else
               { /* bound feasible */
                  checkIfLess(new_bound, upperBound[notint], lp.solution[notint]);
                  new_upperBound[notint] = new_bound;
                  lp.debugPrint(Level, "starting second subproblem with bounds:");
                  lp.debugPrintBounds(Level, new_upperBound, lowerBound);
                  lp.etaValid = FALSE;
                  restwo = milpsolve(lp, new_upperBound, lowerBound, new_basis, new_lower, new_bas, TRUE);
                  lp.etaValid = FALSE;
               }
            }
            if(resone != FALSE && restwo != FALSE) /* both failed and must have been infeasible */
               failure = INFEASIBLE;
            else
               failure = OPTIMAL;
         }
         else
         { /* all required values are int */
            lp.debugPrint(Level, "--> valid solution found");
            if(lp.maximise != FALSE)
               is_worse = (lp.solution[0] < lp.bestSolution[0]) ? TRUE : FALSE;
            else
               is_worse = (lp.solution[0] > lp.bestSolution[0]) ? TRUE : FALSE;
            if(!(is_worse != FALSE))
            { /* Current solution better */
               if((lp.debug != FALSE) || ((lp.verbose != FALSE) && !(lp.printSolution != FALSE)))
                  print("*** new best solution: old: " + ((double)lp.bestSolution[0]) + ", new: " + ((double)lp.solution[0]) + " ***\n");

               //memcpy(lp.bestSolution, lp.solution, (lp.sum + 1) * sizeof(REAL));
               System.arraycopy(lp.solution, 0, lp.bestSolution, 0, lp.sum + 1);
               calculateDuals(lp);
               if(lp.printSolution != FALSE)
                  lp.printSolution();
               if(lp.breakAtInt != FALSE)
               {
                  if((lp.maximise != FALSE) && (lp.bestSolution[0] > lp.breakValue))
                     Break_bb = TRUE;
                  if(!(lp.maximise != FALSE) && (lp.bestSolution[0] < lp.breakValue))
                     Break_bb = TRUE;
               }
            }
         }
      }
      Level--;

      /* failure can have the values OPTIMAL, UNBOUNDED and INFEASIBLE. */
      return (failure);
   } /* milpsolve */

   public int solve() throws Exception
   {
      return solve(model);
   }
   /**
   * Init BEST-Solution, init perhaps basis, call milpsolve.
   */
   private int solve(LpModel lp) throws Exception
   {
      int result, i;
      lp.totalIter = 0;
      lp.maxLevel = 1;
      lp.totalNodes = 0;
      if(lp.isValid() != FALSE)
      {
         if((lp.maximise != FALSE) && (lp.objBound == lp.infinite))
            lp.bestSolution[0] = -lp.infinite;
         else
            if(!(lp.maximise != FALSE) && (lp.objBound == -lp.infinite))
               lp.bestSolution[0] = lp.infinite;
            else
               lp.bestSolution[0] = lp.objBound;
         Level = 0;
         if(!(lp.basisValid != FALSE))
         {
            for(i = 0;i <= lp.rows;i++)
            {
               lp.basis[i] = TRUE;
               lp.bas[i] = i;
            }
            for(i = lp.rows + 1;i <= lp.sum;i++)
               lp.basis[i] = FALSE;
            for(i = 0;i <= lp.sum;i++)
               lp.lower[i] = TRUE;
            lp.basisValid = TRUE;
         }
         lp.etaValid = FALSE;
         Break_bb = FALSE;
         result = milpsolve(lp, lp.origUpperBound, lp.origLowerBound, lp.basis, lp.lower, lp.bas, FALSE);
         return (result);
      }

      /* if we get here, isvalid(lp) failed. I suggest we return FAILURE */
      print("Error, the current LP seems to be invalid\n");
      return (FAILURE);
   } /* solve */

   /**
    * Lagrangian solver.
    */
   int lagrangianSolve(LpModel lp, double start_bound, int num_iter, int verbose) throws Exception
   {
      int i, j, result, citer;
      int status, OrigFeas, AnyFeas, same_basis;
      double[] OrigObj, ModObj, SubGrad, BestFeasSol;
      double Zub, Zlb, Ztmp, pie;
      double rhsmod, Step, SqrsumSubGrad;
      int[] old_bas;
      int[] old_lower;

      /* allocate mem */

      //MALLOC(OrigObj, lp->columns + 1);
      OrigObj = new double[lp.columns + 1];

      //CALLOC(ModObj, lp->columns + 1);
      ModObj = new double[lp.columns + 1];

      //CALLOC(SubGrad, lp->nrLagrange);
      SubGrad = new double[lp.nrLagrange];

      //CALLOC(BestFeasSol, lp->sum + 1);
      BestFeasSol = new double[lp.sum + 1];

      //MALLOCCPY(old_bas, lp->bas, lp->rows + 1);
      old_bas = new int[lp.rows];
      System.arraycopy(lp.bas, 0, old_bas, 0, lp.rows + 1);

      //MALLOCCPY(old_lower, lp->lower, lp->sum + 1);
      old_lower = new int[lp.sum + 1];
      System.arraycopy(lp.lower, 0, old_lower, 0, lp.rows + 1);
      lp.getRow(0, OrigObj);
      pie = 2;
      if(lp.maximise!=FALSE)
      {
         Zub = DEF_INFINITE;
         Zlb = start_bound;
      }
      else
      {
         Zlb = -DEF_INFINITE;
         Zub = start_bound;
      }
      status = RUNNING;
      Step = 1;
      OrigFeas = FALSE;
      AnyFeas = FALSE;
      citer = 0;
      for(i = 0;i < lp.nrLagrange;i++)
         lp.lambda[i] = 0;
      while(status == RUNNING)
      {
         citer++;
         for(i = 1;i <= lp.columns;i++)
         {
            ModObj[i] = OrigObj[i];
            for(j = 0;j < lp.nrLagrange;j++)
            {
               if(lp.maximise!=FALSE)
                  ModObj[i] -= lp.lambda[j] * lp.lagRow[j][i];
               else
                  ModObj[i] += lp.lambda[j] * lp.lagRow[j][i];
            }
         }
         for(i = 1;i <= lp.columns;i++)
            lp.setMatrixElement(0, i, ModObj[i]);
         rhsmod = 0;
         for(i = 0;i < lp.nrLagrange;i++)
            if(lp.maximise!=FALSE)
               rhsmod += lp.lambda[i] * lp.lagRhs[i];
            else
               rhsmod -= lp.lambda[i] * lp.lagRhs[i];
         if(verbose != FALSE)
         {
            println("Zub: " + Zub + " Zlb: " + Zlb + " Step: " + Step + " pie: " + pie + " Feas " + OrigFeas);
            for(i = 0;i < lp.nrLagrange;i++)
               println(i + " SubGrad " + SubGrad[i] + " lambda " + lp.lambda[i]);
         }
         if((verbose != FALSE) && lp.sum < 20)
            lp.printLp();
         result = solve(lp);
         if((verbose != FALSE) && lp.sum < 20)
            lp.printSolution();
         same_basis = TRUE;
         i = 1;
         while((same_basis!=FALSE) && i < lp.rows)
         {
            same_basis = (old_bas[i] == lp.bas[i]) ? TRUE : FALSE;
            i++;
         }
         i = 1;
         while((same_basis!=FALSE) && i < lp.sum)
         {
            same_basis = (old_lower[i] == lp.lower[i]) ? TRUE : FALSE;
            i++;
         }
         if(!(same_basis!=FALSE))
         {

            //memcpy(old_lower, lp->lower, (lp->sum+1) * sizeof(short));
            System.arraycopy(lp.lower, 0, old_lower, 0, lp.sum + 1);

            //memcpy(old_bas, lp->bas, (lp->rows+1) * sizeof(int));
            System.arraycopy(lp.bas, 0, old_bas, 0, lp.rows + 1);
            pie *= 0.95;
         }
         if(verbose!=FALSE)
            println("result: " + result + "  same basis: " + same_basis);
         if(result == UNBOUNDED)
         {
            for(i = 1;i <= lp.columns;i++)
               print(ModObj[i] + " ");

            //exit(EXIT_FAILURE);
            throw (new Exception("Unbounded!"));
         }
         if(result == FAILURE)
            status = FAILURE;
         if(result == INFEASIBLE)
            status = INFEASIBLE;
         SqrsumSubGrad = 0;
         for(i = 0;i < lp.nrLagrange;i++)
         {
            SubGrad[i] = -lp.lagRhs[i];
            for(j = 1;j <= lp.columns;j++)
               SubGrad[i] += lp.bestSolution[lp.rows + j] * lp.lagRow[i][j];
            SqrsumSubGrad += SubGrad[i] * SubGrad[i];
         }
         OrigFeas = TRUE;
         for(i = 0;i < lp.nrLagrange;i++)
            if(lp.lagConType[i]!=FALSE)
            {
               if(Math.abs(SubGrad[i]) > lp.epsb)
                  OrigFeas = FALSE;
            }
            else
               if(SubGrad[i] > lp.epsb)
                  OrigFeas = FALSE;
         if(OrigFeas!=FALSE)
         {
            AnyFeas = TRUE;
            Ztmp = 0;
            for(i = 1;i <= lp.columns;i++)
               Ztmp += lp.bestSolution[lp.rows + i] * OrigObj[i];
            if((lp.maximise!=FALSE) && (Ztmp > Zlb))
            {
               Zlb = Ztmp;
               for(i = 1;i <= lp.sum;i++)
                  BestFeasSol[i] = lp.bestSolution[i];
               BestFeasSol[0] = Zlb;
               if(verbose != FALSE)
                  print("Best feasible solution: " + ((double)Zlb) + "\n");
            }
            else
               if(Ztmp < Zub)
               {
                  Zub = Ztmp;
                  for(i = 1;i <= lp.sum;i++)
                     BestFeasSol[i] = lp.bestSolution[i];
                  BestFeasSol[0] = Zub;
                  if(verbose != FALSE)
                     print("Best feasible solution: " + ((double)Zub) + "\n");
               }
         }
         if(lp.maximise!=FALSE)
            Zub = Math.min(Zub, rhsmod + lp.bestSolution[0]);
         else
            Zlb = Math.max(Zlb, rhsmod + lp.bestSolution[0]);
         if(Math.abs(Zub - Zlb) < 0.001)
            status = OPTIMAL;
         Step = pie * ((1.05 * Zub) - Zlb) / SqrsumSubGrad;
         for(i = 0;i < lp.nrLagrange;i++)
         {
            lp.lambda[i] += Step * SubGrad[i];
            if(!(lp.lagConType[i] != FALSE) && (lp.lambda[i] < 0))
               lp.lambda[i] = 0;
         }
         if(citer == num_iter && status == RUNNING)
         {
            if(AnyFeas != FALSE)
               status = FEAS_FOUND;
            else
               status = NO_FEAS_FOUND;
         }
      }
      for(i = 0; i <= lp.sum;i++)
         lp.bestSolution[i] = BestFeasSol[i];
      for(i = 1;i <= lp.columns;i++)
         lp.setMatrixElement(0, i, OrigObj[i]);
      if(lp.maximise!=FALSE)
         lp.lagBound = Zub;
      else
         lp.lagBound = Zlb;
      return (status);
   }

   void println(String str)
   {
      if (viewer!=null)
         viewer.messageln(str);
      else
         System.out.println(str);
   }
   void print(String str)
   {
      if (viewer!=null)
         viewer.message(str);
      else
         System.out.print(str);
   }

}

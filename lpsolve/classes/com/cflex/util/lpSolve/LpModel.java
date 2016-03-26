package com.cflex.util.lpSolve;

import java.util.*;
import java.lang.*;
import java.io.*;

/**
* @author Rodrigo Almeida Gonçalves
*/
public class LpModel implements LpConstant
{
   public SolverListener viewer;

   /** the name of the lp */
   String lpName;

   /** ## Verbose flag */
   public int verbose;

   /** ## PrintDuals flag for PrintSolution */
   int printDuals;

   /** ## used in lp_solve */
   int printSolution;

   /** ## Print B&B information */
   public int debug;

   /** ## Print information at every reinversion */
   int printAtInvert;

   /** ## Print information on pivot selection */
   public int trace;

   /** ## Do perturbations */
   int antiDegen;

   /** perform matrix presolving */
   int doPresolve;

   /** Nr of constraint rows in the problem */
   int rows;

   /** The allocated memory for Rows sized data */
   int rowsAlloc;

   /** The number of columns (= variables) */
   int columns;
   int columnsAlloc;

   /** The size of the variables + the slacks */
   int sum;
   int sumAlloc;

   /** Flag to indicate if names for rows and columns are used */
   int namesUsed;

   /** rowsAlloc+1 */
   String[] rowName;

   /** columnsAlloc+1 */
   String[] colName;

   /** Row[0] of the sparce matrix is the objective function */

   /** The number of elements in the sparce matrix*/
   int nonZeros;

   /** The allocated size for matrix sized structures */
   int matAlloc;
   LpMatrec[] mat; /* matAlloc :The sparse matrix */

   /** columnsAlloc+1 :Cend[i] is the index of the first element after column i.
    column[i] is stored in elements
    colEnd[i-1] to colEnd[i]-1 */
   int[] colEnd;

   /* matAlloc :From Row 1 on, colNo contains the column nr. of the nonzero elements, row by row */
   int[] colNo;

   /** true if rowEnd & colNo are valid */
   int rowEndValid;

   /* rowsAlloc+1 :rowEnd[i] is the index of the first element in Colno after row i */
   int[] rowEnd;

   /** rowsAlloc+1 :The RHS after scaling & sign changing, but before `Bound transformation' */
   double[] origRh;

   /** rowsAlloc+1 :As origRh, but after Bound transformation */
   double[] rh;

   /** rowsAlloc+1 :The RHS of the current simplex tableau */
   double[] rhs;

   /** sumAlloc+1 :TRUE if variable must be integer */
   int[] mustBeInt;

   /* sumAlloc+1 :Bound before transformations */
   double[] origUpperBound;
   double[] origLowerBound;

   /*  " " :Upper bound after transformation & B&B work */
   double[] upperBound;

   /* Lower bound after transformation & B&B work */
   double[] lowerBound;
   int basisValid; /* TRUE is the basis is still valid */
   int[] bas; /* rowsAlloc+1 :The basis column list */
   int[] basis; /* sumAlloc+1 : basis[i] is TRUE if the column
               is in the basis */
   int[] lower; /*  "       "  :TRUE is the variable is at its
                lower bound (or in the basis), it is FALSE
                if the variable is at its upper bound */
   int etaValid; /* TRUE if current Eta structures are valid */
   int etaAlloc; /* The allocated memory for Eta */
   int etaSize; /* The number of Eta columns */
   int numInv; /* The number of real pivots */
   int maxNumInv; /* ## The number of real pivots between
                   reinversions */
   double[] etaValue; /* etaAlloc :The Structure containing the
                      values of Eta */
   int[] etaRowNr; /*  "     "  :The Structure containing the Row
                    indexes of Eta */
   int[] etaColEnd; /* rowsAlloc + MaxNumInv : etaColEnd[i] is
                     the start index of the next Eta column */
   int bbRule; /* what rule for selecting B&B variables */
   int breakAtInt; /* TRUE if stop at first integer better than
                    break_value */
   double breakValue;
   double objBound; /* ## Objective function bound for speedup of
                    B&B */
   int iter; /* The number of iterations in the simplex
            solver (LP) */
   int totalIter; /* The total number of iterations (B&B)
                  (ILP) */
   int maxLevel; /* The Deepest B&B level of the last solution */
   int totalNodes; /* total number of nodes processed in b&b */
   double[] solution; /* sumAlloc+1 :The Solution of the last LP,
                      0 = The Optimal Value,
                      1..rows The Slacks,
                      rows+1..sum The Variables */
   double[] bestSolution; /*  "       "  :The Best 'Integer' Solution */
   double[] duals; /* rowsAlloc+1 :The dual variables of the
                  last LP */
   int maximise; /* TRUE if the goal is to maximise the
                objective function */
   int floorFirst; /* TRUE if B&B does floor bound first */
   int[] changedSign; /* rowsAlloc+1 :TRUE if the Row in the matrix
                  has changed sign
                  (a`x > b, x>=0) is translated to
                  s + -a`x = -b with x>=0, s>=0) */
   int scalingUsed; /* TRUE if scaling is used */
   int columnsScaled; /* TRUE is the columns are scaled too, Only use
                      if all variables are non-integer */
   double[] scale; /* sumAlloc+1:0..Rows the scaling of the Rows,
                  Rows+1..Sum the scaling of the columns */
   int nrLagrange; /* Nr. of Langrangian relaxation constraints */
   double[][] lagRow; /* NumLagrange, columns+1:Pointer to pointer of
                      rows */
   double[] lagRhs; /* NumLagrange :Pointer to pointer of Rhs */
   double[] lambda; /* NumLagrange :Lambda Values */
   int[] lagConType; /* NumLagrange :TRUE if constraint type EQ */
   double lagBound; /* the lagrangian lower bound */
   int valid; /* Has this lp pased the 'test' */
   double infinite; /* ## numerical stuff */
   double epsilon; /* ## */
   double epsb; /* ## */
   double epsd; /* ## */
   double epsel; /* ## */
   HashMap rowNameHashtable; /* hash table to store row names */
   HashMap colNameHashtable; /* hash table to store column names */


   /**
   * Construct a new LP. Set all variables to some default values.
   * The LP has "rows" rows and "columns" columns. The matrix contains
   * no values, but space for one value. All arrays which depend on
   * "rows" and "columns" are malloced.
   *
   * The problem contains only continuous variables.
   * Upper bounds are infinity, lower bounds are zero.
   * The basis is true, all rows are in basis. All columns are nonbasic.
   * The eta-file is valid. Solution, bestSolution and duals are Zero.
   * And some other default values.
   */
   public LpModel(int nrows, int ncolumns) throws Exception
   {
      int i, sum;
      if(nrows < 0 || ncolumns < 0)
         throw (new Exception("rows < 0 or columns < 0"));
      sum = nrows + ncolumns;
      this.sum = sum;
      this.rows = nrows;
      this.columns = ncolumns;
      this.lpName = "unnamed";
      this.verbose = FALSE;
      this.printDuals = FALSE;
      this.printSolution = FALSE;
      this.debug = FALSE;
      this.printAtInvert = FALSE;
      this.trace = FALSE;
      this.rowsAlloc = rows;
      this.columnsAlloc = columns;
      this.sumAlloc = sum;
      this.namesUsed = FALSE;
      this.objBound = DEF_INFINITE;
      this.infinite = DEF_INFINITE;
      this.epsilon = DEF_EPSILON;
      this.epsb = DEF_EPSB;
      this.epsd = DEF_EPSD;
      this.epsel = DEF_EPSEL;
      this.nonZeros = 0;
      this.matAlloc = 1;
      this.mat = new LpMatrec[matAlloc];
      for(i = 0;i < matAlloc;i++)
         mat[i] = new LpMatrec(0, 0);
      colNo = new int[matAlloc];
      Arrays.fill(colNo, 0);
      colEnd = new int[columns + 1];
      Arrays.fill(colEnd, 0);
      rowEnd = new int[rows + 1];
      Arrays.fill(rowEnd, 0);
      this.rowEndValid = FALSE;
      origRh = new double[rows + 1];
      Arrays.fill(origRh, 0);
      rh = new double[rows + 1];
      Arrays.fill(rh, 0);
      rhs = new double[rows + 1];
      Arrays.fill(rhs, 0);
      mustBeInt = new int[sum + 1];
      Arrays.fill(mustBeInt, FALSE);
      origUpperBound = new double[sum + 1];
      Arrays.fill(origUpperBound, infinite);
      upperBound = new double[sum + 1];
      Arrays.fill(upperBound, 0);
      origLowerBound = new double[sum + 1];
      Arrays.fill(origLowerBound, 0);
      lowerBound = new double[sum + 1];
      Arrays.fill(lowerBound, 0);
      this.basisValid = TRUE;
      bas = new int[rows + 1];
      basis = new int[sum + 1];
      lower = new int[sum + 1];
      for(i = 0;i <= rows;i++)
      {
         bas[i] = i;
         basis[i] = TRUE;
      }
      for(i = rows + 1;i <= sum;i++)
         this.basis[i] = FALSE;
      for(i = 0;i <= sum;i++)
         this.lower[i] = TRUE;
      this.etaValid = TRUE;
      this.etaSize = 0;
      this.etaAlloc = INITIAL_MAT_SIZE;
      this.maxNumInv = DEFNUMINV;
      this.nrLagrange = 0;
      etaValue = new double[etaAlloc];
      Arrays.fill(etaValue, 0);
      etaRowNr = new int[etaAlloc];
      Arrays.fill(etaRowNr, 0);

      /* +1 reported by Christian Rank */
      etaColEnd = new int[rowsAlloc + maxNumInv + 1];
      Arrays.fill(etaColEnd, 0);
      this.bbRule = FIRST_NI;
      this.breakAtInt = FALSE;
      this.breakValue = 0;
      this.iter = 0;
      this.totalIter = 0;
      solution = new double[sum + 1];
      Arrays.fill(solution, 0);
      bestSolution = new double[sum + 1];
      Arrays.fill(bestSolution, 0);
      duals = new double[rows + 1];
      Arrays.fill(duals, 0);
      this.maximise = FALSE;
      this.floorFirst = TRUE;
      this.scalingUsed = FALSE;
      this.columnsScaled = FALSE;
      changedSign = new int[rows + 1];
      Arrays.fill(changedSign, FALSE);
      this.valid = FALSE;

      /* create two hash tables for names */
      this.rowNameHashtable = new HashMap(HASHSIZE);
      this.colNameHashtable = new HashMap(HASHSIZE);
   }

   /**
   * This copy constructor. Copies first the structure of the lp, this means especially, that all
   * the constant values are copied.
   * Copy all the arrays of the lp and set the pointers to the new arrays.
   */
   public LpModel(LpModel lp)  throws Exception
   {
      int i, rowsplus, colsplus, sumplus;
      rowsplus = rowsAlloc + 1;
      colsplus = columnsAlloc + 1;
      sumplus = sumAlloc + 1;

      /* copy all non pointers (much easier in C...)*/
      this.sum = sum;
      this.rows = rows;
      this.columns = columns;
      this.lpName = lpName;
      this.verbose = verbose;
      this.printDuals = printDuals;
      this.printSolution = printSolution;
      this.debug = debug;
      this.printAtInvert = printAtInvert;
      this.trace = trace;
      this.rowsAlloc = rowsAlloc;
      this.columnsAlloc = columnsAlloc;
      this.sumAlloc = sumAlloc;
      this.namesUsed = namesUsed;
      this.objBound = objBound;
      this.infinite = infinite;
      this.epsilon = epsilon;
      this.epsb = epsb;
      this.epsd = epsd;
      this.epsel = epsel;
      this.nonZeros = nonZeros;
      this.matAlloc = matAlloc;
      this.rowEndValid = rowEndValid;
      this.basisValid = basisValid;
      this.etaValid = etaValid;
      this.etaSize = etaSize;
      this.etaAlloc = etaAlloc;
      this.maxNumInv = maxNumInv;
      this.nrLagrange = nrLagrange;
      this.bbRule = bbRule;
      this.breakAtInt = breakAtInt;
      this.breakValue = breakValue;
      this.iter = iter;
      this.totalIter = totalIter;
      this.maximise = maximise;
      this.floorFirst = floorFirst;
      this.scalingUsed = scalingUsed;
      this.columnsScaled = columnsScaled;
      this.valid = valid;
      if(namesUsed == TRUE)
      {
         this.colName = new String[colsplus];
         System.arraycopy(colName, 0, this.colName, 0, colName.length);
         this.rowName = new String[rowsplus];
         System.arraycopy(rowName, 0, this.rowName, 0, rowName.length);
      }
      this.rowNameHashtable = (HashMap) rowNameHashtable.clone();
      this.colNameHashtable = (HashMap) colNameHashtable.clone();
      this.mat = new LpMatrec[this.matAlloc];
      System.arraycopy(mat, 0, this.mat, 0, mat.length);
      this.colEnd = new int[colsplus];
      System.arraycopy(colEnd, 0, this.colEnd, 0, colEnd.length);
      this.rowEnd = new int[rowsplus];
      System.arraycopy(rowEnd, 0, this.rowEnd, 0, rowEnd.length);
      this.origRh = new double[rowsplus];
      System.arraycopy(origRh, 0, this.origRh, 0, origRh.length);
      this.rh = new double[rowsplus];
      System.arraycopy(rh, 0, this.rh, 0, rh.length);
      this.rhs = new double[rowsplus];
      System.arraycopy(rhs, 0, this.rhs, 0, rhs.length);
      this.mustBeInt = new int[sumplus];
      System.arraycopy(mustBeInt, 0, this.mustBeInt, 0, mustBeInt.length);
      this.origUpperBound = new double[sumplus];
      System.arraycopy(origUpperBound, 0, this.origUpperBound, 0, origUpperBound.length);
      this.origLowerBound = new double[sumplus];
      System.arraycopy(origLowerBound, 0, this.origLowerBound, 0, origLowerBound.length);
      this.upperBound = new double[sumplus];
      System.arraycopy(upperBound, 0, this.upperBound, 0, upperBound.length);
      this.lowerBound = new double[sumplus];
      System.arraycopy(lowerBound, 0, this.lowerBound, 0, lowerBound.length);
      this.bas = new int[rowsplus];
      System.arraycopy(bas, 0, this.bas, 0, bas.length);
      this.basis = new int[sumplus];
      System.arraycopy(basis, 0, this.basis, 0, basis.length);
      this.lower = new int[sumplus];
      System.arraycopy(lower, 0, this.lower, 0, lower.length);
      this.etaValue = new double[etaAlloc];
      System.arraycopy(etaValue, 0, this.etaValue, 0, etaValue.length);
      this.etaRowNr = new int[etaAlloc];
      System.arraycopy(etaRowNr, 0, this.etaRowNr, 0, etaRowNr.length);
      this.solution = new double[sumplus];
      System.arraycopy(solution, 0, this.solution, 0, solution.length);
      this.bestSolution = new double[sumplus];
      System.arraycopy(bestSolution, 0, this.bestSolution, 0, bestSolution.length);
      this.duals = new double[rowsplus];
      System.arraycopy(duals, 0, this.duals, 0, duals.length);
      this.changedSign = new int[rowsplus];
      System.arraycopy(changedSign, 0, this.changedSign, 0, changedSign.length);
      this.colNo = new int[matAlloc + 1];
      System.arraycopy(colNo, 0, this.colNo, 0, colNo.length);
      this.etaColEnd = new int[rowsAlloc + maxNumInv + 1];
      System.arraycopy(etaColEnd, 0, this.etaColEnd, 0, etaColEnd.length);
      if(this.scalingUsed == TRUE)
      {
         this.scale = new double[sumplus];
         System.arraycopy(scale, 0, this.scale, 0, scale.length);
      }
      if(this.nrLagrange > 0)
      {
         this.lagRhs = new double[this.nrLagrange];
         System.arraycopy(lagRhs, 0, this.lagRhs, 0, lagRhs.length);
         this.lambda = new double[this.nrLagrange];
         System.arraycopy(lambda, 0, this.lambda, 0, lambda.length);
         this.lagConType = new int[this.nrLagrange];
         System.arraycopy(lagConType, 0, this.lagConType, 0, lagConType.length);
         this.lagRow = new double[this.nrLagrange][colsplus];
         for(i = 0;i < this.nrLagrange;i++)
            System.arraycopy(lagRow[i], 0, this.lagRow[i], 0, lagRow[i].length);
      }
   }

   /**
   * Test if realloc necessary. If yes, realloc arrays "mat" and "colNo".
   */
   public void incrementMatrixSpace(int maxextra) throws Exception
   {
      if(nonZeros + maxextra >= matAlloc)
      {

         /* let's allocate at least INITIAL_MAT_SIZE  entries */
         if(matAlloc < INITIAL_MAT_SIZE)
            matAlloc = INITIAL_MAT_SIZE;

         /* increase the size by 50% each time it becomes too small */
         while(nonZeros + maxextra >= matAlloc)
            matAlloc *= 1.5;

         //REALLOC(lp->mat, lp->matAlloc);
         int i;
         LpMatrec[] old_mat = mat;
         mat = new LpMatrec[matAlloc];
         /**@todo RODRIGO */
         //for(i = old_mat.length;i < mat.length;i++)
         //   mat[i] = new LpMatrec(0, 0); /**@todo is it really needed?? */
         System.arraycopy(old_mat, 0, mat, 0, old_mat.length);

         //REALLOC(lp->colNo, lp->matAlloc + 1);
         int[] old_colNo = colNo;
         colNo = new int[matAlloc + 1];
         System.arraycopy(old_colNo, 0, colNo, 0, old_colNo.length);
      }
   }

   /**
   * Test, if increment necessary.
   * This routine increments the space for rows with 10 additional rows.
   * Therefore one condition for correct work of this routine is that
   * it is never necessary to increase the
   * number of additionally rows in one step with more than 10!
   * Several arrays are realloced.
   */
   public void incrementRowSpace() throws Exception
   {
      if(rows > rowsAlloc)
      {
         rowsAlloc = rows + 10;
         sumAlloc = rowsAlloc + columnsAlloc;

         //REALLOC(lp->origRh, lp->rowsAlloc + 1);
         double[] db_ptr = origRh;
         origRh = new double[rowsAlloc + 1];
         System.arraycopy(db_ptr, 0, origRh, 0, db_ptr.length);

         //REALLOC(lp->rh, lp->rowsAlloc + 1);
         db_ptr = rh;
         rh = new double[rowsAlloc + 1];
         System.arraycopy(db_ptr, 0, rh, 0, db_ptr.length);

         //REALLOC(lp->rhs, lp->rowsAlloc + 1);
         db_ptr = rhs;
         rhs = new double[rowsAlloc + 1];
         System.arraycopy(db_ptr, 0, rhs, 0, db_ptr.length);

         //REALLOC(lp->origUpperBound, lp->sumAlloc + 1);
         db_ptr = origUpperBound;
         origUpperBound = new double[sumAlloc + 1];
         System.arraycopy(db_ptr, 0, origUpperBound, 0, db_ptr.length);

         //REALLOC(lp->upperBound, lp->sumAlloc + 1);
         db_ptr = upperBound;
         upperBound = new double[sumAlloc + 1];
         System.arraycopy(db_ptr, 0, upperBound, 0, db_ptr.length);

         //REALLOC(lp->origLowerBound, lp->sumAlloc + 1);
         db_ptr = origLowerBound;
         origLowerBound = new double[sumAlloc + 1];
         System.arraycopy(db_ptr, 0, origLowerBound, 0, db_ptr.length);

         //REALLOC(lp->lowerBound, lp->sumAlloc + 1);
         db_ptr = lowerBound;
         lowerBound = new double[sumAlloc + 1];
         System.arraycopy(db_ptr, 0, lowerBound, 0, db_ptr.length);

         //REALLOC(lp->solution, lp->sumAlloc + 1);
         db_ptr = solution;
         solution = new double[sumAlloc + 1];
         System.arraycopy(db_ptr, 0, solution, 0, db_ptr.length);

         //REALLOC(lp->bestSolution, lp->sumAlloc + 1);
         db_ptr = bestSolution;
         bestSolution = new double[sumAlloc + 1];
         System.arraycopy(db_ptr, 0, bestSolution, 0, db_ptr.length);

         //REALLOC(lp->rowEnd, lp->rowsAlloc + 1);
         int[] int_ptr = rowEnd;
         rowEnd = new int[rowsAlloc + 1];
         System.arraycopy(int_ptr, 0, rowEnd, 0, int_ptr.length);

         //REALLOC(lp->basis, lp->sumAlloc + 1);
         int_ptr = basis;
         basis = new int[sumAlloc + 1];
         System.arraycopy(int_ptr, 0, basis, 0, int_ptr.length);

         //REALLOC(lp->lower, lp->sumAlloc + 1);
         int_ptr = lower;
         lower = new int[sumAlloc + 1];
         System.arraycopy(int_ptr, 0, lower, 0, int_ptr.length);

         //REALLOC(lp->mustBeInt, lp->sumAlloc + 1);
         int_ptr = mustBeInt;
         mustBeInt = new int[sumAlloc + 1];
         System.arraycopy(int_ptr, 0, mustBeInt, 0, int_ptr.length);

         //REALLOC(lp->bas, lp->rowsAlloc + 1);
         int_ptr = bas;
         bas = new int[rowsAlloc + 1];
         System.arraycopy(int_ptr, 0, bas, 0, int_ptr.length);

         //REALLOC(lp->duals, lp->rowsAlloc + 1);
         db_ptr = duals;
         duals = new double[rowsAlloc + 1];
         System.arraycopy(db_ptr, 0, duals, 0, db_ptr.length);

         //REALLOC(lp->changedSign, lp->rowsAlloc + 1);
         int_ptr = changedSign;
         changedSign = new int[rowsAlloc + 1];
         System.arraycopy(int_ptr, 0, changedSign, 0, int_ptr.length);

         //REALLOC(lp->etaColEnd, lp->rowsAlloc + lp->maxNumInv + 1);
         int_ptr = etaColEnd;
         etaColEnd = new int[rowsAlloc + maxNumInv + 1];
         System.arraycopy(int_ptr, 0, etaColEnd, 0, int_ptr.length);
         if(namesUsed != FALSE)
         {

            //REALLOC(lp->rowName, lp->rowsAlloc + 1);
            String[] str_ptr = rowName;
            rowName = new String[rowsAlloc + 1];
            System.arraycopy(str_ptr, 0, rowName, 0, str_ptr.length);
         }
         if(scalingUsed != FALSE)
         {

            //REALLOC(lp->scale, lp->sumAlloc + 1);
            db_ptr = scale;
            scale = new double[sumAlloc + 1];
            System.arraycopy(db_ptr, 0, scale, 0, db_ptr.length);
         }
      }
   }

   /**
   * Similar to routine increment row space. The problems are also the same
   * Several Arrays are realloced, but no shift of values.
   */
   public void incrementColumnSpace() throws Exception
   {
      if(columns >= columnsAlloc)
      {
         int[] int_ptr;
         columnsAlloc = columns + 10;
         sumAlloc = rowsAlloc + columnsAlloc;

         //REALLOC(lp->mustBeInt, lp->sumAlloc + 1);
         int[] short_ptr = mustBeInt;
         mustBeInt = new int[sumAlloc + 1];
         System.arraycopy(short_ptr, 0, mustBeInt, 0, short_ptr.length);

         //REALLOC(lp->origUpperBound, lp->sumAlloc + 1);
         double[] double_ptr = origUpperBound;
         origUpperBound = new double[sumAlloc + 1];
         System.arraycopy(double_ptr, 0, origUpperBound, 0, double_ptr.length);

         //REALLOC(lp->upperBound, lp->sumAlloc + 1);
         double_ptr = upperBound;
         upperBound = new double[sumAlloc + 1];
         System.arraycopy(double_ptr, 0, upperBound, 0, double_ptr.length);

         //REALLOC(lp->origLowerBound, lp->sumAlloc + 1);
         double_ptr = origLowerBound;
         origLowerBound = new double[sumAlloc + 1];
         System.arraycopy(double_ptr, 0, origLowerBound, 0, double_ptr.length);

         //REALLOC(lp->lowerBound, lp->sumAlloc + 1);
         double_ptr = lowerBound;
         lowerBound = new double[sumAlloc + 1];
         System.arraycopy(double_ptr, 0, lowerBound, 0, double_ptr.length);

         //REALLOC(lp->solution, lp->sumAlloc + 1);
         double_ptr = solution;
         solution = new double[sumAlloc + 1];
         System.arraycopy(double_ptr, 0, solution, 0, double_ptr.length);

         //REALLOC(lp->bestSolution, lp->sumAlloc + 1);
         double_ptr = bestSolution;
         bestSolution = new double[sumAlloc + 1];
         System.arraycopy(double_ptr, 0, bestSolution, 0, double_ptr.length);

         //REALLOC(lp->basis, lp->sumAlloc + 1);
         short_ptr = basis;
         basis = new int[sumAlloc + 1];
         System.arraycopy(short_ptr, 0, basis, 0, short_ptr.length);

         //REALLOC(lp->lower, lp->sumAlloc + 1);
         short_ptr = lower;
         lower = new int[sumAlloc + 1];
         System.arraycopy(short_ptr, 0, lower, 0, short_ptr.length);
         if(namesUsed != FALSE)
         {

            //REALLOC(lp->colName, lp->columnsAlloc + 1);
            String[] str_ptr = colName;
            colName = new String[columnsAlloc + 1];
            System.arraycopy(str_ptr, 0, colName, 0, str_ptr.length);
         }
         if(scalingUsed != FALSE)
         {

            //REALLOC(lp->scale, lp->sumAlloc + 1);
            double_ptr = scale;
            scale = new double[sumAlloc + 1];
            System.arraycopy(double_ptr, 0, scale, 0, double_ptr.length);
         }

         //REALLOC(lp->colEnd, lp->columnsAlloc + 1);
         int_ptr = colEnd;
         colEnd = new int[columnsAlloc + 1];
         System.arraycopy(int_ptr, 0, colEnd, 0, int_ptr.length);
      }
   }

   /**
   * set one element in matrix.
   * Test, if row and column are in range. Scale value.
   * If colnum is in basis and row not objective row set Basis_valid = FALSE
   * Always set etaValid = FALSE (is this necessary?)
   *
   * Search in column for entry with correct rownumber.
   * If row found scale value again but with other expression than first time.
   * Perhaps change sign
   *
   * If row not found:
   * Increment mat_space for one additional element.
   * Shift matrix and update colEnd.
   * Set new element "row" and scale value perhaps (same problem as above)
   * Rowend is not valid any longer
   * update number of nonzeros and copy this value if lp is active
   */
   public void setMatrixElement(int Row, int Column, double Value) throws Exception
   {
      int elmnr, lastelm, i;

      /* This function is very inefficient if used to add new matrix entries in
      other places than at the end of the matrix. OK for replacing existing
      non-zero values */
      if(Row > rows || Row < 0)
         throw (new Exception("Row out of range"));
      if(Column > columns || Column < 1)
         throw (new Exception("Column out of range"));

      /* scaling is performed twice? MB */
      if(scalingUsed != FALSE)
         Value *= scale[Row] * scale[rows + Column];
      if(basis[Column] == TRUE && Row > 0)
         basisValid = FALSE;
      etaValid = FALSE;

      /* find out if we already have such an entry */
      elmnr = colEnd[Column - 1];
      while((elmnr < colEnd[Column]) && (mat[elmnr].rowNr != Row))
         elmnr++;
      if((elmnr != colEnd[Column]) && (mat[elmnr].rowNr == Row))
      {

         /* there is an existing entry */
         if(Math.abs(Value) > epsilon)
         {

            /* we replace it by something non-zero */
            if(scalingUsed != FALSE)
            {
               if(changedSign[Row]!=FALSE)
                  mat[elmnr].value = -Value * scale[Row] * scale[Column];
               else
                  mat[elmnr].value = Value * scale[Row] * scale[Column];
            }
            else
            {

               /* no scaling */
               if(changedSign[Row] != FALSE)
                  mat[elmnr].value = -Value;
               else
                  mat[elmnr].value = Value;
            }
         }
         else
         {

            /* setting existing non-zero entry to zero. Remove the entry */

            /* this might remove an entire column, or leave just a bound. No
            nice solution for that yet */

            /* Shift the matrix */
            lastelm = nonZeros;
            for(i = elmnr;i < lastelm;i++)
               mat[i] = mat[i + 1];
            for(i = Column;i <= columns;i++)
               colEnd[i]--;
            nonZeros--;
         }
      }
      else
         if(Math.abs(Value) > epsilon)
         {

            /* no existing entry. make new one only if not nearly zero */

            /* check if more space is needed for matrix */
            incrementMatrixSpace(1);

            /* Shift the matrix */
            lastelm = nonZeros;
            for(i = lastelm;i > elmnr;i--)
               mat[i] = mat[i - 1];
            for(i = Column;i <= columns;i++)
               colEnd[i]++;

            /* Set new element */
            if (mat[elmnr]==null)
               mat[elmnr] = new LpMatrec(0, (double)0); //by R.
            mat[elmnr].rowNr = Row;
            if(scalingUsed != FALSE)
            {
               if(changedSign[Row] != FALSE)
                  mat[elmnr].value = -Value * scale[Row] * scale[Column];
               else
                  mat[elmnr].value = Value * scale[Row] * scale[Column];
            }
            else /* no scaling */
            {
               if(changedSign[Row] != FALSE)
                  mat[elmnr].value = -Value;
               else
                  mat[elmnr].value = Value;
            }
            rowEndValid = FALSE;
            nonZeros++;
         }
   }

   /**
   * call in one loop for dense row the function set_mat().
   * No test is done, if we want to include Elements with value "0".
   * These values are included into the matrix!
   */
   public void setObjFn(double[] row) throws Exception
   {
      int i;
      for(i = 1;i <= columns;i++)
         setMatrixElement(0, i, row[i]);
   }

   /**
   * reserve space for one row
   * try with "strtod()" to change all the strings to real values
   * call set_obj_fn()
   * free space
   */
   public void strSetObjFn(String row) throws Exception
   {
      double[] arow;
      arow = new double[columns + 1];
      int i = 0;
      StringTokenizer stk = new StringTokenizer(row);
      while(stk.hasMoreTokens() && i < columns)
      {
         i++;
         arow[i] = Double.valueOf(stk.nextToken()).doubleValue();
      }
      if(i < columns)
         throw (new Exception("Not enough numbers in the string in str_set_obj_fn"));
      setObjFn(arow);
   }

   /**
   * first reserve space for integers for length of one row.
   * Mark all the positions, which contain nonzeros and update nonZeros
   * malloc space for a complete new matrix
   * increment matrix space by null??
   * rows++
   * sum++
   * increment row space
   * if scaling
   * shift the values
   * and set scaling value for new row to 1
   * if names used
   * invent new name for row
   * if columns are scaled
   * scale coefficients
   * calculate change_sign
   * copy every column from old matrix to new matrix. Perhaps add new entry for
   * new row.
   * Update colEnd
   * copy new matrix back to old matrix.
   * free the allocated arrays
   *
   * shift orig_upper_bounds
   * orig_lower_bounds
   * basis
   * lower
   * mustBeInt
   *
   * update Basis info
   * set bounds for slack variables
   *
   * change_sign for rhs, but comparison is made with sense of constraint.
   *
   * rows_end_valid = false
   * put slackvariable for this row into basis
   * if lp == active, set globals
   * eta_file = non_valid.
   */
   public void addConstraint(double[] row, int constr_type, double rh) throws Exception
   {
      LpMatrec[] newmat;
      int i, j;
      int elmnr;
      int stcol;
      int[] addtoo;
      addtoo = new int[columns + 1];
      for(i = 1;i <= columns;i++)
         if(row[i] != 0)
         {
            addtoo[i] = TRUE;
            nonZeros++;
         }
         else
            addtoo[i] = FALSE;

      //MALLOC(newmat, lp->nonZeros);
      newmat = new LpMatrec[nonZeros];
      for(i = 0;i < newmat.length;i++)
         newmat[i] = new LpMatrec(0, (double)0);
      incrementMatrixSpace(0);
      rows++;
      sum++;
      incrementRowSpace();
      if(scalingUsed != FALSE)
      {

         /* shift scale */
         for(i = sum;i > rows;i--)
            scale[i] = scale[i - 1];
         scale[rows] = 1;
      }
      if(namesUsed != FALSE)
         rowName[rows] = new String("r_" + rows);
      if(scalingUsed != FALSE && columnsScaled != FALSE)
         for(i = 1;i <= columns;i++)
            row[i] *= scale[rows + i];
      if(constr_type == GE)
         changedSign[rows] = TRUE;
      else
         changedSign[rows] = FALSE;
      elmnr = 0;
      stcol = 0;
      for(i = 1;i <= columns;i++)
      {
         for(j = stcol;j < colEnd[i];j++)
         {
            newmat[elmnr].rowNr = mat[j].rowNr;
            newmat[elmnr].value = mat[j].value;
            elmnr++;
         }
         if(addtoo[i] != FALSE)
         {
            if(changedSign[rows] != FALSE)
               newmat[elmnr].value = -row[i];
            else
               newmat[elmnr].value = row[i];
            newmat[elmnr].rowNr = rows;
            elmnr++;
         }
         stcol = colEnd[i];
         colEnd[i] = elmnr;
      }

      //memcpy(lp->mat, newmat, lp->nonZeros * sizeof(matrec));
      System.arraycopy(newmat,0,mat,0,newmat.length);

      for(i = sum;i > rows;i--)
      {
         origUpperBound[i] = origUpperBound[i - 1];
         origLowerBound[i] = origLowerBound[i - 1];
         basis[i] = basis[i - 1];
         lower[i] = lower[i - 1];
         mustBeInt[i] = mustBeInt[i - 1];
      }

      /* changed from i <= lp->rows to i < lp->rows, MB */
      for(i = 1;i <= rows;i++)
         if(bas[i] >= rows)
            bas[i]++;
      if(constr_type == LE || constr_type == GE)
         origUpperBound[rows] = infinite;
      else
         if(constr_type == EQ)
            origUpperBound[rows] = 0;
         else
            throw (new Exception("Wrong constraint type\n"));
      origLowerBound[rows] = 0;
      if(constr_type == GE && rh != 0)
         origRh[rows] = -rh;
      else
         origRh[rows] = rh;
      rowEndValid = FALSE;
      bas[rows] = rows;
      basis[rows] = TRUE;
      lower[rows] = TRUE;
      etaValid = FALSE;
   }

   /**
   * This routine is similar to the routine str_set_obj_fn. The same idea,
   * but call add_constraint.
   */
   public void strAddConstraint(String row_string, int constr_type, double rh) throws Exception
   {
      int i = 0;
      double[] aRow;
      aRow = new double[columns + 1];
      StringTokenizer stk = new StringTokenizer(row_string);
      while(stk.hasMoreTokens() && i < columns)
      {
         i++;
         aRow[i] = Double.valueOf(stk.nextToken()).doubleValue();
      }
      addConstraint(aRow, constr_type, rh);
   }

   /**
   * First check, if rownumber exists.
   * For all columns
   * For every coefficient in column
   * if it is not rownumber,
   * then shift elements to smaller nonzero index and perhaps correct row index.
   * else  delete
   * update colEnd
   * shift values for origRhs, changedSign, bas, rowName down by one.
   * Update values in bas
   * shift values for lower, basis, origUpperBound, origLowerBound, mustBeInt, scaling down
   * by one.
   * update rows and sum
   * set rowEndValid = FALSE
   * if lp = active, set globals.
   * etaValid = FALSE
   * basisValid = FALSE
   */
   public void delConstraint(int del_row) throws Exception
   {
      int i, j;
      int elmnr;
      int startcol;
      if(del_row < 1 || del_row > rows)
         throw (new Exception("There is no constraint nr. " + del_row));
      elmnr = 0;
      startcol = 0;
      for(i = 1;i <= columns;i++)
      {
         for(j = startcol;j < colEnd[i];j++)
         {
            if(mat[j].rowNr != del_row)
            {
               mat[elmnr] = mat[j];
               if(mat[elmnr].rowNr > del_row)
                  mat[elmnr].rowNr--;
               elmnr++;
            }
            else
               nonZeros--;
         }
         startcol = colEnd[i];
         colEnd[i] = elmnr;
      }
      for(i = del_row;i < rows;i++)
      {
         origRh[i] = origRh[i + 1];
         changedSign[i] = changedSign[i + 1];
         bas[i] = bas[i + 1];
         if(namesUsed != FALSE)
            rowName[i] = rowName[i + 1];
      }
      for(i = 1;i < rows;i++)
         if(bas[i] > del_row)
            bas[i]--;
      for(i = del_row;i < sum;i++)
      {
         lower[i] = lower[i + 1];
         basis[i] = basis[i + 1];
         origUpperBound[i] = origUpperBound[i + 1];
         origLowerBound[i] = origLowerBound[i + 1];
         mustBeInt[i] = mustBeInt[i + 1];
         if(scalingUsed != FALSE)
            scale[i] = scale[i + 1];
      }
      rows--;
      sum--;
      rowEndValid = FALSE;
      etaValid = FALSE;
      basisValid = FALSE;
   }

   /**
   * Calloc/Realloc space for lagRow, lagRhs, lambda, lagConType
   * Fill arrays.
   */
   public void addLagCon(double[] row, int con_type, double rhs) throws Exception
   {
      int i;
      double sign = 0;
      if(con_type == LE || con_type == EQ)
         sign = 1;
      else
         if(con_type == GE)
            sign = -1;
         else
            throw (new Exception("con_type not implemented\n"));
      nrLagrange++;
      if(nrLagrange == 1)
      {
         lagRow = new double[nrLagrange][];
         lagRhs = new double[nrLagrange];
         lambda = new double[nrLagrange];
         lagConType = new int[nrLagrange];
         for(i = 0;i < nrLagrange;i++)
         {
            lagRhs[i] = 0;
            lambda[i] = 0;
            lagConType[i] = 0;
         }
      }
      else
      {
         double[][] db2_ptr = lagRow;
         lagRow = new double[nrLagrange][];
         System.arraycopy(db2_ptr, 0, lagRow, 0, db2_ptr.length);
         double[] db_ptr = lagRhs;
         lagRhs = new double[nrLagrange];
         System.arraycopy(db_ptr, 0, lagRhs, 0, db_ptr.length);
         db_ptr = lambda;
         lambda = new double[nrLagrange];
         System.arraycopy(db_ptr, 0, lambda, 0, db_ptr.length);
         int[] short_ptr = lagConType;
         lagConType = new int[nrLagrange];
         System.arraycopy(short_ptr, 0, lagConType, 0, short_ptr.length);
      }
      lagRow[nrLagrange - 1] = new double[columns + 1];
      lagRhs[nrLagrange - 1] = rhs * sign;
      for(i = 1;i <= columns;i++)
         lagRow[nrLagrange - 1][i] = row[i] * sign;
      lambda[nrLagrange - 1] = 0;
      lagConType[nrLagrange - 1] = (con_type == EQ) ? (int)1 : (int)0;
   }

   /**
   * Same idea as always. Reserve space for array, strtod values into this array,
   * call add_lag_con and free array.
   */
   public void strAddLagCon(String row, int con_type, double rhs) throws Exception
   {
      int i = 0;
      double[] a_row;
      a_row = new double[columns + 1];
      StringTokenizer stk = new StringTokenizer(row);
      while(stk.hasMoreTokens() && i < columns)
      {
         i++;
         a_row[i] = Double.valueOf(stk.nextToken()).doubleValue();
      }
      if(i < columns)
         throw (new Exception("not enough value for str_add_lag_con"));
      addLagCon(a_row, con_type, rhs);
   }

   /**
   * update columns and sums,
   * increment space for columns and matrix
   * if scaling used
   * set scaling factor for column to "1" and scale all values with row[scaling].
   * for all elements in (dense) column
   * if value is not zero
   * write it in matrix.
   * update colEnd
   * origLowerBound
   * origUpperBound
   * lower
   * basis
   * mustBeInt
   * invent perhaps name for column
   * rowEndValid = FALSE
   */
   public void addColumn(double[] column) throws Exception
   {
      int i, elmnr;

      /* if the column has only one entry, this should be handled as a bound, but
      this currently is not the case */
      columns++;
      sum++;
      incrementColumnSpace();
      incrementMatrixSpace(rows + 1);
      if(scalingUsed != FALSE)
      {
         for(i = 0;i <= rows;i++)
            column[i] *= scale[i];
         scale[sum] = 1;
      }
      elmnr = colEnd[columns - 1];
      for(i = 0;i <= rows;i++)
         if(column[i] != 0)
         {
            mat[elmnr].rowNr = i;
            if(changedSign[i] != FALSE)
               mat[elmnr].value = -column[i];
            else
               mat[elmnr].value = column[i];
            nonZeros++;
            elmnr++;
         }
      colEnd[columns] = elmnr;
      origLowerBound[sum] = 0;
      origUpperBound[sum] = infinite;
      lower[sum] = TRUE;
      basis[sum] = FALSE;
      mustBeInt[sum] = FALSE;
      if(namesUsed != FALSE)
         colName[columns] = new String("var_" + columns);
      rowEndValid = FALSE;
   }

   /**
   * Same idea as always. Reserve space for array, strtod values into this array,
   * call add_column and free array.
   */
   public void strAddColumn(String col_string) throws Exception
   {
      int i = 0;
      double[] aCol;
      aCol = new double[rows + 1];
      StringTokenizer stk = new StringTokenizer(col_string);
      while(stk.hasMoreTokens() && i < rows)
      {
         i++;
         aCol[i] = Double.valueOf(stk.nextToken()).doubleValue();
      }
      if(i < rows)
         throw (new Exception("Bad String in str_add_column: " + col_string));
      addColumn(aCol);
   }

   /**
   * check, if column is in range
   * if column in Basis set basisValid to FALSE
   * else update bas
   * shift namesUsed,
   * mustBeInt
   * origUpperBound
   * origLowerBound
   * upperBound
   * lowerBound
   * basis
   * lower
   * scaling
   * update lagrangian stuff
   * copy elements in matrix down.
   * update colEnd
   * update nonZeros
   * rowEndValid = FALSE
   * etaValid = FALSE
   * update sum
   * column
   */
   public void deleteColumn(int column) throws Exception
   {
      int i, j, from_elm, to_elm, elm_in_col;
      if(column > columns || column < 1)
         throw (new Exception("Column out of range in del_column"));
      for(i = 1;i <= rows;i++)
      {
         if(bas[i] == rows + column)
            basisValid = FALSE;
         else
            if(bas[i] > rows + column)
               bas[i]--;
      }
      for(i = rows + column;i < sum;i++)
      {
         if(namesUsed != FALSE)
            colName[i - rows] = colName[i - rows + 1];
         mustBeInt[i] = mustBeInt[i + 1];
         origUpperBound[i] = origUpperBound[i + 1];
         origLowerBound[i] = origLowerBound[i + 1];
         upperBound[i] = upperBound[i + 1];
         lowerBound[i] = lowerBound[i + 1];
         basis[i] = basis[i + 1];
         lower[i] = lower[i + 1];
         if(scalingUsed != FALSE)
            scale[i] = scale[i + 1];
      }
      for(i = 0;i < nrLagrange;i++)
         for(j = column;j <= columns;j++)
            lagRow[i][j] = lagRow[i][j + 1];
      to_elm = colEnd[column - 1];
      from_elm = colEnd[column];
      elm_in_col = from_elm - to_elm;
      for(i = from_elm;i < nonZeros;i++)
      {
         mat[to_elm] = mat[i];
         to_elm++;
      }
      for(i = column;i < columns;i++)
         colEnd[i] = colEnd[i + 1] - elm_in_col;
      nonZeros -= elm_in_col;
      rowEndValid = FALSE;
      etaValid = FALSE;
      sum--;
      columns--;
   }

   /**
   * Test if column number in range
   * scale value
   * Test, if new value is feasible (greater than lower bound)
   * etaValid = FALSE
   * set origUpperBound
   */
   public void setUpperBound(int column, double value) throws Exception
   {
      if(column > columns || column < 1)
         throw (new Exception("Column out of range"));
      if(scalingUsed != FALSE)
         value /= scale[rows + column];
      if(value < origLowerBound[rows + column])
         throw (new Exception("Upperbound must be >= lowerbound"));
      etaValid = FALSE;
      origUpperBound[rows + column] = value;
   }

   /**
   * Test if column number in range
   * scale value
   * Test, if new value is feasible (smaller than upper bound)
   * etaValid = FALSE
   * set origLowerBound
   */
   public void setLowerBound(int column, double value) throws Exception
   {
      if(column > columns || column < 1)
         throw (new Exception("Column out of range"));
      if(scalingUsed != FALSE)
         value /= scale[rows + column];
      if(value > origUpperBound[rows + column])
         throw (new Exception("Upperbound must be >= lowerbound"));

      /*
      if(value < 0)
      error("Lower bound cannot be < 0");
      */
      etaValid = FALSE;
      origLowerBound[rows + column] = value;
   }

   /**
   * Test if column number in range
   * set mustBeInt
   * If variable must be integer, unscale column
   */
   public void setInt(int column, int mustBeInt_V) throws Exception
   {
      if(column > columns || column < 1)
         throw (new Exception("Column out of range"));
      mustBeInt[rows + column] = mustBeInt_V;
      if(mustBeInt_V == TRUE)
         if(columnsScaled != FALSE)
            unscaleColumns();
   }

   /**
   * Test, if row_number is in range
   * Test, if row_number for objective row should be set, WARNING
   * scale value and change sign.
   * etaValid = FALSE
   */
   public void setRh(int row, double value) throws Exception
   {
      if(row > rows || row < 0)
         throw (new Exception("Row out of Range"));
      if(row == 0) /* setting of RHS of OF not meaningful */
      {
         throw (new Exception("Warning: attempt to set RHS of objective function, ignored"));
      }
      if(scalingUsed != FALSE)
         if(changedSign[row] != FALSE)
            origRh[row] = -value * scale[row];
         else
            origRh[row] = value * scale[row];
      else
         if(changedSign[row] != FALSE)
            origRh[row] = -value;
         else
            origRh[row] = value;
      etaValid = FALSE;
   }

   /**
   * For all rows
   * scale and change sign
   * set origRh
   * etaValid = FALSE
   */
   public void setRhVec(double[] rh) throws Exception
   {
      int i;
      if(scalingUsed != FALSE)
         for(i = 1;i <= rows;i++)
            if(changedSign[i] != FALSE)
               origRh[i] = -rh[i] * scale[i];
            else
               origRh[i] = rh[i] * scale[i];
      else
         for(i = 1;i <= rows;i++)
            if(changedSign[i] != FALSE)
               origRh[i] = -rh[i];
            else
               origRh[i] = rh[i];
      etaValid = FALSE;
   }

   /**
   * Same idea as always. Reserve space for array, strtod values into this array,
   * call set_rh_vec and free array.
   */
   public void strSetRhVec(String rh_string) throws Exception
   {
      int i = 0;
      double[] newrh;
      newrh = new double[rows + 1];
      StringTokenizer stk = new StringTokenizer(rh_string);
      while(stk.hasMoreTokens() && i < rows)
      {
         i++;
         newrh[i] = Double.valueOf(stk.nextToken()).doubleValue();
      }
      setRhVec(newrh);
   }

   /**
   * if maxim == FALSE
   * multiply all Values in row[0] with -1
   * etaValid = FALSE
   * set maximise = TRUE
   * changedSign[0] = TRUE
   */
   public void setMaximum() throws Exception
   {
      int i;
      if(maximise == FALSE)
      {
         for(i = 0;i < nonZeros;i++)
            if(mat[i].rowNr == 0)
               mat[i].value *= -1;
         etaValid = FALSE;
         origRh[0] *= -1;
      }
      maximise = TRUE;
      changedSign[0] = TRUE;
   }

   /**
   * if maxim == TRUE
   * multiply all Values in row[0] with -1
   * etaValid = FALSE
   * set maximise = FALSE
   * changedSign[0] = FALSE
   */
   void setMinimum() throws Exception
   {
      int i;
      if(maximise == TRUE)
      {
         for(i = 0;i < nonZeros;i++)
            if(mat[i].rowNr == 0)
               mat[i].value = -mat[i].value;
         etaValid = FALSE;
         origRh[0] *= -1;
      }
      maximise = FALSE;
      changedSign[0] = FALSE;
   }

   /**
    Test, if row_number is in range
    if type == EQUAL
    set upper bound on slackvariable to zero
    basisValid == FALSE
    if change_sign[row]
    multiply all coefficients with -1
    etaValid = FALSE
    change_sign = FALSE
    change sign of origRh
    if type == LESSEQUAL
    set upper bound on slackvariable to infinity
    basisValid == FALSE
    if change_sign[row]
    multiply all coefficients with -1
    etaValid = FALSE
    change_sign = FALSE
    change sign of origRh
    if type == GREATEREQUAL
    set upper bound on slackvariable to infinity
    basisValid == FALSE
    if NOT change_sign[row]
    multiply all coefficients with -1
    etaValid = FALSE
    change_sign = TRUE
    change sign of origRh
    else
    error wrong constraint type
    */
   void set_constr_type(int row, int con_type) throws Exception
   {
      int i;
      if(row > rows || row < 1)
         throw (new Exception("Row out of Range"));
      if(con_type == EQ)
      {
         origUpperBound[row] = 0;
         basisValid = FALSE;
         if(changedSign[row] != FALSE)
         {
            for(i = 0;i < nonZeros;i++)
               if(mat[i].rowNr == row)
                  mat[i].value *= -1;
            etaValid = FALSE;
            changedSign[row] = FALSE;
            if(origRh[row] != 0)
               origRh[row] *= -1;
         }
      }
      else
         if(con_type == LE)
         {
            origUpperBound[row] = infinite;
            basisValid = FALSE;
            if(changedSign[row] != FALSE)
            {
               for(i = 0;i < nonZeros;i++)
                  if(mat[i].rowNr == row)
                     mat[i].value *= -1;
               etaValid = FALSE;
               changedSign[row] = FALSE;
               if(origRh[row] != 0)
                  origRh[row] *= -1;
            }
         }
         else
            if(con_type == GE)
            {
               origUpperBound[row] = infinite;
               basisValid = FALSE;
               if(!(changedSign[row] != FALSE))
               {
                  for(i = 0;i < nonZeros;i++)
                     if(mat[i].rowNr == row)
                        mat[i].value *= -1;
                  etaValid = FALSE;
                  changedSign[row] = TRUE;
                  if(origRh[row] != 0)
                     origRh[row] *= -1;
               }
            }
            else
               throw (new Exception("Constraint type not (yet) implemented"));
   }

   /** get value of matrix element in row and column
    Test, if row_number is in range
    Test, if col_number is in range
    value = 0
    loop through column
    if value found
    unscale and change_sign
    return value
    */
   public double matrixElement(int row, int column) throws Exception
   {
      double value;
      int elmnr;
      if(row < 0 || row > rows)
         throw (new Exception("Row out of range in mat_elm"));
      if(column < 1 || column > columns)
         throw (new Exception("Column out of range in mat_elm"));
      value = 0;
      elmnr = colEnd[column - 1];
      while(mat[elmnr].rowNr != row && elmnr < colEnd[column])
         elmnr++;
      if(elmnr != colEnd[column])
      {
         value = mat[elmnr].value;
         if(changedSign[row] != FALSE)
            value = -value;
         if(scalingUsed != FALSE)
            value /= scale[row] * scale[rows + column];
      }
      return (value);
   }

   /**
    this is dense form
    Test, if row_number is in range
    for all columns
    initialise value with 0
    for all entries in column
    if row found, write value
    unscale value
    if change_sign
    multiply with -1
    */
   public void getRow(int rowNr, double[] row) throws Exception
   {
      int i, j;
      if(rowNr < 0 || rowNr > rows)
         throw (new Exception("Row nr. out of range in get_row"));
      for(i = 1;i <= columns;i++)
      {
         row[i] = 0;
         for(j = colEnd[i - 1];j < colEnd[i];j++)
            if(mat[j].rowNr == rowNr)
               row[i] = mat[j].value;
         if(scalingUsed != FALSE)
            row[i] /= scale[rows + i] * scale[rowNr];
      }
      if(changedSign[rowNr] != FALSE)
         for(i = 0;i <= columns;i++)
            if(row[i] != 0)
               row[i] = -row[i];
   }

   /**
    Test, if column is in range.
    column is dense
    initialise columnarray with 0
    for all elements in this colum, copy to dense array
    unscale and change sign
    */
   public void getColumn(int col_nr, double[] column) throws Exception
   {
      int i;
      if(col_nr < 1 || col_nr > columns)
         throw (new Exception("Col. nr. out of range in get_column"));
      for(i = 0;i <= rows;i++)
         column[i] = 0;
      for(i = colEnd[col_nr - 1];i < colEnd[col_nr];i++)
         column[mat[i].rowNr] = mat[i].value;
      for(i = 0;i <= rows;i++)
         if(column[i] != 0)
         {
            if(changedSign[i] != FALSE)
               column[i] *= -1;
            if(scalingUsed != FALSE)
               column[i] /= (scale[i] * scale[rows + col_nr]);
         }
   }
   final double myRound(double val, double eps) throws Exception
   {
      return (Math.abs(val) < eps) ? 0 : val;
   }

   /**
    Unscale values and look, if they are between orig_lower and orig_upper bounds
    allocate space for a new rhs
    With this values calculate rhs
    check if rhs is lessequal than orig rhs for LE rows and equal to origRhs
    for EQ rows.
    */
   public int isFeasible(double[] values) throws Exception
   {
      int i, elmnr;
      double[] this_rhs;
      double dist;
      if(scalingUsed!=FALSE)
      {
         for(i = rows + 1;i <= sum;i++)
            if(values[i - rows] < origLowerBound[i] * scale[i] || values[i - rows] > origUpperBound[i] * scale[i])
               return (FALSE);
      }
      else
      {
         for(i = rows + 1;i <= sum;i++)
            if(values[i - rows] < origLowerBound[i] || values[i - rows] > origUpperBound[i])
               return (FALSE);
      }

      //CALLOC(this_rhs, lp->rows + 1);
      this_rhs = new double[rows + 1];
      for(i = 0;i <= rows;i++)
         this_rhs[i] = 0;
      if(columnsScaled != FALSE)
      {
         for(i = 1;i <= columns;i++)
            for(elmnr = colEnd[i - 1];elmnr < colEnd[i];elmnr++)
               this_rhs[mat[elmnr].rowNr] += mat[elmnr].value * values[i] / scale[rows + i];
      }
      else
      {
         for(i = 1;i <= columns;i++)
            for(elmnr = colEnd[i - 1];elmnr < colEnd[i];elmnr++)
               this_rhs[mat[elmnr].rowNr] += mat[elmnr].value * values[i];
      }
      for(i = 1;i <= rows;i++)
      {
         dist = origRh[i] - this_rhs[i];
         dist = myRound(dist, 0.001); /* ugly constant, MB */
         if((origUpperBound[i] == 0 && dist != 0) || dist < 0)
            return (FALSE);
      }
      return (TRUE);
   }

   /**
    for all columns
    for all elements in column
    unscale value and change_sign
    check if difference smaller than epsilon
    return TRUE or FALSE

    fixed by Enrico Faggiolo
    */
   int columnInLp(double[] testcolumn) throws Exception
   {
      int i, j;
      int nz, ident;
      double value;
      for(nz = 0,i = 0;i <= rows;i++)
         if(Math.abs(testcolumn[i]) > epsel)
            nz++;
      if(scalingUsed != FALSE)
         for(i = 1;i <= columns;i++)
         {
            ident = nz;
            for(j = colEnd[i - 1];j < colEnd[i];j++)
            {
               value = mat[j].value;
               if(changedSign[mat[j].rowNr]!=FALSE)
                  value = -value;
               value /= scale[rows + i];
               value /= scale[mat[j].rowNr];
               value -= testcolumn[mat[j].rowNr];
               if(Math.abs(value) > epsel)
                  break;
               ident--;
               if(ident == 0)
                  return (TRUE);
            }
         }
      else
         for(i = 1;i <= columns;i++)
         {
            ident = nz;
            for(j = colEnd[i - 1];j < colEnd[i];j++)
            {
               value = mat[j].value;
               if(changedSign[mat[j].rowNr]!=FALSE)
                  value = -value;
               value -= testcolumn[mat[j].rowNr];
               if(Math.abs(value) > epsel)
                  break;
               ident--;
               if(ident == 0)
                  return (TRUE);
            }
         }
      return (FALSE);
   }

   /** print rowwise in readable form. */
   public void printLp() throws Exception
   {
      println(toString());
   }

   /** print rowwise in readable form - print_lp()*/
   public String toString()
   {
      StringBuffer straux = new StringBuffer();
      int i, j;
      double[] fatmat;
      fatmat = new double[(rows + 1) * columns];
      for(i = 0;i < fatmat.length;i++)
         fatmat[i] = 0;
      for(i = 1;i <= columns;i++)
         for(j = colEnd[i - 1];j < colEnd[i];j++)
            fatmat[(i - 1) * (rows + 1) + mat[j].rowNr] = mat[j].value;
      straux.append("problem name: " + lpName + "\n");
      straux.append("          ");
      for(j = 1;j <= columns;j++)
         if(namesUsed != FALSE)
            straux.append(colName[j]);
         else
            straux.append("Var[" + j + "] ");
      if(maximise != FALSE)
      {
         straux.append("\nMaximise  ");
         for(j = 0;j < columns;j++)
            straux.append(" " + (-fatmat[j * (rows + 1)]) + " ");
      }
      else
      {
         straux.append("\nMinimize  ");
         for(j = 0;j < columns;j++)
            straux.append(" " + fatmat[j * (rows + 1)] + " ");
      }
      straux.append("\n");
      for(i = 1;i <= rows;i++)
      {
         if(namesUsed != FALSE)
            straux.append(rowName[i]);
         else
            straux.append("Row[" + i + "]  ");
         for(j = 0;j < columns;j++)
            if(changedSign[i] != FALSE && fatmat[j * (rows + 1) + i] != 0)
               straux.append((-fatmat[j * (rows + 1) + i]) + " ");
            else
               straux.append(fatmat[j * (rows + 1) + i] + " ");
         if(origUpperBound[i] == infinite)
            if(changedSign[i] != FALSE)
               straux.append(">= ");
            else
               straux.append("<= ");
         else
            straux.append(" = ");
         if(changedSign[i] != FALSE)
            straux.append(-origRh[i]);
         else
            straux.append(origRh[i]);
      }
      straux.append("Type      ");
      for(i = 1;i <= columns;i++)
         if(mustBeInt[rows + i] == TRUE)
            straux.append("     Int ");
         else
            straux.append("    double ");
      straux.append("\nupperBound      ");
      for(i = 1;i <= columns;i++)
         if(origUpperBound[rows + i] == infinite)
            straux.append("     Inf ");
         else
            straux.append(origUpperBound[rows + i] + " ");
      straux.append("\nlowerBound     ");
      for(i = 1;i <= columns;i++)
         straux.append(origLowerBound[rows + i] + " ");
      straux.append("\n");
      for(i = 0;i < nrLagrange;i++)
      {
         straux.append("lag[" + i + "]  ");
         for(j = 1;j <= columns;j++)
            straux.append(lagRow[i][j] + " ");
         if(origUpperBound[i] == infinite)
            if(lagConType[i] == GE)
               straux.append(">= ");
            else
               if(lagConType[i] == LE)
                  straux.append("<= ");
               else
                  if(lagConType[i] == EQ)
                     straux.append(" = ");
         straux.append(lagRhs[i]);
      }
      return straux.toString();
   }

   /**
    Perhaps allocate memory for names and initialise with default names
    strcpy rowname
    */
   public void setRowName(int row, String newName) throws Exception
   {
      int i;
      if(namesUsed == 0)
      {
         rowName = new String[rowsAlloc + 1];
         colName = new String[columnsAlloc + 1];
         namesUsed = TRUE;
         for(i = 0;i <= rows;i++)
            rowName[i] = new String("r_" + i);
         for(i = 1;i <= columns;i++)
            colName[i] = new String("var_" + i);
      }
      rowName[row] = newName;
   }

   /**
    Perhaps allocate memory for names and initialise with default names
    strcpy colname
    */
   public void setColumnName(LpModel lp, int column, String newName) throws Exception
   {
      int i;
      if(namesUsed == 0)
      {
         rowName = new String[rowsAlloc + 1];
         colName = new String[columnsAlloc + 1];
         namesUsed = TRUE;
         for(i = 0;i <= rows;i++)
            rowName[i] = new String("r_" + i);
         for(i = 1;i <= columns;i++)
            colName[i] = new String("var_" + i);
      }
      colName[column] = newName;
   }

   /**
    * calculate scaling factor depending on min and max
    */
   private double minMaxToScale(double min, double max) throws Exception
   {
      double scale;

      /* should do something sensible when min or max is 0, MB */
      if((min == 0) || (max == 0))
         return ((double)1);

      /* scale = 1 / pow(10, (log10(min) + log10(max)) / 2); */

      /* Jon van Reet noticed: can be simplified to: */
      scale = 1.0 / Math.sqrt(min * max);
      return (scale);
   }

   /**
    for all columns
    for all coefficients in column
    unscale (columnscaling)
    for all columns
    unscale bounds
    set scaling vector to 1
    columnsScaled = FALSE
    etaValid = FALSE
    */
   public void unscaleColumns() throws Exception
   {
      int i, j;

      /* unscale mat */
      for(j = 1;j <= columns;j++)
         for(i = colEnd[j - 1];i < colEnd[j];i++)
            mat[i].value /= scale[rows + j];

      /* unscale bounds as well */
      for(i = rows + 1;i < sum;i++)
      {
         if(origLowerBound[i] != 0)
            origLowerBound[i] *= scale[i];
         if(origUpperBound[i] != infinite)
            origUpperBound[i] *= scale[i];
      }
      for(i = rows + 1;i <= sum;i++)
         scale[i] = 1;
      columnsScaled = FALSE;
      etaValid = FALSE;
   }

   /**
    Work only if scaling used
    for all columns
    for all coefficients in column
    unscale (columnscaling)
    for all columns
    unscale bounds
    for all columns
    for all coefficients in column
    unscale (rowscaling)
    for all rows
    unscale origRhs
    free scale
    scalingUsed = FALSE
    etaValid = FALSE
    */
   void unscale() throws Exception
   {
      int i, j;
      if(scalingUsed != FALSE)
      {

         /* unscale mat */
         for(j = 1;j <= columns;j++)
            for(i = colEnd[j - 1];i < colEnd[j];i++)
               mat[i].value /= scale[rows + j];

         /* unscale bounds */
         for(i = rows + 1;i <= sum;i++)
         { /* was < */ /* changed by PN */
            if(origLowerBound[i] != 0)
               origLowerBound[i] *= scale[i];
            if(origUpperBound[i] != infinite)
               origUpperBound[i] *= scale[i];
         }

         /* unscale the matrix */
         for(j = 1;j <= columns;j++)
            for(i = colEnd[j - 1];i < colEnd[j];i++)
               mat[i].value /= scale[mat[i].rowNr];

         /* unscale the rhs! */
         for(i = 0;i <= rows;i++)
            origRh[i] /= scale[i];

         /* and don't forget to unscale the upper and lower bounds ... */
         for(i = 0;i <= rows;i++)
         {
            if(origLowerBound[i] != 0)
               origLowerBound[i] /= scale[i];
            if(origUpperBound[i] != infinite)
               origUpperBound[i] /= scale[i];
         }
         scalingUsed = FALSE;
         etaValid = FALSE;
      }
   }

   /** find row maximum and row minimum. Use these values to scale problem. */
   public void autoScale() throws Exception
   {
      int i, j, rowNr;
      double[] row_max;
      double[] row_min;
      double[] scalechange;
      double absval;
      double col_max, col_min;
      if(!(scalingUsed != FALSE))
      {

         //MALLOC(lp->scale, lp->sumAlloc + 1);
         scale = new double[sumAlloc + 1];
         for(i = 0;i <= sum;i++)
            scale[i] = 1;
      }

      //MALLOC(row_max, lp->rows + 1);
      row_max = new double[rows + 1];

      //MALLOC(row_min, lp->rows + 1);
      row_min = new double[rows + 1];

      //MALLOC(scalechange, lp->sum + 1);
      scalechange = new double[sum + 1];

      /* initialise min and max values */
      for(i = 0;i <= rows;i++)
      {
         row_max[i] = 0;
         row_min[i] = infinite;
      }

      /* calculate min and max absolute values of rows */
      for(j = 1;j <= columns;j++)
         for(i = colEnd[j - 1];i < colEnd[j];i++)
         {
            rowNr = mat[i].rowNr;
            absval = Math.abs(mat[i].value);
            if(absval != 0)
            {
               row_max[rowNr] = Math.max(row_max[rowNr], absval);
               row_min[rowNr] = Math.min(row_min[rowNr], absval);
            }
         }

      /* calculate scale factors for rows */
      for(i = 0;i <= rows;i++)
      {
         scalechange[i] = minMaxToScale(row_min[i], row_max[i]);
         scale[i] *= scalechange[i];
      }

      /* now actually scale the matrix */
      for(j = 1;j <= columns;j++)
         for(i = colEnd[j - 1];i < colEnd[j];i++)
            mat[i].value *= scalechange[mat[i].rowNr];

      /* and scale the rhs and the row bounds (RANGES in MPS!!) */
      for(i = 0;i <= rows;i++)
      {
         origRh[i] *= scalechange[i];
         if((origUpperBound[i] < infinite) && (origUpperBound[i] != 0))
            origUpperBound[i] *= scalechange[i];
         if(origLowerBound[i] != 0)
            origLowerBound[i] *= scalechange[i];
      }
      row_max = null;
      row_min = null;

      /* calculate column scales */
      for(j = 1;j <= columns;j++)
      {
         if(mustBeInt[rows + j]!=FALSE)
         { /* do not scale integer columns */
            scalechange[rows + j] = 1;
         }
         else
         {
            col_max = 0;
            col_min = infinite;
            for(i = colEnd[j - 1];i < colEnd[j];i++)
            {
               if(mat[i].value != 0)
               {
                  col_max = Math.max(col_max, Math.abs(mat[i].value));
                  col_min = Math.min(col_min, Math.abs(mat[i].value));
               }
            }
            scalechange[rows + j] = minMaxToScale(col_min, col_max);
            scale[rows + j] *= scalechange[rows + j];
         }
      }

      /* scale mat */
      for(j = 1;j <= columns;j++)
         for(i = colEnd[j - 1];i < colEnd[j];i++)
            mat[i].value *= scalechange[rows + j];

      /* scale bounds as well */
      for(i = rows + 1;i <= sum;i++)
      { /* was < */ /* changed by PN */
         if(origLowerBound[i] != 0)
            origLowerBound[i] /= scalechange[i];
         if(origUpperBound[i] != infinite)
            origUpperBound[i] /= scalechange[i];
      }
      columnsScaled = TRUE;

      //free(scalechange);
      scalechange = null; //Thanks God I am using Java! R.
      scalingUsed = TRUE;
      etaValid = FALSE;
   }

   /** basisValid=FALSE */
   public void resetBasis() throws Exception
   {
      basisValid = FALSE;
   }

   /** Returs the result value of an variable. The input is the number of the variable (starting from 1) and the result is bestSolution[rows+i] */
   public double getResult(int i) throws Exception
   {
      if (i<1)
         throw(new Exception("Invalid index: "+i));
      return bestSolution[rows + i];
   }

   /**
    Print solution to stdout
    Print all variables
    In some cases
    Print slack variables ???
    Print duals
    */
   public void printSolution() throws Exception
   {
      println(solutionToString());
   }
   public String solutionToString() throws Exception
   {
      StringBuffer straux = new StringBuffer();
      int i;
      straux.append("Value of objective function: " + bestSolution[0] + "\n");

      /* print normal variables */
      for(i = 1;i <= columns;i++)
         if(namesUsed != FALSE)
            straux.append(colName[i] + " " + bestSolution[rows + i] + "\n");
         else
            straux.append("Var [" + i + "]  " + bestSolution[rows + i] + "\n");

      /* print achieved constraint values */
      if(verbose != FALSE)
      {
         straux.append("\nActual values of the constraints:\n");
         for(i = 1;i <= rows;i++)
            if(namesUsed != FALSE)
               straux.append(rowName[i] + " " + bestSolution[i] + "\n");
            else
               straux.append("Row [" + i + "]  " + " " + bestSolution[i] + "\n");
      }
      if((verbose != FALSE || printDuals != FALSE))
      {
         if(maxLevel != 1)
            straux.append("These are the duals from the node that gave the optimal solution.\n");
         else
            straux.append("\nDual values:\n");
         for(i = 1;i <= rows;i++)
            if(namesUsed != FALSE)
               straux.append(rowName[i] + " " + duals[i] + "\n");
            else
               straux.append("Row [" + i + "]  " + duals[i] + "\n");
      }
      return straux.toString();
   } /* Printsolution */

   /** print the LP matrix for debugging */
   public void writeMatrix(PrintStream output) throws Exception
   {
      double[] row;
      row = new double[columns + 1];
      for(int j = 0;j <= rows;j++)
      {
         getRow(j,row);
         for(int i = 1;i <= columns;i++)
            output.print(row[i]+" ");
         output.println();
      }
   }

   /** print LP rowwise in readable form. */
   public void writeLp(PrintStream output) throws Exception
   {
      int i, j;
      double[] row;
      row = new double[columns + 1];
      if(maximise != FALSE)
         output.print("max:");
      else
         output.print("min:");
      getRow(0, row);
      for(i = 1;i <= columns;i++)
         if(row[i] != 0)
         {
            if(row[i] == -1)
               output.print(" -");
            else
               if(row[i] == 1)
                  output.print(" +");
               else
                  output.print(row[i]);
            if(namesUsed != FALSE)
               output.print(colName[i]);
            else
               output.print("x" + i);
         }
      output.print(";\n");
      for(j = 1;j <= rows;j++)
      {
         if(namesUsed != FALSE)
            output.print(rowName[j]);
         getRow(j, row);
         for(i = 1;i <= columns;i++)
            if(row[i] != 0)
            {
               if(row[i] == -1)
                  output.print(" -");
               else
                  if(row[i] == 1)
                     output.print(" +");
                  else
                     output.print(" " + row[i] + " ");
               if(namesUsed != FALSE)
                  output.print(colName[i]);
               else
                  output.print("x" + i);
            }
         if(origUpperBound[j] == 0)
            output.print(" =");
         else
            if(changedSign[j] != FALSE)
               output.print(" >");
            else
               output.print(" <");
         if(changedSign[j] != FALSE)
            output.println(" " + (-origRh[j]));
         else
            output.println(" " + origRh[j]);
      }
      for(i = rows + 1;i <= sum;i++)
      {
         if(origLowerBound[i] != 0)
         {
            if(namesUsed != FALSE)
               output.println(colName[i - rows] + ">" + origLowerBound[i] + ";");
            else
               output.print("x" + (i - rows) + " > " + origLowerBound[i] + ";");
         }
         if(origUpperBound[i] != infinite)
         {
            if(namesUsed != FALSE)
               output.println(colName[i - rows] + " < " + origUpperBound[i] + ";");
            else
               output.println("x" + (i - rows) + " < " + origUpperBound[i] + ";");
         }
      }
      i = 1;
      /*
      while(mustBeInt[rows + i] == FALSE && i <= columns)
         i++;
      if(i <= columns)
      {
         if(namesUsed != FALSE)
            output.print("\nint " + colName[i]);
         else
            output.print("\nint x" + i);
         i++;
         for(;i <= columns;i++)
            if(mustBeInt[rows + i] != FALSE)
               if(namesUsed != FALSE)
                  output.print("," + colName[i]);
               else
                  output.print(", x" + i);
         output.print(";\n");
      }
      */
   }

   //   /**
   //      The routine write_MPS seems to do no unscaling. However it uses internally
   //      the routine get_column() which does unscaling!
   //    */
   //   void write_MPS(lprec *lp, FILE *output)
   //   {
   //     int i, j, marker, putheader;
   //     REAL *column, a;
   //
   //
   //     MALLOC(column, lp->rows + 1);
   //     marker = 0;
   //     fprintf(output, "NAME          %s\n", lp->lpName);
   //     fprintf(output, "ROWS\n");
   //     for(i = 0; i <= lp->rows; i++) {
   //       if(i == 0)
   //         fprintf(output, " N  ");
   //       else
   //         if(lp->origUpperBound[i] != 0) {
   //      if(lp->changedSign[i])
   //        fprintf(output, " G  ");
   //      else
   //        fprintf(output, " L  ");
   //         }
   //         else
   //      fprintf(output, " E  ");
   //       if(lp->namesUsed)
   //         fprintf(output, "%s\n", lp->rowName[i]);
   //       else
   //         fprintf(output, "r_%d\n", i);
   //     }
   //
   //     fprintf(output, "COLUMNS\n");
   //
   //     for(i = 1; i <= lp->columns; i++) {
   //       if((lp->mustBeInt[i + lp->rows]) && (marker % 2) == 0) {
   //         fprintf(output,
   //            "    MARK%04d  'MARKER'                 'INTORG'\n",
   //            marker);
   //         marker++;
   //       }
   //       if((!lp->mustBeInt[i + lp->rows]) && (marker % 2) == 1) {
   //         fprintf(output,
   //            "    MARK%04d  'MARKER'                 'INTEND'\n",
   //            marker);
   //         marker++;
   //       }
   //       /* this gets slow for large LP problems. Implement a sparse version? */
   //       get_column(lp, i, column);
   //       j = 0;
   //       if(lp->maximise) {
   //         if(column[j] != 0) {
   //      if(lp->namesUsed)
   //        fprintf(output, "    %-8s  %-8s  %12g\n", lp->colName[i],
   //           lp->rowName[j], (double)-column[j]);
   //      else
   //        fprintf(output, "    var_%-4d  r_%-6d  %12g\n", i, j,
   //           (double)-column[j]);
   //         }
   //       }
   //       else {
   //         if(column[j] != 0) {
   //      if(lp->namesUsed)
   //        fprintf(output, "    %-8s  %-8s  %12g\n", lp->colName[i],
   //           lp->rowName[j], (double)column[j]);
   //      else
   //        fprintf(output, "    var_%-4d  r_%-6d  %12g\n", i, j,
   //           (double)column[j]);
   //         }
   //       }
   //       for(j = 1; j <= lp->rows; j++)
   //         if(column[j] != 0) {
   //      if(lp->namesUsed)
   //        fprintf(output, "    %-8s  %-8s  %12g\n", lp->colName[i],
   //           lp->rowName[j], (double)column[j]);
   //      else
   //        fprintf(output, "    var_%-4d  r_%-6d  %12g\n", i, j,
   //           (double)column[j]);
   //         }
   //     }
   //     if((marker % 2) == 1) {
   //       fprintf(output, "    MARK%04d  'MARKER'                 'INTEND'\n",
   //          marker);
   //       /* marker++; */ /* marker not used after this */
   //     }
   //
   //     fprintf(output, "RHS\n");
   //     for(i = 1; i <= lp->rows; i++) {
   //       a = lp->origRh[i];
   //       if(lp->scalingUsed)
   //         a /= lp->scale[i];
   //
   //       if(lp->changedSign[i]) {
   //         if(lp->namesUsed)
   //      fprintf(output, "    RHS       %-8s  %12g\n", lp->rowName[i],
   //         (double)-a);
   //         else
   //      fprintf(output, "    RHS       r_%-6d  %12g\n", i, (double)-a);
   //       }
   //       else {
   //         if(lp->namesUsed)
   //      fprintf(output, "    RHS       %-8s  %12g\n", lp->rowName[i],
   //         (double)a);
   //         else
   //      fprintf(output, "    RHS       r_%-6d  %12g\n", i, (double)a);
   //       }
   //     }
   //
   //     putheader = TRUE;
   //     for(i = 1; i <= lp->rows; i++)
   //       if((lp->origUpperBound[i] != lp->infinite) && (lp->origUpperBound[i] != 0.0)) {
   //         if(putheader) {
   //      fprintf(output, "RANGES\n");
   //      putheader = FALSE;
   //         }
   //         a = lp->origUpperBound[i];
   //         if(lp->scalingUsed)
   //      a /= lp->scale[i];
   //         if(lp->namesUsed)
   //      fprintf(output, "    RGS       %-8s  %12g\n", lp->rowName[i],
   //         (double)a);
   //         else
   //      fprintf(output, "    RGS       r_%-6d  %12g\n", i,
   //         (double)a);
   //       }
   //       else if((lp->origLowerBound[i] != 0.0)) {
   //         if(putheader) {
   //      fprintf(output, "RANGES\n");
   //      putheader = FALSE;
   //         }
   //         a = lp->origLowerBound[i];
   //         if(lp->scalingUsed)
   //      a /= lp->scale[i];
   //         if(lp->namesUsed)
   //      fprintf(output, "    RGS       %-8s  %12g\n", lp->rowName[i],
   //         (double)-a);
   //         else
   //      fprintf(output, "    RGS       r_%-6d  %12g\n", i,
   //         (double)-a);
   //       }
   //
   //     fprintf(output, "BOUNDS\n");
   //     if(lp->namesUsed)
   //       for(i = lp->rows + 1; i <= lp->sum; i++) {
   //         if((lp->origLowerBound[i] != 0) && (lp->origUpperBound[i] < lp->infinite) &&
   //       (lp->origLowerBound[i] == lp->origUpperBound[i])) {
   //      a = lp->origUpperBound[i];
   //      if(lp->scalingUsed)
   //        a *= lp->scale[i];
   //      fprintf(output, " FX BND       %-8s  %12g\n",
   //         lp->colName[i - lp->rows], (double)a);
   //         }
   //         else {
   //      if(lp->origUpperBound[i] < lp->infinite) {
   //        a = lp->origUpperBound[i];
   //        if(lp->scalingUsed)
   //          a *= lp->scale[i];
   //        fprintf(output, " UP BND       %-8s  %12g\n",
   //           lp->colName[i - lp->rows], (double)a);
   //      }
   //      if(lp->origLowerBound[i] != 0) {
   //        a = lp->origLowerBound[i];
   //        if(lp->scalingUsed)
   //          a *= lp->scale[i];
   //        /* bug? should a be used instead of lp->origLowerBound[i] MB */
   //        fprintf(output, " LO BND       %-8s  %12g\n",
   //           lp->colName[i - lp->rows], (double)lp->origLowerBound[i]);
   //      }
   //         }
   //       }
   //     else
   //       for(i = lp->rows + 1; i <= lp->sum; i++) {
   //         if((lp->origLowerBound[i] != 0) && (lp->origUpperBound[i] < lp->infinite) &&
   //       (lp->origLowerBound[i] == lp->origUpperBound[i])) {
   //      a = lp->origUpperBound[i];
   //      if(lp->scalingUsed)
   //        a *= lp->scale[i];
   //      fprintf(output, " FX BND       %-8s  %12g\n",
   //         lp->colName[i - lp->rows], (double)a);
   //         }
   //         else {
   //      if(lp->origUpperBound[i] < lp->infinite) {
   //        a = lp->origUpperBound[i];
   //        if(lp->scalingUsed)
   //          a *= lp->scale[i];
   //        fprintf(output, " UP BND       var_%-4d  %12g\n",
   //           i - lp->rows, (double)a);
   //      }
   //      if(lp->origLowerBound[i] != 0) {
   //        a = lp->origLowerBound[i];
   //        if(lp->scalingUsed)
   //          a *= lp->scale[i];
   //        fprintf(output, " LO BND       var_%-4d  %12g\n", i - lp->rows,
   //           (double)a);
   //      }
   //         }
   //       }
   //     fprintf(output, "ENDATA\n");
   //     free(column);
   //   }

   /**
    Print all duals
    */
   public void printDuals() throws Exception
   {
      println(dualsToString());
   }
   public String dualsToString() throws Exception
   {
      StringBuffer straux = new StringBuffer();
      int i;
      for(i = 1;i <= rows;i++)
         if(namesUsed != FALSE)
            straux.append(rowName[i] + " [" + i + "] " + duals[i] + "\n");
         else
            straux.append("Dual       [" + i + "] " + duals[i] + "\n");
      return straux.toString();
   }

   /**
    Print all row scales
    print all column scales.
    */
   public void printScales(LpModel lp) throws Exception
   {
      println(scalesToString());
   }
   public String scalesToString() throws Exception
   {
      StringBuffer straux = new StringBuffer();
      int i;
      if(scalingUsed != FALSE)
      {
         for(i = 0;i <= rows;i++)
            straux.append("Row[" + i + "]    scaled at " + scale[i] + "\n");
         for(i = 1;i <= columns;i++)
            straux.append("Column[" + i + "] scaled at " + scale[rows + i] + "\n");
      }
      return straux.toString();
   }

   /**
    * multiply the column with all matrices in etafile

    for every matrix between start and end
    calculate End of the matrix
    r = number of column in Eta matrix
    theta = pcol[r]
    for one matrix
    multiply pcol with the matrix (?)
    update pcol[r]
    round values in pcol
    */
   public void ftran(double[] pcol) throws Exception
   {
      int i, j, k, r, rowp[];
      double theta, valuep[];
      for(i = 1;i <= etaSize;i++)
      {
         k = etaColEnd[i] - 1;
         r = etaRowNr[k];
         theta = pcol[r];
         if(theta != 0)
         {
            j = etaColEnd[i - 1];

            ///* CPU intensive loop, let's do pointer arithmetic */

            //for(rowp = etaRowNr + j, valuep = etaValue + j; j < k; j++, rowp++, valuep++)

            //   pcol[*rowp] += theta * *valuep;
            for(j = etaColEnd[i - 1];j < k;j++)
               pcol[etaRowNr[j]] += theta * etaValue[j]; /* cpu expensive line - poor Java... ;-) R.*/
            pcol[r] *= etaValue[k];
         }
      }

      /* round small values to zero */
      for(i = 0;i <= rows;i++)
         pcol[i] = myRound(pcol[i], epsel);
   } /* ftran */

   /**
    For all Eta matrices, Starting with the highest number
    k = number of column in Eta-matrix
    for one matrix
    do multiplication
    round result
    set row[Eta_rowNr[k]] = result
    */
   void btran(double[] row) throws Exception
   {
      int i, j, k, rowp[];
      double f, valuep[];
      for(i = etaSize;i >= 1;i--)
      {
         f = 0;
         k = etaColEnd[i] - 1;
         j = etaColEnd[i - 1];

         //for(rowp = etaRowNr + j, valuep = etaValue + j;   j <= k;   j++, rowp++, valuep++)

         //  f += row[*rowp] * *valuep;
         for(j = etaColEnd[i - 1];j <= k;j++)
            f += row[etaRowNr[j]] * etaValue[j];
         f = myRound(f, epsel);
         row[etaRowNr[k]] = f;
      }
   } /* btran */

   /**
    Calculate the structures rowEnd[] and
    colNo[].
    internally two arrays are used:
    rowNr[], which contains the number of coefficients
    in row[i] and num[], which is a working array and
    contains the already used part of colNo in row[i].
    The array colNo is written at several positions
    at the same time. So it could look like

    +------------------------------------+
    |**         ****         *     **    |
    +------------------------------------+

    The second part of the routine uses two arrays
    rownum[] and colnum[]. It tests, if there are some
    empty columns in the matrix and prints a
    Warning message in this case.

    In detail:
    if (!lp->rowEndValid)
    malloc space for arrays num and rownum.
    initialise with zero
    count in rownum[i] how many coefficients are in row i
    set rowEnd (ATTENTION: documentation of rowEnd
    seems to be wrong. rowEnd points to LAST
    coefficient in row. But this is never used??)
    colNo[0] is not used!!!
    loop through all the columns,
    forget row[0] = objective row
    write column index in array colNo.
    free num, rownum
    rowEndValid = TRUE
    if (!lp->valid)
    Calloc rownum, colnum.
    for all columns
    colnum[i]++, for every coefficient in column.

    if colnum[i] = 0, print warning.
    */
   public int isValid() throws Exception
   {
      int i, j, rownum[], colnum[];
      int num[], rowNr;
      if(!(rowEndValid != FALSE))
      {

         //MALLOC(num, lp->rows + 1);
         num = new int[rows + 1];

         //MALLOC(rownum, lp->rows + 1);
         rownum = new int[rows + 1];
         for(i = 0;i <= rows;i++)
         {
            num[i] = 0;
            rownum[i] = 0;
         }
         for(i = 0;i < nonZeros;i++)
            rownum[mat[i].rowNr]++;
         rowEnd[0] = 0;
         for(i = 1;i <= rows;i++)
            rowEnd[i] = rowEnd[i - 1] + rownum[i];
         for(i = 1;i <= columns;i++)
            for(j = colEnd[i - 1];j < colEnd[i];j++)
            {
               rowNr = mat[j].rowNr;
               if(rowNr != 0)
               {
                  num[rowNr]++;
                  colNo[rowEnd[rowNr - 1] + num[rowNr]] = i;
               }
            }

         //free(num);

         //free(rownum);
         rowEndValid = TRUE;
      }
      if(valid!=FALSE)
         return (TRUE);

      //CALLOC(rownum, lp->rows + 1);
      rownum = new int[rows + 1];

      //CALLOC(colnum, lp->columns + 1);
      colnum = new int[columns + 1];
      for(i = 1;i <= columns;i++)
         for(j = colEnd[i - 1];j < colEnd[i];j++)
         {
            colnum[i]++;
            rownum[mat[j].rowNr]++;
         }
      for(i = 1;i <= columns;i++)
         if(colnum[i] == 0)
         {
            if(namesUsed!=FALSE)
               warning("Warning: Variable " + colName[i] + " not used in any constraints\n");
            else
               warning("Warning: Variable " + i + " not used in any constraints\n");
         }
      valid = TRUE;
      return (TRUE);
   }

   /** simple REALLOC */
   public void resizeEta(int minSize) throws Exception
   {
      while(etaAlloc <= minSize)
         etaAlloc *= 1.5;

      /* fprintf(stderr, "resizing eta to size %d\n", lp->etaAlloc); */

      //REALLOC(lp->etaValue, lp->etaAlloc + 1);
      double[] new_etaValue = new double[etaAlloc + 1];
      System.arraycopy(etaValue, 0, new_etaValue, 0, etaValue.length);
      etaValue = new_etaValue;

      //REALLOC(lp->etaRowNr, lp->etaAlloc + 1);
      int[] new_etaRowNr = new int[etaAlloc + 1];
      System.arraycopy(etaRowNr, 0, new_etaRowNr, 0, etaRowNr.length);
      etaRowNr = new_etaRowNr;
   } /* resize_eta */

   /**
   * if necessary:
   * resizeEta()
   * For all rows
   * if i <> rowNr && pcol[i] <> 0
   * Eta_rowNr = i
   * Eta_value  = pcol[i]
   * elnr++
   *
   * Last Action: write element for diagonal
   * Eta_rowNr = rowNr
   * Eta_value = pcol[rowNr]
   *
   * update Eta_colEnd
   */
   public void condenseColumn(int rowNr, double[] pcol) throws Exception
   {
      int i, elnr, minSize;
      elnr = etaColEnd[etaSize];
      minSize = elnr + rows + 2;
      if(minSize >= etaAlloc) /* maximum local growth of Eta */
         resizeEta(minSize);
      for(i = 0;i <= rows;i++)
         if(i != rowNr && pcol[i] != 0)
         {
            etaRowNr[elnr] = i;
            etaValue[elnr] = pcol[i];
            elnr++;
         }
      etaRowNr[elnr] = rowNr;
      etaValue[elnr] = pcol[rowNr];
      elnr++;
      etaColEnd[etaSize + 1] = elnr;
   } /* condensecol */


/**
* return the ith member of the bestSolution[]
*/
public double getBestSolution(int i)
{
  return bestSolution[i];
}

/**
* get the number of rows
*/
public int getRows()
{
  return rows;
}

/**
* get the number of columns
*/
public int getColumns()
{
  return columns;
}



private void printIndent(int level)
{
  int i;

  print(""+level);
  if(level < 50) /* useless otherwise */
    for(i = level; i > 0; i--)
      print("--");
  else
    print(" *** too deep ***");
  print("> ");
} /* print_indent */


public void debugPrintSolution(int level)
{
  int i;

  if(debug != FALSE)
    for (i = rows + 1; i <= sum; i++)
      {
         printIndent(level);
        if (namesUsed != FALSE)
           println(colName[i - rows] + " " + solution[i]);
        else
          println("Var[" + (i-rows) + "]   " + solution[i]);
      }
} /* debug_printSolutionutionution */


public void debugPrintBounds(int level, double[] upperBound, double[] lowerBound)
{
  int i;

  if(debug != FALSE)
    for(i = rows + 1; i <= sum; i++)
      {
   if(lowerBound[i] != 0)
     {
       printIndent(level);
            if (namesUsed != FALSE)
               println(colName[i - rows] + " > " + lowerBound[i]);
            else
              print("Var[" + (i-rows) + "]  > " + lowerBound[i]);
     }
   if(upperBound[i] != this.infinite)
     {
       printIndent(level);
       if (namesUsed != FALSE)
              println(colName[i - rows] + " < " +
             upperBound[i]);
            else
              println("Var[" + (i-rows) + "]  < " + upperBound[i]);
          }
      }
} /* debug_print_bounds */


public void debugPrint(int level, String format)
{
  if(debug != FALSE)
    {
      printIndent(level);
      print(format);
    }
} /* debug_print */


   public void warning(String str)
   {
      println(str);
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

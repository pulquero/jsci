package com.cflex.util.lpSolve;

/**
* @author Rodrigo Almeida Gonçalves
*/
public interface LpConstant
{
   final static int FAIL = -1;
   final static int NULL = 0;
   final static int FALSE = 0;
   final static int TRUE = 1;
   final static int DEFNUMINV = 50;
   final static int INITIAL_MAT_SIZE = 10000;

   /* solve status values */
   final static int OPTIMAL = 0;
   final static int MILP_FAIL = 1;
   final static int INFEASIBLE = 2;
   final static int UNBOUNDED = 3;
   final static int FAILURE = 4;
   final static int RUNNING = 5;

   /* lag_solve extra status values */
   final static int FEAS_FOUND = 6;
   final static int NO_FEAS_FOUND = 7;
   final static int BREAK_BB = 8;
   final static int FIRST_NI = 0;
   final static int RAND_NI = 1;
   final static int LE = 0;
   final static int EQ = 1;
   final static int GE = 2;
   final static int OF = 3;
   final static int MAX_WARN_COUNT = 20;
   final static double DEF_INFINITE = 1e24; /* limit for dynamic range */
   final static double DEF_EPSB = 5.01e-7; /* for rounding RHS values to 0 determine
                                          infeasibility basis */
   final static double DEF_EPSEL = 1e-8; /* for rounding other values (vectors) to 0 */
   final static double DEF_EPSD = 1e-6; /* for rounding reduced costs to zero */
   final static double DEF_EPSILON = 1e-3; /* to determine if a float value is integer */
   final static double PREJ = 1e-3; /* pivot reject (try others first) */
   final static int HASHSIZE = 10007; /* prime number is better, MB */
   final static int ETA_START_SIZE = 10000; /* start size of array Eta. Realloced if needed */
   final static String STD_ROW_NAME_PREFIX = "r_";
} // end of interface constant

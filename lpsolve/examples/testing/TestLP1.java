import java.util.*;
import java.lang.*;
import javax.swing.*;

import com.cflex.util.lpSolve.*;

/**
 * Title:        lp_solve
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:      CFLEX
 * @author Rodrigo Almeida Gonçalves
 * @version
 */
public class TestLP1
{

  public static void main(String args[])
  {
   try
   {
       LpModel lpIn = new LpModel(2, 2);

       lpIn.debug = LpModel.TRUE;
       lpIn.verbose = LpModel.TRUE;
       lpIn.trace = LpModel.TRUE;

       double v[] = new double[3];
       double rhs;
       v[1] = -1;
       v[2] = 2;
       lpIn.setObjFn(v);
       lpIn.setMaximum();
       v[1] = 2;
       v[2] = 1;
       rhs = 5;
       lpIn.addConstraint(v, LpModel.LE, rhs);
       v[1] = -4;
       v[2] = 4;
       rhs = 5;
       lpIn.addConstraint(v, LpModel.LE, rhs);
       lpIn.setInt(1, LpModel.TRUE);
       lpIn.setInt(2, LpModel.TRUE);

       LpSolver lpSolve = new LpSolver(lpIn);
       SolverViewer viewer = lpSolve.getViewer();
       SolverFrame frame = new SolverFrame(viewer);


   } catch (Exception ex)
   {
      ex.printStackTrace();
   }
  } // end of main
} // end of class TestLp


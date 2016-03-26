import java.util.*;
import java.lang.*;
import java.io.*;
import javax.swing.*;
import com.cflex.util.lpSolve.*;

/**
 * @author Rodrigo Almeida Gonçalves
 */
public class TestLP0 //implements Constant
{
  public static void main(String args[])
  {
   try
   {
    LpModel lpIn = new LpModel(0, 16);
    double v[] = new double[17];
    double rhs;
    String s;
    s = "0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 ";
    lpIn.strSetObjFn(s);
    lpIn.setMaximum();
    rhs = 1;
    String str[] = { "0 0 1 1 0 0 1 1 0 0 0 0 0 0 0 0",
             "0 0 0 0 0 0 0 0 1 1 0 0 1 1 0 0",
             "0 0 0 0 0 0 0 0 0 0 0 0 1 1 1 0",
             "0 0 0 1 0 0 0 1 0 0 0 1 0 0 0 0",
             "0 0 0 0 0 0 0 0 1 1 0 1 0 0 0 0",
             "0 0 1 0 0 0 1 0 0 0 0 0 0 0 1 0",
             "0 0 0 0 1 0 1 1 0 0 0 0 0 0 0 0",
             "0 1 0 0 0 0 0 0 0 1 0 0 0 1 0 0",
             "0 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0",
             "0 0 0 0 1 0 0 0 1 0 0 0 1 0 0 0"
             };

    for (int i = 0; i < 10; i++ )
    {
      lpIn.strAddConstraint(str[i], LpModel.LE, rhs);
    }
    for (int i = 0; i <= lpIn.getRows(); i++)
    {
      lpIn.getRow(i, v);
      for (int index = 1; index <= lpIn.getColumns(); index++)
      {
         System.out.print(v[index] + " ");
      }
      System.out.println();
    }

      PrintStream ps = new PrintStream(new FileOutputStream("modelTestLp0.lp"));
      lpIn.writeLp(ps);
      ps.close();
      ps = null;

       LpSolver lpSolve = new LpSolver(lpIn);
       SolverViewer viewer = lpSolve.getViewer();
       SolverFrame frame = new SolverFrame(viewer);


  } catch (Exception ex)
  {
      ex.printStackTrace();
  }
  } // end of main
} // end of class TestLp0

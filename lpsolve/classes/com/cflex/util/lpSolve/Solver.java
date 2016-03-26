package com.cflex.util.lpSolve;

import java.lang.*;
import java.util.*;
import javax.swing.*;

/**
* @author Rodrigo Almeida Gonçalves
*/
public abstract class Solver
{
   public LpModel model;

   private Solver()
   {
      //This constructor should not be used by the final user...
   }

   public Solver(LpModel model)
   {
      this.model = model;
      state = STATE_0_NOT_INITIALIZED;
      currentThread = null;
   }

   public int state = 0;
   public static final int STATE_0_NOT_INITIALIZED = 0;
   public static final int STATE_1_INITIALIZING = 1;
   public static final int STATE_2_INITIALIZED = 2;
   public static final int STATE_3_RUNNING = 3;
   public static final int STATE_4_PAUSED = 4;
   public static final int STATE_5_FINISHED = 5;
   public static final int STATE_6_CANCELED = 6;


   public abstract int solve() throws Exception;

   SolverThread currentThread;
   class SolverThread extends Thread
   {
      Solver solver;
      public SolverThread(Solver solver)
      {
         this.solver = solver;
      }

      public void run()
      {
         try
         {
            //this.setPriority(Thread.MAX_PRIORITY);

            state = STATE_3_RUNNING;
            viewer.stateChanged();
            int result = solver.solve();
            if (state != STATE_6_CANCELED)
            {
               if (result == LpModel.OPTIMAL)
               {
                  getResults();
               }
//               else
//               {
//                  int ret = JOptionPane.showConfirmDialog(null,"O sistema não tem solução, você deseja recuperar um resultado parcial?","Sistema sem solução",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
//                  if (ret == JOptionPane.YES_OPTION)
//                  {
//                     System.out.println("Capturando resultado parcial:");
//                     getResults();
//                  }
//               }
               viewer.finished(result);
               state = STATE_5_FINISHED;
            } else
            {
               viewer.finished(-1);
            }
            viewer.stateChanged();
         } catch (Exception ex)
         {
            ex.printStackTrace();
            viewer.error(ex);
         }
      }
   }

   /**
   * Extend this class to get the results into your program
   */
   public void getResults() throws Exception
   {
      model.printSolution();
   }

   /**
   * Starts the optimization
   */
   public void startSolver()
   {
      if (state < STATE_3_RUNNING)
      {
         currentThread = new SolverThread(this);
         currentThread.start();
      } else
      if (state == STATE_4_PAUSED)
         pauseSolver();
   }

   public SolverListener viewer;

   public SolverViewer getViewer() throws Exception
   {
      if (viewer==null)
         viewer = new SolverViewer(this);
      return (SolverViewer) viewer;
   }

   /**
   * Pauses solver
   */
   public void pauseSolver()
   {
      if (state == STATE_4_PAUSED)
      {
         state = STATE_3_RUNNING;
         currentThread.notify();
         return;
      } else
      if (state == STATE_3_RUNNING)
         state = STATE_4_PAUSED;
      else
         JOptionPane.showMessageDialog(null,"Trying to pause at a invalid state. Ignored","Error",JOptionPane.ERROR_MESSAGE);
   }

   /**
   * Terminate algorithm.
   * Termina o algoritmo
   */
   public void stopSolver()
   {
      state = STATE_6_CANCELED;
   }

   /**
   * Pega a iteracao corrente
   */
   public long getCurrentIteraction()
   {
      return model.iter;
   }


}

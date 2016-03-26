package com.cflex.util.lpSolve;

import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.Dimension.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.io.*;
import java.net.*;


/**
* Esta classe implementa uma GUI que controla o fluxo de vários algoritmos do sistema
* @author Rodrigo Almeida Gonçalves
*/
public class SolverViewer extends JPanel implements ActionListener, SolverListener
{

   /** Aponta para o solver relacionado com este viewer */
   public Solver solver;
   public Display display;
   public JButton playJB, pauseJB, stopJB, dismissJB;
   public JButton clearJB;
   public JCheckBox debugJCB, verboseJCB, traceJCB;
   public JTextArea messagesJTA;

   public ImageIcon canceledIMG, optimalIMG, unboundedIMG, nofeasfoundIMG, errorIMG;

   public void actionPerformed(ActionEvent e)
   {
      if (e.getSource() == playJB)
      {
         display.clearImage();
         solver.startSolver();
         stopJB.setEnabled(true);
         playJB.setEnabled(false);
         pauseJB.setEnabled(true);
         dismissJB.setEnabled(false);
      } else
      if (e.getSource() == stopJB)
      {
         solver.stopSolver();
         playJB.setEnabled(false);
         pauseJB.setEnabled(false);
         stopJB.setEnabled(false);
         dismissJB.setEnabled(true);
      } else
      if (e.getSource() == pauseJB)
      {
         if (solver.state == solver.STATE_3_RUNNING)
            stopJB.setEnabled(false);
         else
            stopJB.setEnabled(true);
         solver.pauseSolver();
      } else
      if (e.getSource() == debugJCB)
      {
         if (debugJCB.isSelected())
            solver.model.debug = LpConstant.TRUE;
         else
            solver.model.debug = LpConstant.FALSE;
      } else
      if (e.getSource() == verboseJCB)
      {
         if (verboseJCB.isSelected())
            solver.model.verbose = LpConstant.TRUE;
         else
            solver.model.verbose = LpConstant.FALSE;
      } else
      if (e.getSource() == traceJCB)
      {
         if (traceJCB.isSelected())
            solver.model.trace = LpConstant.TRUE;
         else
            solver.model.trace = LpConstant.FALSE;
      } else
      if (e.getSource() == clearJB)
      {
         messagesJTA.setText("");
      } else
      if (e.getSource() == dismissJB)
      {
         if (solver.state == solver.STATE_5_FINISHED ||
             solver.state == solver.STATE_6_CANCELED)
         {
            solver.model=null;
            solver = null;
            System.gc();

            Component c = this;
            while(c != null &&
                  !(c instanceof JInternalFrame) &&
                  !(c instanceof JFrame))
               c = (Component) c.getParent();

            if (c!=null)
            {
               if (c instanceof JInternalFrame)
               {
                  JInternalFrame ji = (JInternalFrame) c;
                  ji.setVisible(false);
                  ji.dispose();
               } else
               if (c instanceof JFrame)
               {
                  JFrame jf = (JFrame) c;
                  jf.setVisible(false);
                  jf.dispose();
               }
            }
         }
      }
   }

   public SolverViewer()
   {
   }

   public SolverViewer(Solver solver) throws Exception
   {

      solver.viewer = this;
      solver.model.viewer = this;
      this.solver = solver;

      display = new Display();
      playJB = new JButton("Play");
      pauseJB = new JButton("Pause");
      stopJB = new JButton("Stop");
      clearJB = new JButton("Clear");
      dismissJB = new JButton("Dismiss");

      debugJCB = new JCheckBox("Debug");
      verboseJCB = new JCheckBox("Verbose");
      traceJCB = new JCheckBox("Trace");

      messagesJTA = new JTextArea();

      JPanel buttonPanel = new JPanel(new FlowLayout());
      buttonPanel.add(playJB);
      buttonPanel.add(pauseJB);
      buttonPanel.add(stopJB);
      buttonPanel.add(dismissJB);

      JPanel checkPanel = new JPanel(new FlowLayout());
      checkPanel.add(debugJCB);
      checkPanel.add(verboseJCB);
      checkPanel.add(traceJCB);
      checkPanel.setBorder(new TitledBorder("Controls"));

      JPanel controlsPanel = new JPanel(new BorderLayout());
      controlsPanel.add(checkPanel,BorderLayout.CENTER);
      controlsPanel.add(clearJB,BorderLayout.EAST);

      JPanel outroPanel = new JPanel(new BorderLayout());
      outroPanel.add(controlsPanel,BorderLayout.NORTH);
      outroPanel.add(new JScrollPane(messagesJTA),BorderLayout.CENTER);
      outroPanel.add(buttonPanel,BorderLayout.SOUTH);

      canceledIMG = loadImageIcon("images/canceled.gif");
      optimalIMG = loadImageIcon("images/optimal.gif");
      unboundedIMG = loadImageIcon("images/unbounded.gif");
      nofeasfoundIMG = loadImageIcon("images/nofeasfound.gif");
      errorIMG = loadImageIcon("images/error.gif");

      this.setLayout(new BorderLayout());
      this.add(display,BorderLayout.NORTH);
      this.add(outroPanel,BorderLayout.CENTER);

      playJB.addActionListener(this);
      pauseJB.addActionListener(this);
      stopJB.addActionListener(this);
      debugJCB.addActionListener(this);
      verboseJCB.addActionListener(this);
      traceJCB.addActionListener(this);
      clearJB.addActionListener(this);
      dismissJB.addActionListener(this);

      debugJCB.setSelected((solver.model.debug == LpConstant.TRUE));
      verboseJCB.setSelected((solver.model.verbose == LpConstant.TRUE));
      traceJCB.setSelected((solver.model.trace == LpConstant.TRUE));
      stopJB.setEnabled(false);
   }

   /**
   * Le uma gif
   */
   public ImageIcon loadImageIcon(String filename)
   {
      URL url;
      url = getClass().getResource(filename);
      if (url==null)
         return null;
      return new ImageIcon(url, null);
   }

   /**
   * Envia uma mensagem para o viewer com algum aviso sobre o algoritmo ou sobre seu estado
   */
   public void message(String msg)
   {
      messagesJTA.append(msg);
   }
   public void messageln(String msg)
   {
      message(msg+"\n");
   }
   public void errorMessage(String msg)
   {
      messageln(msg);
   }

   /** Notifica o viewer o passo atual do algoritmo */
   public void stepUpdate(long step)
   {
      display.timedSetValue(step);
   }

   public void stateChanged()
   {
      repaint(0);
   }

   public void error(Exception ex)
   {
      ex.printStackTrace();
      display.setImage(errorIMG);
      messageln(ex.getClass().getName()+ex.getMessage());
      stopJB.setEnabled(false);
      playJB.setEnabled(false);
      pauseJB.setEnabled(false);
      dismissJB.setEnabled(true);
      repaint();
   }

   public void finished(int status)
   {
      switch (status)
      {
         case -1:
            display.setImage(canceledIMG);
            break;
         case LpConstant.OPTIMAL:
            display.setImage(optimalIMG);
            break;
         case LpConstant.UNBOUNDED:
            display.setImage(unboundedIMG);
            break;
         case LpConstant.MILP_FAIL:
            display.setImage(nofeasfoundIMG);
            break;
         case LpConstant.FAILURE:
            display.setImage(nofeasfoundIMG);
            break;
         case LpConstant.INFEASIBLE:
            display.setImage(nofeasfoundIMG);
            break;
         default:
            display.setImage(errorIMG);
            break;
      }
      try{Thread.sleep(500);}catch(Exception ex){ex.printStackTrace();}
      stopJB.setEnabled(false);
      playJB.setEnabled(false);
      pauseJB.setEnabled(false);
      dismissJB.setEnabled(true);
      repaint();
   }
}

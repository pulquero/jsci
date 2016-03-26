package com.cflex.util.lpSolve;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Title:        lp_solve
 * Description:
 * Copyright:    Copyright (c) 1999
 * Company:      CFLEX
 * @author Rodrigo Almeida Gonçalves
 * @version
 */

public class SolverFrame extends JFrame
{
   public SolverFrame(SolverViewer viewer)
   {
      this.setSize(500,300);
      this.getContentPane().setLayout(new BorderLayout());
      this.getContentPane().add(viewer, BorderLayout.CENTER);
      this.setVisible(true);
   }
}

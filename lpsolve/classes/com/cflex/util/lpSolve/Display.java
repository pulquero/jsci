package com.cflex.util.lpSolve;

import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.net.URL;
import java.beans.*;
import java.lang.reflect.*;

/**
* Display de números utilizado para contadores
* @author Rodrigo Almeida Gonçalves
*/
public class Display extends JPanel
{
  long value=0;
  long lastUpdate = 0;
  int nDigits;
  Color backgroundColor;
  public long updateTimeInterval = 1500;
  Image[] digitos;
  Image image;

   /** Le uma gif */
   public ImageIcon loadImageIcon(String filename)
   {
      URL url;
      url = getClass().getResource(filename);
      if (url==null)
         return null;
      return new ImageIcon(url, null);
   }

  /** Muda o valor mas com repaint só a cada dT (=1500ms de default) */
  public void timedSetValue(long number)
  {
      value = number;
      long agora = System.currentTimeMillis();
      if (agora-lastUpdate > updateTimeInterval)
      {
         lastUpdate = agora;
         setValue(number);
      }
  }
  /** Set value */
  public void setValue(long number)
  {
     value = number;
     repaint(0);
  }
  /** Incrementa */
  public void increment()
  {
     if (value==Long.MAX_VALUE)
      value=0;
     value=value+1;
     setValue(value);
  }
  /** Incrementa */
  public void timedIncrement()
  {
     if (value==Long.MAX_VALUE)
      value=0;
     value=value+1;
     timedSetValue(value);
  }
  /** Ajusta o numero de digitos */
  public void setDigits(int i)
  {
     nDigits = i;
     repaint(0);
  }

  public Display()
  {
      super(true);
      value = 0;
      backgroundColor = Color.black;
      nDigits = 8;
      lastUpdate = 0;
      leDigitos();
      image = null;
      setPreferredSize(new Dimension(408,60));
  }

  public void setImage(ImageIcon icon)
  {
      this.image = icon.getImage();
      repaint(0);
  }

  public void clearImage()
  {
      this.image = null;
      repaint(0);
  }


  /** Le digitos de um determinado estilo (subdir)*/
  public void leDigitos()
  {
      digitos = new Image[10];
      for (int i=0; i<10; i++)
      {
         ImageIcon icon = loadImageIcon("images/"+i+".gif");
         if (icon==null)
         {
            JOptionPane.showMessageDialog(null,"NOT FOUND:"+"images/"+i+".gif ABORTING!","Error",JOptionPane.ERROR_MESSAGE);
            System.exit(0);
         }
         else
            digitos[i] = icon.getImage();
      }
  }

   /** Desenha o seven seg */
   public void paint(Graphics g)
   {
      //Apaga o desenho anterior com a cor de fundo
      Dimension d = getSize();
      double sizeX = d.width;
      double sizeY = d.height;
      g.setColor(backgroundColor);
      g.fillRect(0,0,(int)sizeX,(int)sizeY);
      if (image == null)
      {
         //Pega o tamanho das figuras
         int sizeImageX = (int) (sizeX/(double)nDigits);
         int sizeImageY = (int) sizeY;
         String numero = Long.toString(value);
         char[] numeroChar = numero.toCharArray();
         int last = numeroChar.length;
         int currentPos = 0;
         for (int i=0; i<nDigits; i++)
         {
            int index = last-nDigits+i;
            if (index<0)
               g.drawImage(digitos[0], currentPos, 0, sizeImageX, sizeImageY, null);
            else
               g.drawImage(digitos[numeroChar[index]-'0'], currentPos, 0, sizeImageX, sizeImageY, null);
            currentPos+=sizeImageX;
         }
      } else
      {
         g.drawImage(image,0,0,(int)sizeX,(int)sizeY, null);
      }
   }

   public static void main(String argv[])
   {
      Display disp = new Display();
      JFrame fr = new JFrame("Teste");
      fr.getContentPane().add(disp);
      fr.setSize(200,100);
      fr.show();
      System.out.println("Ok");
      for (long i=0; i<Long.MAX_VALUE; i++)
         for (long c=0; c<Long.MAX_VALUE; c++)
         {
            disp.setValue(c);
            try
            {
               Thread.sleep(1000);
            } catch (Exception e)
            {
            }
         }
   }
}

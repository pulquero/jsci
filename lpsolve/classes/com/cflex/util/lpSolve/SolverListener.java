package com.cflex.util.lpSolve;

/**
* This interface should be implemented by those who want to interate with the solver
* @author Rodrigo Almeida Gonçalves
*/
public interface SolverListener
{
   /** Envia uma mensagem para o viewer com algum aviso sobre o algoritmo ou sobre seu estado */
   public void message(String msg);
   public void messageln(String msg);
   public void errorMessage(String msg);
   /** Notifica o viewer o passo atual do algoritmo */
   public void stepUpdate(long step);
   public void stateChanged();
   public void error(Exception ex);
   public void finished(int status);
}

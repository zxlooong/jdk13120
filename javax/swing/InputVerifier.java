/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.swing;

import java.util.*;

/**
 * The purpose of this class is to help clients support smooth focus
 * navigation through GUIs with text fields. Such GUIs often need
 * to ensure that the text entered by the user is valid (for example,
 * that it's in
 * the proper format) before allowing the user to navigate out of
 * the text field. To do this, clients create a subclass of
 * InputVerifier and, using JComponent's <code>setInputVerifier</code> method,
 * attach an instance of their subclass to the JComponent whose input they
 * want to validate. Before focus is transfered to another Swing component
 * that requests it, the input verifier's <code>shouldYieldFocus</code> method is
 * called.  Focus is transfered only if that method returns <code>true</code>.
 * <p>
 * The following example has two text fields, with the first one expecting
 * the string "pass" to be entered by the user. If that string is entered in
 * the first text field, then the user can advance to the second text field
 * either by clicking in it or by pressing TAB. However, if another string
 * is entered in the first text field, then the user will be unable to 
 * transfer focus to the second text field.
 * <p>
 * <pre>
 * import java.awt.*;
 * import java.util.*;
 * import java.awt.event.*;
 * import javax.swing.*;
 * 
 * // This program demonstrates the use of the Swing InputVerifier class.
 * // It creates two text fields; the first of the text fields expects the
 * // string "pass" as input, and will allow focus to advance out of it
 * // only after that string is typed in by the user.
 *
 * class VerifierTest extends JFrame {
 *
 *  public VerifierTest () {
 *    JTextField tf;
 *    tf = new JTextField ("TextField1");
 *			   
 *    getContentPane().add (tf, BorderLayout.NORTH);
 *    tf.setInputVerifier(new PassVerifier());
 *
 *    tf = new JTextField ("TextField2");
 *    getContentPane().add (tf, BorderLayout.SOUTH);
 *                  
 *    addWindowListener (new MyWAdapter ());
 *  }
 *
 *
 *
 *   public static void main (String [] args) {
 *     Frame f = new VerifierTest ();
 *     f.pack();
 *     f.show();
 *   }
 *
 *   class MyWAdapter extends WindowAdapter {
 *
 *     public void windowClosing(WindowEvent event) {
 *       System.exit (0);
 *     }
 *   }
 *               
 *   class PassVerifier extends InputVerifier {
 *
 *     public boolean verify(JComponent input) {
 *       JTextField tf = (JTextField) input;
 *       String pass = tf.getText();
 *       if (pass.equals("pass")) return true;
 *       else return false;
 *     }
 *   }
 * }
 * </pre>
 *
 *  @since 1.3
 */


public abstract class InputVerifier {

  /**
   * Checks whether the JComponent's input is valid. This method should
   * have no side effects. It returns a boolean indicating the status
   * of the argument's input.
   *
   * @param input the JComponent to verify
   * @return <code>true</code> when valid, <code>false</code> when invalid
   * @see JComponent#setInputVerifier
   * @see JComponent#getInputVerifier
   *
   */
  
  public abstract boolean verify(JComponent input);


  /**
   * Calls <code>verify(input)</code> to ensure that the input is valid.
   * This method can have side effects. In particular, this method
   * is called when the user attempts to advance focus out of the
   * argument component into another Swing component in this window.
   * If this method returns <code>true</code>, then the focus is transfered
   * normally; if it returns <code>false</code>, then the focus remains in
   * the argument component.
   *
   * @param input the JComponent to verify
   * @return <code>true</code> when valid, <code>false</code> when invalid
   * @see JComponent#setInputVerifier
   * @see JComponent#getInputVerifier
   *
   */

  public boolean shouldYieldFocus(JComponent input) {
    return verify(input);
  }

}

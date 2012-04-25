/* TOTEM-v3.2 June 18 2008*/

/*
 * ===========================================================
 * TOTEM : A TOolbox for Traffic Engineering Methods
 * ===========================================================
 *
 * (C) Copyright 2004-2006, by Research Unit in Networking RUN, University of Liege. All Rights Reserved.
 *
 * Project Info:  http://totem.run.montefiore.ulg.ac.be
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License version 2.0 as published by the Free Software Foundation;
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
*/

package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/*
* Changes:
* --------
* 10-Feb.-2006 : Change layout, add cancel button. (GMO)
* 04-Apr.-2006 : Default cancel action added, isCancelled ca be used to check if the button was pushed. (GMO)
* 13-Mar.-2007 : Add constructor with width parameter (GMO)
*
*/


/**
 * A Class that is responsible for displaying a panel with a Progression Bar when some computation are done
 *
 * @author : Olivier Materne ( O.Materne@student.ulg.ac.be)
 *
 */
public class ProgressBarPanel extends JPanel {
    private JProgressBar progressBar = null;
    private JLabel text = null;
    private JButton cancel = null;
    private boolean cancelable = false;
    private boolean cancelled = false;


    /**
     * A constructor, with initial and final value for the progression bar
     *
     * @param min initial value
     * @param max final value
     */
    public ProgressBarPanel(int min, int max) {
        progressBar = new JProgressBar(min, max);
        text = new JLabel("");
        text.setHorizontalAlignment(SwingConstants.CENTER);

        this.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        this.add(buttonPanel, BorderLayout.SOUTH);
        this.add(centerPanel, BorderLayout.CENTER);

        centerPanel.add(progressBar);
        centerPanel.add(text);
        cancel = new JButton("Cancel Operation");
        cancel.setEnabled(false);
        cancel.addActionListener(new DefaultCancelAction());
        buttonPanel.add(cancel, Box.CENTER_ALIGNMENT);
    }

    /**
     * A constructor, with initial and final value for the progression bar
     *
     * @param min initial value
     * @param max final value
     * @param width progressbar preferred width
     */
    public ProgressBarPanel(int min, int max, int width) {
        this(min, max);
        progressBar.setPreferredSize(new Dimension(width, progressBar.getPreferredSize().height));
    }

    /**
     * @return the progression bar
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Change the value of the Progression Bar to the given number
     *
     * @param i the new value for the progression bar
     */
    public void setValue(int i) {
        progressBar.setValue(i);
    }

    /**
     * Change Maximum value of the progression bar
     *
     * @param i new final value
     */
    public void setMaximum(int i) {
        progressBar.setMaximum(i);
    }


    /**
     * Associate a message with this progression bar
     *
     * @param msg the message to be displayed
     */
    public void setMessage(String msg) {
        text.setText(msg);
    }

    public void setCancelAction(ActionListener l) {
        cancel.addActionListener(l);
    }

    /**
     * return true if the operation can be cancelled
     * @return
     */
    public boolean isCancelable() {
        return cancelable;
    }

    /**
     * Tells that the operation is cancelable or not. if cancelable is false, disable the cancel button.
     * @param cancelable
     */
    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        cancel.setEnabled(cancelable);
    }

    /**
     * return true if the operation was cancelled (by clicking on the cancel button)
     * @return
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Set the status of the bar to cancelled state, disable cancel button.
     */
    private void cancel() {
        cancel.setEnabled(false);
        this.cancelled = true;
    }

    /**
     * Call the cancel method when the action is performed. 
     */
    private class DefaultCancelAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (isCancelable())
                cancel();
        }
    }
}

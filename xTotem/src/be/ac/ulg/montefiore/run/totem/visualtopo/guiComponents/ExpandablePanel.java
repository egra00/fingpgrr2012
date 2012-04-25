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
* - 08-Jun-2007: Add setExpanded(.) method (GMO)
*/

/**
 * An expandable JPanel.<br>
 * When not expanded, it displays only a button with the provided name.
 * A click on this button will display the panel just below the button.
 * The default state of the panel is not expanded. When the panel is expanded or retracted the parent window is repacked
 * by a call to the <code>pack()</code> method.
 *
 * <p>Creation date: 19/10/2006
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ExpandablePanel extends JPanel {
    private AbstractButton button;
    private JPanel panel;
    private Window parent;
    private String name;

    /**
     * Create an retracted expandable panel.
     * @param parent Parent window (will be repacked after state change)
     * @param name Button name
     * @param panel Panel to display in expanded mode
     */
    public ExpandablePanel(Window parent, String name, JPanel panel) {
        super(new BorderLayout());

        button = new JButton();

        this.name = name;
        this.parent = parent;
        this.panel = panel;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        button.addActionListener(new ExpandActionListener());
        buttonPanel.add(button);
        add(buttonPanel, BorderLayout.NORTH);

        button.setText(getButtonText(false));
        panel.setVisible(false);
        add(panel, BorderLayout.CENTER);
    }

    private String getButtonText(boolean expanded) {
        if (name == null)
            return expanded ? "Retract <<<" : "Expand >>>";
        else return expanded ? (name + " <<<") : (name + " >>>");
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean isExpanded() {
        return panel.isVisible();
    }

    public void setExpanded(boolean expanded) {
        panel.setVisible(expanded);
        button.setText(getButtonText(expanded));

        //validate();
        if (parent != null) parent.pack();
    }

    public void setButtonToolTipText(String text) {
        button.setToolTipText(text);
    }

    private class ExpandActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            setExpanded(!isExpanded());
        }
    }

}

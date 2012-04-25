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

import org.apache.log4j.WriterAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import javax.swing.*;
import java.io.PrintStream;
import java.io.OutputStream;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/*
* Changes:
* --------
*
*
*/

/**
* Dialog containing a textarea that represent standard output, error output and a log4j Appender.
* You can set the log4j level of debugging via a combobox.
*
* <p>Creation date: 24 janv. 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class Console extends JDialog {
    PrintStream out;
    PrintStream err;
    JTextArea textArea;
    WriterAppender wa;
    Level OldLevel;
    JComboBox levelCombo;

    public Console() {
        super(MainWindow.getInstance(), "Console");

        OldLevel = Logger.getRootLogger().getLevel();
        setupUI();

        out = System.out;
        err = System.err;
        OutputStream output = new TextAreaOutputStream(textArea);
        PrintStream ps = new PrintStream(output);
        System.setOut(ps);
        System.setErr(ps);

        wa = new WriterAppender(new PatternLayout("%-5p [%c{1}]: %m%n"), output);
        Logger.getRootLogger().addAppender(wa);


    }

    private void setupUI() {
        setLayout(new BorderLayout());

        /* Build noth panel */
        JPanel northPanel = new JPanel(new FlowLayout());
        Level[] items = {Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL};
        levelCombo = new JComboBox(items);
        levelCombo.addActionListener(new ComboActionListener());
        levelCombo.setSelectedItem(OldLevel);
        northPanel.add(levelCombo);

        JButton clearBtn = new JButton("clear");
        clearBtn.addActionListener(new ClearActionListener());
        northPanel.add(clearBtn);

        add(northPanel, BorderLayout.NORTH);

        /* Build center components */
        textArea = new JTextArea();
        textArea.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setSize(120, 120);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }


    public void dispose() {
        super.dispose();
        System.setOut(out);
        System.setErr(err);
        Logger.getRootLogger().removeAppender(wa);
        Logger.getRootLogger().setLevel(OldLevel);
    }

    /**
     * Set level of debugging
     * @param level
     */
    public void setLevel(Level level) {
        Logger.getRootLogger().setLevel(level);
        levelCombo.setSelectedItem(level);
    }

    /**
     * DEBUG, INFO, WARN, ERROR, FATAL, ALL, OFF
     * @param level
     */
    public void setLevel(String level) {
        setLevel(Level.toLevel(level));
    }

    /**
     * clear the text
     */
    public void clear() {
        textArea.setText("");
    }



    private class ComboActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            setLevel((Level)levelCombo.getSelectedItem());
        }
    }

    private class ClearActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            clear();
        }
    }

}

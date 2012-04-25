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

import be.ac.ulg.montefiore.run.totem.visualtopo.util.SpringUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/*
* Changes:
* --------
*
*/

/**
* Simple dialog that allows to choose a server host and port. 
*
* <p>Creation date: 19/06/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class HostPortDialog extends JDialog {

    private JTextField hostTF;
    private JTextField portTF;
    private JButton okBtn;

    public HostPortDialog() {
        super(MainWindow.getInstance(), "Server parameters");
        setupUI();
    }

    private void setupUI() {
        JPanel generalPanel = new JPanel(new BorderLayout(5, 5));

        JPanel upPanel = new JPanel(new SpringLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

        generalPanel.add(upPanel, BorderLayout.CENTER);
        generalPanel.add(buttonsPanel, BorderLayout.SOUTH);

        hostTF = new JTextField("localhost");
        portTF = new JTextField("1234");

        upPanel.add(new JLabel("Host:"));
        upPanel.add(hostTF);
        upPanel.add(new JLabel("Port:"));
        upPanel.add(portTF);

        SpringUtilities.makeCompactGrid(upPanel, 2, 2, 5, 5, 5, 5);

        okBtn = new JButton("Connect");
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonsPanel.add(okBtn);
        buttonsPanel.add(cancelBtn);

        add(generalPanel);

        getRootPane().setDefaultButton(okBtn);
    }

    public String getHost() {
        return hostTF.getText();
    }

    public void setHost(String host) {
        hostTF.setText(host);
    }

    public void setPort(int port) {
        portTF.setText(String.valueOf(port));
    }

    public int getPort() throws NumberFormatException {
        return Integer.valueOf(portTF.getText());
    }

    public void setActionListener(ActionListener l) {
        okBtn.addActionListener(l);
    }
}

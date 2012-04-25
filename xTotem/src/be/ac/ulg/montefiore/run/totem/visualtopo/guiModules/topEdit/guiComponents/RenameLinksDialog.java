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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.guiComponents;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.LinkDecorator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashSet;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
* Dialog to rename all links of the loaded domain following a given pattern.
*
* <p>Creation date: 12/12/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class RenameLinksDialog extends JDialog {
    private static final Logger logger = Logger.getLogger(RenameLinksDialog.class);

    private static final String defaultPattern = "%src-%dst";
    private static final String tooltipText = "<html>%src: source node id<br>" +
                                                    "%dst: destination node id<br>" +
                                                    "%bw: link bandwidth<br>" +
                                                    "%srcIf: source node interface id<br>" +
                                                    "%dstIf: destination node interface id" +
                                                    "</html>";
    
    private final JTextField patternField;
    private final JButton okBtn;
    private final JButton cancelBtn;


    public RenameLinksDialog() {
        super(TopEditGUI.getInstance(), "Rename all links", false);

        patternField = new JTextField(defaultPattern);
        okBtn = new JButton("Rename");
        okBtn.addActionListener(new AcceptActionListener());
        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        setupUI();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new GridBagLayout());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        
        add(mainPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 5, 5, 5);
        c.weightx = 0.0;
        c.weighty = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        mainPanel.add(new JLabel("Pattern: "), c);
        c.weightx = 1.0;
        c.gridx++;
        mainPanel.add(patternField, c);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        mainPanel.add(new JLabel(tooltipText), c);

        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);

    }

    private class AcceptActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            DomainDecorator domainDec = TopEditGUI.getInstance().getCurrentPanel().getDomainDecorator();
            if (domainDec == null) {
                JOptionPane.showMessageDialog(getParent(), "A domain must be loaded to rename links.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            HashSet<String> renamedLinks = new HashSet<String>();

            for (LinkDecorator l : domainDec.getAllLinks()) {
                //compute id string
                String idString = patternField.getText();
                String fromIfStr = l.getLink().getFrom().isSetIf() ? l.getLink().getFrom().getIf() : "";
                idString = idString.replaceAll("%srcIf", fromIfStr);

                String toIfStr = l.getLink().getTo().isSetIf() ? l.getLink().getTo().getIf() : "";
                idString = idString.replaceAll("%dstIf", toIfStr);

                idString = idString.replaceAll("%src", l.getLink().getFrom().getNode());
                idString = idString.replaceAll("%dst", l.getLink().getTo().getNode());
                
                String bwStr = "";
                if (l.getLink().isSetBw())
                    bwStr = String.valueOf(l.getLink().getBw());
                else if (l.getLinkIgp() != null && l.getLinkIgp().isSetStatic() && l.getLinkIgp().getStatic().isSetMrbw()) {
                    bwStr = String.valueOf(l.getLinkIgp().getStatic().getMrbw());
                }
                idString = idString.replaceAll("%bw", bwStr);

                int num = 0;
                String newId = idString;
                while (renamedLinks.contains(newId)) {
                    newId = idString + "-" + num++;
                }

                logger.debug("Link " + l.getLink().getId() + " renamed to " + newId);
                l.getLink().setId(newId);
                if (l.getLinkIgp() != null)
                    l.getLinkIgp().setId(newId);

                renamedLinks.add(newId);

            }
            
            dispose();
        }
    }

}

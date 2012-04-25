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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.impl;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIActionPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIAction;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.exception.BadParametersException;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.DomainElementListCellRenderer;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;

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
* Panel to choose a link to modify its capcity.
*
* <p>Creation date: 30/05/2007
*
* @see ChangeLinkCapacityWIAction
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ChangeLinkCapacityWIActionPanel extends WIActionPanel {
    private JComboBox linksCmb;
    private JTextField capacityField;
    private JCheckBox reverseLinkChk;

    public ChangeLinkCapacityWIActionPanel(Domain domain) {
        super(new GridLayout(5, 1, 5, 5));
        this.domain = domain;
        setupUI();
    }

    private void setupUI() {
        Link[] link = new Link[0];

        add(new JLabel("Choose link:"));

        linksCmb = new JComboBox(domain.getAllLinks().toArray(link));
        linksCmb.setRenderer(new DomainElementListCellRenderer());
        linksCmb.addActionListener(new LinksActionListener());
        add(linksCmb);

        reverseLinkChk = new JCheckBox("Reverse link");
        reverseLinkChk.setSelected(true);
        add(reverseLinkChk);

        add(new JLabel("Enter new Capacity:"));

        capacityField = new JTextField("0.0");

        add(capacityField);

        if (linksCmb.getSelectedItem() != null) {
            capacityField.setText(String.valueOf((((Link)linksCmb.getSelectedItem()).getBandwidth())));
        }

        setBorder(BorderFactory.createTitledBorder("Change link capacity"));

    }

    public WIAction createWIAction() throws BadParametersException {
        WIAction action;
        float bw;
        try {
            bw = Float.valueOf(capacityField.getText());
        } catch (NumberFormatException e) {
            throw new BadParametersException("Bandwidth is not a float.");
        }

        try {
            action = new ChangeLinkCapacityWIAction(domain, (Link)linksCmb.getSelectedItem(), bw, reverseLinkChk.isSelected());
        } catch (LinkNotFoundException e) {
            throw new BadParametersException(e);
        }
        return action;
    }

    public String getWIActionName() {
        return "Change Capacity";
    }

    public void destroy() {
    }

    private class LinksActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Link link = (Link)linksCmb.getSelectedItem();
            capacityField.setText(String.valueOf(link.getBandwidth()));
        }
    }
}

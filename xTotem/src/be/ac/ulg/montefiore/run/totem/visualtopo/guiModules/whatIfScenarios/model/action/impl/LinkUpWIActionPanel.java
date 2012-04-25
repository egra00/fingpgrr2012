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

import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.exception.BadParametersException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIAction;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIActionPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.DomainElementListCellRenderer;

import javax.swing.*;
import java.awt.*;

/*
* Changes:
* --------
*
*/

/**
* Panel to select a link to set up.
*
* <p>Creation date: 23/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LinkUpWIActionPanel extends WIActionPanel {
    private JComboBox linksCmb;
    private JCheckBox reverseLinkChk;

    public LinkUpWIActionPanel(Domain domain) {
        super(new GridLayout(3, 1, 5, 5));
        this.domain = domain;
        add(new JLabel("Select the link to set up."));

        Link[] link = new Link[0];
        linksCmb = new JComboBox(domain.getAllLinks().toArray(link));
        linksCmb.setRenderer(new DomainElementListCellRenderer());
        add(linksCmb);

        reverseLinkChk = new JCheckBox("Reverse link");
        reverseLinkChk.setSelected(true);
        add(reverseLinkChk);

        setBorder(BorderFactory.createTitledBorder("Link up"));
    }

    public WIAction createWIAction() throws BadParametersException {
        WIAction action;
        try {
            action = new LinkUpWIAction(domain, (Link)linksCmb.getSelectedItem(), reverseLinkChk.isSelected());
        } catch (LinkNotFoundException e) {
            throw new BadParametersException(e);
        }
        return action;
    }

    public String getWIActionName() {
        return "Link up";
    }

    /**
     * Destroys the panel. Mainly to remove listeners.
     */
    public void destroy() {
    }

}
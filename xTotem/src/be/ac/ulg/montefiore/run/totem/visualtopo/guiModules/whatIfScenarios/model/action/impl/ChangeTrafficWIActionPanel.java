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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManagerListener;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.exception.BadParametersException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIAction;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIActionPanel;

import javax.swing.*;
import java.awt.*;

/*
* Changes:
* --------
*
*/

/**
* Panel to choose a new default traffic matrix to be routed.
*
* <p>Creation date: 24/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ChangeTrafficWIActionPanel extends WIActionPanel implements TrafficMatrixManagerListener {

    private JComboBox combo;

    public ChangeTrafficWIActionPanel(Domain domain) {
        super(new GridLayout(2, 1, 5, 5));
        this.domain = domain;
        TrafficMatrixManager.getInstance().addListener(this);
        setupUI();
    }

    private void setupUI() {
        add(new JLabel("Select the new traffic matrix:"));

        Integer[] tmIds = new Integer[0];
        tmIds = TrafficMatrixManager.getInstance().getTrafficMatrices(domain.getASID()).toArray(tmIds);

        if (tmIds.length > 1) {
            combo = new JComboBox(tmIds);
            add(combo);
        } else {
            add(new Label("There should be 2 matrices loaded in order to observe changes between them."));
        }

        setBorder(BorderFactory.createTitledBorder("Change TM"));

    }

    public WIAction createWIAction() throws BadParametersException {
        if (combo == null || combo.getItemCount() < 1) throw new BadParametersException("No traffic matrix");
        TrafficMatrix tm = null;
        int tmId = ((Integer)combo.getSelectedItem()).intValue();
        try {
            tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(), tmId);
            WIAction action = new ChangeTrafficWIAction(domain, tm);
            return action;
        } catch (InvalidTrafficMatrixException e) {
            throw new BadParametersException("TrafficMatrix not found (tmId: " + tmId);
        }
    }

    public String getWIActionName() {
        return "Change TM";
    }

    /**
     * Destroys the panel. Mainly to remove listeners.
     */
    public void destroy() {
        TrafficMatrixManager.getInstance().removeListener(this);
    }

    /**
     * A TrafficMatrix has been loaded
     *
     * @param tm   the new loaded traffic matrix
     * @param tmId the id of the newly loaded matrix
     */
    public void addTrafficMatrixEvent(TrafficMatrix tm, int tmId) {
        if (tm.getASID() == domain.getASID()) {
            removeAll();
            setupUI();
            validate();
        }
    }

    /**
     * A traffic matrix has been removed
     *
     * @param tm a reference to the removed traffic Matrix
     */
    public void removeTrafficMatrixEvent(TrafficMatrix tm, int tmId) {
        if (tm.getASID() == domain.getASID()) {
            removeAll();
            setupUI();
            validate();
        }
    }

    /**
     * The default traffic matrix has changed for the domain given by its asId.
     *
     * @param asId Asid of the domain for which the traffic matrix has changed
     * @param tm   The new default traffic matrix for the domain
     */
    public void changeDefaultTrafficMatrixEvent(int asId, TrafficMatrix tm) {
    }
}

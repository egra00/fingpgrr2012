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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.exception.BadParametersException;

import javax.swing.*;
import java.awt.*;

/*
* Changes:
* --------
*
*/

/**
* Represent a panel corresponding to a WI action. The panel is used to select the parameters of a WIAction.
*  The method {@link #createWIAction()} returns the action cretaed thanks to the selected parameters.
*
* <p>Creation date: 23/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public abstract class WIActionPanel extends JPanel {

    protected Domain domain;

    public Domain getDomain() {
        return domain;
    }

    /**
     * Create a new buffered JPanel with the specified layout manager
     *
     * @param layout the LayoutManager to use
     */
    protected WIActionPanel(LayoutManager layout) {
        super(layout);
    }

    /**
     * Creates a new <code>JPanel</code> with a double buffer
     * and a flow layout.
     */
    protected WIActionPanel() {
        super();
    }

    /**
     * Create a WIAction based on the selected parameters.
     * @return
     * @throws BadParametersException
     */
    public abstract WIAction createWIAction() throws BadParametersException;

    /**
     * Get the name of the action that the panel is intended to create.
     * @return
     */
    public abstract String getWIActionName();

    /**
     * Destroys the panel. Mainly to remove listeners.
     */
    public abstract void destroy();
}

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

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIAction;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.exception.ActionExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkCapacityExceededException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.DiffServConfigurationException;
import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
* When executed, this action change the capacity of a link and possibly of the reverse link if desired.
*
* <p>Creation date: 30/05/2007
*
* @see ChangeLinkCapacityWIActionPanel
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class ChangeLinkCapacityWIAction implements WIAction {
    private final static Logger logger = Logger.getLogger(ChangeLinkCapacityWIAction.class);

    private Link link;
    private float newBw;
    private boolean reverseLink;
    private Domain domain;

    public ChangeLinkCapacityWIAction(Domain domain, Link link, float bw) throws LinkNotFoundException {
        this(domain, link, bw, false);
    }

    public ChangeLinkCapacityWIAction(Domain domain, Link link, float bw, boolean reverseLink) throws LinkNotFoundException {
        domain.getLink(link.getId());
        this.domain = domain;
        this.link = link;
        this.newBw = bw;
        this.reverseLink = reverseLink;
    }

    public void execute() throws ActionExecutionException {
        float oldBw = link.getBandwidth();

        try {
            link.setBandwidth(newBw);
        } catch (LinkCapacityExceededException e) {
            e.printStackTrace();
            throw new ActionExecutionException(e);
        } catch (DiffServConfigurationException e) {
            e.printStackTrace();
            throw new ActionExecutionException(e);
        }

        if (reverseLink) {
        try {
            Link reverseLink = domain.getReverseLink(link);
            if (reverseLink != null) {
                reverseLink.setBandwidth(newBw);
            }
        } catch (NodeNotFoundException e) {
        } catch (LinkCapacityExceededException e) {
            try {
                link.setBandwidth(oldBw);
            } catch (LinkCapacityExceededException e1) {
                logger.error("Unable to cancel change in ");
            } catch (DiffServConfigurationException e1) {
                logger.error("Unable to cancel change in ");
            }
            e.printStackTrace();
            throw new ActionExecutionException(e);
        } catch (DiffServConfigurationException e) {
            try {
                link.setBandwidth(oldBw);
            } catch (LinkCapacityExceededException e1) {
                logger.error("Unable to cancel change in ");
            } catch (DiffServConfigurationException e1) {
                logger.error("Unable to cancel change in ");
            }
            e.printStackTrace();
            throw new ActionExecutionException(e);
        }
        }

    }

    public String getName() {
        return "Change Capacity (Link: " + link.getId() + " bw: " + newBw + ")";
    }
}

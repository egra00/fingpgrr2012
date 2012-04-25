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
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.StatusTypeException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.exception.ActionExecutionException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIAction;
import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
* Action that sets a link down.
*
* <p>Creation date: 24/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LinkDownWIAction implements WIAction {
    private static final Logger logger = Logger.getLogger(LinkDownWIAction.class);

    private Domain domain;
    private Link link;
    private boolean reverseLink;

    public LinkDownWIAction(Domain domain, Link link) throws LinkNotFoundException {
        this(domain, link, false);
    }

    public LinkDownWIAction(Domain domain, Link link, boolean reverseLink) throws LinkNotFoundException {
        this.domain = domain;
        domain.getLink(link.getId());
        this.link = link;
        this.reverseLink = reverseLink;
    }

    public void execute() throws ActionExecutionException {
        int oldStatus;
        try {
            oldStatus = setLinkDown(link);
        } catch (StatusTypeException e) {
            throw new ActionExecutionException(e);
        }

        if (reverseLink) {
            try {
                Link rLink = domain.getReverseLink(link);
                if (rLink != null) setLinkDown(rLink);
            } catch (NodeNotFoundException e) {
            } catch (StatusTypeException e) {
                //reverse Changes
                try {
                    link.setLinkStatus(oldStatus);
                } catch (StatusTypeException e1) {
                    e1.printStackTrace();
                }
                throw new ActionExecutionException(e);
            }
        }
    }

    private int setLinkDown(Link l) throws StatusTypeException {
        int oldStatus = l.getLinkStatus();

        if (oldStatus == Link.STATUS_DOWN) {
            logger.warn("Trying to set down a link that is already down.");
        }

        l.setLinkStatus(Link.STATUS_DOWN);

        return oldStatus;
    }

    public String getName() {
        return "Link Down: " + link.getId();
    }
}

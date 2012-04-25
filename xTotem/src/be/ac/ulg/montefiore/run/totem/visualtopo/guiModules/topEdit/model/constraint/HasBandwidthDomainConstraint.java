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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.constraint;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.constraint.DomainConstraint;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.LinkDecorator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;

import java.util.List;
import java.util.ArrayList;

/*
* Changes:
* --------
*
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 19/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class HasBandwidthDomainConstraint implements DomainConstraint {
    private final static String description = "Check that all links have bandwidth (either <bw> tag in topology, or <mrbw> tag in <igp>.";
    private String msg;
    private List<LinkDecorator> failedLinks = null;

    public boolean validate(DomainDecorator domain) {

        boolean ok = true;
        msg = "SUCCESS";
        for (LinkDecorator link : domain.getAllLinks()) {
            if (!link.getLink().isSetBw() &&
                    (link.getLinkIgp() == null || !link.getLinkIgp().isSetStatic() || !link.getLinkIgp().getStatic().isSetMrbw())) {
                ok = false;
                msg = "ERROR: Some links do not have banwidth set. Example: link " + link.getLink().getId();
                if (failedLinks == null)
                    failedLinks = new ArrayList<LinkDecorator>();
                failedLinks.add(link);
            }
        }

        return ok;
    }

    public String getMessage() {
        return msg;
    }

    public String getDescription() {
        return description;
    }

    public List<LinkDecorator> getFailedLinks() {
        return failedLinks;
    }
}

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

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Node;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.constraint.DomainConstraint;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;

import java.util.List;

/*
* Changes:
* --------
*
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 22/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LocationIsSetDomainConstraint implements DomainConstraint {
    private final static String description = "Check that location is set or not set for all nodes";
    private String msg = null;
    protected boolean shouldBeSet = true;

    public boolean validate(DomainDecorator domain) {
        msg = "SUCCESS";

        for (Node node : (List<Node>)domain.getDomain().getTopology().getNodes().getNode()) {
            if (!(shouldBeSet ^ node.isSetLocation())) {
                msg = "Location " + (shouldBeSet ? "not set" : "set") + " for some nodes. Example: " + node.getId();
                return false;
            }
        }
        return true;
    }

    public String getMessage() {
        return msg;
    }

    public String getDescription() {
        return description;
    }
}

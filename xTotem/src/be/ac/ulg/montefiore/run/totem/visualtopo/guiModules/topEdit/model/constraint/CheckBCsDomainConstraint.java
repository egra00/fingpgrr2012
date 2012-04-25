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

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.LinkIgp;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Information;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;

import java.util.List;
import java.util.Arrays;

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

public class CheckBCsDomainConstraint implements DomainConstraint {
    private final static String description = "Check that the bandwidth constraints are set and correspond to the classtypes defined for the domain.";
    private String msg;

    public boolean validate(DomainDecorator domain) {
        msg = "SUCCESS";

        if (!domain.getDomain().getInfo().isSetDiffServ()) {
            //no diffserv, bcs should not be set
            if (domain.getDomain().isSetIgp() && domain.getDomain().getIgp().isSetLinks()) {
                for (LinkIgp igp : (List<LinkIgp>)domain.getDomain().getIgp().getLinks().getLink()) {
                    if (igp.isSetStatic() && igp.getStatic().isSetDiffServ() && igp.getStatic().getDiffServ().isSetBc()) {
                        msg = "ERROR: some links has BC but no classtype is defined for the domain. Example: " + igp.getId();
                        return false;
                    }
                }
            } else {
                return true;
            }
        } else {
            // first get all classtypes
            int nbCts = 0;
            boolean[] cts = new boolean[8];
            Arrays.fill(cts, false);
            for (Information.DiffServType.PriorityType p : (List<Information.DiffServType.PriorityType>)domain.getDomain().getInfo().getDiffServ().getPriority()) {
                if (!cts[p.getCt()]) {
                    nbCts++;
                    cts[p.getCt()] = true;
                }
            }

            if (domain.getDomain().isSetIgp() && domain.getDomain().getIgp().isSetLinks()) {
                for (LinkIgp igp : (List<LinkIgp>)domain.getDomain().getIgp().getLinks().getLink()) {
                    //copy the array so we can work on it
                    boolean[] ctsCopy = cts.clone();

                    if (igp.isSetStatic() && igp.getStatic().isSetDiffServ() && igp.getStatic().getDiffServ().isSetBc()) {
                        //check correspondance with cts.
                        if (nbCts != igp.getStatic().getDiffServ().getBc().size()) {
                            msg = "ERROR: number of BCs does not match number of classtypes of the domain. Example: link " + igp.getId();
                            return false;
                        }
                        for (LinkIgp.StaticType.DiffServType.BcType bcType : (List<LinkIgp.StaticType.DiffServType.BcType>)igp.getStatic().getDiffServ().getBc()) {
                            if (!ctsCopy[bcType.getId()]) {
                                msg = "ERROR: could not find classtype corresponding to BC " + bcType.getId() + " for link " + igp.getId();
                                return false;
                            }
                            // set to false so we can check that not two values are equals.
                            ctsCopy[bcType.getId()] = false;
                        }
                    } else {
                        msg = "ERROR: some classtype is defined for the domain but some links do not have them set. Example: link " + igp.getId();
                        return false;
                    }
                }
            } else {
                // no links would be acceptable
                if (!domain.getDomain().getTopology().isSetLinks() || domain.getDomain().getTopology().getLinks().getLink().size() == 0)
                    return true;
                msg = "ERROR: IGP section not set but diffserv defined for the domain.";
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

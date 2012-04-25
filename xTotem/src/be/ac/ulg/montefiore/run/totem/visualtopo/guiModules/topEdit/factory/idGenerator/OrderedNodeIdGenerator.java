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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory.idGenerator;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Node;

/*
* Changes:
* --------
*
*/

/**
* Generate ids for nodes of a domain. Assure that the generated id is not in the domain.
* <br>generated values are: 0,1,2,... 
*
* <p>Creation date: 3/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class OrderedNodeIdGenerator implements NodeIdGenerator {

    private int num = 0;
    private final DomainDecorator domain;

    public OrderedNodeIdGenerator(DomainDecorator domain) {
        this.domain = domain;
    }

    public String generate(Node node) {
        String id = String.valueOf(num++);

        while (domain.getNode(id) != null) {
            id = String.valueOf(num++);
        }

        return id;
    }
}

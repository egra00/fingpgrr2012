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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory;

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.*;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.DomainImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.NodeImpl;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBContext;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.HashMap;

/*
* Changes:
* --------
*
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 3/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class DomainFactory extends DomainElementFactory<Domain> {
    private final static ObjectFactory factory = new ObjectFactory();

    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;

    /**
     * Create a domain with minimum elements (ASID, bw units, delay units, node section)
     * @param params
     * @return
     */
    public Domain createInstance(HashMap<String, String> params) {
        Domain domain = null;
        try {
            domain = new DomainImpl();

            domain.setASID(0);

            domain.setInfo(factory.createInformation());

            domain.getInfo().setUnits(factory.createInformationUnitsType());

            UnitType unit = factory.createUnitType();
            unit.setType(UnitsType.BANDWIDTH);
            unit.setValue(BandwidthUnits.MBPS);
            domain.getInfo().getUnits().getUnit().add(unit);

            unit = factory.createUnitType();
            unit.setType(UnitsType.DELAY);
            unit.setValue(DelayUnits.MS);
            domain.getInfo().getUnits().getUnit().add(unit);

            domain.setTopology(factory.createTopology());

            domain.getTopology().setNodes(factory.createTopologyNodesType());
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return domain;
    }

    /**
     * Clone the domain by marshalling then unmarshalling. <br>
     * @param object
     * @return
     */
    protected Object cloneObject(Object object) {
        Domain domain = (Domain)object;

        if (marshaller == null) {
            try {
                JAXBContext jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.domain.model.jaxb");
                marshaller = jc.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.FALSE);

                unmarshaller = jc.createUnmarshaller();
                unmarshaller.setValidating(false);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        //Note: the unmarshalling operation will not work if the domain is not valid.
        //A newly created domain is not valid since it contains no nodes.
        //In this case, add a dummy node and then remove it.

        ByteArrayOutputStream os = new ByteArrayOutputStream(8192);
        Domain clone = null;
        try {
            boolean dummyNodeAdded = false;
            if (!domain.getTopology().getNodes().isSetNode()) {
                Node dummyNode = new NodeImpl();
                dummyNode.setId("dummyNode");
                domain.getTopology().getNodes().getNode().add(dummyNode);
                dummyNodeAdded = true;
            }

            marshaller.marshal(domain, os);

            InputStream is = new ByteArrayInputStream(os.toByteArray());
            clone = (Domain)unmarshaller.unmarshal(is);

            if (dummyNodeAdded) {
                clone.getTopology().getNodes().unsetNode();
                domain.getTopology().getNodes().unsetNode();
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return clone;
    }
}

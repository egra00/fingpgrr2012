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
package be.ac.ulg.montefiore.run.totem.trafficMatrix.persistence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.BandwidthUnit;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.TrafficMatrixImpl;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.*;

/*
 * Changes:
 * --------
 *
 * - 08-Mar-2005: modifications to deal with interdomain tm (OD)
 * - 08-Mar-2005: Use TrafficMatrix-v1_1 (JL).
 * - 11-Jul-2006: loadTrafficMatrix now throws InvalidTrafficMatrixException when a JAXBException occurs (GMO)
 * - 11-Jul-2006: Retrieve the units used in the matrix file when loading the matrix and put it when saving (GMO)
 * - 20-Sep-2007: Add loadInterDomainMatrix(.) (GMO)
 */

/**
 * Factory to load and save traffic matrices.
 *
 * <p>Creation date: 28-janv.-2005
 * 
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 */
public class TrafficMatrixFactory {

    private static final Logger logger = Logger.getLogger(TrafficMatrixFactory.class);
    
    /**
     * Returns the intra-domain traffic matrix contained in the file <code>fileName</code>.
     * @param fileName The name of the file containing the intra-domain traffic matrix.
     * @return The traffic matrix contained in the file <code>fileName</code>.
     * @throws InvalidTrafficMatrixException If the matrix cannot be loaded (error in XML).
     * @throws InvalidDomainException If there is no domain corresponding to the traffic matrix.
     * @throws NodeNotFoundException If there is an unknown node in the traffic matrix. 
     */
    public static TrafficMatrix loadTrafficMatrix(String fileName) throws InvalidDomainException, NodeNotFoundException, InvalidTrafficMatrixException {
        return TrafficMatrixFactory.loadTrafficMatrix(new File(fileName));
    }

    /**
     * Returns the intra-domain traffic matrix contained in the given file <code>file</code>.
     *
     * @param file the file containing the intra-domain traffic matrix.
     * @return The traffic matrix contained in the file <code>fileName</code>.
     * @throws InvalidTrafficMatrixException If the matrix cannot be loaded (error in XML).
     * @throws InvalidDomainException If there is no domain corresponding to the traffic matrix.
     * @throws NodeNotFoundException If there is an unknown node in the traffic matrix.
     */
    public static TrafficMatrix loadTrafficMatrix(File file) throws InvalidTrafficMatrixException, InvalidDomainException, NodeNotFoundException {
        TrafficMatrix tm = null;
        try {
            JAXBContext jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb");
            Unmarshaller u = jc.createUnmarshaller();
            TrafficMatrixFileType tmFile = (TrafficMatrixFileType) u.unmarshal(file);

            tm = new TrafficMatrixImpl(tmFile.getIntraTM().getASID());
            if ((tmFile.isSetInfo()) && (tmFile.getInfo().isSetDate())) {
                tm.setDate(tmFile.getInfo().getDate());
            }
            if ((tmFile.isSetInfo()) && (tmFile.getInfo().isSetDuration())) {
                tm.setDuration(tmFile.getInfo().getDuration());
            }

            BandwidthUnit unit = BandwidthUnit.DEFAULT_UNIT;
            if (tmFile.isSetInfo() && tmFile.getInfo().isSetUnits() && tmFile.getInfo().getUnits().isSetUnit()) {
                unit = BandwidthUnit.valueOf(tmFile.getInfo().getUnits().getUnit().getValue().toString().toUpperCase());
            }

            // TODO: add a check here
            //if (!tmFile.getIntraTMOrInterTM().isSetIntraTM())...
            for(Iterator it1 = tmFile.getIntraTM().getSrc().iterator(); it1.hasNext();) {
                IntraTMType.SrcType srcType = (IntraTMType.SrcType) it1.next();
                String src = srcType.getId();
                for(Iterator it2 = srcType.getDst().iterator(); it2.hasNext();) {
                    IntraTMType.SrcType.DstType dstType = (IntraTMType.SrcType.DstType) it2.next();
                    String dst = dstType.getId();
                    tm.set(src, dst, dstType.getValue());
                }
            }

            tm.setUnit(unit);
        }
        catch(JAXBException e) {
            logger.error("JAXBException in loadTrafficMatrix. Message: "+e.getMessage());
            throw new InvalidTrafficMatrixException();
        }
        return tm;
    }


    /**
     * Saves the intra-domain traffic matrix <code>tm</code> to the file <code>fileName</code>.
     * @param fileName The name of the target file.
     * @param tm The traffic matrix to save.
     * @throws InvalidDomainException If there is no domain corresponding to <code>tm</code>.
     * @throws NodeNotFoundException If there is an unknown node in the traffic matrix.
     */
    public static void saveTrafficMatrix(String fileName, TrafficMatrix tm) throws InvalidDomainException, NodeNotFoundException {
        ObjectFactory factory = new ObjectFactory();
        
        try {
            TrafficMatrixFile tmFile = factory.createTrafficMatrixFile();
            //IntraTMType intraTM = factory.createIntraTMType();

            IntraTM intraTM = factory.createIntraTM();
            //intraTMOrInterTM.setIntraTM(intraTM);

            intraTM.setASID(tm.getASID());

            Domain domain = InterDomainManager.getInstance().getDomain(tm.getASID());

            List<Node> nodes = domain.getAllNodes();
            for (Iterator<Node> iter = nodes.iterator(); iter.hasNext();) {
                Node src = iter.next();
                IntraTMType.SrcType srcType = factory.createIntraTMTypeSrcType();
                srcType.setId(src.getId());
                intraTM.getSrc().add(srcType);
                for (Iterator<Node> iterator = nodes.iterator(); iterator.hasNext();) {
                    Node dst = iterator.next();
                    float value = tm.get(src.getId(), dst.getId());
                    IntraTMType.SrcType.DstType dstType = factory.createIntraTMTypeSrcTypeDstType();
                    dstType.setId(dst.getId());
                    dstType.setValue(value);
                    srcType.getDst().add(dstType);
                }
            }

            tmFile.setIntraTM(intraTM);

            if (tm.getUnit() != null) {
                if (tmFile.getInfo() == null) {
                    tmFile.setInfo(factory.createInformation());
                }
                if (tmFile.getInfo().getUnits() == null) {
                    tmFile.getInfo().setUnits(factory.createInformationUnitsType());
                }

                Information.UnitsType.UnitType u = factory.createInformationUnitsTypeUnitType();
                u.setType("bandwidth");
                u.setValue(BandwidthUnits.fromString(tm.getUnit().toString().toLowerCase()));
                tmFile.getInfo().getUnits().setUnit(u);
            }

            JAXBContext jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");
            m.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "http://totem.run.montefiore.ulg.ac.be/Schema/TrafficMatrix-v1_2.xsd");
            m.marshal(tmFile, new FileWriter(fileName));
        }
        catch(JAXBException e) {
            logger.error("JAXBException in saveTrafficMatrix. Message: "+e.getMessage());
        }
        catch(IOException e) {
            logger.error("IOException in saveTrafficMatrix. Message: "+e.getMessage());
        }
    }
    
    /**
     * Updates the intra-domain traffic matrix <code>tmId</code> with the information contained in the file <code>fileName</code>.
     * @param fileName The name of the file containing the update information.
     * @param tmId The TM ID of the traffic matrix to update.
     * @throws InvalidTrafficMatrixException If the traffic matrix to update can't be retrieved.
     * @throws NodeNotFoundException If the information contained in the file is not consistent with the traffic matrix to update.
     */
    public static void updateTrafficMatrix(String fileName, int tmId) throws NodeNotFoundException, InvalidTrafficMatrixException {
        try {
            JAXBContext jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb");
            Unmarshaller u = jc.createUnmarshaller();
            TrafficMatrixFileType tmFile = (TrafficMatrixFileType) u.unmarshal(new File(fileName));
            int asId = tmFile.getIntraTM().getASID();
            TrafficMatrix tm = TrafficMatrixManager.getInstance().getTrafficMatrix(asId, tmId);
            for(Iterator it1 = tmFile.getIntraTM().getSrc().iterator(); it1.hasNext();) {
                IntraTMType.SrcType srcType = (IntraTMType.SrcType) it1.next();
                String src = srcType.getId();
                for(Iterator it2 = srcType.getDst().iterator(); it2.hasNext();) {
                    IntraTMType.SrcType.DstType dstType = (IntraTMType.SrcType.DstType) it2.next();
                    String dst = dstType.getId();
                    float value = dstType.getValue();
                    tm.set(src, dst, value);
                }
            }
        }
        catch(JAXBException e) {
            logger.error("JAXBException in updateTrafficMatrix. Message: "+e.getMessage());
        }
    }

    /**
     * Load an inter Domain traffic matrix
     *
     * @param netflowXMLTrafficMatrixFileName
     * @return
     * @throws InvalidTrafficMatrixException If the matrix is not an inter domain one
     * @throws JAXBException if JAXB cannot load the matrix
     */
    public static TrafficMatrixFileType loadInterDomainMatrix(String netflowXMLTrafficMatrixFileName) throws InvalidTrafficMatrixException, JAXBException {
        JAXBContext jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb");
        Unmarshaller u = jc.createUnmarshaller();
        TrafficMatrixFileType tmFile = (TrafficMatrixFileType) u.unmarshal(new File(netflowXMLTrafficMatrixFileName));

        if (!tmFile.isSetInterTM()) {
            throw new InvalidTrafficMatrixException("File is not an XML inter-domain traffic matrix!");
        }

        return tmFile;
    }
}

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
package be.ac.ulg.montefiore.run.totem.scenario.persistence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;
import be.ac.ulg.montefiore.run.totem.scenario.model.Scenario;
import be.ac.ulg.montefiore.run.totem.util.FileFunctions;

/*
 * Changes:
 * --------
 *
 * 01-Mar.-2005: change the schema location and add the namespace (JL).
 * 01-Mar.-2005: use the preferences manager (JL).
 * 09-Jan.-2007: loadScenario now returns a be.ac.ulg.montefiore.run.totem.scenario.model.Scenario object and sets the scenarioPath of the scenario (GMO) 
 */

/**
 * The <code>ScenarioFactory</code> class is used to load/save scenarios.
 *
 * <p>Creation date: 03-d�c.-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class ScenarioFactory {
    
    private static final Logger logger = Logger.getLogger(ScenarioFactory.class);
    
    /**
     * Loads the specified scenario XML file.
     * @param fileName The XML file to load.
     * @return The loaded scenario.
     */
    public static Scenario loadScenario(String fileName) {
        Scenario scenario = null;
        
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(PreferenceManager.getInstance().getPrefs().get("SCENARIO-PACKAGES", "be.ac.ulg.montefiore.run.totem.scenario.model.jaxb:be.ac.ucl.ingi.totem.scenario.model.jaxb"));
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            scenario = (Scenario) unmarshaller.unmarshal(new File(fileName));
            if (!scenario.isSetPathsRelativeTo()) {
                scenario.setPathsRelativeTo(System.getProperty("user.dir"));
            }
            scenario.setScenarioPath(FileFunctions.getFilenameFromContext(fileName, scenario.getPathsRelativeTo()));
        }
        catch(JAXBException e) {
            logger.error("JAXBException in loadScenario. Message: "+e.getMessage());
            e.printStackTrace();
        }
        
        return scenario;
    }
    
    /**
     * Saves the specified scenario object into the specified file.
     * @param fileName The targeted file.
     * @param scenario The scenario to save.
     */
    public static void saveScenario(String fileName, be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.Scenario scenario) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(PreferenceManager.getInstance().getPrefs().get("SCENARIO-PACKAGES", "be.ac.ulg.montefiore.run.totem.scenario.model.jaxb:be.ac.ucl.ingi.totem.scenario.model.jaxb"));
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, PreferenceManager.getInstance().getPrefs().get("SCENARIO-SCHEMA-LOCATION", "http://jaxb.model.scenario.totem.run.montefiore.ulg.ac.be http://totem.run.montefiore.ulg.ac.be/Schema/Scenario-v1_1.xsd http://jaxb.model.scenario.totem.ingi.ucl.ac.be http://totem.run.montefiore.ulg.ac.be/Schema/CBGP-Scenario-v1_0.xsd"));
            marshaller.marshal(scenario, new FileWriter(fileName));
        }
        catch(JAXBException e) {
            logger.error("JAXBException in saveScenario. Message: "+e.getMessage());
        }
        catch(IOException e) {
            logger.error("IOException in saveScenario. Message: "+e.getMessage());
        }
    }
}

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

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Domain;
import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;

import javax.xml.bind.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.*;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXParseException;
import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 12-Feb-2008: domain schema was harcoded, the preference property was not used. (Thanks Andreas Siegrist) (GMO)
*/

/**
* Domain marshaller, unmarshaller, validator.
*
* <p>Creation date: 8/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class XMLFactory {
    private static final Logger logger = Logger.getLogger(XMLFactory.class);

    private static JAXBContext jc = null;
    private static Marshaller m = null;
    private static Validator v = null;
    private static Unmarshaller um = null;

    private static String schema = null;

    private static void createContext() throws JAXBException {
        if (jc == null)
            jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.domain.model.jaxb");
    }

    private static void createMarshaller() throws JAXBException {
        if (m == null)
            m = jc.createMarshaller();
    }

    private static void createValidator() throws JAXBException {
        if (v == null)
            v = jc.createValidator();
    }

    private static void createUnmarshaller() throws JAXBException {
        if (um == null)
            um = jc.createUnmarshaller();
    }

    public static String getXML(DomainDecorator domainDec) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Domain domain = domainDec.getDomain();
        try {
            createContext();
            createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
            //long time = System.currentTimeMillis();
            m.marshal(domain,os);
            //time = System.currentTimeMillis() - time;
            //System.out.println("Marshalling process takes " + time + " milliseconds");
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        return os.toString();
    }

    public static InputStream getInputStream(DomainDecorator domainDec) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Domain domain = domainDec.getDomain();
        try {
            createContext();
            createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
            //long time = System.currentTimeMillis();
            m.marshal(domain,os);
            //time = System.currentTimeMillis() - time;
            //System.out.println("Marshalling process takes " + time + " milliseconds");
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }

        InputStream is = new ByteArrayInputStream(os.toByteArray());
        return is;
    }

    public static boolean validate(DomainDecorator domainDec) throws Exception {
        /*
        Domain domain = domainDec.getDomain();
        createContext();
        createValidator();
        return v.validateRoot(domain);
        */

        ValidationHandler handler = null;
        handler = new ValidationHandler();

        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(true);

        SAXParser saxParser = saxFactory.newSAXParser();
        saxParser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        if (schema == null) {
            schema = PreferenceManager.getInstance().getPrefs().get("DOMAIN-SCHEMA-LOCATION", null);
            if (schema == null) throw new Exception("Key DOMAIN-SCHEMA-LOCATION not found in preferences file.");
        }
        saxParser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", schema);

        saxParser.parse(getInputStream(domainDec), handler);

        return handler.isValid();

    }

    public static Domain loadDomain(String filename) throws JAXBException {
        createContext();
        createUnmarshaller();

        Domain domain = (Domain)um.unmarshal(new File(filename));

        return domain;
    }

    public static void saveDomain(Domain domain, String filename) throws JAXBException, FileNotFoundException {
        createContext();
        createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
        m.marshal(domain, new FileOutputStream(filename));
    }

    /**
     * This class is used by the XML files validator.
     *
     * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
     */
    private static class ValidationHandler extends DefaultHandler {

        private boolean isValid;

        public ValidationHandler() {
            isValid = true;
        }

        public void error(SAXParseException e) throws SAXParseException {
            isValid = false;
            logger.error("It seems a validation error occurred. Message: "+e.getMessage());
            throw e;
        }

        public void warning(SAXParseException e) throws SAXParseException {
            isValid = false;
            logger.error("A SAX warning was produced during the validation process. Message: "+e.getMessage());
            throw e;
        }

        public void fatalError(SAXParseException e) throws SAXParseException {
            isValid = false;
            logger.error("A SAX fatal error was produced during the validation process. Message: "+e.getMessage());
            throw e;
        }

        public boolean isValid() {
            return isValid;
        }
    }
}

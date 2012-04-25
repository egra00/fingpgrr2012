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
package be.ac.ulg.montefiore.run.totem.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Changes:
 * --------
 * 28-Apr.-2006: Schema location can be the path to a file without file:// (GMO).
 */

/**
 * This class is a generic XML files validator.
 *
 * <p>Creation date: 23-mai-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class XMLFilesValidator {

    private static final Logger logger = Logger.getLogger(XMLFilesValidator.class);
    
    private ValidationHandler handler;
    
    public XMLFilesValidator() {
        handler = new ValidationHandler();
    }
    
    /**
     * Validates the XML file <code>fileName</code> using the schema specified
     * in the XML file.
     * @param fileName The XML file to validate.
     * @return <code>true</code> if the XML file is valid and
     * <code>false</code> otherwise.
     */
    public boolean validate(String fileName) {
        return this.validate(fileName, null, false);
    }

    /**
     * Validates the XML file <code>fileName</code> using the schema located
     * at <code>schemaLocation</code>.
     * @param fileName The XML file to validate.
     * @param schemaLocation The location of the schema to use. This can be an
     * URL. If the scheme (file://, http://, ...) is not provided, it defaults to file.
     * @return <code>true</code> if the XML file is valid and
     * <code>false</code> otherwise.
     */
    public boolean validate(String fileName, String schemaLocation) {
        return this.validate(fileName, schemaLocation, true);
    }
    

    private boolean validate(String fileName, String schemaLocation, boolean useExternalSchema) {
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        saxFactory.setNamespaceAware(true);
        saxFactory.setValidating(true);
        
        String tmpSchema = System.getProperty("user.dir") + File.separatorChar + "Schema.xsd";
        if(useExternalSchema) {
            try {
                FileFunctions.copy(new URL(new URL("file:"), schemaLocation), tmpSchema);
            } catch (IOException e) {
                logger.error("An IOException occured during the copy of the schema. Message: "+e.getMessage());
                if(logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                return false;
            }
        }

        SAXParser saxParser = null;
        try {
            saxParser = saxFactory.newSAXParser();
            saxParser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            if(useExternalSchema) {
                saxParser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", tmpSchema);
            }
        }
        catch (SAXException e) {
            logger.error("Cannot create a validating parser. Reason: "+e.getMessage());
            return false;
        }
        catch(ParserConfigurationException e) {
            logger.error("Cannot create a validating parser. Reason: "+e.getMessage());
            return false;
        }
        
        try {
            saxParser.parse(new File(fileName), handler);
        }
        catch(SAXException e) {
            logger.error("A SAX exception was produced during the validation process. Maybe this has already been logged.");
            return false;
        }
        catch(IOException e) {
            logger.error("An IO exception was produced during the validation process. Are you sure the file exists ?");
            return false;
        }

        if(useExternalSchema) {
            File f = new File(tmpSchema);
            if (f.exists()) {
                f.delete();
            }
        }

        return handler.isValid();
    }

    /**
     * This class is used by the XML files validator.
     *
     * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
     */
    private class ValidationHandler extends DefaultHandler {

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

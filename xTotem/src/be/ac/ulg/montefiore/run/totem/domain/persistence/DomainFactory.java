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
package be.ac.ulg.montefiore.run.totem.domain.persistence;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

/*
 * Changes:
 * --------
 *  25-Jan-05 : add validate method (JL)
 *  7-Dec.-2005 : set the URI when loading the domain (GMO)
 *  03-Apr.-2006 : loadDomain now throws exceptions if the domain fails to initialize or the file is not found (GMO)
 *  10-Oct-2006 : add loadDistantDomain(...) methods. and loadDomain(InputStream,...) method (GMO)
 */

/**
 * Factory for loading and saving domain
 *
 * <p>Creation date: 12-Jan-2005 18:10:16
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class DomainFactory {

    private static final Logger logger = Logger.getLogger(DomainFactory.class);

    public static final int DEFAULT_PORT = 1234;

    public static Domain loadDistantDomain(String host, boolean removeMultipleLink, boolean useBwSharing) throws IOException, InvalidDomainException {
        return loadDistantDomain(host, DEFAULT_PORT, removeMultipleLink, useBwSharing);
    }

    public static Domain loadDistantDomain(String host, int port, boolean removeMultipleLink, boolean useBwSharing) throws IOException, InvalidDomainException {
        Socket client = new Socket(host, port);

        InputStream is = client.getInputStream();

        Domain domain = loadDomain(is, removeMultipleLink, useBwSharing);

        client.close();

        try {
            domain.setURI(new URI(null, null, host, port, null, null, null));
        } catch (URISyntaxException e) {
            //e.printStackTrace();
            logger.error("Bad URI syntax");
        }

        return domain;
    }

    /* This can be used for debug purposes
    public static Domain loadDistantDomain(String host, int port, boolean removeMultipleLink, boolean useBwSharing) throws IOException, InvalidDomainException {
        Socket client = new Socket(host, port);

        InputStream is = client.getInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        StringBuffer sb = new StringBuffer();

        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = br.readLine();
        }

        client.close();

        System.out.println(sb.toString());

        ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());
        Domain domain = loadDomain(bais, removeMultipleLink, useBwSharing);

        try {
            domain.setURI(new URI(null, null, host, port, null, null, null));
        } catch (URISyntaxException e) {
            //e.printStackTrace();
            logger.error("Bad URI syntax");
        }

        return domain;
    }
    */

    public static Domain loadDistantDomain(String host, int port) throws IOException, InvalidDomainException {
        return loadDistantDomain(host, port, false, false);
    }


    public static Domain loadDomain(InputStream is) throws InvalidDomainException {
        Domain domain = loadDomain(is,false, false);
        try {
            domain.setURI(new URI("inputstream://" + domain.getASID()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return domain;
    }

    /**
     * Load a domain from a file not removing multiple links nor using BwSharing
     * @param fileName file name of the domain
     * @return the loaded domain
     * @throws InvalidDomainException If the domain fails to initialize
     * @throws FileNotFoundException If the given file is not found
     */
    public static Domain loadDomain(String fileName) throws InvalidDomainException, FileNotFoundException {
        return loadDomain(fileName,false, false);
    }

    /**
     * Load a domain from a file
     * @param fileName file name of the domain
     * @param removeMultipleLink
     * @param useBwSharing
     * @return the loaded domain
     * @throws InvalidDomainException If the domain fails to initialize
     * @throws FileNotFoundException If the given file is not found
     */
    public static Domain loadDomain(String fileName, boolean removeMultipleLink, boolean useBwSharing) throws InvalidDomainException, FileNotFoundException {
        Domain domain = loadDomain(new FileInputStream(fileName), removeMultipleLink, useBwSharing);
        domain.setURI(new File(fileName).toURI());
        return domain;
    }

    public static Domain loadDomain(InputStream is, boolean removeMultipleLink, boolean useBwSharing) throws InvalidDomainException {
        Domain domain = null;
        try {
            // creates a JAXBContext capable of handling classes generated into the topology package
            JAXBContext jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.domain.model.jaxb");

            // creates an Unmarshaller
            Unmarshaller u = jc.createUnmarshaller();

            // unmarshals a network Domain document into a tree of Java content,
            // i.e. creates Java object corresponding to the XML file.
            // objects composed of classes from the domain package.
            //long time = System.currentTimeMillis();
            domain  = (be.ac.ulg.montefiore.run.totem.domain.model.Domain) u.unmarshal(is);
            //time = System.currentTimeMillis() - time;
            //System.out.println("UnMarshalling process takes " + time + " milliseconds");

            // Initialize the domain
            domain.init(removeMultipleLink, useBwSharing);
        } catch( JAXBException je ) {
            je.printStackTrace();
            throw new InvalidDomainException("The file is not a valid domain file.");
        }
        return domain;
    }

    /**
     * Save a domain to a file
     *
     * @param fileName file name in which saving the domain
     * @param domain the domain to save
     */
    public static void saveDomain(String fileName, Domain domain) {
        try {
            JAXBContext jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.domain.model.jaxb");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
            //long time = System.currentTimeMillis();
            m.marshal(domain,new FileOutputStream(fileName));
            //time = System.currentTimeMillis() - time;
            //System.out.println("Marshalling process takes " + time + " milliseconds");
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

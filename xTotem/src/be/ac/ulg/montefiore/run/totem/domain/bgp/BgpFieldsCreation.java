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
package be.ac.ulg.montefiore.run.totem.domain.bgp;

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.*;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.DomainImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpRouterImpl;
import be.ac.ulg.montefiore.run.totem.domain.exception.RouterAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidFileException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.regex.Matcher;

/*
 * Changes:
 * --------
 * - 17-Sep-2007: add methods to use with a domain object (not filename). MRT can be gzipped. (GMO)
 *
 */

/**
 * This class provides methods to add iBGP/eBGP sessions informations to an XML domain file.
 * It can create the corresponding full mesh of iBGP sessions, and read eBGP sessions in a
 * BGP MRT dump.
 *
 * <p>Creation date: 03/03/2005
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 */


public class BgpFieldsCreation {

    public void addiBGPFullMesh(DomainImpl domain) {
        List<Node> nodesList = domain.getTopology().getNodes().getNode();
        for (Node n : nodesList) {
            try {
                BgpRouterImpl router = (BgpRouterImpl)createRouterFullmesh(domain, n);
                domain.addBgpRouter(router);
            } catch (JAXBException e) {
                e.printStackTrace();
            } catch (RouterAlreadyExistException e) {
                e.printStackTrace();
            }
        }
    }

    private BgpRouter createRouterFullmesh(Domain domain, Node node) throws JAXBException {
        ObjectFactory factory = new ObjectFactory();

        List<Node> nodesList = domain.getTopology().getNodes().getNode();

        BgpRouter bgpRouter = factory.createBgpRouter();
        bgpRouter.setId(node.getId());
        bgpRouter.setRid(node.getRid());

        BgpRouter.NeighborsType neighborsType = factory.createBgpRouterNeighborsType();

        List<BgpNeighbor> bgpNeighboursList = neighborsType.getNeighbor();

        for (int i=0; i<nodesList.size(); i++){
            Node node1 = nodesList.get(i);
            if (!node1.getId().equals(node.getId())){
                BgpNeighbor bgpNeighbor = factory.createBgpNeighbor();
                bgpNeighbor.setIp(node1.getRid());
                bgpNeighbor.setAs(domain.getASID());
                bgpNeighboursList.add(bgpNeighbor);
            }

        }

        bgpRouter.setNeighbors(neighborsType);

        return bgpRouter;
    }

    /**
     * This method adds a full mesh of iBGP sessions.  It supposes that no BGP fields are already defined
     * in the domain XML file!
     * @param srcDomainName the source domain XML file name
     * @param dstDomainName the destination domain XML file name
     */
    public void addiBGPFullMesh(String srcDomainName, String dstDomainName){
        DomainImpl domain = null;

        try {
            // creates a JAXBContext capable of handling classes generated into the topology package
            JAXBContext jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.domain.model.jaxb");

            // creates an Unmarshaller
            Unmarshaller u = jc.createUnmarshaller();

            // unmarshals a network Domain document into a tree of Java content,
            // i.e. creates Java object corresponding to the XML file.
            // objects composed of classes from the domain package.
            //long time = System.currentTimeMillis();
            domain  = (DomainImpl) u.unmarshal(new FileInputStream(srcDomainName));
            //time = System.currentTimeMillis() - time;
            //System.out.println("UnMarshalling process takes " + time + " milliseconds");
        } catch( JAXBException je ) {
            je.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ObjectFactory factory = new ObjectFactory();

        try{
            Bgp bgp = factory.createBgp();
            Bgp.RoutersType routersType = factory.createBgpRoutersType();
            List<BgpRouter> bgpRoutersList = routersType.getRouter();

            List<Node> nodesList = domain.getTopology().getNodes().getNode();

            for (int j=0; j<nodesList.size(); j++){

                //create
                BgpRouter bgpRouter = createRouterFullmesh(domain, nodesList.get(j));

                bgpRoutersList.add(bgpRouter);

            }

            bgp.setRouters(routersType);

            domain.setBgp(bgp);

        } catch(JAXBException e){
            e.printStackTrace();
        }

        try {
            JAXBContext jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.domain.model.jaxb");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
            //long time = System.currentTimeMillis();
            m.marshal(domain,new FileOutputStream(dstDomainName));
            //time = System.currentTimeMillis() - time;
            //System.out.println("Marshalling process takes " + time + " milliseconds");
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addeBGPSessions(DomainImpl domain, String BGPbaseDirectory, String BGPdirFileName) throws Exception {
        ObjectFactory factory = new ObjectFactory();

        List<Node> nodes = domain.getTopology().getNodes().getNode();

        boolean idNaming = BGPInfoChecker.isIdNaming(domain, BGPbaseDirectory);

        String id = idNaming ? nodes.get(0).getId() : nodes.get(0).getRid();

        // Now trying opening corresponding dumps which must be in MRT ASCII MACHINE READABLE FORMAT
        if (!BGPInfoChecker.isMRTFile(BGPbaseDirectory + File.separator + id + File.separator + BGPdirFileName)) {
            throw new InvalidFileException("MRT file not found!  For router "+ id + " it should be in directory " + BGPbaseDirectory + "/" + id + "/" + " and named:" + BGPdirFileName);
        }

        boolean gzipped = BGPdirFileName.endsWith(".gz");

        // Now adding all the eBGP Sessions
        for (int i=0; i<nodes.size(); i++){
            id = idNaming ? nodes.get(i).getId() : nodes.get(i).getRid();

            File tempFile = new File (BGPbaseDirectory + File.separator + id + File.separator + BGPdirFileName);
            if (!tempFile.exists()){
                System.out.println("MRT file not found!  For router "+ id + " it should be in directory " + BGPbaseDirectory + "/" + id + "/" + " and named: " + BGPdirFileName);
                continue;
            }

            // searching the corresponding BGP fields
            List<BgpRouter> bgpRouters =  domain.getBgp().getRouters().getRouter();

            BgpRouter bgpRouter = null;
            for (int j=0; j<bgpRouters.size(); j++){
                if (bgpRouters.get(j).getRid().equals(nodes.get(i).getRid())){
                    bgpRouter = bgpRouters.get(j);
                    break;
                }
            }

            BufferedReader in;
            if (gzipped) {
                FileInputStream fis = new FileInputStream(tempFile);
                GZIPInputStream gis = new GZIPInputStream(fis);
                InputStreamReader isr = new InputStreamReader(gis);
                in = new BufferedReader(isr);
            } else {
                FileReader fr = new FileReader(tempFile);
                in = new BufferedReader(fr);
            }

            String line;
            while ((line=in.readLine())!=null){

                Matcher matcher = BGPInfoChecker.MRTFormatPattern.matcher(line);
                if (matcher.find()){
                    List<NodeInterface> interfaces = nodes.get(i).getInterfaces().getInterface();
                    int r=0;
                    for (r=0;r<interfaces.size(); r++){
                        if (interfaces.get(r).getIp()  != null){
                            if (interfaces.get(r).getIp().getValue()!=null){
                                if (interfaces.get(r).getIp().getValue().equals(matcher.group(1))) break;
                            }
                        }
                    }
                    if (r==interfaces.size() && (!matcher.group(1).equals(nodes.get(i).getRid())))
                        throw new Exception("Peer IP seems different from router ID!, aborting");

                    // Adding the corresponding eBGP sessions

                    String peerIP = matcher.group(5);

                    // check if peer IP is not in the domain
                    int f=0;
                    for (f=0; f<nodes.size(); f++){
                        if (nodes.get(f).getRid().equals(peerIP)) break;
                        List<NodeInterface> interfacesList = nodes.get(f).getInterfaces().getInterface();
                        int q=0;
                        for (q=0; q<interfacesList.size(); q++){
                            if (interfacesList.get(q).getIp()  != null){
                                if (interfacesList.get(q).getIp().getValue()!=null){
                                    if (interfacesList.get(q).getIp().getValue().equals(peerIP)) break;
                                }
                            }
                        }
                        if (q<interfacesList.size())break;
                    }

                    if (f==nodes.size()){ // otherwise, peer is inside domain

                        int peerASID = Integer.parseInt((String)matcher.group(3));

                        // Adding an eBGP session between the concerned router and the peer identified by peerIP and of peerASID AS
                        // if it does not already exist
                        List<BgpNeighbor> bgpNeighbors = bgpRouter.getNeighbors().getNeighbor();

                        boolean found = false;
                        for (int j=0; j<bgpNeighbors.size(); j++){
                            if (bgpNeighbors.get(j).getIp().equals(peerIP)){
                                if (bgpNeighbors.get(j).getAs() != peerASID){
                                   System.out.println("For node " + nodes.get(i).getId() + " Peer IP=" + peerIP + " has 2 AS numbers: PeerASID=" + peerASID + " oldASID=" + bgpNeighbors.get(j).getAs());
                                   throw new Exception("Problem in file, two AS numbers for same peer");

                                }

                                found = true;
                                break;
                            }
                        }

                        if (!found){
                            // adding the eBGP session
                            BgpNeighbor bgpNeighbor = factory.createBgpNeighbor();
                            bgpNeighbor.setIp(peerIP);
                            bgpNeighbor.setAs(peerASID);
                            bgpNeighbor.setNextHopSelf(true);
                            bgpNeighbors.add(bgpNeighbor);
                        }
                    }
                }
            }
        }
    }

    /**
     * This function adds eBGPSessions to an XML domain file, reading them in MRT ASCII MACHINE READABLE BGP
     * dump files (1 for each router is expected, in a directory named either by the IP of this router or its
     * identifier). This function supposes that basic BGP fields are already created.  It can for example be run
     * after addiBGPFullMesh.
     * @param srcDomainName the source XML domain file name
     * @param dstDomainName the destination domain file name
     * @param BGPbaseDirectory the base directory containing directories named following the identifier or IP of the concerned router
     * @param BGPdirFileName the date/hour of the rib capture, name of the BGP dump files should be ribYYYYMMDDHHMM
     * @throws Exception
     */
    public void addeBGPSessions(String srcDomainName, String dstDomainName, String BGPbaseDirectory, String BGPdirFileName) throws Exception{

        DomainImpl domain = null;

        try {
            // creates a JAXBContext capable of handling classes generated into the topology package
            JAXBContext jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.domain.model.jaxb");

            // creates an Unmarshaller
            Unmarshaller u = jc.createUnmarshaller();

            // unmarshals a network Domain document into a tree of Java content,
            // i.e. creates Java object corresponding to the XML file.
            // objects composed of classes from the domain package.
            //long time = System.currentTimeMillis();
            domain  = (DomainImpl) u.unmarshal(new FileInputStream(srcDomainName));
            //time = System.currentTimeMillis() - time;
            //System.out.println("UnMarshalling process takes " + time + " milliseconds");
        } catch( JAXBException je ) {
            je.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        addeBGPSessions(domain, BGPbaseDirectory, BGPdirFileName);

        try {
            JAXBContext jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.domain.model.jaxb");
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
            //long time = System.currentTimeMillis();
            m.marshal(domain,new FileOutputStream(dstDomainName));
            //time = System.currentTimeMillis() - time;
            //System.out.println("Marshalling process takes " + time + " milliseconds");
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}

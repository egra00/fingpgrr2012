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
package it.unina.traffic;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.NodeInterface;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeInterfaceNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NotInitialisedException;

import java.net.*;
import java.io.IOException;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
 * Sends link weights to every nodes of the domain through a Datagram socket on port 8888.
 * rid should be present for nodes
 * node interfaces sould be defined and should contains IP address
 * Link weights are send in the format: <br>
 * <code>if_IP_address:weight [if_IP_address:weight]*</code>
 * <p/>
 * <p>Creation date: 5 oct. 2006
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class MultipleUDPLinkWeightsSender implements LinkWeightsSender {
    private static Logger logger = Logger.getLogger(MultipleUDPLinkWeightsSender.class);

    private static final int DEFAULT_PORT = 8888;

    private static final boolean TESTMODE = true;

    private Domain domain;

    private DatagramSocket[] clients;

    private Writer writer;

    public MultipleUDPLinkWeightsSender(Domain domain) {
        this.domain = domain;
        writer = new ColorConsoleWriter();
    }

    public MultipleUDPLinkWeightsSender(Domain domain, Writer writer) {
        this.domain = domain;
        this.writer = writer;
    }

    /**
     * This method sends the link weight the the hosts of the domain.
     * It doesn't throw exception but log messages about which hosts cause errors.
     */
    public void sendLinkWeights() {

        clients = new DatagramSocket[domain.getNbNodes()];
        int top = 0;

        for (Node n : domain.getAllNodes()) {
            String IP = n.getRid();
            if (IP == null) {
                logger.error("No IP found for node: " + n.getId());
                continue;
            }

            InetAddress ina;
            try {
                ina = InetAddress.getByName(IP);
            } catch (UnknownHostException e) {
                logger.error("Unknown host: " + IP);
                continue;
            }

            DatagramSocket client;
            try {
                clients[top] = new DatagramSocket();
                client = clients[top];
                top++;
            } catch (SocketException e) {
                e.printStackTrace();
                logger.error("Unable to create socket for host: " + IP + ": " + e.getMessage());
                continue;
            }

            StringBuffer sb = new StringBuffer();

            try {
                for (Link l : n.getOutLink()) {
                    float metric = l.getMetric();
                    Node src = l.getSrcNode();;
                    NodeInterface nif = l.getSrcInterface();

                    String nifIP;
                    try {
                        nifIP = nif.getIP();
                    } catch (NotInitialisedException e) {
                        logger.error("IP of interface not found for node: " + src.getId() + " if: " + nif.getId());
                        continue;
                    }

                    sb.append(nifIP);
                    sb.append(":");
                    sb.append(metric);
                    sb.append(" ");
                }
            } catch (NodeNotFoundException e) {
                //should never happen
                logger.fatal("Node not found!!!");
                return;
            } catch (NodeInterfaceNotFoundException e) {
                logger.error("Node interface not found for node with IP: " + IP);
                continue;
            }

            String command = sb.toString();
            logger.info("Sending link weights to host with IP: " + IP);

            DatagramPacket dp = new DatagramPacket(command.getBytes(), command.getBytes().length, ina, DEFAULT_PORT);
            writer.writeRequest(ina.getHostAddress(), "Link Weights Update request", command);
            try {
                if (!TESTMODE) {
                    new Thread(new LinkWeightsUDPReceiver(client, writer)).start();
                    client.send(dp);
                }
            } catch (IOException e) {
                logger.error("Link weights could not be sent to host with IP: " + IP);
                logger.error(e.getClass().getSimpleName() + " : " + e.getMessage());
            }

        }

    }

    public void stop() {
        logger.info("Stopping the receiver threads.");
        //close the socket (it'll stop the receiver)
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] != null) clients[i].close();
        }
    }

}

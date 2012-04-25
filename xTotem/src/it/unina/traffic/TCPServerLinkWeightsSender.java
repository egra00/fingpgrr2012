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
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
 * Sends link weights to a server through a TCP socket on port 9500.
 * node interfaces sould be defined and should contains IP address
 * Link weights are send in the format: <br>
 * <code>if_IP_address:weight[ if_IP_address:weight]*</code>
 * <p/>
 * <p>Creation date: 19 jun. 2006
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class TCPServerLinkWeightsSender implements LinkWeightsSender {
    private static Logger logger = Logger.getLogger(TCPServerLinkWeightsSender.class);

    public static final int DEFAULT_PORT = 9500;
    public static final String DEFAULT_SERVER = "127.0.0.1";

    private static final boolean TESTMODE = true;

    private Domain domain;

    private Writer writer;

    private Socket socket = null;
    private OutputStream os = null;
    private InputStream is = null;

    public TCPServerLinkWeightsSender(Domain domain, String server) throws IOException {
        this.domain = domain;
        writer = new ColorConsoleWriter();
        socket = new Socket(server, DEFAULT_PORT);
        os = socket.getOutputStream();
        is = socket.getInputStream();
    }

    public TCPServerLinkWeightsSender(Domain domain, Writer writer, String server, int port) throws IOException {
        this.domain = domain;
        this.writer = writer;
        socket = new Socket(server, port);
        os = socket.getOutputStream();
        is = socket.getInputStream();
    }

    /**
     * This method sends the link weight the the hosts of the domain.
     * It doesn't throw exception but log messages about which hosts cause errors.
     */
    public void sendLinkWeights() {

        DataOutputStream dos = new DataOutputStream(os);

        TCPReceiver receiver = new TCPReceiver(is, writer);
        new Thread(receiver).start();

        for (Node n : domain.getAllNodes()) {
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
                logger.error("Node interface not found for node : " + n.getId());
                continue;
            }

            String command = sb.toString();
            logger.info("Sending link weights to server for node: " + n.getId());

            try {
                if (!TESTMODE) {
                    dos.writeBytes(command);
                    dos.flush();
                }
                writer.writeRequest(n.getId(), "Link Weights Update request", command);
            } catch (IOException e) {
                e.printStackTrace();
                writer.writeError(e.getClass().getSimpleName() + " : " + e.getMessage());
            }
        }

    }

    public void stop() {
        logger.info("Killing the socket");
        //close the socket (it'll stop the receiver)
        try {
            if (is != null) is.close();
            if (os != null) os.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

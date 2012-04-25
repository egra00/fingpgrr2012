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

import be.ac.ulg.montefiore.run.totem.domain.model.BandwidthUnit;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

/*
* Changes:
* --------
*
*/

/**
 * <Replace this by a description of the class>
 * <p/>
 * <p>Creation date: 27/11/2006
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class LSPEstablishment {

    private static final Logger logger = Logger.getLogger(LSPEstablishment.class);

    private static final int DEFAULT_PORT = 8889;

    private static final boolean TESTMODE = true;

    private boolean sendBandwidth = false;

    private Writer writer;

    private HashMap<String, Socket> clients;

    public LSPEstablishment() {
        this(new ColorConsoleWriter());
    }

    public LSPEstablishment(Writer writer) {
        this.writer = writer;
        clients = new HashMap<String, Socket>();
    }

    public void establishLSPs(Domain domain) {

        if (domain.getNbLsps() == 0) {
            logger.warn("Nothing to do: No lsp in the domain with ID: " + domain.getASID());
            return;
        }

        for (Lsp lsp : domain.getAllLsps()) {
            String ingressIP = lsp.getLspPath().getNodePath().get(0).getRid();
            if (ingressIP == null) {
                logger.error("No IP found for node " + lsp.getLspPath().getNodePath().get(0).getId() + "..." + "Skipping lsp " + lsp.getId());
                continue;
            }

            Socket socket = clients.get(ingressIP);
            if (socket == null) {
                InetAddress ina;
                try {
                    ina = InetAddress.getByName(ingressIP);
                } catch (UnknownHostException e) {
                    logger.error("Unknown host: " + ingressIP);
                    continue;
                }

                try {
                    socket = new Socket(ina, DEFAULT_PORT);
                    clients.put(ingressIP, socket);
                } catch (IOException e) {
                    logger.error("Could not cretae socket for IP " + ina.getHostAddress() + ": " + e.getMessage());
                    continue;
                }
            }

            String command = "";
            if (sendBandwidth) {
                command += BandwidthUnit.BPS.convert(domain.getBandwidthUnit(), lsp.getReservation());
                command += " ";
            }
            for (Node n : lsp.getLspPath().getNodePath()) {
                command += n.getRid();
                command += " ";
            }

            logger.info("Sending LSP establishment request for lsp \"" + lsp.getId() + "\" to " + ingressIP);


            new Thread(new LSPEstablishmentTCPReceiver(socket, writer));
            try {
                writer.writeRequest(socket.getInetAddress().getHostAddress(), "LSP establishment request", command);
                if (!TESTMODE) {
                    socket.getOutputStream().write(command.getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("LSP establishment request could not be sent to host with IP: " + ingressIP);
                logger.error(e.getClass().getSimpleName() + " : " + e.getMessage());
            }
        }

    }

    public void stop() {
        for (Socket socket : clients.values()) {
            try {
                socket.getOutputStream().close();
                socket.getInputStream().close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        clients.clear();
    }

}

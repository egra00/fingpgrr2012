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

import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.BandwidthUnit;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;

import java.net.*;
import java.io.*;
import java.util.HashMap;

import org.apache.log4j.Logger;
import it.unina.traffic.packetSize.PacketSize;
import it.unina.traffic.packetSize.ConstantPacketSize;
import it.unina.traffic.packetSize.UniformPacketSize;
import it.unina.traffic.packetSize.ExponentialPacketSize;
import it.unina.traffic.interDepartureTime.*;

/*
* Changes:
* --------
*
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 3 oct. 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class TrafficGenerator {
    private static final Logger logger = Logger.getLogger(TrafficGenerator.class);

    private static final boolean TESTMODE = false;

    public final int DEFAULT_PORT_SENDER_MANAGER = 8998;
    private int remotePort = 9021;

    private DatagramSocket[] clients;

    // use for testing purpose. Single client used when sending traffic to a specified host only
    private DatagramSocket tmpClient = null;

    private Writer writer;

    public TrafficGenerator() {
        writer = new ColorConsoleWriter();
    }

    public TrafficGenerator(Writer writer) {
        this.writer = writer;
    }

    /**
     * Send commands on the client socket to generate traffic with D-ITG
     * @param params
     * @throws InvalidTrafficMatrixException
     * @throws InvalidDomainException
     */
    public void sendTraffic(HashMap<String, String> params) throws InvalidTrafficMatrixException, InvalidDomainException {
        Domain domain = InterDomainManager.getInstance().getDefaultDomain();
        if (domain == null) throw new InvalidDomainException("No default domain");
        TrafficMatrix tm = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(domain.getASID());
        sendTraffic(domain, tm, params);
    }

    /**
     * Send commands on the client socket to generate traffic with D-ITG.
     * Sends packets of constant size with constant inter departure time.
     * remote port is incremented for each request.
     * @param domain
     * @param tm
     * @param params
     */
    public void sendTraffic(Domain domain, TrafficMatrix tm, HashMap<String, String> params) {
        if (domain == null || tm == null) throw new IllegalArgumentException("Null argument");

        StringBuffer sb = new StringBuffer();

        // default packet size
        int meanPacketSize = 512;

        if (params != null) {
            String value;
            if ((value = params.get("protocol")) != null) {
                if (value.equals("UDP") || value.equals("TCP")) {
                    sb.append(" -T ");
                    sb.append(value);
                } else {
                    logger.error("Unknown protocol : " + value);
                }
            }

            if ((value = params.get("DSbyte")) != null) {
                try {
                    byte b = Byte.valueOf(value);
                    sb.append(" -b ");
                    sb.append(b);
                } catch (NumberFormatException e) {
                    logger.error("Error in DSbyte value : " + value);
                }
            }

            if ((value = params.get("duration")) != null) {
                try {
                    int duration = Integer.valueOf(value);
                    sb.append(" -t ");
                    sb.append(duration);
                } catch (NumberFormatException e) {
                    logger.error("Error in duration value : " + value);
                }
            }

            if ((value = params.get("meanPacketSize")) != null) {
                try {
                    meanPacketSize = Integer.valueOf(value);
                    sb.append(" -c ");
                    sb.append(meanPacketSize);
                } catch (NumberFormatException e) {
                    logger.error("Error in packetSize value : " + value);
                }
            }
        }

        clients = new DatagramSocket[domain.getNbNodes()];
        int top = 0;

        for (Node src : domain.getAllNodes()) {
            String srcIP = src.getRid();
            if (srcIP == null) {
                logger.error("No IP found for node : " + src.getId());
                continue;
            }

            InetAddress ina = null;
            try {
                ina = InetAddress.getByName(srcIP);
            } catch (UnknownHostException e) {
                logger.error("Unknow host: " + srcIP);
                continue;
            }

            DatagramSocket client;
            try {
                clients[top] = new DatagramSocket();
                client = clients[top];
                top++;
            } catch (SocketException e) {
                e.printStackTrace();
                logger.error("Unable to create socket for host: " + srcIP + ": " + e.getMessage());
                continue;
            }

            for (Node dst : domain.getAllNodes()) {
                String dstIP = dst.getRid();
                if (dstIP == null) {
                    logger.error("No IP found for node : " + dst.getId());
                    continue;
                }

                float tmBw;
                try {
                    tmBw = tm.get(src.getId(), dst.getId());
                } catch (NodeNotFoundException e) {
                    //e.printStackTrace();
                    logger.fatal("Node not found!!!");
                    return;
                }

                if (tmBw == 0) continue;

                String pktsDistrib = params.get("PacketSizeDistribution");

                PacketSize pktsSize;
                if (pktsDistrib.equals("Constant")) {
                    pktsSize = new ConstantPacketSize();
                } else if (pktsDistrib.equals("Uniform")) {
                    pktsSize = new UniformPacketSize();
                } else if (pktsDistrib.equals("Exponential")) {
                    pktsSize = new ExponentialPacketSize();
                } else {
                    if (pktsDistrib != null) logger.error("Unknown PacketSize distribution: " + pktsDistrib + ", using Uniform.");
                    else logger.info("PacketSize distribution not found, using Uniform.");
                    pktsSize = new UniformPacketSize();
                }

                pktsSize.setMean(meanPacketSize);

                String IDTDistrib = params.get("IDTDistribution");
                if (IDTDistrib == null) IDTDistrib = "Uniform";

                double meanBitsPerPacket = pktsSize.getMean() * 8;
                double meanBitsPerSecond = tm.getUnit() == null ? BandwidthUnit.BPS.convert(domain.getBandwidthUnit(), tmBw) : BandwidthUnit.BPS.convert(tm.getUnit(), tmBw);
                double meanPacketPerSecond = meanBitsPerSecond / meanBitsPerPacket;

                InterDepartureTime IDT;
                if (IDTDistrib.equals("Constant")) {
                    IDT = new ConstantIDT(meanPacketPerSecond);
                } else if (IDTDistrib.equals("Uniform")) {
                    IDT = new UniformIDT(meanPacketPerSecond);
                } else if (IDTDistrib.equals("Exponential")) {
                    IDT = new ExponentialIDT(meanPacketPerSecond);
                } else if (IDTDistrib.equals("Normal")) {
                    IDT = new NormalIDT(meanPacketPerSecond);
                } else if (IDTDistrib.equals("Poisson")) {
                    IDT = new PoissonIDT(meanPacketPerSecond);
                } else {
                    if (IDTDistrib != null) logger.error("Unknown IDT distribution: " + IDTDistrib + ", using Uniform.");
                    else logger.info("IDT distribution not found, using Uniform.");
                    IDT = new UniformIDT(meanPacketPerSecond);
                }

                String command = "-a " + dstIP + " -rp " + remotePort++ + " " + pktsSize.getCommand() + " " + IDT.getCommand() + sb.toString();

                logger.debug("Sending traffic generation request to " + srcIP + ". Command: " + command);

                writer.writeRequest(ina.getHostAddress(), "Traffic generation request", command);

                DatagramPacket dp = new DatagramPacket(command.getBytes(), command.getBytes().length, ina, DEFAULT_PORT_SENDER_MANAGER);

                try {
                    new Thread(new TrafficGeneratorUDPReceiver(client, writer)).start();
                    if (!TESTMODE)
                        client.send(dp);

                } catch (IOException e) {
                    logger.error("Traffic generation request could not be send to host: " + srcIP + " dest: " + dstIP);
                    logger.error(e.getClass().getSimpleName() + " : " + e.getMessage());
                }
            }
        }
    }

    public void sendTraffic(String srcHost, String dstHost, float bps) throws IOException {

        if (tmpClient == null) {
            tmpClient = new DatagramSocket();
        }

        InetAddress ina = InetAddress.getByName(srcHost);

        double nbPacketsS = bps / 512 * 8;
        String command = "-a " + dstHost + " -rp "+ remotePort++ + " -C "+ nbPacketsS + " -c 512" + " -T UDP";

        DatagramPacket dp = new DatagramPacket(command.getBytes(), command.getBytes().length, ina, DEFAULT_PORT_SENDER_MANAGER);

        new Thread(new TrafficGeneratorUDPReceiver(tmpClient, writer)).start();
        tmpClient.send(dp);

    }

    public void stop() {
        logger.info("Stopping the receiver threads.");
        //close the socket (it'll stop the receiver)
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] != null) clients[i].close();
        }
    }
}

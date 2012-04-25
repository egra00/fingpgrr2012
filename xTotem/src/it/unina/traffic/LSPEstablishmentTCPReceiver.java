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

import org.apache.log4j.Logger;

import java.net.Socket;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;

/*
* Changes:
* --------
*
*/

/**
* TODO: respionse message format yet to be defined
*
* <p>Creation date: 29/11/2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LSPEstablishmentTCPReceiver implements Runnable {
    private static Logger logger = Logger.getLogger(LSPEstablishmentTCPReceiver.class);

    private Socket socket;
    private Writer writer;

    public LSPEstablishmentTCPReceiver(Socket socket, Writer writer) {
        this.socket = socket;
        this.writer = writer;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                writer.writeResponse(socket.getInetAddress().getHostAddress(), "LSP establishment ACK", br.readLine());
            }
        } catch (IOException e) {
            //e.printStackTrace();
            logger.info("End of TCP Receiver Thread");
        }
    }
}

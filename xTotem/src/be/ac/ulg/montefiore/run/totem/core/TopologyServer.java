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
package be.ac.ulg.montefiore.run.totem.core;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/*
* Changes:
* --------
*
*/

/**
* This class starts a server. When a client connects to it, it sends a topology file, then close the connection.
* <p>
* Use it with the ant task : build-toposerver<br>
* (manifest file : MANIFEST_TOPOSERVER.MF)
*
* <p>Creation date: 3 oct. 2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class TopologyServer {

    private int port;
    private String filename;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Start a server that send a topology file when a client connects.");
            System.out.println("Command line arguments: <port> <filename>");
            return;
        }

        int port = Integer.valueOf(args[0]);
        String filename = String.valueOf(args[1]);

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File does not exists.");
            return;
        }

        TopologyServer st = new TopologyServer(filename, port);

        try {
            st.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public TopologyServer(String filename, int port) {
        this.filename = filename;
        this.port = port;
    }

    public void start() throws IOException {
        ServerSocket ss = new ServerSocket(port);

        Socket client = ss.accept();

        OutputStream os = client.getOutputStream();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));

        BufferedReader br = new BufferedReader(new FileReader(filename));

        String line = br.readLine();

        while (line != null) {
            System.out.println("\033[32mWriting line:\033[0m");
            System.out.println(line);
            bw.write(line);
            bw.write("\n");
            line = br.readLine();
        }

        System.out.println("\033[32mFlushing buffer\033[0m");
        bw.flush();

        client.shutdownOutput();
    }
}

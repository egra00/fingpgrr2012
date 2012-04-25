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
package be.ac.ulg.montefiore.run.totem.socketInterface;

import java.util.concurrent.BlockingQueue;
import java.net.Socket;
import java.io.*;

/*
 * Changes:
 * --------
 * 09-June-2005: changed to use LinkedBlockingQueue instead of traditional synchronized/wait/notify calls.
 * 24-Apr.-2006: add colors for linux console (GMO)
 * 26-Sept-2006: bugfixes & add port attribute (GMO)
 * 28-Sept-2006: rewrite (GMO)
 */

/**
 * It listens on a socket to messages and stores them in a BlockingQueue (along with client outputStream) where they
 * will be read by the Consumer class.
 * <p/>
 * <p>Creation date: 03-June-2005
 *
 * @author Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 * @author Gael Monfort (monfort@run.montefiore.ulg.ac.be)
 */


public class Producer implements Runnable {

    private Socket client;
    private BlockingQueue<Server.Input> queue;

    public Producer(Socket client, BlockingQueue<Server.Input> queue) {
        this.client = client;
        this.queue = queue;
    }

    public void run() {
        try {
            InputStream in = client.getInputStream();
            OutputStream out  = client.getOutputStream();

            BufferedReader bin = new BufferedReader(new InputStreamReader(in));

            while (true) {
                //read a new line
                System.out.println("\033[35mReading a line on the socket...\033[0m");

                /*
                if (!bin.ready()) {
                    System.out.println("\033[35mEnd of buffer. Client disconnected.\033[0m");
                    break;
                }
                */

                String someString = bin.readLine();

                if (someString == null) {
                    System.out.println("\033[35mEnd of buffer. Client disconnected.\033[0m");
                    break;                    
                }

                System.out.println("\033[35mMessage received from socket : \033[0m" + someString);

                System.out.println("\033[35mPut a message on the queue...\033[0m");


                try {
                    queue.put(new Server.Input(someString, out));
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                    System.out.println("\033[35mProducer interrupted.\033[0m");
                    return;
                }

                if (someString.equals("stop")) {
                    break;
                }
            }
            System.out.println("\033[35mComing out of producer\033[0m");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

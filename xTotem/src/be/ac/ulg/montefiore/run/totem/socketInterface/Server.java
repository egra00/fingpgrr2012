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

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.LinkedBlockingQueue;

/*
* Changes:
* --------
* - 18-Jun-2007: Move some cleaning code from catch to finally block (GMO) 
*/

/**
*
* Wait for clients on a socket interface.
* Receives scenario events from the socket interface, treat them and sends responses.
*<p>
* A call to start(.) method will create a new thread that waits for clients and a "consumer" to execute received
* scenario events.<br>
* When a client is connected, a "producer" thread is launched. It reads messages coming from socket and puts them in a
* queue. The consumer executes the events from the queue and sends the response to the client.<p>
*
* note: Multiple clients can be enabled by removing the call to producerThread.join() in the start method. but this was not tested.<br>
* note: Does not seem to stop correctly.
*
*
* <p>Creation date: 26 sept. 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class Server {
    /**
     * Simple structure to use in the message queue. It contains the message itself and the client stream where to send
     * the response. 
     */
    static public final class Input {
        private String message;
        //stream on which to write message response
        private OutputStream outputStream;

        public Input(String message, OutputStream outputStream) {
            this.message = message;
            this.outputStream = outputStream;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public OutputStream getOutputStream() {
            return outputStream;
        }

        public void setOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
        }
    }

    public static final int DEFAULT_PORT = 1234;

    private LinkedBlockingQueue<Input> messagesQueue;

    private static Server server = null;

    private Thread serverThread = null;

    private ServerSocket listener;

    private Server() {
         messagesQueue = new LinkedBlockingQueue<Input>();
    }

    public static Server getInstance() {
        if (server == null)
            server = new Server();
        return server;
    }

    public void start() throws IOException {
        start(DEFAULT_PORT);
    }

    public void start(final int port) throws IOException {
        // wait for the client to connect

        listener = new ServerSocket(port);

        Runnable a  = (new Runnable() {

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
                Thread consumerThread = null;
                Thread producerThread = null;
                try {
                    Consumer consumer = new Consumer(messagesQueue);
                    consumerThread = new Thread(consumer);
                    consumerThread.start();


                    while (true) {
                        System.out.println("\033[34mWaiting for a client.\033[0m");

                        Socket client = listener.accept();

                        System.out.println("\033[34mClient connected.\033[0m");


                        //create producer

                        Producer producer = new Producer(client, messagesQueue);

                        producerThread = new Thread(producer);
                        producerThread.start();

                        try {
                            producerThread.join();
                        } catch (InterruptedException e) {
                            producerThread.interrupt();
                            producerThread = null;
                            consumerThread.interrupt();
                            consumerThread = null;
                            System.out.println("\033[34mServer interrupted\033[0m");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("\033[34mComing out of server: " + e.getMessage() + "\033[0m");
                } finally {
                    messagesQueue.clear();
                    if (producerThread != null) {
                        producerThread.interrupt();
                        producerThread = null;
                    }
                    if (consumerThread != null) {
                        consumerThread.interrupt();
                        consumerThread = null;
                    }
                }
            }
        });

        serverThread = new Thread(a);
        serverThread.start();
    }

    public void stop() {
        serverThread.interrupt();
        try {
            listener.close();
        } catch (IOException e) {
            System.out.println("\033[34mError closing the socket.\033[0m");
            e.printStackTrace();
        }
    }

    /**
     * Waits for the server to finish execution.
     */
    public void join() {
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
    }

}

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

import be.ac.ulg.montefiore.run.totem.scenario.model.Scenario;
import be.ac.ulg.montefiore.run.totem.scenario.persistence.ScenarioFactory;
import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;
import be.ac.ulg.montefiore.run.totem.util.jaxb.runtime.MarshallerImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.net.Socket;
import java.io.*;
import java.util.Iterator;

import com.sun.xml.bind.JAXBObject;
import com.sun.tools.xjc.generator.validator.StringOutputStream;

/*
* Changes:
* --------
* 10-Oct-2006: add configurable delay, update javadoc (GMO)
*/

/**
* Client to use with the socket interface. It sends a scenario file to the socket interface of the toolbox on the
* specified host and port. It also creates a receiver thread to receive the responses from the server.
*
* Scenario events are sent one by one, on a single line. A configurable delay period is observed between each event sending.  
*
* <p>Creation date: 25 sept. 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class SocketClient {
    private Socket client;
    private Thread receiver;

    private long delay;

    public static void main(String[] args) {
        if (args.length != 3 && args.length != 4) {
            System.out.println("Sends a scenario file to the socket interface of a running Toolbox.");
            System.out.println("Command line arguments: <host> <port> <filename> [delay s]");
            return;
        }

        String host = String.valueOf(args[0]);
        int port = Integer.valueOf(args[1]);
        String filename = String.valueOf(args[2]);
        float delay = args.length > 3 ? Float.valueOf(args[3]) : 1.0f;

        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File does not exists.");
            return;
        }

        try {
            (new SocketClient(host, port, delay)).sendScenarioSocket(filename);
        } catch (IOException e) {
            System.out.println("Could not start the client.");
            e.printStackTrace();
        }

    }


    public SocketClient(String host, int port, float delay) throws IOException {
        //create the socket
        client = new Socket(host, port);
        this.delay = (long) (1000*delay);
    }

    /**
     * Send a scenario file to the toolbox
     * @param filename
     */
    public void sendScenarioSocket(String filename) {
        Scenario scenario = (Scenario) ScenarioFactory.loadScenario(filename);

        try {
            OutputStream out = client.getOutputStream();
            PrintWriter pout = new PrintWriter(out,true);
            String message;

            JAXBContext jaxbContext = JAXBContext.newInstance(PreferenceManager.getInstance().getPrefs().get("SCENARIO-PACKAGES", "be.ac.ulg.montefiore.run.totem.scenario.model.jaxb:be.ac.ucl.ingi.totem.scenario.model.jaxb"));
            Marshaller marshaller = jaxbContext.createMarshaller();
            //marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(MarshallerImpl.XMLDECLARATION, Boolean.FALSE);
            //marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, PreferenceManager.getInstance().getPrefs().get("SCENARIO-SCHEMA-LOCATION", "http://jaxb.model.scenario.totem.run.montefiore.ulg.ac.be http://totem.run.montefiore.ulg.ac.be/Schema/Scenario-v1_1.xsd http://jaxb.model.scenario.totem.ingi.ucl.ac.be http://totem.run.montefiore.ulg.ac.be/Schema/CBGP-Scenario-v1_0.xsd"));

            //start the receiver
            receiver = new Thread(new ClientReceiver(scenario.getEvent().size()));
            receiver.start();

            for (Iterator i = scenario.getEvent().iterator(); i.hasNext();) {
                JAXBObject b = (JAXBObject) i.next();
                //first marshal into a string
                StringWriter sw = new StringWriter();
                StringOutputStream sos = new StringOutputStream(sw);
                marshaller.marshal(b, sos);
                message = "";
                //all on one line
                String[] lines = sw.toString().split("\n");
                for (String s : lines) {
                    message = message.concat(s.trim());
                }
                //output the message
                System.out.println("\033[32mSending message \033[0m" + message);
                //send the string to the socket
                pout.println(message);
                pout.flush();

                Thread.sleep(delay);
            }

            receiver.interrupt();
            receiver = null;

            /*
            //sending stop message
            message = "stop";
            System.out.println("\033[32mSending message \033[0m" + message);
            pout.println(message);
            pout.flush();

            receiver.join();
            */

            //Thread.sleep(2000);

            /*
            System.out.println("\033[32mClose the socket.\033[0m");

            client.close();
            */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClientReceiver implements Runnable {
        private int nbIter;

        public ClientReceiver(int nbIter) {
            this.nbIter = nbIter;
        }

        public void run() {
            try {
                InputStream in = client.getInputStream();

                for (int i = 0; i < nbIter; i++) {
                    System.out.println("\033[31mWaiting for a message...\033[0m");
                    BufferedReader bin = new BufferedReader(new InputStreamReader(in));
                    String someString = bin.readLine();
                    System.out.println("\033[31mMessage received : \033[0m" + someString);

                    if (someString == null) {
                        System.out.println("\033[31mthe thread should be killed now...\033[0m");
                        break;
                    }
                }
                System.out.println("\033[31mReceived all " + nbIter + " response. Now closing socket. \033[0m");
                client.close();
            } catch (IOException e) {
                System.out.println("\033[31mError reading on socket. Socket closed?\033[0m");
                //e.printStackTrace();
            }

        }
    }

}

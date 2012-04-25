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

import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.Scenario;
import be.ac.ulg.montefiore.run.totem.scenario.model.Event;
import be.ac.ulg.montefiore.run.totem.scenario.model.EventResult;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.LspImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.NodeImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.LinkImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Mpls;
import be.ac.ulg.montefiore.run.totem.util.jaxb.runtime.MarshallerImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import com.sun.tools.xjc.generator.validator.StringOutputStream;
import com.sun.xml.bind.JAXBObject;


/*
 * Changes:
 * --------
 * 24-Apr.-2006: add colors for linux console (GMO)
 * 24-Apr-2006 : made more generic (GMO)
 * 25-Sep-2006: better handling of non-event message (JAXBException) (GMO)
 * 26-Sep-2006: replace EOL character by <br/> in the XML response (GMO)
 * 10-Oct-2006: now writes direclty on the blocking queue, change constructor (GMO)
 */

/**
 * This class consumes elements found in the BlockingQueue filled by the Producer.  Then it unmarshalls the message of this
 * element which should be XML scenario events, and executes the corresponding action. It then write the response on
 * the client stream.
 * <p/>
 * <p>Creation date: 03-06-2005
 *
 * @author Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 * @author Gael Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class Consumer implements Runnable {

    private static final Logger logger = Logger.getLogger(Consumer.class);

    private static final String scenarioHeader = "<scenario xsi:schemaLocation=\"http://jaxb.model.scenario.totem.run.montefiore.ulg.ac.be http://totem.run.montefiore.ulg.ac.be/Schema/Scenario-v1_1.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://jaxb.model.scenario.totem.run.montefiore.ulg.ac.be\">";
    private static final String scenarioFooter = "</scenario>";

    private BlockingQueue<Server.Input> queue;

    public Consumer(BlockingQueue<Server.Input> queue) {
        this.queue = queue;
    }

    public void run() {
        while (true) {

            System.out.println("\033[36mTake a message on the queue...\033[0m");

            Server.Input input = null;
            try {
                input = queue.take();
            } catch (InterruptedException e) {
                //e.printStackTrace();
                System.out.println("\033[36mConsumer interrupted.\033[0m");
                return;
            }
            String message = input.getMessage();

            PrintWriter pout = new PrintWriter(input.getOutputStream(), false);


            System.out.println("\033[36mGot message :\033[0m" + message);
            if (message.equals("stop")) break;

            String copyMessage = message.toString();

            message = scenarioHeader.concat(message).concat(scenarioFooter);
            StringReader stringReader = new StringReader(message);
            InputSource inputSource = new InputSource(stringReader);

            Scenario scenario = null;
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(PreferenceManager.getInstance().getPrefs().get("SCENARIO-PACKAGES", "be.ac.ulg.montefiore.run.totem.scenario.model.jaxb:be.ac.ucl.ingi.totem.scenario.model.jaxb"));
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                scenario = (Scenario) unmarshaller.unmarshal(inputSource);
            } catch (JAXBException e) {
                logger.error("JAXBException when unmarshalling. Message: " + e.getMessage());
                e.printStackTrace();
                String ret = buildScenarioEventError(copyMessage, "JAXBException when unmarshalling.", e);
                System.out.println("\033[36mSending the response to the socket :\033[0m" + ret);
                pout.println(ret);
                pout.flush();
                continue;
            }

            Marshaller m;
            try {
                JAXBContext jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.domain.model.jaxb");
                m = jc.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
                m.setProperty(MarshallerImpl.XMLDECLARATION, Boolean.FALSE);
            } catch (JAXBException e) {
                logger.error("JAXBException when creating marshaller. Message: " + e.getMessage());
                e.printStackTrace();
                String ret = buildScenarioEventError(copyMessage, "JAXBException when creating marshaller.", e);
                System.out.println("\033[36mSending the response to the socket :\033[0m" + ret);
                pout.println(ret);
                pout.flush();
                continue;
            }

            Iterator iterator = scenario.getEvent().iterator();
            for (; iterator.hasNext();) {
                String status;
                String object = "";
                String msg = "";
                String exception = "";

                Event event = (Event) iterator.next();
                try {
                    EventResult er = event.action();
                    Object r = er.getObject();

                    if (r instanceof JAXBObject) {
                        JAXBObject b = (JAXBObject) r;
                        StringWriter sw = new StringWriter();
                        StringOutputStream output = new StringOutputStream(sw);

                        be.ac.ulg.montefiore.run.totem.domain.model.jaxb.ObjectFactory of = new be.ac.ulg.montefiore.run.totem.domain.model.jaxb.ObjectFactory();
                        try {
                            //This encapsulate elements from the domain in the upper level element, so the call to marshal()
                            // will marshal the complete object.
                            if (b instanceof LspImpl) {
                                Mpls mpls = of.createMpls();
                                mpls.getLsp().add(b);
                                b = (JAXBObject) mpls;
                            } else if (b instanceof NodeImpl) {
                                be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Topology.NodesType nodes = of.createTopologyNodesType();
                                nodes.getNode().add(b);
                                b = (JAXBObject) nodes;
                            } else if (b instanceof LinkImpl) {
                                be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Topology.LinksType links = of.createTopologyLinksType();
                                links.getLink().add(b);
                                b = (JAXBObject) links;
                            }

                            m.marshal(b, output);
                            //all on one line
                            String[] lines = sw.toString().split("\n");
                            for (String s : lines) {
                                object = object.concat(s.trim());
                            }
                        } catch (JAXBException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (r != null) object = r.toString();
                    }
                    msg = er.getMessage();
                    status = "OK";
                } catch (EventExecutionException e) {
                    status = "FAILED";
                    String classStr;
                    String excMsg;
                    if (e.getCause() == null) {
                        excMsg = e.getMessage();
                        classStr = e.getClass().getSimpleName();
                    } else {
                        excMsg = e.getCause().getMessage();
                        classStr = e.getCause().getClass().getSimpleName();
                    }

                    exception = "<exception class=\"" + classStr + "\">";
                    exception += excMsg;
                    exception += "</exception>";
                }

                StringBuilder returnMessage = new StringBuilder("<result><command>");
                returnMessage.append(copyMessage);
                returnMessage.append("</command><output>");
                returnMessage.append("<object>");
                returnMessage.append(object);
                returnMessage.append("</object>");
                returnMessage.append("<message>");
                returnMessage.append(msg);
                returnMessage.append("</message>");
                returnMessage.append("</output>");
                returnMessage.append("<status>");
                returnMessage.append(status);
                returnMessage.append("</status>");
                returnMessage.append("<exceptions>");
                returnMessage.append(exception);
                returnMessage.append("</exceptions>");
                returnMessage.append("</result>");

                String ret = returnMessage.toString().replaceAll("\n", "<br/>");

                System.out.println("\033[36mSending the response to the socket :\033[0m" + ret);
                pout.println(ret);
                pout.flush();

            }

        }
        System.out.println("\033[36mComing out of consumer\033[0m");

    }

    /**
     * Sends a default error when the Event can't be executed (Bad event format, ...)
     */
    private String buildScenarioEventError(String copyMessage, String message, Exception e) {
        StringBuilder returnMessage = new StringBuilder("<result><command>");
        returnMessage.append(copyMessage);
        returnMessage.append("</command><output>");
        returnMessage.append("<object>");
        returnMessage.append("</object>");
        returnMessage.append("<message>");
        returnMessage.append(message);
        returnMessage.append("</message>");
        returnMessage.append("</output>");
        returnMessage.append("<status>");
        returnMessage.append("FAILED");
        returnMessage.append("</status>");
        returnMessage.append("<exceptions>");

        String exception = "<exception class=\"" + e.getClass().getSimpleName() + "\">";
        exception += e.getMessage();
        exception += "</exception>";

        returnMessage.append(exception);
        returnMessage.append("</exceptions>");
        returnMessage.append("</result>");

        String ret = returnMessage.toString().replaceAll("\n", "<br/>");

        return ret;
    }

}

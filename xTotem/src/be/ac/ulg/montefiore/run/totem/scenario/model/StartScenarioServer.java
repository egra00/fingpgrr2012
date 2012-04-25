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
package be.ac.ulg.montefiore.run.totem.scenario.model;

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.StartScenarioServerImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.socketInterface.Server;

import java.io.IOException;


/*
 * Changes:
 * --------
 * - 30-Mar-2006: move into be.ac.ulg.montefiore.run.totem.scenario.model package (JLE).
 * - 25-Sept-2006: add port for producer (GMO).
 * - 10-Oct-2006: rewrite : change architecture (GMO) 
 */

/**
 * This class implements a StartScenarioServer event. It starts a server that listens to XML events.
 *
 * <p>Creation date: 03-Jun-2005
 *
 * @author Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 * @author Gael Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class StartScenarioServer extends StartScenarioServerImpl implements Event {

    private static final Logger logger = Logger.getLogger(StartScenarioServer.class);

    public StartScenarioServer() {}

    /**
     * @see Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a StartScenarioServer");

        try {
            if (isSetPort()) Server.getInstance().start(getPort());
            else Server.getInstance().start();
            Server.getInstance().join();
        } catch (IOException e) {
            throw new EventExecutionException(e);
        }

        return new EventResult(null, "Starting to listen to remote XML events.");
    }
}

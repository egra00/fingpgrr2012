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
package be.ac.ulg.montefiore.run.totem.repository.SAMTE.scenario;

import be.ac.ulg.montefiore.run.totem.repository.SAMTE.scenario.jaxb.impl.GenerateCPLImpl;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPL;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPLGenerator;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPLGeneratorParameter;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPLFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.Event;
import be.ac.ulg.montefiore.run.totem.scenario.model.EventResult;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomainBuilder;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
 * Implements the GenerateCPL simulation scenario event.
 *
 * It allows to generate a candidate path list with a maximum number of path and a maximum depth during
 * the all distinct path algorithm computations.
 *
 * <p>Creation date: 23-Jun-2005 14:47:38
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class GenerateCPL extends GenerateCPLImpl implements Event {

    private static final Logger logger = Logger.getLogger(GenerateCPL.class);

    /**
     * This method must be implemented by each event. This method contains what must be done to
     * process the event.
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing an generateCPL event - ASID: "+ _ASID);
        boolean verbose = this.isSetVerbose() ? this._Verbose : false;

        try {
            // Get all the parameters
            int asId = isSetASID() ? _ASID : InterDomainManager.getInstance().getDefaultDomain().getASID();
            int nbPath = this.isSetNbPath() ? this._NbPath : 5;
            int maxDepth = this.isSetMaxDepth() ? this._MaxDepth : 5;
            String fileName = this.isSetFileName() ? this._FileName : "cpl.txt";
            Domain domain = InterDomainManager.getInstance().getDomain(asId);
            SimplifiedDomain sDomain = SimplifiedDomainBuilder.build(domain);

            // Generate Candidate Path List
            long time = System.currentTimeMillis();
            SinglePathCPLGenerator generator = new SinglePathCPLGenerator();
            SinglePathCPL cpl = generator.generate(new SinglePathCPLGeneratorParameter(maxDepth,sDomain,nbPath),verbose);
            time = System.currentTimeMillis() - time;
            if (verbose)
                cpl.analyse();
            SinglePathCPLFactory factory = new SinglePathCPLFactory();
            factory.saveCPL(fileName,cpl);

            StringBuffer line = new StringBuffer("Generating CPL ");
            line.append(fileName);
            line.append(" (nbPath: ");
            line.append(nbPath); line.append(", maxDepth: "); line.append(maxDepth);
            line.append(") takes "); line.append(time); line.append(" ms");
            String msg = line.toString();
            logger.info(msg);
            return new EventResult(cpl, msg);
        } catch (Exception e) {
            logger.error("An exception occurred. Message: "+e.getMessage());
            e.printStackTrace();
            throw new EventExecutionException(e);
        }
    }

}

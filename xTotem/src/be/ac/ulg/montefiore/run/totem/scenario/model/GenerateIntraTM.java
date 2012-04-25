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

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.GenerateIntraTMImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.facade.ScenarioExecutionContext;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.generation.InterDomainTrafficMatrixGeneration;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.generation.POPPOPTrafficMatrixGeneration;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.TrafficMatrixFile;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.persistence.TrafficMatrixFactory;
import be.ac.ulg.montefiore.run.totem.util.FileFunctions;
import org.apache.log4j.Logger;

import java.util.HashMap;

/*
* Changes:
* --------
* 09-Jan-2007: use scenario context for file name (GMO)
* 02-Feb-2007: fix bug in scenario context (GMO)
* 28-Jun-2007: Adapt to new interfaces of InterDomainTrafficMatrixGeneration and POPPOPTrafficMatrixGeneration. Unit conversion is now done in InterDomainTrafficMatrixGeneration (GMO)
*/

/**
 * This scenario event produces an intra-domain traffic matrix from NetFlow and BGP information.
 *
 * <p>Creation date: 11-05-2005
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 * @author  Simon Balon (balon@run.montefiore.ulg.ac.be)
 */

public class GenerateIntraTM extends GenerateIntraTMImpl implements Event{
    private static final Logger logger = Logger.getLogger(GenerateIntraTM.class);

    public GenerateIntraTM() {}

    public GenerateIntraTM(String BGPbaseDirectory,String BGPdirFileName, String NETFLOWbaseDirectory, String NETFLOWdirFileName, String ClusterFileName, String TrafficMatrixFileName, int Minutes) {
        _BGPbaseDirectory = BGPbaseDirectory;
        _BGPdirFileName = BGPdirFileName;
        _NETFLOWbaseDirectory = NETFLOWbaseDirectory;
        _NETFLOWdirFileName = NETFLOWdirFileName;
        _ClusterFileName = ClusterFileName;
        _TrafficMatrixFileName = TrafficMatrixFileName;
        _Minutes = Minutes;
    }


    public EventResult action() throws EventExecutionException {

        Domain domain = InterDomainManager.getInstance().getDefaultDomain();

        InterDomainTrafficMatrixGeneration IDtrafficMatrixGeneration = new InterDomainTrafficMatrixGeneration(domain, _Minutes, _SamplingRate);

        try {
            String BGPbaseDirectory = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), _BGPbaseDirectory);
            String BGPdirFileName = _BGPdirFileName;
            String NETFLOWbaseDirectory = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), _NETFLOWbaseDirectory);
            String NETFLOWdirFileName = _NETFLOWdirFileName;
            String ClusterFileName = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), _ClusterFileName);
            String TrafficMatrixFileName = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), _TrafficMatrixFileName);

            POPPOPTrafficMatrixGeneration trafficMatrixGeneration = new POPPOPTrafficMatrixGeneration(InterDomainManager.getInstance().getDefaultDomain());

            HashMap<String, String> hashMap = null;
            hashMap = trafficMatrixGeneration.readCluster(ClusterFileName, BGPbaseDirectory, BGPdirFileName);

            trafficMatrixGeneration.simRun();

            String[] suffixes = new String[1];
            suffixes[0] = "";

            TrafficMatrix tm = null;

            TrafficMatrixFile tmFile = IDtrafficMatrixGeneration.generateXMLTrafficMatrixfromNetFlow(NETFLOWbaseDirectory,NETFLOWdirFileName,suffixes);

            tm = trafficMatrixGeneration.generateTrafficMatrix(null,hashMap, tmFile);

            TrafficMatrixFactory.saveTrafficMatrix(TrafficMatrixFileName,tm);
            logger.info("TrafficMatrix saved as: " + TrafficMatrixFileName);

            return new EventResult(tm);
        } catch(Exception e){
            throw new EventExecutionException(e);
        }
    }
}

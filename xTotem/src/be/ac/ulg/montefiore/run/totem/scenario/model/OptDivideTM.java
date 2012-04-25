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

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.OptDivideTMImpl;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.OptDivideTMObjFun;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.LinkLoadComputerIdException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.LinkLoadComputerAlreadyExistsException;
import be.ac.ulg.montefiore.run.totem.repository.optDivideTM.OptDivideTMLoadComputer;

/*
 * Changes:
 * --------
 * - 26-Feb-2008: adapt to the new LinkLoadComputer interface (GMO)
 */

/**
 * The event starts the algorithm.  The resulting load
 * can be displayed using {@link ShowLinkLoad} event.
 * <br>
 * This class implements a Traffic Engineering method which consist to divide the traffic matrix into
 * N sub-matrices (called strata). Each stratum is routed independently of each other. Concerning the
 * implementation of such solution, it is possible to establish multiple MPLS full-mesh (N) or to use
 * the Multi-Topology functionality (N multiple topologies). This code only implements the multiple
 * full-mesh functionnality.
 *<br>
 * This method is described in "Dividing the Traffic Matrix to approach optimal Traffic Engineering"
 * by S. Balon and G. Leduc, submitted to ICON 2006
 *
 * <p>Creation date: 22-mars-2006
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 *
 */
public class OptDivideTM extends OptDivideTMImpl implements Event {

    private static final Logger logger = Logger.getLogger(OptDivideTM.class);
    
    public OptDivideTM() {}
    
    public OptDivideTM(int asId, int tmId) {
        setASID(asId);
        setTMID(tmId);
    }

    // TODO: add constructors

    public EventResult action() throws EventExecutionException {
        logger.debug("OptDivideTM called...");

        Domain domain;
        TrafficMatrix tm;
        int N;
        String objFunction;
        boolean establishMultipleFullMesh;
        boolean verbose;
        try {
            if (this.isSetASID()) {
                domain = InterDomainManager.getInstance().getDomain(this.getASID());
            } else {
                domain = InterDomainManager.getInstance().getDefaultDomain();
                this.setASID(domain.getASID());
            }

            if (this.isSetTMID()) {
                tm = TrafficMatrixManager.getInstance().getTrafficMatrix(this.getASID(), this.getTMID());
            } else {
                tm = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(this.getASID());
            }

            if (this.isSetN()) {
                N = this.getN();
            } else {
                N = 3; // default value
            }

            if (this.isSetObjectiveFunction()) {
                OptDivideTMObjFun of = this.getObjectiveFunction();
                objFunction = of.getValue();
            } else {
                objFunction = "WMeanDelay";
            }

            if (this.isSetEstablishMultipleFullMesh()) {
                establishMultipleFullMesh = this.isEstablishMultipleFullMesh();
            } else {
                establishMultipleFullMesh = false;
            }

            if (this.isSetVerbose()) {
                verbose = this.isVerbose();
            } else {
                verbose = false;
            }

            OptDivideTMLoadComputer opt = new OptDivideTMLoadComputer(domain, tm);
            opt.setN(N);
            opt.setObjectiveFunction(objFunction);
            opt.setVerbose(verbose);

            String id;
            try {
                if (isSetLlcId()) {
                    LinkLoadComputerManager.getInstance().addLinkLoadComputer(opt, true, getLlcId());
                    id = getLlcId();
                } else {
                    id = LinkLoadComputerManager.getInstance().addLinkLoadComputer(opt);
                }
            } catch (LinkLoadComputerIdException e) {
                throw new EventExecutionException(e);
            } catch (LinkLoadComputerAlreadyExistsException e) {
                throw new EventExecutionException(e);
            }

            if (establishMultipleFullMesh)
                opt.establishFullmeshes();
            else opt.recompute();

            double score = opt.computeOptimumApproximation();

            EventResult er = new EventResult();
            er.setMessage("Score: " + String.valueOf(score));
            return er;

        } catch  (InvalidDomainException e) {
            e.printStackTrace();
            throw new EventExecutionException(e);
        } catch  (InvalidTrafficMatrixException e) {
            e.printStackTrace();
            throw new EventExecutionException(e);
        } catch  (Exception e) {
            e.printStackTrace();
            throw new EventExecutionException(e);
        }
    }
}

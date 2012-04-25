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
package be.ac.ulg.montefiore.run.totem.repository.optDivideTM;

import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.LspImpl;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPFTEMetric;
import be.ac.ulg.montefiore.run.totem.repository.model.AddLspAction;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemAction;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.TotemActionExecutionException;
import be.ac.ulg.montefiore.run.totem.repository.optDivideTM.MTRWO.*;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LoadData;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.*;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/*
* Changes:
* --------
* - 05-Feb-2007: a call to computeLoad does not erase TE metric anymore (GMO)
* - 26-Feb-2008: adpat to the new LinkLoadComputer interface (GMO)
*/

/**
 * This class implements a Traffic Engineering method which consist to divide the traffic matrix into
 * N sub-matrices (called strata). Each stratum is routed independently of each other. Concerning the
 * implementation of such solution, it is possible to establish multiple MPLS full-mesh (N) or to use
 * the Multi-Topology functionality (N multiple topologies).
 * <p/>
 * <p>Creation date: 18/01/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class OptDivideTMLoadComputer extends AbstractLinkLoadComputer {

    private final static Logger logger = Logger.getLogger(OptDivideTMLoadComputer.class);

    private TEObjectiveFunction objectiveFunction = null;
    private int N = 3;
    private boolean verbose = false;
    private boolean stopOnError = true;
    private boolean ECMP = true;

    private CSPFTEMetric spf;

    private SettableIPLoadData data;

    private boolean includeInterDomainLinks = false;
    private TrafficMatrix tm;

    public OptDivideTMLoadComputer(Domain domain, TrafficMatrix tm) {
        super(domain, null);
        changeListener = new LinkLoadComputerInvalidator(this) {
            public void linkTeMetricChangeEvent(Link link) {
                llc.invalidate();
            }
        };
        this.tm = tm;
        objectiveFunction = new WMeanDelayOF();
        spf = new CSPFTEMetric();
        data = new SettableIPLoadData(domain);
    }

    public boolean isECMP() {
        return ECMP;
    }

    public void setECMP(boolean ECMP) {
        this.ECMP = ECMP;
    }

    public void recompute() {
        if (data == null) {
            data = new SettableIPLoadData(domain);
        }
        data.clear();
        try {
            computeLoad(false);
        } catch (NoRouteToHostException e) {
            e.printStackTrace();
        } catch (RoutingException e) {
            e.printStackTrace();
        }
        dataChanged();
    }

    public void establishFullmeshes() throws NoRouteToHostException, RoutingException {
        computeLoad(true);
    }

    private void computeLoad(boolean establishMultipleFullmesh) throws NoRouteToHostException, RoutingException {

        LinkLoadStrategy lls = new SPFLinkLoadStrategy(domain, tm);
        lls.setSPFAlgo(spf);

        if (establishMultipleFullmesh) ECMP = false;
        lls.setECMP(ECMP);
        logger.debug("ECMP is activated ? " + lls.isECMP());

        lls.recompute();
        LoadData currentData = lls.getData();

        //save TE metric
        float metricBackup[] = new float[domain.getConvertor().getMaxLinkId()];
        for (Link l : domain.getAllLinks()) {
            try {
                metricBackup[domain.getConvertor().getLinkId(l.getId())] = l.getTEMetric();
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
                logger.fatal("Unexpected Error");
            }
        }

        try {

            //Set all the TE metrics to their initial values.
            for (Link currentLink : domain.getAllLinks()) {
                currentLink.setTEMetric(objectiveFunction.getFirstDerivate(currentLink.getBandwidth(), 0));
                if ((!includeInterDomainLinks) && (currentLink.getLinkType() == Link.Type.PEERING)) {
                    currentLink.setTEMetric(0);
                }
            }

            // Compute the initial loads and store these values.
            //double currentLoads[] = currentData.getLoad();

            //iterativeLoads = new double[domain.getNbLinks()];
            //linkCapacities = new double[domain.getNbLinks()];
            data.clear();

            /*
            if (currentLoads.length != domain.getNbLinks()) {
                throw new IllegalStateException("MTRWO : There are some removed links... This program could not work properly!");
            }*/

            long startTime = System.currentTimeMillis();

            double initialMeanUtilisation = 0;
            double initialMaxUtilisation = 0;
            for (Link l : domain.getAllLinks()) {
                data.addTraffic(l, currentData.getLoad(l) / N);
                double utilisation = currentData.getUtilization(l);
                initialMeanUtilisation += utilisation;
                if (utilisation > initialMaxUtilisation) {
                    initialMaxUtilisation = utilisation;
                }
            }
            initialMeanUtilisation /= domain.getNbLinks();

            //System.out.println("Initial values of : mean utilisation = " + initialMeanUtilisation + " and max utilisation = " + initialMaxUtilisation);
            //System.out.println("");

            // Print the initial value of the objective function
            //System.out.println("Initial Value of the Objective Function : " + teobjectiveFunction.getScore(linkCapacities, currentLoads));


            if (establishMultipleFullmesh) {
                List<Path> fullMesh = spf.computeFullMeshSPF(domain);
                for (Iterator<Path> iter = fullMesh.iterator(); iter.hasNext();) {
                    Path currentPath = iter.next();
                    try {
                        Lsp lsp = new LspImpl(domain, domain.generateLspId(), tm.get(currentPath.getSourceNode().getId(), currentPath.getDestinationNode().getId()) / N, currentPath);
                        TotemAction addLsp = new AddLspAction(domain, lsp);
                        addLsp.execute();
                    } catch (NodeNotFoundException e) {
                        if (stopOnError) throw new RoutingException(e);
                    } catch (TotemActionExecutionException e) {
                        if (stopOnError) throw new RoutingException(e);
                    }
                }
            }

            // Iterate
            int nbIterations = 1;
            while (nbIterations < N) {
                // Update the values of the IGP weights
                for (Iterator<Link> iter = domain.getAllLinks().iterator(); iter.hasNext();) {
                    Link currentLink = iter.next();
                    double load = data.getLoad(currentLink);
                    currentLink.setTEMetric(objectiveFunction.getFirstDerivate(currentLink.getBandwidth(), load));
                    if ((!includeInterDomainLinks) && (currentLink.getLinkType() == Link.Type.PEERING)) {
                        currentLink.setTEMetric(0);
                    }
                }

                /*System.out.println("Updated weight values :");
                for (int i = 0; i < domain.getNbLinks(); i++) {
                    Link lnk = domain.getAllLinks().get(i);
                    System.out.println(lnk.getTEMetric());
                }
                System.out.println("");*/

                // Compute the new load values, store these

                lls.recompute();
                for (Link link : domain.getAllLinks()) {
                    data.addTraffic(link, currentData.getLoad(link)/N);
                }

                // Print the current value of the objective function
                //System.out.println("Current Value of the Objective Function : " + teobjectiveFunction.getScore(linkCapacities, currentLoads));


                if (establishMultipleFullmesh) {
                    List<Path> fullMesh = spf.computeFullMeshSPF(domain);
                    for (Iterator<Path> iter = fullMesh.iterator(); iter.hasNext();) {
                        Path currentPath = iter.next();
                        try {
                            Lsp lsp = new LspImpl(domain, domain.generateLspId(), tm.get(currentPath.getSourceNode().getId(), currentPath.getDestinationNode().getId()) / N, currentPath);
                            TotemAction addLsp = new AddLspAction(domain, lsp);
                            addLsp.execute();
                        } catch (NodeNotFoundException e) {
                            if (stopOnError) throw new RoutingException(e);
                        } catch (TotemActionExecutionException e) {
                            if (stopOnError) throw new RoutingException(e);
                        }
                    }
                }
                nbIterations++;
            }


            long duration = System.currentTimeMillis() - startTime;
            if (verbose) {

                double linkCapacities[] = new double[domain.getConvertor().getMaxLinkId()];
                for (Link l : domain.getAllLinks()) {
                    try {
                        linkCapacities[domain.getConvertor().getLinkId(l.getId())] = l.getBandwidth();
                    } catch (LinkNotFoundException e) {
                        e.printStackTrace();
                    }
                }


                logger.info("Duration = " + duration + " millisec");
                logger.info("Final Value of the Objective Function : " + objectiveFunction.getScore(linkCapacities, data.getLoad()));

                double finalMeanUtilisation = 0;
                double finalMaxUtilisation = 0;
                for (Link link : domain.getAllLinks()) {
                    double utilisation = data.getUtilization(link);
                    finalMeanUtilisation += utilisation;
                    if (utilisation > finalMaxUtilisation) {
                        finalMaxUtilisation = utilisation;
                    }
                }

                finalMeanUtilisation /= domain.getNbLinks();
                logger.info("Final values of : mean utilisation = " + finalMeanUtilisation + " and max utilisation = " + finalMaxUtilisation);
            }

        } finally {
            lls.destroy();
            //restore TE metric
            for (Link l : domain.getAllLinks()) {
                try {
                    l.setTEMetric(metricBackup[domain.getConvertor().getLinkId(l.getId())]);
                } catch (LinkNotFoundException e) {
                    e.printStackTrace();
                    logger.fatal("Unexpected Error");
                }
            }
        }
        return;
    }

    public double computeOptimumApproximation() {
        double linkCapacities[] = new double[domain.getConvertor().getMaxLinkId()];
        for (Link l : domain.getAllLinks()) {
            try {
                linkCapacities[domain.getConvertor().getLinkId(l.getId())] = l.getBandwidth();
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }
        return objectiveFunction.getScore(linkCapacities, data.getLoad());
    }

    public void setObjectiveFunction(String objectiveFunction) throws ObjectiveFunctionException {
        if (objectiveFunction.equalsIgnoreCase("WMeanDelay")) {
            this.objectiveFunction = new WMeanDelayOF();
        } else if (objectiveFunction.equalsIgnoreCase("MeanDelay")) {
            this.objectiveFunction = new MeanDelayOF();
        } else if (objectiveFunction.equalsIgnoreCase("InvCap")) {
            this.objectiveFunction = new InvCapOF();
        } else if (objectiveFunction.equalsIgnoreCase("MinHop")) {
            this.objectiveFunction = new MinHopOF();
        } else if (objectiveFunction.equalsIgnoreCase("NLFortz")) {
            this.objectiveFunction = new NLFortzOF();
        } else {
            throw new ObjectiveFunctionException("Unknown objective function: " + objectiveFunction);
        }
    }

    public void setObjectiveFunction(TEObjectiveFunction objectiveFunction) {
        if (objectiveFunction == null) throw new IllegalArgumentException("Null argument.");
        this.objectiveFunction = objectiveFunction;
    }

    public void setN(int N) {
        if (N < 1) throw new IllegalArgumentException("N must be >= 1");
        this.N = N;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public List<TrafficMatrix> getTrafficMatrices() {
        List<TrafficMatrix> tms = new ArrayList<TrafficMatrix>(1);
        tms.add(tm);
        return tms;
    }

    public String getShortName() {
        return toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OptDivideTMLoadComputer)) return false;
        OptDivideTMLoadComputer str = (OptDivideTMLoadComputer) o;
        if (str.N != N) return false;
        if (!str.objectiveFunction.getName().equals(objectiveFunction.getName())) return false;
        if (str.ECMP != ECMP) return false;
        return true;
    }

    public int hashCode() {
        int result = (ECMP ? 1 : 0);
        result = 29 * N;
        result = 29 * result + objectiveFunction.getName().hashCode();
        return result;
    }

    public LoadData getData() {
        return data;
    }
 
    public LoadData detachData() {
        LoadData oldData = data;
        this.data = null;
        return oldData;
    }

    public String toString() {
        return getClass().getSimpleName() + " (N: " + N + ", obj: " + objectiveFunction.getName() + ", ECMP: " + ECMP + ")";
    }
}

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
package be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo;

import be.ac.ucl.poms.repository.model.IGPWeightOptimization;
import be.ac.ucl.ingi.cbgp.CBGP;
import be.ac.ucl.ingi.cbgp.CBGPException;
import be.ac.ucl.ingi.cbgp.bgp.Router;
import be.ac.ucl.ingi.cbgp.bgp.Peer;
import be.ac.ucl.ingi.cbgp.bgp.Route;
import be.ac.ucl.ingi.cbgp.net.IGPDomain;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemActionList;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SimulatedAnnealingConcepts.LinksWeightSolution;
import be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SimulatedAnnealingConcepts.LinkLoads;
import be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SimulatedAnnealingConcepts.NeighborFunction.BGPAwareNeighborFunction;
import be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SimulatedAnnealingConcepts.NeighborFunction.OneRandomWeightChange;
import be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SimulatedAnnealingConcepts.ObjectiveFunctions.BGPAwareObjectiveFunction;
import be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SimulatedAnnealingConcepts.ObjectiveFunctions.MaxUtil;
import be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SimulatedAnnealingConcepts.ObjectiveFunctions.Fortz;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.DomainImpl;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.TrafficMatrixImpl;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.SPFLinkLoadStrategy;

import java.util.*;
import java.io.*;

/**
 * <p>Creation date: 11 sept. 2007
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 *
 * This is the main class of the simulated annealing version of a generic BGP-aware version of
 * a link weight optimizer. This optimizer can optimize link weights considering the hot-potato
 * reroutings that occurs in the network, for any iBGP configuration, considering iBGP multipath
 * or not, and with the ability to "block" egress points.
 *
 */

public class SaBgpAwareLwo implements IGPWeightOptimization {

    private static final ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();
    private static HashMap runningParams = null;

//    private String iBGPconfig = "full-mesh"; // "full-mesh" -> iBGP full-mesh; "1.0.0.1" -> 1.0.0.1 is the Route Reflector
    private String iBGPconfig = "62.40.102.11"; // "full-mesh" -> iBGP full-mesh; "1.0.0.1" -> 1.0.0.1 is the Route Reflector
//    private String iBGPconfig = "1.0.0.1"; // "full-mesh" -> iBGP full-mesh; "1.0.0.1" -> 1.0.0.1 is the Route Reflector
    private boolean iBGPMultipath = true;
    public boolean includeInterDomainLinksInObjFunction = false;
    public boolean avoidHPReroutings = false;
//    private BGPAwareObjectiveFunction objectiveFunction = new Delay();
    private BGPAwareObjectiveFunction objectiveFunction = new Fortz();
    private BGPAwareNeighborFunction neighborFunction = new OneRandomWeightChange();
    public static int maxLinkWeight = 150;

    public boolean considerDeflection = true; // considering deflection is only usefull when RR are used. It is available in this software only when iBGP mulitpath is used

    // Simulated Annealing parameters
//    private double initialTemperature = 0.0004;  // 0.0004 good for delay and geant
    private double initialTemperature = 100000;  // 100000 good for fortz and geant
    private int plateauSize = 50;
    private float coolingFactor = 0.8f;
//    private int epsilon = 2;  // RR
    private int epsilon = 10; // FM
    private int k = 3;

    private String logFileName = "/home/balon/bgpAwareLog.out";
    private Map<String,String> rid2NodeId;

    private int nbIterationsEgressChanges = 0;
    private int totalNbEgressChanges = 0;
    private boolean firstEgressComputation = true;
    private Map<String, String> precedentEgress = new Hashtable<String, String>();

    static {
    	try {
    		params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
    	} catch (AlgorithmParameterException e) {
    		e.printStackTrace();
    	}
    }

    /**
     *
     */
    public TotemActionList calculateWeights(int ASID, int[] TMID) throws Exception {
        System.out.println("first line of calculate Weigths...");
        FileWriter logFile = new FileWriter(logFileName);

        long startTime = System.currentTimeMillis();

        logFile.write("Running the weight optimizer with the following parameters :\n");
        if (iBGPconfig.compareTo("full-mesh") == 0) {
            logFile.write("iBGP configuration is a full-mesh of iBGP sessions.\n");
        } else {
            logFile.write("iBGP configuration is : " + iBGPconfig + " is the only route reflector of the network.\n");
        }
        if (includeInterDomainLinksInObjFunction) {
            logFile.write("The interdomain links are considered in the objective function.\n");
        } else {
            logFile.write("The interdomain links are NOT considered in the objective function.\n");
        }
        if (iBGPMultipath) {
            logFile.write("iBGP Multipath load sharing is supposed to be activated in all the BGP routers.\n");
        } else {
            logFile.write("iBGP Multipath load sharing is supposed to be NOT activated in all the BGP routers.\n");
        }
        if (avoidHPReroutings) {
            logFile.write("The objective function includes a penalty for HP reroutings.\n");
        } else {
            logFile.write("The objective function does NOT include a penalty for HP reroutings.\n");
        }
        logFile.write("The objective function is " + objectiveFunction.getName() + "\n");
        logFile.write("The neighbor function is " + neighborFunction.getName() + "\n");
        logFile.write("The maximal link weight is " + maxLinkWeight + "\n");
        logFile.write("Simulated Annealing parameters : T0 = " + initialTemperature + ", plateau size = " + plateauSize + ", alpha = " + coolingFactor + ", eps = " + epsilon + ", K = " + k + "\n\n");

        if (TMID.length != 1) {
            throw new Exception("SaBGpAwareLwo works with one AS and one and only one TM !");
        }

        Domain augmentedDomain = InterDomainManager.getInstance().getDomain(ASID);
        rid2NodeId = new HashMap<String,String>();
        for (Node n : augmentedDomain.getUpNodes()) {
            rid2NodeId.put(n.getRid(),n.getId());
        }
        TrafficMatrix aggregatedInterDomainTrafficMatrix = TrafficMatrixManager.getInstance().getTrafficMatrix(ASID, TMID[0]);

        Domain intraDomain = createIntraDomain(augmentedDomain);
        InterDomainManager.getInstance().addDomain(intraDomain);

        TrafficMatrix intraTrafficMatrix = new TrafficMatrixImpl(augmentedDomain.getASID());

        LinksWeightSolution initialLinkWeights = new LinksWeightSolution(intraDomain);
        LinksWeightSolution currentLinkWeights = new LinksWeightSolution(intraDomain);
        LinksWeightSolution currentLinkWeightsPrime = new LinksWeightSolution(intraDomain);

        System.out.println("just before compute intadomain traffic matrix");
        computeIntraDomainTrafficMatrix(intraDomain, aggregatedInterDomainTrafficMatrix, augmentedDomain, intraTrafficMatrix);
        System.out.println("just after compute intadomain traffic matrix");

        LinkLoads linkLoads = new LinkLoads(augmentedDomain);
        computeLinkLoads(augmentedDomain, intraTrafficMatrix, linkLoads);

        double initialOFValue = objectiveFunction.getValue(linkLoads, intraDomain, includeInterDomainLinksInObjFunction, avoidHPReroutings);
        double currentOFValue = initialOFValue;
        double currentOFValuePrime = initialOFValue;
        double bestOFValue = initialOFValue;

        BGPAwareObjectiveFunction umaxOF = new MaxUtil();
        double umaxValueForBestSolution = umaxOF.getValue(linkLoads, intraDomain, includeInterDomainLinksInObjFunction, false);

        logFile.write("Initial value of OF = " + initialOFValue + ", umax = " + umaxValueForBestSolution + "\n\n");

        logFile.write(initialLinkWeights.toString() + "\n");

        System.out.println(currentLinkWeightsPrime.toString() );
        for (Node n1 : intraDomain.getUpNodes()) {
            if (n1.getNodeType() == Node.Type.CORE) {
                for (Node n2 : intraDomain.getUpNodes()) {
                    logFile.write("traffic from node " + n1.getId() + " to node " + n2.getId() + " is equal to " + intraTrafficMatrix.get(n1.getId(), n2.getId()) + "\n");
//                    System.out.println("traffic from node " + n1.getId() + " to node " + n2.getId() + " is equal to " + intraTrafficMatrix.get(n1.getId(), n2.getId()));
                }
            }
        }
        logFile.write("\n");
        for (Link l : intraDomain.getUpLinks()) {
            logFile.write("link " + l.getId() + " of type " + l.getLinkType().toString() + " has a load of " + linkLoads.getLoad(l.getId()) + " (capa = " + l.getBandwidth() + ")\n");
        }
        logFile.write("\n");

        double temperature = initialTemperature;

        neighborFunction.init(intraDomain);

        boolean stoppingConditions = false;
        int nbIterations = 0;
        int nbIterationsInCurrentPlateau = 0;
        boolean bestSolutionHasImproved = false;
        int nbAcceptedMoves[] = new int[k];
        for (int i=0; i < k; i++) {
            nbAcceptedMoves[i] = plateauSize;
        }
        nbAcceptedMoves[0] = 0;
        int nbAcceptedMoves2 = 0;
        int currentKIndex = 0;
        while (!stoppingConditions) {
            nbIterations++;
            nbIterationsInCurrentPlateau++;
            System.out.println("Iteration " + nbIterations);

            // propose a move
            neighborFunction.proposeMove(currentLinkWeights, currentLinkWeightsPrime);
            setLinkWeights(intraDomain, currentLinkWeightsPrime);

            // evaluate the score of the new solution
            computeIntraDomainTrafficMatrix(intraDomain, aggregatedInterDomainTrafficMatrix, augmentedDomain, intraTrafficMatrix);
            computeLinkLoads(augmentedDomain, intraTrafficMatrix, linkLoads);
            currentOFValuePrime = objectiveFunction.getValue(linkLoads, intraDomain, includeInterDomainLinksInObjFunction, avoidHPReroutings);

            // evaluate the difference of score function
            double differenceOFValues = currentOFValuePrime - currentOFValue;

            // decide to accept or not the move
            boolean accept = false;
            if (differenceOFValues <= 0) {
                accept = true;
                if (differenceOFValues != 0) {
                    System.out.println("decreasing move accepted");
                } else {
                    System.out.println("stable move accepted");
                }
                // Save the best solution value if it is the best so far
                if (currentOFValuePrime < bestOFValue) {
                    bestOFValue = currentOFValuePrime;
                    umaxValueForBestSolution = umaxOF.getValue(linkLoads, intraDomain, includeInterDomainLinksInObjFunction, false);
                    bestSolutionHasImproved = true;
                    logFile.write("\n");
                    for (Link l : intraDomain.getUpLinks()) {
                        logFile.write("link " + l.getId() + " of type " + l.getLinkType().toString() + " has a load of " + linkLoads.getLoad(l.getId()) + " (capa = " + l.getBandwidth() + ")\n");
                    }
                    logFile.write("\n");
                }
            } else {
                double pK = Math.exp(- (differenceOFValues / temperature));
                if (Math.random() < pK) {
                    // Accept the move
                    accept = true;
                    System.out.println("increasing move accepted");
                } else {
                    System.out.println("increasing move NOT accepted");
                }
            }

            // If the move is not accepted, update currentLinkWeights
            if (accept) {
                copyLinkWeights(currentLinkWeightsPrime, currentLinkWeights);
                currentOFValue = currentOFValuePrime;
                if (differenceOFValues != 0) {
                    nbAcceptedMoves[currentKIndex]++;
                }
                nbAcceptedMoves2++;
            }

            // log some information
            logFile.write("Iteration number " + nbIterations + "\n");
            logFile.write("Value of OF = " + currentOFValuePrime + ", move accepted ? = " + accept + "\n");
            logFile.write("Best OF value = " + bestOFValue + "\n");
            logFile.write("Corresponding umax value = " + umaxValueForBestSolution + "\n\n");

//            logFile.write(currentLinkWeightsPrime.toString() + "\n\n");

/*            System.out.println(currentLinkWeightsPrime.toString());
            for (Node n1 : intraDomain.getUpNodes()) {
                if (n1.getNodeType() == Node.Type.CORE) {
                    for (Node n2 : intraDomain.getUpNodes()) {
                        logFile.write("traffic from node " + n1.getId() + " to node " + n2.getId() + " is equal to " + intraTrafficMatrix.get(n1.getId(), n2.getId()) + "\n");
//                        System.out.println("traffic from node " + n1.getId() + " to node " + n2.getId() + " is equal to " + intraTrafficMatrix.get(n1.getId(), n2.getId()));
                    }
                }
            }
            logFile.write("\n");
            for (Link l : intraDomain.getUpLinks()) {
                logFile.write("link " + l.getId() + " of type " + l.getLinkType().toString() + " has a utilization of " + linkLoads.getLoad(l.getId()) + "\n");
            }
            logFile.write("\n");*/


            // evaluate the stopping conditions
            if (nbIterationsInCurrentPlateau == plateauSize) {
                System.out.println("Evaluating the stopping conditions");

                if (temperature == initialTemperature) {
                    // The first plateau just has ended
                    float percentageOfAcceptedMoves = (nbAcceptedMoves2 * 100.f) / plateauSize;
                    if ((percentageOfAcceptedMoves < 90.f) && (percentageOfAcceptedMoves > 50.f)) {
  //                      System.out.println("OK the percentage of accepted moves during the first plateau is " + percentageOfAcceptedMoves);
                        logFile.write("OK the percentage of accepted moves during the first plateau is " + percentageOfAcceptedMoves + "\n");
                    } else {
//                        System.out.println("Pay attention please: the percentage of accepted moves during the first plateau is " + percentageOfAcceptedMoves + " while it should be between 50 and 90.");
                        logFile.write("Pay attention please: the percentage of accepted moves during the first plateau is " + percentageOfAcceptedMoves + " while it should be between 50 and 90.\n");
                    }
                }

                nbIterationsInCurrentPlateau = 0;
                temperature *= coolingFactor;
                int totalNbAcceptedMoves = 0;
                for (int i=0; i < k; i++) {
//                    System.out.println("accepted moves [" + i + "] = " + nbAcceptedMoves[i] + " (total = " + totalNbAcceptedMoves + ")");
                    totalNbAcceptedMoves += nbAcceptedMoves[i];
                }
                currentKIndex++;
                if (currentKIndex == k) {
                    currentKIndex = 0;
                }
                nbAcceptedMoves[currentKIndex] = 0;
                float treshold = ((epsilon / 100.f) * plateauSize * k);
                System.out.println("Nb Accepted moves during the last " + k + " plateaux = " + totalNbAcceptedMoves + " (threshold = " + treshold + ")");
                if ((bestSolutionHasImproved == false) && (totalNbAcceptedMoves < treshold)) {
                    stoppingConditions = true;
                }
                bestSolutionHasImproved = false;
            }
        }

        setLinkWeights(intraDomain, initialLinkWeights);

        long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
        logFile.write("\nSimulation Time = " + elapsedTime + " sec\n");

/*        logFile.write("The number of iterations in which egress points have changed is " + nbIterationsEgressChanges + " out of " + nbIterations + " iterations\n");
        float percentageOfIterationsEgressChanges = nbIterationsEgressChanges;
        percentageOfIterationsEgressChanges /= nbIterations;
        logFile.write("which is " + percentageOfIterationsEgressChanges + " percents of the iterations.");
        float meanNbEgressChanges = totalNbEgressChanges;
        meanNbEgressChanges /= nbIterationsEgressChanges;
        logFile.write("The mean number of flows affected by egress change when there is at least one is " + meanNbEgressChanges + "\n");
*/
        logFile.close();

        // Restore the initial link metrics...
        TotemActionList actionList = new TotemActionList();

        setLinkWeights(intraDomain, initialLinkWeights);

        return actionList;
    }

    private void copyLinkWeights(LinksWeightSolution linkWeights, LinksWeightSolution intoLinkWeights) {
        for (String linkId : linkWeights.linkWeights.keySet()) {
            intoLinkWeights.setMetric(linkId, linkWeights.getMetric(linkId));
        }
    }

    private void setLinkWeights(Domain intraDomain, LinksWeightSolution linkWeights) {
        for (Link l : intraDomain.getUpLinks()) {
            if (l.getLinkType() == Link.Type.INTRA) {
                l.setMetric(linkWeights.getMetric(l.getId()));
            }
        }
    }

    private Domain createIntraDomain(Domain augmentedDomain) throws NodeAlreadyExistException {
        Domain newDomain = new DomainImpl(augmentedDomain.getASID() + 1);

        for (Node n : augmentedDomain.getUpNodes()) {
            if (n.getNodeType() != Node.Type.VIRTUAL) {
                try {
                    newDomain.addNode(n);
                } catch (NodeAlreadyExistException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Link l : augmentedDomain.getUpLinks()) {
            if (l.getLinkType() != Link.Type.VIRTUAL) {
                try {
                    newDomain.addLink(l);
                } catch (LinkAlreadyExistException e) {
                    e.printStackTrace();
                } catch (NodeNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return newDomain;
    }

    private void computeLinkWeights(Domain domain, TrafficMatrix trafficMatrix) {



    }

    /**
     * Please note that links must have the same link metric in both directions...
     * Tags CORE, NEIGH, VIRTUAL, INTRA and INTER must be correctly placed on nodes and links
     * All the nodes (CORE, NEIGH and VIRTUAL) must have a RID which is an IP address...
     *
     * @param aggregatedInterDomainTrafficMatrix
     * @param augmentedDomain
     * @param intraTrafficMatrix
     * @throws CBGPException
     * @throws NodeNotFoundException
     */
    private void computeIntraDomainTrafficMatrix(Domain intraDomain, TrafficMatrix aggregatedInterDomainTrafficMatrix, Domain augmentedDomain, TrafficMatrix intraTrafficMatrix) throws CBGPException, NodeNotFoundException, NoRouteToHostException, Exception {

        boolean debug = false;

        System.out.println("Generating the intradomain traffic matrix using C-BGP");

        CBGP cbgp = new CBGP();
        cbgp.init("/tmp/cbgp_jni.log");

        boolean thisIterationHasChanged = false;

        Map<String, Link> addedLinks = new HashMap<String, Link>();

        Hashtable<String, String> virtualAndCoreNodeToNeighNode = new Hashtable<String, String>();

        Hashtable<String, be.ac.ucl.ingi.cbgp.net.Node> cbgpNodesById = new Hashtable<String, be.ac.ucl.ingi.cbgp.net.Node>();

        IGPDomain intraIgpDomain = cbgp.netAddDomain(1);
        IGPDomain interIgpDomain = cbgp.netAddDomain(2);

        if (debug)
            System.out.println("adding intraDomain nodes");
        // Adding intradomain nodes
        for (Node n : augmentedDomain.getUpNodes()) {
            if (n.getNodeType() == Node.Type.CORE) {
                if (debug)
                    System.out.println("node : " + n.getRid());
                be.ac.ucl.ingi.cbgp.net.Node currentNode = intraIgpDomain.addNode(n.getRid());
                cbgpNodesById.put(n.getRid(), currentNode);
            }
        }

        if (debug)
            System.out.println("adding Neighboring nodes");
        // Adding neighboring nodes
        for (Node n : augmentedDomain.getUpNodes()) {
            if (n.getNodeType() == Node.Type.NEIGH) {
                if (debug)
                    System.out.println("node : " + n.getRid());
                be.ac.ucl.ingi.cbgp.net.Node currentNode = interIgpDomain.addNode(n.getRid());
                cbgpNodesById.put(n.getRid(), currentNode);
            }
        }


        // Adding the intradomain links
        if (debug)
            System.out.println("Adding intradomain links");
        for (Link l : augmentedDomain.getUpLinks()) {
            if (l.getLinkType() == Link.Type.INTRA) {
                if (addedLinks.containsKey(l.getDstNode().getRid() + "+" + l.getSrcNode().getRid()) != true) {
                    if (debug)
                        System.out.println("adding in cbgp link from " + l.getSrcNode().getRid() + " to " + l.getDstNode().getRid());
                    cbgp.netAddLink(l.getSrcNode().getRid(), l.getDstNode().getRid(), 0);
                    addedLinks.put(l.getSrcNode().getRid() + "+" + l.getDstNode().getRid(), l);
                }
            }
        }

        // Filling the virtualAndCoreNodeToNeighNode table
        for (Link l : augmentedDomain.getUpLinks()) {
            if (l.getLinkType() == Link.Type.VIRTUAL) {
                String coreNode;
                String neighNode = l.getSrcNode().getId();
                String virtualNode = l.getDstNode().getId();

                List<Link> inLinks = l.getSrcNode().getInLink();
                if (inLinks.size() != 1) {
                    throw new RoutingException("A neighboring node cannot have multiple incoming links and it is the case for " + neighNode);
                } else {
                    coreNode = inLinks.get(0).getSrcNode().getId();
                }

                if (debug)
                    System.out.println("adding in the table virtual, core, neigh := " + virtualNode + " " + coreNode + " " + neighNode);
                virtualAndCoreNodeToNeighNode.put(virtualNode + "+" + coreNode, neighNode);
                virtualAndCoreNodeToNeighNode.put(virtualNode + "+" + neighNode, neighNode);
            }
        }

        // Setting the link metrics
        Hashtable<String, be.ac.ucl.ingi.cbgp.net.Link> customIdToCbgpLinks = new Hashtable<String, be.ac.ucl.ingi.cbgp.net.Link>();
        Vector<be.ac.ucl.ingi.cbgp.net.Node> nodes = intraIgpDomain.getNodes();
        for (be.ac.ucl.ingi.cbgp.net.Node currentNode : nodes) {
            Vector<be.ac.ucl.ingi.cbgp.net.Link> links = currentNode.getLinks();
            for (be.ac.ucl.ingi.cbgp.net.Link currentLink : links) {
                customIdToCbgpLinks.put(currentNode.getAddress().toString() + "+" + currentLink.getNexthopIf().toString(), currentLink);
            }
        }
        for (Link l : augmentedDomain.getUpLinks()) {
            if (l.getLinkType() == Link.Type.INTRA) {
                int metric = (new Float(l.getMetric())).intValue();
                customIdToCbgpLinks.get(l.getSrcNode().getRid() + "+" + l.getDstNode().getRid()).setWeight(metric);
            }
        }

        // Adding the interdomain links
        if (debug)
            System.out.println("Adding interdomain links");
        for (Link l : augmentedDomain.getUpLinks()) {
            if (l.getLinkType() == Link.Type.INTER) {
                if (debug)
                    System.out.println("adding in cbgp link from " + l.getSrcNode().getRid() + " to " + l.getDstNode().getRid());
                be.ac.ucl.ingi.cbgp.net.Link cbgpLink = cbgp.netAddLink(l.getSrcNode().getRid(), l.getDstNode().getRid(), 0);
                cbgpLink.setWeight(0);
            }
        }

        // Creating the static routes for interdomain links
        if (debug)
            System.out.println("Adding static routes...");
        for (Link l : augmentedDomain.getUpLinks()) {
            if (l.getLinkType() == Link.Type.INTER) {
                if (debug)
                    System.out.println("From " + l.getSrcNode().getRid() + " to " + l.getDstNode().getRid() + " and vice versa");
                cbgpNodesById.get(l.getSrcNode().getRid()).addRoute(l.getDstNode().getRid() + "/32", l.getDstNode().getRid(), 0);
                //cbgp.netNodeRouteAdd(l.getSrcNode().getRid(), l.getDstNode().getRid() + "/32", l.getDstNode().getRid(), 0);
                cbgpNodesById.get(l.getDstNode().getRid()).addRoute(l.getSrcNode().getRid() + "/32", l.getSrcNode().getRid(), 0);
                //cbgp.netNodeRouteAdd(l.getDstNode().getRid(), l.getSrcNode().getRid() + "/32", l.getSrcNode().getRid(), 0);
            }
        }

        // Computing all the routes inside the domain
        if (debug)
            System.out.println("Compute all the routes inside the domain");
        intraIgpDomain.compute();
        // TODO Verify that neighboring nodes are reachable from all the intradomain nodes

        Hashtable<String, Router> ridToCbgpRouter = new Hashtable<String, Router>();
        // Setting all the BGP routers
        if (debug)
            System.out.println("setting up all the BGP routers.");
        for (Node n : augmentedDomain.getUpNodes()) {
            if (n.getNodeType() == Node.Type.CORE) {
                if (debug)
                    System.out.println("Creating the BGP node " + n.getRid() + " (domain 1) and adding it in the table");
                Router currentRouter = cbgp.bgpAddRouter(n.getRid(), n.getRid(), 1);
                ridToCbgpRouter.put(n.getRid(), currentRouter);
            }
        }
        for (Node n : augmentedDomain.getUpNodes()) {
            if (n.getNodeType() == Node.Type.NEIGH) {
                if (debug)
                    System.out.println("Creating the BGP node " + n.getRid() + " (domain 2) and adding it in the table");
                Router currentRouter = cbgp.bgpAddRouter(n.getRid(), n.getRid(), 2);
                ridToCbgpRouter.put(n.getRid(), currentRouter);
            }
        }

        // Setting all the BGP sessions
        // Intradomain iBGP sessions : a full-mesh or one route-reflector for all
        if (debug)
            System.out.println("Setting up the iBGP topology");
        if (iBGPconfig.compareTo("full-mesh") == 0) {
            for (Node n1 : augmentedDomain.getUpNodes()) {
                if (n1.getNodeType() == Node.Type.CORE) {
                    for (Node n2 : augmentedDomain.getUpNodes()) {
                        if ((n1.getRid().compareTo(n2.getRid()) != 0) && (n2.getNodeType() == Node.Type.CORE)) {
                            if (debug)
                                System.out.println("adding an iBGP session from router " + n1.getRid() + " to router " + n2.getRid());
                            Router currentRouter = ridToCbgpRouter.get(n1.getRid());
                            Peer newPeer = currentRouter.addPeer(n2.getRid(), 1);
                            newPeer.openSession();

//                            cbgp.bgpRouterAddPeer(n1.getRid(), n2.getRid(), 1);
//                            cbgp.bgpRouterPeerUp(n1.getRid(), n2.getRid(), true);
                        }
                    }
                }
            }
        } else {
            String routeReflectorIp = iBGPconfig;
            for (Node n1 : augmentedDomain.getUpNodes()) {
                if ((n1.getNodeType() == Node.Type.CORE) && (n1.getRid().compareTo(routeReflectorIp) != 0)) {
//                    System.out.println("Adding an iBGP session between RR " + routeReflectorIp + " and client " + n1.getRid());
                    Router currentRouter = ridToCbgpRouter.get(routeReflectorIp);
                    if (currentRouter == null) {
                        System.out.println("The route reflector (IP = " + routeReflectorIp + ") router has not been found.");
                        System.exit(-1);
                    }
                    if (debug)
                        System.out.println("Adding an iBGP session from RR " + routeReflectorIp + " to " + n1.getRid());
                    Peer newPeer = currentRouter.addPeer(n1.getRid(), 1);
                    newPeer.setReflectorClient();
                    newPeer.openSession();

//                    cbgp.bgpRouterAddPeer(routeReflectorIp, n1.getRid(), 1);
//                    cbgp.bgpRouterPeerReflectorClient(routeReflectorIp, n1.getRid());
//                    cbgp.bgpRouterPeerUp(routeReflectorIp, n1.getRid(), true);
                }
            }
            for (Node n1 : augmentedDomain.getUpNodes()) {
                if ((n1.getNodeType() == Node.Type.CORE) && (n1.getRid().compareTo(routeReflectorIp) != 0)) {
                    if (debug)
                        System.out.println("Adding an iBGP session between " + n1.getRid() + " and " + routeReflectorIp + " (RR)");
                    Router currentRouter = ridToCbgpRouter.get(n1.getRid());
                    Peer newPeer = currentRouter.addPeer(routeReflectorIp, 1);
                    newPeer.openSession();

//                    cbgp.bgpRouterAddPeer(n1.getRid(), routeReflectorIp, 1);
//                    cbgp.bgpRouterPeerUp(n1.getRid(), routeReflectorIp, true);
                }
            }
        }
        // Interdomain eBGP sessions
        for (Link l : augmentedDomain.getUpLinks()) {
            if (l.getLinkType() == Link.Type.INTER) {
                if (debug)
                    System.out.println("Adding two eBGP sessions (nexthopself) between " + l.getSrcNode().getRid() + " and " + l.getDstNode().getRid());

                Router currentRouter = ridToCbgpRouter.get(l.getSrcNode().getRid());
                Peer newPeer = currentRouter.addPeer(l.getDstNode().getRid(), 2);
                newPeer.setNextHopSelf(true);
                newPeer.openSession();

                currentRouter = ridToCbgpRouter.get(l.getDstNode().getRid());
                newPeer = currentRouter.addPeer(l.getSrcNode().getRid(), 1);
                newPeer.setNextHopSelf(true);
                newPeer.openSession();

//                cbgp.bgpRouterAddPeer(l.getSrcNode().getRid(), l.getDstNode().getRid(), 2);
//                cbgp.bgpRouterPeerNextHopSelf(l.getSrcNode().getRid(), l.getDstNode().getRid());
//                cbgp.bgpRouterPeerUp(l.getSrcNode().getRid(), l.getDstNode().getRid(), true);
//                cbgp.bgpRouterAddPeer(l.getDstNode().getRid(), l.getSrcNode().getRid(), 1);
//                cbgp.bgpRouterPeerNextHopSelf(l.getDstNode().getRid(), l.getSrcNode().getRid());
//                cbgp.bgpRouterPeerUp(l.getDstNode().getRid(), l.getSrcNode().getRid(), true);
            }
        }
        // Adding the network prefixes...
        for (Link l : augmentedDomain.getUpLinks()) {
            if (l.getLinkType() == Link.Type.VIRTUAL) {
                if (debug)
                    System.out.println("Adding network " + l.getDstNode().getRid() + "/32 to node " + l.getSrcNode().getRid());
                Router currentRouter = ridToCbgpRouter.get(l.getSrcNode().getRid());
                currentRouter.addNetwork(l.getDstNode().getRid() + "/32");
//                cbgp.bgpRouterAddNetwork(l.getSrcNode().getRid(), l.getDstNode().getRid() + "/32");
            }
        }

        if (debug)
            System.out.println("Running the simulator");
        cbgp.simRun();

        // Copy values for intra and neigh nodes.
        for (Node n1 : augmentedDomain.getUpNodes()) {
            if (n1.getNodeType() == Node.Type.CORE) {
                for (Node n2 : augmentedDomain.getUpNodes()) {
                    if (n2.getNodeType() == Node.Type.CORE) {
                        intraTrafficMatrix.set(n1.getId(), n2.getId(), aggregatedInterDomainTrafficMatrix.get(n1.getId(), n2.getId()));
                    } else if (n2.getNodeType() == Node.Type.NEIGH) {
                        intraTrafficMatrix.set(n1.getId(), n2.getId(), aggregatedInterDomainTrafficMatrix.get(n1.getId(), n2.getId()));
                    }
                }
            }
        }

  /*      System.out.println("");
        for (Node n1 : augmentedDomain.getUpNodes()) {
            if (n1.getNodeType() == Node.Type.CORE) {
                for (Node n2 : augmentedDomain.getUpNodes()) {
                    if (n2.getNodeType() == Node.Type.VIRTUAL) {
                        System.out.println("Printing the Adj RIB-out of " + n1.getRid() + " toward virtual prefix " +  n2.getRid() + "/32");
                        for (Node n3 : augmentedDomain.getUpNodes()) {
                            if ((n3.getNodeType() == Node.Type.CORE) || (n3.getNodeType() == Node.Type.NEIGH)) {
                                Router currentRouter = ridToCbgpRouter.get(n1.getRid());
                                Vector<Route> routes = currentRouter.getAdjRIB(n3.getRid(), n2.getRid() + "/32", false);
                                if (routes != null) {
                                    for (Route currentRoute : routes) {
                                        System.out.println("Route to next hop " + currentRoute.getNexthop() + " is best route ? = " + currentRoute.isBest());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println("");
        for (Node n1 : augmentedDomain.getUpNodes()) {
            if (n1.getNodeType() == Node.Type.CORE) {
                System.out.println("Printing all the peers of " + n1.getRid());
                Router currentRouter = ridToCbgpRouter.get(n1.getRid());
                Vector<Peer> peers = currentRouter.getPeers();
                for (Peer peer : peers) {
                    System.out.println("peer : " + peer.getAddress() + " in AS " + peer.getAS() + " is RR client ? = " + peer.isReflectorClient() + " session state = " + Peer.sessionStateToString(peer.getSessionState()));
                }
            }
        }                      */

        System.out.println("");
        // Assign traffic of virtual nodes to neigh nodes.
        for (Node n1 : augmentedDomain.getUpNodes()) {
            if (n1.getNodeType() == Node.Type.CORE) {
                for (Node n2 : augmentedDomain.getUpNodes()) {
                    if (n2.getNodeType() == Node.Type.VIRTUAL) {
                        if (this.iBGPMultipath) {
                            if (debug)
                                System.out.println("Printing the Adj RIB-in of " + n1.getRid() + " toward virtual prefix " +  n2.getRid() + "/32");

                            List<String> availableNextHops = new ArrayList<String>();
                            String bestRouteNextHop = "";
                            Router currentRouter = ridToCbgpRouter.get(n1.getRid());

                            // Create the table of available next hops.
                            for (Peer currentPeer : currentRouter.getPeers()) {
                                Vector<Route> routes = currentRouter.getAdjRIB(currentPeer.getAddress().toString(), n2.getRid() + "/32", true);
                                if (routes != null) {
                                    for (Route currentRoute : routes) {
                                        if (debug)
                                            System.out.println("Route to next hop " + currentRoute.getNexthop() + " is best route ? = " + currentRoute.isBest());
                                        availableNextHops.add(currentRoute.getNexthop().toString());
                                        if (currentRoute.isBest()) {
                                            bestRouteNextHop = currentRoute.getNexthop().toString();
                                        }
                                    }
                                }
                            }

                            CSPF cspf = new CSPF();
                            if (bestRouteNextHop.compareTo("") != 0) {
                                float shortestDistance = cspf.computeSPF(augmentedDomain, n1.getId(), rid2NodeId.get(bestRouteNextHop)).getSumLinkMetrics();
                                List<String> availableShortestNextHopsNodeIds = new ArrayList<String>();
                                int nbShortestPaths = 0;
                                for (String currentNextHop : availableNextHops) {
                                    Path currentShortestPath = cspf.computeSPF(augmentedDomain, n1.getId(), rid2NodeId.get(currentNextHop));
                                    float currentDistance = currentShortestPath.getSumLinkMetrics();
                                    if (debug)
                                        System.out.println("distance from " + n1.getId() + " to " + rid2NodeId.get(currentNextHop) + " is equal to " + currentDistance);
                                    if (currentDistance <= shortestDistance) {
                                        if (debug)
                                            System.out.println("And this is a shortest path !");
                                        // current Next Hop will be used except if path deflection occurs... -> verify that path deflection does not occur
                                        boolean deflection = false;
                                        String deflectedNextHop = "";
                                        // For each node of the shortest path to this next hop
                                        if (considerDeflection) {
                                            if (debug)
                                                System.out.println("Verifying deflection");
                                            for (Node currentNode : currentShortestPath.getNodePath()) {
                                                // Verify that the best route of this node has the same next hop
                                                String currentRid = currentNode.getRid();
                                                if (debug)
                                                    System.out.println("Node on the shortest path : " + currentRid);
                                                if (!deflection) {
                                                    if ((n1.getRid().compareTo(currentRid) != 0) && (currentNextHop.compareTo(currentRid) != 0)) {
                                                        String bestRouteNextHopForCurrentRid = "";
                                                        Router currentRouterForCurrentRid = ridToCbgpRouter.get(currentRid);

                                                        // Create the table of available next hops.
                                                        for (Peer currentPeer : currentRouterForCurrentRid.getPeers()) {
    //                                                        System.out.println("analysing peer " + currentPeer.getRouterID().toString());
                                                            Vector<Route> routes = currentRouterForCurrentRid.getAdjRIB(currentPeer.getAddress().toString(), n2.getRid() + "/32", true);
                                                            if (routes != null) {
                                                                for (Route currentRoute : routes) {
                                                                    if (debug)
                                                                        System.out.println("Route to next hop " + currentRoute.getNexthop() + " is best route ? = " + currentRoute.isBest());
                                                                    if (currentRoute.isBest()) {
                                                                        bestRouteNextHopForCurrentRid = currentRoute.getNexthop().toString();
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        if (bestRouteNextHopForCurrentRid.compareTo(currentNextHop) != 0) {
                                                            deflection = true;
                                                            deflectedNextHop = bestRouteNextHopForCurrentRid;
                                                            if (debug)
                                                                System.out.println("Deflection occurred : " + deflectedNextHop + " instead of " + currentNextHop);
                                                        } else {
                                                            if (debug)
                                                                System.out.println("No deflection at this node");
                                                        }
                                                    } else {
                                                        if (debug)
                                                            System.out.println("Not analysed...");
                                                    }
                                                }

                                            }
                                        }
                                        if (deflection) {
                                            availableShortestNextHopsNodeIds.add(rid2NodeId.get(deflectedNextHop));
//                                            System.out.println("Deflection occurred : " + rid2NodeId.get(deflectedNextHop) + " instead of " + rid2NodeId.get(currentNextHop));
//                                            System.out.println("Deflection occurred : " + deflectedNextHop + " instead of " + currentNextHop);
                                        } else {
                                            availableShortestNextHopsNodeIds.add(rid2NodeId.get(currentNextHop));
//                                            System.out.println("No deflection for " + currentNextHop + " or " + rid2NodeId.get(currentNextHop));
                                        }
                                        nbShortestPaths++;
                                    }
                                }
                                for (String currentNextHopNodeId : availableShortestNextHopsNodeIds) {
                                    String neighNode = virtualAndCoreNodeToNeighNode.get(n2.getId() + "+" + currentNextHopNodeId);
                                    if (debug)
                                        System.out.println("node " + currentNextHopNodeId + " for virtual node " + n2.getId() + " is replaced by " + neighNode);
                                    intraTrafficMatrix.set(n1.getId(), neighNode, intraTrafficMatrix.get(n1.getId(), neighNode) + aggregatedInterDomainTrafficMatrix.get(n1.getId(), n2.getId()) / nbShortestPaths);
                                }
                            } else {
                                throw new Exception("No Best route toward this prefix !!!");
                            }
                        } else {
                            if (debug)
                                System.out.println("Printing the RIB of " + n1.getRid() + " toward virtual prefix " +  n2.getRid() + "/32");
                            Router currentRouter = ridToCbgpRouter.get(n1.getRid());
                            Vector<Route> routes = currentRouter.getRIB(n2.getRid() + "/32");
//                            Vector<BGPRoute> routes = (Vector<BGPRoute>) cbgp.bgpRouterGetRib(n1.getRid(), n2.getRid() + "/32");
                            for (Route currentRoute : routes) {
                                if (debug)
                                    System.out.println("Route to next hop " + currentRoute.getNexthop() + " is best route ? = " + currentRoute.isBest());
                                if (currentRoute.isBest()) {


                                    String neighNode = virtualAndCoreNodeToNeighNode.get(n2.getId() + "+" + rid2NodeId.get(currentRoute.getNexthop().toString()));
                                    if (debug)
                                        System.out.println("node " + rid2NodeId.get(currentRoute.getNexthop().toString()) + " for virtual node " + n2.getId() + " is replaced by " + neighNode);
                                    intraTrafficMatrix.set(n1.getId(), neighNode, intraTrafficMatrix.get(n1.getId(), neighNode) + aggregatedInterDomainTrafficMatrix.get(n1.getId(), n2.getId()));
                                    if (firstEgressComputation) {
                                        precedentEgress.put(n1.getId() + n2.getRid(), neighNode);
                                    } else {
                                        if (neighNode.compareTo(precedentEgress.get(n1.getId() + n2.getRid())) != 0) {
                                            // This egress node has changed
                                            totalNbEgressChanges++;
                                            thisIterationHasChanged = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("");

        cbgp.simClear();
        cbgp.destroy();

        if (firstEgressComputation)
            firstEgressComputation = false;

        if (thisIterationHasChanged)
            nbIterationsEgressChanges++;
    }

    private void computeLinkLoads(Domain augmentedDomain, TrafficMatrix intraDomainTrafficMatrix, LinkLoads linkLoads) throws Exception {
        LinkLoadStrategy strategy = new SPFLinkLoadStrategy(augmentedDomain, intraDomainTrafficMatrix);
        strategy.recompute();
        double[] loads = strategy.getData().getLoad();
        for (int i=0; i < loads.length; i++) {
            linkLoads.setLoad(augmentedDomain.getConvertor().getLinkId(i), loads[i]);
        }
    }

    /**
     * Used to start and initialize the algorithm
     */
    public void start(HashMap params) throws AlgorithmInitialisationException {
        runningParams = params;
    }

    /**
     * Used to stop the algorithm
     */
    public void stop() {
        runningParams = null;
    }

    /**
     *  Returns the optional parameters that can be given when starting the algorithm
     * @return the list of algorithm parameters
     */
    public List<ParameterDescriptor> getStartAlgoParameters() {
        return (List<ParameterDescriptor>) params.clone();
    }

    /**
     * Returns the parameters given when the algorithm was started
     * @return
     */
    public HashMap getRunningParameters() {
        return (runningParams == null) ? null : (HashMap)runningParams.clone();
    }


}

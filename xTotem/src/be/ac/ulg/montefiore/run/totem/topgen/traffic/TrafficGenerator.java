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
package be.ac.ulg.montefiore.run.totem.topgen.traffic;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.topgen.traffic.model.TrafficModel;
import be.ac.ulg.montefiore.run.totem.topgen.util.IntPair;
import be.ac.ulg.montefiore.run.totem.topgen.util.RoutingMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.DataConsistencyException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.SPFLinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.TrafficMatrixImpl;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/*
* Changes:
* --------
* - 07-Jul-2005 : add generateOnlyEdgeTraffic property (FSK)
* - 20-Mar-2006 : use LinkLoadComputer to obtain utilisation (GMO)
* - 30-Oct-2007 : deprecate use (GMO)
*/

/**
 * This class implements a traffic generator using the traffic models.
 *
 * <p>Creation date: 2004
 * @deprecated Use {@link TrafficGeneratorInterface} and subclasses instead.
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class TrafficGenerator {

    private static final Logger logger = Logger.getLogger(TrafficGenerator.class);

    private int ASID;
    private TrafficModel model;
    private RoutingMatrix matrix;
    private double fraction;
    private double[] traffic, linkCounts;
    public static final int MAX_TRIALS = 3;
    private boolean generateOnlyEdgeTraffic = false;

    public TrafficGenerator(int ASID,TrafficModel model,double fraction) {
        this.ASID = ASID;
        this.model = model;
        this.fraction = fraction;
    }

    /**
     Initialises a newly created <code>TrafficGenerator</code> object. All
     the pairs of nodes will generate traffic.
     @param model The traffic model to be used.
     @param matrix The routing matrix to be used.
     */
    public TrafficGenerator(TrafficModel model, RoutingMatrix matrix) {
        this.model = model;
        this.matrix = matrix;
        fraction = 1;
    }

    /**
     Initialises a newly created <code>TrafficGenerator</code> object.
     @param model The traffic model to be used.
     @param matrix The routing matrix to be used.
     @param fraction The fraction of the pairs of nodes that must generate
     traffic.
     @throws IllegalArgumentException If <code>fraction</code> is equal to or
     lower than 0 or greater than 1.
     */
    public TrafficGenerator(TrafficModel model, RoutingMatrix matrix, double fraction) {
        if(fraction <= 0 || fraction > 1)
            throw new IllegalArgumentException("fraction <= 0 or "
                    +"fraction > 1.");
        this.model = model;
        this.matrix = matrix;
        this.fraction = fraction;
    }

    public final int getKey(int src, int dst) {
        int nbNodes = InterDomainManager.getInstance().getDefaultDomain().getNbNodes();
        if(src == dst)
            throw new IllegalArgumentException("src is equal to dst");
        if(src > dst)
            return (nbNodes-1) * src + dst;
        else // src < dst
            return (nbNodes-1) * src + dst - 1;
    }

    public final IntPair getIds(int column) {
        int nbNodes = InterDomainManager.getInstance().getDefaultDomain().getNbNodes();
        int nbPairs = (int) (nbNodes * nbNodes-1);
        if(column < 0 || column >= nbPairs)
            throw new IllegalArgumentException("Bad value for column = "
                    +column);
        int src = column / (nbNodes - 1);
        int dst = column % (nbNodes - 1);
        if(src <= dst)
            ++dst;
        return new IntPair(src, dst);
    }

    /**
     * This method generates traffic between <code>src</code> and <code>dst</code> using
     * <code>model</code>. It also uses <code>simTopo</code> and <code>matrix</code> to place
     * the result in <code>traffic</code>.
     */
    private void trafficGeneration(Node src, Node dst) {
        double d = model.generate(src, dst);
        try {
            int srcInt = InterDomainManager.getInstance().getDefaultDomain().getConvertor().getNodeId(src.getId());
            int dstInt = InterDomainManager.getInstance().getDefaultDomain().getConvertor().getNodeId(dst.getId());
            //int col = matrix.getKey(srcInt, dstInt);
            int col = this.getKey(srcInt, dstInt);
            traffic[col] = d;
        }
        catch(NodeNotFoundException e) {
            logger.error("NodeNotFoundException in trafficGeneration. Message: "+e.getMessage());
        }
    }

    /**
     * Get the generateOnlyEdgeTraffic property value
     *
     * If generateOnlyEdgeTraffic is true, the generator generates traffic for nodes of type EDGE only
     * otherwise it generates traffic for all pairs of nodes
     *
     * @return the generateOnlyEdge property value
     */
    public boolean isGenerateOnlyEdgeTraffic() {
        return generateOnlyEdgeTraffic;
    }

    /**
     * Set the generateOnlyEdgeTraffic property value.
     *
     * If generateOnlyEdgeTraffic is true, the generator generates traffic for nodes of type EDGE only
     * otherwise it generates traffic for all pairs of nodes
     *
     * @param generateOnlyEdgeTraffic
     */
    public void setGenerateOnlyEdgeTraffic(boolean generateOnlyEdgeTraffic) {
        this.generateOnlyEdgeTraffic = generateOnlyEdgeTraffic;
    }

    /**
     * Generates traffic for <code>TopologyManager.getInstance().getDomain()</code> using
     * <code>model</code>. This method ensures that the capacity of the links
     * is sufficient. If this is not the case, it regenerates traffic. If after
     * MAX_TRIALS, the method didn't arrive to generate traffic, it throws
     * a <code>TrafficGenerationException</code>. Note that if <code>fraction < 1</code>,
     * the pairs that generate traffic are selected randomly (using an uniform distribution).
     *
     * @deprecated generateTM()
     */
    public void generate() throws TrafficGenerationException {
        long times = System.currentTimeMillis();
        System.out.println("TrafficGenerator : Start generation");
        Domain net = InterDomainManager.getInstance().getDefaultDomain();
        List<Node> nodes = net.getUpNodes();
        // if generateOnlyEdgeTraffic is set, we remove all the core nodes
        if (generateOnlyEdgeTraffic) {
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getNodeType() == Node.Type.CORE) {
                    nodes.remove(i);
                }
            }
        }


        //int nbPairs = (int) (matrix.getNbColumns() * fraction);
        int nbNodes = InterDomainManager.getInstance().getDefaultDomain().getNbNodes();
        int nbPairs = (int) ((nbNodes * nbNodes-1) * fraction);
        if(nbPairs == 0)
            return;
        traffic = new double[net.getConvertor().getMaxNodeId() * (net.getConvertor().getMaxNodeId() - 1)];
        List<Link> links = net.getUpLinks();
        linkCounts = new double[net.getConvertor().getMaxLinkId()];

        int trial;
        Link link = null;
        int linkIdInt = -1;
        for(trial = 0; trial < MAX_TRIALS; ++trial) {

            // Traffic Generation :
            if(nbPairs == (nodes.size()*(nodes.size()-1)))  {
                // A full mesh is desired, so we don't need to use random functions.

                for(Iterator<Node> it1 = nodes.iterator(); it1.hasNext();) {
                    Node src = it1.next();
                    for(Iterator<Node> it2 = nodes.iterator(); it2.hasNext();) {
                        Node dst = it2.next();
                        if(src == dst)
                            continue;
                        trafficGeneration(src, dst);
                    }
                }
            }
            else {
                // Only nbPairs (< total number of pairs) will generate traffic.

                Random rand = new Random();
                HashMap<String, Object> alreadyUsed = new HashMap<String, Object>(nbPairs);
                for(int i = 0; i < nbPairs; ++i) {
                    int srcIndex, dstIndex;
                    Node src, dst;
                    while(true) {
                        do {
                            srcIndex = rand.nextInt(nodes.size());
                            dstIndex = rand.nextInt(nodes.size());
                        } while(srcIndex == dstIndex);
                        src = nodes.get(srcIndex);
                        dst = nodes.get(dstIndex);
                        if(!alreadyUsed.containsKey(src.getId()+dst.getId()))
                            break;
                    }
                    alreadyUsed.put(src.getId()+dst.getId(), null);
                    trafficGeneration(src, dst);
                }
            }

            // We check if the capacity of the links is sufficient.
            /*
            for(int i = 0; i < links.size(); ++i) {
            double linkCount = 0;
            IntFloatPair[] row = matrix.getRow(i);
            if(row != null) {
            for(int j = 0; j < row.length; ++j) {
            int index = row[j].getInteger();
            float value = row[j].getFloat();
            linkCount += (value * traffic[index]);
            }
            }
            linkCounts[i] = linkCount;
            }
            */
            boolean capacityOK = true;
            /*
            for(Iterator<Link> it = links.iterator(); it.hasNext() && capacityOK;) {
            link = it.next();
            String linkId = link.getId();
            try {
            linkIdInt = net.getConvertor().getLinkId(linkId);
            if(linkCounts[linkIdInt] > link.getBandwidth())
            capacityOK = false;
            }
            catch(LinkNotFoundException e) {
            logger.error("LinkNotFoundException in generate. Message: "+e.getMessage());
            }
            }
            */

            if(capacityOK)
                break;
        }

        if(trial == MAX_TRIALS) {
            traffic = null; // delete the generated traffic
            double linkCount = linkCounts[linkIdInt];
            linkCounts = null;
            throw new TrafficGenerationException("Cannot generate traffic: "
                    +link.getBandwidth()
                    +" available "+linkCount
                    +" needed !");
        }
        times = System.currentTimeMillis() - times;
        System.out.println("TrafficGenerator : End generation in " + times + " ms");
    }

    public TrafficMatrix generateTM() throws InvalidDomainException, NodeNotFoundException, NoRouteToHostException, RoutingException, DataConsistencyException {
        long time = System.currentTimeMillis();
        TrafficMatrix tm = new TrafficMatrixImpl(ASID);
        Domain domain = InterDomainManager.getInstance().getDefaultDomain();
        List<Node> nodes = domain.getUpNodes();
        // if generateOnlyEdgeTraffic is set, we remove all the core nodes
        if (generateOnlyEdgeTraffic) {
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getNodeType() == Node.Type.CORE) {
                    nodes.remove(i);
                }
            }
        }
        //TODO implement traffic generation for a fraction of the nodes only

        LinkLoadComputer llc = new SPFLinkLoadStrategy(domain, tm);
        double max = 0;
        int nbTrials = 0;
        do {
            System.out.println("Try " + nbTrials + " to generate TM");
            for (int srcNodeIdx = 0; srcNodeIdx < nodes.size(); srcNodeIdx++) {
                Node srcNode = nodes.get(srcNodeIdx);
                for (int dstNodeIdx = 0; dstNodeIdx < nodes.size(); dstNodeIdx++) {
                    Node dstNode = nodes.get(dstNodeIdx);
                    if (srcNodeIdx == dstNodeIdx) {
                        tm.set(srcNode.getId(),dstNode.getId(),0f);
                    } else {
                        tm.set(srcNode.getId(),dstNode.getId(),(float) model.generate(srcNode,dstNode));
                    }
                }
            }

            llc.update();            
            max = DoubleArrayAnalyse.getMaximum(llc.getData().getUtilization());
            nbTrials++;
        } while ((max >= 1) && (nbTrials < MAX_TRIALS));
        llc.destroy();
        time = System.currentTimeMillis() - time;
        System.out.println("Traffic matrix generated in " + time + " ms");
        return tm;
    }

    /**
     Returns the fraction of the pairs of nodes that will generate traffic.
     */
    public double getFraction() {
        return fraction;
    }

    /**
     Returns the routing matrix of this traffic generator.
     */
    public RoutingMatrix getMatrix() {
        return matrix;
    }

    /**
     Returns the generated traffic. Let's name the returned array
     <code>traffic</code>. <code>traffic[i]</code> designates the traffic
     between the pair of nodes i. The ids of the pair of nodes i can be
     found using <code>getMatrix().getIds(i)</code>.
     @throws IllegalStateException If <code>generate</code> was not called, or
     if <code>fraction</code> is too small.
     */
    public double[] getTraffic() {
        if(traffic == null)
            throw new IllegalStateException("Generate was not called or "
                    +"fraction too small.");
        double[] ret = new double[traffic.length];
        System.arraycopy(traffic, 0, ret, 0, traffic.length);
        return ret;
    }

    /**
     * Returns the link counts vector.
     * @throws IllegalStateException If <code>generate</code> was not called, or
     *                               if <code>fraction</code> is too small.
     */
    public double[] getLinkCounts() {
        if(linkCounts == null)
            throw new IllegalStateException("Generate was not called or fraction too small.");
        double[] ret = new double[linkCounts.length];
        System.arraycopy(linkCounts, 0, ret, 0, linkCounts.length);
        return ret;
    }

    /**
     Sets the fraction of the pairs of nodes that will generate traffic.
     @throws IllegalArgumentException If <code>fraction</code> is equal to or
     lower than 0 or greater than 1.
     */
    public void setFraction(double fraction) {
        if(fraction <= 0 || fraction > 1)
            throw new IllegalArgumentException("fraction <= 0 or "
                    +"fraction > 1.");
        this.fraction = fraction;
    }
}

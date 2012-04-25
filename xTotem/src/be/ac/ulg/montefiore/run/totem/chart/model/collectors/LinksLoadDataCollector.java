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
package be.ac.ulg.montefiore.run.totem.chart.model.collectors;

import be.ac.ulg.montefiore.run.totem.chart.model.exception.ChartParameterException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemAlgorithm;
import be.ac.ulg.montefiore.run.totem.repository.model.SPF;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidLinkLoadComputerException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.*;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/*
 * Changes:
 * --------
 * 25-Jan-2006: implements the getParameters and getDataParameters methods (GMO).
 * 13-Feb-2006: bugfix (JLE).
 * 13-Feb-2006: add statistic parameter (JLE).
 * 13-Feb-2006: comments fixes (JLE).
 * 13-Feb-2006: add SuppressWarnings annotations (JLE).
 * 20-Mar-2006: use LinkLoadComputer to calculate load (GMO).
 * 22-Mar-2006: the data now takes only one parameter which is the id of the computed load (GMO).
 * 23-Mar-2006: the data can take either the id of the computedload either a set of parameter describing the load (GMO).
 * 13-Aug-2007: implements getDefaultSeriesName() method (GMO)
 * 26-Feb-2008: add Overlay and igp shortcut, deprecate parameters (GMO)
 */

/**
* Compute the load of the links of a domain given a traffic matrix.
* The domain is, by default, the default domain at instantiation time. This can be overriden by a call to setParameters.
* The traffic matrix used is the one given to collectData or the default one if none is provided to collectData.
*
* <p>Creation date: 20 déc. 2005
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LinksLoadDataCollector implements ChartDataCollector {
    private static final Logger logger = Logger.getLogger(LinksLoadDataCollector.class);

    private static final int STAT_ALL = 0;
    private static final int STAT_MAX = 1;
    private static final int STAT_MEAN = 2;
    
    private static final ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();
    private static final ArrayList<ParameterDescriptor> dataParams = new ArrayList<ParameterDescriptor>();

    static {
        try {
        	params.add(new ParameterDescriptor("asId", "Domain ASID (leave blank for default domain).", Integer.class, null));
            params.add(new ParameterDescriptor("absoluteLoad", "Choose weither relative load should be considered or not", Boolean.class, new Boolean(false)));
            params.add(new ParameterDescriptor("statistic", "Choose the statistic to be collected", String.class, "All", new String[]{"All", "Max", "Mean"}));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }

        try {
            //this parameter is not accessible from scenario events
            dataParams.add(new ParameterDescriptor("linkLoadComputerId", "Use only this parameter (others are deprecated). Id of the LinkLoadComputer that calculates the load (leave blank for default).", String.class, null));
        	dataParams.add(new ParameterDescriptor("tmId", "Deprecated. Traffic Matrix Id (leave blank for default).", Integer.class, null));
            dataParams.add(new ParameterDescriptor("routingAlgo", "Deprecated. Routing algorithm", String.class, null));
            dataParams.add(new ParameterDescriptor("strategy", "Deprecated. Routing strategy", String.class, null, new String[]{null, "IP", "BIS", "IS", "OVERLAY"}));
            dataParams.add(new ParameterDescriptor("ECMP", "Deprecated. Equal Cost Multi Path enabled.", Boolean.class, null));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    private Domain domain = null;
    private boolean absoluteLoad = false;
    private int statistic = STAT_ALL;

    public LinksLoadDataCollector() {
        domain = InterDomainManager.getInstance().getDefaultDomain();
    }

    /**
     * The parameters are
     *    - asId : specify the asId of the domain to use for data collection.
     *    - absoluteLoad (default false) : specify if the computed load should be absolute or relative to link bandwidth.
     * @param params
     * @throws be.ac.ulg.montefiore.run.totem.chart.model.exception.ChartParameterException if the given domain is not found in the InterDomainManager
     */
    public void setParameters(HashMap<String, String> params) throws ChartParameterException {
        try {
            if (params != null && params.get("asId") != null) {
                domain = InterDomainManager.getInstance().getDomain(Integer.parseInt(params.get("asId")));
            } else {
                domain = InterDomainManager.getInstance().getDefaultDomain();
            }
        } catch (InvalidDomainException e) {
            domain = null;
            e.printStackTrace();
        }
        if (domain == null){
            logger.error("Domain not found (not loaded)");
            throw new ChartParameterException("Domain not found.");
        }

        if ((params != null) && params.get("absoluteLoad") != null) {
            absoluteLoad = params.get("absoluteLoad").equals("true");
        }
        
        if(params != null) {
            String statistic = params.get("statistic");
            if (statistic == null) return;
            if(statistic.equals("All")) {
                this.statistic = STAT_ALL;
            } else if(statistic.equals("Max")) {
                this.statistic = STAT_MAX;
            } else if(statistic.equals("Mean")) {
                this.statistic = STAT_MEAN;
            } else {
                throw new ChartParameterException("Statistic "+statistic+" unknown in LinksLoadDataCollector.");
            }
        }
    }


    /**
     * return a vector of link load computed with the given trafficMatrix using a SPF strategy.
     * The index of a specific link in the returned array is given by domain.getConvertor().getLinkId(linkName)
     * if statistic is STAT_ALL. Else, the statistic value is returned taking only up links into account.
     *
     * @param params :
     *               <ul>
     *               <li>linkLoadComputerId (optional), id of the LinkLoadComputer object. If this parameter is set,
     *               the others are ignored and the load is taken from an existing linkLoadComputer object.</li>
     *               <li>tmId (optional, deprecated), trafficMatrix id. The matrix must be loaded in the TraffixMatrixManager.
     *               If ommitted, the default trafficatrix is used.</li>
     *               <li>routingAlgo (optional, deprecated), SPF routing algorihtm. The algorithm must be loaded in the repositoryManager
     *               If ommited, default CSPF is used.</li>
     *               <li>strategy (optional, deprecated), the strategy to use. Can be IP for SPFLinkLoadStrategy or BIS for BasicIGPShortcut.
     *               If ommited, IP is used.</li>
     *               <li>ECMP (optional, deprecated), Equal cost multi path. Can be true or false for enabled or disabled.
     *               If ommited, disabled by default.</li></ul>
     * @throws be.ac.ulg.montefiore.run.totem.chart.model.exception.ChartParameterException
     *          if tmId is NaN or the corresponding TrafficMatrix is not loaded, or if the domain is null
     */
    public double[] collectData(HashMap<String, String> params) throws ChartParameterException {
        double[] loads = null;

        if (domain == null) {
            throw new ChartParameterException("Domain not specified");
        }

        if (params == null) throw new ChartParameterException("Missing parameters.");

        if (params.get("tmId") != null) logger.warn("Parameter tmId deprecated.");
        if (params.get("strategy") != null) logger.warn("Parameter strategy deprecated.");
        if (params.get("ECMP") != null) logger.warn("Parameter ECMP deprecated.");
        if (params.get("routingAlgo") != null) logger.warn("Parameter routingAlgo deprecated.");

        LinkLoadComputer llc;
        if (params.get("linkLoadComputerId") != null) {
            //using LLC by id

            try {
                llc = LinkLoadComputerManager.getInstance().getLinkLoadComputer(domain, params.get("linkLoadComputerId"));
            } catch (InvalidLinkLoadComputerException e) {
                throw new ChartParameterException("LinkLoadComputer not found.");
            }
            if (logger.isEnabledFor(Level.WARN)) {
                if (params.get("tmId") != null || params.get("strategy") != null || params.get("ECMP") != null ||
                        params.get("routingAlgo") != null) {
                    logger.warn("using linkLoadComputerId, ignoring other parameters.");
                }
            }
        } else {
            if (params.get("tmId") == null && params.get("strategy") == null && params.get("ECMP") == null &&
                    params.get("routingAlgo") == null) {
                //using default LLC
                try {
                    llc = LinkLoadComputerManager.getInstance().getDefaultLinkLoadComputer(domain);
                } catch (InvalidLinkLoadComputerException e) {
                    throw new ChartParameterException("LinkLoadComputer not found.");
                }
            } else {
                // use parameter to get or create LLC
                if (domain == null) {
                    throw new ChartParameterException("Domain not specified");
                }

                SPF spfAlgo = null;
                TrafficMatrix TM = null;
                int tmId = -1;
                int asId = domain.getASID();

                if (params.get("tmId") != null) {
                    try {
                        tmId = Integer.parseInt(params.get("tmId"));
                    } catch (NumberFormatException e) {
                        throw new ChartParameterException("Invalid parameter format: tmId");
                    }
                }

                if (params.get("routingAlgo") != null) {
                    try {
                        TotemAlgorithm algo = RepositoryManager.getInstance().getAlgo(params.get("routingAlgo"), asId, tmId);
                        spfAlgo = (SPF) algo;
                    } catch (NoSuchAlgorithmException e) {
                        throw new ChartParameterException("Invalid parameter: routingAlgo not found or not started:" + params.get("routingAlgo"));
                    } catch (ClassCastException e) {
                        throw new ChartParameterException("Invalid parameter: routingAlgo is not a SPF algo:" + params.get("routingAlgo"));
                    }
                }

                try {
                    if (tmId < 0) {
                        TM = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(asId);
                    } else {
                        TM = TrafficMatrixManager.getInstance().getTrafficMatrix(asId, tmId);
                    }
                } catch (InvalidTrafficMatrixException e) {
                    logger.error("Impossible to compute Link Load");
                    throw new ChartParameterException("asId or tmId does not correspond to a loaded object.");
                }

                String strategyStr = params.get("strategy");
                LinkLoadStrategy strategy;
                if (strategyStr == null || strategyStr.equals("IP")) {
                    strategy = new SPFLinkLoadStrategy(domain, TM);
                } else if (strategyStr.equals("BIS")) {
                    strategy = new BasicIGPShortcutStrategy(domain, TM);
                } else if (strategyStr.equals("IS")) {
                    strategy = new IGPShortcutStrategy(domain, TM);
                } else if (strategyStr.equals("OVERLAY")) {
                    strategy = new OverlayStrategy(domain, TM);
                } else {
                    throw new ChartParameterException("Strategy not found.");
                }

                String ECMPStr = params.get("ECMP");
                if (ECMPStr != null && ECMPStr.equals("true")) {
                    strategy.setECMP(true);
                }
                else if (ECMPStr != null && ECMPStr.equals("false")) {
                    strategy.setECMP(false);
                }

                if (spfAlgo != null) strategy.setSPFAlgo(spfAlgo);

                llc = strategy;

            }
        }

        // get fresh values
        llc.update();

        if (absoluteLoad) {
            loads = llc.getData().getLoad();
        } else {
            loads = llc.getData().getUtilization();
        }

        if (statistic == STAT_ALL)
            return loads;

        double[] linkLoads = new double[domain.getUpLinks().size()];
        int i = 0;
        for (Link l : domain.getUpLinks()) {
            try {
                linkLoads[i++] = loads[domain.getConvertor().getLinkId(l.getId())];
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }
        switch(statistic) {
        case STAT_ALL:
            return loads;
        case STAT_MAX:
            return new double[]{DoubleArrayAnalyse.getMaximum(linkLoads)};
        case STAT_MEAN:
            return new double[]{DoubleArrayAnalyse.getMeanValue(linkLoads)};
        default:
            throw new ChartParameterException("Unknown statistic "+statistic+".");
        }
    }

    @SuppressWarnings("unchecked")
    public List<ParameterDescriptor> getDataParameters() {
        return (List<ParameterDescriptor>) dataParams.clone();
    }

    /**
     * Returns the identifier of the default link load computer as default series name
     * @return
     */
    public String getDefaultSeriesName() {
        try {
            return LinkLoadComputerManager.getInstance().getDefaultLinkLoadComputerId(InterDomainManager.getInstance().getDefaultDomain());
        } catch (InvalidLinkLoadComputerException e) {
            //e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<ParameterDescriptor> getParameters() {
		return (List<ParameterDescriptor>) params.clone();
    }

}

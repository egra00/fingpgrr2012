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
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/*
* Changes:
* --------
* - 25-Jan-2006: implements the getParameters and getDataParameters methods (GMO)
* - 09-Aug-2007: add statistic parameter (GMO)
* - 29-Feb-2008: fix bug in load calculation (wrong index) (GMO)
*
*/

/**
* Get the links reserved bandwidth of a domain.
*
* <p>Creation date: 1 d�c. 2005
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LinksReservedBWDataCollector implements ChartDataCollector {
    private static final Logger logger = Logger.getLogger(LinksReservedBWDataCollector.class);

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

        //no data parameters
    }


    private Domain domain = null;
    private boolean absoluteLoad = false;
    private int statistic = STAT_ALL;

    public LinksReservedBWDataCollector() {
        domain = InterDomainManager.getInstance().getDefaultDomain();
    }

    /**
     * The parameters are
     *    - asId : specify the asId of the domain to use for data collection.
     *    - absoluteLoad (default false) : specify if the computed load should be absolute or relative to link bandwidth.
     * @param params
     * @throws ChartParameterException if the given domain is not found in the InterDomainManager
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

        if (params != null && params.get("absoluteLoad") != null) {
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
     * return a vector of link resevrved bandwidth at the moment of the call.
     * The index of a specific link in the returned array is given by domain.getConvertor().getLinkId(linkName)
     * if statistic is STAT_ALL. Else, the statistic value is returned taking all links nito account.
     * @param params no parameters are used.
     * @return
     * @throws ChartParameterException is never thrown
     */
    public double[] collectData(HashMap<String,String> params) throws ChartParameterException {
        //no params used

        if (domain == null) {
            throw new ChartParameterException("Domain not specified.");
        }

        if (statistic == STAT_ALL) {
            double[] loads = new double[domain.getConvertor().getMaxLinkId()];
            if (absoluteLoad) {
                for (Link lnk : domain.getAllLinks()) {
                    try {
                        int idx = domain.getConvertor().getLinkId(lnk.getId());
                        loads[idx] = lnk.getReservedBandwidth();
                    } catch (LinkNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                for (Link lnk : domain.getAllLinks()) {
                    try {
                        int idx = domain.getConvertor().getLinkId(lnk.getId());
                        loads[idx] = lnk.getReservedBandwidth() / lnk.getBandwidth();
                    } catch (LinkNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            }

            return loads;
        }

        // statistic asked. Use all links, even down ones

        double[] linkLoads = new double[domain.getNbLinks()];
        int i = 0;
        if (absoluteLoad) {
            for (Link l : domain.getAllLinks()) {
                linkLoads[i++] = l.getReservedBandwidth();
            }
        } else {
            for (Link l : domain.getAllLinks()) {
                linkLoads[i++] = l.getReservedBandwidth() / l.getBandwidth();
            }
        }
        switch(statistic) {
        case STAT_MAX:
            return new double[]{DoubleArrayAnalyse.getMaximum(linkLoads)};
        case STAT_MEAN:
            return new double[]{DoubleArrayAnalyse.getMeanValue(linkLoads)};
        default:
            throw new ChartParameterException("Unknown statistic "+statistic+".");
        }
    }

    public List<ParameterDescriptor> getParameters() {
		return (List<ParameterDescriptor>) params.clone();
    }

    public List<ParameterDescriptor> getDataParameters() {
		return (List<ParameterDescriptor>) dataParams.clone();
    }

    public String getDefaultSeriesName() {
        return null;
    }

}

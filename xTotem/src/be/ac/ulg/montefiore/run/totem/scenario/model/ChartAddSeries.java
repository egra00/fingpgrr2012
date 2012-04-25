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

import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.chart.facade.ChartManager;
import be.ac.ulg.montefiore.run.totem.chart.model.Chart;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.ChartParameterException;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.NoSuchChartException;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ChartAddSeriesType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.Param;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.ChartAddSeriesImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class implements a chart add series event.
 *
 * <p>Creation date: 13-janv.-2006
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class ChartAddSeries extends ChartAddSeriesImpl implements Event {

    private static final Logger logger = Logger.getLogger(ChartAddSeries.class);

    public ChartAddSeries() {}
    
    public ChartAddSeries(String chartId, String seriesName, HashMap<String, String> params) {
        setChartId(chartId);
        setSeriesName(seriesName);
        if((params == null) || (params.size() == 0)) {
            return;
        }
        ObjectFactory factory = new ObjectFactory();
        try {
            ChartAddSeriesType.CollectorType collector = factory.createChartAddSeriesTypeCollectorType();
            setCollector(collector);
            for(Entry<String, String> entry : params.entrySet()) {
                Param param = factory.createParam();
                param.setName(entry.getKey());
                param.setValue(entry.getValue());
                collector.getParam().add(param);
            }
        } catch (JAXBException e) {
            logger.error("JAXBException in constructor of chart add series event. Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a chart add series event - chartId: "+getChartId()+" - seriesName: "+getSeriesName());
        Chart chart;
        try {
            chart = ChartManager.getInstance().getChart(getChartId());
        } catch(NoSuchChartException e) {
            logger.error("There is no chart "+getChartId());
            throw new EventExecutionException(e);
        }
        HashMap<String, String> params = new HashMap<String, String>();
        if(getCollector() != null) {
            for(Object o : getCollector().getParam()) {
                Param param = (Param) o;
                params.put(param.getName(), param.getValue());
            }
        }
        try {
            chart.addSeries(getSeriesName(), params);
        } catch(ChartParameterException e) {
            logger.error("The series name "+getSeriesName()+" is already used for this chart!");
            throw new EventExecutionException("Series name already used", e);
        }
        logger.info("Data series added.");
        return new EventResult(chart);
    }

}

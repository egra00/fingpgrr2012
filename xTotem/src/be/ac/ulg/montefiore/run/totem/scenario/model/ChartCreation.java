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
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.chart.facade.ChartManager;
import be.ac.ulg.montefiore.run.totem.chart.model.Chart;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.ChartParameterException;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.Param;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.ChartCreationImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class implements a chart creation event.
 *
 * <p>Creation date: 13-janv.-2006
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class ChartCreation extends ChartCreationImpl implements Event {

    private static final Logger logger = Logger.getLogger(ChartCreation.class);
    
    public ChartCreation() {}
    
    public ChartCreation(String id, String collectorName, HashMap<String, String> params) {
        setId(id);
        
        try {
            ObjectFactory factory = new ObjectFactory();
            CollectorType collector = factory.createChartCreationTypeCollectorType();
            setCollector(collector);
            collector.setName(collectorName);
            if(params == null) {
                return;
            }
            for(Entry<String, String> entry : params.entrySet()) {
                Param param = factory.createParam();
                param.setName(entry.getKey());
                param.setValue(entry.getValue());
                collector.getParam().add(param);
            }
        } catch (JAXBException e) {
            logger.error("JAXBException in constructor of ChartCreation. Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

   public EventResult action() throws EventExecutionException {
        logger.debug("Processing a chart creation event - id: "+getId()+" - collector name: "+getCollector().getName());
        HashMap<String, String> params = new HashMap<String, String>();
        for(Object o : getCollector().getParam()) {
            Param param = (Param) o;
            params.put(param.getName(), param.getValue());
        }
        Chart chart = null;
        try {
            chart = new Chart(getCollector().getName(), params);
            ChartManager.getInstance().addChart(getId(), chart);
        } catch (ChartParameterException e) {
            logger.error("Exception in chart parameters.");
            throw new EventExecutionException(e);
        }
        logger.info("Chart created.");
        return new EventResult(chart);
    }
}

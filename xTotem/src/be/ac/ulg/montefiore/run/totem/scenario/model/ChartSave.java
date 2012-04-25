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
import be.ac.ulg.montefiore.run.totem.chart.model.exception.NoSuchChartException;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.ChartParameterException;
import be.ac.ulg.montefiore.run.totem.chart.persistence.ChartFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ChartFormatType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.Param;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.ChartSaveImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.facade.ScenarioExecutionContext;
import be.ac.ulg.montefiore.run.totem.util.FileFunctions;

/*
 * Changes:
 * --------
 *
 * - 17-Jan-2006: add EPS format (JLE).
 * - 09-Jan-2007: use scenario context for file name (GMO)
 * - 09-Aug-2007: the object returned by the action method contains a message indicating where the chart was saved (GMO) 
 */

/**
 * This class implements a chart save event.
 *
 * <p>Creation date: 13-janv.-2006
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class ChartSave extends ChartSaveImpl implements Event {

    private static final Logger logger = Logger.getLogger(ChartSave.class);
    
    public ChartSave() {}
    
    public ChartSave(String chartId, String fileName, ChartFormatType format, String title, String xAxisTitle, String yAxisTitle, int width, int height, String plotterName, HashMap<String, String> params) {
        setChartId(chartId);
        setFile(fileName);
        setFormat(format);
        setTitle(title);
        setXAxisTitle(xAxisTitle);
        setYAxisTitle(yAxisTitle);
        setWidth(width);
        setHeight(height);
        
        ObjectFactory factory = new ObjectFactory();
        try {
            PlotterType plotter = factory.createChartSaveTypePlotterType();
            setPlotter(plotter);
            plotter.setName(plotterName);
            if(params == null) {
                return;
            }
            for(Entry<String, String> entry : params.entrySet()) {
                Param param = factory.createParam();
                param.setName(entry.getKey());
                param.setValue(entry.getValue());
                plotter.getParam().add(param);
            }
        } catch (JAXBException e) {
            logger.error("JAXBException in constructor of ChartSave. Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a chart save event - chartId: "+getChartId()+" - file: "+getFile()+" - format: "+getFormat().toString());
        Chart chart;
        try {
            chart = ChartManager.getInstance().getChart(getChartId());
        } catch(NoSuchChartException e) {
            logger.error("There is no chart "+getChartId());
            throw new EventExecutionException(e);
        }
        HashMap<String, String> params = new HashMap<String, String>();
        for(Object o : getPlotter().getParam()) {
            Param param = (Param) o;
            params.put(param.getName(), param.getValue());
        }
        try {
            chart.plot(getPlotter().getName(), getTitle(), getXAxisTitle(), getYAxisTitle(), params);
        } catch (ChartParameterException e) {
            //e.printStackTrace();
            logger.error("Impossible to save the chart : " + e.getMessage());
            throw new EventExecutionException(e);
        }
        String ff = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), _File);
        if(getFormat().equals(ChartFormatType.JPG)) {
            ChartFactory.saveChart(ff, chart, ChartFactory.JPEG_FORMAT, getWidth(), getHeight());
        } else if(getFormat().equals(ChartFormatType.PNG)) {
            ChartFactory.saveChart(ff, chart, ChartFactory.PNG_FORMAT, getWidth(), getHeight());
        } else if(getFormat().equals(ChartFormatType.EPS)) {
            ChartFactory.saveChart(ff, chart, ChartFactory.EPS_FORMAT, getWidth(), getHeight());
        } else {
            logger.warn("The format "+getFormat().toString()+" has not yet been implemented in the chart save event!");
            throw new EventExecutionException("The format "+getFormat().toString()+" has not yet been implemented in the chart save event!");
        }
        logger.info("Chart saved.");
        return new EventResult(null, "Chart saved as " + ff);
    }
}

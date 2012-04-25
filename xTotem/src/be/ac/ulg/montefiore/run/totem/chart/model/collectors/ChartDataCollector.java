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
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

import java.util.HashMap;
import java.util.List;


/*
* Changes:
* --------
* 25-Jan-2006: add the getParameters and getDataParameters methods (GMO)
* 13-Aug-2007: add getDefaultSeriesName() method (GMO)
*/

/**
* Collect and compute some data to use with a chart.
* @see be.ac.ulg.montefiore.run.totem.chart.model.Chart
*
* <p>Creation date: 20 d�c. 2005
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public interface ChartDataCollector {
    /**
     * Set additional (implementation dependant) parameter of the collector.
     * @param params
     * @throws ChartParameterException if one of the parameters is invalid or malformed.
     */
    public void setParameters(HashMap<String, String> params) throws ChartParameterException;
    /**
     * Collect the data and return it in a double vector.
     * @param params Parameters for computing the data to be collected
     * @return
     * @throws ChartParameterException if one of the parameters is invalid or malformed. 
     */
    public double[] collectData(HashMap<String, String> params) throws ChartParameterException;

    /**
     * returns a list of parameters that can be given to the setParameters method
     * @return
     */
    public List<ParameterDescriptor> getParameters();

    /**
     * returns a list of parameters that can be given to the collectData method
     * @return
     */
    public List<ParameterDescriptor> getDataParameters();

    /**
     * Returns the default name of the next series to add to the chart
     * @return
     */
    public String getDefaultSeriesName();
}

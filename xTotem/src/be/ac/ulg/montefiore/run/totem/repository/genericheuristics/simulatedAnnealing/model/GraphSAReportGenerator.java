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
package be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.IOException;

/*
 * Changes:
 * --------
 *
 */

/**
 * Graph SA report generator
 *
 * <p>Creation date: 20-Dec.-2004
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 */
public class GraphSAReportGenerator implements SAReportGenerator {


    XYSeries presentSeries;
    XYSeries bestSeries;
    XYSeriesCollection xySeriesCollection;
    int counter;
    int plotCounter;
    int factor;

    public GraphSAReportGenerator(){
        bestSeries = new XYSeries("Best Solution");
        presentSeries = new XYSeries("Current Solution");
        counter = 0;
        factor = 1;

    }
     public GraphSAReportGenerator(int factor){
        bestSeries = new XYSeries("Best Solution");
        presentSeries = new XYSeries("Current Solution");
        counter = 0;
        this.factor = factor;

    }


    public void addSolution(double presentSolutionCost, double bestSolutionCost, float Temperature) {
        //To change body of implemented methods use File | Settings | File Templates.

        if (plotCounter%factor == 0){
            bestSeries.add(plotCounter,bestSolutionCost);
            presentSeries.add(plotCounter,presentSolutionCost);
        }
        plotCounter++;



    }

    public void save(String FileName) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.

    }

    public XYSeriesCollection GetGraphData(){
        xySeriesCollection = new XYSeriesCollection();
        xySeriesCollection.addSeries(presentSeries);
        xySeriesCollection.addSeries(bestSeries);

        return xySeriesCollection;

        

    }


}

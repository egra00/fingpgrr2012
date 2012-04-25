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
package it.unina.traffic.interDepartureTime;

import java.text.DecimalFormat;

/*
* Changes:
* --------
*
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 27/11/2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class NormalIDT implements InterDepartureTime {
    //in packets per second
    private double mean;

    private DecimalFormat df;

    public NormalIDT(double mean) {
        this.mean = mean;
        df = new DecimalFormat();
        df.setMaximumFractionDigits(6);
    }

    /* we want to have more or less 95% of values between (mean-(mean*inter)) and (mean+(mean*inter))
      we know that 95.44997361036% of the values are between 2 stddev.
      stddev is thus inter*mean.
    */
    public double inter = 0.1;

    public double getMean() {
        return mean;
    }

    public void setMean(double meanIDT) {
        mean = meanIDT;
    }

    public String getCommand() {
        double meanIDT = 1000/mean;
        double stddev = inter*meanIDT;

        return "-N " + df.format(meanIDT) + " " + df.format(stddev);
    }
}

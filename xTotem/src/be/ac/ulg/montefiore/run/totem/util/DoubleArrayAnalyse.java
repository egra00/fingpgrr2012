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
package be.ac.ulg.montefiore.run.totem.util;

import java.util.Arrays;

/*
 * Changes:
 * --------
 *
 */

/**
 * Provide some statistics on an array
 *
 * <p>Creation date: 29-Oct-2004 15:22:21
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class DoubleArrayAnalyse {

    /**
     * Get the maximum element
     *
     * @param array
     * @return the maximum of the array
     */
    public static double getMaximum(double array[]) {
        if (array.length == 0)
            return 0;
        double maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue)
                maxValue = array[i];
        }
        return maxValue;
    }

    /**
     * Get the minimum element
     *
     * @param array
     * @return the minimum of the array
     */
    public static double getMinimum(double array[]) {
        if (array.length == 0)
            return 0;
        double minValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < minValue)
                minValue = array[i];
        }
        return minValue;
    }

    /**
     * Get the mean value of the elements
     *
     * @param array
     * @return
     */
    public static double getMeanValue(double array[]) {
        if (array.length == 0)
            return 0;
        double sumValue = 0;
        int nbElem = 0;
        for (nbElem = 0; nbElem < array.length; nbElem++) {
            sumValue += array[nbElem];
        }
        return (sumValue / nbElem);
    }

    /**
     * Get the standard deviation of the elements
     * @param array
     * @return
     */
    public static double getStandardDeviation(double array[]) {
       if (array.length == 0)
            return 0;
        double stddev = 0;
        double sumValue = 0;
        int nbElem = 0;
        for (nbElem = 0; nbElem < array.length; nbElem++) {
            stddev += Math.pow(array[nbElem],2);
            sumValue += array[nbElem];
        }
        double mean = sumValue / nbElem;
        stddev = (double) Math.sqrt((stddev / nbElem) - Math.pow(mean, 2));
        return stddev;
    }

    /**
     * Get the variance of the elements
     * @param array
     * @return
     */
    public static double getVariance(double array[]) {
        if (array.length == 0)
            return 0;
        double mean = getMeanValue(array);
        double variance = 0;
        for (int i = 0; i < array.length; ++i) {
            variance += Math.pow(array[i] - mean, 2);
        }
        return variance / array.length;
    }
    
    /**
     * Returns the percentile 10.
     * @param array
     * @return
     */
    public static double getPercentile10(double array[]) {
        return getPercentile(array,90);
    }

    /**
     * Get the specified percentile
     *
     * @param array
     * @return
     */
    public static double getPercentile(double array[], int percentile) {
        if (percentile == 0)
            return array[0];
        double cpArray[] = new double[array.length];
        System.arraycopy(array,0,cpArray,0,array.length);
        Arrays.sort(cpArray);
        int idx = (int) ((double) cpArray.length * ((double) percentile / 100.0));
        return cpArray[idx];
    }

     public static double getMinResidualBandwidth(double utilizations[], float capacities[]) {
        double minResidualBandwidth = Double.MAX_VALUE;
        for (int i=0; i < utilizations.length; i++) {
            if (capacities[i] != -1) {
                double residualBw = capacities[i] * (1 - utilizations[i]);
                if (residualBw < minResidualBandwidth) {
                    minResidualBandwidth = residualBw;
                }
            }
        }
        return minResidualBandwidth;
    }

    public static double getRUNObjFunc(double utilizations[], float capacities[]) {
        double meanResidualBw = 0f;
        int nbVal = 0;
        for (int i=0; i < utilizations.length; i++) {
            if (capacities[i] != -1) {
                double residualBw = capacities[i] * (1 - utilizations[i]);
                meanResidualBw += (1 / residualBw);
                nbVal++;
            }
        }
        meanResidualBw /= nbVal;
        return meanResidualBw;
    }

    public static double getIGPWOObjectiveFunctionValue(double utilizations[], float capacities[]) {
        double totalCostFunction = 0;

        for (int i=0; i < utilizations.length; i++) {
            if (capacities[i] != -1) {
                double phi;
                if (utilizations[i] < (1f/3)) {
                    phi = utilizations[i] * capacities[i];
                } else if (utilizations[i] < (2f/3)) {
                    phi = (3 * utilizations[i] * capacities[i]) - ((2f/3) * capacities[i]);
                } else if (utilizations[i] < (9f/10)) {
                    phi = (10 * utilizations[i] * capacities[i]) - ((16f/3) * capacities[i]);
                } else if (utilizations[i] < 1f) {
                    phi = (70 * utilizations[i] * capacities[i]) - ((178f/3) * capacities[i]);
                } else if (utilizations[i] < (11f/10)) {
                    phi = (500 * utilizations[i] * capacities[i]) - ((1468f/3) * capacities[i]);
                } else  {
                    phi = (5000 * utilizations[i] * capacities[i]) - ((16318f/3) * capacities[i]);
                }
                totalCostFunction += phi;
            }
        }

        return totalCostFunction;
    }


    public static double getFortz(double load[], float capacities[]) {
        double totalCostFunction = 0;

        for (int i=0; i < load.length; i++) {
            if (capacities[i] != -1) {
                double phi;
                double util = load[i] / capacities[i];
                if (util < (1f/3)) {
                    phi = load[i];
                } else if (util < (2f/3)) {
                    phi = (3 * load[i]) - ((2f/3) * capacities[i]);
                } else if (util < (9f/10)) {
                    phi = (10 * load[i]) - ((16f/3) * capacities[i]);
                } else if (util < 1f) {
                    phi = (70 * load[i]) - ((178f/3) * capacities[i]);
                } else if (util < (11f/10)) {
                    phi = (500 * load[i]) - ((1468f/3) * capacities[i]);
                } else  {
                    phi = (5000 * load[i]) - ((16318f/3) * capacities[i]);
                }
                totalCostFunction += phi;
            }
        }

        return totalCostFunction;
    }

    public static double getFortz(double load[], double capacities[]) {
        double totalCostFunction = 0;

        for (int i=0; i < load.length; i++) {
            if (capacities[i] != -1) {
                double phi;
                double util = load[i] / capacities[i];
                if (util < (1f/3)) {
                    phi = load[i];
                } else if (util < (2f/3)) {
                    phi = (3 * load[i]) - ((2f/3) * capacities[i]);
                } else if (util < (9f/10)) {
                    phi = (10 * load[i]) - ((16f/3) * capacities[i]);
                } else if (util < 1f) {
                    phi = (70 * load[i]) - ((178f/3) * capacities[i]);
                } else if (util < (11f/10)) {
                    phi = (500 * load[i]) - ((1468f/3) * capacities[i]);
                } else  {
                    phi = (5000 * load[i]) - ((16318f/3) * capacities[i]);
                }
                totalCostFunction += phi;
            }
        }

        return totalCostFunction;
    }


}

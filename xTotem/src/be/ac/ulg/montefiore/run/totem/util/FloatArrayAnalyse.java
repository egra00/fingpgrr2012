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
 * @author  Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class FloatArrayAnalyse {

    /**
     * Get the maximum element
     *
     * @param array
     * @return
     */
    public static float getMaximum(float array[]) {
        if (array.length == 0)
            return 0;
        float maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue)
                maxValue = array[i];
        }
        return maxValue;
    }

    /**
     * Get the mean value of the elements
     *
     * @param array
     * @return
     */
    public static float getMeanValue(float array[]) {
        if (array.length == 0)
            return 0;
        float sumValue = 0;
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
    public static float getStandardDeviation(float array[]) {
       if (array.length == 0)
            return 0;
        float stddev = 0;
        int nbElem = 0;
        float mean = getMeanValue(array);
        for (nbElem = 0; nbElem < array.length; nbElem++) {
            stddev += Math.pow((array[nbElem] - mean),2);
        }
        stddev = (float) Math.sqrt(stddev / nbElem);
        return stddev;
    }

    /**
     * Get the percentile 10
     * @param array
     * @return
     */
    public static float getPercentile10(float array[]) {
        return getPercentile(array,90);
    }

    /**
     * Get the specified percentile
     *
     * @param array
     * @return
     */
    public static float getPercentile(float array[], int percentile) {
        if (percentile == 0) {
            return array[0];
        }
        
        float[] cloneArray = new float[array.length];
        System.arraycopy(array, 0, cloneArray, 0, array.length);
        
        Arrays.sort(cloneArray);
        int idx = (int) ((float) cloneArray.length * ((float) percentile / 100.0));
        return cloneArray[idx];
    }

    public static float getMinResidualBandwidth(float utilizations[], float capacities[]) {
        float minResidualBandwidth = Float.MAX_VALUE;
        for (int i=0; i < utilizations.length; i++) {
            if (capacities[i] != -1) {
                float residualBw = capacities[i] * (1 - utilizations[i]);
                if (residualBw < minResidualBandwidth) {
                    minResidualBandwidth = residualBw;
                }
            }
        }
        return minResidualBandwidth;
    }

    public static float getRUNObjFunc(float utilizations[], float capacities[]) {
        float meanResidualBw = 0f;
        int nbVal = 0;
        for (int i=0; i < utilizations.length; i++) {
            if (capacities[i] != -1) {
                float residualBw = capacities[i] * (1 - utilizations[i]);
                meanResidualBw += (1 / residualBw);
                nbVal++;
            }
        }
        meanResidualBw /= nbVal;
        return meanResidualBw;
    }

    public static float getIGPWOObjectiveFunctionValue(float utilizations[], float capacities[]) {
        float totalCostFunction = 0;

        for (int i=0; i < utilizations.length; i++) {
            if (capacities[i] != -1) {
                float phi;
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
}

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

package be.ac.ulg.montefiore.run.totem.repository.optDivideTM.MTRWO;

import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 */

/**
 * 
 * <p/>
 * <p>Creation date : 8 d�c. 2005 09:47:27
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class InvCapOF implements TEObjectiveFunction {
    private static Logger logger = Logger.getLogger(InvCapOF.class);

    public float getFirstDerivate(float capacity, double load) {
        return (float) (1 / capacity);
    }

    public double getScore(double linkCapacities[], double linkLoads[]) {
        double score = 0;

        for (int i=0; i < linkLoads.length; i++) {
            score += (linkLoads[i] / linkCapacities[i]);
        }

        return score;
    }

    public String getName() {
        return new String("InvCap");
    }

    public String toString() {
        return getName();
    }

}

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

package be.ac.ulg.montefiore.run.totem.domain.model;

/*
 * Changes:
 * --------
 *
 * - 19-Oct-2006: fix layout and comments of the class (JLE).
 */

/**
 * An enum class that expresses bandwidth units.
 * 
 * <p>Creation date: 11-Jul-2006
 * 
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public enum BandwidthUnit {
    DEFAULT_UNIT, BPS, KBPS, MBPS, GBPS, TBPS;
    
    /**
     * Converts a float in the unit given as parameter to the unit of this enum.
     * @param convertFrom The unit in which <code>number</code> must be interpreted
     * @param number The float to convert
     * @return the float converted in the unit of this object
     */
    public float convert(BandwidthUnit convertFrom, float number) {
        if (convertFrom == DEFAULT_UNIT ^ this == DEFAULT_UNIT) {
            throw new IllegalArgumentException("Unable to convert DEFAULT_UNIT.");
        } else if (convertFrom == null) {
            throw new IllegalArgumentException("Null argument");
        }
        int ordinal = this.ordinal();
        int fromOrdinal = convertFrom.ordinal();
        int diff = ordinal - fromOrdinal;
        return (float)(number * Math.pow(1000, -diff));
    }
}

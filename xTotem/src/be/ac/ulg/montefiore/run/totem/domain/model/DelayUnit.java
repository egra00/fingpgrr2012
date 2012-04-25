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
 *
 */

/**
 * An enum class that expresses delay units.
 *
 * <p>Creation date: 06-avr.-07
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public enum DelayUnit {
    DEFAULT_UNIT, NS, µS, MS, S;
    
    /**
     * Converts a float in the unit given as parameter to the unit of this enum.
     * @param convertFrom The unit in which <code>number</code> must be interpreted
     * @param number The float to convert
     * @return the float converted in the unit of this object
     */
    public float convert(DelayUnit convertFrom, float number) {
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

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

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
* Manipulate floating-point (float) numbers.
*
* <p>Creation date: 4 sept. 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class FloatingPointArithmetic {

    private final static Logger logger = Logger.getLogger(FloatingPointArithmetic.class);

    /**
     * Return the exponent of a float.
     * @param value
     * @return
     */
    public static int getExponent(float value) {

        int f = Float.floatToIntBits(value);

        return (f >>> 23)-127;
    }

    /**
     * returns the mantisse of a float
     * @param value
     * @return
     */
    public static float getMantisse(float value) {

        double d = (double)value / Math.pow(2, getExponent(value));

        return (float)d;
    }

    /**
     * Returns a string representing the bits of a float, in a readable format.
     * @param value
     * @return
     */
    public static String getBitString(float value) {
        int numberInt = Float.floatToIntBits(value);

        String base = Integer.toBinaryString(numberInt);

        StringBuffer sb = new StringBuffer(base);

        int nb = 32 - sb.length();
        for (int i = 0; i < nb; i++)
            sb.insert(0, "0");

        int offset = 1;
        sb.insert(offset, " | ");
        offset += 3;

        offset+=4;
        sb.insert(offset, " ");
        offset++;

        offset+=4;
        sb.insert(offset, " | ");
        offset += 3;

        offset += 3;

        for (int i = 0; i < 5; i++) {
            sb.insert(offset, " ");
            offset++;
            offset+=4;
        }

        return sb.toString();
    }

    /**
     * Round a small number so that it have the same precision of a big number.<br/>
     * If we know <code>x <= bignumber</code> and <code>b1 + b2 + ... + bn = x</code> then
     * if all <code>bi</code> are rounded relative to the big number, we have <code>x - b1 - b2 - ... - bn = 0</code>.<br/>
     * This is not the case if the numbers are not rounded, due to floating point lack of precision.
     * @param bigNumber
     * @param smallNumber
     * @return
     */
    public static float round(float bigNumber, float smallNumber) {
        // get the exponent of the big number
        int bigInt = Float.floatToIntBits(bigNumber);
        int gd = (bigInt >>> 23)-127;

        // get the exponent of the small number
        int smallInt = Float.floatToIntBits(smallNumber);

        int pt = (smallInt >>> 23)-127;

        // get offset
        int off = gd - pt;
        if (off > 23) {
            //System.out.println("Number rounds to 0. Nb bits to remove: " + off);
            logger.debug("Number rounds to 0. Nb bits to remove: " + off);
            return 0;
        }

        int result = (smallInt >> off) << off;

        return Float.intBitsToFloat(result);

    }
}

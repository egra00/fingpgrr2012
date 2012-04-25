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

import java.util.Random;

/*
 * Changes:
 * --------
 *
 */

/**
 * IdGenerator is a singleton used to generate unique id.
 * It uses the java.util.Random random number generator.
 *
 * <p>Creation date: 02-Feb-2005 11:05:40
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class IdGenerator {

    private static IdGenerator instance = null;
    private static Random generator;

    /**
     * Private constructor
     */
    private IdGenerator() {
        generator = new Random(System.currentTimeMillis());
    }

    /**
     * Get IdGenerator unique instance
     *
     * @return
     */
    public static IdGenerator getInstance() {
        if (instance == null) {
            instance = new IdGenerator();
        }
        return instance;
    }


    /**
     * Return a int Id uniformely distributed between 0 and Integer.MAX_VALUE
     *
     * @return
     */
    public int generateIntId() {
        return generator.nextInt(Integer.MAX_VALUE);
    }

    /**
     * Return a int Id uniformely distributed between 0 and n (exclusive)
     *
     * @param n : max int Id
     * @return
     */
    public int generateIntId(int n) {
        return generator.nextInt(n);
    }

    /**
     * Return a String id composed by the prefix given and a random number between 0 and Integer.MAX_VALUE
     *
     * @return String id
     */
    public String generateStringId(String prefix) {
        StringBuffer sb = new StringBuffer(prefix);
        sb.append(generateIntId());
        return sb.toString();
    }

}

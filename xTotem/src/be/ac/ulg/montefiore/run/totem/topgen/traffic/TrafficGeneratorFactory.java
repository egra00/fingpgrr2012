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
package be.ac.ulg.montefiore.run.totem.topgen.traffic;

import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

import java.util.List;
import java.util.HashMap;

/*
* Changes:
* --------
*
*/

/**
*
* <p>Creation date: 26/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class TrafficGeneratorFactory {

    private static HashMap<String, TrafficGeneratorInterface> instances = new HashMap<String, TrafficGeneratorInterface>();

    /**
     * Create a generator.
     * @param generator the generator type. Can be "Random", "Gravity" or "Constant".
     * @return
     * @throws InvalidParameterException if the generator is unknown
     */
    public static TrafficGeneratorInterface createGenerator(String generator) throws InvalidParameterException {
        if (generator.equalsIgnoreCase("Random")) {
            return new RandomTrafficGenerator();
        } else if (generator.equalsIgnoreCase("Gravity")) {
            return new GravityTrafficGenerator();
        } else if (generator.equalsIgnoreCase("Constant")) {
            return new ConstantTrafficGenerator();
        }

        throw new InvalidParameterException("Traffic generator unknown: " + generator);
    }

    /**
     * Create the default generator (RANDOM).
     * @return
     */
    public static TrafficGeneratorInterface createDefaultGenerator() {
        try {
            return createGenerator("Random");
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getAvailableGenerators() {
        return new String[] { "Random", "Gravity", "Constant" };
    }

    /**
     * Returns a list of parameters that can be used for the given generator (call to {@link TrafficGeneratorInterface#getAvailableParameters()} ).
     * @param generator
     * @return
     */
    public static List<ParameterDescriptor> getParameters(String generator) {
        if (instances.get(generator) == null) {
            TrafficGeneratorInterface gen = null;
            try {
                gen = createGenerator(generator);
                instances.put(generator, gen);
                return gen.getAvailableParameters();
            } catch (InvalidParameterException e) {
                e.printStackTrace();
                return null;
            }
        }
        return instances.get(generator).getAvailableParameters();
    }

}

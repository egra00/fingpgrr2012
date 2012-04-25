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
package be.ac.ulg.montefiore.run.totem.visualtopo.graph;

import java.awt.*;

/*
* Changes:
* --------
* - 20-Mar.-2006: add setColor and setName methods (GMO).
*/

/**
 * A ColorLegend is a set of colors associated with names.
 * The ColorLegend associates colors with floats.
 * <p/>
 * <p>Creation date: 15 mars 2006
 *
 * @see LinkColorShower
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public interface ColorLegend {

    /**
     * Returns the color for the given float value.
     * @param value
     * @return
     */
    public Color getColor(float value);

    /**
     * Returns the color at specified index.
     * @param index
     * @return
     */
    public Color getColor(int index);

    /**
     * Returns the number of colors.
     * @return
     */
    public int getNbColors();

    /**
     * Returns the legend associated with the color at specified index.
     * The name of the color should reflect its use (ex: "10-20%")
     * @param index
     * @return
     */
    public String getName(int index);

    /**
     * Sets the color <code>color</code> at index <code>index</code>
     * @param index
     * @param color
     */
    public void setColor(int index, Color color);

    /**
     * Sets the name of the color at index <code>index</code>
     * @param index
     * @param name
     */
    public void setName(int index, String name);
}

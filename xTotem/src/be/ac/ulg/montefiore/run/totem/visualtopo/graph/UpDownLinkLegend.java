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
*
*/

/**
* Legend for representing link status (up, down).
*
* <p>Creation date: 17 mars 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class UpDownLinkLegend implements ColorLegend {
    private Color[] colors = {Color.red, Color.green};//, Color.black, Color.blue};
    private String[] names =  {"Link Down", "Link Up"};//, "Node Down", "Node Up"};

    private Color errorColor = Color.black;

    public Color getColor(float value) {
        try {
            return colors[(int)value];
        } catch (Exception e) {
            return errorColor;
        }
    }

    public Color getColor(int index) {
        return colors[index];
    }

    public int getNbColors() {
        return colors.length;
    }

    public String getName(int index) {
        return names[index];
    }

    public void setColor(int index, Color color) {
        colors[index] = color;
    }

    public void setName(int index, String name) {
        names[index] = name;
    }
}

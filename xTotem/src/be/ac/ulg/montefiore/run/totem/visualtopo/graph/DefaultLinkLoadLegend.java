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
* 03-Apr.-2006 : change colors, add a color for > 100% (GMO)
* 11-Jul-2006 : fix bug for very big load (GMO).
*/

/**
* Legend representing a percentage of load in 12 colors.
*
* <p>Creation date: 16 mars 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class DefaultLinkLoadLegend implements ColorLegend {
    private String[] names = {"Link Down",
                                       "< 5%",
                                       "5 - 10 %",
                                       "10 - 20 %",
                                       "20 - 30 %",
                                       "30 - 40 %",
                                       "40 - 50 %",
                                       "50 - 60 %",
                                       "60 - 70 %",
                                       "70 - 80 %",
                                       "80 - 90 %",
                                       "90 - 100 %",
                                       "> 100 %"};

    private Color[] colors = {Color.BLACK,
                              new Color(0, 255, 0),
                              new Color(0, 255, 128),
                              new Color(0, 255, 255),
                              new Color(0, 170, 255),
                              new Color(0, 90, 255),
                              new Color(0, 0, 255),
                              new Color(128, 0, 255),
                              new Color(175, 0, 255),
                              new Color(255, 0, 255),
                              new Color(255, 0, 170),
                              new Color(255, 0, 90),
                              new Color(255, 0, 0)};

    private Color errorColor = Color.BLACK;

    public DefaultLinkLoadLegend() {
    }

    public Color getColor(float value) {
        if (value < 0) {
            return errorColor;
        }
        value *= 10;
        if (value < 0.5)
            return colors[1];
        else if (value > 10)
            return colors[12];
        int divint = (int) value;
        return colors[divint + 2];
    }

    public Color getColor(int index) {
        return colors[index];
    }

    public Color getErrorColor() {
        return errorColor;
    }

    public void setErrorColor(Color color) {
        errorColor = color;
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

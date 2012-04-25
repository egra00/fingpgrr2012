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

import javax.swing.*;
import java.awt.*;

/*
* Changes:
* --------
*
*/

/**
* This class implements a icon that display a simple colored square
*
* <p>Creation date: 16 mars 2006
*
* @author Olivier Materne (O.Materne@student.ulg.ac.be)
*/
public class ColorSquare implements Icon {
    private Color color;

    /**
     * Constructor.  A square of the given color
     *
     * @param color The color with which the square will be drawn
     */
    public ColorSquare(Color color) {
        this.color = color;
    }


    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(Color.BLACK);
        g.fillRect(x, y, getIconWidth(), getIconHeight());
        g.setColor(color);
        g.fillRect(x + 2, y + 2, getIconWidth() - 4, getIconHeight() - 4);
    }

    /**
     * Return the icon width (allways 11 here)
     *
     * @return the icon width
     */
    public int getIconWidth() {
        return 11;
    }


    /**
     * Return the icon height (allways 11 here)
     *
     * @return the icon height
     */
    public int getIconHeight() {
        return 11;
    }
}

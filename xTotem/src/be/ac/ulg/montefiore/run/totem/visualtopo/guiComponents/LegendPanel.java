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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents;

import be.ac.ulg.montefiore.run.totem.visualtopo.graph.ColorLegend;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.ColorSquare;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.DefaultLinkLoadLegend;

import javax.swing.*;
import java.awt.*;

/*
* Changes:
* --------
*
*/

/**
 * Graphical representation of {@link ColorLegend}.
 * TODO: get default legend from global property
 * <p/>
 * <p>Creation date: 16 mars 2006
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class LegendPanel extends JPanel {
    ColorLegend legend = null;

    /**
     * Creates the panel with a new DefautlLinkLoadLegend
     */
    public LegendPanel() {
        super(new GridLayout(0, 1, 0, 3));
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), "Legend"));
        legend = new DefaultLinkLoadLegend();
        rebuild();
    }

    /**
     * Creates the panel for a specific legend
     * @param legend
     */
    public LegendPanel(ColorLegend legend) {
        this();
        this.legend = legend;
    }

    private void rebuild() {
        removeAll();
        if (legend == null) return;
        for (int i = 0; i < legend.getNbColors(); i++) {
            JLabel label = new JLabel(legend.getName(i));
            label.setIcon(new ColorSquare(legend.getColor(i)));
            add(label);
        }
        revalidate();
    }

    /**
     * Sets the legend that the panel represents.
     * @param legend
     */
    public void setLegend(ColorLegend legend) {
        this.legend = legend;
        rebuild();
    }
}

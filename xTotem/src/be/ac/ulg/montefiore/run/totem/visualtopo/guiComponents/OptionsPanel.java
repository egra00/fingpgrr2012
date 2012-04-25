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

import be.ac.ulg.montefiore.run.totem.visualtopo.graph.LinkLabeller;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.DefaultLinkLoadLegend;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
 * Changes:
 * --------
 *
 * - 03-May-2005: Fix javadoc (JL).
 * - 08-Dec.-2005: added the possibility to display Link Metric and Link TE-Metric (GMO).
 * - 20-Mar.-2006: extract class LegendEditor, ColorMeaningPanel and LegendPanel (GMO).
 * - 20-Mar.-2006: change layout (GMO).
 */

/**
 * This class creates and manages the right OptionPanel of the GUI.
 * It is also resposible for diplaying a panel that allow to choose Links colors
 * <p/>
 * <p>Creation date: 15-Feb-2005
 *
 * @author Olivier Materne (O.Materne@student.ulg.ac.be)
 */
public class OptionsPanel extends JPanel {

    private final String[] labellerNames = {"None",
                                      "Bandwidth",
                                      "Reserved Bandwidth",
                                      "Metric",
                                      "TE Metric"

    };
    private final int[] labellerCodes = {LinkLabeller.NO_LABEL,
                                   LinkLabeller.LINK_BW,
                                   LinkLabeller.LINK_RESERVED_BW,
                                   LinkLabeller.LINK_METRIC,
                                   LinkLabeller.LINK_TE_METRIC
    };                                                

    JComboBox labellerChooser;
    private ColorMeaningPanel linksShowsPanel = null;
    private LegendPanel legendPanel = null;


    /**
     * Constructor
     */
    public OptionsPanel() {
        super();

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Info above Link:"), BorderLayout.NORTH);
        labellerChooser = new JComboBox(labellerNames);
        labellerChooser.addActionListener(new ChoseLabellerListener());
        panel.add(labellerChooser, BorderLayout.CENTER);

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;

        add(panel, c);

        JPanel emptyPanel = new JPanel();
        c.weighty = 1.0;
        add(emptyPanel, c);
        c.weighty = 0.0;
        linksShowsPanel = new ColorMeaningPanel();
        add(linksShowsPanel, c);
        legendPanel = new LegendPanel(linksShowsPanel.getCurrentLegend());
        add(legendPanel, c);
    }

    public ColorMeaningPanel getColorMeaningPanel() {
        return linksShowsPanel;
    }

    public LegendPanel getLegendPanel() {
        return legendPanel;
    }

    /**
     * The ActionListener class responsible for handling Link labels option changes
     */
    class ChoseLabellerListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            LinkLabeller.getInstance().select(labellerCodes[labellerChooser.getSelectedIndex()]);
        }
    }

}

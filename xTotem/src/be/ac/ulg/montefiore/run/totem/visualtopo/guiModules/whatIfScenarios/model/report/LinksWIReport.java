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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.report;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.domainTables.DomainTable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 04-May-2007: The JTable is now a DomainTable (GMO)
* - 31-May-2007: Add toString() method, use getShortName() instead of getName() in the report (GMO)
* - 28-Feb-2008: Possibility to add some extra stat panels (GMO)
*/

/**
* An abstract report class that collects some data about links, displays agregate information (max, mean, percentile,...)
* and a table where each row correspond to a link. This table uses a {@link LinksWITableModel}.
* The panel also contains a button to show charts, if charts is desired (see {@link #hasCharts()} and ç{@link #showCharts()}  methods).
*
* <p>Creation date: 23/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public abstract class LinksWIReport implements WIReport {
    private final static Logger logger = Logger.getLogger(LinksWIReport.class);

    protected double[] initialData;
    protected double[] finalData;

    protected Domain domain;

    private List<JPanel> additionalstatPanels;

    static protected NumberFormat formatter = new DecimalFormat();
    static {
        formatter.setMaximumFractionDigits(4);
    }

    protected LinksWIReport(Domain domain) {
        this.domain = domain;
    }

    public JPanel getPanel() {
        final JPanel generalPanel = new JPanel(new BorderLayout());
        final JPanel nPanel = new JPanel(new BorderLayout());
        final JPanel statPanel = new JPanel(new GridBagLayout());
        final JPanel chartsBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        nPanel.add(statPanel, BorderLayout.NORTH);
        nPanel.add(chartsBtnPanel, BorderLayout.SOUTH);
        nPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createBevelBorder(1), ""));
        generalPanel.add(nPanel, BorderLayout.WEST);

        if (hasCharts()) {
            JButton btn = new JButton("Show charts");
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showCharts();
                }
            });
            chartsBtnPanel.add(btn);
        }

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridx = 0;
        c.gridy = 0;

        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.gridy = 0;
        c1.insets = new Insets(0, 0, 5, 10);
        c1.anchor = GridBagConstraints.LINE_START;
        c1.weightx = 0;
        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 1;
        c2.gridy = 0;
        c2.weightx  = 1.0;
        GridBagConstraints c3 = new GridBagConstraints();
        c3.gridx = 0;
        c3.gridy = 1;
        c3.insets = new Insets(0, 0, 5, 10);
        c3.weightx = 0;
        c3.anchor = GridBagConstraints.LINE_START;
        GridBagConstraints c4 = new GridBagConstraints();
        c4.gridx = 1;
        c4.gridy = 1;
        c4.weightx = 1.0;

        final JPanel maxLoad = new JPanel(new GridBagLayout());
        statPanel.add(maxLoad, c);
        c.gridy++;
        maxLoad.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Maximum " + getShortName()));

        if (initialData.length != domain.getNbLinks()) {
            logger.error("Stats may be inaccurate as link ids are not consecutive");
        }

        final JLabel label4 = new JLabel("Max " + getShortName() + " Before scenario");
        maxLoad.add(label4, c1);
        final JLabel label1 = new JLabel(formatter.format(DoubleArrayAnalyse.getMaximum(initialData)));
        maxLoad.add(label1, c2);
        final JLabel label3 = new JLabel("Max " + getShortName() + " After scenario");
        maxLoad.add(label3, c3);
        final JLabel label2 = new JLabel(formatter.format(DoubleArrayAnalyse.getMaximum(finalData)));
        maxLoad.add(label2, c4);

        final JPanel meanLoad = new JPanel(new GridBagLayout());
        statPanel.add(meanLoad, c);
        c.gridy++;
        meanLoad.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Mean Value"));

        final JLabel label8 = new JLabel("Mean " + getShortName() + " Before scenario");
        meanLoad.add(label8, c1);
        final JLabel label5 = new JLabel(formatter.format(DoubleArrayAnalyse.getMeanValue(initialData)));
        meanLoad.add(label5, c2);
        final JLabel label7 = new JLabel("Mean " + getShortName() + " After scenario");
        meanLoad.add(label7, c3);
        final JLabel label6 = new JLabel(formatter.format(DoubleArrayAnalyse.getMeanValue(finalData)));
        meanLoad.add(label6, c4);

        final JPanel stdDev = new JPanel(new GridBagLayout());
        statPanel.add(stdDev, c);
        c.gridy++;
        stdDev.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Standard Deviation"));

        final JLabel label12 = new JLabel("Standard Deviation Before scenario");
        stdDev.add(label12, c1);
        final JLabel label9 = new JLabel(formatter.format(DoubleArrayAnalyse.getStandardDeviation(initialData)));
        stdDev.add(label9, c2);
        final JLabel label11 = new JLabel("Standard Deviation After scenario");
        stdDev.add(label11, c3);
        final JLabel label10 = new JLabel(formatter.format(DoubleArrayAnalyse.getStandardDeviation(finalData)));
        stdDev.add(label10, c4);

        final JPanel percentile = new JPanel(new GridBagLayout());
        statPanel.add(percentile, c);
        c.gridy++;
        percentile.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Percentile"));

        final JLabel label16 = new JLabel("Percentile 10 Before scenario");
        percentile.add(label16, c1);
        final JLabel label13 = new JLabel(formatter.format(DoubleArrayAnalyse.getPercentile10(initialData)));
        percentile.add(label13, c2);
        final JLabel label15 = new JLabel("Percentile 10 After scenario");
        percentile.add(label15, c3);
        final JLabel label14 = new JLabel(formatter.format(DoubleArrayAnalyse.getPercentile10(finalData)));
        percentile.add(label14, c4);

        if (additionalstatPanels != null) {
            for (JPanel panel : additionalstatPanels) {
                statPanel.add(panel, c);
                c.gridy++;
            }
        }

        JTable linksTable = new DomainTable(new LinksWITableModel(domain, initialData, finalData));

        final JScrollPane scrollPane1 = new JScrollPane(linksTable);
        generalPanel.add(scrollPane1, BorderLayout.CENTER);

        return generalPanel;
    }

    protected void addStatPanel(JPanel panel) {
        if (additionalstatPanels == null) {
            additionalstatPanels = new ArrayList<JPanel>(2);
        }
        additionalstatPanels.add(panel);
    }

    /**
     * Must return true if a chart can be display by the {@link #showCharts()} method. In this case a button will
     * be added to the panel.
     * @return
     */
    protected abstract boolean hasCharts();

    /**
     * Show the charts. This method is called only if {@link #hasCharts()} returns true.
     */
    protected abstract void showCharts();

    public String toString() {
        return getName();
    }
}

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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.stats;

import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/*
* Changes:
* --------
* - 17-Jan-2007: add percentile statistics, statistics now display in percent (GMO)
* - 18-Jan-2007: Inner concrete classes, limit fraction digits to 3 for statistic values (GMO)
* - 13-Mar-2007: add Fortz (GMO)
*/

/**
 * Gives statistics about a domain
 * <p/>
 * <p>Creation date: 23 mars 2006
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public abstract class NetworkStatTable extends JTable {
    public NetworkStatTable() {
        super(new AbstractTableModel() {
            Object[][] data = new Object[9][2];

            public String getColumnName(int col) {
                switch (col) {
                    case 0:
                        return "Stat Name";
                    case 1:
                        return "Value";
                    default:
                        return "Unknown column";
                }
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                return data[rowIndex][columnIndex];
            }

            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                data[rowIndex][columnIndex] = aValue;
            }

            public int getRowCount() {
                return data.length;
            }

            public int getColumnCount() {
                return data[0].length;
            }

            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return String.class;
                    case 1:
                        //column 1 is rendered as a string since default renderer for double displays too few decimals.
                        return String.class;
                        //return Double.class;
                    default:
                        return Object.class;
                }
            }
        });
    }

    /**
     * index i corresponds to link domain.getAllLinks().get(i)
     * @param load
     * @param capacities
     */
    protected void fill(double[] load, double[] capacities) {
        if (load.length != capacities.length)
            throw new IllegalArgumentException("load and capacity arrays must have the same size");

        double[] data = new double[load.length];
        for (int i = 0; i < load.length; i++) {
            data[i] = load[i] / capacities[i];
        }

        NumberFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits(3);

        String value;

        setValueAt("Max Util", 0, 0);
        value = nf.format(DoubleArrayAnalyse.getMaximum(data)*100) + " %";
        setValueAt(value, 0, 1);

        setValueAt("Mean Util", 1, 0);
        value = nf.format(DoubleArrayAnalyse.getMeanValue(data)*100) + " %";
        setValueAt(value, 1, 1);

        setValueAt("Std Dev Util", 2, 0);
        value = nf.format(DoubleArrayAnalyse.getStandardDeviation(data)*100) + " %";
        setValueAt(value, 2, 1);

        setValueAt("Percentile 10 Util", 3, 0);
        value = nf.format(DoubleArrayAnalyse.getPercentile(data, 90)*100) + " %";
        setValueAt(value, 3, 1);

        setValueAt("Percentile 20 Util", 4, 0);
        value = nf.format(DoubleArrayAnalyse.getPercentile(data, 80)*100) + " %";
        setValueAt(value, 4, 1);

        setValueAt("Percentile 30 Util", 5, 0);
        value = nf.format(DoubleArrayAnalyse.getPercentile(data, 70)*100) + " %";
        setValueAt(value, 5, 1);

        setValueAt("Percentile 50 Util", 6, 0);
        value = nf.format(DoubleArrayAnalyse.getPercentile(data, 50)*100) + " %";
        setValueAt(value, 6, 1);

        setValueAt("Percentile 90 Util", 7, 0);
        value = nf.format(DoubleArrayAnalyse.getPercentile(data, 10)*100) + " %";
        setValueAt(value, 7, 1);

        setValueAt("Fortz", 8, 0);
        value = nf.format(DoubleArrayAnalyse.getFortz(load, capacities));
        setValueAt(value, 8, 1);

    }
}


class ReservationNetworkStatTable extends NetworkStatTable {
    public ReservationNetworkStatTable(Domain domain) {
        double[] load  = new double[domain.getNbLinks()];
        double[] cap  = new double[domain.getNbLinks()];
        int i = 0;
        for (Link lnk : domain.getAllLinks()) {
            load[i] = lnk.getTotalReservedBandwidth();
            cap[i] = lnk.getBandwidth();
            i++;
        }
        fill(load, cap);
    }
}

class ReservationCTNetworkStatTable extends NetworkStatTable {
    public ReservationCTNetworkStatTable(Domain domain, int ct) {
        double[] load  = new double[domain.getNbLinks()];
        double[] cap  = new double[domain.getNbLinks()];
        int i = 0;
        for (Link lnk : domain.getAllLinks()) {
            load[i] = lnk.getReservedBandwidthCT(ct);
            cap[i] = lnk.getBCs()[ct];
            i++;
        }
        fill(load, cap);
    }
}

/**
* Give statistics calculated from utilisation of a LinkLoadComputer object.
*
* <p>Creation date: 23 mars 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/
class LoadNetworkStatTable extends NetworkStatTable {
    public LoadNetworkStatTable(LinkLoadComputer llc) {
        double[] load  = new double[llc.getDomain().getNbLinks()];
        double[] cap  = new double[llc.getDomain().getNbLinks()];

        int i = 0;
        for (Link lnk : llc.getDomain().getAllLinks()) {
            load[i] = llc.getData().getLoad(lnk);
            cap[i] = lnk.getBandwidth();
            i++;
        }
        fill(load, cap);
    }
}


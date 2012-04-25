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

import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import edu.uci.ics.jung.graph.ArchetypeEdge;
import edu.uci.ics.jung.graph.decorators.EdgeStringer;

/*
 * Changes:
 * --------
 * - 8-Dec.-2005 : added the possibility to display Link metric et Link TE-metric (GMO)
 * - 30-Jun-2006 : adapt methods to match EdgeStringer interface of Jung new version (GMO) 
 *
 */

/**
 * A small class that allows to draw a label below a link.
 * <p/>
 * <p>Creation date: 15-Feb-2005
 *
 * @author Olivier Materne (O.Materne@student.ulg.ac.be)
 */
public class LinkLabeller implements EdgeStringer {
    public final static int NO_LABEL = 0;
    public final static int LINK_BW = 1;
    public final static int LINK_RESERVED_BW = 2;
    public final static int LINK_METRIC = 3;
    public final static int LINK_TE_METRIC = 4;

    static private EdgeStringer labeller = null;
    static private LinkLabeller linkLabeller;

    private LinkLabeller() {
    }


    /**
     * get an instance of the LinkLabeller
     *
     * @return a LinkLabeller
     */
    public static LinkLabeller getInstance() {
        if (linkLabeller == null) {
            linkLabeller = new LinkLabeller();
            linkLabeller.select(NO_LABEL);
        }
        return linkLabeller;
    }


    /**
     * Select which kind of information will be displayed
     *
     * @param select
     */
    public void select(int select) {
        switch (select) {
            case NO_LABEL:
                labeller = new NoLabelStringer();
                break;
            case LINK_BW:
                labeller = new LinkBWStringer();
                break;
            case LINK_RESERVED_BW:
                labeller = new LinkReservedBWStringer();
                break;
            case LINK_METRIC:
                labeller = new LinkMetricStringer();
                break;
            case LINK_TE_METRIC:
                labeller = new LinkTEMetricStringer();
                break;
        }
        GraphManager.getInstance().repaint();
    }


    /**
     * This methode compute and return the label that will be associate with the Edge
     *
     * @param e an ArchetypeEdge
     * @return returns the label to be showed below the Edge
     */
    public String getLabel(ArchetypeEdge e) {
        return labeller.getLabel(e);
    }

    /************************************
     *   INNER CLASSES
     *
     ************************************/


    /**
     * An inner class for displaying no info
     */
    class NoLabelStringer implements EdgeStringer {
        public String getLabel(ArchetypeEdge edge) {
            return null;
        }
    }


    /**
     * An inner class for displaying BW as link info
     */
    class LinkBWStringer implements EdgeStringer {
        public String getLabel(ArchetypeEdge e) {
            try {
                return String.valueOf(((Link) e.getUserDatum(MyVisualizationViewer.TKEY)).getBandwidth());
            } catch (Exception ex) {
                //if an exception is catch do not label the link
                return null;
            }
        }
    }


    /**
     * An inner class for displaying reserved BW as link info
     */
    class LinkReservedBWStringer implements EdgeStringer {
        public String getLabel(ArchetypeEdge e) {
            try {
                return String.valueOf(((Link) e.getUserDatum(MyVisualizationViewer.TKEY)).getReservedBandwidth());
            } catch (Exception ex) {
                //if an exception is catch do not label the link
                return null;
            }
        }
    }

    /**
     * An inner class for displaying Igp metric as link info
     */
    class LinkMetricStringer implements EdgeStringer {
        public String getLabel(ArchetypeEdge e) {
            try {
                return String.valueOf(((Link) e.getUserDatum(MyVisualizationViewer.TKEY)).getMetric());
            } catch (Exception ex) {
                //if an exception is catch do not label the link
                return null;
            }
        }
    }

    /**
     * An inner class for displaying Igp TE metric as link info
     */
    class LinkTEMetricStringer implements EdgeStringer {
        public String getLabel(ArchetypeEdge e) {
            try {
                return String.valueOf(((Link) e.getUserDatum(MyVisualizationViewer.TKEY)).getTEMetric());
            } catch (Exception ex) {
                //if an exception is catch do not label the link
                return null;
            }
        }
    }

}

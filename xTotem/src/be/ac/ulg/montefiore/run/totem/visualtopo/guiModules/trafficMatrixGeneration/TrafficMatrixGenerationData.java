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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.trafficMatrixGeneration;

import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.TrafficMatrixFileType;

import java.util.HashMap;

/*
* Changes:
* --------
* JJ-MMM-YYY: <changes> (<Three-letter author acronym>) [Example : 9-May-2005: decreasing order by default for the fullmesh (FSK)]
*
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 18/09/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
class TrafficMatrixGenerationData {
    private String BGPRibDir = null;
    private String BGPRibFileSuffix = null;
    private boolean idNaming = false;
    private boolean isSetIdNaming = false;

    private TrafficMatrixFileType interTm = null;
    private String clusterFilename = null;
    private HashMap<String, String> prefixesMap = null;

    public String getBGPRibFileSuffix() {
        return BGPRibFileSuffix;
    }

    public void setBGPRibFileSuffix(String BGPRibFileSuffix) {
        this.BGPRibFileSuffix = BGPRibFileSuffix;
    }

    public boolean isSetBGPRibFileSuffix() {
        return BGPRibFileSuffix != null;
    }

    public String getBGPRibDir() {
        return BGPRibDir;
    }

    public void setBGPRibDir(String BGPRibDir) {
        this.BGPRibDir = BGPRibDir;
    }

    public boolean isSetBGPRibDir() {
        return BGPRibDir != null;
    }

    public String getClusterFilename() {
        return clusterFilename;
    }

    public void setClusterFilename(String clusterFilename) {
        this.clusterFilename = clusterFilename;
    }

    public boolean isSetClusterFilename() {
        return clusterFilename != null;
    }

    public boolean isIdNaming() {
        return idNaming;
    }

    public void setIdNaming(boolean idNaming) {
        this.idNaming = idNaming;
        isSetIdNaming = true;
    }

    public boolean isSetIdNaming() {
        return isSetIdNaming;
    }


    public TrafficMatrixFileType getInterTm() {
        return interTm;
    }

    public void setInterTm(TrafficMatrixFileType interTm) {
        this.interTm = interTm;
    }

    public boolean isSetInterTm() {
        return interTm != null;
    }

    public HashMap<String, String> getPrefixesMap() {
        return prefixesMap;
    }

    public void setPrefixesMap(HashMap<String, String> prefixesMap) {
        this.prefixesMap = prefixesMap;
    }

    public boolean isSetPrefixesMap() {
        return prefixesMap != null;
    }
}

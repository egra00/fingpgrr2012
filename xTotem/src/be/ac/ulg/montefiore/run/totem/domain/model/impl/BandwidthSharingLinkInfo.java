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
package be.ac.ulg.montefiore.run.totem.domain.model.impl;

/*
* Changes:
* --------
*
*/

/**
 * This class contains all the information concerning one link (Lij).<br>
 * This info is :
 * <ul>
 * <li> the sum of the bandwidth of the primary LSP that pass on this link (Pij). </li>
 * <li> the bandwidth that is free on this link when another link fail (Fij). </li>
 * <li> the bandwidth that is reserved on this link when another link fail (Bij).</li>
 * </ul>
 * <p/>
 * <p>Creation date: 14/12/2007
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)B
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class BandwidthSharingLinkInfo implements Cloneable {
    private int[][] linksInNode; // topology information. Shared by all BandwidthSharingLinkInfo

    // This link is the Lij link

    private int id, maxLinkId;
    private float bandwidth;
    private float Pij; // Pij is the sum of the BW of all the primary lsps that pass on this link (Lij)
    private long nbPrim; // Number of primary LSPs on this link
    private long nbBackups; // Total number of backups on this link
    private float[] Fij; // Fij contains the BW that is freed on this link (Lij) when another link fails.
    private float[] Bij; // Bij contains the BW that is reserved on this link (Lij) when another link fails.
    private long[] nbBackupsLij; // Number of backups LSPs on this link when another link fails.

    public BandwidthSharingLinkInfo(int id, float bandwidth, int maxLinkId, int[][] linksInNode) {
        this.id = id;
        this.bandwidth = bandwidth;
        this.maxLinkId = maxLinkId;
        Pij = 0;
        nbPrim = 0;
        nbBackups = 0;
        Fij = new float[maxLinkId];
        Bij = new float[maxLinkId];
        nbBackupsLij = new long[maxLinkId];
        for (int i = 0; i < maxLinkId; i++) {
            Fij[i] = 0;
            Bij[i] = 0;
            nbBackupsLij[i] = 0;
        }
        this.linksInNode = linksInNode;
    }

    public int getId() {
        return id;
    }

    public float getBw() {
        return bandwidth;
    }

    public long getNbPrim() {
        return nbPrim;
    }

    public long getNbBackups() {
        return nbBackups;
    }

    public long getMaxNbBackupsLij() {
        long tmp = 0;
        for (int i = 0; i < maxLinkId; i++) {
            if (nbBackupsLij[i] > tmp) {
                tmp = nbBackupsLij[i];
            }
        }
        for (int i = 0; i < linksInNode.length; i++) {
            long nbBackupNi = 0;
            for (int j = 0; j < linksInNode[i].length; j++) {
                nbBackupNi += nbBackupsLij[linksInNode[i][j]];
                if (linksInNode[i][j] == this.getId()) {
                    nbBackupNi = 0;
                    break;
                }
            }
            if (nbBackupNi > tmp) {
                tmp = nbBackupNi;
            }
        }
        return tmp;
    }

    public float getPij() {
        return Pij;
    }

    public float getRij() {
        float tmp = 0;
        for (int i = 0; i < maxLinkId; i++) {
            if ((Bij[i] - Fij[i]) > tmp) {
                //System.out.println("On link " + getId() + ", failure of link " + i + " BIJ : " + Bij[i] + " and FIJ : " + Fij[i]);
                tmp = (Bij[i] - Fij[i]);
            }
        }
        for (int i = 0; i < linksInNode.length; i++) {
            float nodeBij = 0;
            float nodeFij = 0;
            if (linksInNode[i] == null) continue;
            for (int j = 0; j < linksInNode[i].length; j++) {
                //try {
                //    convertor.getLinkId(linksInNode[i][j]);
                    nodeBij += Bij[linksInNode[i][j]];
                    nodeFij += Fij[linksInNode[i][j]];
                    if (linksInNode[i][j] == this.getId()) {
                        nodeFij = 0;
                        nodeBij = 0;
                        break;
                    }
                //} catch (LinkNotFoundException e) {
                //    e.printStackTrace();
                //}
            }
            if ((nodeBij - nodeFij) > tmp) {
                tmp = (nodeBij - nodeFij);
            }
        }
        //System.out.println("tmp = " + tmp + " and Pij = " + Pij);
        return (tmp + Pij);
    }

    public float getMaxBij() {
        float tmp = 0;
        for (int i = 0; i < maxLinkId; i++) {
            if (Bij[i] > tmp) {
                tmp = Bij[i];
            }
        }
        for (int i = 0; i < linksInNode.length; i++) {
            float nodeBij = 0;
            for (int j = 0; j < linksInNode[i].length; j++) {
                nodeBij += Bij[linksInNode[i][j]];
                if (linksInNode[i][j] == this.getId()) {
                    nodeBij = 0;
                    break;
                }
            }
            if (nodeBij > tmp) {
                tmp = nodeBij;
            }
        }
        return tmp;
    }

    public void addPij(float value) {
        Pij += value;
        nbPrim++;
    }

    public void subPij(float value) {
        Pij -= value;
        nbPrim--;
    }

    public float getFij(int index) {
        if ((index < maxLinkId) && (index >= 0)) {
            return Fij[index];
        } else {
            return -1;
        }
    }

    public void addFij(float value, int index) {
        if ((index < maxLinkId) && (index >= 0)) {
            Fij[index] += value;
            //System.out.println("On link " + getId() + " failure of link " + index + " Fij = " + Fij[index]);
        } else {
            // there is a problem !!
            System.out.println("Compute sharing problem !");
        }
    }

    public void subFij(float value, int index) {
        if ((index < maxLinkId) && (index >= 0)) {
            Fij[index] -= value;
            //System.out.println("On link " + getId() + " failure of link " + index + " Fij = " + Fij[index]);
        } else {
            // there is a problem !!
            System.out.println("Compute sharing problem !");
        }
    }

    public float getBij(int index) {
        if ((index < maxLinkId) && (index >= 0)) {
            return Bij[index];
        } else {
            return -1;
        }
    }

    public void addBij(float value, int index) {
        if ((index < maxLinkId) && (index >= 0)) {
            Bij[index] += value;
            nbBackups++;
            nbBackupsLij[index]++;
        } else {
            // there is a problem !!
            System.out.println("Compute sharing problem !");
        }
    }

    public void subBij(float value, int index) {
        if ((index < maxLinkId) && (index >= 0)) {
            Bij[index] -= value;
            nbBackups--;
            nbBackupsLij[index]--;
        } else {
            // there is a problem !!
            System.out.println("Compute sharing problem !");
        }
    }

    protected BandwidthSharingLinkInfo clone() {
        try {
            BandwidthSharingLinkInfo clone = (BandwidthSharingLinkInfo)super.clone();
            clone.Bij = Bij.clone();
            clone.Fij = Fij.clone();
            clone.nbBackupsLij = nbBackupsLij.clone();
            //note: the linksInNode array is not cloned as it is shared (and accessed read-only)
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

}

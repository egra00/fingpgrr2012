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
package be.ac.ulg.montefiore.run.totem.repository.MplsRouting;

import org.apache.log4j.Logger;
import be.ac.ulg.montefiore.run.totem.repository.MultiCommodityFlow.MultiCommodityFlow;
import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;
import be.ac.ulg.montefiore.run.totem.core.Totem;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomainBuilder;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.util.FileFunctions;

import java.io.*;
import java.net.URL;

/*
 * Changes:
 * --------
 * 
 */

/**
 * Compute the optimal full mesh of LSPs using a MIP solver. This model used one LSP for each demand.
 * This problem with the unicity of the path is very complex and perhaps can takes a long time
 * before finding solutions on big networks.
 *
 * <p>Creation date: 25-Mar-2005 11:41:34
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class MplsRouting {

    private static Logger logger = Logger.getLogger(MplsRouting.class.getName());
    private final String defaultModelFileName = "/resources/modelAMPL/MplsRouting.mod";
    private final String tmpModelFile = "model.mod";

    private String glpsolBin;
    private String dataFileName;
    private String resultFileName;
    private String modelFileName;
    private boolean removeFile;

    public MplsRouting() {
        this.dataFileName = "mplsRouting.dat";
        this.resultFileName = "mplsRouting-result.txt";
        glpsolBin = PreferenceManager.getInstance().getPrefs().get("GLPSOL-BIN","glpsol");
        removeFile = true;
        modelFileName = PreferenceManager.getInstance().getPrefs().get("MPLS-ROUTING-MODEL",defaultModelFileName);
    }

    /**
     * Specify a data file and a result file
     *
     * @param dataFile
     * @param resultFile
     */
    public MplsRouting(String dataFile, String resultFile) {
        this();
        this.dataFileName = dataFile;
        this.resultFileName = resultFile;
        removeFile = false;
    }

    /**
     * Compute the link utilisation produce by the Mpls Routing using glpsol
     *
     * @param domain
     * @param tm
     * @return
     * @throws java.io.IOException
     * @throws be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException
     * @throws be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException
     */
    public float[] compute(Domain domain, TrafficMatrix tm)
            throws IOException, LinkNotFoundException, NodeNotFoundException {
        this.createDataFile(domain,tm);

        File file = new File(modelFileName);
        if (!file.exists()) {
            logger.debug("Cannot find Mpls Routing AMPL model in the file " + modelFileName);
            URL url = Totem.class.getResource(modelFileName);
            if (url == null) {
                logger.error("Cannot find Mpls Routing AMPL model in JAR : " + modelFileName);
                System.exit(0);
            }
            logger.info("Init Mpls Routing AMPL model from JAR with  : " + modelFileName);
            try {
                FileFunctions.copy(url,tmpModelFile);
                this.modelFileName = tmpModelFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("Init Mpls Routing AMPL model with  : " + modelFileName);
        }

        Process p = Runtime.getRuntime().exec(glpsolBin + " -m " + modelFileName + " -d "+ dataFileName + " -o " + resultFileName);
        // Wait for it to finish running
        try {
            p.waitFor();
        } catch (InterruptedException ie) {
            System.out.println(ie);
        }
        int ret = p.exitValue();
        if (ret != 0) {
            logger.error("ERROR with glpsol : " + ret);
            return null;
        }
        float[] linkLoad = readUtilizationFromResultFile(SimplifiedDomainBuilder.build(domain),resultFileName);
        if (removeFile) {
            File f = new File(dataFileName);
            if (f.exists())
                f.delete();
            f = new File(resultFileName);
            if (f.exists())
                f.delete();
        }
        File f = new File(tmpModelFile);
        if (f.exists())
            f.delete();

        return linkLoad;
    }

    /**
     * Create the data file
     *
     * @param domain
     * @param tm
     * @throws IOException
     * @throws NodeNotFoundException
     * @throws LinkNotFoundException
     */
    public void createDataFile(Domain domain, TrafficMatrix tm)
            throws IOException, NodeNotFoundException, LinkNotFoundException {
        SimplifiedDomain sDomain = SimplifiedDomainBuilder.build(domain);

        FileWriter fw = new FileWriter(dataFileName);
        BufferedWriter br = new BufferedWriter(fw);
        br.write("set VERTICES := ");
        int nbNodes = sDomain.getNbNodes();
        for (int i=0; i < nbNodes;i++) {
            if (sDomain.isNode(i)) {
                br.write("V" + i);
                if (i < nbNodes-1)
                    br.write(", ");
            }
        }
        br.write(";\n\n");

        br.write("set LINKS := ");
        SimplifiedDomain.Link[] linkList = sDomain.getLinks();
        int nbLinks = linkList.length;
        for (int i=0; i < nbLinks;i++) {
            if (linkList[i] != null) {
                br.write("L" + linkList[i].getId());
                if (i < nbLinks-1)
                    br.write(", ");
            }
        }
        br.write(";\n\n");


        br.write("param InLinks :=\n");
        for (int i=0; i < nbNodes;i++) {
            if (sDomain.isNode(i)) {
                br.write("[V" + i + ",*]");
                int inLinks[] = sDomain.getInLinks(i);
                for (int j = 0; j < inLinks.length; j++) {
                    br.write(" L" + inLinks[j] + " 1");
                }
                if (i < nbNodes-1)
                    br.write("\n");
            }
        }
        br.write(";\n\n");

        br.write("param OutLinks :=\n");
        for (int i=0; i < nbNodes;i++) {
            if (sDomain.isNode(i)) {
                br.write("[V" + i + ",*]");
                int outLinks[] = sDomain.getOutLinks(i);
                for (int j = 0; j < outLinks.length; j++) {
                    br.write(" L" + outLinks[j] + " 1");
                }
                if (i < nbNodes-1)
                    br.write("\n");
            }
        }
        br.write(";\n\n");

        br.write("param Capa :=\n");
        for (int i=0; i < nbLinks;i++) {
            if (linkList[i] != null) {
                int linkId = linkList[i].getId();
                br.write("[L" + linkId + "]");
                int capa = (new Float(sDomain.getLinkCapacity(linkId))).intValue();
                br.write(" " + capa);
                if (i < nbLinks-1)
                    br.write("\n");
            }
        }
        br.write(";\n\n");


        // TO CHECK
        br.write("param Demand :=\n");
        int totalBw = 0;
        for (int i=0; i < nbNodes;i++) {
            if (sDomain.isNode(i)) {
                totalBw = 0;
                br.write("[*,V" + i + "]");
                for (int j = 0; j < nbNodes; j++) {
                    if (j != i) {
                        int bw = (new Float(tm.get(i,j))).intValue();
                        if (bw != 0) {
                            totalBw += bw;
                            br.write(" V" + j + " " + bw);
                        }
                    } else {
                        br.write(" V" + j + " " + 0);
                    }
                }
                if (i < nbNodes-1)
                    br.write("\n");
            }
        }
        br.write(";\n\nend;\n");
        br.close();
        fw.close();
    }


    /**
     * Read the link utilisation from the result file
     *
     * @param domain
     * @param resultFileName
     * @return
     * @throws IOException
     */
    private float[] readUtilizationFromResultFile(SimplifiedDomain domain,String resultFileName) throws IOException {
        int nbLinks = domain.getLinks().length;
        float[] utilization = new float[nbLinks];
        for (int i = 0; i < utilization.length; i++) {
            utilization[i] = 0;
        }
        FileReader fr = new FileReader(resultFileName);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            if (line.indexOf(" utilization[") != -1) {
                int start = line.indexOf('[');
                int end = line.indexOf(']');
                int linkId = Integer.parseInt(line.substring(start+2,end));
                if (linkId >= nbLinks)
                    logger.error("Error : link id not successive integer");
                line = br.readLine();
                String lineAr[] = line.split("\\s+");
                utilization[linkId] = Float.parseFloat(lineAr[2]);
            }
        }
        return utilization;
    }

}

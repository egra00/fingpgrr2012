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
package be.ac.ulg.montefiore.run.totem.repository.MultiCommodityFlow;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.DomainConvertor;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomainBuilder;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LoadData;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.*;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;
import be.ac.ulg.montefiore.run.totem.core.Totem;
import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;
import be.ac.ulg.montefiore.run.totem.util.FileFunctions;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 14-Jun-2007: now implements TmLoadComputer, use double instead of float (GMO)
* - 08-Aug-2007: check for glpsol existence before use (GMO)
* - 20-Aug-2007: bugfix in model initialisation. computeLoad can be called multiple times (GMO)
* - 25-Feb-2008: bugfix when dealing with assymmetric links. (GMO)
* - 26-Feb-2008: use the new LinkLoadComputer interface (GMO)
*/

/**
 * This implementation of the multi commodity flow algorithm compute the link load
 * associated to a Domain and a TraffixMatrix. We use an AMPL model located in
 * <code>/resources/modelAMPL/mcf-min-maxUtil.mod</code>. This model minimize the utilisation of the
 * max utilised link. This class can only generate the data file or use the glpsol solver
 * to compute the link load.
 *<p>
 * Two preferences are used :
 * <ul>
 * <li> GLPSOL-BIN : specifies the location of the glpsol binary</li>
 * <li> MCF-MODEL : specifies the location of the AMPL model</li>
 * </ul>
 *
 * <p>Creation date: 4-Feb-2005
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class MultiCommodityFlow extends AbstractLinkLoadComputer{

    private static final Logger logger = Logger.getLogger(MultiCommodityFlow.class);

    private final String defaultModelFileName = "/resources/modelAMPL/mcf-min-maxUtil.mod";
    private final String tmpModelFile = "model.mod";

    private String glpsolBin;
    private String dataFileName;
    private String resultFileName;
    private String modelFileName;
    private boolean removeFile;

    private String modelStr;

    private TrafficMatrix tm;

    private SettableIPLoadData data;

    public MultiCommodityFlow(Domain domain, TrafficMatrix tm) throws IOException {
        super(domain);
        this.tm = tm;
        this.dataFileName = "mcf.dat";
        this.resultFileName = "mcf-result.txt";
        glpsolBin = PreferenceManager.getInstance().getPrefs().get("GLPSOL-BIN","glpsol");
        this.removeFile = true;

        data = new SettableIPLoadData(domain);

        logger.debug("Checking if glpsol can be found.");
        Process p = Runtime.getRuntime().exec(glpsolBin + " --version");
        // Wait for it to finish running
        try {
            p.waitFor();
        } catch (InterruptedException ie) {
            logger.error(ie);
            throw new IOException("Process glpsol interrupted.");
        }

        int ret = p.exitValue();
        if (ret != 0) {
            logger.error("ERROR with glpsol : " + ret);
            throw new IOException("ERROR with glpsol. Return code: " + ret);
        }

        modelFileName = PreferenceManager.getInstance().getPrefs().get("MCF-MODEL",defaultModelFileName);
        initModel();
    }

    private void initModel() throws IOException {
        File file = new File(modelFileName);
        if (!file.exists()) {
            modelFileName = PreferenceManager.getInstance().getPrefs().get("MCF-MODEL",defaultModelFileName);
            logger.debug("Cannot find MCF AMPL model in the file " + modelFileName);
            URL url = Totem.class.getResource(modelFileName);
            if (url == null) {
                logger.error("Cannot find MCF AMPL model in JAR : " + modelFileName);
                throw new IOException("Cannot find MCF AMPL model in JAR : " + modelFileName);
            }
            logger.info("Init MCF AMPL model from JAR with  : " + modelFileName);
            modelStr = modelFileName + " (from JAR)";
            try {
                FileFunctions.copy(url,tmpModelFile);
                this.modelFileName = tmpModelFile;
            } catch (IOException e) {
                throw e;
            }
        } else {
            logger.info("Init MCF AMPL model with  : " + modelFileName);
            modelStr = modelFileName + " (from file)";
        }
    }


    /**
     * Specify a data file and a result file
     *
     * @param dataFile
     * @param resultFile
     */
    public MultiCommodityFlow(Domain domain, TrafficMatrix tm, String dataFile, String resultFile) throws IOException {
        this(domain, tm);
        this.dataFileName = dataFile;
        this.resultFileName = resultFile;
        removeFile = false;
    }

    /**
     * Compute the link utilisation produce by the MCF using glpsol
     *
     * @return
     * @throws IOException
     * @throws LinkNotFoundException
     * @throws NodeNotFoundException
     */
    private double[] computeCommodityFlow()
            throws IOException, LinkNotFoundException, NodeNotFoundException {

        this.createMCFMinMaxUtilDataFile(domain,tm);
        initModel();

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
            throw new IOException("ERROR with glpsol. Return code: " + ret);
        }
        double[] linkLoad = readUtilizationFromResultFile(SimplifiedDomainBuilder.build(domain),resultFileName);
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
     * @return The absolute file path of the created file
     * @throws IOException
     * @throws NodeNotFoundException
     * @throws LinkNotFoundException
     */
    public String createMCFMinMaxUtilDataFile(Domain domain, TrafficMatrix tm)
            throws IOException, NodeNotFoundException, LinkNotFoundException {
        SimplifiedDomain sDomain = SimplifiedDomainBuilder.build(domain);

        File f = new File(dataFileName);

        FileWriter fw = new FileWriter(f);
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

        br.write("set COMMODITIES := ");
        for (int i=0; i < nbNodes;i++) {
            if (sDomain.isNode(i)) {
                br.write("K" + i);
                if (i < nbNodes-1)
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
        br.write("param ComValue :=\n");
        int totalBw = 0;
        for (int i=0; i < nbNodes;i++) {
            if (sDomain.isNode(i)) {
                totalBw = 0;
                br.write("[*,K" + i + "]");
                for (int j = 0; j < nbNodes; j++) {
                    if (j != i) {
                        int bw = (new Float(tm.get(j,i))).intValue();
                        //System.out.println("from node " + domain.getConvertor().getNodeId(i) + " to node " + domain.getConvertor().getNodeId(j) + ", traffic = " + bw);
                        //if (bw != 0) {
                            totalBw += bw;
                            br.write(" V" + j + " " + bw);
                        //}
                    }
                }
                br.write(" V" + i + " -" + totalBw);
                if (i < nbNodes-1)
                    br.write("\n");
            }
        }
        br.write(";\n\nend;\n");
        br.close();
        fw.close();

        return f.getAbsolutePath();
    }


    /**
     * Read the link utilisation from the result file
     *
     * @param domain
     * @param resultFileName
     * @return
     * @throws IOException
     */
    private double[] readUtilizationFromResultFile(SimplifiedDomain domain,String resultFileName) throws IOException {
        int nbLinks = domain.getLinks().length;
        double[] utilization = new double[nbLinks];
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
                utilization[linkId] = Double.parseDouble(lineAr[2]);
                //System.out.println("- " + utilization[linkId]);
            }
        }
        return utilization;
    }

    public void recompute() {
        logger.debug("Computing MCF");
        data.clear();
        try {
            double[] util = computeCommodityFlow();
            DomainConvertor conv = domain.getConvertor();

            for (int i = 0; i < util.length; i++) {
                try {
                    Link link = domain.getLink(conv.getLinkId(i));
                    double capacity = link.getBandwidth();
                    data.addTraffic(link, util[i] * capacity);
                } catch (LinkNotFoundException e) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }
        dataChanged();
    }

    public String toString() {
        return getClass().getSimpleName() + ", " + modelStr.substring(modelStr.lastIndexOf("/")+1);
    }

    public List<TrafficMatrix> getTrafficMatrices() {
        List<TrafficMatrix> tms = new ArrayList<TrafficMatrix>(1);
        tms.add(tm);
        return tms;
    }

    public String getShortName() {
        return toString();
    }

    public boolean equals(Object o) {
        if (!(o instanceof MultiCommodityFlow)) return false;
        MultiCommodityFlow str = (MultiCommodityFlow) o;
        //if (!str.modelStr.equals(modelStr)) return false;
        try {
            if (tm.getTmId() != str.tm.getTmId()) return false;
        } catch (TrafficMatrixIdException e) {
            e.printStackTrace();
        }
        if (!str.modelFileName.equals(modelFileName)) return false;
        return true;
    }

    public int hashCode() {
        int result = modelStr.hashCode();
        return result;
    }

    public LoadData getData() {
        return data;
    }

    public LoadData detachData() {
        LoadData oldData = data;
        data = null;
        return oldData;
    }

}

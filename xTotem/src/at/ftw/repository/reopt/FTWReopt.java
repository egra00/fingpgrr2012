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
package at.ftw.repository.reopt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;
import be.ac.ulg.montefiore.run.totem.core.Totem;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomainBuilder;
import be.ac.ulg.montefiore.run.totem.repository.model.SetLspReservationAction;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemActionList;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.util.FileFunctions;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class runs the Reopt (FTW, Vienna) algorithm.
 *
 * <p>Creation date: 09-mai-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class FTWReopt {

    private static final Logger logger = Logger.getLogger(FTWReopt.class);

    private final String tmpModelFile = "model.mod";
    
    private String dataFile, solver, resultFile, initialModelFile, reoptModelFile;
    private String cplexRunFile = "ftw_reopt.run";
    private boolean removeFile;
    
    public FTWReopt() {
        dataFile = "ftw_reopt-data.dat";
        removeFile = true;
        solver = PreferenceManager.getInstance().getPrefs().get("GLPSOL-BIN", "glpsol");
        initialModelFile = PreferenceManager.getInstance().getPrefs().get("FTW_REOPT-INIT-MODEL", "/resources/modelAMPL/ftw_reopt-init.mod");
        reoptModelFile = PreferenceManager.getInstance().getPrefs().get("FTW_REOPT-REOPT-MODEL", "/resources/modelAMPL/ftw_reopt-reopt.mod");
        resultFile = "ftw_reopt-result.txt";
    }
    
    public FTWReopt(String dataFile, String resultFile) {
        this.dataFile = dataFile;
        removeFile = false;
        solver = PreferenceManager.getInstance().getPrefs().get("GLPSOL-BIN", "glpsol");
        initialModelFile = PreferenceManager.getInstance().getPrefs().get("FTW_REOPT-INIT-MODEL", "/resources/modelAMPL/ftw_reopt-init.mod");
        reoptModelFile = PreferenceManager.getInstance().getPrefs().get("FTW_REOPT-REOPT-MODEL", "/resources/modelAMPL/ftw_reopt-reopt.mod");
        this.resultFile = resultFile;
    }
    
    public FTWReopt(String dataFile, String resultFile, String cplexRunFile) {
        this.dataFile = dataFile;
        removeFile = false;
        solver = PreferenceManager.getInstance().getPrefs().get("GLPSOL-BIN", "glpsol");
        initialModelFile = PreferenceManager.getInstance().getPrefs().get("FTW_REOPT-INIT-MODEL", "/resources/modelAMPL/ftw_reopt-init.mod");
        reoptModelFile = PreferenceManager.getInstance().getPrefs().get("FTW_REOPT-REOPT-MODEL", "/resources/modelAMPL/ftw_reopt-reopt.mod");
        this.resultFile = resultFile;
        this.cplexRunFile = cplexRunFile;
    }
    
    public TotemActionList<SetLspReservationAction> calculateInitialSolution(Domain domain, TrafficMatrix tm, int profit, int changeCost) throws IOException, LinkNotFoundException, NodeNotFoundException {
        this.createDataFile(domain, tm, profit, changeCost, true);
        
        File file = new File(initialModelFile);
        if (!file.exists()) {
            logger.debug("Cannot find MCF AMPL model in the file " + initialModelFile);
            URL url = Totem.class.getResource(initialModelFile);
            if (url == null) {
                logger.error("Cannot find MCF AMPL model in JAR : " + initialModelFile);
                return null;
            }
            logger.info("Init MCF AMPL model from JAR with  : " + initialModelFile);
            try {
                FileFunctions.copy(url,tmpModelFile);
                this.initialModelFile = tmpModelFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("Init MCF AMPL model with  : " + initialModelFile);
        }
        
        Process p;
        if(!solver.equals("glpsol")) {
            this.createCplexRunFile(true);
            p = Runtime.getRuntime().exec("ampl "+cplexRunFile);
        } else {
            p = Runtime.getRuntime().exec(solver + " -m " + initialModelFile + " -d "+ dataFile + " -o " + resultFile);
        }

        // Wait for it to finish running
        try {
            p.waitFor();
        } catch (InterruptedException ie) {
            System.out.println(ie);
        }
        int ret = p.exitValue();
        if (ret != 0) {
            logger.error("ERROR with "+solver+" : " + ret);
            return null;
        }
        TotemActionList<SetLspReservationAction> actionList = readCapacitiesFromResultFile(domain, resultFile);
        if (removeFile) {
            File f = new File(dataFile);
            if (f.exists())
                f.delete();
            f = new File(resultFile);
            if (f.exists())
                f.delete();
            f = new File(cplexRunFile);
            if (f.exists())
                f.delete();
        }
        File f = new File(tmpModelFile);
        if (f.exists())
            f.delete();

        return actionList;
    }
    
    public TotemActionList<SetLspReservationAction> reopt(Domain domain, TrafficMatrix tm, int profit, int changeCost) throws IOException, NodeNotFoundException, LinkNotFoundException {
        this.createDataFile(domain, tm, profit, changeCost, false);

        File file = new File(reoptModelFile);
        if (!file.exists()) {
            logger.debug("Cannot find MCF AMPL model in the file " + reoptModelFile);
            URL url = Totem.class.getResource(reoptModelFile);
            if (url == null) {
                logger.error("Cannot find MCF AMPL model in JAR : " + reoptModelFile);
                return null;
            }
            logger.info("Init MCF AMPL model from JAR with  : " + reoptModelFile);
            try {
                FileFunctions.copy(url,tmpModelFile);
                this.reoptModelFile = tmpModelFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("Init MCF AMPL model with  : " + reoptModelFile);
        }
        
        Process p;
        if(!solver.equals("glpsol")) {
            this.createCplexRunFile(false);
            p = Runtime.getRuntime().exec("ampl "+cplexRunFile);
        } else {
            p = Runtime.getRuntime().exec(solver + " -m " + reoptModelFile + " -d "+ dataFile + " -o " + resultFile);
        }
        
        // Wait for it to finish running
        try {
            p.waitFor();
        } catch (InterruptedException ie) {
            System.out.println(ie);
        }
        int ret = p.exitValue();
        if (ret != 0) {
            logger.error("ERROR with "+solver+" : " + ret);
            return null;
        }
        TotemActionList<SetLspReservationAction> actionList = readCapacitiesFromResultFile(domain, resultFile);
        if (removeFile) {
            File f = new File(dataFile);
            if (f.exists())
                f.delete();
            f = new File(resultFile);
            if (f.exists())
                f.delete();
            f = new File(cplexRunFile);
            if (f.exists())
                f.delete();
        }
        File f = new File(tmpModelFile);
        if (f.exists())
            f.delete();
        
        return actionList;
    }
    
    public void createDataFile(Domain domain, TrafficMatrix tm, int profit, int changeCost, boolean isInitialDataFile) throws IOException, LinkNotFoundException, NodeNotFoundException {
        SimplifiedDomain sDomain = SimplifiedDomainBuilder.build(domain);
        
        FileWriter fw = new FileWriter(dataFile);
        BufferedWriter bw = new BufferedWriter(fw);
        
        bw.write("set LINKS := ");
        int nbLinks = sDomain.getNbLinks();
        for(int i = 0; i < nbLinks; ++i) {
            if(sDomain.isLink(i)) {
                bw.write("u"+i);
                if(i < (nbLinks-1)) {
                    bw.write(' ');
                }
            }
        }
        bw.write(";\n\n");
        
        int nbNodes = sDomain.getNbNodes();
        bw.write("set K := ");
        for(int i = 0; i < nbNodes; ++i) {
            for(int j = 0; j < nbNodes; ++j) {
                if(i == j) {
                    continue;
                }
                
                if(sDomain.isNode(i) && sDomain.isNode(j)) {
                    bw.write("d"+i+"_"+j);
                    if((i != (nbNodes-1)) || (j != (nbNodes-1))) {
                        bw.write(' ');
                    }
                }
            }
        }
        bw.write(";\n\n");
        
        bw.write("param cap := ");
        for(int i = 0; i < nbLinks; ++i) {
            if(sDomain.isLink(i)) {
                bw.write("u"+i+" "+sDomain.getLinkCapacity(i));
                if(i < (nbLinks-1)) {
                    bw.write(' ');
                }
            }
        }
        bw.write(";\n\n");
        
        bw.write("param earn default "+profit+";\n");
        bw.write("param change_cost default "+changeCost+";\n\n");
        
        if(!isInitialDataFile) {
            bw.write("param x0 := \n");
            List<Lsp> lsps = domain.getAllLsps();
            Collections.sort(lsps, new LspComparator(domain));
            Iterator<Lsp> it = lsps.iterator();
            Lsp lsp = it.next();
            for(int i = 0; i < nbNodes; ++i) {
                for(int j = 0; j < nbNodes; ++j) {
                    if(i == j) {
                        continue;
                    }
                    
                    if(sDomain.isNode(i) && sDomain.isNode(j)) {
                        int z = 1;
                        while((domain.getConvertor().getNodeId(lsp.getLspPath().getSourceNode().getId()) == i) && (domain.getConvertor().getNodeId(lsp.getLspPath().getDestinationNode().getId()) == j)) {
                            bw.write("d"+i+"_"+j+" "+(z++)+" "+lsp.getReservation()+"\n");
                            if(!it.hasNext()) {
                                break;
                            }
                            lsp = it.next();
                        }
                    }
                }
            }
            bw.write(";\n\n");
        }
        
        bw.write("param PATH\n");
        List<Lsp> lsps = domain.getAllLsps();
        Collections.sort(lsps, new LspComparator(domain));
        Iterator<Lsp> it = lsps.iterator();
        
        Lsp lsp = it.next();
        ArrayList<Integer> nbLsps = new ArrayList<Integer>();
        for(int i = 0; i < nbNodes; ++i) {
            for(int j = 0; j < nbNodes; ++j) {
                if(i == j) {
                    continue;
                }
                
                if(sDomain.isNode(i) && sDomain.isNode(j)) {
                    bw.write("[d"+i+"_"+j+",*,*](tr):\n");
                    for(int k = 0; k < nbLinks; ++k) {
                        if(sDomain.isLink(k)) {
                            bw.write("u"+k+" ");
                        }
                    }
                    bw.write(":=\n");
                    
                    int z = 1;
                    while((domain.getConvertor().getNodeId(lsp.getLspPath().getSourceNode().getId()) == i) && (domain.getConvertor().getNodeId(lsp.getLspPath().getDestinationNode().getId()) == j)) {
                        bw.write(Integer.toString(z++));
                        
                        for(int k = 0; k < nbLinks; ++k) {
                            if(sDomain.isLink(k)) {
                                if(lsp.getLspPath().containsLink(domain.getLink(domain.getConvertor().getLinkId(k)))) {
                                    bw.write(" 1");
                                }
                                else {
                                    bw.write(" 0");
                                }
                            }
                        }
                        bw.write("\n");
                        
                        if(!it.hasNext()) {
                            break;
                        }
                        lsp = it.next();
                    }
                    nbLsps.add(z-1);
                }
            }
        }
        bw.write(";\n");
        
        bw.write("param P := ");
        Iterator<Integer> iter = nbLsps.iterator();
        for(int i = 0; i < nbNodes; ++i) {
            for(int j = 0; j < nbNodes; ++j) {
                if(i == j) {
                    continue;
                }
                
                if(sDomain.isNode(i) && sDomain.isNode(j)) {
                    bw.write("d"+i+"_"+j+" "+iter.next()+" ");
                }
            }
        }
        bw.write(";\n\n");

        bw.write("param demand := ");
        for(int i = 0; i < nbNodes; ++i) {
            for(int j = 0; j < nbNodes; ++j) {
                if(i == j) {
                    continue;
                }
                
                if(sDomain.isNode(i) && sDomain.isNode(j)) {
                    bw.write("d"+i+"_"+j+" "+tm.get(i, j)+" ");
                }
            }
        }
        bw.write(";\n\n");
        
        bw.write("end;\n");
        
        bw.flush();
        fw.flush();
        bw.close();
        fw.close();
    }
    
    public void createCplexRunFile(boolean isInitialRun) throws IOException {
        FileWriter fw = new FileWriter(cplexRunFile);
        BufferedWriter bw = new BufferedWriter(fw);
        
        if(isInitialRun) {
            bw.write("model "+initialModelFile+";\n");
        } else {
            bw.write("model "+reoptModelFile+";\n");
        }
        
        bw.write("data "+dataFile+";\n");
        bw.write("option solver "+solver+";\n");
        bw.write("solve;\n");
        bw.write("printf {i in K, j in 1..P[i]}: \"x[%s,%d] %f\n\", i, j, x[i,j] > "+resultFile+";\n");
        
        bw.flush();
        fw.flush();
        bw.close();
        fw.close();
    }
    
    public TotemActionList<SetLspReservationAction> readCapacitiesFromResultFile(Domain domain, String file) throws IOException {
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        
        List<Lsp> lsps = domain.getAllLsps();
        Collections.sort(lsps, new LspComparator(domain));
        Iterator<Lsp> iter = lsps.iterator();
        
        TotemActionList<SetLspReservationAction> actionList = new TotemActionList<SetLspReservationAction>();
        
        String line = br.readLine();
        while((line != null) && (line.indexOf("x[") == -1)) {
            line = br.readLine();
        }
        if(line == null) {
            // EOF reached
            return null;
        }
        
        while((line != null) && (line.indexOf("x[") != -1)) {
            line = line.trim();
            String[] tokens = line.split(" +");
            try {
                actionList.add(new SetLspReservationAction(iter.next(), Float.parseFloat(tokens[tokens.length-1])));
            } catch (NumberFormatException e) {
                logger.error("Bad output... Message: "+e.getMessage());
            }
            line = br.readLine();
        }
        
        br.close();
        fr.close();
        
        return actionList;
    }
    
    private class LspComparator implements Comparator<Lsp> {
        private Domain domain;
        
        public LspComparator(Domain domain) {
            this.domain = domain;
        }
        
        public int compare(Lsp l1, Lsp l2) {
            try {
                // Maybe we should use the strings instead of the ints?
                int src1 = domain.getConvertor().getNodeId(l1.getLspPath().getSourceNode().getId());
                int src2 = domain.getConvertor().getNodeId(l2.getLspPath().getSourceNode().getId());
                int dst1 = domain.getConvertor().getNodeId(l1.getLspPath().getDestinationNode().getId());
                int dst2 = domain.getConvertor().getNodeId(l2.getLspPath().getDestinationNode().getId());
                
                if(src1 < src2) {
                    return -1;
                }
                else if(src1 > src2) {
                    return 1;
                }
                else if(dst1 < dst2) {
                    return -1;
                }
                else if(dst1 > dst2) {
                    return 1;
                }
                else {
                    return l1.getId().compareTo(l2.getId());
                }
                
            } catch (NodeNotFoundException e) {
                logger.error("NodeNotFoundException in compare. Message: "+e.getMessage());
                if(logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                return -1;
            }
        }
    }
}

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
package be.ac.ulg.montefiore.run.totem.repository.SAMTE.simulation;

import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPFTEMetric;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemActionList;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemAction;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.SPFLinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;
import be.ac.ucl.poms.repository.IGPWO.IGPWO;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.text.DateFormat;

/*
* Changes:
* --------
* - 20-Mar-2006: use LinkLoadComputer to calculate load (GMO)
* - 20-Mar-2006: remove unused import (GMO)
* - 28-Feb-2008: use the new LinkLoadComputer interface (GMO)
* - 29-Feb-2008: fix bugs with ids in arrays (GMO)
*/

/**
 * <p>Creation date: 08-Jul-2005 11:09:05
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class IGPWOSimulation {

    private Domain domain;
    private String tmDirName;
    private String outputFile;
    private int nbIter;
    private int maxPossibleWeight;
    private LinkUtilInfo linkUtilInfo[];
    private float linkCapacities[];


    public IGPWOSimulation(int ASID,String tmDirName, String outputFile, int nbIter, int maxPossibleWeight) {
        try {
            domain = InterDomainManager.getInstance().getDomain(ASID);
        } catch (InvalidDomainException e) {
            e.printStackTrace();
        }
        this.tmDirName = tmDirName;
        this.outputFile = outputFile;
        this.nbIter = nbIter;
        this.maxPossibleWeight = maxPossibleWeight;
    }

    private void processTM(int tmIdx, BufferedWriter writer) throws Exception {
        TrafficMatrix tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(),tmIdx);
        LinkLoadStrategy lls = new SPFLinkLoadStrategy(domain, tm);
        lls.setSPFAlgo(new CSPFTEMetric());
        int tmIds[] = new int[1];
        tmIds[0] = tmIdx;

        IGPWO igpwo = (IGPWO) RepositoryManager.getInstance().getAlgo("IGPWO", domain.getASID());

        DateFormat df1 = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);

        long time = System.currentTimeMillis();
        TotemActionList actionList = igpwo.calculateWeightsParameters(domain.getASID(), tmIds, nbIter, maxPossibleWeight);
        time = System.currentTimeMillis() - time;
        System.out.println("IGPWO (nbIter " + nbIter + ",maxWeight " + maxPossibleWeight + ") takes " + time + " ms to compute weights for TM " + df1.format(tm.getDate().getTime()));
        for (Iterator iter = actionList.iterator(); iter.hasNext();) {
            TotemAction action = (TotemAction) iter.next();
            action.execute();
        }
        double linkUtil[] = lls.getData().getUtilization();
        double linkLoad[] = lls.getData().getLoad();
        lls.destroy();

        double[] upLinkLoad = new double[domain.getUpLinks().size()];
        double[] upLinkUtil = new double[domain.getUpLinks().size()];
        int i = 0;
        for (Link l : domain.getUpLinks()) {
            int idx = domain.getConvertor().getLinkId(l.getId());
            upLinkLoad[i] = linkLoad[idx];
            upLinkUtil[i] = linkUtil[idx];
            i++;
        }

        StringBuffer line = new StringBuffer(df1.format(tm.getDate().getTime()));
        line.append("\t");
        line.append(DoubleArrayAnalyse.getMaximum(upLinkUtil));
        line.append("\t");
        line.append(DoubleArrayAnalyse.getMeanValue(upLinkUtil));
        line.append("\t");
        line.append(DoubleArrayAnalyse.getStandardDeviation(upLinkUtil));
        line.append("\t");
        line.append(DoubleArrayAnalyse.getPercentile10(upLinkUtil));
        line.append("\t");
        line.append(DoubleArrayAnalyse.getFortz(upLinkLoad,linkCapacities));
        line.append("\n");
        writer.write(line.toString());

    }

    public void start() throws Exception {
        String tmToLoad[] = null;
        File f = new File(tmDirName);
        int tmId = 0;
        if (!f.isDirectory()) {
            System.out.println(tmDirName + " not a directory !!!");
        } else {
            File listOfFiles[] = f.listFiles();
            Arrays.sort(listOfFiles); //,new FileNameComparator());
            tmToLoad = new String[listOfFiles.length];
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].getAbsolutePath().contains(".xml")) {
                    tmToLoad[tmId] = listOfFiles[i].getAbsolutePath();
                    tmId++;
                }
            }
        }

        RepositoryManager.getInstance().startAlgo("IGPWO",null,domain.getASID());

        List<Link> upLinks = domain.getUpLinks();
        linkCapacities = new float[upLinks.size()];
        int i = 0;
        for (Iterator<Link> it = upLinks.iterator(); it.hasNext(); ++i) {
            Link link = it.next();
            linkCapacities[i] = link.getBandwidth();
        }

        FileWriter outputFileWriter = new FileWriter(outputFile);
        BufferedWriter outputFile = new BufferedWriter(outputFileWriter);
        outputFile.write("# IGPWO simulation on domain " + ((domain.getName() != null) ? domain.getName() : Integer.toString(domain.getASID())) + "\n");
        outputFile.write("# Traffic matrix dir : " + tmDirName + "\n");
        outputFile.write("# Date \t Max \t Mean \t Std \t Per10 \t Fortz\n");

        int nbTM = tmId;
        linkUtilInfo = new LinkUtilInfo[nbTM];

        // Process all TM one by one
        for (int tmIdx = 0; tmIdx < nbTM; tmIdx++) {
            TrafficMatrixManager.getInstance().loadTrafficMatrix(tmToLoad[tmIdx],tmIdx,false);
            System.out.println("Load TM " + tmToLoad[tmIdx].substring(tmToLoad[tmIdx].lastIndexOf('/'),tmToLoad[tmIdx].length()));
            this.processTM(tmIdx,outputFile);
            TrafficMatrixManager.getInstance().removeTrafficMatrix(domain.getASID(),tmIdx);
        }

        outputFile.close();
        outputFileWriter.close();
    }
}

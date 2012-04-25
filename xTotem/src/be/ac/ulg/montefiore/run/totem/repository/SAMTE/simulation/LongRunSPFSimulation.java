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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.repository.model.SPF;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.SPFLinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/*
* Changes:
* --------
*
*/

/**
 * <p>Creation date: 12-Jul-2005 16:37:22
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class LongRunSPFSimulation {

    private Domain domain;
    private String tmDirName;
    private String outFileName;
    private BufferedWriter report;
    private boolean ECMP;
    private SPF spf;
    private float linkCapacities[];

    public LongRunSPFSimulation(Domain domain, String tmDirName, String outFileName, boolean ECMP,SPF spf) {
        this.domain = domain;
        this.tmDirName = tmDirName;
        this.outFileName = outFileName;
        this.ECMP = ECMP;
        this.spf = spf;
    }

    private void processTM(int tmIdx) throws Exception {
        TrafficMatrix tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(),tmIdx);
        LinkLoadStrategy lls = new SPFLinkLoadStrategy(domain, tm);
        lls.setECMP(ECMP);
        lls.setSPFAlgo(spf);
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

        DateFormat df1 = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
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
        report.write(line.toString());
    }

    public void start() throws Exception {

        FileWriter reportFW = new FileWriter(outFileName);
        report = new BufferedWriter(reportFW);

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
        int nbTM = tmId;

        List<Link> upLinks = domain.getUpLinks();
        linkCapacities = new float[upLinks.size()];
        int i = 0;
        for (Iterator<Link> it = upLinks.iterator(); it.hasNext(); ++i) {
            Link link = it.next();
            linkCapacities[i] = link.getBandwidth();
        }

        report.write("# SPF simulation on domain " + ((domain.getName() != null) ? domain.getName() : Integer.toString(domain.getASID())) + "\n");
        report.write("# Traffic matrix dir : " + tmDirName + "\n");
        report.write("# ECMP : " + ECMP + "\n");
        report.write("# ALGO : " + spf.getClass().getName() + "\n");
        report.write("# Date \t Max \t Mean \t Std \t Per10 \t Fortz\n");

        for (int tmIdx = 0; tmIdx < nbTM; tmIdx++) {
            TrafficMatrixManager.getInstance().loadTrafficMatrix(tmToLoad[tmIdx],tmIdx,false);
            System.out.println("Process TM " + tmToLoad[tmIdx].substring(tmToLoad[tmIdx].lastIndexOf('/'),tmToLoad[tmIdx].length()));
            this.processTM(tmIdx);
            TrafficMatrixManager.getInstance().removeTrafficMatrix(domain.getASID(),tmIdx);
        }
        report.close();
        reportFW.close();

    }

}

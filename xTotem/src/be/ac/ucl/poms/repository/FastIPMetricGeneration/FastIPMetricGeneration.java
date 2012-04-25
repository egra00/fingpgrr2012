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
package be.ac.ucl.poms.repository.FastIPMetricGeneration;

import be.ac.ucl.poms.repository.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.*;
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
import java.lang.Object;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.Math;



import org.apache.log4j.Logger;

/**
 * This implementation of the multi commodity flow problem generates
 * heuristic IGP Weights using dual variables of the load constraints.
 * We use an AMPL model located in /resources/modelAMPL/fastipmetric.mod.
 * This model uses a piecewise linear objective function Fortz&Thorup (2000)
 * that penalizes increasing link loads.
 * This class can only generate the data file or use the glpsol solver
 * to compute the link load.
 *
 * Two preferences are used :
 *  - GLPSOL-BIN : specifies the location of the glpsol binary
 *  - FASTIPMETRIC-MODEL : specifies the location of the AMPL model
 *
 * <p>Creation date: 20-March-2008
 *
 * @author  Hakan Umit (hakan.umit@uclouvain.be)
 */
public class FastIPMetricGeneration {

    private static Logger logger = Logger.getLogger(FastIPMetricGeneration.class.getName());
    private final String defaultModelFileName = "/resources/modelAMPL/fastipmetric.mod";
    private final String tmpModelFile = "model.mod";

    private String glpsolBin;
    private String dataFileName;
    private String resultFileName;
    private String modelFileName;
    private boolean removeFile;

    public FastIPMetricGeneration() {
        this.dataFileName = "mcf.dat";
        this.resultFileName = "mcf-result.txt";
        glpsolBin = PreferenceManager.getInstance().getPrefs().get("GLPSOL-BIN","glpsol");
        removeFile = false;
        modelFileName = PreferenceManager.getInstance().getPrefs().get("FASTIPMETRIC-MODEL",defaultModelFileName);
    }

    /**
     * Specify a data file and a result file
     *
     * @param dataFile
     * @param resultFile
     */
    public FastIPMetricGeneration(String dataFile, String resultFile) {
        this();
        this.dataFileName = dataFile;
        this.resultFileName = resultFile;
        removeFile = true;
    }

    /**
     * Solve the LP using glpsol
     *
     * @param domain
     * @param tm
     * @return
     * @throws IOException
     * @throws LinkNotFoundException
     * @throws NodeNotFoundException
     */
public TotemActionList computeLP(Domain domain, TrafficMatrix tm)
            throws IOException, LinkNotFoundException, NodeNotFoundException{

        this.createUtilDataFile(domain,tm);

        File file = new File(modelFileName);
        if (!file.exists()) {
            logger.debug("Cannot find fastipmetric AMPL model in the file " + modelFileName);
            URL url = Totem.class.getResource(modelFileName);
            if (url == null) {
                logger.error("Cannot find fastipmetric AMPL model in JAR : " + modelFileName);
                System.exit(0);
            }
            logger.info("Init fastipmetric AMPL model from JAR with  : " + modelFileName);
            try {
                FileFunctions.copy(url,tmpModelFile);
                this.modelFileName = tmpModelFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("Init MCF AMPL model with  : " + modelFileName);
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
	//double[] linkWeight = readWeightArrayFromResultFile(SimplifiedDomainBuilder.build(domain),resultFileName);
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
	//return linkWeight;
	TotemActionList actionList = new TotemActionList();

		try{

			double[] linkWeight = readWeightArrayFromResultFile(SimplifiedDomainBuilder.build(domain),resultFileName);
			TotemAction updateIGPWeights = new UpdateIGPWeightsAction(domain,linkWeight);

			actionList.add(updateIGPWeights);

		}
		catch(Exception e){
			e.printStackTrace();
		}

		return actionList;
    }

    /**
     * Create the input data file for glpsol
     *
     * @param domain
     * @param tm
     * @throws IOException
     * @throws NodeNotFoundException
     * @throws LinkNotFoundException
     */
    public void createUtilDataFile(Domain domain, TrafficMatrix tm)
            throws IOException, NodeNotFoundException, LinkNotFoundException {
        SimplifiedDomain sDomain = SimplifiedDomainBuilder.build(domain);

        FileWriter fw = new FileWriter(dataFileName);
        BufferedWriter br = new BufferedWriter(fw);

        br.write("set NODES := ");
        int nbNodes = sDomain.getNbNodes();
        for (int i=0; i < nbNodes;i++) {
            if (sDomain.isNode(i)) {
                br.write(" " + i);
                if (i < nbNodes-1)
                    br.write("\n");
            }
        }
        br.write(";\n\n");

        br.write("set LINKS := ");
        SimplifiedDomain.Link[] linkList = sDomain.getLinks();
        int nbLinks = linkList.length;
        for (int i=0; i < nbLinks;i++) {
            if (linkList[i] != null) {
                br.write(" " + linkList[i].getId());
                if (i < nbLinks-1)
                    br.write(" ");
            }
        }
        br.write(";\n\n");

      br.write("param TailNode :=\n");
        for (int i=0; i < nbLinks;i++) {
            if (linkList[i] != null) {
                int linkId = linkList[i].getId();
                br.write("" + linkId + "");
                int tailnode = (new Float(sDomain.getLinkSrc(linkId))).intValue();
                br.write(" " + tailnode);
                if (i < nbLinks-1)
                    br.write("\n");
            }
        }

        br.write(";\n\n");

	br.write("param HeadNode :=\n");
        for (int i=0; i < nbLinks;i++) {
            if (linkList[i] != null) {
                int linkId = linkList[i].getId();
                br.write("" + linkId + "");
                int headnode = (new Float(sDomain.getLinkDst(linkId))).intValue();
                br.write(" " + headnode);
                if (i < nbLinks-1)
                    br.write("\n");
            }
        }

        br.write(";\n\n");

        br.write("param Cap :=\n");
        for (int i=0; i < nbLinks;i++) {
            if (linkList[i] != null) {
                int linkId = linkList[i].getId();
                br.write("" + linkId + "");
                float capa = (new Float(sDomain.getLinkCapacity(linkId)));
                br.write(" " + capa);
                if (i < nbLinks-1)
                    br.write("\n");
            }
        }

        br.write(";\n\n");

        br.write("param Dem : \n");
        for (int i=0; i < nbNodes;i++) {
            if (sDomain.isNode(i)) {
		br.write(" " + i);
	    }
	}
	br.write(":=");
	for (int i=0; i < nbNodes;i++) {
	    if (sDomain.isNode(i)) {
		br.write(" " + i);
		for (int j = 0; j < nbNodes; j++) {
		     if (sDomain.isNode(j)) {
		    float bw = (new Float(tm.get(i,j)));
		    br.write(" " + bw);
		    }
		}
		br.write("\n");
	    }
	}
	br.write(";\n\n end;\n");
        br.close();
        fw.close();
    }


    /**
     * Read the link weights from the result file
     *
     * @param domain
     * @param resultFileName
     * @return
     * @throws IOException
     */
    private double[] readWeightArrayFromResultFile(SimplifiedDomain domain,String resultFileName) throws IOException {
        int nbLinks = domain.getLinks().length;
        double[] load = new double[nbLinks];
        for (int i = 0; i < load.length; i++) {
            load[i] = 0;
        }
        FileReader fr = new FileReader(resultFileName);
        BufferedReader br = new BufferedReader(fr);
        String line;
	/*
	while ((line = br.readLine()) != null) {
	if (line.indexOf("Objective:") != -1) {
	  int start = line.indexOf('='); 
	  int end = line.indexOf('(');  
          
	  int obj = Integer.parseInt(line.substring(start+2,end-1),10);
	  System.out.println("Obj: "+ obj); 
	  if(obj==0.0){ 
	      System.out.println("ERROR"); 
	      System.out.println("Objective Function equals to zero: Check the topology or the traffic matrix"); 
	      System.exit(0);
	  }
	}
	}
	*/
	while ((line = br.readLine()) != null) {
            if (line.indexOf("load_ctr[") != -1) {
                int start = line.indexOf('[');
                int end = line.indexOf(']');
                int linkId = Integer.parseInt(line.substring(start+1,end),10); 
		if (linkId >= nbLinks)
		    logger.error("Error : link id not successive integer");

		String lineAr[] = line.split("\\s+");

		Pattern p = Pattern.compile("eps");
	    Matcher m = p.matcher(line); // get a matcher object

		       if(m.find()) {
		         load[linkId]=1;
		          System.out.println("LinkId:" + linkId + "   Metric:" + load[linkId]);
		          continue;
		         //count++;
		           //System.out.println("Match number "+count);
		           //System.out.println("start(): "+m.start());
		           //System.out.println("end(): "+m.end());
		       }

		if(linkId>=100){
		   line = br.readLine();
		   String lineAr2[] = line.split("\\s+");
		   Double w = Double.parseDouble(lineAr2[5]);
		   if(w<0){
		       w = Math.abs(w);
		   }
		   load[linkId] = w;
		} else{ 
		    Double w = Double.parseDouble(lineAr[7]);
		    if(w<0){
			w = Math.abs(w); 
			//w=1.0;
		    }
		    load[linkId] = w;
		}
		System.out.println("LinkId:" + linkId + "   Metric:" + load[linkId]);
            }
	}
	return load;
}

}

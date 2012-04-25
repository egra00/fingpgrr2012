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
package be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist;

import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedPath;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

/*
* Changes:
* --------
*
*/

/**
 * <p>Creation date: 08-Mar-2005 14:37:49
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class SinglePathCPLFactory implements CandidatePathListFactory {

    private List<? extends CandidatePath> parseLine(String line, SimplifiedDomain domain) {
        List<SinglePathCP> resultList = new ArrayList<SinglePathCP>();
        Pattern urlPattern = Pattern.compile("\\[\\s((\\d+\\s)+)]");
        Matcher matcher = urlPattern.matcher(line);

        int[] pathArray = null;
        while (matcher.find()){
            String matchedString = matcher.group(1);
            String[] fields = matchedString.split("\\s+");
            pathArray = new int[fields.length];
            for (int i=0; i<fields.length; i++){
                pathArray[i] = Integer.parseInt(fields[i]);
            }
            SinglePathCP cp = new SinglePathCP(new SimplifiedPath(domain,pathArray));
            resultList.add(cp);
            //System.out.print("\t" + cp.getPath().toString());
        }
        //System.out.println("");
        return resultList;
    }

    public SinglePathCPL loadCPL(String fileName, SimplifiedDomain domain) throws IOException {
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        String line = null;
        SinglePathCPL cpl = new SinglePathCPL(domain.getNbNodes());
        while ((line = br.readLine()) != null) {
            Pattern urlPattern = Pattern.compile("^\\[(\\d+),(\\d+)\\]");
            int srcNode = 0;
            int dstNode = 0;
            Matcher matcher = urlPattern.matcher(line);
            if (matcher.find()){
                //System.out.println("Valeur de matcher.group(1) " + matcher.group(1));
                //System.out.println("Valeur de matcher.group(2) " + matcher.group(2));
                srcNode = Integer.parseInt(matcher.group(1));
                dstNode = Integer.parseInt(matcher.group(2));
            }
            //System.out.println("Pair ("+srcNode+","+dstNode+") :");
            cpl.setPath(srcNode,dstNode,parseLine(line,domain));
        }
        return cpl;
    }

    public void saveCPL(String fileName, CandidatePathList cpl) throws IOException {
        FileWriter fw = new FileWriter(fileName);
        BufferedWriter br = new BufferedWriter(fw);
        int nbNodes = cpl.getNbNodes();
        for (int srcNode = 0; srcNode < nbNodes; srcNode++) {
            for (int dstNode = 0; dstNode < nbNodes; dstNode++) {
                if (srcNode != dstNode) {
                    StringBuffer sb = new StringBuffer("[");
                    sb.append(srcNode);
                    sb.append(",");
                    sb.append(dstNode);
                    sb.append("]");
                    List<? extends CandidatePath> cpList = cpl.getPath(srcNode,dstNode);
                    if (cpList != null) {
                        for (int i = 0; i < cpList.size(); i++) {
                            CandidatePath candidatePath = cpList.get(i);
                            sb.append("\t");
                            sb.append(candidatePath.toString());
                        }
                    }
                    sb.append("\n");
                    br.write(sb.toString());
                }
            }
        }
        br.close();
        fw.close();
    }
}

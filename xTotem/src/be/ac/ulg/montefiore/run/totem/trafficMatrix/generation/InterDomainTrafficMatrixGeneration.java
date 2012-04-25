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
package be.ac.ulg.montefiore.run.totem.trafficMatrix.generation;

import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.BandwidthUnit;
import be.ac.ulg.montefiore.run.totem.domain.bgp.BGPInfoChecker;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.*;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.BandwidthUnits;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.zip.GZIPInputStream;
import java.util.List;

/*
 * Changes:
 * --------
 *
 * - 14-Sep-2005: code cleaning and use GZipped files (JLE).
 * - 14-Oct-2005: add code to handle GZipped and non GZipped files (JLE).
 * - 02-Feb-2007: make use of system dependent file separator (GMO).
 * - 28-Jun-2007: Add ability to convert units given sampling rate and minutes parameters (GMO)
 * - 20-Sep-2007: use BGPInfoChecker class (GMO)
 */

/**
 * This class provides inter-domain traffic matrix generation from NetFlow data
 *
 * <p>Creation date: 02/03/2005
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 * @author  Simon Balon (balon@run.montefiore.ulg.ac.be)
 */

public class InterDomainTrafficMatrixGeneration {

    private Domain domain = null;

    private int samplingRate;
    private int minutes;

    /**
     * Tell if samplingRate and minutes should be used.
     */
    private boolean raw = true;

    public InterDomainTrafficMatrixGeneration(Domain domain) {
        if (domain == null) throw new NullPointerException("Domain is null");
        this.domain = domain;
        this.raw = true;
    }

    public InterDomainTrafficMatrixGeneration(Domain domain, int minutes, int samplingRate) {
        if (domain == null) throw new NullPointerException("Domain is null");
        this.domain = domain;
        this.minutes = minutes;
        this.samplingRate = samplingRate;
        this.raw = false;
    }

    /**
     * If raw is true, sampling rate and minutes won't be used. Data will be in bytes per period.
     * see {@link #setSamplingParams(int, int)} if you want to use sampling rate and minutes.
     */
    public void useRaw() {
        this.raw = true;
    }

    /**
     * Use samping rate and minutes to convert the data from netflow.
     * @param minutes
     * @param samplingRate
     */
    public void setSamplingParams(int minutes, int samplingRate) {
        this.minutes = minutes;
        this.samplingRate = samplingRate;
        this.raw = false;
    }

    public Domain getDomain() {
        return domain;
    }

   /**
    * This function reads NetFlow aggregated data and stores them in one XML inter-domain traffic matrix file.
    * If raw is true, the data will be represented in bytes per period. Otherwise, it will be converted in the units of the domain.
    * The generated traffic matrix will be saved in the file <code>XMLTrafficMatrixFileName</code>.
    *
    * @param NETFLOWbaseDirectory the base directory containing aggregated NetFlow data
    * @param NETFLOWdirFileName directory and filename of the NetFlow aggregated data (found in NETFLOWbaseDirectory/id/ or NETFLOWbaseDirectory/rid/)
    * @param suffixes an array of potential suffixes for filenames
    * @param XMLTrafficMatrixFileName the name of the generated inter-domain XML traffic matrix
    * @return the inter domain traffic matrix
    * @throws Exception
    */
    public TrafficMatrixFile generateXMLTrafficMatrixfromNetFlow(String NETFLOWbaseDirectory,String NETFLOWdirFileName, String suffixes[], String XMLTrafficMatrixFileName) throws Exception{
       TrafficMatrixFile tmFile = generateXMLTrafficMatrixfromNetFlow(NETFLOWbaseDirectory, NETFLOWdirFileName, suffixes);

       JAXBContext jc = JAXBContext.newInstance("be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb");
       Marshaller m = jc.createMarshaller();
       m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
       m.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");
       m.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "http://totem.run.montefiore.ulg.ac.be/Schema/TrafficMatrix-v1_1.xsd");
       m.marshal(tmFile, new FileWriter(XMLTrafficMatrixFileName));

       return tmFile;
    }

   /**
    * This function reads NetFlow aggregated data and stores them in one XML inter-domain traffic matrix file.
    * If raw is true, the data will be represented in bytes per period. Otherwise, it will be converted in the units of the domain.
    *
    * @param NETFLOWbaseDirectory the base directory containing aggregated NetFlow data
    * @param NETFLOWdirFileName directory and filename of the NetFlow aggregated data (found in NETFLOWbaseDirectory/id/ or NETFLOWbaseDirectory/rid/)
    * @param suffixes an array of potential suffixes for filenames
    * @return the inter domain traffic matrix
    * @throws Exception
    */
   public TrafficMatrixFile generateXMLTrafficMatrixfromNetFlow(String NETFLOWbaseDirectory,String NETFLOWdirFileName, String suffixes[]) throws Exception{

        // Accessing aggregated NetFlow data
        List<Node> nodes = domain.getAllNodes();

       if (suffixes == null || suffixes.length == 0) {
           suffixes = new String[] {""};
       }

       File tempFile = null;
       boolean idNaming = BGPInfoChecker.isIdNaming(domain, NETFLOWbaseDirectory);

        String id;

       /*     // Now trying opening corresponding netflow traces which must be in aggregated text format!

       tempFile = new File (NETFLOWbaseDirectory+"/"+id+"/"+NETFLOWdirFileName);
       if (!tempFile.exists()){
       throw new Exception("Netflow file not found!  For router "+ id + " it should be in directory " + NETFLOWbaseDirectory + "/" + id + "/" + " and named:" + NETFLOWdirFileName);
       }


       // Little test with regular expressions (to enhance) to see if format is aggregated text format (source prefix, destination prefix, flow size)
       FileReader fr = new FileReader(tempFile);
       BufferedReader in = new BufferedReader(fr);
       String line;
       if ((line = in.readLine())!=null){
       Pattern urlPattern = Pattern.compile("((\\d{1,3}\\.?){4,4}/\\d{1,2})\\s+((\\d{1,3}\\.?){4,4}/\\d{1,2})\\s+(\\d+)");
       Matcher matcher = urlPattern.matcher(line);
       if (matcher.find()){
       //if (matcher.group(1)==null || matcher.group(3)==null || matcher.group(5) == null){
       //    throw new Exception("File seems to be in MRT ASCII MACHINE READABLE FORMAT but not some fields seem missing");
       //}         */
       /*System.out.println("Valeur de matcher.group(1) " + matcher.group(1));
       System.out.println("Valeur de matcher.group(2) " + matcher.group(2));
       System.out.println("Valeur de matcher.group(3) " + matcher.group(3));
       System.out.println("Valeur de matcher.group(4) " + matcher.group(4));
       System.out.println("Valeur de matcher.group(5) " + matcher.group(5));  */
       /*
       }else throw new Exception("File must be in text format src_prefix (..../. or ..../..) dst_prefix flow_size");
       }
       else throw new Exception("File is empty!");                   */

       // Creating the corresponding inter-domain XML traffic matrix

       ObjectFactory factory = new ObjectFactory();
       String line = null;

       try {
            TrafficMatrixFile tmFile = factory.createTrafficMatrixFile();

            /*InterTMType interTM = factory.createInterTMType();

            TrafficMatrixFileType.IntraTMOrInterTM intraTMOrInterTM = factory.createTrafficMatrixFileTypeIntraTMOrInterTM();
            intraTMOrInterTM.setInterTM(interTM);
            */

            InterTM interTM = factory.createInterTM();

            interTM.setASID(domain.getASID()); // domain on which netflow traces have been collected



        List<InterTMType.NodeType> nodesTM = interTM.getNode();

        int index=0;
        for (int i=0; i<nodes.size(); i++){


            // create a section for this node in the interdomain traffic matrix
            // JAXB code

            InterTMType.NodeType nodeType = factory.createInterTMTypeNodeType();
            nodeType.setId(nodes.get(i).getId());

            // end JAXB code

            id = idNaming ? nodes.get(i).getId() : nodes.get(i).getRid();

            int v=0;
            boolean gzipped = false;
            //System.out.println("suffixes.length = " + suffixes.length);
            for (v=0; v<suffixes.length; v++){
                tempFile = new File (NETFLOWbaseDirectory+ File.separatorChar +id+ File.separatorChar +NETFLOWdirFileName+suffixes[v]);
                //System.out.println("Looking for " + NETFLOWbaseDirectory+"/"+id+"/"+NETFLOWdirFileName+suffixes[v]);
                if (tempFile.exists()) {
                    gzipped = tempFile.getPath().endsWith(".gz");
                    break;
                } else {
                    tempFile = new File (NETFLOWbaseDirectory+ File.separatorChar +id+ File.separatorChar +NETFLOWdirFileName+suffixes[v]+".gz");
                    //System.out.println("Looking for " + NETFLOWbaseDirectory+"/"+id+"/"+NETFLOWdirFileName+suffixes[v]+".gz");
                    if(tempFile.exists()) {
                        gzipped = true;
                        break;
                    }
                }
            }
            if (v==suffixes.length){
                System.out.println("NetFlow file not found for router " + id);
                continue;
            }


            BufferedReader in;
            if(gzipped) {
                FileInputStream fis = new FileInputStream(tempFile);
                GZIPInputStream gis = new GZIPInputStream(fis);
                InputStreamReader isr = new InputStreamReader(gis);
                in = new BufferedReader(isr);
            } else {
                FileReader fr = new FileReader(tempFile);
                in = new BufferedReader(fr);
            }

            boolean test = false;

            while ((line=in.readLine())!=null){
                
                // IPv4 prefix | space | IPv4 prefix | space | traffic volume 
                Pattern urlPattern = Pattern.compile("((\\d{1,3}\\.?){4,4}/\\d{1,2})\\s+((\\d{1,3}\\.?){4,4}/\\d{1,2})\\s+(\\d+)");
                Matcher matcher = urlPattern.matcher(line);
                if (matcher.find()){
                    test = true;

                    // first check that this source prefix does not already exist
                    // JAXB code
                    InterTMType.NodeType.SrcType srcType = null;
                    List<InterTMType.NodeType.SrcType> srcTypeList = nodeType.getSrc();
                    for (int u=0; u<srcTypeList.size(); u++){
                        if (srcTypeList.get(u).getPrefix().equals(matcher.group(1))){
                            srcType = srcTypeList.get(u);
                            break;
                        }
                    }
                    if (srcType == null){
                        // create one!
                        srcType = factory.createInterTMTypeNodeTypeSrcType();
                        srcType.setPrefix(matcher.group(1));

                        srcTypeList.add(srcType);

                    }

                    // Now add the dst element, check if does not already exist for this src element
                    // If it does, add corresponding values!

                    InterTMType.NodeType.SrcType.DstType dstType = null;
                    List<InterTMType.NodeType.SrcType.DstType> dstTypeList = srcType.getDst();
                    for (int u=0; u<dstTypeList.size(); u++){
                        if (dstTypeList.get(u).getPrefix().equals(matcher.group(3))){
                            dstType = dstTypeList.get(u);
                            break;
                        }
                    }

                    if (dstType == null){
                        // create one!
                        dstType = factory.createInterTMTypeNodeTypeSrcTypeDstType();
                        dstType.setPrefix(matcher.group(3));
                        dstTypeList.add(dstType);

                    }

                    // update value

                    dstType.setValue(dstType.getValue()+Integer.parseInt(matcher.group(5)));

                }
                else throw new Exception("File must be in text format src_prefix (..../. or ..../..) dst_prefix flow_size");
            }

            in.close();
            
            // JAXB code

            if (test==true){
                nodesTM.add(index,nodeType); //prevent jaxb error
                index++;
            }


        }

           //tmFile.setIntraTMOrInterTM(intraTMOrInterTM);
           tmFile.setInterTM(interTM);

           /* Convert the data if necessary */
           if (!raw) {
               // Add bandwidth units
               if (!tmFile.isSetInfo()) {
                   tmFile.setInfo(factory.createInformation());
               }
               if (!tmFile.getInfo().isSetUnits()) {
                   tmFile.getInfo().setUnits(factory.createInformationUnitsType());
               }
               if (!tmFile.getInfo().getUnits().isSetUnit()) {
                   tmFile.getInfo().getUnits().setUnit(factory.createInformationUnitsTypeUnitType());
               }
               tmFile.getInfo().getUnits().getUnit().setType("bandwidth");
               tmFile.getInfo().getUnits().getUnit().setValue(BandwidthUnits.fromString(domain.getBandwidthUnit().toString().toLowerCase()));

               // Convert units
               List<InterTMType.NodeType> allNodes = interTM.getNode();
               for (InterTMType.NodeType node : allNodes) {
                   List<InterTMType.NodeType.SrcType> srcs = node.getSrc();
                   for (InterTMType.NodeType.SrcType src : srcs) {
                       List<InterTMType.NodeType.SrcType.DstType> dsts = src.getDst();
                       for (InterTMType.NodeType.SrcType.DstType dst : dsts) {
                           float value = dst.getValue();
                           value *= (8.0f*samplingRate)/(minutes*60);
                           //value is now in bps, convert it in domain units
                           value = domain.getBandwidthUnit().convert(BandwidthUnit.BPS, value);
                           dst.setValue(value);
                       }
                   }
               }
           }

           return tmFile;

       }
       catch(JAXBException e) {
           throw e;
       }
       catch(IOException e) {
           throw e;
       }
   }
}

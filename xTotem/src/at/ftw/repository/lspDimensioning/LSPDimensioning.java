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
package at.ftw.repository.lspDimensioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LspNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkCapacityExceededException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemAlgorithm;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.LibraryInitialisationException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;

/*
 * Changes:
 * --------
 *  29-Nov-2005 : Added the possibility to obtain the algorithm parameters (getStartAlgoParameters()). (GMO)
 *  08-Dec-2005 : Implement new getRunningParameters() from the TotemAlgorithm interface. (GMO)
 *  5-Mar-2007: add library loading in start and throw exception (GMO)
 */

/**
 * This class is the Java part of the LSPDimensioning algorithm.
 * 
 * <p>The <code>start</code> method needs the following parameters:
 * <ul>
 *  <li>SLOT_TIME: the length of one measurement slot in ms.</li>
 *  <li>MS_NU: the number of measurement slots in one resizing window.</li>
 *  <li>BE_NU: the number of resizing windows.</li>
 *  <li>W: the weighting factor which is used in calculating the exponential
 *      moving average (belongs to [0,1]).</li>
 *  <li>PS_TYPE: the specific provisioning scheme.</li>
 *  <li>DELAY: the delay bound in ms.</li>
 *  <li>EPSILON: the target delay violation probability.</li>
 *  <li>ASID: the AS ID of the domain containing the target LSP.</li>
 *  <li>LSPID: the target LSP.</li>
 * </ul>
 *
 * <p>Creation date : 20 juin 2005 11:09:53
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class LSPDimensioning implements TotemAlgorithm {
    
    private static Logger logger = Logger.getLogger(LSPDimensioning.class);
    
    private Lsp lsp;

    private HashMap runningParams = null;

    private static final ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();

    static {
        try {
        	params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
            params.add(new ParameterDescriptor("SLOT_TIME", "Length of one measurement slot in ms.", Float.class, new Float(0.0)));
            params.add(new ParameterDescriptor("MS_NU", "Number of measurement slots in one resizing window.", Integer.class, new Integer(0)));
            params.add(new ParameterDescriptor("W", "Weighting factor which is used in calculating the exponential moving average (belongs to [0,1]).", Float.class, new Float(0.0)));
            params.add(new ParameterDescriptor("PS_TYPE", "Specific provisioning scheme.", Integer.class, new Integer(0)));
            params.add(new ParameterDescriptor("DELAY", "Delay bound in ms.", Float.class, new Float(0.0)));
            params.add(new ParameterDescriptor("EPSILON", "Target delay violation probability.", Float.class, new Float(0.0)));
            params.add(new ParameterDescriptor("LSPID", "Target LSP.", String.class, ""));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }
    }    
    
    public void start(HashMap params) throws AlgorithmInitialisationException {


        try {
            System.loadLibrary("LSPDimensioning");
        } catch (UnsatisfiedLinkError e){
            throw new LibraryInitialisationException("Cannot load library LSPDimensioning.");
        }

        float slot_time = Float.parseFloat((String) params.get("SLOT_TIME"));
        int MS_nu = Integer.parseInt((String) params.get("MS_NU"));
        //int BE_nu = Integer.parseInt((String) params.get("BE_NU"));
        float w = Float.parseFloat((String) params.get("W"));
        int PS_type = Integer.parseInt((String) params.get("PS_TYPE"));
        float Delay = Float.parseFloat((String) params.get("DELAY"));
        float epsilon = Float.parseFloat((String) params.get("EPSILON"));

        runningParams = params;

        String asId = (String) params.get("ASID");
        Domain domain;
        if(asId == null) {
            domain = InterDomainManager.getInstance().getDefaultDomain();
        } else {
            try {
                domain = InterDomainManager.getInstance().getDomain(Integer.parseInt(asId));
            } catch(InvalidDomainException e) {
                logger.error("Cannot retrieve domain "+asId);
                return;
            }
        }
        try {
            lsp = domain.getLsp((String) params.get("LSPID"));
        } catch(LspNotFoundException e) {
            logger.error("Cannot retrieve LSP "+params.get("LSPID"));
            return;
        }
        JNILSPDimensioning.jniinitLSPDimensioning(slot_time, MS_nu, w, PS_type, Delay, epsilon);
    }
    
    public void stop() {
        runningParams = null;
        try {
            JNILSPDimensioning.jnikillLSPDimensioning();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public float computeBWAssign(float[] samples) {
        float bw = JNILSPDimensioning.jnicomputeBWAssign(samples);
        try {
            lsp.setReservation(bw);
        } catch (LinkCapacityExceededException e) {
            e.printStackTrace();
        }
        return bw;
    }

	public List<ParameterDescriptor> getStartAlgoParameters() {
		return (List<ParameterDescriptor>) params.clone();
	}

    public HashMap getRunningParameters() {
        return (runningParams == null) ? null : (HashMap)runningParams.clone();
    }
}

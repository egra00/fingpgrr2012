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
package at.ftw.scenario.model;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import at.ftw.scenario.model.jaxb.impl.LSPDimensioningImpl;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.scenario.model.Event;
import be.ac.ulg.montefiore.run.totem.scenario.model.EventResult;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;

/*
 * Changes:
 * --------
 *
 */

/**
 * This event runs the LSPDimensioning algorithm.
 *
 * <p>Creation date: 20-juin-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class LSPDimensioning extends LSPDimensioningImpl implements Event {

    private static final Logger logger = Logger.getLogger(LSPDimensioning.class);

    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a LSPDimensioning event...");
        
        List loadSamples = this.getLoadSample();
        float[] samples = new float[loadSamples.size()];
        int i = 0;
        for (Iterator iter = loadSamples.iterator(); iter.hasNext();) {
            float f = (Float) iter.next();
            samples[i++] = f;
        }
        
        at.ftw.repository.lspDimensioning.LSPDimensioning lspDim;
        try {
            lspDim = (at.ftw.repository.lspDimensioning.LSPDimensioning) RepositoryManager.getInstance().getAlgo("LSPDimensioning");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Weird NoSuchAlgorithmException. Message: "+e.getMessage());
            throw new EventExecutionException(e);
        }
        return new EventResult(null, "Bandwidth for new resizing window: "+lspDim.computeBWAssign(samples));
    }
}

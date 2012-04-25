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

package be.ac.ulg.montefiore.run.totem.scenario.model;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LspNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Mpls;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.LSPDeletionImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;

import javax.xml.bind.JAXBException;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class implements a LSP deletion event.
 *
 * <p>Creation date: 02-d�c.-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class LSPDeletion extends LSPDeletionImpl implements Event {

    private static final Logger logger = Logger.getLogger(LSPDeletion.class);
    
    public LSPDeletion() {}
    
    public LSPDeletion(String lspId) {
        setLspId(lspId);
    }
    
    public LSPDeletion(int asId, String lspId) {
        this(lspId);
        setASID(asId);
    }
    
    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a LSP deletion event - ASID: "+_ASID+" - LSP ID: "+_LspId);
        try {
            Domain domain;
            if(isSetASID()) {
                domain = InterDomainManager.getInstance().getDomain(_ASID);
            }
            else {
                domain = InterDomainManager.getInstance().getDefaultDomain();
            }
            Lsp lsp = domain.getLsp(_LspId);
            domain.removeLsp(lsp);

            logger.info("LSP " + _LspId + " deleted.");
            return new EventResult(lsp);
        }
        catch(LspNotFoundException e) {
            logger.error("Unknown LSP "+_LspId);
            throw new EventExecutionException(e);
        }
        catch(InvalidDomainException e) {
            logger.error("Unknown domain "+_ASID);
            throw new EventExecutionException(e);
        }
    }

}

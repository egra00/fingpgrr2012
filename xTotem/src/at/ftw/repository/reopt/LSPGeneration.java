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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.LspImpl;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.*;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomainBuilder;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedPath;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.BhandariKDisjointPath;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.CreatePathException;

/*
* Changes:
* --------
*
*/

/**
 * Generate a fullmesh of K disjoint LSPs between each nodes of a domain.
 * This class is used by the REOPT method.
 *
 * <p>Creation date: 09-May-2005 16:58:22
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class LSPGeneration {

    /**
     * Generate a fullmesh of nbParallelPath disjoint LSPs between each nodes of the domain ASID.
     * If it's not possible to find nbParallelPath disjoint path, it add the maximum possible disjoint path.
     *
     * @param ASID
     * @param nbParallelPath
     * @throws InvalidDomainException
     * @throws CloneNotSupportedException
     * @throws CreatePathException
     * @throws LinkNotFoundException
     * @throws NodeNotFoundException
     * @throws RoutingException
     * @throws LspAlreadyExistException
     * @throws LinkCapacityExceededException
     */
    public void generateLSP(int ASID, int nbParallelPath) throws InvalidDomainException, CloneNotSupportedException,
            CreatePathException, LinkNotFoundException, NodeNotFoundException,
            RoutingException, LspAlreadyExistException, LinkCapacityExceededException {
        Domain domain = InterDomainManager.getInstance().getDomain(ASID);
        SimplifiedDomain sDomain = SimplifiedDomainBuilder.build(domain);
        BhandariKDisjointPath bhandari = new BhandariKDisjointPath(sDomain);
        for(int srcId=0;srcId < sDomain.getNbNodes();srcId++) {
            for(int dstId=0;dstId < sDomain.getNbNodes();dstId++) {
                if ((sDomain.isNode(srcId)) && (sDomain.isNode(dstId)) && (srcId != dstId)) {
                    for (int nbPath = nbParallelPath;nbPath > 0;nbPath--) {
                        try {
                            SimplifiedPath pathList[] = bhandari.computeLinkDisjointPath(srcId,dstId,nbPath);
                            for (int i = 0; i < pathList.length; i++) {
                                SimplifiedPath simplifiedPath = pathList[i];
                                Path path = simplifiedPath.convert(domain);
                                Lsp lsp = new LspImpl(domain,domain.generateLspId(),0,path);
                                domain.addLsp(lsp);
                            }
                            break;
                        } catch (NoRouteToHostException e) {
                        } catch (InvalidPathException e) {
                            e.printStackTrace();
                        } catch (LspNotFoundException e) {
                            e.printStackTrace();
                        } catch (DiffServConfigurationException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}

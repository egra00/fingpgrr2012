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
package be.ac.ulg.montefiore.run.totem.domain.model.impl;

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.SPF;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

/*
* Changes:
* --------
* - 20-Mar-2006: Added stopOnError parameter to getPath. (GMO)
* - 21-Jun-2007: Always use ECMP to cache paths (but return only one path if ecmp is not desired) (GMO)
* - 04-Jul-2007: Fix bug. ECMP on by default (GMO)
* - 27-Feb-2008: Fix bug. Cache not cleared on nodeStatusChange event (GMO)
*/

/**
 * The SPFCache is designed to improve the performance of SPF path computation.
 *
 * It's based on the assumption that path does not change except when a link metric
 * change, a link status change, a link addition or a link remove. So it can
 * improve the performance if multiple call to getPath are done without metric, status or links changes.
 *
 * <p>Creation date: 07-Jul-2005 15:09:25
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class SPFCacheImpl implements SPFCache {

    private Domain domain;
    private SPF spf;
    private HashMap<Node,HashMap<Node,List<Path>>> cache;
    private SPFCacheListener listener;

    public SPFCacheImpl(Domain domain) {
        this.domain = domain;
        spf = new CSPF();
        cache = new HashMap<Node,HashMap<Node,List<Path>>>();
        listener = new SPFCacheListenerImpl(this);
        //System.out.println("Create SPFCache");
    }

    public SPFCacheListener getListener() {
        return listener;
    }

    /**
     * Get the SPF path between a source node and a destination node
     *
     * @param src the source node
     * @param dst the destination node
     * @return the SPF path
     * @throws NoRouteToHostException
     * @throws RoutingException
     */
    public Path getPath(Node src, Node dst) throws NoRouteToHostException, RoutingException {
        List<Path> paths = getPath(src,dst,true);
        if ((paths == null) || (paths.size() == 0)) {
            throw new RoutingException("ERROR in SPFCacheImpl.getPath : no path between " + src.getId()
                    + " and " + dst.getId());
        }
        return paths.get(0);
    }


    /**
     * Get all the SPF path between a source node and a destination node if the ECMP is true
     * and a single SPF otherwise
     *
     * @param src the source node
     * @param dst the destination node
     * @param ECMP true if equal cost multiple path is activated and false otherwise
     * @return a list of equal cost SPF
     * @throws NoRouteToHostException
     * @throws RoutingException
     */
    public List<Path> getPath(Node src, Node dst, boolean ECMP) throws NoRouteToHostException, RoutingException {
        return getPath(src, dst, ECMP, true);
    }

    public List<Path> getPath(Node src, Node dst, boolean ECMP, boolean stopOnError) throws NoRouteToHostException, RoutingException {
        if (!cache.containsKey(src) || (!cache.get(src).containsKey(dst))) {
            long time = System.currentTimeMillis();
            List<Path> paths = null;
            try {
               paths = spf.computeSPF(domain,src.getId(),true);
            } catch (NoRouteToHostException e) {
                if (stopOnError) throw e;
                else {
                    // calculate the path only for the given destination
                    paths = spf.computeSPF(domain, src.getId(), dst.getId(), true);
                }
            }
            int maxLength = 0;
            for (int i = 0; i < paths.size(); i++) {
                Path path = paths.get(i);
                if (!cache.containsKey(path.getSourceNode())) {
                    cache.put(path.getSourceNode(),new HashMap<Node,List<Path>>());
                }
                if (!cache.get(path.getSourceNode()).containsKey(path.getDestinationNode())) {
                    cache.get(path.getSourceNode()).put(path.getDestinationNode(),new ArrayList<Path>());
                    cache.get(path.getSourceNode()).get(path.getDestinationNode()).add(path);
                } else if (!cache.get(path.getSourceNode()).get(path.getDestinationNode()).contains(path)) {
                    cache.get(path.getSourceNode()).get(path.getDestinationNode()).add(path);
                }
                if (path.getLinkPath().size() > maxLength) {
                    maxLength = path.getLinkPath().size();
                }
            }
            time = System.currentTimeMillis() - time;
            //System.out.println("Add " + paths.size() + " path (size max " + maxLength + ") from " + src.getId() + " in the cache computed " + time + " ms");
        }

        List<Path> paths = cache.get(src).get(dst);
        if (ECMP || paths == null)
            return paths;
        else {
            List<Path> l = new ArrayList<Path>(1);
            l.add(paths.get(0));
            return l;
        }
    }


    /**
     * Remove the SPF path computed between source node an destination node from the cache
     *
     * This method must be used if the path have potentially changed.
     *
     * @param src the source node
     * @param dst the destination node
     */
    public void clearPath(Node src, Node dst) {
        if ((cache.containsKey(src)) && (cache.get(src).containsKey(dst))) {
            //System.out.println("Clear cache between " + src.getId() + " and " + dst.getId() );
            cache.get(src).remove(dst);
        }
    }

    /**
     * Remove all the SPF path in the cache     *
     */
    public void clear() {
        //System.out.println("Clear cache");
        cache.clear();
    }

    /**
     * Implement the SPFCacheListener.
     */
    private class SPFCacheListenerImpl extends DomainChangeAdapter implements SPFCacheListener {

        private SPFCache cache;

        public SPFCacheListenerImpl(SPFCache cache) {
            this.cache = cache;
        }

        public void linkMetricChangeEvent(Link link) {
            cache.clear();
        }

        public void linkStatusChangeEvent(Link link) {
            cache.clear();
        }

        public void removeLinkEvent(Link link) {
            cache.clear();
        }

        public void addLinkEvent(Link link) {
            cache.clear();
        }

        public void nodeStatusChangeEvent(Node node) {
            cache.clear();
        }

    }

}

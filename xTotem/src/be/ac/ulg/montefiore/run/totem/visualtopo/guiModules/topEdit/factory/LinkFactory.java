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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.LinkDecorator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.LinkDecoratorImpl;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory.idGenerator.LinkIdGenerator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory.idGenerator.SrcDstIdGenerator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.exception.NotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.*;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.LinkImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.LinkIgpImpl;

import javax.xml.bind.JAXBException;
import java.util.HashMap;

/*
* Changes:
* --------
*
*/

/**
* Create link decorators.
*
* @see LinkDecorator
*
* <p>Creation date: 5/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LinkFactory extends DomainElementFactory<LinkDecorator>{

    private LinkIdGenerator linkIdGenerator;

    /**
     * An id generator to generate each instance a unique id.
     * @param linkIdGenerator
     */
    public LinkFactory(LinkIdGenerator linkIdGenerator) {
        super();
        this.linkIdGenerator = linkIdGenerator;
    }

    /**
     * Create a link factory for the given domain using a {@link SrcDstIdGenerator}.
     * @param domain
     */
    public LinkFactory(DomainDecorator domain) {
        super();
        linkIdGenerator = new SrcDstIdGenerator(domain);
    }

    /**
     * Create an instance of a link with a generated id. The params used are src and dst.
     * The instance has minimum element (to, from and id).
     * @param params
     * @return
     */
    public LinkDecorator createInstance(HashMap<String, String> params) {
        Link link = new LinkImpl();

        try {
            link.setFrom(factory.createLinkFromType());
            //if (params != null && params.get("src") != null)
            //    link.getFrom().setNode(params.get("src"));

            link.setTo(factory.createLinkToType());
            //if (params != null && params.get("dst") != null)
            //    link.getTo().setNode(params.get("dst"));

            link.setId(linkIdGenerator.generate(link));
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return new LinkDecoratorImpl(link, null);
    }

    public LinkDecorator clone(LinkDecorator toClone) {
        Link linkClone = (Link)cloneObject(toClone.getLink());
        LinkIgp linkIgpClone = (LinkIgp)cloneObject(toClone.getLinkIgp());
        return new LinkDecoratorImpl(linkClone, linkIgpClone);
    }

    /**
     * Clone the given model, change its id to a generated one and set to and from node if given as parameters.
     * @param model
     * @param params
     * @return
     * @throws NotFoundException
     */
    public LinkDecorator createObject(String model, HashMap<String, String> params) throws NotFoundException {
        LinkDecorator dec = super.createObject(model, params);

        if (params != null && params.get("src") != null)
            dec.getLink().getFrom().setNode(params.get("src"));

        if (params != null && params.get("dst") != null)
            dec.getLink().getTo().setNode(params.get("dst"));

        String id = linkIdGenerator.generate(dec.getLink());
        dec.getLink().setId(id);
        if (dec.getLinkIgp() != null)
            dec.getLinkIgp().setId(id);

        return dec;
    }

    /**
     * Clone an {@link LinkIgp} object or a {@link Link} object
     * @param object
     * @return
     */
    protected Object cloneObject(Object object) {

        if (object instanceof Link) {

        Link link = (Link)object;
        Link clone = new LinkImpl();
        clone.setId(link.getId());
        if (link.isSetBw())
            clone.setBw(link.getBw());
        if (link.isSetDelay())
            clone.setDelay(link.getDelay());
        if (link.isSetDescription())
            clone.setDescription(link.getDescription());

        if (link.isSetFrom()) {
            clone.setFrom(new LinkImpl.FromTypeImpl());
        if (link.getFrom().isSetIf())
            clone.getFrom().setIf(link.getFrom().getIf());
        if (link.getFrom().isSetNode())
            clone.getFrom().setNode(link.getFrom().getNode());
        }

        if (link.isSetTo()) {
            clone.setTo(new LinkImpl.ToTypeImpl());
        if (link.getTo().isSetAs())
            clone.getTo().setAs(link.getTo().getAs());
        if (link.getTo().isSetIf())
            clone.getTo().setIf(link.getTo().getIf());
        if (link.getTo().isSetNode())
            clone.getTo().setNode(link.getTo().getNode());
        }

        if (link.isSetSrlgs()) {
            clone.setSrlgs(new LinkImpl.SrlgsTypeImpl());
            if (link.getSrlgs().isSetSrlg()) {
                for (Object o : link.getSrlgs().getSrlg()) {
                    Integer i = ((Integer)o).intValue();
                    clone.getSrlgs().getSrlg().add(i);
                }
            }
        }

        if (link.isSetStatus())
            clone.setStatus(StatusType.fromValue(link.getStatus().getValue()));
        if (link.isSetTechnology())
            clone.setTechnology(link.getTechnology());
        if (link.isSetType())
            clone.setType(LinkType.fromValue(link.getType().getValue()));

            return clone;
        } else if (object instanceof LinkIgp) {

        LinkIgp linkIgpClone = null;
            LinkIgp linkIgp = (LinkIgp)object;
            linkIgpClone = new LinkIgpImpl();
            if (linkIgp.isSetId())
                linkIgpClone.setId(linkIgp.getId());
            if (linkIgp.isSetStatic()) {
                linkIgpClone.setStatic(new LinkIgpImpl.StaticTypeImpl());
                if (linkIgp.getStatic().isSetAdmingroup())
                    linkIgpClone.getStatic().setAdmingroup(linkIgp.getStatic().getAdmingroup());
                if (linkIgp.getStatic().isSetDiffServ()) {
                    linkIgpClone.getStatic().setDiffServ(new LinkIgpImpl.StaticTypeImpl.DiffServTypeImpl());
                    if (linkIgp.getStatic().getDiffServ().isSetBc()) {
                        for (Object o : linkIgp.getStatic().getDiffServ().getBc()) {
                            LinkIgp.StaticType.DiffServType.BcType bcType = (LinkIgp.StaticType.DiffServType.BcType)o;
                            LinkIgp.StaticType.DiffServType.BcType bcTypeClone = new LinkIgpImpl.StaticTypeImpl.DiffServTypeImpl.BcTypeImpl();
                            if (bcType.isSetId())
                                bcTypeClone.setId(bcType.getId());
                            if (bcType.isSetValue())
                                bcTypeClone.setValue(bcType.getValue());
                            linkIgpClone.getStatic().getDiffServ().getBc().add(bcTypeClone);
                        }
                    }
                    if (linkIgp.getStatic().getDiffServ().isSetBcm())
                        linkIgpClone.getStatic().getDiffServ().setBcm(BcmType.fromValue(linkIgp.getStatic().getDiffServ().getBcm().getValue()));
                }
                if (linkIgp.getStatic().isSetMbw())
                    linkIgpClone.getStatic().setMbw(linkIgp.getStatic().getMbw());
                if (linkIgp.getStatic().isSetMetric())
                    linkIgpClone.getStatic().setMetric(linkIgp.getStatic().getMetric());
                if (linkIgp.getStatic().isSetMrbw())
                    linkIgpClone.getStatic().setMrbw(linkIgp.getStatic().getMrbw());
                if (linkIgp.getStatic().isSetTeMetric())
                    linkIgpClone.getStatic().setTeMetric(linkIgp.getStatic().getTeMetric());
            }
            if (linkIgp.isSetDynamic()) {
                linkIgpClone.setDynamic(new LinkIgpImpl.DynamicTypeImpl());
                if (linkIgp.getDynamic().isSetRbw()) {
                    linkIgpClone.getDynamic().setRbw(new LinkIgpImpl.DynamicTypeImpl.RbwTypeImpl());
                    if (linkIgp.getDynamic().getRbw().isSetPriority()) {
                        for (Object o : linkIgp.getDynamic().getRbw().getPriority()) {
                            LinkIgp.DynamicType.RbwType.PriorityType pType = (LinkIgp.DynamicType.RbwType.PriorityType)o;
                            LinkIgp.DynamicType.RbwType.PriorityType pTypeClone = new LinkIgpImpl.DynamicTypeImpl.RbwTypeImpl.PriorityTypeImpl();
                            if (pType.isSetId())
                                pTypeClone.setId(pType.getId());
                            if (pType.isSetValue())
                                pTypeClone.setValue(pType.getValue());
                            linkIgpClone.getDynamic().getRbw().getPriority().add(pTypeClone);
                        }
                    }
                }
            }
            return linkIgpClone;
        }
        //throw new Exception();
        return null;
    }
}

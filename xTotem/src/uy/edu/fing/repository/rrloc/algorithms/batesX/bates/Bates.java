package uy.edu.fing.repository.rrloc.algorithms.batesX.bates;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.iAlgorithm.BindAlgorithm;
import uy.edu.fing.repository.rrloc.iAlgorithm.ManagerRRLocAlgorithm;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpNeighborImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpRouterImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.DomainImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpNeighbor;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.GraphManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.TopoChooser;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;

public class Bates extends BindAlgorithm
{	
	private Domain domain;
	private int cant_rr;
	private String name;
	
	public Bates() {
		logger = Logger.getLogger(Bates.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new BatesAlgorithm();
		name = "Bates";
		thread = new Thread(this, name);
		
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
			params.add(new ParameterDescriptor("Amount RRs", "Amount of router reflectors (by default is two).", Integer.class, null));
		} catch (AlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
		
	@Override
	public Object getAlgorithmParams(HashMap params) 
	{
        String asId = (String) params.get("ASID");
        String rrs = (String) params.get("Amount RRs");
        cant_rr = 2;
        
        if(asId == null || asId.isEmpty()) {
        	domain = InterDomainManager.getInstance().getDefaultDomain();
        	if(domain == null){
	        	logger.error("There is no default domain");
	            return null;
        	}
        } else {
            try {
                domain = InterDomainManager.getInstance().getDomain(Integer.parseInt(asId));
            } catch(InvalidDomainException e) {
                logger.error("Cannot load domain " + asId);
                return null;
            }
        }
        
        if(rrs != null && !rrs.isEmpty() && Integer.parseInt(rrs) < domain.getNbNodes()) cant_rr = Integer.parseInt(rrs);
		
		
		// Topología IGP representada en un grafo jung 
		Graph<Node, Link> jIGPTopology = new UndirectedSparseMultigraph<Node, Link>();
		
		// Cargo un nodo Jung con los datos necesarios para realizar el algoritmo
		Operations.addAllVertices(jIGPTopology, (Set<Node>)(new HashSet<Node>(domain.getAllNodes())));
		for (Link link : domain.getAllLinks()) {
			try {
				// Elimino la direccionalidad del grafo
				if (jIGPTopology.findEdge(link.getDstNode(), link.getSrcNode()) == null) {
					jIGPTopology.addEdge(link, link.getSrcNode(), link.getDstNode());
				}
			} catch (NodeNotFoundException e) {
				logger.error("Parsing Totem domain to Jung graph");
				e.printStackTrace();
			}
		}
	
		BatesAlgorithm.Params par = new BatesAlgorithm.Params();
		par.cant_rr = cant_rr;
		par.graph = jIGPTopology;
		
		return par;
	}
	
	@Override
	public Object initAlgorithmResult() {
		return new ArrayList<iBGPSession>();
	}
	
	@Override
	public void log(Object algorithmResult) {
		List<iBGPSession> iBGPTopology = (List<iBGPSession>)algorithmResult;
		
		logger.debug("iBGP sessions ("+iBGPTopology.size()+")");
		for (iBGPSession session: iBGPTopology) {
			logger.debug(session.getIdLink1() + " - " + session.getIdLink2() + " -> " + session.getSessionType());
		}
		
	}

}

package uy.edu.fing.repository.rrloc.algorithms.batesX.batesY;

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

public class BatesY extends BindAlgorithm 
{

	private Domain domain;
	private String name;
	private int pops;
	
	public BatesY() {
		logger = Logger.getLogger(BatesY.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new BatesYAlgorithm();
		name = "BatesY";
		thread = new Thread(this, name);
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
			params.add(new ParameterDescriptor("Amount PoPs", "Amount of PoPs (by default is one).", Integer.class, null));
		} catch (AlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
		
	@Override
	public Object getAlgorithmParams(HashMap params) 
	{
        String asId = (String) params.get("ASID");
        String popsS = (String) params.get("Amount PoPs");
        pops = 1;
        
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
		
        if(popsS != null && !popsS.isEmpty() && Integer.parseInt(popsS) <= domain.getNbNodes()) pops = Integer.parseInt(popsS);
     
        
		// Topologia IGP representada en un grafo jung 
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
		
		BatesYAlgorithm.Params param = new BatesYAlgorithm.Params();
		param.graph = jIGPTopology;
		param.pops = pops;
		
		return param;
	}
	
	@Override
	public Object initAlgorithmResult() {
		return new ArrayList<iBGPSession>();
	}

	public void saveTopo() {
        TopoChooser saver = new TopoChooser();
        File file = saver.saveTopo(MainWindow.getInstance());
        if (file != null) {
        	GraphManager.getInstance().updateLocation();
            try {
                String filename = file.getAbsolutePath();
                if (!filename.toLowerCase().endsWith(".xml")) {
                    filename = filename.concat(".xml");
                }
                InterDomainManager.getInstance().saveDomain(domain.getASID(), filename);
            } catch (Exception e) {
                MainWindow.getInstance().errorMessage("The domain could not be saved");
            }
        }
    }

	@Override
	public void dumpResultInDomain(Object algorithmResult) throws Exception {
		List<iBGPSession> iBGPTopology = (List<iBGPSession>)algorithmResult;
		
		ManagerRRLocAlgorithm.getInstance().lock(domain.getASID());
		
		String information = "BGP information will change for the domain " + domain.getASID() + "\n";
		String description = (domain.getDescription() == null || domain.getDescription().isEmpty() ? "No description" : domain .getDescription() ) + "\n";
		String action = "This action saves the previous version and will delete all existing information. Would you like to continue?" + "\n";
		String title = "@Run " + name + " algorithm reports";
		
        int n = JOptionPane.showConfirmDialog(MainWindow.getInstance(), information + description + action, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        
        if (n == JOptionPane.YES_OPTION) 
        {
        	saveTopo();
        }
        	
    	ObjectFactory factory = new ObjectFactory();
    	
		// Se elimina toda posible configuracion previa
		((DomainImpl)domain).removeBgpRouters();
		
		// Todos los routers tendrán sesiones bgp
		for (Node router : domain.getAllNodes()) {
			BgpRouter bgpRouter = factory.createBgpRouter();
	        bgpRouter.setId(router.getId());
	        bgpRouter.setRid(router.getRid());
	        domain.addBgpRouter((BgpRouterImpl)bgpRouter);
		}
		
		// Creo las sesiones
		for (iBGPSession session : iBGPTopology) {
			
			BgpRouterImpl router1 = (BgpRouterImpl)domain.getBgpRouter(session.getIdLink1());
			BgpRouterImpl router2 = (BgpRouterImpl)domain.getBgpRouter(session.getIdLink2());
			
			// El router2, el destino, sera reflector en caso que router1 sea su cliente
			router2.setReflector(
					router2.isReflector() ||
					session.getSessionType().equals(iBGPSessionType.client));
			
			BgpNeighbor bgpNeighbor = factory.createBgpNeighbor();
			bgpNeighbor.setIp(router2.getRid());
			bgpNeighbor.setAs(domain.getASID());
			if (router1.getNeighbors() == null) {
				router1.setNeighbors(factory.createBgpRouterNeighborsType());
			}
			router1.getNeighbors().getNeighbor().add((be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor)bgpNeighbor);
			
			// El router1, el origen, sera cliente en caso de tener una session de tipo client.
			((BgpNeighborImpl)bgpNeighbor).setReflectorClient(
					((BgpNeighborImpl)bgpNeighbor).isReflectorClient() ||
					session.getSessionType().equals(iBGPSessionType.client));
			
			bgpNeighbor = factory.createBgpNeighbor();
			bgpNeighbor.setIp(router1.getRid());
			bgpNeighbor.setAs(domain.getASID());
			if (router2.getNeighbors() == null) {
				router2.setNeighbors(factory.createBgpRouterNeighborsType());
			}
			router2.getNeighbors().getNeighbor().add((be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor)bgpNeighbor);
		}
        
        ManagerRRLocAlgorithm.getInstance().unlock(domain.getASID());
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
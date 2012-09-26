package uy.edu.fing.repository.rrloc.algorithms.cbr;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

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
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpFilter;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpFilter.RuleType;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpFilter.RuleType.ActionType;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpNeighbor;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpNeighbor.FiltersType;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.NodeType;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.NodeImpl;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.GraphManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.TopoChooser;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;

@SuppressWarnings("unchecked")
public class Cbr extends BindAlgorithm {
	private Domain domain;
	private String name;
		
	public Cbr() {
		logger = Logger.getLogger(Cbr.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new CbrAlgorithm();
		name = "Cbr";
		thread = new Thread(this, name);
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
		} catch (AlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAlgorithmParams(HashMap params) {
		Object[] restParams = new Object[2];
		
		
        String asId = (String) params.get("ASID");
      
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
		
		restParams[0] = jIGPTopology;
		
		// Cargo los routers de borde
		List<Node> nextHops = new ArrayList<Node>();
		for (Node router : domain.getAllNodes()) {
			if(((NodeImpl)router).getType()==NodeType.EDGE){
				nextHops.add(router);
			}
		}
		
		restParams[1] = nextHops;
				
		return restParams;
	}
	
	@Override
	public Object initAlgorithmResult() {
		return new Object[4];
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
		Object[] result = (Object[])algorithmResult;
		
		// Cargo los resultados
		
		HashMap<String, HashMap<String, ArrayList<iBGPSessionColored>>> coloredTopology = 
				(HashMap<String, HashMap<String,ArrayList<iBGPSessionColored>>>)result[1];
		
		HashMap<String, ArrayList<iBGPColor>> colorsPerRouter = (HashMap<String, ArrayList<iBGPColor>>)result[2];
		
		List<Node> nextHops = (List<Node>)result[3];
		
		ManagerRRLocAlgorithm.getInstance().lock(domain.getASID());
		
		String information = "BGP information will change for the domain " + domain.getASID() + "\n";
		String description = (domain.getDescription() == null || domain.getDescription().isEmpty() ? "No description" : domain .getDescription() ) + "\n";
		String action = "This action saves the previous version and will delete all existing information. Would you like to continue?" + "\n";
		String title = "@Run " + name + " algorithm reports";
		
        int resp = JOptionPane.showConfirmDialog(MainWindow.getInstance(), information + description + action, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (resp == JOptionPane.YES_OPTION) {
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
		
		// Para cada router
		
		//Set<String> procecedRouters = new HashSet<String>();
		for (Entry<String, HashMap<String, ArrayList<iBGPSessionColored>>> router : coloredTopology.entrySet()) {
			//procecedRouters.add(router.getKey());
			
			// Para cada enlace
			for (Entry<String, ArrayList<iBGPSessionColored>> peer : router.getValue().entrySet()) {
				// Tengo las sesiones "bidirecionales", cuando miro desde un router a un peer que ya procese lo salteo
				//if (procecedRouters.contains(peer.getKey())) {
					//continue;
				//}
				BgpRouterImpl router1 = (BgpRouterImpl)domain.getBgpRouter(router.getKey());
				BgpRouterImpl router2 = (BgpRouterImpl)domain.getBgpRouter(peer.getKey());
				
				router2.setReflector(true);
				
				BgpNeighbor bgpNeighbor = factory.createBgpNeighbor();
				bgpNeighbor.setIp(router2.getRid());
				bgpNeighbor.setAs(domain.getASID());
				((BgpNeighborImpl)bgpNeighbor).setReflectorClient(false);
				if (router1.getNeighbors() == null) {
					router1.setNeighbors(factory.createBgpRouterNeighborsType());
				}
				router1.getNeighbors().getNeighbor().add((be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor)bgpNeighbor);	
			}
		}
		
		// FIXME, falta crear los filtros que agregan la community, pero para esto es necesario 
		// conocer el next-hop: ¿cual es el next-hop? no estamos reprecentando esto.
		
		for (Node r : nextHops) {
			BgpRouterImpl router = (BgpRouterImpl)domain.getBgpRouter(r.getId());
			
			BgpNeighbor bgpNeighbor = factory.createBgpNeighbor();
			bgpNeighbor.setIp("");
			bgpNeighbor.setAs(domain.getASID() + 1);
			((BgpNeighborImpl)bgpNeighbor).setReflectorClient(false);
			if (router.getNeighbors() == null) {
				router.setNeighbors(factory.createBgpRouterNeighborsType());
			}
			
			if (bgpNeighbor.getFilters() == null) {
				BgpFilter inFilter = factory.createBgpFilter();
				
				FiltersType filterType = factory.createBgpNeighborFiltersType();
				filterType.setInFilter(inFilter);
				
				bgpNeighbor.setFilters(filterType);
			} else if (bgpNeighbor.getFilters().getInFilter() == null) {
				BgpFilter inFilter = factory.createBgpFilter();
				
				FiltersType filterType = factory.createBgpNeighborFiltersType();
				filterType.setInFilter(inFilter);
				
				bgpNeighbor.getFilters().setInFilter(inFilter);
			}
			
			ActionType actionType = factory.createBgpFilterRuleTypeActionType();
			actionType.setValue(
					"bgp router " + router.getRid() + "\n" +
					"peer ##next-hop for " + router.getRid()+ "##\n" + 
					"	filter in \n" +
					"		add-rule \n" +
					"			match any \n" + 
					"			action \"community add " + router.getId() + " \"\n" +
					"			exit \n" +
					"		exit \n" +
					"	exit \n" +
					"exit \n"
			);
			
			logger.debug(
					"bgp router " + router.getRid() + "\n" +
							"peer ##next-hop for " + router.getRid()+ "##\n" + 
							"	filter in \n" +
							"		add-rule \n" +
							"			match any \n" + 
							"			action \"community add " + router.getId() + " \"\n" +
							"			exit \n" +
							"		exit \n" +
							"	exit \n" +
							"exit \n"
			);
			
			RuleType rule = factory.createBgpFilterRuleType();
			rule.getAction().add(actionType);
			
			bgpNeighbor.getFilters().getInFilter().setRule(rule);
			
			router.getNeighbors().getNeighbor().add((be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor)bgpNeighbor);
		}

		// Para cada router
		for (Entry<String, HashMap<String, ArrayList<iBGPSessionColored>>> router : coloredTopology.entrySet()) {
			// Para cada enlace
			for (Entry<String, ArrayList<iBGPSessionColored>> peer : router.getValue().entrySet()) {
				// Para cada color que conoce el router
				for (iBGPColor color : colorsPerRouter.get(router.getKey())) {
					// Si este enlace no tiene este color, agrego filtro
					if (!peer.getValue().contains(color)) {
						BgpRouterImpl router1 = (BgpRouterImpl)domain.getBgpRouter(router.getKey());
						BgpRouterImpl router2 = (BgpRouterImpl)domain.getBgpRouter(peer.getKey());
						
						// Busco el vecino
						BgpNeighbor neighbor = null;
						for (Object o : router1.getNeighbors().getNeighbor()) {
							neighbor = (BgpNeighbor)o;
							if (neighbor.getIp().equals(peer.getKey())) {
								break;
							}
						}
						if (neighbor == null) {
							logger.warn("Unknown BGP neighbor" + peer.getKey() + " to " + router.getKey());
							continue;
						}
						
						if (neighbor.getFilters() == null) {
							BgpFilter outFilter = factory.createBgpFilter();
							
							FiltersType filterType = factory.createBgpNeighborFiltersType();
							filterType.setOutFilter(outFilter);
							
							neighbor.setFilters(filterType);
						} else if (neighbor.getFilters().getOutFilter() == null) {
							BgpFilter outFilter = factory.createBgpFilter();
							
							FiltersType filterType = factory.createBgpNeighborFiltersType();
							filterType.setOutFilter(outFilter);
							
							neighbor.getFilters().setOutFilter(outFilter);
						}
						
						ActionType actionType = factory.createBgpFilterRuleTypeActionType();
						actionType.setValue(
								"bgp router " + router1.getRid() + "\n" +
								"peer " + router2.getRid() + "\n" + 
								"	filter out \n" +
								"		add-rule \n" +
								"			match \"community is " + color + " \"\n" + 
								"			action deny \n" +
								"			exit \n" +
								"		exit \n" +
								"	exit \n" +
								"exit \n"
						);
						
						logger.debug(
								"bgp router " + router1.getRid() + "\n" +
								"peer " + router2.getRid() + "\n" + 
								"	filter out \n" +
								"		add-rule \n" +
								"			match \"community is " + color + " \"\n" + 
								"			action deny \n" +
								"			exit \n" +
								"		exit \n" +
								"	exit \n" +
								"exit \n"
						);
						
						RuleType rule = factory.createBgpFilterRuleType();
						rule.getAction().add(actionType);
						
						neighbor.getFilters().getOutFilter().setRule(rule);
					}
				}
			}
		}  
        
        ManagerRRLocAlgorithm.getInstance().unlock(domain.getASID());
	}

	@Override
	public void log(Object algorithmResult) {
		Object[] ret = (Object[])algorithmResult;
		Set<iBGPSessionColored> iBGPTopology = (Set<iBGPSessionColored>)ret[0];
		
		logger.debug("iBGP sessions ("+iBGPTopology.size()+")");
		for (iBGPSessionColored session: iBGPTopology) {
			logger.debug(session.getIdLink1() + " - " + session.getIdLink2() + " -> " + session.getSessionType() + " -> " + session.getColor());
		}
		
	}
}

package uy.edu.fing.repository.rrloc.iAlgorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemAlgorithm;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.GraphManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.TopoChooser;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class BindAlgorithm implements Runnable, TotemAlgorithm {
	
	protected Logger logger;
	protected HashMap runningParams = null;
	protected Thread thread;
	protected RRLocAlgorithm algorithm;
	protected ArrayList<ParameterDescriptor> params;
	
	protected Domain domain;
	protected String name;
    
    Object algorithmParams;
    Object algorithmResult;
	
	/*
	 * Used for initialize the parameters of input
	 * 
	 * @param domain
	 * @return the algorithm parameters 
	 * 
	 */
	public abstract Object getAlgorithmParams(HashMap params);
	
	/*
	 * It is called for initialize the result parameter
	 * 
	 * @return the parameter result
	 * 
	 */
	public abstract Object initAlgorithmResult();
	
	/*
	 * It is called when a algorithm end and is necessary impact
	 * the changes in the domain
	 * 
	 * @param domain
	 * @param algorithmResult is the result of the algorithm
	 * 
	 */
	public abstract void dumpResultInDomain(Object algorithmResult) throws Exception;
	
	/*
	 * Used in debug mode, log the result of algorithm in logger
	 * 
	 * @param algorithmResult is the result of the algorithm
	 * 
	 */
	public abstract void log(Object algorithmResult);
	
	
	public RRLocAlgorithm getAlgorithm()
	{
		return algorithm;
	}
	
	@Override
	public void start(HashMap params) throws AlgorithmInitialisationException {
		runningParams = params;
        logger.debug("Starting...");
        
        algorithmParams = getAlgorithmParams(params);
        algorithmResult = initAlgorithmResult();
        
        
		if(algorithmParams != null && algorithmResult != null) {
			if (!MainWindow.cliMode()) {
			    thread.start();
			    logger.debug("Finish");
			}
			else {
				run();
			}
		}
		else
			logger.debug("Incorrect initialization of input parameters or output parameters");
	}

	@Override
	public void stop() {
		runningParams = null;
		thread.interrupt();
	}

	@Override
	public void run() 
	{
        algorithm.run(algorithmParams, algorithmResult);
        
        log(algorithmResult);
        
        String path = domain.getURI().getPath();
        path = path.endsWith(".xml") ? path.substring(0, path.length() - 4) : path;
        path += "-" + name;
        
        try {
        	dumpResultInDomain(algorithmResult);
        	if (MainWindow.cliMode()) {
        		save(new File(path));
        	}
        	else {
        		saveTopo();
        	}
		} catch (Exception e) {
			logger.error("Dumping iBGP topology in Totem domain");
			e.printStackTrace();
		}
	}
	
	
	@Override
	public HashMap getRunningParameters() {
		return (runningParams == null) ? null : (HashMap)runningParams.clone();
	}

	@Override
	public List<ParameterDescriptor> getStartAlgoParameters() {
		
		return (List<ParameterDescriptor>) params.clone();
	}
	
	public void saveTopo() {
    	File file = null;
    	
		String information = "BGP information will change for the domain " + domain.getASID() + "\n";
		String description = (domain.getDescription() == null || domain.getDescription().isEmpty() ? "No description" : domain .getDescription() ) + "\n";
		String action = "This action saves the previous version and will delete all existing information. Would you like to continue?" + "\n";
		String title = "@Run " + name + " algorithm reports";
		
        int n = JOptionPane.showConfirmDialog(MainWindow.getInstance(), information + description + action, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (n != JOptionPane.YES_OPTION) {
        	return;
        }
        
        TopoChooser saver = new TopoChooser();
        file = saver.saveTopo(MainWindow.getInstance());
    	
        save(file);
    }
    
	protected void save(File file) {
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

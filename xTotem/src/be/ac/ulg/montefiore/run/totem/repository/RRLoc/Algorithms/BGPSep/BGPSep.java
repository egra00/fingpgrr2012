package be.ac.ulg.montefiore.run.totem.repository.RRLoc.Algorithms.BGPSep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemAlgorithm;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BGPSep implements TotemAlgorithm {
	private static Logger logger = Logger.getLogger(BGPSep.class);
	
	private static final ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();
	private static HashMap runningParams = null;
    
    static {
        try {
        	params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void start(HashMap params) throws AlgorithmInitialisationException {
		runningParams = params;

        String asId = (String) params.get("ASID");
        Domain domain;
        if(asId == null) {
        	domain = InterDomainManager.getInstance().getDefaultDomain();
        	if(domain == null){
	        	logger.error("There is no default domain");
	            return;
        	}
        } else {
            try {
                domain = InterDomainManager.getInstance().getDomain(Integer.parseInt(asId));
            } catch(InvalidDomainException e) {
                logger.error("Cannot load domain " + asId);
                return;
            }
        }
        logger.debug("Starting BGPSEP...");
        BGPSepBackend.BGPSepAlgorithm(domain);
        logger.debug("BGPSEP finish");
	}

	@Override
	public void stop() {
		runningParams = null;
	}

	@Override
	public HashMap getRunningParameters() {
		return (runningParams == null) ? null : (HashMap)runningParams.clone();
	}

	@Override
	public List<ParameterDescriptor> getStartAlgoParameters() {
		
		return (List<ParameterDescriptor>) params.clone();
	}

}

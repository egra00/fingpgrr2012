/* TOTEM-v@VERSION@ @DATE@*/

/*
@LICENCE@
*/
package be.ac.ulg.montefiore.run.totem.trafficMatrix;

import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.generation.InterDomainTrafficMatrixGeneration;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.generation.POPPOPTrafficMatrixGeneration;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.persistence.TrafficMatrixFactory;
import be.ac.ulg.montefiore.run.totem.domain.bgp.BgpFieldsCreation;
import be.ac.ucl.ingi.totem.repository.CBGP;

import java.util.HashMap;
import java.util.List;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class generates one example traffic matrix for 5 minutes based on Abilene information.
 *
 * <p>Creation date: 03-05-2005
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 */ 

public class AbileneExampleTM {




    public static void main(String[] args) throws Exception {


        be.ac.ulg.montefiore.run.totem.core.Totem.init();
        String topologyName = "../run-totem/examples/abilene/abilene.xml";
        String iBGPTopologyName = "../run-totem/examples/abilene/abileneiBGP.xml";
        String BGPTopologyName = "../run-totem/examples/abilene/abileneBGP.xml";
        String clusterFileName = "../run-totem/examples/abilene/bgp/cluster.txt";
        String dumpBaseDirectory= "../run-totem/examples/abilene/bgp";
        String netflowBaseDirectory="../run-totem/examples/abilene/netflow";
        String trafficmatrixBaseDirectory="../run-totem/examples/abilene/trafficmatrix/";
        String dumpTime = "20050101";


        //add iBGP and eBGP sessions to Abilene.

        BgpFieldsCreation bgpFieldsCreation = new BgpFieldsCreation();

        System.out.println("Initializing iBGP fields");
        bgpFieldsCreation.addiBGPFullMesh(topologyName,iBGPTopologyName);

        System.out.println("Initializing eBGP fields");
        bgpFieldsCreation.addeBGPSessions(iBGPTopologyName,BGPTopologyName,dumpBaseDirectory,"2005/2005-01/2005-01-01/rib."+dumpTime);


        InterDomainManager.getInstance().loadDomain(BGPTopologyName,true,true,false);

        InterDomainTrafficMatrixGeneration interDomainTrafficMatrixGeneration = new InterDomainTrafficMatrixGeneration();

        CBGP cbgp = new CBGP();


        cbgp.start();
        
        try{
        cbgp.simRun();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        cbgp.runCmd("bgp options msg-monitor /tmp/CBGPmsgmonitor.txt");

        POPPOPTrafficMatrixGeneration trafficMatrixGeneration = new POPPOPTrafficMatrixGeneration();

        HashMap hashMap = trafficMatrixGeneration.readCluster(InterDomainManager.getInstance().getDefaultDomain(),clusterFileName, cbgp, dumpBaseDirectory, "2005/2005-01/2005-01-01/rib."+dumpTime);

        System.out.println("Running simulation");
        try{
        cbgp.simRun();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        String[] suffixes = new String[1];
        suffixes[0] = "";

        interDomainTrafficMatrixGeneration.generateXMLTrafficMatrixfromNetFlow(InterDomainManager.getInstance().getDefaultDomain(),netflowBaseDirectory,"2005/2005-01/2005-01-01/ft-v05.2005-01-01",suffixes,trafficmatrixBaseDirectory.concat("interDomain.2005-01-01.0000.xml"));

        TrafficMatrix tm = trafficMatrixGeneration.generateTrafficMatrix(null,hashMap, InterDomainManager.getInstance().getDefaultDomain(),cbgp,trafficmatrixBaseDirectory.concat("interDomain.2005-01-01.0000.xml"));

        // For all elements of this traffic matrix, update values according to the fact that these are bytes (total) and we want kbps, so have to multiply by 8
        // and divide by 7200 all values

        /*List<Node> nodes = InterDomainManager.getInstance().getDefaultDomain().getAllNodes();

        for (int v=0; v<nodes.size(); v++){
            for (int w=0; w<nodes.size(); w++){
                float value = tm.get(nodes.get(v).getId(),nodes.get(w).getId());
                float updateValue = value*(8.0f/900);
                tm.set(nodes.get(v).getId(),nodes.get(w).getId(),updateValue);
            }
        }
        */

        TrafficMatrixFactory.saveTrafficMatrix(trafficmatrixBaseDirectory.concat("intraDomain.2005-01-01.0000.xml"),tm);

    }
}


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
package be.ac.ulg.montefiore.run.totem.core;

import java.awt.Font;
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.apache.log4j.xml.DOMConfigurator;

import be.ac.ulg.montefiore.run.totem.domain.exception.DomainAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.netController.facade.NetworkControllerManager;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.scenario.facade.ScenarioManager;
import be.ac.ulg.montefiore.run.totem.scenario.generation.GenerateScenario;
import be.ac.ulg.montefiore.run.totem.scenario.generation.LSPWorstCaseLinkFailure;
import be.ac.ulg.montefiore.run.totem.scenario.generation.OneLSPbyDemand;
import be.ac.ulg.montefiore.run.totem.scenario.generation.PrimaryBackupFullMesh;
import be.ac.ulg.montefiore.run.totem.scenario.generation.WorstCaseLinkFailure;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.Scenario;
import be.ac.ulg.montefiore.run.totem.scenario.persistence.ScenarioFactory;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.util.XMLFilesValidator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;

/*
 * Changes:
 * --------
 *
 * 29-Apr-2005: add the dir parameter (JL).
 * 09-May-2005: decreasing order by default for the fullmesh (FSK)
 * 24-May-2005: new XML file validator (JL).
 * 18-Jan-2006: force English locale (GMO).
 * 27-Jan-2006: stop algorithms on exit (JLE).
 * 04-Jan-2007: allow any method for backup scenarios (GMO)
 * 08-Jan-2007: add CLI parameter stopOnError (GMO)
 * 10-May-2007: display Goodbye message only when using CLI (GMO)
 * 31-May-2007: Add -init to start the GUI and then execute a scenaro (GMO)
 * 09-Aug-2007: rewrite scenario generation CLI code, chart is now with -c switch (GMO)
 */

/**
 * Totem class of the application. The init method must be used to initialise
 * the logging and preference manager. This class displays the CLI menu.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class Totem {

    /**
     * Init the logging and preference manager
     */
    public static void init(){
        String log4jFile = "log4j.xml";
        File file = new File(log4jFile);
        if (!file.exists()) {
            String defaultLog4jFile = "/resources/log4j.xml";
            URL url = Totem.class.getResource(defaultLog4jFile);
            if (url == null) {
                System.out.println("Cannot find default log config file in JAR : " + defaultLog4jFile);
                System.exit(0);
            }
            System.out.println("Init Logging from JAR with config file : /resources/log4j.xml");
            DOMConfigurator.configure(url);
        } else {
            System.out.println("Init Logging with config file : log4j.xml");
            DOMConfigurator.configure(log4jFile);
        }
        PreferenceManager.getInstance().getPrefs();

        Locale.setDefault(Locale.ENGLISH);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                RepositoryManager.getInstance().stopAlgorithms();
            }
        });
    }
    
    /**
     * Print the usage of the toolbox using the CLI
     */
    private static void printUsage() {
        System.out.println("Usage : ");
        System.out.println("\t -rrloc_bgpsep <topology.xml> : start BGPSEP algorithm running over the topology specifies in topology.xml.");
        System.out.println("\t -rrloc_bgpsepB <topology.xml> : start BGPSEPBackbone algorithm running over the topology specifies in topology.xml.");
        System.out.println("\t -rrloc_bgpsepD <topology.xml> : start BGPSEPD algorithm running over the topology specifies in topology.xml.");
        System.out.println("\t -rrloc_bgpsepS <topology.xml> : start BGPSEPS algorithm running over the topology specifies in topology.xml.");
        System.out.println("\t -rrloc_cbr <topology.xml> : start CBR algorithm running over the topology specifies in topology.xml.");
        System.out.println("\t -rrloc_fullmesh <topology.xml> : start FULLMESH algorithm running over the topology specifies in topology.xml.");
        System.out.println("\t -rrloc_optimal <topology.xml> : start OPTIMAL algorithm running over the topology specifies in topology.xml.");
        System.out.println("\t -rrloc_zhang <topology.xml> : start ZHANG algorithm running over the topology specifies in topology.xml.");
        System.out.println("\t -rrloc_bates <topology.xml> : start BATES algorithm running over the topology specifies in topology.xml.");
        System.out.println("\t -rrloc_batesY <topology.xml> : start BATESY algorithm running over the topology specifies in topology.xml.");
        System.out.println("\t -rrloc_batesZ <topology.xml> : start BATESZ algorithm running over the topology specifies in topology.xml.");
        System.out.println("\t -demo : start the GUI with increased font size (so it can be projected).");
        System.out.println("\t -s <scenario.xml> [stopOnError]: execute a scenario. If stopOnError is present, scenario execution will stop on first error.");
        System.out.println("\t -dir <directory_name> [stopOnError]: execute all the scenario files in the specified directory.  If stopOnError is present, each scenario execution will stop on first error.");
        System.out.println("\t -validate <file.xml> [<schema location>] : validate the xml file");
        System.out.println("\t -gs : generate a scenario <type>");
        System.out.println("\t\t <type> can be : ");
        System.out.println("\t\t\t fullmesh : generate a full mesh of LSPs");
        System.out.println("\t\t\t wca : simulate a failure of each link and display the load");
        System.out.println("\t\t\t backup : generates a full mesh of LSPs followed by a full mesh of backups");
        System.out.println("\t\t if type equals fullmesh, following arguments are needed");
        System.out.println("\t\t\t -f <scenario.xml> -d <domain.xml> -t <traffic_matrix.xml> -m <method>");
        System.out.println("\t\t if type equals backup, the following argument is also needed");
        System.out.println("\t\t\t -backuptype <backuptype> where backuptype can be LOCAL or GLOBAL");
        System.out.println("\t\t if type equals wca, following arguments are needed");
        System.out.println("\t\t\t <wca_type> -f <scenario.xml> -d <domain.xml> -t <traffic_matrix.xml> -m <method> [-c <chart.eps.png.jpg>]");
        System.out.println("\t\t\t where <wca_type> can be \"SPF_MCF\" or \"CSPF_DAMOTE\"");
        System.out.println("\t\t\t Note that charts cannot be created with \"MCF\" as method.");
    }

    private static void endError(String msg) {
        System.out.println();
        System.out.println("SYNTAX ERROR");
        System.out.println(msg);
        System.out.println();
        printUsage();
        System.exit(1);
    }

    /**
     * Totem methods start the GUI by default
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        init();
        System.out.println("Welcome in the toolbox");
        if (args.length > 0) {
            if ((args[0].equals("-validate")) && ((args.length == 2) || (args.length == 3))) {
                XMLFilesValidator validator = new XMLFilesValidator();
                if(args.length == 2) {
                    if (validator.validate(args[1])) {
                        System.out.println("File " + args[1] + " validated!");
                    } else {
                        System.out.println("File " + args[1] + " contains errors (see log file)!");
                    }
                } else {
                    if (validator.validate(args[1], args[2])) {
                        System.out.println("File " + args[1] + " validated!");
                    } else {
                        System.out.println("File " + args[1] + " contains errors (see log file)!");
                    }                    
                }
            } else if (args[0].equals("-s") && ((args.length == 2) || ((args.length == 3) && args[2].equals("stopOnError")))) {
                String scenarioName = args[1];
                try {
                    ScenarioManager.getInstance().setStopOnError(args.length == 3);
                    ScenarioManager.getInstance().loadScenario(scenarioName);
                } catch(NullPointerException e) {
                    System.err.println("Invalid scenario file "+scenarioName);
                    System.err.println();
                    System.exit(1);
                }
                ScenarioManager.getInstance().executeScenario();
            } else if ((args[0].equals("-dir")) && ((args.length == 2) || ((args.length == 3) && args[2].equals("stopOnError")))) {
                String dirName = args[1];
                File dir = new File(dirName);
                if(!dir.isDirectory()) {
                    endError("Not a directory!");
                }
                File[] scenarios = dir.listFiles(new FileFilter() {
                    public boolean accept(File f) {
                        return ((!f.isDirectory()) && (f.getName().endsWith(".xml")));
                    }
                });
                System.out.println();
                System.out.println("Executing scenario files...");
                System.out.println("===========================");
                for (int i = 0; i < scenarios.length; ++i) {
                    try {
                        ScenarioManager.getInstance().loadScenario(scenarios[i].getPath());
                        ScenarioManager.getInstance().setStopOnError(args.length == 3);
                    } catch(NullPointerException e) {
                        System.err.println();
                        System.err.println("Invalid scenario file "+scenarios[i]);
                        continue;
                    }
                    System.out.println();
                    System.out.println(i+". Executing "+scenarios[i].getPath());
                    ScenarioManager.getInstance().executeScenario();
                    InterDomainManager.getInstance().removeAllDomains();
                    NetworkControllerManager.getInstance().removeAllNetworkControllers();
                    RepositoryManager.getInstance().stopAlgorithms();
                    TrafficMatrixManager.getInstance().removeAllTrafficMatrices();
                }
                System.exit(0);
            } else if (args[0].equals("-gs")) {
                if (args.length < 2) endError("Please specify the type of scenario to generate.");

                HashMap<String, String> params = new HashMap<String, String>();

                int i = args[1].equals("wca") ? 3 : 2;
                while (i < args.length-1) {
                    if (args[i].startsWith("-")) {
                        if (params.put(args[i], args[i+1]) != null) {
                            System.out.println("Multiple values for parameter " + args[i]);
                            printUsage();
                            System.exit(1);
                        }
                    } else {
                        endError("Parse error");
                    }
                    i+=2;
                }

                if (i != args.length) {
                    endError("Parse error");
                }

                String scenarioFileName = params.get("-f");
                if (scenarioFileName == null) endError("Scenario destination filename must be specified.");

                String domainName = params.get("-d");
                if (domainName == null) endError("Domain filename must be specified.");

                String trafficMatrixName = params.get("-t");
                if (trafficMatrixName == null) endError("Traffic matrix filename must be specified.");

                String methodName = params.get("-m");
                if (methodName == null) endError("The method must be specified.");

                if (args[1].equals("wca")) {
                    String wcaType = args[2];

                    String chartFileName = params.get("-c");
                    boolean createChart = chartFileName != null;

                    int nbKeys = createChart ? 5 : 4;
                    if (params.size() != nbKeys) endError("Too many arguments");

                    HashMap startAlgoParameters = new HashMap();
                    
                    if(wcaType.equals("SPF_MCF")) {
                        Scenario scenario = new WorstCaseLinkFailure(domainName,trafficMatrixName,methodName,startAlgoParameters, createChart, chartFileName).generateScenario();
                        ScenarioFactory.saveScenario(scenarioFileName,scenario);
                    }
                    else if(wcaType.equals("CSPF_DAMOTE")) {
                        HashMap algoParameters = new HashMap();
                        Scenario scenario;
                        if (createChart)
                            scenario = new LSPWorstCaseLinkFailure(domainName, trafficMatrixName, GenerateScenario.NO_ORDER, methodName, startAlgoParameters, algoParameters, chartFileName).generateScenario();
                        else
                            scenario = new LSPWorstCaseLinkFailure(domainName, trafficMatrixName, GenerateScenario.NO_ORDER, methodName, startAlgoParameters, algoParameters).generateScenario();
                        ScenarioFactory.saveScenario(scenarioFileName, scenario);
                    }
                    else {
                        endError("Wrong WCA type");
                        return;
                    }
                } else if (args[1].equals("fullmesh")){
                    int nbKeys = 4;
                    if (params.size() != nbKeys) endError("Too many arguments");

                    HashMap startAlgoParameters = null;
                    HashMap algoParameters = null;
                    startAlgoParameters = new HashMap();
                    algoParameters = new HashMap();

                    Scenario scenario = new OneLSPbyDemand(domainName,trafficMatrixName,GenerateScenario.DECREASING_ORDER,methodName,startAlgoParameters,algoParameters).generateScenario();
                    ScenarioFactory.saveScenario(scenarioFileName,scenario);

                } else if (args[1].equals("backup")){
                    String backupType = params.get("-backuptype");
                    if (backupType == null) endError("The type of backup must be specified (LOCAL or GLOBAL");

                    int nbKeys = 5;
                    if (params.size() != nbKeys) endError("Too many arguments");

                    HashMap startAlgoParameters = null;
                    HashMap algoParameters = null;
                    startAlgoParameters = new HashMap();
                    algoParameters = new HashMap();

                    Scenario scenario = new PrimaryBackupFullMesh(domainName,trafficMatrixName,methodName,startAlgoParameters,algoParameters,backupType).generateScenario();
                    ScenarioFactory.saveScenario(scenarioFileName,scenario);
                }
                else {
                    endError("Unknown scenario type: " + args[1]);
                    return;
                }

            } else if (args[0].equals("-init")) {
                /* Start the GUI and execute a scenario */
                MainWindow.getInstance();
                final String filename = args[1];
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        ScenarioManager.getInstance().loadScenario(filename);
                        ScenarioManager.getInstance().executeScenario();
                    }
                });
            } else if (args[0].equals("-demo")) {
                // increase default font size by 150% so it can be read when projected
                UIDefaults defaults = UIManager.getDefaults();
                Enumeration keys = defaults.keys();
                while (keys.hasMoreElements()) {
                    Object key = keys.nextElement();
                    Object value = defaults.get(key);
                    if (value instanceof Font) {
                        Font font = (Font) value;
                        float size = font.getSize2D();
                        UIManager.put(key, new FontUIResource(font.deriveFont(size * 1.5f)));
                    }
                }
                MainWindow.getInstance();
                return;
            }
            else if (args[0].equals("-rrloc_bgpsep") || 
            		args[0].equals("-rrloc_bgpsepB") || 
            		args[0].equals("-rrloc_bgpsepD") || 
            		args[0].equals("-rrloc_bgpsepS") || 
            		args[0].equals("-rrloc_cbr") || 
            		args[0].equals("-rrloc_fullmesh") || 
            		args[0].equals("-rrloc_optimal") || 
            		args[0].equals("-rrloc_zhang") ||
            		args[0].equals("-rrloc_bates") ||
            		args[0].equals("-rrloc_batesY") ||
            		args[0].equals("-rrloc_batesZ")) {
            	
            	if (args.length < 2)
            		endError("Please specify the topology file.");
            	
            	HashMap<String, String> params = new HashMap<String, String>();
            	
            	int i = 2;
            	while (i < args.length-1) {
                    if (args[i].startsWith("-")) {
                        if (params.put(args[i], args[i+1]) != null) {
                            System.out.println("Multiple values for parameter " + args[i]);
                            printUsage();
                            System.exit(1);
                        }
                    } else {
                        endError("Parse error");
                    }
                    i+=2;
                }

                if (i != args.length) {
                    endError("Parse error");
                }

            	
            	File file = new File(args[1]);

                if (!file.exists() || file.isDirectory()) {
                    endError("File does not exist or is a directory.");
                    return;
                }
                
                try {
                    InterDomainManager.getInstance().loadDomain(file.getAbsolutePath(), true, false, false);
                } catch (InvalidDomainException e1) {
                    e1.printStackTrace();
                    endError("Invalid Domain file " + (e1.getMessage() == null ? "" : (": " + e1.getMessage())));
                } catch (DomainAlreadyExistException e1) {
                	e1.printStackTrace();
                	endError("A domain with the same ASID already exists.");
                } catch (Exception e) {
                    e.printStackTrace();
                    endError("An unexpected error occurs: " + e.getClass().getSimpleName());
                }

                if (args[0].equals("-rrloc_bgpsep")) {
                	RepositoryManager.getInstance().startAlgo("BGPSep", params);
                }
                else if (args[0].equals("-rrloc_bgpsepB")) {
                	RepositoryManager.getInstance().startAlgo("BGPSepBackbone", params);
                }
                else if (args[0].equals("-rrloc_bgpsepD")) {
                	RepositoryManager.getInstance().startAlgo("BGPSepD", params);
                }
				else if (args[0].equals("-rrloc_bgpsepS")) {
					RepositoryManager.getInstance().startAlgo("BGPSepS", params);
				}
				else if (args[0].equals("-rrloc_cbr")) {
					RepositoryManager.getInstance().startAlgo("Cbr", params);
				}
				else if (args[0].equals("-rrloc_fullmesh")) {
					RepositoryManager.getInstance().startAlgo("FullMesh", params);
				}
				else if (args[0].equals("-rrloc_optimal")) {
					RepositoryManager.getInstance().startAlgo("Optimal", params);
				}
				else if (args[0].equals("-rrloc_zhang")) {
					RepositoryManager.getInstance().startAlgo("Zhang", params);
				}
				else if (args[0].equals("-rrloc_bates")) {
					RepositoryManager.getInstance().startAlgo("Bates", params);
				}
				else if (args[0].equals("-rrloc_batesY")) {
					RepositoryManager.getInstance().startAlgo("BatesY", params);
				}
				else if (args[0].equals("-rrloc_batesZ")) {
					RepositoryManager.getInstance().startAlgo("BatesZ", params);
				}
                System.out.println("Success! running in background");
                System.out.println("Wait for the results");
                
            }
            else {
                printUsage();
            }
            
            System.out.println("Goodbye...");
        } else {
            MainWindow.getInstance();
        }
    }

}

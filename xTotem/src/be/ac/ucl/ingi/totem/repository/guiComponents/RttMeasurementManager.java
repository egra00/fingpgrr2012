package be.ac.ucl.ingi.totem.repository.guiComponents;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.xml.bind.JAXBException;

import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;

import javax.swing.JLabel;

import be.ac.ucl.ingi.totem.repository.CBGP;
import be.ac.ucl.ingi.totem.repository.RttMeasurementReportInterpreter;
import be.ac.ucl.ingi.totem.repository.RttMeasurementRequestGeneration;
import be.ac.ucl.ingi.totem.repository.guiComponents.RttMeasuresGraphImplementations.NormalizedIncreasingDelayGraph;
import be.ac.ucl.ingi.totem.repository.guiComponents.RttMeasuresGraphImplementations.SimpleIncreasingDelayGraph;
import be.ac.ucl.ingi.totem.repository.model.CBGPSimulator;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.generation.POPPOPTrafficMatrixGeneration;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.AbstractGUIModule;

/**
 * 
 * @author Thomas Dekens
 * 
 */
public class RttMeasurementManager extends AbstractGUIModule {

	private static String GUI_MODULE_NAME = "RttMeasurement";

	private static MainWindow mainWindow = MainWindow.getInstance();

	private Hashtable<String, RttMeasurementReportInterpreter> importedMeasurementReports = new Hashtable<String, RttMeasurementReportInterpreter>();

	public JMenu getMenu() {
		JMenu menu = new JMenu(getName());

		JMenuItem menuItem;
		
		menuItem = new JMenuItem("Generate RttMeasurementRequest");
		menuItem.addActionListener(new GenerateMeasurementRequestActionListener());
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Import RttMeasurementReport");
		menuItem.addActionListener(new ImportMeasurementActionListener());
		menu.add(menuItem);

		JMenu menu2 = new JMenu("Graph");

		menuItem = new JMenuItem("Simple inscreasing delay graph");
		menuItem.addActionListener(new GraphActionListener(
				GraphActionListener.SIMPLE_INC_DEL));
		menu2.add(menuItem);

		menuItem = new JMenuItem("Normalized inscreasing delay graph");
		menuItem.addActionListener(new GraphActionListener(
				GraphActionListener.NORM_INC_DEL));
		menu2.add(menuItem);

		menu.add(menu2);

		return menu;
	}

	public boolean loadAtStartup() {
		return true;
	}

	public String getName() {
		return GUI_MODULE_NAME;
	}

	/**
	 * 
	 * @author tdekens
	 * TODO choose a name for the import
	 */
	private class ImportMeasurementActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String baseDirectory;
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser
					.setDialogTitle("Select base directory containing measurement reports");
			jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (jFileChooser.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
				try {
					RttMeasurementReportInterpreter ri = new RttMeasurementReportInterpreter(
							jFileChooser.getSelectedFile().getAbsolutePath());
					ri.setDescription(jFileChooser.getSelectedFile().getName());
					ImportNameFrame inf = new ImportNameFrame(ri);
				} catch (JAXBException ex) {
					System.out
							.println("Error unmarshalling files in directory "
									+ jFileChooser.getSelectedFile().getPath());
					ex.printStackTrace();
				}
			}
		}

		private class ImportNameFrame implements ActionListener {
			private JTextField commentField = null;

			private JDialog dialog = null;

			RttMeasurementReportInterpreter ri;

			public ImportNameFrame(RttMeasurementReportInterpreter ri) {
				this.ri = ri;
				dialog = new JDialog(MainWindow.getInstance(),
						"Enter a name for this import");
				dialog.setSize(200, 70);
				dialog.setContentPane(setupUI());
				dialog.setLocationRelativeTo(dialog.getParent());
				dialog.setVisible(true);
			}

			private JPanel setupUI() {
				JPanel jp = new JPanel();
				jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
				commentField = new JTextField();
				jp.add(commentField);

				JButton accept = new JButton("Ok");
				accept.addActionListener(this);
				jp.add(accept);
				dialog.getRootPane().setDefaultButton(accept);
				return jp;
			}

			public void actionPerformed(ActionEvent e) {
				String name = commentField.getText();
				ri.setDescription(name);
				importedMeasurementReports.put(name, ri);
				System.out.println("Reports correctly imported");
				dialog.dispose();
			}
		}
	}
	
	
	private class GenerateMeasurementRequestActionListener implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			new MeasurementRequestOptionsChooser();
			
		}
		
		private class MeasurementRequestOptionsChooser implements ActionListener{
			
			String[] units = {"s", "ms", "µs", "ns"};
			JDialog dialog;
			JCheckBox poisson, randomize, takeFirst;
			JComboBox timingUnits;
			JTextField numQueries, numObs, lambda, dateField;
			RttMeasurementRequestGeneration mrg;
			
			
			public MeasurementRequestOptionsChooser(){
				mrg = new RttMeasurementRequestGeneration();
				boolean poissOn = mrg.isPoissonDistribution();
				
				poisson = new JCheckBox("Poisson distribution");
				poisson.setSelected(poissOn);
				poisson.addActionListener(new PoissonCheckBoxActionListener());
				randomize = new JCheckBox("Randomize subnet list");
				randomize.setEnabled(poissOn);
				randomize.setSelected(mrg.isRandomizeDNSInList());
				takeFirst = new JCheckBox();
				takeFirst.setSelected(mrg.isTakeFirstMeasureDirectly());
				takeFirst.setEnabled(poissOn);
				lambda = new JTextField(Double.toString(mrg.getLamba()));
				lambda.setEnabled(poissOn);
				numObs = new JTextField(Integer.toString(mrg.getNumberOfObservations()));
				numObs.setEnabled(poissOn);
				
				timingUnits = new JComboBox(units);
				timingUnits.setSelectedIndex(0);
				timingUnits.setEnabled(poissOn);
				numQueries = new JTextField(Integer.toString(mrg.getNumberOfQueries()));
				dateField= new JTextField(mrg.getStartTime().getTime().toString());
				
				dialog = new JDialog();
				dialog.setContentPane(setupUI());
				dialog.setLocationRelativeTo(dialog.getParent());
				dialog.pack();
				dialog.setVisible(true);
				
				
				
				
				
			}
			
			private JPanel setupUI(){
				JPanel jPanel = new JPanel();
				jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.PAGE_AXIS));
				JPanel xPanel;
				jPanel.add(new JLabel("Select measurement request options for the current default domain"));
				xPanel = new JPanel();
				xPanel.setLayout(new BoxLayout(xPanel, BoxLayout.LINE_AXIS));
				xPanel.add(new JLabel("number of Queries"));
				xPanel.add(numQueries);
				xPanel.add(new JLabel("Earliest query start date"));
				xPanel.add(dateField);
				jPanel.add(xPanel);
				xPanel = new JPanel();
				xPanel.setLayout(new BoxLayout(xPanel, BoxLayout.LINE_AXIS));
				xPanel.add(new JLabel("Poisson distributions"));
				xPanel.add(poisson);
				jPanel.add(xPanel);
				xPanel = new JPanel();
				xPanel.setLayout(new BoxLayout(xPanel, BoxLayout.LINE_AXIS));
				xPanel.add(new JLabel("Lamba"));
				xPanel.add(lambda);
				xPanel.add(new JLabel("units"));
				xPanel.add(timingUnits);
				xPanel.add(new JLabel("number of observations"));
				xPanel.add(numObs);
				jPanel.add(xPanel);
				xPanel = new JPanel();
				xPanel.setLayout(new BoxLayout(xPanel, BoxLayout.LINE_AXIS));
				xPanel.add(new JLabel("Randomize subnets in list"));
				xPanel.add(randomize);
				xPanel.add(new JLabel("Take first measure directly"));
				xPanel.add(takeFirst);
				jPanel.add(xPanel);
				JButton button = new JButton("OK");
				button.addActionListener(this);
				jPanel.add(button);
				return jPanel;
			}
			
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
				Domain domain = InterDomainManager.getInstance().getDefaultDomain();
				String netFlowTMFile;
				JFileChooser fChooser = new JFileChooser();
				fChooser.setDialogTitle("Choose an interdomain traffic matrix");
				fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (fChooser.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
					netFlowTMFile = fChooser.getSelectedFile().getAbsolutePath();
				}else{
					return;
				}
				Hashtable<String, String[]> subDNS;
				fChooser = new JFileChooser();
				fChooser.setDialogTitle("Choose a subnet-dns file");
				fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (fChooser.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
					try {
						subDNS= RttMeasurementRequestGeneration.readSubnetDnsFile(fChooser.getSelectedFile().getAbsolutePath());
					} catch (FileNotFoundException e1) {
						e1.printStackTrace();
						return;
					} catch (IOException e1) {
						e1.printStackTrace();
						return;
					}
				}else{
					return;
				}
				CBGP cbgp;
				try {
	                cbgp = (CBGP) RepositoryManager.getInstance().getAlgo("CBGP");
	            }catch (NoSuchAlgorithmException ee) {
	                mainWindow.errorMessage("Please start the CBGP algorithm before using it!");
	                return;
	            }
	            String clustFile;
	            fChooser = new JFileChooser();
	            fChooser.setDialogTitle("Select Cluster file");
	            fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				if (fChooser.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
					clustFile = fChooser.getSelectedFile().getAbsolutePath();
				}else{
					return;
				}
				String bgpBase;
	            fChooser = new JFileChooser();
	            fChooser.setDialogTitle("Select BGP base Directory");
				fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fChooser.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
					bgpBase = fChooser.getSelectedFile().getAbsolutePath();
				}else{
					return;
				}
				new SelectBGPFileNameFrame(domain,netFlowTMFile,subDNS, cbgp, clustFile, bgpBase);
			}
			
			
			private class SelectBGPFileNameFrame implements ActionListener {
				private JTextField commentField = null;

				private JDialog dialog = null;
				Domain domain;
				Hashtable<String, String[]> subdns;
				String netFlowTMFile, bgpBaseDir,clustFile;
				CBGP cbgp;
				

				public SelectBGPFileNameFrame(Domain domain, String netFlowTMFile, Hashtable<String, String[]> subdns, CBGP cbgp, String clustFile, String bgpBaseDir) {
					
					this.domain = domain;
					this.netFlowTMFile = netFlowTMFile;
					this.subdns = subdns;
					this.bgpBaseDir = bgpBaseDir;
					this.clustFile = clustFile;
					this.cbgp = cbgp;					
					dialog = new JDialog(MainWindow.getInstance(),
							"Enter BGP rib dir and filename");
					dialog.setSize(200, 70);
					dialog.setContentPane(setupUI());
					dialog.setLocationRelativeTo(dialog.getParent());
					dialog.setVisible(true);
				}

				private JPanel setupUI() {
					JPanel jp = new JPanel();
					jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
					commentField = new JTextField();
					jp.add(commentField);

					JButton accept = new JButton("Ok");
					accept.addActionListener(this);
					jp.add(accept);
					dialog.getRootPane().setDefaultButton(accept);
					return jp;
				}

				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
					String bgpFileName = commentField.getText();
					String outDir;
					JFileChooser fChooser = new JFileChooser();
					fChooser.setDialogTitle("Select OutputDirectory");
					fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					if (fChooser.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
						outDir = fChooser.getSelectedFile().getAbsolutePath();
					}else{
						return;
					}
					POPPOPTrafficMatrixGeneration pop = new POPPOPTrafficMatrixGeneration(domain);
					HashMap clust;
					try {
						clust = pop.readCluster(clustFile, bgpBaseDir, bgpFileName);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						dialog.dispose();
						return;
					}
					
					mrg.generateRttMeasurementRequests(domain, netFlowTMFile, subdns, cbgp, clust, outDir);
					
					System.out.println("Measurement requests generated");
					dialog.dispose();
				}
			}
			
			public class PoissonCheckBoxActionListener implements ActionListener{
				boolean box;
				public void actionPerformed(ActionEvent e) {
					box = poisson.isSelected();
					randomize.setEnabled(box);
					takeFirst.setEnabled(box);
					lambda.setEnabled(box);
					numObs.setEnabled(box);
					timingUnits.setEnabled(box);					
				}
				
			}
			
		}
		
	}

	private class GraphActionListener implements ActionListener {
		public static final int SIMPLE_INC_DEL = 1;

		public static final int NORM_INC_DEL = 2;

		int type;

		public GraphActionListener(int type) {
			this.type = type;
		}

		public void actionPerformed(ActionEvent e) {
			RttMeasuresGraph graph = null;

			switch (type) {
			case SIMPLE_INC_DEL:
				graph = new RttMeasuresGraphImplementations().new SimpleIncreasingDelayGraph();
				break;
			case NORM_INC_DEL:
				graph = new RttMeasuresGraphImplementations().new NormalizedIncreasingDelayGraph();
				break;
			}

			new ChooseReports(graph);
		}

		/*
		 * public void actionPerformed(ActionEvent e) {
		 * RttMeasurementReportInterpreter[] ri= new
		 * RttMeasurementReportInterpreter[1]; ri[0] =
		 * (RttMeasurementReportInterpreter)importedMeasurementReports.values().toArray()[0];
		 * String[][] keys= new String[1][]; keys[0] = new String[7]; keys[0][0] =
		 * null; keys[0][1] = "ATLA"; keys[0][2] = "NYCM"; keys[0][3] = "STTL";
		 * keys[0][4] = "WASH"; keys[0][5] = "IPLS"; keys[0][6] = "LOSA";
		 * 
		 * NormalizedIncreasingDelayGraph g = new
		 * RttMeasuresGraphImplementations().new
		 * NormalizedIncreasingDelayGraph(); g.setParameters(ri, keys);
		 * JFreeChart chart = g.graph();
		 * 
		 * ChartFrame frame = new ChartFrame("bla", chart); frame.pack();
		 * frame.setVisible(true);
		 *  }
		 */

		private class ChooseReports implements ActionListener {

			RttMeasuresGraph graph;

			private JDialog dialog;

			String msg;

			JCheckBox[] boxes;

			String[] keys;

			public ChooseReports(RttMeasuresGraph graph) {

				this.graph = graph;
				dialog = new JDialog(MainWindow.getInstance(),
						"Choose imported reports");
				if (graph.maxRttMeasurementReportInterpretors() > 0) {
					msg = "Please select a minimum of "
							+ graph.minRttMeasurementReportInterpretors()
							+ " and a maximum of"
							+ graph.maxRttMeasurementReportInterpretors()
							+ " imported results";
				} else {
					msg = "Please select at least "
							+ graph.minRttMeasurementReportInterpretors()
							+ " imported results";
				}
				dialog.setContentPane(setupUI());
				dialog.setLocationRelativeTo(dialog.getParent());
				dialog.pack();
				dialog.setVisible(true);

			}

			private JPanel setupUI() {
				JPanel jPanel = new JPanel();
				jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.PAGE_AXIS));

				jPanel.add(new JLabel(msg));

				JCheckBox box;
				keys = importedMeasurementReports.keySet().toArray(
						new String[0]);
				boxes = new JCheckBox[keys.length];
				for (int i = 0; i < keys.length; i++) {
					box = new JCheckBox(keys[i]);
					jPanel.add(box);
					box.setActionCommand(keys[i]);
					boxes[i] = box;
				}

				JButton jButton = new JButton("OK");
				jButton.addActionListener(this);
				jPanel.add(jButton);

				return jPanel;
			}

			public void actionPerformed(ActionEvent e) {
				int n = 0;
				for (int i = 0; i < boxes.length; i++) {
					if (boxes[i].isSelected()) {
						n++;
					}
				}
				if (n < graph.minRttMeasurementReportInterpretors()) {
					mainWindow.errorMessage(msg);
					return;
				}
				if (graph.maxRttMeasurementReportInterpretors() > 0
						&& n > graph.maxRttMeasurementReportInterpretors()) {
					mainWindow.errorMessage(msg);
					return;
				}

				dialog.dispose();
				/*
				int[] indexes = new int[n];
				int k = 0;
				for (int i = 0; i < boxes.length; i++) {
					if (boxes[i].isSelected()) {
						indexes[k] = i;
						k++;
					}
				}*/
			
				RttMeasurementReportInterpreter[] ri = new RttMeasurementReportInterpreter[n];
				int k =0;
				for(int i = 0; i<boxes.length ; i++){
					if(boxes[i].isSelected()){
						ri[k] = importedMeasurementReports.get(boxes[i].getActionCommand());
						k++;
					}
				}
				
				if (graph.mustChooseNode()) {
					new ChooseNodes(graph, ri);
				} else {
					graph.setParameters(ri, null);
					new GraphDisplay(graph.graph());
				}

			}

			private class ChooseNodes implements ActionListener{
				
				JCheckBox[][] boxes;
				String msg;
				JDialog dialog;
				RttMeasurementReportInterpreter[] ri;
				RttMeasuresGraph graph;
				

				public ChooseNodes(RttMeasuresGraph graph, RttMeasurementReportInterpreter[] ri) {
					
					this.ri = ri;
					this.graph = graph;
					JCheckBox box;
					dialog = new JDialog();
					if(graph.mustBeSimilarNodes()){
						boxes = new JCheckBox[1][];
						LinkedList<String> nodesOut;
						String tmpNode;
						Iterator<String> nodeIter;
						nodesOut = new LinkedList<String>(ri[0].getNodeNames());
						for(int l= 1; l<ri.length; l++){
							nodeIter = nodesOut.iterator();
							nodesOut = new LinkedList<String>();
							while(nodeIter.hasNext()){
								tmpNode = nodeIter.next();
								if(ri[l].getRttResult(tmpNode)!=null){
									nodesOut.add(tmpNode);
								}
							}
						}
						boxes[0] = new JCheckBox[nodesOut.size() + 1];
						nodeIter = nodesOut.iterator();
						int l =0;
						while(nodeIter.hasNext()){
							tmpNode = nodeIter.next();
							box = new JCheckBox(tmpNode);
							box.setActionCommand(tmpNode);
							boxes[0][l] = box;
							l++;
						}
						box = new JCheckBox("Aggregate");
						box.setActionCommand(RttMeasurementReportInterpreter.AGGREGATE);
						boxes[0][l] = box;
						
					}else{
						boxes = new JCheckBox[ri.length][];
						String[] nodes;
						
						for(int i = 0; i<ri.length; i++){
							nodes = ri[i].getNodeNames().toArray(new String[0]);
							boxes[i] = new JCheckBox[nodes.length +1];
							for(int j = 0; j<nodes.length; j++){
								box = new JCheckBox(nodes[j]);
								box.setActionCommand(nodes[j]);
								boxes[i][j] = box;
							}
							box = new JCheckBox("Aggregate");
							box.setActionCommand(RttMeasurementReportInterpreter.AGGREGATE);
							boxes[i][nodes.length] = box;
						}
					}
					
					if( ! (graph.maxNodesChosen()> 0)){
						msg="Please choose a minimum of " + graph.minNodesChosen();
					}else{
						msg+= " and a maximum of" + graph.maxNodesChosen();
					}
					msg += " nodes";
					
					dialog.setContentPane(setupUI());
					dialog.setLocationRelativeTo(dialog.getParent());
					dialog.pack();
					dialog.setVisible(true);
					
					
				}
				
				private JPanel setupUI(){
					JPanel jPanel = new JPanel();
					jPanel.setLayout(new BoxLayout(jPanel,BoxLayout.PAGE_AXIS));
					jPanel.add(new JLabel(msg));
					JPanel xPanel = new JPanel();
					xPanel.setLayout(new BoxLayout(xPanel, BoxLayout.LINE_AXIS));
					JPanel yPanel;
					if(graph.mustBeSimilar){
						yPanel = new JPanel();
						yPanel.setLayout(new BoxLayout(yPanel, BoxLayout.PAGE_AXIS));
						for(int i = 0; i<boxes[0].length; i++){
							yPanel.add(boxes[0][i]);
						}
						xPanel.add(yPanel);
					}else{
						for(int i = 0; i< boxes.length; i++){
							yPanel = new JPanel();
							yPanel.setLayout(new BoxLayout(yPanel, BoxLayout.PAGE_AXIS));
							yPanel.add(new JLabel(ri[i].getDescription()));
							for(int j= 0; j<boxes[i].length; j++){
								yPanel.add(boxes[i][j]);
							}
							xPanel.add(yPanel);
						}
					}
					jPanel.add(xPanel);
					JButton jButton = new JButton("Ok");
					jButton.addActionListener(this);
					jPanel.add(jButton);
					
					return jPanel;
				}

				public void actionPerformed(ActionEvent e) {
					int[] count;
					int tot = 0;
					
					count = new int[boxes.length];
					for(int i = 0; i<boxes.length; i++){
						count[i]=0;
						for(int j = 0; j<boxes[i].length; j++){
							if(boxes[i][j].isSelected()){
								count[i]++;
								tot++;
							}
						}
					}
					
					if(tot<graph.minNodesChosen()){
						mainWindow.errorMessage(msg);
						return;
					}
					if(graph.maxNodesChosen()>0 && tot>graph.maxNodesChosen()){
						mainWindow.errorMessage(msg);
						return;
					}
					dialog.dispose();
					
					String[][] out = new String[boxes.length][];
					int k;
					for(int i = 0; i<boxes.length; i++){
						out[i] = new String[count[i]];
						k=0;
						for(int j = 0; j<boxes[i].length; j++){
							if(boxes[i][j].isSelected()){
								out[i][k] = boxes[i][j].getActionCommand();
								k++;
							}
						}
					}
					graph.setParameters(ri, out);
					new GraphDisplay(graph.graph());
				}
			}

			private class GraphDisplay {

				JFreeChart chart;

				public GraphDisplay(JFreeChart chart) {
					this.chart = chart;
					
					 ChartFrame frame = new ChartFrame("", chart);
					 frame.pack();
					 frame.setVisible(true);
					
				}
			}
		}
	}

}

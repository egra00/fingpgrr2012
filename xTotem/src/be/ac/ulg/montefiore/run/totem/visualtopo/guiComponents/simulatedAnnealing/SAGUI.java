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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.simulatedAnnealing;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;

import org.jfree.data.xy.XYSeriesCollection;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.ArrayList;

import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.*;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.facade.SimulatedAnnealing;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model.ObjectiveFunction;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model.UpperBoundGenerator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.SpringUtilities;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.SwingWorker;

/*
 * Changes:
 * --------
 * - 10-Feb.-2006 : now extends JDialog instead of JFrame (GMO).
 * - 13-Feb.-2006 : use swing default layout instead of IntelliJ one. (GMO)
 */

/**
 * Graphical user interface
 *
 * <p>Creation date: 20-Dec.-2004
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 */
public class SAGUI extends JDialog implements ActionListener {
    private JTextField T0Parameter;
    private JTextField LParameter;
    private JTextField alphaParameter;
    private JTextField epsilonParameter;
    private JTextField KParameter;

    private JButton loadButton;
    private JButton executeSAButton;
    private JButton abortSAButton;
    private JButton displaySolutionsButton;
    private JButton generateParametersButton;
    private JButton exitButton;

    private JComboBox initialSolution;
    private JComboBox Neighborhood;
    private JComboBox scoreFunction;

    private JLabel costFunctionValue;
    private JLabel datasetUsed;
    private JLabel upperBoundValue;
    private JLabel timeValue;

    private Container mainPanel;

    private JPanel graphPanel;
    private String graphTitle;

    private SwingWorker worker;

    private SASolution tempSASolution;

    private static Logger logger = Logger.getLogger(SAGUI.class.getName());

    private ArrayList<SANeighbourhood> neighborhoodOperator;
    private ArrayList<SAInitialSolutionGenerator> solutionGeneratorOperator;
    private ArrayList<ObjectiveFunction> objectiveFunctionList;

    private UpperBoundGenerator upperBoundGenerator;
    private SolutionDisplayer solDisplayer;
    private SAParameter params;
    private JCheckBox fastPlotting;


    public SAGUI(ArrayList<ObjectiveFunction> objectiveFunctionList,
                 ArrayList<SANeighbourhood> neighborhoodList,
                 ArrayList<SAInitialSolutionGenerator> solutionGeneratorList,
                 UpperBoundGenerator upperBoundGenerator,
                 SolutionDisplayer solutionDisplayer,
                 SAParameter params) {

        super(MainWindow.getInstance(), "Fast Simulated Annealing @RUN");

        setupUI();

        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        //setting initial parameters value
        T0Parameter.setText(new Float(params.getT0()).toString());
        LParameter.setText(new Integer(params.getL()).toString());
        alphaParameter.setText(new Float(params.getAlpha()).toString());
        epsilonParameter.setText(new Float(params.getEpsilon2()).toString());
        KParameter.setText(new Integer(params.getK2()).toString());

        this.neighborhoodOperator = neighborhoodList;
        this.solutionGeneratorOperator = solutionGeneratorList;
        this.objectiveFunctionList = objectiveFunctionList;
        this.upperBoundGenerator = upperBoundGenerator;
        this.solDisplayer = solutionDisplayer;
        this.params = params;

        //setting the comboboxes
        for (int i = 0; i < solutionGeneratorOperator.size(); i++) {
            SAInitialSolutionGenerator gen = solutionGeneratorOperator.get(i);
            initialSolution.addItem(gen.toString());
        }

        for (int i = 0; i < neighborhoodOperator.size(); i++) {
            SANeighbourhood nbh = neighborhoodOperator.get(i);
            Neighborhood.addItem(nbh.toString());
        }

        for (int i = 0; i < objectiveFunctionList.size(); i++) {
            ObjectiveFunction fct = objectiveFunctionList.get(i);
            scoreFunction.addItem(fct.getName());
        }

        initialSolution.setSelectedIndex(0);
        scoreFunction.setSelectedIndex(0);

        //adding ActionListener for each button
        loadButton.addActionListener(this);
        executeSAButton.addActionListener(this);
        abortSAButton.addActionListener(this);
        exitButton.addActionListener(this);
        displaySolutionsButton.addActionListener(this);
        generateParametersButton.addActionListener(this);

        //creating a very basic chart with no data
        graphTitle = "Objective " + objectiveFunctionList.get(scoreFunction.getSelectedIndex()).getName();

        ChartPanel chartpanel = createChart(null,graphTitle);

        chartpanel.setDisplayToolTips(false);
        chartpanel.setRefreshBuffer(true);
        graphPanel.add(chartpanel);

        MainWindow.getInstance().showDialog(this);
    }




    /**
     * Method called when an action is performed on one of the buttons for which an ActionListener has been added
     * @param e
     */
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == loadButton){
            String fileLoaded = loadFile();
            //if (fileLoaded != null){
            datasetUsed.setText(fileLoaded);
            executeSAButton.setEnabled(true);
            displaySolutionsButton.setEnabled(false);
            generateParametersButton.setEnabled(true);
            costFunctionValue.setText("0");
            upperBoundValue.setText("None set");
            double upperBound = 0;
            if (upperBoundGenerator != null) {
                upperBound = upperBoundGenerator.compute();
                upperBoundValue.setText((new Double(upperBound)).toString());
            }
            timeValue.setText("0");
            graphPanel.removeAll();


            ChartPanel chartPanel = createChart(null,graphTitle);

            graphPanel.add(chartPanel);
            graphPanel.repaint();
            //}
        }

        if (e.getSource() == executeSAButton){
            abortSAButton.setEnabled(true);
            executeSAButton.setEnabled(false);
            generateParametersButton.setEnabled(false);
            loadButton.setEnabled(false);
            exitButton.setEnabled(false);
            displaySolutionsButton.setEnabled(false);
            executeSA();
        }

        if (e.getSource() == abortSAButton){
            worker.interrupt();
            abortSAButton.setEnabled(false);
            executeSAButton.setEnabled(true);
            generateParametersButton.setEnabled(true);
            displaySolutionsButton.setEnabled(false);
            //loadButton.setEnabled(true);
            exitButton.setEnabled(true);
        }

        if (e.getSource() == exitButton){
            dispose();
            //System.exit(0);
        }

        if (e.getSource() == displaySolutionsButton){
            displaySolutions();
            //printLatexSolutions();
        }

        if (e.getSource() == generateParametersButton){
            abortSAButton.setEnabled(true);
            executeSAButton.setEnabled(false);
            loadButton.setEnabled(false);
            exitButton.setEnabled(false);
            displaySolutionsButton.setEnabled(false);
            generateSAParameters();
        }
    }

    private void displaySolutions(){
        solDisplayer.display(tempSASolution);
    }

    private void executeSA(){

        worker =
                new SwingWorker(){
                    public Object construct(){
                        return ExecuteSAfromUI(Float.parseFloat(T0Parameter.getText()),
                                Integer.parseInt(LParameter.getText()),
                                Float.parseFloat(alphaParameter.getText()),
                                Float.parseFloat(epsilonParameter.getText()),
                                Integer.parseInt(KParameter.getText()),
                                initialSolution.getSelectedIndex(),
                                Neighborhood.getSelectedIndex(),
                                scoreFunction.getSelectedIndex());
                    }
                    public void finished(){
                        //loadButton.setEnabled(true);
                        exitButton.setEnabled(true);
                        abortSAButton.setEnabled(false);
                        executeSAButton.setEnabled(true);
                        generateParametersButton.setEnabled(true);

                        ComplexObject complexObject = (ComplexObject)get();

                        if (complexObject != null) {
                            displaySolutionsButton.setEnabled(true);

                            XYSeriesCollection data = complexObject.getReport().GetGraphData();

                            tempSASolution = complexObject.getSolution();
                            long time = complexObject.getTime();
                            timeValue.setText((new Long(time)).toString());

                            try{
                                double finalCost = tempSASolution.evaluate();
                                costFunctionValue.setText(new Float(finalCost).toString());

                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }

                            graphPanel.removeAll();
                            graphTitle = "Objective " + objectiveFunctionList.get(scoreFunction.getSelectedIndex()).getName();
                            ChartPanel chartPanel = createChart(data,graphTitle);


                            graphPanel.add(chartPanel);
                            graphPanel.repaint();
                            //pack();
                            //repaint();

                        }
                    }

                };
        worker.start();

    }

    private ChartPanel createChart(XYSeriesCollection data, String title){
        JFreeChart chart = ChartFactory.createXYLineChart(title,
                "Iterations",           // X-Axis label    (domain)
                "Objective function value",           // Y-Axis label    (range)
                data,          // Dataset
                PlotOrientation.VERTICAL,
                true,                // Show legend
                true,
                false);
        //System.out.println("Valeur de counter: " + counter);

        XYPlot plot = chart.getXYPlot();

        NumberAxis rangeAxis = (NumberAxis)plot.getRangeAxis();
        rangeAxis.setAutoRangeIncludesZero(false);

        chart.setAntiAlias(false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseZoomable(true);
        chartPanel.setDisplayToolTips(false);
        chartPanel.setRefreshBuffer(true);

        return chartPanel;



    }
    private void generateSAParameters(){

        worker =
                new SwingWorker(){
                    public Object construct(){
                        return generateParameters(Float.parseFloat(T0Parameter.getText()),Integer.parseInt(LParameter.getText()), Float.parseFloat(alphaParameter.getText()), Float.parseFloat(epsilonParameter.getText()), Integer.parseInt(KParameter.getText()), initialSolution.getSelectedIndex(), Neighborhood.getSelectedIndex(),scoreFunction.getSelectedIndex());
                    }
                    public void finished(){

                        abortSAButton.setEnabled(false);
                        executeSAButton.setEnabled(true);
                        displaySolutionsButton.setEnabled(false);
                        generateParametersButton.setEnabled(true);
                        //loadButton.setEnabled(true);
                        exitButton.setEnabled(true);

                        SAParameter parameters = (SAParameter)get();

                        T0Parameter.setText(new Float(parameters.getT0()).toString());
                        LParameter.setText(new Integer(parameters.getL()).toString());
                        alphaParameter.setText(new Float(parameters.getAlpha()).toString());
                        epsilonParameter.setText(new Float(parameters.getEpsilon2()).toString());
                        KParameter.setText(new Integer(parameters.getK2()).toString());

                    }

                };
        worker.start();

    }

    private String loadFile(){
        /*
        JFileChooser chooser = new JFileChooser();
        File dir = new File("data/");
        chooser.setCurrentDirectory(dir);
        TXTFileFilter fileFilter = new TXTFileFilter("txt", "Text Data Files");

        chooser.addChoosableFileFilter(fileFilter);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.CANCEL_OPTION) return null;
        try{

        File file = chooser.getSelectedFile();
        DataFileManager.getInstance().loadDataFile(file.getAbsolutePath());
        logger.info("Load file " + file.getAbsolutePath() + " with " +
        DataFileManager.getInstance().getDataFile().getNbSegments() + " segments and " +
        DataFileManager.getInstance().getDataFile().getNbAttributes() + " attributes");

        return file.getName();
        }
        catch (Exception e){

        JOptionPane.showMessageDialog(null,"Error when loading this file", "ERROR!", JOptionPane.WARNING_MESSAGE);

        }
        */

        return null;
    }



    SAParameter generateParameters(float T0, int L, float alpha, float epsilon2, int K, int initialSolutionIndex, int neighborhoodIndex,int objectiveFunctionIndex){
        SAParameterGenerator parameterGenerator = new SAParameterGenerator();
        SAInitialSolutionGenerator solGenerator = solutionGeneratorOperator.get(initialSolutionIndex);
        SANeighbourhood neighbourhood = neighborhoodOperator.get(neighborhoodIndex);
        ObjectiveFunction fct = objectiveFunctionList.get(objectiveFunctionIndex);
        boolean minimise = params.getMinimise();

        try{
            SAParameter SAparam = new SAParameter(T0, L, alpha, epsilon2, K, minimise);
            BasicSAReportGenerator reportGenerator = new BasicSAReportGenerator();
            SimulatedAnnealing SAalgorithm = new SimulatedAnnealing(neighbourhood, solGenerator, fct, SAparam, reportGenerator);

            SAParameter parameters = parameterGenerator.generateParameter(SAalgorithm);
            return parameters;
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }



    ComplexObject ExecuteSAfromUI(float T0, int L, float alpha, float epsilon2, int K, int initialSolutionIndex, int neighborhoodIndex, int objectiveFunctionIndex){
        try{

            SAInitialSolutionGenerator solGenerator = solutionGeneratorOperator.get(initialSolutionIndex);
            SANeighbourhood neighbourhood = neighborhoodOperator.get(neighborhoodIndex);
            ObjectiveFunction fct = objectiveFunctionList.get(objectiveFunctionIndex);

            boolean minimise = params.getMinimise();
            SAParameter SAparam = new SAParameter(T0, L, alpha, epsilon2, K, minimise);

            GraphSAReportGenerator reportGenerator = null;
            // if fastplot is set
            if (fastPlotting.isSelected()){
                reportGenerator = new GraphSAReportGenerator(10);
            }
            else {
                reportGenerator = new GraphSAReportGenerator();
            }


            long time = System.currentTimeMillis();

            SimulatedAnnealing SAalgorithm = new SimulatedAnnealing(neighbourhood, solGenerator, fct, SAparam, reportGenerator);
            SASolution solution = SAalgorithm.solve();
            time = System.currentTimeMillis() - time;
            System.out.println("Best Solution : " + solution.evaluate() + " fct " + solution.getObjectiveFunction().getName());
            if (solution == null){
                logger.warn("ABORTED BY USER");
            } else{
                logger.info("Utility of the best solution found is : " + solution.evaluate() + "(execution time = " + time + " ms)");
                return new ComplexObject(reportGenerator,solution,time);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void setupUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setEnabled(false);
        final JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        executeSAButton = new JButton();
        executeSAButton.setEnabled(true);
        executeSAButton.setText("Execute SA");
        buttonPanel.add(executeSAButton);

        displaySolutionsButton = new JButton();
        displaySolutionsButton.setEnabled(false);
        displaySolutionsButton.setText("Display solutions");
        buttonPanel.add(displaySolutionsButton);

        loadButton = new JButton();
        loadButton.setEnabled(false);
        loadButton.setText("Load");
        //buttonPanel.add(loadButton);

        abortSAButton = new JButton();
        abortSAButton.setEnabled(false);
        abortSAButton.setText("Abort");
        buttonPanel.add(abortSAButton);

        exitButton = new JButton();
        exitButton.setEnabled(true);
        exitButton.setText("Exit");
        buttonPanel.add(exitButton);

        final JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
        mainPanel.add(leftPanel, BorderLayout.WEST);
        final JPanel panel3 = new JPanel(new SpringLayout());
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(panel3);

        final JLabel label6 = new JLabel();
        label6.setText("Parameter");
        panel3.add(label6);
        final JLabel label7 = new JLabel();
        label7.setText("Value");
        panel3.add(label7);
        final JLabel label8 = new JLabel();
        label8.setText("Range");
        panel3.add(label8);


        final JLabel label1 = new JLabel();
        label1.setEnabled(true);
        label1.setText("T0");
        label1.setToolTipText("Initial Temperature");
        panel3.add(label1);
        T0Parameter = new JTextField();
        T0Parameter.setText("");
        panel3.add(T0Parameter);
        final JLabel label9 = new JLabel();
        label9.setText("]0,inf[");
        panel3.add(label9);

        final JLabel label5 = new JLabel();
        label5.setText("L");
        label5.setToolTipText("Size of the plateau");
        panel3.add(label5);
        LParameter = new JTextField();
        LParameter.setText("");
        panel3.add(LParameter);
        final JLabel label10 = new JLabel();
        label10.setText("[1,inf[");
        panel3.add(label10);

        final JLabel label2 = new JLabel();
        label2.setText("alpha ");
        label2.setToolTipText("Cooling Factor");
        panel3.add(label2);
        alphaParameter = new JTextField();
        panel3.add(alphaParameter);
        final JLabel label11 = new JLabel();
        label11.setText("]0,1[");
        panel3.add(label11);

        final JLabel label3 = new JLabel();
        label3.setText("epsilon");
        label3.setToolTipText("Terminaison Value");
        panel3.add(label3);
        epsilonParameter = new JTextField();
        panel3.add(epsilonParameter);
        final JLabel label12 = new JLabel();
        label12.setText("%");
        panel3.add(label12);

        final JLabel label4 = new JLabel();
        label4.setText("K");
        panel3.add(label4);
        KParameter = new JTextField();
        panel3.add(KParameter);
        final JLabel label13 = new JLabel();
        label13.setText("[1,inf[");
        panel3.add(label13);

        SpringUtilities.makeCompactGrid(panel3, 6, 3, 5, 5, 5, 5);

        leftPanel.add(Box.createVerticalGlue());
        generateParametersButton = new JButton();
        generateParametersButton.setEnabled(true);
        generateParametersButton.setText("Generate Parameters");
        generateParametersButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(generateParametersButton);
        leftPanel.add(Box.createVerticalGlue());

        final JPanel panel5 = new JPanel(new SpringLayout());
        leftPanel.add(panel5);

        final JLabel label15 = new JLabel();
        label15.setText("Inital Solution: ");
        panel5.add(label15);
        initialSolution = new JComboBox();
        panel5.add(initialSolution);

        final JLabel label14 = new JLabel();
        label14.setText("Neighbourhood: ");
        panel5.add(label14);
        Neighborhood = new JComboBox();
        panel5.add(Neighborhood);

        final JLabel label21 = new JLabel();
        label21.setText("Score Function: ");
        panel5.add(label21);
        scoreFunction = new JComboBox();
        panel5.add(scoreFunction);

        final JLabel label16 = new JLabel();
        label16.setText("Dataset Used: ");
        panel5.add(label16);
        datasetUsed = new JLabel();
        datasetUsed.setText("None");
        panel5.add(datasetUsed);

        final JLabel label18 = new JLabel();
        label18.setText("Upper bound: ");
        panel5.add(label18);
        upperBoundValue = new JLabel();
        upperBoundValue.setText("None set");
        panel5.add(upperBoundValue);

        JLabel label20 = new JLabel();
        label20.setText("     ");
        panel5.add(label20);
        label20 = new JLabel();
        label20.setText("     ");
        panel5.add(label20);

        final JLabel label17 = new JLabel();
        label17.setText("Best solution:");
        panel5.add(label17);
        costFunctionValue = new JLabel();
        costFunctionValue.setText("0");
        panel5.add(costFunctionValue);

        final JLabel label19 = new JLabel();
        label19.setText("Obtained in [ms]:");
        panel5.add(label19);
        timeValue = new JLabel();
        timeValue.setText("0");
        panel5.add(timeValue);

        final JLabel label22 = new JLabel();
        label22.setText("Fast Plotting: ");
        panel5.add(label22);
        fastPlotting = new JCheckBox();
        fastPlotting.setText("Click here to enable \"Fast Plotting\"");
        panel5.add(fastPlotting);

        SpringUtilities.makeCompactGrid(panel5, 9, 2, 5, 5, 5, 5);

        graphPanel = new JPanel(new BorderLayout());
        mainPanel.add(graphPanel, BorderLayout.CENTER);
    }
}

class ComplexObject {

    private GraphSAReportGenerator reportGenerator;
    private SASolution solution;
    private long time;

    public ComplexObject (GraphSAReportGenerator reportGenerator, SASolution solution, long time){
        this.reportGenerator = reportGenerator;
        this.solution = solution;
        this.time = time;
    }

    public GraphSAReportGenerator getReport(){
        return reportGenerator;
    }

    public SASolution getSolution(){
        return solution;
    }

    public long getTime(){
        return time;
    }
}

package be.ac.ucl.ingi.totem.repository.guiComponents;

import org.jfree.chart.JFreeChart;

import be.ac.ucl.ingi.totem.repository.RttMeasurementReportInterpreter;

public abstract class RttMeasuresGraph {
	public static final String AGGREGATE = "Aggregate";

	public  int minRtt, maxRtt, minNodes, maxNodes;

	boolean mustChooseNode, mustBeSimilar;
	RttMeasurementReportInterpreter[] ri;
	String[][] keys;
	
	/**
	 * 
	 * @return the minimum number of measurement reports this graph needs
	 */
	public int minRttMeasurementReportInterpretors(){
		return minRtt;
	}
	/**
	 * 
	 * @return the maximum number of measurement reports this graph needs. -1 if there is no maximum
	 */
	public int maxRttMeasurementReportInterpretors(){
		return maxRtt;
	}
	/**
	 * 
	 * @return whether one must specify which nodes of the chosen measurement reports have to be graphed
	 */
	public boolean mustChooseNode(){
		return mustChooseNode;
	}
	/**
	 * 
	 * @return the total minimum number of nodes chosen from the measurement reports
	 */
	public int minNodesChosen()	{
		return minNodes;
	}
	/**
	 * 
	 * @return the total maximum number of nodes chosen fro the measurement reports
	 */
	public int maxNodesChosen(){
		return maxNodes;
	}
	/**
	 * 
	 * @return returns whether the nodes chosen the measurement reports must be the same
	 */
	public boolean mustBeSimilarNodes(){
		return mustBeSimilar;
	}
	/**
	 * Sets the measurement reports and chosen keys that are to be graphed
	 * @param ri the measurement reports whose data is to be graphed
	 * @param keys the first dimensionof the array refers to the index of ri. the second dimension is the 
	 * the nodes that are to be graphed. RttMeasurementReportInterpreter.AGGREGATE refers to the aggregate of data
	 * In mustBeSimilarNodes() is true then it is admissable to idicate all the keys in the first array ie keys[0].lenght == 1
	 */
	public void setParameters(RttMeasurementReportInterpreter[] ri, String[][] keys){
		this.ri = ri;
		this.keys = keys;
	}
	/**
	 * 
	 * @return a JFreeChart containing the graph with parameters previously specified with setParameters
	 */
	public abstract JFreeChart graph();
	
}
package uy.edu.fing.repository.tools.iBGPViewer.model;

import edu.uci.ics.jung2.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung2.graph.Graph;


public class LocalLayout extends AbstractLayout<MyNode, MyLink> 
{

	public LocalLayout(Graph<MyNode, MyLink> graph) 
	{
		super(graph);
	}

	public void initialize() 
	{
		
		//System.out.println("//////////////////////////////////SET");
		for(MyNode node : graph.getVertices())
		{
			super.setLocation(node, node.getX(), node.getY());
		}
		
		super.initialized = true;
	}

	public void reset() 
	{
		super.initialized = false;
		//System.out.println("//////////////////////////////////RESET");
	}

}

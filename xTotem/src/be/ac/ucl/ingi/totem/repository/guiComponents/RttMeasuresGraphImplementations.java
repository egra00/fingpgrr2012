package be.ac.ucl.ingi.totem.repository.guiComponents;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import be.ac.ucl.ingi.totem.repository.RttMeasurementReportInterpreter.RttResult;

public class RttMeasuresGraphImplementations {

	public class SimpleIncreasingDelayGraph extends RttMeasuresGraph {
		
		public SimpleIncreasingDelayGraph(){
			maxNodes = -1;
			minNodes = 1;
			minRtt = 1;
			maxRtt = -1;
			mustBeSimilar = false;
			mustChooseNode = true;
		}


		public JFreeChart graph() {
			JFreeChart jFreeChart;
			XYSeriesCollection dataSet = new XYSeriesCollection();
			XYSeries series;
			List<RttResult> res;
			TreeSet sorted;
			int n;
			RttResult tmpRttResult;
			Iterator<RttResult> rttResultIterator;
			String name;
			
			for(int i = 0; i<ri.length; i++){
				for(int j = 0; j< keys[i].length; j++){
					res = ri[i].getRttResult(keys[i][j]); 
					if(res!=null){
						sorted = new TreeSet<RttResult>(new AvgRttComparator()); 
						sorted.addAll(res);
						n = 0;
						rttResultIterator = sorted.iterator();
						name = keys[i][j];
						series = new XYSeries(ri[i].getDescription()+"/" + name);
						while(rttResultIterator.hasNext()){
							tmpRttResult = rttResultIterator.next();
							series.add(n, tmpRttResult.getAverage());
							n++;							
						}
						dataSet.addSeries(series);
					}
				}
			}
			jFreeChart = ChartFactory.createXYLineChart("Simple increasing delay chart", "subnets", "delay µs", dataSet, PlotOrientation.VERTICAL, true, true, false);
			return jFreeChart;
		}
		
	}
	
	public class NormalizedIncreasingDelayGraph extends RttMeasuresGraph{
		public NormalizedIncreasingDelayGraph(){
			maxNodes = -1;
			minNodes = 1;
			minRtt = 1;
			maxRtt = -1;
			mustBeSimilar = false;
			mustChooseNode = true;
		}
		
		
		public JFreeChart graph() {
			JFreeChart jFreeChart;
			XYSeriesCollection dataSet = new XYSeriesCollection();
			XYSeries series;
			List<RttResult> res;
			TreeSet sorted;
			double n, tot;
			RttResult tmpRttResult;
			Iterator<RttResult> rttResultIterator;
			String name;
			
			for(int i = 0; i<ri.length; i++){
				for(int j = 0; j< keys[i].length; j++){
					res = ri[i].getRttResult(keys[i][j]); 
					if(res!=null){
						sorted = new TreeSet<RttResult>(new AvgRttComparator()); 
						sorted.addAll(res);
						tot = 0;
						rttResultIterator = sorted.iterator();
						while(rttResultIterator.hasNext()){
							tot += rttResultIterator.next().getWeight();
						}
						n = 0;
						rttResultIterator = sorted.iterator();
						name = keys[i][j];
						series = new XYSeries(ri[i].getDescription()+"/" + name);
						while(rttResultIterator.hasNext()){
							tmpRttResult = rttResultIterator.next();
							series.add(n, tmpRttResult.getAverage());
							n+= tmpRttResult.getWeight()/tot;							
						}
						dataSet.addSeries(series);
					}
				}
			}
			jFreeChart = ChartFactory.createXYLineChart("Normallized increasing delay chart", "prefix *  weight / sum(weight)", "delay µs", dataSet, PlotOrientation.VERTICAL, true, true, false);
			return jFreeChart;
		}
		
	}
	
		
	
	private class WeightComparator implements Comparator<RttResult>{
		public int compare(RttResult o1, RttResult o2) {
			return Double.compare(o1.getWeight(), o2.getWeight());
		}
	}
	private class AvgRttComparator implements Comparator<RttResult>{
		public int compare(RttResult o1, RttResult o2) {
			return Double.compare(o1.getAverage(), o2.getAverage());
		}
	}
	
	
}

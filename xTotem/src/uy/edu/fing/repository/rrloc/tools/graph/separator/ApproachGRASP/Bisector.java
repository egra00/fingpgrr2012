package uy.edu.fing.repository.rrloc.tools.graph.separator.ApproachGRASP;

import java.util.HashSet;
import java.util.Set;

public class Bisector 
{
	
	private Set<Integer> S;
	private Set<Integer> C1;
	private Set<Integer> C2;
	
	public Bisector() 
	{
		S = new HashSet<Integer>();
		C1 = new HashSet<Integer>();
		C2 = new HashSet<Integer>();
	}
	
	public boolean isBetter(double alfa, double beta, Bisector b)
	{
		if (b.C1.size()<=0 || b.C2.size()<=0 || b.S.size()<=0) return true;
		if (C1.size()<=0 || C2.size()<=0 || S.size()<=0) return false;
		
		double self_size = S.size();
		double self_average = (C1.size() + C2.size()) / 2;
		double self_balanced = Math.abs(C1.size() - self_average) + Math.abs(C2.size() - self_average);
		
		double b_size = b.S.size();
		double b_average = (b.C1.size() + b.C2.size()) / 2;
		double b_balanced = Math.abs(b.C1.size() - b_average) + Math.abs(b.C2.size() - b_average);
		
		
		if(self_size < b_size)
		{
			if(self_balanced < b_balanced)
			{
				return true;
			}
			else if (self_balanced <= (1+alfa)*b_balanced)
			{
				return true;
			}
		}
		else if(self_size <= (1+beta)*b_size)
		{
			if(self_balanced < b_balanced)
			{
				return true;
			}
		}
		
		return false;
	}

	public Set<Integer> getS() {
		return S;
	}

	public Set<Integer> getC1() {
		return C1;
	}

	public Set<Integer> getC2() {
		return C2;
	}
}

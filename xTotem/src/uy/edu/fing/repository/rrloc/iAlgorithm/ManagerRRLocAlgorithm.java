package uy.edu.fing.repository.rrloc.iAlgorithm;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class ManagerRRLocAlgorithm 
{
	private static ManagerRRLocAlgorithm instance = null;
	private HashMap<Integer, Semaphore> map;
	private Semaphore semaphore;
	
	
	private ManagerRRLocAlgorithm()
	{
		map = new HashMap<Integer, Semaphore>();
		semaphore = new Semaphore(1);
	}
	
	public synchronized static ManagerRRLocAlgorithm getInstance()
	{
		if (instance == null)
			instance = new ManagerRRLocAlgorithm();
		
		return instance;
	}
	
	
	public void lock(int id)
	{
		try 
		{
			semaphore.acquire();
			if (!map.containsKey(id))
			{
				map.put(id, new Semaphore(1));
			}
			semaphore.release();
		} 
		catch (InterruptedException e1) 
		{
			semaphore.release();
			e1.printStackTrace();
		}
		
		try 
		{
			map.get(id).acquire();
		} 
		catch (InterruptedException e) 
		{
			map.get(id).release();
			e.printStackTrace();
		}
	}
	
	
	public void unlock(int id)
	{
		if (map.containsKey(id))
		{
			map.get(id).release();
		}
	}
	
}

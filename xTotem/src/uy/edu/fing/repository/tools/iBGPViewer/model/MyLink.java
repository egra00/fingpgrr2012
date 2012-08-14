package uy.edu.fing.repository.tools.iBGPViewer.model;

import be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter;

public class MyLink 
{
	
	private BgpRouter router;
	private BgpNeighbor neighbor;
	
	public MyLink(BgpRouter r, BgpNeighbor n)
	{
		router = r;
		neighbor = n;
	}

	public BgpRouter getRouter() {
		return router;
	}

	public void setRouter(BgpRouter router) {
		this.router = router;
	}

	public BgpNeighbor getNeighbor() {
		return neighbor;
	}

	public void setNeighbor(BgpNeighbor neighbor) {
		this.neighbor = neighbor;
	}



}

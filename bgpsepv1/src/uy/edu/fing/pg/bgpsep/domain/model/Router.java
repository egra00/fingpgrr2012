package uy.edu.fing.pg.bgpsep.domain.model;

public class Router {
	
	private int idRouter;
	private RouterType tag;
	private RouterConf conf;
	
	public Router(int idRouter, RouterType tag, RouterConf conf) {
		this.idRouter = idRouter;
		this.tag = tag;
		this.conf = conf;
	}

	public RouterConf getConf() {
		return conf;
	}

	public void setConf(RouterConf conf) {
		this.conf = conf;
	}

	public int getIdRouter() {
		return idRouter;
	}
	
	public void setIdRouter(int idRouter) {
		this.idRouter = idRouter;
	}
	
	public RouterType getTag() {
		return tag;
	}
	
	public void setTag(RouterType tag) {
		this.tag = tag;
	}

}

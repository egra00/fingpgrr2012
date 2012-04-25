package be.ac.ulg.montefiore.run.totem.domain.model.impl;

import be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor;

public class BgpNeighborImpl extends be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.BgpNeighborImpl implements BgpNeighbor
{

    /**
     * Returns the neighbor's IP address.
     *
     * @return IP address of the neighbor.
     */
    public String getAddress()
    {
	return getIp();
    }

    /**
     * Returns the neighbor's AS number.
     *
     * @return AS number of the neighbor.
     */
    public int getASID()
    {
	return getAs();
    }

    /**
     * Returns true if the neighbor is a route-reflector client.
     *
     * @return true iff the neighbor is a route-reflector client.
     */
    public boolean isReflectorClient()
    {
	return isReflectorClient();
    }

    /**
     * Returns true is the neighbor needs next-hop-self.
     *
     * @return true iff the neighbor needs next-hop-self.
     */
    public boolean hasNextHopSelf()
    {
	return isNextHopSelf();
    }

}


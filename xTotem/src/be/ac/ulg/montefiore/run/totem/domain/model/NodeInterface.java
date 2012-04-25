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
package be.ac.ulg.montefiore.run.totem.domain.model;

import be.ac.ulg.montefiore.run.totem.domain.exception.IPAddressFormatException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NotInitialisedException;
import be.ac.ulg.montefiore.run.totem.domain.exception.StatusTypeException;

/*
 * Changes:
 * --------
 *
 * - 08-Feb-2006: rename getInterfaceStatus into getNodeInterfaceStatus (JLE).
 * - 08-Feb-2006: rename setInterfaceStatus into setNodeInterfaceStatus (JLE).
 * - 08-Feb-2006: add TODO about observer events (JLE).
 * - 08-Feb-2006: add equals(Object) and hashCode() methods (JLE).
 * - 27-Oct-2006: add setIP(String) and setIPMask(String, int) (JLE).
 */

/**
 * Represents an interface of a node.
 *
 * <p>Creation date: 07-f�vr.-2006
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public interface NodeInterface extends DomainElement {

    public final int STATUS_UP = 0;
    public final int STATUS_DOWN = 1;
    
    public int getNodeInterfaceStatus();
    
    //TODO: we should generate observer events for this method!
    public void setNodeInterfaceStatus(int status) throws StatusTypeException;
    
    public String getIP() throws NotInitialisedException;
    public String getIPMask() throws NotInitialisedException;
    
    public void setIP(String address) throws IPAddressFormatException;
    public void setIPMask(String address, int maskLength) throws IPAddressFormatException;
    
    public boolean equals(Object o);
    public int hashCode();
}

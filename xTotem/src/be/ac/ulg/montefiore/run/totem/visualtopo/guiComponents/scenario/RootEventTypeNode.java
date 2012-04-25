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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.scenario;

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.EventType;

/*
* Changes:
* --------
* 24-Apr.-2006 : add status constants and state (GMO).
*
*/

/**
* Node identified by a Event. It is the first level in the tree representing a scenario.
*
* <p>Creation date: 6 janv. 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class RootEventTypeNode extends EventTypeNode {
    public static final int NOT_EXECUTED = 0;
    public static final int EXECUTED_NO_ERROR = 1;
    public static final int EXECUTED_ERROR = 2;

    private EventType value = null;
    private int status = NOT_EXECUTED;

    public RootEventTypeNode(EventType value) {
        super(null, null, value.getClass());
        this.value = value;
    }

    public RootEventTypeNode(EventType value, boolean useNotSet) {
        super(null, null, value.getClass(), useNotSet);
        this.value = value;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        if (status >= 0 && status < 3)
           this.status = status;
        else System.out.println("Status error in RootEventTypeNode");
    }

    public boolean isset() {
        return true;
    }

    public Object getValue() {
        return value;
    }

    public String displayString() {
        return getValue().getClass().getSimpleName();
    }
}

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
package be.ac.ulg.montefiore.run.totem.scenario.facade;

/*
* Changes:
* --------
*
*/

/**
* This class is used by scenario events to identify the context on which they are executed
* (from which path relative paths should be interpreted). This is used because scenario events do not have a reference
* to the scenario to which they belong.
* <p/>
* The context is set by the scenario manager and suppose that only one scenario is executed at a time.   
*
* <p>Creation date: 8/01/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ScenarioExecutionContext {
    private static String context = "";

    public static String getContext() {
        return context;
    }

    public static void setContext(String context) {
        ScenarioExecutionContext.context = context;
    }

    public static void unsetContext() {
        context = "";
    }
}

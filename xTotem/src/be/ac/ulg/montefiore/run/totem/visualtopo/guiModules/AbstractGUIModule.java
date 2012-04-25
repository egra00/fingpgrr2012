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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules;



/*
 * Changes:
 * --------
 * 
 *
 */

/**
 * An abstract implementation of the GUIModule interface. Extend it to create
 * your own GUIModule.
 * <p/>
 * <p>Creation date: 21-Mar-2005
 *
 * @author Olivier Materne (O.Materne@student.ulg.ac.be)
 */
public abstract class AbstractGUIModule implements GUIModule {


    /**
     * Should the Module be loaded at GUI startup ?
     *
     * @return false
     */
    public boolean loadAtStartup() {
        return false;
    }


    /**
     * Do nothing. Override this method to execute any action that should be done when the module is loading
     */
    public void initialize() {
    }


    /**
     * Do nothing. Override this method to execute any action that should be done when the module is unloading
     */
    public void terminate() {
    }


    /**
     * is the module UnLoadable ??
     *
     * @return false
     */
    public boolean isUnloadable() {
        return false;
    }

}

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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.report;

import javax.swing.*;

/*
* Changes:
* --------
* - 31-May-2007 : Add getShortName() method (GMO)
*/

/**
 * A WI report is used to display the result of the execution of a What-if scenario. the method
 * {@link #computeInitialData()} will be called before the execution of the scenario and {@link #computeFinalData()}
 * will be called after the What-If execution. The panel returned by the {@link #getPanel()} should emphasize the
 * differences between before and after.
 * <p/>
 * <p>Creation date: 23/04/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public interface WIReport {

    /**
     * Returns a panel displaying the difference between the data collected by {@link #computeInitialData()} and by
     * {@link #computeFinalData()}
     * @return
     */
    public JPanel getPanel();

    /**
     * Save the initial data in its internal state.
     */
    public void computeInitialData();

    /**
     * Save the final data in its internal state.
     */
    public void computeFinalData();

    /**
     * Returns a name corresponding to the type of report and its parameters.
     * @return
     */
    public String getName();

    /**
     * Returns the name of the type of report.
     * @return
     */
    public String getShortName();
}

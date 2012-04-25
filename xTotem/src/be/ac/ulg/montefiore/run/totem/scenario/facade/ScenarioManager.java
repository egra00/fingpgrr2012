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

import java.util.Iterator;

import be.ac.ulg.montefiore.run.totem.scenario.model.Event;
import be.ac.ulg.montefiore.run.totem.scenario.model.EventResult;
import be.ac.ulg.montefiore.run.totem.scenario.model.Scenario;
import be.ac.ulg.montefiore.run.totem.scenario.persistence.ScenarioFactory;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;

/*
 * Changes:
 * --------
 * - 27-Jan-2006: don't stop algorithms after scenario execution (JLE).
 * - 24-Apr.-2006 : new implementation (GMO).
 * - 09-Jan-2007: use ScenarioExecutionContext' context (GMO)
 */

/**
 * ScenarioManager is a singleton that provides a global point of access 
 * to the scenario information. 
 *
 * <p>Creation date: 1-Jan-2004
 * 
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 * @author  Gael Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class ScenarioManager {

    private Iterator iterator;
    private static ScenarioManager manager = null;

    private boolean stopOnError = false;

    public boolean isStopOnError() {
        return stopOnError;
    }

    public void setStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
    }

    private ScenarioManager() {
        iterator = null;
    }

    /**
     * Returns the single instance of the ScenarioManager.
     */
    public static ScenarioManager getInstance() {
        if (manager == null) {
            manager = new ScenarioManager();
        }
        return manager;
    }

    /**
     * Loads the specified scenario object into the manager.
     */
    public void loadScenario(Scenario scenario) {
        iterator = scenario.getEvent().iterator();
        ScenarioExecutionContext.setContext(scenario.getScenarioPath());
    }
    
    /**
     * Loads the specified scenario XML file.
     */
    public void loadScenario(String fileName) {
        Scenario scenario = ScenarioFactory.loadScenario(fileName);
        iterator = scenario.getEvent().iterator();
        loadScenario(scenario);
    }
    
    /**
     * Executes the rest of the scenario.
     */
    public void executeScenario() {
        for (;iterator.hasNext();) {
            Object o = iterator.next();
            Event event = (Event) o;
            EventResult result;
            try {
                result = event.action();
                if (result.getMessage() != null)
                    System.out.println(result.getMessage());
            } catch (EventExecutionException e) {
                if (e.getCause() == null)
                    System.out.println(e.getMessage());
                else
                    e.getCause().printStackTrace();
                if (stopOnError) {
                    System.out.println("Scenario execution stopped.");
                    return;
                }
            }
        }
        //RepositoryManager.getInstance().stopAlgorithms();
    }
    
    /**
     * Executes the next event in the scenario if there is one.
     */
    public void executeNextEvent() {
        if(iterator.hasNext()) {
            Event event = (Event) iterator.next();
            EventResult result;
            try {
                result = event.action();
                System.out.println(result.getMessage());
                System.out.println(result.getObject());
            } catch (EventExecutionException e) {
                e.printStackTrace();
                if (stopOnError) {
                    System.out.println("Scenario execution stopped.");
                    return;
                }
            }
            
            //if(!iterator.hasNext()) {
                //RepositoryManager.getInstance().stopAlgorithms();
            //}
        }
    }
    
    /**
     * Executes the <code>n</code> next events in the scenario. If there are less than <code>n</code>
     * pending events, this method is equivalent to <code>executeScenario</code>. 
     * @param n The number of events to execute.
     */
    public void executeNextEvents(int n) {
        for(;(iterator.hasNext() && (n > 0)); --n) {
            Event event = (Event) iterator.next();
            EventResult result;
            try {
                result = event.action();
                System.out.println(result.getMessage());
                System.out.println(result.getObject());
            } catch (EventExecutionException e) {
                e.printStackTrace();
                if (stopOnError) {
                    System.out.println("Scenario execution stopped.");
                    return;
                }
            }
        }
        //if(!iterator.hasNext()) {
            //RepositoryManager.getInstance().stopAlgorithms();
        //}
    }
    
    /**
     * Returns <code>true</code> if there are pending events and <code>false</code> otherwise.
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }
}

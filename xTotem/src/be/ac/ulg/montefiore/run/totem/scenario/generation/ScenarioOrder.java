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
package be.ac.ulg.montefiore.run.totem.scenario.generation;

import be.ac.ulg.montefiore.run.totem.scenario.model.LSPCreation;


import java.util.*;

/*
* Changes:
* --------
*
*/

/**
 * This class is used to order scenario lsps request
 *
 * <p>Creation date: 15-Dec.-2004
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 */
public class ScenarioOrder {

    public void Order(int order, List events, List lspsList, String methodName, HashMap algoParams){

        int lspID = 0;

        Compara comparator = new Compara();


        if (order==GenerateScenario.INCREASING_ORDER||order==GenerateScenario.DECREASING_ORDER){
            Collections.sort(lspsList, comparator);
        }else if (order==GenerateScenario.RANDOM_ORDER){
            Collections.shuffle(lspsList);
        }

        if (order!=GenerateScenario.DECREASING_ORDER){
            for (Iterator it1 = lspsList.iterator(); it1.hasNext();){
                LspScenario scenarioElem = (LspScenario) it1.next();
                LSPCreation lspCreation = null;
                if (methodName.equals("DAMOTE")){
                    lspCreation = new LSPCreation(scenarioElem.getSrc(), scenarioElem.getDst(), Integer.toString(lspID++), (float)scenarioElem.getBandwidth(), 0, 0, 0, 0, 0, methodName, algoParams);
                    events.add(lspCreation);

                }
                else {
                    lspCreation = new LSPCreation(scenarioElem.getSrc(), scenarioElem.getDst(), Integer.toString(lspID++), (float)scenarioElem.getBandwidth(), methodName, algoParams);
                    events.add(lspCreation);
                }
            }
        }

        if (order==GenerateScenario.DECREASING_ORDER){
            for (int i=lspsList.size()-1; i >= 0; i--){
                LspScenario scenarioElem = (LspScenario) lspsList.get(i);

                LSPCreation lspCreation = null;
                if (methodName.equals("DAMOTE")){
                    lspCreation = new LSPCreation(scenarioElem.getSrc(), scenarioElem.getDst(), Integer.toString(lspID++), (float)scenarioElem.getBandwidth(), 0, 0, 0, 0, 0, methodName, algoParams);
                    events.add(lspCreation);

                }
                else {
                    lspCreation = new LSPCreation(scenarioElem.getSrc(), scenarioElem.getDst(), Integer.toString(lspID++), (float)scenarioElem.getBandwidth(), methodName, algoParams);
                    events.add(lspCreation);
                }
            }
        }
    }






    class Compara implements Comparator{

        public int compare(Object d1, Object d2){
            LspScenario elem1 = (LspScenario) d1;
            Float elem1f = new Float(elem1.getBandwidth());
            LspScenario elem2 = (LspScenario) d2;
            Float elem2f = new Float(elem2.getBandwidth());
            return elem1f.compareTo(elem2f);

        }


    }
}

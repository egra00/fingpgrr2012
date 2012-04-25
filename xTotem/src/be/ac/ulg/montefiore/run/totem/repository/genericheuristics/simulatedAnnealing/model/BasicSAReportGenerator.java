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

package be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Vector;
import java.util.ArrayList;

/*
 * Changes:
 * --------
 *
 */

/**
 * Basic reporter that can save the SAsolution in a text file
 *
 * <p>Creation date: 19 nov. 2004 10:29:33
 *
 * @author  Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class BasicSAReportGenerator implements SAReportGenerator {
    int iterationNumber;
    int step;
    ArrayList dataBase;

    public BasicSAReportGenerator() {
        dataBase = new ArrayList(100);
        iterationNumber = 0;
        this.step = 1;
    }

    public BasicSAReportGenerator(int step) {
        dataBase = new ArrayList(100);
        iterationNumber = 0;
        this.step = step;
    }

    public void clean() {
        dataBase.clear();
        iterationNumber = 0;
    }

    public void addSolution(double presentSolutionCost, double bestSolutionCost, float Temperature) {
        if (((iterationNumber % step) == 0) | (iterationNumber <= 10)) {
            Entry entry = new Entry(iterationNumber, presentSolutionCost,bestSolutionCost, Temperature);
            dataBase.add(entry);
        }
        iterationNumber++;
    }

    public void save(String FileName) throws IOException {
        FileWriter fw;
        BufferedWriter br;
        fw = new FileWriter(FileName);
        br = new BufferedWriter(fw);

        br.write("#iteration\tpresentSolution\tbestSolution\ttemperature\n");
        for (int i = 0; i < dataBase.size(); i++) {
            Entry entry = (Entry) dataBase.get(i);
            br.write(entry.getIterationNumber() + "\t" + entry.getPresentSolutionCost() + "\t" + entry.getBestSolutionCost() + "\t" + entry.getTemperature() + "\n");
        }

        br.close();
        fw.close();
    }

    private class Entry {
        int iterationNumber;
        double presentSolutionCost;
        double bestSolutionCost;
        float temperature;

        public Entry(int iterationNumber, double presentSolutionCost, double bestSolutionCost, float temperature) {
            this.iterationNumber = iterationNumber;
            this.presentSolutionCost = presentSolutionCost;
            this.bestSolutionCost = bestSolutionCost;
            this.temperature = temperature;
        }

        public int getIterationNumber() {
            return iterationNumber;
        }

        public double getPresentSolutionCost() {
            return presentSolutionCost;
        }

        public double getBestSolutionCost() {
            return bestSolutionCost;
        }

        public float getTemperature() {
            return temperature;
        }
    }
}

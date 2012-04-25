package be.ac.ulg.montefiore.run.totem.chart.model;

import be.ac.ulg.montefiore.run.totem.chart.model.exception.InvalidChartDataException;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;

import java.util.ArrayList;
import java.util.HashMap;

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

/**
*  Class to maintain the collected data to use in charts. It represent a 2-dimensional array of double.
*  Each row and column has an unique string identifier (because it may serve as a key in the JFreeChart dataset).
*
*  @see Chart
*
*
* <p>Creation date: 16 d�c. 2005
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class ChartData {

    private ArrayList<double[]> data = null;
    private ArrayList<String> rowTitles = null;

    private String[] columnTitles = null;

    private HashMap<String, Integer> rowTitlesSearch = null;
    private HashMap<String, Integer> columnTitlesSearch = null;

    public ChartData() {
        data = new ArrayList<double[]>();
        rowTitles = new ArrayList<String>();
        rowTitlesSearch = new HashMap<String, Integer>();
        columnTitlesSearch = new HashMap<String, Integer>();
    }

    /**
     * Add a row to the data.
     * @param name Unique name of the new row
     * @param row  array containing the data
     * @throws InvalidChartDataException if a row with the same name already exists.
     */
    public void addRow(String name, float[] row) throws InvalidChartDataException {
        double[] doubleRow = new double[row.length];
        for (int i = 0; i < row.length; i++) {
            doubleRow[i] = (double)row[i];
        }
        addRow(name, doubleRow);
    }

    /**
     * Add a row to the data.
     * @param name Unique name of the new row
     * @param row  array containing the data
     * @throws InvalidChartDataException if a row with the same name already exists.
     */
    public void addRow(String name, double[] row) throws InvalidChartDataException {
        if (getColumnCount() != -1 && row.length != getColumnCount())
            throw new InvalidChartDataException();

        if (rowTitlesSearch.containsKey(name)) {
            throw new InvalidChartDataException("Row with same name already exists");
        }

        data.add(row);

        rowTitlesSearch.put(name, data.size()-1);
        rowTitles.add(name);

    }

    /**
     * @return the number of rows.
     */
    public int getRowCount() {
        return data.size();
    }

    /**
     * @return the number of columns or -1 if it is not yet defined.
     * The number of column is not defined if there are no data and no column titles.
     */
    public int getColumnCount() {
        if (columnTitles == null) {
            return data.size() == 0 ? -1 : data.get(0).length;
        }
        else return columnTitles.length;
    }

    /**
     * Return a specific row given its name
     * @param name name of the row to be returned
     * @return double array representing the row data
     */
    public double[] getRow(String name) {
        Integer index = rowTitlesSearch.get(name);
        if (index == null) return null;

        //if (data.get(index) == null)
        //    System.err.println("Programmation bug");

        return data.get(index);
    }

    /**
     * Return a specific row given its index
     * @param index
     * @return double array representing the row data
     */
    public double[] getRow(int index) {
       return data.get(index);
    }

    /**
     * get the index of a specific row given its name
     * @param name
     * @return
     */
    public int getRowIndex(String name) {
        Integer index = rowTitlesSearch.get(name);
        return index == null ? -1 : index.intValue();
    }

    /**
     * remove a row from the data given its name
     * @param name
     */
    public void removeRow(String name) {
        Integer index = rowTitlesSearch.get(name);
        data.remove(index);
        rowTitles.remove(index);
        rowTitlesSearch.remove(name);
    }

    /**
     * remove a row from the data given its index
     * @param index
     */
    public void removeRow(int index) {
        data.remove(index);
        rowTitlesSearch.remove(rowTitles.remove(index));
    }

    /**
     * get the unique name of a row given its position (index)
     * @param index
     * @return
     */
    public String getRowTitle(int index) {
        if (index < 0 || index >= rowTitles.size())
            return null;
        return rowTitles.get(index);
    }

    /**
     * get the names of the columns
     * @return
     */
    public String[] getColumnTitles() {
        return columnTitles;
    }

    /**
     * Set the name of the columns. The column names should be unique (not checked here).
     * Also define the number of elements of each row to be added.
     * @param columnTitles
     * @throws InvalidChartDataException If there are already some data and the number of columnTitles does not match
     * the number of elements of the already defined rows.
     */
    public void setColumnTitles(String[] columnTitles) throws InvalidChartDataException {

        if (getRowCount() > 0 && getColumnCount() != columnTitles.length) {
            throw new InvalidChartDataException("Titles size does not match data size.");
        }

        this.columnTitles = columnTitles;
        columnTitlesSearch = new HashMap<String, Integer>();
        for (int i = 0; i < columnTitles.length; i++) {
            String title = columnTitles[i];
            columnTitlesSearch.put(title, new Integer(i));
        }
    }

    /**
     * for debug purposes
     */
    public void dump() {
        /*
        for (String s : columnTitles) {
            System.out.print(s + " ");
        }
        System.out.println();
        */

        for (double[] a: data) {
            for (double d : a) {
                System.out.print(d + " ");
            }
            System.out.println();
        }
    }

    /**
     * @return an array of size <code>getRowCount()</code> containing the max value of each row
     */
    public double[] getMaximum() {
        double[] result = new double[getRowCount()];
        for (int i = 0; i < getRowCount(); i++) {
            result[i] = DoubleArrayAnalyse.getMaximum(getRow(i));
        }
        return result;
    }

    /**
     * @return an array of size <code>getRowCount()</code> containing the min value of each row
     */
    public double[] getMinimum() {
        double[] result = new double[getRowCount()];
        for (int i = 0; i < getRowCount(); i++) {
            result[i] = DoubleArrayAnalyse.getMinimum(getRow(i));
        }
        return result;
    }

    /**
     * @return an array of size <code>getRowCount()</code> containing the mean value of each row
     */
    public double[] getMean() {
        double[] result = new double[getRowCount()];
        for (int i = 0; i < getRowCount(); i++) {
            result[i] = DoubleArrayAnalyse.getMeanValue(getRow(i));
        }
        return result;
    }

    /**
     * @return an array of size <code>getRowCount()</code> containing the standard deviation value of each row
     */
    public double[] getStandardDeviation() {
        double[] result = new double[getRowCount()];
        for (int i = 0; i < getRowCount(); i++) {
            result[i] = DoubleArrayAnalyse.getStandardDeviation(getRow(i));
        }
        return result;
    }

    /**
     * @return an array of size <code>getRowCount()</code> containing the specified percentile value of each row
     */
    public double[] getPercentile(int percentile) {
        double[] result = new double[getRowCount()];
        for (int i = 0; i < getRowCount(); i++) {
            result[i] = DoubleArrayAnalyse.getPercentile(getRow(i), percentile);
        }
        return result;
    }

    /**
     * @return an array of size <code>getRowCount()</code> containing the percentile 10 value of each row
     */
    public double[] getPercentile10() {
        return getPercentile(90);
    }
}

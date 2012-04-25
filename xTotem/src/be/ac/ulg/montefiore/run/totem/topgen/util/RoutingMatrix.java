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

package be.ac.ulg.montefiore.run.totem.topgen.util;

import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 *
 */

/**
 * This is the base class for the routing matrix classes.
 *
 * <p>The matrix is a float matrix, so you can route packets from A to B with
 * two different paths. As a routing matrix is generally a large sparse matrix,
 * we represent it by means of a doubly linked structure.
 *
 * <p>The rows of the matrix designate the links of the topology and the
 * columns designate the pairs of nodes of the topology.
 *
 * <p>Creation date: 2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public abstract class RoutingMatrix {
    
    private static final Logger logger = Logger.getLogger(RoutingMatrix.class);
    
    // See http://www.montefiore.ulg.ac.be/services/sbanh/si5.pdf pp. 19 for
    // more details on how this sparse matrix is represented.
    
    /**
     * These are pointers to the linked lists for the rows.
     */
    private MatrixElem[] rows;
    
    /**
     * These are pointers to the linked lists for the columns.
     */
    private MatrixElem[] cols;
    
    /**
     * The number of nodes in the topology when setSize was called.
     */
    private int nbNodes;
    
    /**
     * Returns the column corresponding to the pair (<code>src</code>,<code>dst</code>).
     * @throws IllegalArgumentException If <code>src</code> is equal to <code>dst</code>.
     */
    public final int getKey(int src, int dst) {
        if(src == dst)
            throw new IllegalArgumentException("src is equal to dst");
        if(src > dst)
            return (nbNodes-1) * src + dst;
        else // src < dst
            return (nbNodes-1) * src + dst - 1;
    }
    
    /**
     * Returns the ids of the pair of nodes corresponding to the column
     * <code>column</code>. The first <code>int</code> is the source node id
     * and the second <code>int</code> is the destination node id.
     * @throws IllegalArgumentException If <code>column</code> has a bad value.
     */
    public final IntPair getIds(int column) {
        if(column < 0 || column >= cols.length)
            throw new IllegalArgumentException("Bad value for column = "
                    +column);
        int src = column / (nbNodes - 1);
        int dst = column % (nbNodes - 1);
        if(src <= dst)
            ++dst;
        return new IntPair(src, dst);
    }
    
    /**
     * Recomputes the routing matrix.
     */
    public abstract void recompute() throws RoutingException, NoRouteToHostException;
    
    /**
     * Returns the value for the pair (<code>src</code>,<code>dst</code>) and
     * the link <code>link</code>.
     * @throws IllegalArgumentException If <code>link</code>, <code>src</code> and/or <code>dst</code> have a bad value.
     */
    public final float getElement(int link, int src, int dst) {
        if((link < 0) || (link >= rows.length) ||
                (src < 0) || (src >= nbNodes) || (dst < 0) || (dst >= nbNodes))
            throw new IllegalArgumentException("Bad value for link, src and/or"
                    +" dst: link = "+link
                    +" src = "+src+" dst = "+dst);
        
        int column = getKey(src, dst);
        
        // Generally, a routing matrix has more columns than rows, so it is
        // more efficient to look for the element using its column.
        
        MatrixElem elem = cols[column];
        
        while((elem != null) && (elem.getRow() < link))
            elem = elem.getNextRow();
        
        return ((elem == null) || (elem.getRow() > link))
        ? 0 : elem.getValue();
    }
    
    /**
     * Returns the <strong>non-null</strong> elements of the row corresponding
     * to <code>link</code>. If there is no non-null element in the row
     * <code>link</code>, it returns <code>null</code>. The <code>int</code>
     * values of the <code>IntFloatPair</code> objects designate the indexes
     * of the non-null elements and the <code>float</code> values of the
     * <code>IntFloatPair</code> objects designate the values of the elements.
     * @throws IllegalArgumentException If <code>link</code> has a bad value.
     */
    public final IntFloatPair[] getRow(int link) {
        if(link < 0 || link >= rows.length)
            throw new IllegalArgumentException("Bad value for link: "+link);
        
        MatrixElem elem = rows[link];
        if(elem == null) {
            return null;
        }
        
        ArrayList<IntFloatPair> tab = new ArrayList<IntFloatPair>(cols.length);
        do {
            IntFloatPair pair = new IntFloatPair(elem.getColumn(),
                    elem.getValue());
            tab.add(pair);
            elem = elem.getNextColumn();
        } while(elem != null);
        
        IntFloatPair[] ret = new IntFloatPair[tab.size()];
        
        return tab.toArray(ret);
    }
    
    /**
     * Returns the <strong>non-null</strong> elements of the column
     * corresponding to (<code>src</code>,<code>dst</code>). If there is no
     * non-null element, it returns <code>null</code>. The <code>int</code>
     * values of the <code>IntFloatPair</code> objects designate the indexes
     * of the non-null elements and the <code>float</code> values of the
     * <code>IntFloatPair</code> objects designate the values of the elements.
     * @throws IllegalArgumentException If <code>src</code> and/or <code>dst</code> have a bad value.
     */
    public final IntFloatPair[] getColumn(int src, int dst) {
        if(src < 0 || dst < 0 || src >= nbNodes || dst >= nbNodes)
            throw new IllegalArgumentException("Bad value for src and/or dst: "
                    +"src = "+src+" dst = "+dst);
        
        int column = getKey(src, dst);
        
        MatrixElem elem = cols[column];
        if(elem == null) {
            return null;
        }
        
        ArrayList<IntFloatPair> tab = new ArrayList<IntFloatPair>(rows.length);
        do {
            IntFloatPair pair = new IntFloatPair(elem.getRow(),
                    elem.getValue());
            tab.add(pair);
            elem = elem.getNextRow();
        } while(elem != null);
        
        IntFloatPair[] ret = new IntFloatPair[tab.size()];
        return tab.toArray(ret);
    }
    
    /**
     * Returns the number of rows of this routing matrix.
     */
    public final int getNbRows() {
        return rows.length;
    }
    
    /**
     * Returns the number of columns of this routing matrix.
     */
    public final int getNbColumns() {
        return cols.length;
    }
    
    /**
     * Like <code>set</code> but adds <code>value</code> to the current
     * contained value.
     */
    protected final void add(float value, int link, int src, int dst) {
        if((link < 0) || (link >= rows.length) ||
                (src < 0) || (dst < 0) || (src >= nbNodes) || (dst >= nbNodes))
            throw new IllegalArgumentException("Bad value for link, src and/or"
                    +" dst: link = "+link
                    +" src = "+src+" dst = "+dst);
        
        int column = getKey(src, dst);
        
        MatrixElem elemToInsert = null;
        
        MatrixElem elem = cols[column];
        if(elem == null) {
            elemToInsert = new MatrixElem(value, link, column);
            cols[column] = elemToInsert;
        }
        else {
            MatrixElem predElem = null;
            while((elem != null) && (elem.getRow() < link)) {
                predElem = elem;
                elem = elem.getNextRow();
            }
            if((elem == null) || (elem.getRow() > link)) {
                // the element doesn't exist, so we have to create it.
                elemToInsert = new MatrixElem(value, link, column);
                elemToInsert.setNextRow(elem);
                if(predElem != null)
                    predElem.setNextRow(elemToInsert);
                else
                    cols[column] = elemToInsert;
            }
            else {
                // the element exists, so we update value and return (pointers
                // are already OK).
                elem.setValue(elem.getValue() + value);
                return;
            }
        }
        
        elem = rows[link];
        if(elem == null) {
            rows[link] = elemToInsert;
        }
        else {
            MatrixElem predElem = null;
            while((elem != null) && (elem.getColumn() < column)) {
                predElem = elem;
                elem = elem.getNextColumn();
            }
            
            // we don't need to test if the element exist or not because of
            // the return statement before !
            elemToInsert.setNextColumn(elem);
            if(predElem != null)
                predElem.setNextColumn(elemToInsert);
            else
                rows[link] = elemToInsert;
        }
    }
    
    /**
     * This method sets the element corresponding to <code>link</code>,
     * <code>src</code> and <code>dst</code> to <code>value</code>.
     *
     * The subclasses should call this method to set the elements of the
     * matrix.
     * @throws IllegalArgumentException If <code>link</code>, <code>src</code>
     * and/or <code>dst</code> have a bad
     * value.
     */
    protected final void set(float value, int link, int src, int dst) {
        if((link < 0) || (link >= rows.length) ||
                (src < 0) || (dst < 0) || (src >= nbNodes) || (dst >= nbNodes))
            throw new IllegalArgumentException("Bad value for link, src and/or"
                    +" dst: link = "+link
                    +" src = "+src+" dst = "+dst);
        
        int column = getKey(src, dst);
        
        MatrixElem elemToInsert = null;
        
        MatrixElem elem = cols[column];
        if(elem == null) {
            elemToInsert = new MatrixElem(value, link, column);
            cols[column] = elemToInsert;
        }
        else {
            MatrixElem predElem = null;
            while((elem != null) && (elem.getRow() < link)) {
                predElem = elem;
                elem = elem.getNextRow();
            }
            if((elem == null) || (elem.getRow() > link)) {
                // the element doesn't exist, so we have to create it.
                elemToInsert = new MatrixElem(value, link, column);
                elemToInsert.setNextRow(elem);
                if(predElem != null)
                    predElem.setNextRow(elemToInsert);
                else
                    cols[column] = elemToInsert;
            }
            else {
                // the element exists, so we update value and return (pointers
                // are already OK).
                elem.setValue(value);
                return;
            }
        }
        
        elem = rows[link];
        if(elem == null) {
            rows[link] = elemToInsert;
        }
        else {
            MatrixElem predElem = null;
            while((elem != null) && (elem.getColumn() < column)) {
                predElem = elem;
                elem = elem.getNextColumn();
            }
            
            // we don't need to test if the element exist or not because of
            // the return statement before !
            elemToInsert.setNextColumn(elem);
            if(predElem != null)
                predElem.setNextColumn(elemToInsert);
            else
                rows[link] = elemToInsert;
        }
    }
    
    /**
     * Sets the size of the matrix.
     * The subclasses should call this method to set the size of the
     * matrix.
     */
    protected final void setSize(int nbRows, int nbCols) {
        cols = new MatrixElem[nbCols];
        rows = new MatrixElem[nbRows];
        // solution of the equation nbNodes^2 - nbNodes = nbCols.
        nbNodes = (1 + (int) (Math.sqrt(4*nbCols+1))) / 2;
    }
    
    /**
     * Returns a string representing the matrix.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("\t");
        Domain domain = InterDomainManager.getInstance().getDefaultDomain();
        for(int i = 0; i < cols.length; ++i) {
            IntPair pair = getIds(i);
            try {
                String firstPart = domain.getConvertor().getNodeId(pair.getFirstInteger());
                //firstPart = firstPart.substring(0, firstPart.indexOf('.'));
                String sndPart = domain.getConvertor().getNodeId(pair.getSecondInteger());
                //sndPart = sndPart.substring(0, sndPart.indexOf('.'));
                sb.append(firstPart+"-"+sndPart+"\t");
            }
            catch(NodeNotFoundException e) {
                logger.error("NodeNotFoundException in RoutingMatrix::toString().");
            }
        }
        sb.append(System.getProperty("line.separator"));
        
        for(int i = 0; i < rows.length; ++i) {
            try {
                String linkId = domain.getConvertor().getLinkId(i);
                //String sndPart = linkId.split(" -> ")[1];
                //linkId = linkId.substring(0, linkId.indexOf('.'))+"-"+sndPart.substring(0, sndPart.indexOf('.'));
                sb.append(linkId+"\t");
            }
            catch(LinkNotFoundException e) {
                logger.error("NodeNotFoundException in RoutingMatrix::toString().");
            }
            
            MatrixElem elem = rows[i];
            if(elem == null) {
                for(int j = 0; j < cols.length; ++j)
                    sb.append("0.0\t");
            }
            else {
                int oldcol = 0;
                do {
                    int col = elem.getColumn();
                    for(int j = oldcol; j < col; ++j)
                        sb.append("0.0\t");
                    oldcol = col+1;
                    sb.append(elem.getValue()+"\t");
                    elem = elem.getNextColumn();
                } while(elem != null);
                for(; oldcol < cols.length; ++oldcol)
                    sb.append("0.0\t");
            }
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }
    
    /**
     * This class represents an element of the sparse matrix.
     */
    private class MatrixElem {
        
        private float value;
        // indices of the element
        private int row, col;
        // pointers to the next element on the same column and on the same row
        // respectively.
        private MatrixElem nextRow, nextColumn;
        
        /**
         Initialises a newly created <code>MatrixElem</code> object.
         @param value The value of the matrix element.
         @param row The row of the matrix element.
         @param col The column of the matrix element.
         @param nextRow The next element on the same column.
         @param nextColumn The next element on the same row.
         */
        public MatrixElem(float value, int row, int col, MatrixElem nextRow,
                MatrixElem nextColumn) {
            this(value, row, col);
            this.nextRow = nextRow;
            this.nextColumn = nextColumn;
        }
        
        /**
         Initialises a newly created <code>MatrixElem</code> object.
         @param value The value of the matrix element.
         @param row The row of the matrix element.
         @param col The column of the matrix element.
         */
        public MatrixElem(float value, int row, int col) {
            this.value = value;
            this.row = row;
            this.col = col;
        }
        
        /**
         Returns the column of the element.
         */
        public int getColumn() {
            return col;
        }
        
        /**
         Returns the next element on the same column.
         */
        public MatrixElem getNextRow() {
            return nextRow;
        }
        
        /**
         Returns the next element on the same row.
         */
        public MatrixElem getNextColumn() {
            return nextColumn;
        }
        
        /**
         Returns the row of the element.
         */
        public int getRow() {
            return row;
        }
        
        /**
         Returns the value of the element.
         */
        public float getValue() {
            return value;
        }
        
        /**
         Sets the next element on the same column.
         */
        public void setNextRow(MatrixElem nextRow) {
            this.nextRow = nextRow;
        }
        
        /**
         Sets the next element on the same row.
         */
        public void setNextColumn(MatrixElem nextColumn) {
            this.nextColumn = nextColumn;
        }
        
        /**
         Sets the value of this element.
         */
        public void setValue(float value) {
            this.value = value;
        }
    }
}

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
package be.ac.ulg.montefiore.run.totem.util;

/*
* Changes:
* --------
*
*
*/

/**
* Simple class that handle pairs of objects.
*
* <p>Creation date: 5 janv. 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class Pair<O1, O2> {
    private O1 first = null;
    private O2 second = null;

    public Pair(O1 first, O2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * return the first element of the pair.
     * @return
     */
    public O1 getFirst() {
        return first;
    }

    /**
     * sets the first element of the pair.
     * @param first
     */
    public void setFirst(O1 first) {
        this.first = first;
    }

    /**
     * get the second element of the pair.
     * @return
     */
    public O2 getSecond() {
        return second;
    }

    /**
     * set the second element of the pair
     * @param second
     */
    public void setSecond(O2 second) {
        this.second = second;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;

        final Pair pair = (Pair) o;

        if (first != null ? !first.equals(pair.first) : pair.first != null) return false;
        if (second != null ? !second.equals(pair.second) : pair.second != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (first != null ? first.hashCode() : 0);
        result = 29 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "First: " + first.toString() + " Second: " + second.toString();
    }

}

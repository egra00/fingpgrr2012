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
package it.unina.traffic;

/*
* Changes:
* --------
* JJ-MMM-YYY: <changes> (<Three-letter author acronym>) [Example : 9-May-2005: decreasing order by default for the fullmesh (FSK)]
*
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 23/11/2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ColorConsoleWriter implements Writer {
    public void writeRequest(String to, String type, String msg) {
        System.out.print("\033[32m");
        System.out.print("Send message to: ");
        System.out.print(to);
        if (type != null) System.out.print(". Message Type: " + type);
        System.out.println();
        System.out.print(msg);
        System.out.println("\033[0m");
    }

    public void writeResponse(String from, String type, String msg) {
        System.out.print("\033[34m");
        System.out.print("Message received from: ");
        System.out.print(from);
        if (type != null) System.out.print(". Message Type: " + type);
        System.out.println();
        System.out.print(msg);
        System.out.println("\033[0m");
    }

    public void writeError(String msg) {
        System.out.print("\033[31m");
        System.out.print(msg);
        System.out.println("\033[0m");
    }
}

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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.routingGUIModule;

import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.GraphManager;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/*
* Changes:
* --------
*
*/

/**
 * Class to display a list of path in a list.
 * When a path is selected in the list, it is highlighted in the vizualization panel
 * (see {@link GraphManager#highlight(be.ac.ulg.montefiore.run.totem.domain.model.Path)}).
 * <p/>
 * <p>Creation date: 21/06/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class PathMutableJList extends JList {
    public PathMutableJList() {
        super(new DefaultListModel());

        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            private GraphManager manager = GraphManager.getInstance();

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                Object o = getSelectedValue();
                if (o != null && o instanceof Path) {
                    manager.highlight((Path) o);
                } else {
                    manager.unHighlight();
                }
            }
        });
    }

    public DefaultListModel getModel() {
        return (DefaultListModel)super.getModel();
    }
}

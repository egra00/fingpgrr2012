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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.graph;

import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.SettableVertexLocationFunction;

import java.awt.event.InputEvent;
import java.util.HashSet;
import java.util.Set;


/*
* Changes:
* --------
*
*/

/**
* EditingModelGraphMouse that handle batch mode for every GraphMousePlugin implementing the BatchModeCapable interface.
*
* <p>Creation date: 3/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class MyEditingModalGraphMouse extends EditingModalGraphMouse {

    private Set<BatchModeCapable> batchPlugins;
    private boolean batchMode = false;

    public MyEditingModalGraphMouse() {
        super();
    }

    protected void loadPlugins() {
        pickingPlugin = new PickingGraphMousePlugin();
        animatedPickingPlugin = new AnimatedPickingGraphMousePlugin();
        translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
        scalingPlugin = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, in, out);
        rotatingPlugin = new RotatingGraphMousePlugin();
        shearingPlugin = new ShearingGraphMousePlugin();
        editingPlugin = new MyEditingGraphMousePlugin(); //same as EditingGraphMousePlugin() but only directed edges

        this.batchPlugins = new HashSet<BatchModeCapable>();

        add(scalingPlugin);
        setMode(Mode.EDITING);
    }

    public void setVertexLocations(SettableVertexLocationFunction vertexLocations) {
        ((MyEditingGraphMousePlugin)editingPlugin).setVertexLocations(vertexLocations);
    }

    public void setBatchMode(boolean batchMode) {
        this.batchMode = batchMode;
        for (BatchModeCapable b : batchPlugins) {
            b.setBatchMode(batchMode);
        }
    }

    public void add(GraphMousePlugin plugin) {
        if (plugin instanceof BatchModeCapable) {
            BatchModeCapable b = (BatchModeCapable)plugin;
            b.setBatchMode(batchMode);
            batchPlugins.add(b);
        }
        super.add(plugin);
    }

    public void remove(GraphMousePlugin plugin) {
        batchPlugins.remove(plugin);
        super.remove(plugin);
    }
}

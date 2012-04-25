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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.SpringUtilities;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 29-Nov-2007: fix bug (multiple equivalent value were displayed on domain change). Update javadoc. (GMO)
*/

/**
* Panel that permits to choose the classtype, setup and holding preemption level in a domain.
*
* <p>Creation date: 19/10/2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class DiffservPanel extends JPanel {

    private final static Logger logger = Logger.getLogger(DiffservPanel.class);

    private Domain domain;

    private JComboBox priorityCombo;
    private JComboBox classTypeCombo;
    private JComboBox setupCombo;
    private JComboBox holdingCombo;

    private boolean comboEvents = true;

    public DiffservPanel(Domain domain) {
        super();
        this.domain = domain;
        setupUI();
    }

    /**
     * Change the current domain
     * @param domain
     */
    public void setDomain(Domain domain) {
        this.domain = domain;
        fillCombos();
    }

    /**
     * Return the selected class type
     * @return
     */
    public int getClassType() {
        return (Integer)classTypeCombo.getSelectedItem();
    }

    /**
     * Return the selected setup preemption level
     * @return
     */
    public int getSetupLevel() {
        return (Integer)setupCombo.getSelectedItem();
    }

    /**
     * Return the selected holding preemption level
     * @return
     */
    public int getHoldingLevel() {
        return (Integer)holdingCombo.getSelectedItem();
    }

    private void fillCombos() {
        if (domain == null) return;

        comboEvents = false;

        classTypeCombo.removeAllItems();
        for (int c : domain.getAllCTId())
            classTypeCombo.addItem(c);

        priorityCombo.removeAllItems();
        for (int p : domain.getPriorities())
            priorityCombo.addItem(p);

        comboEvents = true;

        if (classTypeCombo.getItemCount() > 0) classTypeCombo.setSelectedIndex(0);
        if (priorityCombo.getItemCount() > 0) priorityCombo.setSelectedIndex(0);
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("DiffServ configuration"));
        JPanel priorityPanel = new JPanel(new GridLayout(1, 1));
        priorityPanel.setBorder(BorderFactory.createTitledBorder("Priority"));

        JPanel classPanel = new JPanel(new SpringLayout());
        classPanel.setBorder(BorderFactory.createTitledBorder("Class Type and preemption levels"));

        priorityCombo = new JComboBox();
        classTypeCombo = new JComboBox();
        setupCombo = new JComboBox();
        holdingCombo = new JComboBox();

        priorityCombo.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (comboEvents) priorityChanged();
            }
        });

        classTypeCombo.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (comboEvents) classTypeChanged();
            }
        });

        setupCombo.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if (comboEvents) setupChanged();
            }
        });

        fillCombos();

        priorityPanel.add(priorityCombo);

        classPanel.add(new JLabel("Class Type"));
        classPanel.add(classTypeCombo);
        classPanel.add(new JLabel("Setup preemption level"));
        classPanel.add(setupCombo);
        classPanel.add(new JLabel("Holding preemption level"));
        classPanel.add(holdingCombo);

        SpringUtilities.makeCompactGrid(classPanel, 3, 2, 0, 0, 10, 10);

        add(priorityPanel, BorderLayout.NORTH);
        add(classPanel, BorderLayout.CENTER);
    }

    private void setupChanged() {
        int classType = (Integer)classTypeCombo.getSelectedItem();
        int preemptionLevel = (Integer)setupCombo.getSelectedItem();
        if (!domain.isExistingPriority(preemptionLevel, classType)) {
            logger.fatal("Non existing priority. Check code.");
            return;
        }
        holdingCombo.setSelectedItem(preemptionLevel);
        int priority = domain.getPriority(preemptionLevel, classType);
        comboEvents = false;
        priorityCombo.setSelectedItem(priority);
        comboEvents = true;
    }

    private void classTypeChanged() {
        List<Integer> list = domain.getPreemptionLevels((Integer)classTypeCombo.getSelectedItem());

        comboEvents = false;

        setupCombo.removeAllItems();
        for (int s : list) {
            setupCombo.addItem(s);
        }

        holdingCombo.removeAllItems();
        for (int h : list) {
            holdingCombo.addItem(h);
        }

        comboEvents = true;

        setupCombo.setSelectedIndex(0);

    }

    private void priorityChanged() {
        int priority = (Integer)priorityCombo.getSelectedItem();
        int classType = domain.getClassType(priority);
        if (classType != (Integer)classTypeCombo.getSelectedItem())
            classTypeCombo.setSelectedItem(classType);
        int pl = domain.getPreemptionLevel(priority);
        setupCombo.setSelectedItem(pl);
    }

}

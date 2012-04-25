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

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/*
* Changes:
* --------
*
*/

/**
* This Panel allows to select the Classes of service that a lsp is able transport.
*
* <p>Creation date: 24/01/2008
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ClassesOfServicePanel extends JPanel {

    private Domain domain;

    private final MutableJList acceptedCoSJList;
    private final MutableJList notAcceptedCoSJList;
    private final JButton addBtn;
    private final JButton removeBtn;

    private static final String ALL = "<ALL>";
    private static final String NONE = "<NONE>";

    public ClassesOfServicePanel(Domain domain) {
        super();

        acceptedCoSJList = new MutableJList();
        notAcceptedCoSJList = new MutableJList();

        addBtn = new JButton("add >>");
        removeBtn = new JButton("<< remove");

        addBtn.addActionListener(new AddClassActionListener());
        removeBtn.addActionListener(new RemoveClassActionListener());

        setDomain(domain);

        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 5, 5, 5);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;

        add(new JLabel("Not accepted"), c);

        c.gridheight = 2;
        c.gridy++;
        add(notAcceptedCoSJList, c);

        c.weightx = 0.0; //change to 0.0
        c.gridheight = 1;
        c.gridy = 1;
        c.gridx++;
        add(addBtn, c);
        c.gridy++;
        add(removeBtn, c);

        c.weightx = 1.0;
        c.gridx++;
        c.gridy = 0;
        add(new JLabel("Accepted"), c);
        c.gridheight = 2;
        c.gridy++;
        add(acceptedCoSJList, c);

    }

    public void setDomain(Domain domain) {
        this.domain = domain;

        acceptedCoSJList.getModel().removeAllElements();
        notAcceptedCoSJList.getModel().removeAllElements();

        acceptedCoSJList.getModel().addElement(ALL);

        boolean empty = true;
        List<String> cls = domain.getClassesOfService();
        if (cls != null) {
            for (String className : cls) {
                notAcceptedCoSJList.getModel().addElement(className);
                empty = false;
            }
        }

        if (empty) {
            notAcceptedCoSJList.getModel().addElement(NONE);
        }
    }

    public List<String> getClassesOfService() {
        List<String> list = new ArrayList<String>();

        if (acceptedCoSJList.getModel().contains(ALL)) {
            for (String className : domain.getClassesOfService()) {
                list.add(className);
            }
            return list;
        }

        for (int i = 0; i < acceptedCoSJList.getModel().size(); i++) {
            String elem = (String)acceptedCoSJList.getModel().getElementAt(i);
            list.add(elem);
        }

        return list;
    }

    private class MutableJList extends JList {
        public MutableJList() {
            super(new DefaultListModel());
            //setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }

        public DefaultListModel getModel() {
            return (DefaultListModel)super.getModel();
        }
    }

    private class AddClassActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object[] selected = notAcceptedCoSJList.getSelectedValues();
            if (selected == null || selected.length <= 0) return;

            if (selected[0] == NONE) return;

            acceptedCoSJList.getModel().removeElement(ALL);

            for (Object o : selected) {
                notAcceptedCoSJList.getModel().removeElement(o);
                acceptedCoSJList.getModel().addElement(o);
            }

            if (notAcceptedCoSJList.getModel().size() == 0) {
                notAcceptedCoSJList.getModel().addElement(NONE);
            }
        }
    }

    private class RemoveClassActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object[] selected = acceptedCoSJList.getSelectedValues();
            if (selected == null || selected.length <= 0) return;

            if (selected[0] == ALL) return;

            notAcceptedCoSJList.getModel().removeElement(NONE);

            for (Object o : selected) {
                acceptedCoSJList.getModel().removeElement(o);
                notAcceptedCoSJList.getModel().addElement(o);
            }

            if (acceptedCoSJList.getModel().size() == 0) {
                acceptedCoSJList.getModel().addElement(ALL);
            }
        }
    }
}

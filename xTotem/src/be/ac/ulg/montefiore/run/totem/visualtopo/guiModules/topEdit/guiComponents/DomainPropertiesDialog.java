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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.guiComponents;

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.*;
import be.ac.ulg.montefiore.run.totem.util.Pair;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ExpandablePanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.SpringUtilities;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.List;

/*
* Changes:
* --------
*/

/**
 * Dialog to edit properties of a domain. Dialog is initialized with a domain instance. The empty method
 * {@link #postProcessingOnSuccess()} is called when the dialog is accepted by the user and no errors in the data are detected.
 * <p/>
 * <p/>
 * <p>Creation date: 03/10/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class DomainPropertiesDialog extends JDialog {
    private static final Logger logger = Logger.getLogger(DomainPropertiesDialog.class);

    private final DiffPanel diffPanel;

    private final JTextField title;
    private final JTextField date;
    private final JTextField author;
    private final JTextField aSId;
    private final JTextArea description;
    private final JComboBox bandwidth;
    private final JComboBox delay;
    private Map<Integer, Pair<Integer, Integer>> priorities = null;

    private Domain domain;

    /**
     * Create a dialog to edit the properties of the given domain.
     *
     * @param domain
     */
    public DomainPropertiesDialog(Domain domain) {
        super(TopEditGUI.getInstance(), "Domain Properties");

        this.domain = domain;

        title = new JTextField();
        author = new JTextField();
        date = new JTextField(DateFormat.getDateInstance(DateFormat.SHORT).format(Calendar.getInstance().getTime()));
        aSId = new JTextField();
        description = new JTextArea(5, 5);

        bandwidth = new JComboBox();
        bandwidth.addItem(BandwidthUnits.BPS);
        bandwidth.addItem(BandwidthUnits.KBPS);
        bandwidth.addItem(BandwidthUnits.MBPS);
        bandwidth.addItem(BandwidthUnits.GBPS);
        bandwidth.addItem(BandwidthUnits.TBPS);

        delay = new JComboBox();
        delay.addItem(DelayUnits.NS);
        delay.addItem(DelayUnits.Ï_S); //µ ?
        delay.addItem(DelayUnits.MS);
        delay.addItem(DelayUnits.S);

        diffPanel = new DiffPanel();
        setupUI();

        initValues();

        pack();
        setLocationRelativeTo(TopEditGUI.getInstance());
        setVisible(true);
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 300;
        c.insets = new Insets(0, 5, 5, 5);
        add(new JLabel("Title"), c);
        c.gridy++;
        add(title, c);
        c.gridy++;
        add(new JLabel("Author"), c);
        c.gridy++;
        add(author, c);
        c.gridy++;
        add(new JLabel("Date (M/D/YY)"), c);
        c.gridy++;
        add(date, c);
        c.gridy++;
        add(new JLabel("ASID *"), c);
        c.gridy++;
        add(aSId, c);
        c.gridy++;
        add(new JLabel("Description"), c);
        c.gridy++;

        JScrollPane scroll = new JScrollPane(description);
        scroll.setMinimumSize(new Dimension(50, 75));
        add(scroll, c);
        c.gridy++;
        add(new JLabel("Bandwidth Unit"), c);
        c.gridy++;
        add(bandwidth, c);
        c.gridy++;
        add(new JLabel("Delay Unit"), c);
        c.gridy++;
        add(delay, c);
        delay.setSelectedIndex(2);
        c.gridy++;
        add(new ExpandablePanel(this, "DiffServ", diffPanel), c);
        c.gridy++;

        JPanel buttonPanel = new JPanel();

        JButton btn = new JButton("Ok");
        buttonPanel.add(btn);
        btn.addActionListener(new AcceptListener());

        btn = new JButton("Cancel");
        buttonPanel.add(btn);
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        add(buttonPanel, c);
    }

    private void initValues() {
        title.setText(domain.getInfo().getTitle());
        if (domain.getInfo().isSetDate()) {
            date.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(domain.getInfo().getDate().getTime()));
        } else date.setText("");
        author.setText(domain.getInfo().getAuthor());
        aSId.setText(String.valueOf(domain.getASID()));
        description.setText(domain.getInfo().getDescription());

        java.util.List units = domain.getInfo().getUnits().getUnit();
        for (Object o : units) {
            UnitType unit = (UnitType) o;
            if (unit.getType() == UnitsType.BANDWIDTH) {
                bandwidth.setSelectedItem(unit.getValue());
            } else if (unit.getType() == UnitsType.DELAY) {
                delay.setSelectedItem(unit.getValue());
            }
        }

        // init values called in DiffPanel constructor 
        //diffPanel.initValues();
    }

    /**
     * This method is executed on success just before the dialog diappears.
     */
    protected void postProcessingOnSuccess() {}

    private class DiffPanel extends JPanel {

        private JLayeredPane addedPrioritiesPanel = null;
        private JPanel priorityPanel = null;
        private JPanel classPanel = null;
        private JPanel buttonPanel = null;
        private JComboBox priorityCombo = null;
        private JComboBox classTypeCombo = null;
        private JComboBox preemptionCombo = null;
        private JButton addButton = null;
        private String[] levels = {"0", "1", "2", "3", "4", "5", "6", "7"};

        public DiffPanel() {
            super();
            priorities = new HashMap<Integer, Pair<Integer, Integer>>();

            this.setupUI();

            initValues();
            fillPriorities();
        }

        public void initValues() {
            //info should be set as it is mandatory
            if (domain.getInfo().isSetDiffServ()) {
                for (Object o : domain.getInfo().getDiffServ().getPriority()) {
                    Information.DiffServType.PriorityType prio = (Information.DiffServType.PriorityType) o;

                    Pair<Integer, Integer> pp = new Pair<Integer, Integer>(prio.getCt(), prio.getPreemption());
                    priorities.put(prio.getId(), pp);
                    addPriority(String.valueOf(prio.getId()), String.valueOf(prio.getCt()), String.valueOf(prio.getPreemption()));
                }
            }
        }

        private void setupUI() {
            setBorder(BorderFactory.createTitledBorder("DiffServ configuration"));
            addedPrioritiesPanel = new JLayeredPane();
            addedPrioritiesPanel.setLayout(new GridLayout(0, 4, 5, 2));
            priorityPanel = new JPanel(new GridLayout(1, 1));
            priorityPanel.setBorder(BorderFactory.createTitledBorder("Priority"));

            classPanel = new JPanel(new SpringLayout());
            classPanel.setBorder(BorderFactory.createTitledBorder("Class Type and preemption levels"));

            buttonPanel = new JPanel(new BorderLayout());

            priorityCombo = new JComboBox(levels);
            classTypeCombo = new JComboBox(levels);
            preemptionCombo = new JComboBox(levels);
            addButton = new JButton("Add");

            classTypeCombo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    classTypeChanged();
                }
            });

            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addAction();
                }
            });

            addedPrioritiesPanel.add(new JLabel("ID"));
            addedPrioritiesPanel.add(new JLabel("CT"));
            addedPrioritiesPanel.add(new JLabel("Preempt"));
            addedPrioritiesPanel.add(new JLabel(" "));

            priorityPanel.add(priorityCombo);

            classPanel.add(new JLabel("Class Type"));
            classPanel.add(classTypeCombo);
            classPanel.add(new JLabel("Preemption level"));
            classPanel.add(preemptionCombo);
            SpringUtilities.makeCompactGrid(classPanel, 2, 2, 0, 0, 10, 10);

            buttonPanel.add(addButton, BorderLayout.LINE_END);

            addedPrioritiesPanel.setVisible(false);
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            add(addedPrioritiesPanel, c);
            c.gridy++;
            c.weighty = 0.0;
            add(priorityPanel, c);
            c.gridy++;
            add(classPanel, c);
            c.gridy++;
            c.weighty = 0.0;
            add(buttonPanel, c);
        }

        private void classTypeChanged() {
            /* fill with all possible values */
            preemptionCombo.removeAllItems();
            for (String i : levels)
                preemptionCombo.addItem(i);

            /* remove used values */
            for (Pair<Integer, Integer> pair : priorities.values()) {
                if (pair.getFirst().equals(Integer.valueOf((String) classTypeCombo.getSelectedItem())))
                    preemptionCombo.removeItem(Integer.toString(pair.getSecond().intValue()));
            }
        }

        private void fillPriorities() {
            priorityCombo.removeAllItems();
            for (String i : levels)
                if (!priorities.containsKey(Integer.valueOf(i)))
                    priorityCombo.addItem(i);
            classTypeChanged();
        }

        private void addAction() {
            Pair<Integer, Integer> p = new Pair<Integer, Integer>(Integer.valueOf((String) classTypeCombo.getSelectedItem()), Integer.valueOf((String) preemptionCombo.getSelectedItem()));
            priorities.put(Integer.valueOf((String) priorityCombo.getSelectedItem()), p);
            addPriority((String) priorityCombo.getSelectedItem(), (String) classTypeCombo.getSelectedItem(), (String) preemptionCombo.getSelectedItem());
            priorityCombo.removeItem(priorityCombo.getSelectedItem());
            preemptionCombo.removeItem(preemptionCombo.getSelectedItem());
            if (priorityCombo.getItemCount() == 0) {
                priorityPanel.setVisible(false);
                classPanel.setVisible(false);
                buttonPanel.setVisible(false);
            }
        }

        private void addPriority(String priority, String ct, String preempt) {
            JButton removeButton = new JButton("remove");
            removeButton.setPreferredSize(new Dimension(2, 2));
            removeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    removeAction(e);
                }
            });
            addedPrioritiesPanel.add(new JLabel(priority));
            addedPrioritiesPanel.add(new JLabel(ct));
            addedPrioritiesPanel.add(new JLabel(preempt));
            addedPrioritiesPanel.add(removeButton);
            addedPrioritiesPanel.setVisible(true);
        }

        private void removeAction(ActionEvent e) {
            JButton b = (JButton) e.getSource();
            JLayeredPane parent = (JLayeredPane) b.getParent();
            int pos = parent.getPosition(b);
            int limit = pos - 3;
            if (limit < 0)
                limit = 0;
            String priorityId = ((JLabel) parent.getComponent(limit)).getText();

            // remove priority from priorities
            priorities.remove(Integer.valueOf(priorityId));

            // add removed priority to priority combobox and update preemption levels
            fillPriorities();

            // remove all guiComponents on the same row
            for (; pos >= limit; pos--)
                parent.remove(pos);

            if (parent.getComponentCount() == 4)
                parent.setVisible(false);

            priorityPanel.setVisible(true);
            classPanel.setVisible(true);
            buttonPanel.setVisible(true);
        }
    }

    private class AcceptListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            try {
                ObjectFactory factory = new ObjectFactory();

                int asId;

                /* get the ASID */
                try {
                    asId = Integer.parseInt(aSId.getText());
                } catch (NumberFormatException e1) {
                    JOptionPane.showMessageDialog(DomainPropertiesDialog.this, "Parse error in ASID", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                /* get the date */
                Calendar da = null;
                if (date.getText() != null && !date.getText().equals("")) {
                    da = new GregorianCalendar();
                    try {
                        da.setTime((DateFormat.getDateInstance(DateFormat.SHORT)).parse(date.getText()));
                    } catch (ParseException e1) {
                        JOptionPane.showMessageDialog(DomainPropertiesDialog.this, "Parse error in Date: " + date.getText(), "Error", JOptionPane.ERROR_MESSAGE);
                        logger.warn(e1.getMessage());
                        return;
                    }
                }

                domain.setASID(asId);

                domain.getInfo().setDate(da);
                domain.getInfo().setTitle(title.getText());
                domain.getInfo().setAuthor(author.getText());
                domain.getInfo().setDescription(description.getText());
                domain.getInfo().getUnits().getUnit().clear();

                UnitType unit = factory.createUnitType();
                unit.setType(UnitsType.BANDWIDTH);
                unit.setValue(bandwidth.getSelectedItem());
                domain.getInfo().getUnits().getUnit().add(unit);

                unit = factory.createUnitType();
                unit.setType(UnitsType.DELAY);
                unit.setValue(delay.getSelectedItem());
                domain.getInfo().getUnits().getUnit().add(unit);

                /*
                // adding priorities
                if (priorities.size() == 0) {
                    JOptionPane.showMessageDialog(DomainPropertiesDialog.this, "Please define at least one classtype.", "Error", JOptionPane.ERROR_MESSAGE);
                    initValues();
                    return;
                }
                */

                domain.getInfo().unsetDiffServ();

                if (priorities.size() != 0) {
                    domain.getInfo().setDiffServ(factory.createInformationDiffServType());

                    List<Integer> prios = new ArrayList<Integer>(priorities.keySet());
                    Collections.sort(prios);

                    for (Integer prio : prios) {
                        Information.DiffServType.PriorityType type = factory.createInformationDiffServTypePriorityType();
                        Pair<Integer, Integer> pair = priorities.get(prio);
                        type.setId(prio.intValue());
                        type.setCt(pair.getFirst().intValue());
                        type.setPreemption(pair.getSecond().intValue());
                        domain.getInfo().getDiffServ().getPriority().add(type);
                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();
                String msg = ex.getMessage();
                if (msg == null)
                    JOptionPane.showMessageDialog(DomainPropertiesDialog.this, ex.getClass().getSimpleName(), "Error", JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(DomainPropertiesDialog.this, ex.getClass().getSimpleName() + ": " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                initValues();
                return;
            }

            postProcessingOnSuccess();
            dispose();
        }

    }

}

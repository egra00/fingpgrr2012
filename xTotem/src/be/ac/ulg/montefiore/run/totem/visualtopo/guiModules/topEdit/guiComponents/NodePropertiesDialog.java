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
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.NodeInterfaceImpl;
import be.ac.ulg.montefiore.run.totem.util.DataValidationTools;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.SetType;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ExpandablePanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.NodeInterfaceListCellRenderer;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.List;
import java.util.HashMap;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 14-Nov-2007: fix bug when setting node id on accept (GMO)
*/

/**
 * Dialog for node edition. Dialog is initialized with a node instance. The empty method
 * {@link #postProcessingOnSuccess()} is called when the dialog is accepted by the user and no errors in the data are detected.
 * <p/>
 * <p>Creation date: Oct 03, 2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class NodePropertiesDialog extends JDialog {
    private final static Logger logger = Logger.getLogger(NodePropertiesDialog.class);

    private final static ObjectFactory factory = new ObjectFactory();

    final private JTextField nodeId;
    final private JTextField rid;
    final private JTextArea description;
    final private JComboBox statusCombo;
    final private JComboBox typeCombo;

    // location related
    private final JComboBox setLocationCombo;
    private final JTextField longitudeField;
    private final JTextField latitudeField;

    private final InterfacesPanel interfacesPanel;

    private final NodeInterfaceMutableJList interfacesList;


    private Node node;
    private DomainDecorator domainDecorator;

    public NodePropertiesDialog(Node node, DomainDecorator domainDecorator) {
        super(TopEditGUI.getInstance(), "Edit node dialog", true);
        setLocationRelativeTo(getParent());

        this.domainDecorator = domainDecorator;
        this.node = node;

        nodeId = new JTextField();
        rid = new JTextField();
        description = new JTextArea(5, 5);
        statusCombo = new JComboBox();
        statusCombo.addItem(SetType.NOT_SET);
        statusCombo.addItem(StatusType.UP);
        statusCombo.addItem(StatusType.DOWN);

        typeCombo = new JComboBox();
        typeCombo.addItem(SetType.NOT_SET);
        typeCombo.addItem(NodeType.CORE);
        typeCombo.addItem(NodeType.EDGE);
        typeCombo.addItem(NodeType.NEIGH);
        typeCombo.addItem(NodeType.VIRTUAL);

        setLocationCombo = new JComboBox();
        setLocationCombo.addItem(SetType.NOT_SET);
        setLocationCombo.addItem(SetType.SET);
        setLocationCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (setLocationCombo.getSelectedItem() == SetType.NOT_SET) {
                    longitudeField.setEnabled(false);
                    latitudeField.setEnabled(false);
                } else {
                    longitudeField.setEnabled(true);
                    latitudeField.setEnabled(true);
                }
            }
        });
        longitudeField = new JTextField();
        latitudeField = new JTextField();

        interfacesList = new NodeInterfaceMutableJList();

        interfacesPanel = new InterfacesPanel();
        setupUI();

        initValues();
    }

    public void setupUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 5, 5, 5);
        add(new JLabel("Node ID *"), c);
        c.gridy++;
        add(nodeId, c);
        c.gridy++;
        add(new JLabel("RID"), c);
        c.gridy++;
        add(rid, c);
        c.gridy++;
        add(new JLabel("Description"), c);
        c.gridy++;
        add(description, c);
        c.gridy++;
        add(new JLabel("Status"), c);
        c.gridy++;
        add(statusCombo, c);
        c.gridy++;
        add(new JLabel("Type"), c);
        c.gridy++;
        add(typeCombo, c);
        c.gridy++;

        JPanel intPanel = new ExpandablePanel(this, "Interfaces", interfacesPanel);
        add(intPanel, c);
        c.gridy++;

        JPanel expPanel = new ExpandablePanel(this, "Location", new LocationPanel());
        add(expPanel, c);
        c.gridy++;

        JPanel buttonPanel = new JPanel();
        JButton btn = new JButton("Edit");
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

    /**
     * Initialise the values in the dialog from information in {@link #node}.
     */
    private void initValues() {
        nodeId.setText(node.getId());
        rid.setText(node.getRid());
        description.setText(node.getDescription());
        if (node.isSetStatus())
            statusCombo.setSelectedItem(node.getStatus());
        else statusCombo.setSelectedItem(SetType.NOT_SET);
        if (node.isSetType())
            typeCombo.setSelectedItem(node.getType());
        else typeCombo.setSelectedItem(SetType.NOT_SET);

        if (node.isSetLocation()) {
            setLocationCombo.setSelectedItem(SetType.SET);
            longitudeField.setText(String.valueOf(node.getLocation().getLongitude()));
            latitudeField.setText(String.valueOf(node.getLocation().getLatitude()));
        } else {
            setLocationCombo.setSelectedItem(SetType.NOT_SET);
        }

        interfacesPanel.initValues();
    }

    /**
     * This method is executed on success just before the dialog disappears.
     */
    protected void postProcessingOnSuccess() {}

    private class InterfacesPanel extends JPanel {
        private final JButton addItemBtn;
        private final JButton removeItemBtn;
        private final JButton editItemBtn;

        //associate an interface id to links that is connected to it (maximum links = 2)
        private final HashMap<String, Link[]> ifToLinks;

        public InterfacesPanel() {
            super();

            addItemBtn = new JButton("+");
            addItemBtn.addActionListener(new AddActionListener());
            removeItemBtn = new JButton("-");
            removeItemBtn.addActionListener(new RemoveActionListener());
            editItemBtn = new JButton("edit");
            editItemBtn.addActionListener(new EditActionListener());

            ifToLinks = new HashMap<String, Link[]>();

            if (domainDecorator.getDomain().getTopology().isSetLinks()) {
                for (Link link : (List<Link>)domainDecorator.getDomain().getTopology().getLinks().getLink()) {
                    if (link.getFrom().getNode().equals(node.getId())) {
                        if (link.getFrom().isSetIf()) {
                            Link[] links = ifToLinks.get(link.getFrom().getIf());
                            if (links == null) {
                                links = new Link[2];
                                links[0] = link;
                                ifToLinks.put(link.getFrom().getIf(), links);
                            } else {
                                if (links[0] != null) {
                                    logger.error("Multiple links outgoing of interface " + link.getFrom().getIf());
                                }
                                links[0] = link;
                            }
                        }
                    }
                    if (link.getTo().getNode().equals(node.getId())) {
                        if (link.getTo().isSetIf()) {
                            Link[] links = ifToLinks.get(link.getTo().getIf());
                            if (links == null) {
                                links = new Link[2];
                                links[1] = link;
                                ifToLinks.put(link.getTo().getIf(), links);
                            } else {
                                if (links[1] != null) {
                                    logger.error("Multiple links incoming at interface " + link.getTo().getIf());
                                }
                                links[1] = link;
                            }
                        }
                    }
                }
            }

            setupUI();
        }

        private void setupUI() {
            setBorder(BorderFactory.createTitledBorder("Interfaces"));
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 3;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.insets = new Insets(0, 5, 5, 5);
            c.fill = GridBagConstraints.BOTH;

            add(new JScrollPane(interfacesList), c);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx++;
            c.weightx = 0.0;
            c.gridheight = 1;

            add(addItemBtn, c);
            c.gridy++;
            add(removeItemBtn, c);
            c.gridy++;
            add(editItemBtn, c);
        }

        private void initValues() {
            interfacesList.getModel().removeAllElements();
            if (node.isSetInterfaces()) {
                for (NodeInterface nif : (List<NodeInterface>)node.getInterfaces().getInterface()) {
                    interfacesList.getModel().addElement(nif);
                }
            }
        }

        private class RemoveActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                if (interfacesList.getSelectedIndex() >= 0) {
                    NodeInterface nif = (NodeInterface)interfacesList.getSelectedValue();
                    Link[] links = ifToLinks.get(nif.getId());
                    if (links != null) {
                        int res = JOptionPane.showConfirmDialog(NodePropertiesDialog.this, "Some links are connected to this interface. If you suppress it, the links will be connected to the node with an undefined interface. Continue ?", "Confirm", JOptionPane.YES_NO_OPTION);
                        if (res == JOptionPane.YES_OPTION) {
                            if (links[0] != null) {
                                links[0].getFrom().unsetIf();
                            }
                            if (links[1] != null) {
                                links[1].getTo().unsetIf();
                            }
                            node.getInterfaces().getInterface().remove(nif);
                            ifToLinks.remove(nif.getId());
                        } else {
                            //cancelled
                            return;
                        }
                    }
                    node.getInterfaces().getInterface().remove(nif);
                    ifToLinks.remove(nif.getId());
                    InterfacesPanel.this.initValues();
                }
            }
        }

        private class AddActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                final NodeInterface nif = new NodeInterfaceImpl();

                JDialog dialog = new InterfaceDialog(nif) {
                    protected void postProcessingOnSuccess() {
                        try {
                            if (!node.isSetInterfaces()) {
                                node.setInterfaces(factory.createNodeInterfacesType());
                            }
                            node.getInterfaces().getInterface().add(nif);
                        } catch (JAXBException e1) {
                            logger.error("Unable to create interfaces.");
                        }
                        InterfacesPanel.this.initValues();
                    }
                };
                dialog.pack();
                dialog.setLocationRelativeTo(NodePropertiesDialog.this);
                dialog.setVisible(true);
            }
        }

        private class EditActionListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                if (interfacesList.getSelectedIndex() >= 0) {
                    final NodeInterface nif = (NodeInterface)interfacesList.getSelectedValue();
                    final String oldId = nif.getId();
                    JDialog dialog = new InterfaceDialog(nif) {
                        protected void postProcessingOnSuccess() {
                            if (!nif.getId().equals(oldId)) {
                                Link[] links = ifToLinks.get(oldId);
                                if (links != null) {
                                    if (links[0] != null) links[0].getFrom().setIf(nif.getId());
                                    if (links[1] != null) links[1].getTo().setIf(nif.getId());
                                }
                            }
                        }
                    };
                    dialog.pack();
                    dialog.setLocationRelativeTo(NodePropertiesDialog.this);
                    dialog.setVisible(true);
                }
            }
        }

        private class InterfaceDialog extends JDialog {
            private final JTextField idField;
            private final JComboBox statusCombo;
            private final JTextField ipField;
            private final JTextField maskField;

            private final NodeInterface nif;

            public InterfaceDialog(NodeInterface nif) {
                super(NodePropertiesDialog.this, "Interface properties", true);

                this.nif = nif;

                idField = new JTextField(20);
                statusCombo = new JComboBox();
                statusCombo.addItem(SetType.NOT_SET);
                statusCombo.addItem(StatusType.UP);
                statusCombo.addItem(StatusType.DOWN);

                ipField = new JTextField(20);
                maskField = new JTextField(20);

                initValues();

                setupUI();
            }

            private void setupUI() {
                setLayout(new BorderLayout());

                JPanel mainPanel = new JPanel(new GridBagLayout());

                GridBagConstraints c = new GridBagConstraints();

                c.gridx = 0;
                c.gridy = 0;
                c.gridwidth = 1;
                c.gridheight = 1;
                c.weightx = 1.0;
                c.weighty = 1.0;
                c.insets = new Insets(0, 5, 5, 5);
                c.fill = GridBagConstraints.BOTH;

                mainPanel.add(new JLabel("id: *"), c);
                c.gridy++;
                mainPanel.add(idField, c);
                c.gridy++;
                mainPanel.add(new JLabel("Status:"), c);
                c.gridy++;
                mainPanel.add(statusCombo, c);
                c.gridy++;
                mainPanel.add(new JLabel("Ip address:"), c);
                c.gridy++;
                mainPanel.add(ipField, c);
                c.gridy++;
                mainPanel.add(new JLabel("Mask:"), c);
                c.gridy++;
                mainPanel.add(maskField, c);

                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

                JButton btn = new JButton("Ok");
                btn.addActionListener(new AcceptActionListener());
                btnPanel.add(btn);

                btn = new JButton("Cancel");
                btn.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                });
                btnPanel.add(btn);

                add(mainPanel, BorderLayout.CENTER);
                add(btnPanel, BorderLayout.SOUTH);
            }

            private void initValues() {
                if (nif.isSetId())
                    idField.setText(nif.getId());
                else idField.setText("");

                if (nif.isSetStatus())
                    statusCombo.setSelectedItem(nif.getStatus());
                else statusCombo.setSelectedItem(SetType.NOT_SET);

                if (nif.isSetIp()) {
                    if (nif.getIp().isSetValue())
                        ipField.setText(nif.getIp().getValue());
                    else ipField.setText("");
                    if (nif.getIp().isSetMask())
                        maskField.setText(nif.getIp().getMask());
                    else maskField.setText("");
                } else {
                    ipField.setText("");
                    maskField.setText("");
                }
            }

            protected void postProcessingOnSuccess() {
            }

            private class AcceptActionListener implements ActionListener {

                public void actionPerformed(ActionEvent e) {

                    try {
                        if (idField.getText() == null || idField.getText().equals("")) {
                            JOptionPane.showMessageDialog(InterfaceDialog.this, "Interface Id must be set.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        if (node.isSetInterfaces()) {
                            for (NodeInterface aNif : (List<NodeInterface>)node.getInterfaces().getInterface()) {
                                if (aNif != nif && aNif.getId().equals(idField.getText())) {
                                    JOptionPane.showMessageDialog(InterfaceDialog.this, "An interface with same id already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                        }

                        if (ipField.getText() != null && !ipField.getText().equals("") && !DataValidationTools.isIPAddress(ipField.getText())) {
                            JOptionPane.showMessageDialog(InterfaceDialog.this, "Field ip is not an IP address.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        if (maskField.getText() != null && !maskField.getText().equals("") && !DataValidationTools.isIPAddress(maskField.getText())) {
                            JOptionPane.showMessageDialog(InterfaceDialog.this, "Field mask is not an IP address.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        nif.setId(idField.getText());
                        if (statusCombo.getSelectedItem() == SetType.NOT_SET)
                            nif.unsetStatus();
                        else
                            nif.setStatus((StatusType) statusCombo.getSelectedItem());

                        if ((ipField.getText() == null || ipField.getText().equals("")) && (maskField.getText() == null || maskField.getText().equals(""))) {
                            nif.unsetIp();
                        } else {
                            nif.setIp(factory.createNodeInterfaceIpType());
                            if (ipField.getText() != null && !ipField.getText().equals("")) {
                                nif.getIp().setValue(ipField.getText());
                            }
                            if (maskField.getText() != null && !maskField.getText().equals("")) {
                                nif.getIp().setMask(maskField.getText());
                            }
                        }
                        postProcessingOnSuccess();
                        dispose();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        String msg = ex.getMessage();
                        if (msg == null)
                            JOptionPane.showMessageDialog(NodePropertiesDialog.this, ex.getClass().getSimpleName(), "Error", JOptionPane.ERROR_MESSAGE);
                        else
                            JOptionPane.showMessageDialog(NodePropertiesDialog.this, ex.getClass().getSimpleName() + ": " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                        initValues();
                        return;
                    }
                }
            }
        }
    }


    private class LocationPanel extends JPanel {

        public LocationPanel() {
            super();

            setupUI();
        }

        private void setupUI() {
            setBorder(BorderFactory.createTitledBorder("Location"));
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.insets = new Insets(0, 5, 5, 5);
            c.fill = GridBagConstraints.HORIZONTAL;

            add(setLocationCombo, c);
            c.gridy++;
            add(new JLabel("Longitude:"), c);
            c.gridy++;
            add(longitudeField, c);
            c.gridy++;
            add(new JLabel("Latitude:"), c);
            c.gridy++;
            add(latitudeField, c);
        }
    }


    private class AcceptListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                if (nodeId.getText() == null || nodeId.getText().equals("")) {
                    JOptionPane.showMessageDialog(NodePropertiesDialog.this, "An ID must be specified", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Node domainNode = domainDecorator.getNode(nodeId.getText());
                if (domainNode != null && domainNode != node) {
                    JOptionPane.showMessageDialog(NodePropertiesDialog.this, "Id already exists in the domain", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                node.setId(nodeId.getText());

                if (rid.getText() == null || rid.getText().equals("")) {
                    node.unsetRid();
                } else if (DataValidationTools.isIPAddress(rid.getText())) {
                    node.setRid(rid.getText());
                } else {
                    JOptionPane.showMessageDialog(NodePropertiesDialog.this, "Rid should be an IP address or not set", "Error", JOptionPane.ERROR_MESSAGE);
                    initValues();
                    return;
                }

                if (description.getText() == null || description.getText().equals("")) {
                    node.unsetDescription();
                } else {
                    node.setDescription(description.getText());
                }

                if (statusCombo.getSelectedItem() == SetType.NOT_SET)
                    node.unsetStatus();
                else node.setStatus((StatusType) statusCombo.getSelectedItem());

                if (typeCombo.getSelectedItem() == SetType.NOT_SET)
                    node.unsetType();
                else node.setType((NodeType) typeCombo.getSelectedItem());

                if (setLocationCombo.getSelectedItem() == SetType.NOT_SET) {
                    node.unsetLocation();
                } else { // location is set
                    float longitude = Float.parseFloat(longitudeField.getText());
                    float latitude = Float.parseFloat(latitudeField.getText());
                    if (!node.isSetLocation()) {
                        node.setLocation(factory.createNodeLocationType());
                    }
                    node.getLocation().setLongitude(longitude);
                    node.getLocation().setLatitude(latitude);
                }

                postProcessingOnSuccess();
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                String msg = ex.getMessage();
                if (msg == null)
                    JOptionPane.showMessageDialog(NodePropertiesDialog.this, ex.getClass().getSimpleName(), "Error", JOptionPane.ERROR_MESSAGE);
                else
                    JOptionPane.showMessageDialog(NodePropertiesDialog.this, ex.getClass().getSimpleName() + ": " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                initValues();
                return;
            }
        }
    }


    private class NodeInterfaceMutableJList extends JList {
        public NodeInterfaceMutableJList() {
            super(new DefaultListModel());
            setCellRenderer(new NodeInterfaceListCellRenderer() {
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof NodeInterface) {
                        String id = ((NodeInterface)value).getId();
                        Link[] links = interfacesPanel.ifToLinks.get(id);
                        if (links != null && (links[0] != null || links[1] != null)) {
                            setForeground(Color.gray);
                        }
                    }
                    return this;
                }
            });
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            super.setPrototypeCellValue("so-0/0/0 (255.255.255.255)");
        }

        public DefaultListModel getModel() {
            return (DefaultListModel)super.getModel();
        }

    }

}

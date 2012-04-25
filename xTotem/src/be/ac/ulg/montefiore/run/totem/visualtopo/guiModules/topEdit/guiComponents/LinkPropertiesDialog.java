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

import javax.swing.*;

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.*;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ExpandablePanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.LinkDecorator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.SetType;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.NodeInterfaceListCellRenderer;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 27-Nov-2007: fix bug when mrbw is null and bw is given (GMO)
*/

/**
 * Dialog for link edition.  Dialog is initialized with a linkDecorator instance. The empty method
 * {@link #postProcessingOnSuccess()} is called when the dialog is accepted by the user and no errors in the data are detected.
 *
 * <p>Creation date: Oct 03, 2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 *
 */
public class LinkPropertiesDialog extends JDialog {
    private static final Logger logger = Logger.getLogger(LinkPropertiesDialog.class);

    private final static ObjectFactory factory = new ObjectFactory();

    private final JTextField linkId;
    private final JTextArea description;
    private final JTextField bwField;
    private final JTextField mrbwField;
    private final JTextField delayField;
    private final JTextField metricField;
    private final JTextField teMetricField;
    //private final JTextField adminField;
    private final JComboBox statusCombo;
    private final JComboBox typeCombo;

    private final JComboBox ifFromCombo;
    private final JComboBox ifToCombo;

    /* badnwidth constraints textFields size=8 */
    private final JTextField[] bwcs;

    private LinkDecorator linkDecorator;
    private Link link;

    private DomainDecorator domainDecorator;
    private Domain domain;

    private String bwUnit = null;
    private String delayUnit = null;

    private final IGPPanel igpPanel;
    private final InterfacesPanel ifPanel;

    /* associate an interface of the source node with links (0: outgoing, 1: incoming) */
    private final HashMap<String, Link[]> ifFromLinks;
    /* associate an interface of the destination node with links (0: outgoing, 1: incoming) */
    private final HashMap<String, Link[]> ifToLinks;

    public LinkPropertiesDialog(DomainDecorator domainDecorator, LinkDecorator linkDecorator) {
        super(TopEditGUI.getInstance(), "Link properties", true);
        super.setLocationRelativeTo(getParent());

        this.linkDecorator = linkDecorator;
        this.link = linkDecorator.getLink();
        this.domainDecorator = domainDecorator;
        this.domain = domainDecorator.getDomain();

        linkId = new JTextField();
        description = new JTextArea(5, 5);
        bwField = new JTextField(10);
        delayField = new JTextField(10);

        statusCombo = new JComboBox();
        statusCombo.addItem(SetType.NOT_SET);
        statusCombo.addItem(StatusType.UP);
        statusCombo.addItem(StatusType.DOWN);

        typeCombo = new JComboBox();
        typeCombo.addItem(SetType.NOT_SET);
        typeCombo.addItem(LinkType.ACCESS);
        typeCombo.addItem(LinkType.INTER);
        typeCombo.addItem(LinkType.INTRA);
        typeCombo.addItem(LinkType.PEERING);
        typeCombo.addItem(LinkType.VIRTUAL);

        //igp
        metricField = new JTextField();
        teMetricField = new JTextField();
        mrbwField = new JTextField();

        //interfaces
        ifToLinks = new HashMap<String, Link[]>();
        ifFromLinks = new HashMap<String, Link[]>();
        String fromNode = link.getFrom().getNode();
        String toNode = link.getTo().getNode();
        if (domain.getTopology().isSetLinks()) {
        for (Link link : (List<Link>)domainDecorator.getDomain().getTopology().getLinks().getLink()) {
            if (link.getFrom().getNode().equals(fromNode)) {
                if (link.getFrom().isSetIf()) {
                    Link[] links = ifFromLinks.get(link.getFrom().getIf());
                    if (links == null) {
                        links = new Link[2];
                        links[0] = link;
                        ifFromLinks.put(link.getFrom().getIf(), links);
                    } else {
                        if (links[0] != null) {
                            logger.error("Multiple links outgoing of interface " + link.getFrom().getIf());
                        }
                        links[0] = link;
                    }
                }
            }
            if (link.getTo().getNode().equals(fromNode)) {
                if (link.getTo().isSetIf()) {
                    Link[] links = ifFromLinks.get(link.getTo().getIf());
                    if (links == null) {
                        links = new Link[2];
                        links[1] = link;
                        ifFromLinks.put(link.getTo().getIf(), links);
                    } else {
                        if (links[1] != null) {
                            logger.error("Multiple links incoming at interface " + link.getTo().getIf());
                        }
                        links[1] = link;
                    }
                }
            }
            if (link.getFrom().getNode().equals(toNode)) {
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
            if (link.getTo().getNode().equals(toNode)) {
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

        ifFromCombo = new JComboBox();
        ifFromCombo.setRenderer(new NodeInterfaceListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NodeInterface) {
                    String fromId = ((NodeInterface)value).getId();
                    if (!isSourceInterfaceSelectable(fromId))
                        setForeground(Color.gray);
                }
                return this;
            }
        });
        ifFromCombo.addItem(SetType.NOT_SET);
        Node from = domainDecorator.getNode(link.getFrom().getNode());
        if (from != null) {
            if (from.isSetInterfaces()) {
                for (NodeInterface nif : (List<NodeInterface>)from.getInterfaces().getInterface()) {
                    ifFromCombo.addItem(nif);
                }
            }
        } else {
            logger.warn("From node not found in the domain.");
        }

        ifToCombo = new JComboBox();
        ifToCombo.setRenderer(new NodeInterfaceListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String fromId;
                if (ifFromCombo.getSelectedItem() == SetType.NOT_SET) {
                    fromId = null;
                } else {
                    fromId = ((NodeInterface) ifFromCombo.getSelectedItem()).getId();
                }
                String toId;
                if (value == SetType.NOT_SET)
                    toId = null;
                else
                    toId = ((NodeInterface) value).getId();
                if (!isInterfaceCompatible(fromId, toId))
                    setForeground(Color.gray);
                return this;
            }
        });
        ifToCombo.addItem(SetType.NOT_SET);
        Node to = domainDecorator.getNode(link.getTo().getNode());
        if (to != null) {
            if (to.isSetInterfaces()) {
                for (NodeInterface nif : (List<NodeInterface>)to.getInterfaces().getInterface()) {
                    ifToCombo.addItem(nif);
                }
            }
        } else {
            logger.warn("To node not found in the domain.");
        }

        // create a textfield for each class type
        bwcs = new JTextField[8];
        Arrays.fill(bwcs, null);

        java.util.List units = domain.getInfo().getUnits().getUnit();
        for (Object o : units) {
            UnitType unit = (UnitType) o;
            if (unit.getType() == UnitsType.BANDWIDTH) {
                bwUnit = ((BandwidthUnits)unit.getValue()).toString();
            } else if (unit.getType() == UnitsType.DELAY) {
                delayUnit = ((DelayUnits)unit.getValue()).toString();
            }
        }

        //info should be set as it is mandatory
        if (domain.getInfo().isSetDiffServ()) {
            for (Object o : domain.getInfo().getDiffServ().getPriority()) {
                Information.DiffServType.PriorityType prio = (Information.DiffServType.PriorityType) o;
                bwcs[prio.getCt()] = new JTextField();
            }
        }

        igpPanel = new IGPPanel();
        ifPanel = new InterfacesPanel();

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
        add(new JLabel("Link ID *"), c);
        c.gridy++;
        add(linkId, c);
        c.gridy++;
        add(new JLabel("Description"), c);
        c.gridy++;
        add(description, c);
        c.gridy++;
        add(new JLabel("Bandwidth"), c);
        c.gridwidth = 1;
        c.gridy++;
        add(bwField, c);
        c.weightx = 0.0;
        c.gridx++;
        add(new JLabel(bwUnit), c);
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.gridy++;
        add(new JLabel("Delay"), c);
        c.gridwidth = 1;
        c.gridy++;
        add(delayField, c);
        c.weightx = 0.0;
        c.gridx++;
        add(new JLabel(delayUnit), c);
        c.gridx = 0;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.gridy++;
        add(new JLabel("Status"), c);
        c.gridy++;
        add(statusCombo, c);
        c.gridy++;
        add(new JLabel("Type"), c);
        c.gridy++;
        add(typeCombo, c);
        c.gridy++;
        add(new ExpandablePanel(this, "Interfaces", ifPanel), c);
        c.gridy++;
        add(new ExpandablePanel(this, "IGP", igpPanel), c);
        c.gridy++;
        JPanel buttonPanel = new JPanel();
        JButton btn = new JButton("Edit");
        buttonPanel.add(btn);
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                addLinkAction();
            }
        });
        btn = new JButton("Cancel");
        buttonPanel.add(btn);
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        add(buttonPanel, c);
    }

    /**
     * Init dialog window in case of a link edition
     *
     */
    private void initValues() {
        linkId.setText(link.getId());
        description.setText(link.getDescription());
        // sets to link capacity
        LinkIgp igpLink = linkDecorator.getLinkIgp();

        /* TODO: maybe do this stuff on accept, just fill fields here */
        boolean hasBw = link.isSetBw();
        boolean hasMbw;
        boolean hasMrbw;
        if (igpLink == null || !igpLink.isSetStatic()) {
            hasMrbw = false;
            hasMbw = false;
        } else {
            hasMrbw = igpLink.getStatic().isSetMrbw();
            hasMbw = igpLink.getStatic().isSetMbw();
        }

        if (hasMbw && !hasMrbw) {
            logger.warn("Igp link " + link.getId() + " has mbw set but not mrbw.");
            if (link.isSetBw()) {
                logger.warn("Setting mrbw to the value of bw (bw:" + link.getBw() + " mbw:" + igpLink.getStatic().getMbw() +")");
                igpLink.getStatic().setMrbw(link.getBw());
            } else {
                logger.warn("Setting mrbw to the value of mbw (mbw:" + igpLink.getStatic().getMbw() +")");
                igpLink.getStatic().setMrbw(igpLink.getStatic().getMbw());
            }
            hasMrbw = true;
        } else if (!hasMbw && hasMrbw) {
            logger.warn("Igp link " + link.getId() + " has mrbw set but not mbw.");
            if (link.isSetBw()) {
                logger.warn("Setting mbw to the value of bw (bw:" + link.getBw() + " mrbw:" + igpLink.getStatic().getMrbw() +")");
                igpLink.getStatic().setMbw(link.getBw());
            } else {
                logger.warn("Setting mbw to the value of mrbw (mrbw:" + igpLink.getStatic().getMrbw() +")");
                igpLink.getStatic().setMbw(igpLink.getStatic().getMrbw());
            }
            hasMbw = true;
        }

        // has the 3 values
        if (hasBw && hasMrbw) {
            if (link.getBw() != igpLink.getStatic().getMrbw() && link.getBw() != igpLink.getStatic().getMbw()) {
                logger.warn("Bandwidth field differs from both mbw and mrbw. Setting it to mbw");
                link.setBw(igpLink.getStatic().getMbw());
            }
        }

        if (hasMbw) {
            bwField.setText(String.valueOf(igpLink.getStatic().getMbw()));
        } else {
            if (link.isSetBw())
                bwField.setText(String.valueOf(link.getBw()));
            else bwField.setText("");
        }

        /*======================*/

        if (link.isSetDelay())
            delayField.setText(String.valueOf(link.getDelay()));
        else delayField.setText("");

        if (link.isSetStatus())
            statusCombo.setSelectedItem(link.getStatus());
        else statusCombo.setSelectedItem(SetType.NOT_SET);
        if (link.isSetType())
            typeCombo.setSelectedItem(link.getType());
        else typeCombo.setSelectedItem(SetType.NOT_SET);

        for (int i = 0; i < 8; i++) {
            if (bwcs[i] != null) {
                bwcs[i].setText(null);
            }
        }

        if (igpLink == null || !igpLink.isSetStatic()) {
            metricField.setText(null);
            teMetricField.setText(null);
            mrbwField.setText(null);
        } else {
            LinkIgp.StaticType staticT = igpLink.getStatic();
            metricField.setText(String.valueOf(staticT.getMetric()));
            teMetricField.setText(String.valueOf(staticT.getTeMetric()));
            mrbwField.setText(String.valueOf(staticT.getMrbw()));
            if (staticT.isSetDiffServ()) {
                for (Object o : staticT.getDiffServ().getBc()) {
                    LinkIgp.StaticType.DiffServType.BcType type = (LinkIgp.StaticType.DiffServType.BcType) o;
                    if (bwcs[type.getId()] == null) {
                        logger.error("Trying to set bc value for an non existing CT: " + type.getId());
                    } else {
                        bwcs[type.getId()].setText(String.valueOf(type.getValue()));
                    }
                }
            }
        }

        //interfaces
        if (!link.getFrom().isSetIf()) {
            ifFromCombo.setSelectedItem(SetType.NOT_SET);
        } else {
            String nif = link.getFrom().getIf();
            // select the node interface with matching id
            for (int i = 0; i < ifFromCombo.getItemCount(); i++) {
                if (ifFromCombo.getItemAt(i) instanceof NodeInterface && ((NodeInterface)ifFromCombo.getItemAt(i)).getId().equals(nif)) {
                    ifFromCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (!link.getTo().isSetIf()) {
            ifToCombo.setSelectedItem(SetType.NOT_SET);
        } else {
            String nif = link.getTo().getIf();
            // select the node interface with matching id
            for (int i = 0; i < ifToCombo.getItemCount(); i++) {
                if (ifToCombo.getItemAt(i) instanceof NodeInterface && ((NodeInterface)ifToCombo.getItemAt(i)).getId().equals(nif)) {
                    ifToCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void addLinkAction() {

        /* First, parse all entries */

        if (linkId.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Link id must be specified", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        /*
        if (bwField.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Bandwidth must be specified", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }*/

        LinkDecorator domainLink = domainDecorator.getLink(linkId.getText());
        if (domainLink != null && domainLink.getLink() != link) {
            JOptionPane.showMessageDialog(this, "Id Already exists", "Error", JOptionPane.ERROR_MESSAGE);
            return;            
        }

        Float bw = null;
        Float delay = null;
        Float metric = null;
        Float teMetric = null;
        Float mrbw = null;

        float bcs[] = new float[8];
        boolean bcSet;

        String name = "";
        try {
            name = "Bandwidth";
            if (bwField.getText() != null && !bwField.getText().equals(""))
                bw = new Float(bwField.getText());

            name = "Delay";
            if (delayField.getText() != null && !delayField.getText().equals(""))
                delay = new Float(delayField.getText());

            name = "Metric";
            if (metricField.getText() != null && !metricField.getText().equals(""))
                metric = new Float(metricField.getText());

            name = "TE Metric";
            if (teMetricField.getText() != null && !teMetricField.getText().equals(""))
                teMetric = new Float(teMetricField.getText());

            name = "Maximum reservable bandwidth";
            if (mrbwField.getText() != null && !mrbwField.getText().equals(""))
                mrbw = new Float(mrbwField.getText());

            boolean bwcsAllSet = true;
            boolean bwcsNoneSet = true;
            for (int i = 0; i < 8; i++) {
                if (bwcs[i] != null) {
                    name = "ClassType " + i;
                    if (bwcs[i].getText() == null || bwcs[i].getText().equals(""))
                        bwcsAllSet = false;
                    else {
                        bcs[i] = Float.parseFloat(bwcs[i].getText());
                        bwcsNoneSet = false;
                    }
                }
            }

            if (!bwcsAllSet && !bwcsNoneSet) {
                JOptionPane.showMessageDialog(this, "Reservable bandwidth should be set for all or none classtypes", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            bcSet = !bwcsNoneSet;

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, name + " must be a float", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {

            link.setId(linkId.getText());

            if (description.getText() == null || description.getText().equals(""))
                link.unsetDescription();
            else
                link.setDescription(description.getText());

            if (delay == null)
                link.unsetDelay();
            else
                link.setDelay(delay.floatValue());

            if (statusCombo.getSelectedItem() == SetType.NOT_SET)
                link.unsetStatus();
            else link.setStatus((StatusType) statusCombo.getSelectedItem());

            if (typeCombo.getSelectedItem() == SetType.NOT_SET)
                link.unsetType();
            else link.setType((LinkType) typeCombo.getSelectedItem());

            LinkIgp igpLink = linkDecorator.getLinkIgp();
            //see if igpLink should be created/removed
            if (!bcSet && metric == null && teMetric == null && mrbw == null) {
                // no igpLink is needed: remove from decorator
                if (igpLink != null) {
                    igpLink = null;
                    linkDecorator.unsetLinkIgp();
                }
                if (bw != null) link.setBw(bw.floatValue());
                else link.unsetBw();
            } else {
                //create the igpLink if it doesn't exists
                if (igpLink == null) {
                    igpLink = factory.createLinkIgp();
                    linkDecorator.setLinkIgp(igpLink);
                }

                // fill values in static
                if (!igpLink.isSetStatic()) {
                    igpLink.setStatic(factory.createLinkIgpStaticType());
                }

                igpLink.setId(linkId.getText());

                if (bw != null) {
                    if (!bcSet && mrbw == null) {
                        // if we do not provide values for the BCs and for mrbw,
                        // the bandwidth is set the topology part allowing us
                        // to provide only metric and temetric in igp part
                        link.setBw(bw.floatValue());
                    } else {
                        // we could also set in the topology part
                        //link.setBw(bw);
                        igpLink.getStatic().setMbw(bw.floatValue());
                        // if we set mbw, also set mrbw
                        if (mrbw == null) igpLink.getStatic().setMrbw(bw.floatValue());
                    }
                }

                if (mrbw != null)
                    igpLink.getStatic().setMrbw(mrbw.floatValue());
                else if (bw == null) {
                    igpLink.getStatic().unsetMrbw();
                }

                if (metric != null)
                    igpLink.getStatic().setMetric(metric.floatValue());
                else
                    igpLink.getStatic().unsetMetric();

                if (teMetric != null)
                    igpLink.getStatic().setTeMetric(teMetric.floatValue());
                else
                    igpLink.getStatic().unsetTeMetric();

                if (bcSet) {
                    if (!igpLink.getStatic().isSetDiffServ()) {
                        igpLink.getStatic().setDiffServ(factory.createLinkIgpStaticTypeDiffServType());
                    }
                    igpLink.getStatic().getDiffServ().setBcm(BcmType.MAM);
                    igpLink.getStatic().getDiffServ().unsetBc();
                    java.util.List bcsList = igpLink.getStatic().getDiffServ().getBc();
                    for (int i = 0; i < 8; i++) {
                        if (bwcs[i] != null) {
                            LinkIgp.StaticType.DiffServType.BcType bct = factory.createLinkIgpStaticTypeDiffServTypeBcType();
                            bct.setId(i);
                            bct.setValue(bcs[i]);
                            bcsList.add(bct);
                        }
                    }
                } else {
                    igpLink.getStatic().unsetDiffServ();
                }
            }


            //interfaces

            String newFromIf;
            if (ifFromCombo.getSelectedItem() == SetType.NOT_SET) {
                newFromIf = null;
            } else {
                newFromIf = ((NodeInterface)ifFromCombo.getSelectedItem()).getId();
            }

            String newToIf;
            if (ifToCombo.getSelectedItem() == SetType.NOT_SET) {
                newToIf = null;
            } else {
                newToIf = ((NodeInterface)ifToCombo.getSelectedItem()).getId();
            }

            if (isInterfaceCompatible(newFromIf, newToIf)) {
                if (newFromIf == null)
                    link.getFrom().unsetIf();
                else
                    link.getFrom().setIf(newFromIf);
                if (newToIf == null)
                    link.getTo().unsetIf();
                else
                    link.getTo().setIf(newToIf);
            } else {
                JOptionPane.showMessageDialog(LinkPropertiesDialog.this, "Interfaces can't be selected for this link", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            postProcessingOnSuccess();

            dispose();

        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = ex.getMessage();
            if (msg == null)
                JOptionPane.showMessageDialog(LinkPropertiesDialog.this, ex.getClass().getSimpleName(), "Error", JOptionPane.ERROR_MESSAGE);
            else
                JOptionPane.showMessageDialog(LinkPropertiesDialog.this, ex.getClass().getSimpleName() + ": " + msg, "Error", JOptionPane.ERROR_MESSAGE);
            initValues();
            return;
        }
    }


    private boolean isSourceInterfaceSelectable(String sourceIf) {
        if (sourceIf == null)
            return true;

        Link[] fromLinks = ifFromLinks.get(sourceIf);

        if (fromLinks == null)
            return true;

        if (fromLinks[0] != null && fromLinks[0] != link)
            return false;

        return true;
    }

    private boolean isInterfaceCompatible(String sourceIf, String destIf) {
        if (sourceIf == null && destIf == null)
            return true;

        Link[] fromLinks = ifFromLinks.get(sourceIf);
        Link[] toLinks = ifToLinks.get(destIf);

        if (fromLinks == null && toLinks == null)
            return true;
        if (fromLinks == null ^ toLinks == null)
            return false;

        if (fromLinks[0] != null && fromLinks[0] != link)
            return false;
        if (toLinks[1] != null && toLinks[1] != link)
            return false;

        return fromLinks[1] == toLinks[0];
    }

    /**
     * This method is executed on success just before the dialog diappears.
     */
    protected void postProcessingOnSuccess() {}

    private class InterfacesPanel extends JPanel {
        public InterfacesPanel() {
            super();
            setupUI();
        }

        private void setupUI() {
            setBorder(BorderFactory.createTitledBorder("Interfaces"));
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 5, 5, 5);

            add(new JLabel("From"), c);
            c.gridy++;
            add(ifFromCombo, c);
            c.gridy++;
            add(new JLabel("To"), c);
            c.gridy++;
            add(ifToCombo, c);
        }
    }

    /**
     * Panel for the IGP's parameters
     *
     * @author Georges Nimubona
     *
     */
    private class IGPPanel extends JPanel {
        private IGPPanel() {
            super();
            setupIgpUI();
        }

        private void setupIgpUI() {
            setBorder(BorderFactory.createTitledBorder("IGP configuration"));
            setLayout(new GridLayout(1, 1));
            JPanel staticPanel = new JPanel(new GridBagLayout());
            staticPanel.setBorder(BorderFactory.createTitledBorder("Static configuration"));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 2, 2, 2);
            staticPanel.add(new JLabel("Metric"), c);
            c.gridy++;
            staticPanel.add(metricField, c);
            c.gridy++;
            staticPanel.add(new JLabel("TE-Metric"), c);
            c.gridy++;
            staticPanel.add(teMetricField, c);
            c.gridy++;
            staticPanel.add(new JLabel("Maximum reservable bandwidth"), c);
            c.gridy++;
            c.gridwidth = 1;
            staticPanel.add(mrbwField, c);
            c.weightx = 0.0;
            c.gridx++;
            staticPanel.add(new JLabel(bwUnit), c);
            c.gridx = 0;
            c.gridwidth = 2;
            c.weightx = 1.0;
            c.gridy++;
            //staticPanel.add(new JLabel("Admin Group"), c);
            //c.gridy++;
            //staticPanel.add(adminField = new JTextField(), c);
            boolean hasDiffServ = false;
            for (int ct = 0; ct < 8; ct++) {
                if (bwcs[ct] != null) {
                    hasDiffServ = true;
                    break;
                }
            }
            if (hasDiffServ) staticPanel.add(buildDiffPanel(), c);
            add(staticPanel);
        }

        private JPanel buildDiffPanel() {
            JPanel pane = new JPanel(new GridLayout(1, 1));
            pane.setBorder(BorderFactory.createTitledBorder("Diff-Serv"));
            JPanel bcPanel = new JPanel(new GridBagLayout());
            bcPanel.setBorder(BorderFactory.createTitledBorder("Bandwidth Constraints"));
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            //c.ipadx = 100;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 0.0;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 5, 5, 5);
            bcPanel.add(new JLabel("CT Id"), c);
            c.gridx++;
            c.weightx = 1.0;
            bcPanel.add(new JLabel("Reservable bandwidth"), c);
            c.gridx = 0;
            c.gridy++;
            //c.fill = GridBagConstraints.NONE;
            c.gridwidth = 1;
            c.weightx = 0.0;

            for (int ct = 0; ct < 8; ct++) {
                if (bwcs[ct] != null) {
                    bcPanel.add(new JLabel("ClassType " + ct), c);
                    c.gridx++;
                    bcPanel.add(bwcs[ct], c);
                    c.gridx = 0;
                    c.gridy++;
                }
            }
            pane.add(bcPanel);
            return pane;
        }
    }
}

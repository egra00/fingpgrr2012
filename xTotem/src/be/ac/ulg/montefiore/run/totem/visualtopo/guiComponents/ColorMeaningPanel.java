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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents;

import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManagerListener;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputerListener;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidLinkLoadComputerException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManagerListener;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.*;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import java.util.HashMap;
import java.util.Enumeration;
import java.util.Set;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 24-Oct-2006: add reservation per class type, add getCurrentLegend() method (GMO)
* - 22-Nov-2006: Total Reservation is shown by default (GMO)
* - 08-Jun-2007: Add close for load elements (GMO)
* - 13-Aug-2007: Select the item corresponding to the default link load computer when panel is rebuilt (GMO)
* - 20-Aug-2007: Set default LLC if selection is changed (GMO)
* - 28-Feb-2008: Add an icon to show and update the calculated load (GMO)
*/

/**
* Intended to choose what the colors of the links represents on the graph.
* You can choose among LinkStatus, Reservation for each class type of the domain, and any of the calculated load in
* LinkLoadManager for the represented domain.
* @todo TODO: initialisation problem : get coherent with the graph aspect functions
* @todo TODO: --> global default scheme
*
* <p>Creation date: 15 mars 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ColorMeaningPanel extends JPanel implements LinkLoadComputerManagerListener, InterDomainManagerListener {

    private static final Logger logger = Logger.getLogger(ColorMeaningPanel.class);

    private static Icon uptoDateIcon;
    private static Icon notUptoDateIcon;

    static {
        try {
            uptoDateIcon = new ImageIcon(ColorMeaningPanel.class.getResource("/resources/img/stock_calc-accept.gif"));
            notUptoDateIcon = new ImageIcon(ColorMeaningPanel.class.getResource("/resources/img/stock_calc-cancel.gif"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Pair<LinkColorShower, ColorLegend>> loadElements;
    private HashMap<String, Pair<LinkColorShower, ColorLegend>> resvElements;
    private HashMap<String, Pair<LinkColorShower, ColorLegend>> baseElements;
    private ButtonGroup group = null;
    private ChoseColorMeaningListener cc = null;
    private CloseLinkLoadComputerListener cllc = null;

    private ColorLegend loadLegend = null;

    private Domain domain;
    //private HashMap<String, LLCButton> llcToButton;
    private HashMap<String, LLCLabel> llcToLabel;


    public ColorMeaningPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(), "Links colors shows :"));

        //llcToButton = new HashMap<String, LLCButton>();
        llcToLabel = new HashMap<String, LLCLabel>();

        cc = new ChoseColorMeaningListener();
        cllc = new CloseLinkLoadComputerListener();

        loadLegend = new DefaultLinkLoadLegend();

        loadElements = new HashMap<String, Pair<LinkColorShower, ColorLegend>>();
        resvElements = new HashMap<String, Pair<LinkColorShower, ColorLegend>>();

        baseElements = new HashMap<String, Pair<LinkColorShower, ColorLegend>>();
        group = new ButtonGroup();

        Pair<LinkColorShower, ColorLegend> p = new Pair<LinkColorShower, ColorLegend>(new TotalReservedBandwidthColorShower(), loadLegend);
        baseElements.put("Total Reservation", p);
        baseElements.put("Link status", new Pair<LinkColorShower, ColorLegend>(new UpDownLinkColorShower(), new UpDownLinkLegend()));
        GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0);
        JRadioButton rb = createRadioButton("Link status");
        add(rb, c);
        c.gridy++;
        rb = createRadioButton("Total Reservation", true);
        add(rb, c);
        c.gridy++;

        GraphAspectFunctions.setColorLegend(p.getSecond());
        GraphAspectFunctions.setColorShower(p.getFirst());

        LinkLoadComputerManager.getInstance().addListener(this);
        InterDomainManager.getInstance().addListener(this);

        revalidate();

    }

    /**
     * rebuilds the panel based on what is in baseElements and getting loadElements from LinkLoadManager
     */
    private void rebuild() {
        /*
        for (LLCButton btn : llcToButton.values()) {
            btn.destroy();
        }
        llcToButton.clear();
        */

        for (LLCLabel btn : llcToLabel.values()) {
            btn.destroy();
        }
        llcToLabel.clear();

        GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0);

        removeAll();
        group = new ButtonGroup();

        domain = GUIManager.getInstance().getCurrentDomain();
        /* rebuild load elements */
        loadElements.clear();
        if (domain != null) {
            Set<Pair<String, LinkLoadComputer>> computers = LinkLoadComputerManager.getInstance().getLinkLoadComputersWithId(domain);
            if (computers.size() > 0) {
                for (Pair<String, LinkLoadComputer> pair : computers) {
                    LinkLoadComputer llc = pair.getSecond();
                    String id = pair.getFirst();
                    loadElements.put(id, new Pair<LinkColorShower, ColorLegend>(new LoadColorShower(llc.getData()), loadLegend));

                    //JButton btn = createComputeButton(id, llc);
                    JLabel btn = createComputeLabel(id, llc);
                    c.weightx = 0.0;
                    add(btn, c);
                    c.gridx++;
                    c.weightx = 1.0;
                    String displayName = id + ": " + llc.getShortName();
                    JRadioButton rb = createRadioButton(displayName, llc.toString(), id, false);
                    add(rb, c);
                    c.gridx = 0;
                    c.gridy++;
                }
            }
        }

        c.gridx = 1;
        resvElements.clear();
        if (domain != null && domain.getAllCTId() != null) {
            for (int CT : domain.getAllCTId()) {
                String command = "Reservation at CT " + CT;
                resvElements.put(command, new Pair<LinkColorShower, ColorLegend>(new ReservedBandwidthColorShower(CT), loadLegend));
                JRadioButton rb = createRadioButton(command);
                add(rb, c);
                c.gridy++;
            }
        }

        /* add other elements buttons */
        for (String s : baseElements.keySet()) {
            JRadioButton rb = createRadioButton(s);
            add(rb, c);
            c.gridy++;
        }

        // select one element
        select();

        //update graph according to selection
        selectionChanged();
        //revalidate();
    }

    /**
     * Add an element to the panel.
     * @param name Name identifying the element, it will be displayed next to the button.
     * @param shower
     * @param legend
     */
    public void addElement(String name, LinkColorShower shower, ColorLegend legend) {
        baseElements.put(name, new Pair<LinkColorShower, ColorLegend>(shower, legend));
        rebuild();
    }


    private JRadioButton createRadioButton(String name) {
        return createRadioButton(name, name, false);
    }

    private JRadioButton createRadioButton(String name, boolean selected) {
        return createRadioButton(name, name, selected);
    }

    /**
     * Add a new button to the button group. The name will be displayed next to the button, the command should be unique
     * and will be displayed as tooltip.
     * @param shortName
     * @param name
     * @param selected
     */
    private JRadioButton createRadioButton(String shortName, String name, boolean selected) {
        return createRadioButton(shortName, name, name, selected);
    }


    private JLabel createComputeLabel(String id, final LinkLoadComputer llc) {
        LLCLabel btn = new LLCLabel(llc);

        llcToLabel.put(id, btn);

        return btn;
    }

    /*
    private JButton createComputeButton(String id, final LinkLoadComputer llc) {
        LLCButton btn = new LLCButton(llc);

        llcToButton.put(id, btn);

        return btn;
    }
    */

    private JRadioButton createRadioButton(String shortName, String name, final String commandName, boolean selected) {
        JRadioButton btn = new JRadioButton(shortName);

        btn.setActionCommand(commandName);
        btn.addActionListener(cc);
        btn.setToolTipText(name);
        btn.setMultiClickThreshhold(500);
        btn.addMouseListener(cllc);
        group.add(btn);
        if (selected) btn.setSelected(true);
        return btn;
    }

    /**
     * remove listeners
     */
    public void destroy() {
        LinkLoadComputerManager.getInstance().removeListener(this);
        InterDomainManager.getInstance().removeListener(this);
        /*
        for (LLCButton btn : llcToButton.values()) {
            btn.destroy();
        }
        llcToButton.clear();
        */
        for (LLCLabel btn : llcToLabel.values()) {
            btn.destroy();
        }
        llcToLabel.clear();

    }

    public void addLinkLoadComputerEvent(LinkLoadComputer llc) {
        if (llc.getDomain() == GUIManager.getInstance().getCurrentDomain()) {
            rebuild();
        }
    }

    public void removeLinkLoadComputerEvent(LinkLoadComputer llc) {
        if (llc.getDomain() == GUIManager.getInstance().getCurrentDomain()) {
            rebuild();
        }
    }

    public void removeMultipleLinkLoadComputerEvent() {
        rebuild();
    }

    public void changeDefaultLinkLoadComputerEvent(int asId, LinkLoadComputer llc) {
        if (select())
            selectionChanged();
    }

    public void addDomainEvent(Domain domain) {
    }

    public void removeDomainEvent(Domain domain) {
    }

    public void changeDefaultDomainEvent(Domain domain) {
        rebuild();
    }

    /**
     * return true if selection changed, false otherwise
     * @return
     */
    private boolean select() {
        if (group.getButtonCount() > 0) {
            String toSelect = null;

            /* Select the default llc */
            if (domain  != null) {
                try {
                    toSelect = LinkLoadComputerManager.getInstance().getDefaultLinkLoadComputerId(domain);
                } catch (InvalidLinkLoadComputerException e) {}
            }

            /* if no llc, select total reservation*/
            if (toSelect == null) {
                toSelect = "Total Reservation";
            } /* if already selected */
            else if (group.getSelection() != null && toSelect.equals(group.getSelection().getActionCommand()))
                return false;

            Enumeration<AbstractButton> en = group.getElements();
            //Select reservation as it is the default one used in GUI (implicit)
            while (en.hasMoreElements()) {
                AbstractButton btn = en.nextElement();
                if (btn.getActionCommand().equals(toSelect)) {
                    btn.setSelected(true);
                    //selectionChanged();
                    logger.debug(toSelect + " selected.");
                    break;
                }
            }
        }
        return true;
    }
    /**
     * Do the action when a selection is made: install the legend and the colorShower to the graph and update the
     * legend panel.
     */
    private void selectionChanged() {
        if (group.getSelection() == null) {
            logger.info("No button selected.");
            return;
        }
        String name = group.getSelection().getActionCommand();
        ColorLegend cl;
        LinkColorShower cs;
        Pair<LinkColorShower, ColorLegend> p;
        if ((p = loadElements.get(name)) == null) {
            if ((p = resvElements.get(name)) == null) {
                p = baseElements.get(name);
            }
        } else {
            try {
                LinkLoadComputerManager.getInstance().setDefaultLinkLoadComputer(domain, name);
            } catch (InvalidLinkLoadComputerException e) {
                logger.error("LLC not found with id: " + name);
                e.printStackTrace();
            }
        }
        cl = p.getSecond();
        cs = p.getFirst();

        GraphAspectFunctions.setColorLegend(cl);
        GraphAspectFunctions.setColorShower(cs);
        MainWindow.getInstance().getOptionsPanel().getLegendPanel().setLegend(cl);
    }

    public ColorLegend getCurrentLegend() {
        String name = group.getSelection().getActionCommand();
        Pair<LinkColorShower, ColorLegend> p;
        if ((p = loadElements.get(name)) == null) {
            if ((p = resvElements.get(name)) == null) {
                p = baseElements.get(name);
            }
        }
        //if (p == null) return null;
        return p.getSecond();
    }

    private class LLCLabel extends JLabel implements LinkLoadComputerListener {
        private final LinkLoadComputer llc;

        public LLCLabel(LinkLoadComputer llc) {
            super();
            this.llc = llc;
            llc.addListener(this);
            setToolTipText("Click to update values");
            setIcon(llc.isUpToDate() ? uptoDateIcon : notUptoDateIcon);
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    LLCLabel.this.llc.update();
                    GraphManager.getInstance().repaint();
                }

                public void mouseEntered(MouseEvent e) {
                    setLocation(getLocation().x+1, getLocation().y+1);
                }

                public void mouseExited(MouseEvent e) {
                    setLocation(getLocation().x-1, getLocation().y-1);
                }
            });
        }

        public void destroy() {
            llc.removeListener(this);
        }

        public void validityChangeEvent(LinkLoadComputer llc) {
            setIcon(llc.isUpToDate() ? uptoDateIcon : notUptoDateIcon);
        }
    }

    private class LLCButton extends JButton implements LinkLoadComputerListener {
        private final LinkLoadComputer llc;

        public LLCButton(LinkLoadComputer llc) {
            super();
            this.llc = llc;
            llc.addListener(this);
            setIcon(llc.isUpToDate() ? uptoDateIcon : notUptoDateIcon);
            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!LLCButton.this.llc.isUpToDate()) {
                        LLCButton.this.llc.update();
                        GraphManager.getInstance().repaint();
                    }
                }});
        }

        public void destroy() {
            llc.removeListener(this);
        }

        public void validityChangeEvent(LinkLoadComputer llc) {
            setIcon(llc.isUpToDate() ? uptoDateIcon : notUptoDateIcon);
        }

        public Dimension getSize() {
            if (getIcon() != null)
                return new Dimension(getIcon().getIconWidth(), getIcon().getIconHeight());
            else return super.getSize();
        }

        public Dimension getMaximumSize() {
            return getSize();
        }

        public Dimension getMinimumSize() {
            return getSize();
        }

    }

    private class ChoseColorMeaningListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            selectionChanged();
        }
    }

    private class CloseLinkLoadComputerListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                Object o = e.getSource();
                if (o instanceof JRadioButton) {
                    JRadioButton btn = ((JRadioButton)o);
                    final String command = btn.getActionCommand();
                    if (loadElements.get(command) != null) {
                        JPopupMenu popup = new JPopupMenu();
                        JMenuItem menuItem =  new JMenuItem("close");
                        menuItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                LinkLoadComputerManager.getInstance().removeLinkLoadComputer(domain, command);
                            }
                        });
                        popup.add(menuItem);
                        popup.show(btn, e.getX(), e.getY());
                    }
                }
            }
        }
    }
}

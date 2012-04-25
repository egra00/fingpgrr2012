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

import javax.swing.*;
import javax.swing.text.Document;
import java.io.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Date;

/*
* Changes:
* --------
*
*/

/**
* A class that is responsible for displaying a text file in a panel.
*
* <p>Creation date: 9 nov. 2005
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ReportPanel extends JPanel implements ActionListener {
    protected JLabel title = null;
    protected JTextArea contentText = null;
    protected JScrollPane scroll = null;
    protected JPanel buttonPanel = null;
    protected JButton close = null;
    protected JButton saveAs = null;

    public ReportPanel() {
        initComponents();
    }

    public ReportPanel(String title) {
        initComponents();
        this.title = new JLabel(title);
    }

    public ReportPanel(String title, String fileName) {
        initComponents();
        this.title.setText(title);
        loadFile(fileName);
    }

    private void initComponents() {
        this.setLayout(new BorderLayout());

        title = new JLabel();
        title.setFont(new Font("Tahoma", Font.BOLD, 14));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(title, BorderLayout.NORTH);

        contentText = new JTextArea();
        contentText.setLineWrap(true);
        contentText.setEditable(false);

        scroll = new JScrollPane(contentText);
        this.add(scroll, BorderLayout.CENTER);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        this.add(buttonPanel, BorderLayout.SOUTH);

        saveAs = new JButton("Save as ...");
        saveAs.addActionListener(this);
        buttonPanel.add(saveAs);

        close = new JButton("Close");
        close.addActionListener(this);
        buttonPanel.add(close);

    }

    public void loadFile(String fileName) {
        contentText.setText("");
        try {
            File file = new File(fileName);
            Date date = new Date(file.lastModified());
            title.setText(title.getText() + " (" + date.toString() + ")");
            BufferedReader buf = new BufferedReader(new FileReader(fileName));
            String str;
            while ((str = buf.readLine()) != null) {
                contentText.append(str);
                contentText.append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            contentText.setText("Error while reading file: " + fileName);
        }
    }

    public void saveFile(String fileName) {
        File file = new File(fileName);
        saveFile(file);
    }

    private void saveFile(File file) {
        String text = contentText.getText();

        try {
            FileWriter fw = new FileWriter(file);
            fw.write(text);
            fw.close();
            JOptionPane.showMessageDialog(this, "File saved: " + file.getName(), "Save File", JOptionPane.INFORMATION_MESSAGE);            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Impossible to save the file: " + file.getName(), "Save File", JOptionPane.ERROR_MESSAGE);
        }
    }


    public void setTitle(String title) {
        this.title = new JLabel(title);
    }

    public String getTitle() {
        return (title == null) ? "" : title.getText();
    }

    public void setText(String text) {
        contentText.setText(text);
    }

    public String getText() {
        return contentText.getText();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveAs) {
            File file;

            JFileChooser fc = new JFileChooser();
            int choice = fc.showSaveDialog(this);
            if (choice == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                saveFile(file);
            }

        }
        else if (e.getSource() == close) {
            ((JDialog)this.getRootPane().getParent()).dispose();
        }
    }
}

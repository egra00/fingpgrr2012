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
import java.io.OutputStream;
import java.io.IOException;

/*
* Changes:
* --------
*
*
*/

/**
 * This class use a textArea as an OutputStream.
 * <p/>
 * <p>Creation date: 4 janv. 2006
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class TextAreaOutputStream extends OutputStream {
    private JTextArea out = null;
    private int pos = 0;
    private final static int BUF_SIZE = 256;
    private byte[] buf = new byte[BUF_SIZE];

    public TextAreaOutputStream(JTextArea out) {
        this.out = out;
    }

    public void write(int b) throws IOException {
        buf[pos++] = (byte) b;
        if (pos == BUF_SIZE || buf[pos-1] == '\n') {
            final String str = new String(buf, 0, pos);
            pos = 0;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (out != null) out.append(str);
                }
            });
        }
    }

    public JTextArea getTextArea() {
        return out;
    }
}

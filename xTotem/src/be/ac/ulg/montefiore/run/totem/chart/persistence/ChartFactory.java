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
package be.ac.ulg.montefiore.run.totem.chart.persistence;

import be.ac.ulg.montefiore.run.totem.chart.model.Chart;
import org.jfree.chart.ChartUtilities;
import org.apache.log4j.Logger;
import org.jibble.epsgraphics.EpsGraphics2D;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
*
*
* <p>Creation date: 21 d�c. 2005
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ChartFactory {

    public static final int PNG_FORMAT = 0;
    public static final int JPEG_FORMAT = 1;
    public static final int EPS_FORMAT = 2;

    private static final Logger logger = Logger.getLogger(ChartFactory.class);

    /**
     * Save a given Chart object to a file.
     * @param fileName Name of the file
     * @param chart Chart to save
     * @param format File format (one of the constants PNG_FORMAT, JPEG_FORMAT or EPS_FORMAT)
     * @param width Width of the generated image
     * @param height Height of the generated image
     */
    public static void saveChart(String fileName, Chart chart, int format, int width, int height) {
        try {
            File file = new File(fileName);
            switch (format) {
                case PNG_FORMAT:
                    ChartUtilities.saveChartAsPNG(file, chart.getPlot(), width, height);
                    logger.info("Chart saved as: \"" + file.getAbsolutePath() + "\" in PNG format");
                    break;
                case JPEG_FORMAT:
                    ChartUtilities.saveChartAsJPEG(file, chart.getPlot(), width, height);
                    logger.info("Chart saved as: \"" + file.getAbsolutePath() + "\" in JPEG format");
                    break;
                case EPS_FORMAT:
                    saveAsEPS(file, chart, width, height);
                    logger.info("Chart saved as: \"" + file.getAbsolutePath() + "\" in EPS format");
                    break;
                default:
                    logger.error("Unknown Format, chart not saved.");
            }
        } catch (IOException e) {
            logger.error("Impossible to save the file \"" + fileName + "\" : " + e.getMessage());
        }
    }

    private static void saveAsEPS(File file, Chart chart, int width, int height) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        EpsGraphics2D g = new EpsGraphics2D("Chart - created with TOTEM", out, 0, 0, width, height);
        Rectangle2D r = new Rectangle2D.Double(0, 0, width, height);
        chart.getPlot().draw(g, r);

        g.flush();
        g.close();
    }
}



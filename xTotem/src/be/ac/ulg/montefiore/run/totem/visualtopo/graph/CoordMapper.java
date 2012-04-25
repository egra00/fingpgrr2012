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
package be.ac.ulg.montefiore.run.totem.visualtopo.graph;

import edu.uci.ics.jung.visualization.Coordinates;
import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 18-Oct-2007: Can keep proportion (GMO)
*/

/**
* Map coordinates from one rectangle to another, by keeping aspect ratio or not.
* Rectangles are specified by a corner coordinate and width and length values.
*
* <p>Creation date: 28 juin 2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class CoordMapper {
    private final static Logger logger = Logger.getLogger(CoordMapper.class);

    private boolean keepProportion = true;

    private double srcRangeX;
    private double srcMinX;
    private double srcRangeY;
    private double srcMinY;

    private double dstRangeX;
    private double dstMinX;
    private double dstRangeY;
    private double dstMinY;

    private double ratio;

    public CoordMapper(double srcRangeX, double srcMinX, double srcRangeY, double srcMinY, double dstRangeX, double dstMinX, double dstRangeY, double dstMinY) {
        this.srcRangeX = srcRangeX;
        this.srcMinX = srcMinX;
        this.srcRangeY = srcRangeY;
        this.srcMinY = srcMinY;

        this.dstRangeX = dstRangeX;
        this.dstMinX = dstMinX;
        this.dstRangeY = dstRangeY;
        this.dstMinY = dstMinY;

        logger.debug(new StringBuffer().append("CoordMapper constructor with srcRangeX:").append(srcRangeX).append(" srcMinX:").append(srcMinX).append(" srcRangeY:").append(srcRangeY).append(" srcMinY:").append(srcMinY).append(" dstRangeX:").append(dstRangeX).append(" dstMinX:").append(dstMinX).append(" dstRangeY:").append(dstRangeY).append(" dstMinY:").append(dstMinY).toString());
        ratio = Math.max(srcRangeX / dstRangeX, srcRangeY / dstRangeY);
    }

    public Coordinates map(double srcX, double srcY) {
        double x;
        double y;
        if (keepProportion) {
            x = ((srcX - srcMinX) * (1/ratio)) + dstMinX;
            y = ((srcY - srcMinY) * (1/ratio)) + dstMinY;
        } else {
            x = ((srcX - srcMinX) / srcRangeX * dstRangeX) + dstMinX;
            y = ((srcY - srcMinY) / srcRangeY * dstRangeY) + dstMinY;
        }
        return new Coordinates(x, y);
    }

    public Coordinates unmap(double dstX, double dstY) {
        double x;
        double y;
        if (keepProportion) {
            x = ((dstX - dstMinX) * ratio) + srcMinX;
            y = ((dstY - dstMinY) * ratio) + srcMinY;
        } else {
            x = ((dstX - dstMinX) / dstRangeX * srcRangeX) + srcMinX;
            y = ((dstY - dstMinY) / dstRangeY * srcRangeY) + srcMinY;
        }
        return new Coordinates(x, y);
    }

    public double getDstMinX() {
        return dstMinX;
    }

    public double getDstMinY() {
        return dstMinY;
    }

    public double getDstRangeX() {
        return dstRangeX;
    }

    public double getDstRangeY() {
        return dstRangeY;
    }

    public double getSrcMinX() {
        return srcMinX;
    }

    public double getSrcMinY() {
        return srcMinY;
    }

    public double getSrcRangeX() {
        return srcRangeX;
    }

    public double getSrcRangeY() {
        return srcRangeY;
    }

    public boolean isKeepProportion() {
        return keepProportion;
    }

    public void setKeepProportion(boolean keepProportion) {
        this.keepProportion = keepProportion;
    }
}

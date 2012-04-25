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
package be.ac.ulg.montefiore.run.totem.core;

import org.apache.log4j.Logger;

import java.util.prefs.Preferences;
import java.util.prefs.InvalidPreferencesFormatException;
import java.io.*;
import java.net.URL;

/*
 * Changes:
 * --------
 * 3-Feb-05 : Read preferences.xml from the jar if not defined by a user file (FS)
 *
 */

/**
 * This class is a singleton that provides a global access point to the 
 * preferences.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public final class PreferenceManager {

    private static Logger logger = Logger.getLogger(PreferenceManager.class.getName());
    private static PreferenceManager instance = new PreferenceManager();
    private Preferences prefs;
    private String preferenceFile = "preferences.xml";
    private String jarPreferenceFile = "/resources/preferences.xml";


    public static PreferenceManager getInstance() {
        return instance;
    }

    private PreferenceManager() {
        // Retrieve the user preference node for the package java.lang
        prefs = Preferences.userNodeForPackage(String.class);

        // First try to read preference from preferenceFile
        InputStream is = null;
        File file = new File(preferenceFile);
        if (file.exists()) {
            try {
                is = new BufferedInputStream(new FileInputStream("preferences.xml"));
            } catch (FileNotFoundException e1) {
                logger.error("Could not read preference file : " + preferenceFile);
                e1.printStackTrace();
            }
        } else {
            // Next try to read the preference from JAR
            URL url = getClass().getResource(jarPreferenceFile);
            if (url == null) {
                logger.error("Preference file : " + jarPreferenceFile + " not found in the JAR");
                System.exit(0);
            } else {
                try {
                    is = new BufferedInputStream(url.openStream());
                } catch (IOException e) {
                    logger.error("Could not read JAR preference file : " + jarPreferenceFile);
                    e.printStackTrace();
                }
            }
        }

        // Import preference data
        try {
            Preferences.importPreferences(is);
        } catch (InvalidPreferencesFormatException e) {
            try{
                is = new BufferedInputStream(getClass().getResourceAsStream(jarPreferenceFile));
                Preferences.importPreferences(is);
            }
            catch (FileNotFoundException f){
                logger.fatal("Cound not find default preference file : " + jarPreferenceFile);
            }
            catch (InvalidPreferencesFormatException f){
                logger.fatal("Problem in default preference file format");
            }
            catch (IOException f){
                logger.fatal("I/O exception when trying to import preferences from default preference file");
            }
        } catch (IOException e) {
            logger.fatal("I/O exception when trying to import preferences from current preference file");
        }
    }


    public Preferences getPrefs() {
        return prefs;
    }

}

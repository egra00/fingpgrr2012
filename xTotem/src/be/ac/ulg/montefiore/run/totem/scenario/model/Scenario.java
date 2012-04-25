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
package be.ac.ulg.montefiore.run.totem.scenario.model;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.ScenarioImpl;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.ScenarioTypeImpl;
import be.ac.ulg.montefiore.run.totem.util.jaxb.runtime.UnmarshallingContext;
import be.ac.ulg.montefiore.run.totem.util.jaxb.runtime.UnmarshallingEventHandler;

/*
 * Changes:
 * --------
 * - 09-Jan-2007: add scenario path and pathsRelativeTo attribute management (GMO)
 * - 25-Feb-2008: fix bug with pathsRelativeTo: when present, classes where not instantiated correctly (GMO)
 */

/**
 * This class extends <code>ScenarioImpl</code> to enable the use of
 * "distributed scenario schemas".
 * <p/>
 * If attributes are changed in the schema, the <code>Unmarshaller.enterElement(.)</code> method must be changed manually.
 *
 * <p>Creation date: 03-mars-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class Scenario extends ScenarioImpl {

    private static final Logger logger = Logger.getLogger(Scenario.class);

    // the paths in the events should be interpreted relative to this directory
    private String scenarioPath;

    /**
     * Gets the absolute directory from which the relative paths (in the events) should be interpreted.
     * @return
     */
    public String getScenarioPath() {
        return scenarioPath;
    }

    /**
     * Sets the absolute directory from which the relative paths (in the events) should be interpreted.<br>
     * The scenario path is set on the scenario loading and depends on the absolute filename of the scenario and on
     * the attribute <code>pathsRelativeTo</code> of the scenario root element.
     * @param scenarioPath
     */
    public void setScenarioPath(String scenarioPath) {
        this.scenarioPath = scenarioPath;
    }

    /**
     * Shortcut for <code>getEvent().add(.)</code>
     * @param event
     */
    public void addEvent(Event event) {
        getEvent().add(event);
    }

    public UnmarshallingEventHandler createUnmarshaller(UnmarshallingContext context) {
        return new Unmarshaller(context);
    }
    
    public class Unmarshaller extends ScenarioImpl.Unmarshaller {
        public Unmarshaller(UnmarshallingContext context) {
            super(context);
        }

        public void enterElement(String uri, String local, String qname, Attributes atts) throws SAXException {
            // This must be checked when changing JAXB version!
            
            while (true) {
                switch (state) {
                case  1 :

                    // WARNING: this must be changed if the scenario attributes are changed in the schema.
                    int attIdx = context.getAttribute("", "pathsRelativeTo");
                    if (attIdx >= 0) {
                        context.consumeAttribute(attIdx);
                        context.getCurrentHandler().enterElement(uri, local, qname, atts);
                        return ;
                    }

                    // Try to find the event using the namespace...
                    String[] tokens = uri.substring(7).split("\\."); // remove "http://" and split using "." as separator
                    StringBuffer buf = new StringBuffer();
                    // Reverse the namespace to obtain the package name (and not include "jaxb").
                    for(int i = tokens.length-1; i >= 1; --i) {
                        buf.append(tokens[i]);
                        buf.append(".");
                    }
                    String packageName = buf.toString();
                    tokens = null;
                    buf = null;
                    
                    // If the first letter of the event is lower-case, convert it to a upper-case letter.
                    String className = local;
                    if(Character.isLowerCase(className.charAt(0))) {
                        className = Character.toUpperCase(className.charAt(0)) + className.substring(1);
                    }
                    
                    try {
                        Class clazz = Thread.currentThread().getContextClassLoader().loadClass(packageName+className);
                        spawnHandlerFromEnterElement(new UnmarshallerType(context), 2, uri, local, qname, atts);
                        return;
                    }
                    catch(ClassNotFoundException e) {
                        // Event not found... Let JAXB throw his exception!
                    }
                    break;
                case  0 :
                    if (("scenario" == local)&&("http://jaxb.model.scenario.totem.run.montefiore.ulg.ac.be" == uri)) {
                        context.pushAttributes(atts, false);
                        state = 1;
                        return ;
                    }
                    break;
                case  3 :
                    revertToParentFromEnterElement(uri, local, qname, atts);
                    return ;
                }
                
                // Event not found, report the error
                unexpectedEnterElement(uri,local,qname,atts);
                break;
            }
        }

        public void enterAttribute(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            while (true) {
                switch (state) {
                    case  1 :
                        // WARNING: this must be changed if the scenario attributes are changed in the schema.
                        if (("pathsRelativeTo" == ___local)&&("" == ___uri)) {
                            spawnHandlerFromEnterAttribute(new UnmarshallerType(context), 2, ___uri, ___local, ___qname);
                            return ;
                        }
                        break;
                    case  3 :
                        revertToParentFromEnterAttribute(___uri, ___local, ___qname);
                        return ;
                }
                super.enterAttribute(___uri, ___local, ___qname);
                break;
            }
        }
    }
    
    public class UnmarshallerType extends ScenarioTypeImpl.Unmarshaller {
        public UnmarshallerType(UnmarshallingContext context) {
            super(context);
        }
        
        public Object owner() {
            // Returns null -- this method is not called...
            return null;
        }

        public void enterElement(String uri, String local, String qname, Attributes atts) throws SAXException {
            while (true) {
                switch (state) {
                    case 3:
                        // Try to find the event using the namespace...
                        String[] tokens = uri.substring(7).split("\\."); // remove "http://" and split using "." as separator
                        StringBuffer buf = new StringBuffer();
                        // Reverse the namespace to obtain the package name (and not include "jaxb").
                        for(int i = tokens.length-1; i >= 1; --i) {
                            buf.append(tokens[i]);
                            buf.append(".");
                        }
                        String packageName = buf.toString();
                        tokens = null;
                        buf = null;

                        // If the first letter of the event is lower-case, convert it to a upper-case letter.
                        String className = local;
                        if(Character.isLowerCase(className.charAt(0))) {
                            className = Character.toUpperCase(className.charAt(0)) + className.substring(1);
                        }

                        try {
                            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(packageName+className);
                            _getEvent().add(spawnChildFromEnterElement(clazz, 4, uri, local, qname, atts));
                            return;
                        }
                        catch(ClassNotFoundException e) {
                            // Event not found... Let JAXB throw his exception!
                        }
                        break;
                    case  4 :
                        // Try to find the event using the namespace...
                        tokens = uri.substring(7).split("\\."); // remove "http://" and split using "." as separator
                        buf = new StringBuffer();
                        // Reverse the namespace to obtain the package name (and not include "jaxb").
                        for(int i = tokens.length-1; i >= 1; --i) {
                            buf.append(tokens[i]);
                            buf.append(".");
                        }
                        packageName = buf.toString();
                        tokens = null;
                        buf = null;
                        
                        // If the first letter of the event is lower-case, convert it to a upper-case letter.
                        className = local;
                        if(Character.isLowerCase(className.charAt(0))) {
                            className = Character.toUpperCase(className.charAt(0)) + className.substring(1);
                        }
                        
                        try {
                            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(packageName+className);
                            _getEvent().add(spawnChildFromEnterElement(clazz, 4, uri, local, qname, atts));
                            return;
                        }
                        catch(ClassNotFoundException e) {
                            // Event not found... Let JAXB throw his exception!
                        }
                        revertToParentFromEnterElement(uri, local, qname, atts);
                        return;
                }
                super.enterElement(uri, local, qname, atts);
                break;
            }
        }
    }
}

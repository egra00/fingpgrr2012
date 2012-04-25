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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.scenario;

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.EventType;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;

import com.sun.xml.bind.JAXBObject;

/*
* Changes:
* --------
*
*
*/

/**
* Class representing a generic node in a ScenarioJTree.
*
* <p>Creation date: 6 janv. 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*
* //TODO: make this one abstract and subclass it.
*/

public class EventTypeNode {

    //associate a list of parameters name and type to a class of event
    private static HashMap<Class, List<Pair<String, Class>>> globalParamsList = null;

    private String name = null;
    private JAXBObject parent = null;
    private Class type = null;
    boolean useNotSet = false;

    /**
     *
     * @param parent the parent object
     * @param name the parameter name to get from the parent
     * @param type the class of the value of the node
     */
    public EventTypeNode(JAXBObject parent, String name, Class type) {
        this.parent = parent;
        this.name = name;
        this.type = type;
    }

    /**
     *
     * @param parent the parent object
     * @param name the parameter name to get from the parent
     * @param type the class of the value of the node
     * @param useNotSet set to retrieve also the child that correspond to parameters that are not set
     */
    public EventTypeNode(JAXBObject parent, String name, Class type, boolean useNotSet) {
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.useNotSet = useNotSet;
    }

    /**
     * returns true if the parameter was set.
     * @return
     */
    public boolean isset() {
        boolean isset = false;
        try {
            //Method m = parent.getClass().getSuperclass().getSuperclass().getMethod("isSet" + name, (Class[])null);
            Method m = parent.getClass().getMethod("isSet" + name, (Class[])null);
            isset = (Boolean)m.invoke(parent);
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
            return false;
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
            return false;
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
            return false;
        }

        //only for debugging
        if (!isset) {
            //System.err.println("param " + name + " not set.");
        }
        return isset;

    }

    /**
     * get the value of the node. The value is the parameter value of the parent Jaxb object.
     * @return the parameter value which is an instance of the class getType() or null if the parameter was not set.
     */
    public Object getValue() {
        if (!isset()) return null;

        try {
            Method get = null;
            //System.out.println(type.getSimpleName());
            if (type.getName().equals("boolean")) {
                //get = parent.getClass().getSuperclass().getSuperclass().getMethod("is" + name, (Class[])null);
                get = parent.getClass().getMethod("is" + name, (Class[])null);
            } else {
                //get = parent.getClass().getSuperclass().getSuperclass().getMethod("get" + name, (Class[])null);
                get = parent.getClass().getMethod("get" + name, (Class[])null);
            }
            return get.invoke(parent);
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
            return null;
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
            return null;
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    /**
     * get the type of the node
     * @return
     */
    public Class getType() {
        return type;
    }

    /**
     * get the children of the nodes.
     * @return returns a list of EventTypeNode that represent the descendants in the XML tree.
     */
    public List<EventTypeNode> getChildren() {
        List<EventTypeNode> ret = new ArrayList();

        //System.out.println(type.getName());


        if (List.class.isAssignableFrom(type)) {
            List lst = (List)getValue();
            if (lst == null) return ret;
            int i = 0;
            for (Object o : lst) {
                EventTypeNode etn = new ListEventTypeNode(o, name + " " + i++);
                ret.add(etn);
            }
            return ret;
        }
        else if (type.isPrimitive() || !(JAXBObject.class.isAssignableFrom(type) || type.getPackage().getName().startsWith("be.ac.ulg.montefiore.run.totem.scenario.model.jaxb"))) {
            return ret;
        }

        //System.out.println(type.getSuperclass().getName());

        if (globalParamsList == null) {
            globalParamsList = new HashMap();
        }
        List<Pair<String, Class>> lst = globalParamsList.get(type);

        //that kind of event has not yet been polled for parameter list
        if (lst == null) {
            lst = new ArrayList<Pair<String, Class>>();

            for (Method m : type.getMethods()) {
                String methodName = m.getName();
                if (methodName.startsWith("isSet")) {
                    String param = methodName.substring(5, methodName.length());
                    try {
                        Method mm = type.getMethod("get" + param, (Class[])null);
                        lst.add(new Pair<String, Class>(param, mm.getReturnType()));
                    } catch (NoSuchMethodException e) {
                        try {
                            Method mm = type.getMethod("is" + param, (Class[])null);
                            lst.add(new Pair<String, Class>(param, mm.getReturnType()));
                        } catch (NoSuchMethodException ex) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            globalParamsList.put(type, lst);

            /*
            for (Field f : type.getSuperclass().getSuperclass().getDeclaredFields()) {
                String fieldName = f.getName();
                if (fieldName.startsWith("_")) {
                    lst.add(new Pair<String, Class>(fieldName.substring(1, fieldName.length()), f.getType()));
                }
            }
            globalParamsList.put(type, lst);
            */
        }

        if (isset()) {
            int lastSetIndex = 0;
            JAXBObject value = null;
            for (Pair<String, Class> pair : lst) {
                if (value == null) value = (JAXBObject)getValue();
                EventTypeNode etn = new EventTypeNode(value, pair.getFirst(), pair.getSecond(), useNotSet);
                if (etn.isset()) ret.add(lastSetIndex++, etn);
                else if (useNotSet) ret.add(etn);
            }
        }
        return ret;
    }

    /**
     * returns the string representation of the object
     * @return
     */
    public String displayString() {
        String s = name;
        if (isset()) {
            s += " : ";
            if (getChildren().size() <= 0) {
                Object o = getValue();
                s += o;
            }
        }

        return s;
    }

}

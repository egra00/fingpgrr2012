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
package be.ac.ulg.montefiore.run.totem.util;

import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;

/*
* Changes:
* --------
* - 23-Mar-2006: Possible values can now be null (GMO)
* - 30-Oct-2007: Add equals() and hashcode() (GMO)
* - 31-Oct-2007: Add support for Boolean values in validate() (GMO)
*
*/

/**
* Use to represent a parameter constituted of a name, description, type, a set of possible values (optional)
* and a default value.
*
* <p>Creation date: 14 nov. 2005
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ParameterDescriptor {

    private String name = null;
    private String description = null;
    private Class type = null;
    private Object defaultValue = null;
    private Object[] possibleValues = null;

    //public ParameterDescriptor() {
    //}

    public ParameterDescriptor(String name, String description, Class type, Object defaultValue) throws AlgorithmParameterException {
        this.name = name;
        this.description = description;
        this.type = type;
        if (defaultValue != null && !defaultValue.getClass().isAssignableFrom(type)) {
            throw new AlgorithmParameterException("Default value and given type correspond not for parameter " + name + ".");
        };
        this.defaultValue = defaultValue;
    }

    public ParameterDescriptor(String name, String description, Class type, Object defaultValue, Object[] possibleValues) throws AlgorithmParameterException {
        this(name, description, type, defaultValue);
        this.setPossibleValues(possibleValues);
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public Class getType() {
        return type;
    }

    private void setType(Class type) throws AlgorithmParameterException {
        if (!defaultValue.getClass().isAssignableFrom(type)) {
            throw new AlgorithmParameterException("Default value and given type correspond not.");
        };
        this.type = type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) throws AlgorithmParameterException {
        if (!defaultValue.getClass().isAssignableFrom(type)) {
            throw new AlgorithmParameterException("Default value and given type correspond not.");
        };
        if (!checkDefaultValue(defaultValue))
            throw new AlgorithmParameterException("Default value and given type correspond not.");
        this.defaultValue = defaultValue;
    }

    public boolean validate(String value) {
        Object o = null;
        try {
            if (type == Integer.class) {
                o = new Integer(Integer.parseInt(value));
            }
            else if (type == String.class) {
                o = value;
            }
            else if (type == Double.class) {
                o = new Double(Double.parseDouble(value));
            }
            else if (type == Float.class) {
            	o = new Float(Float.parseFloat(value));
            } else if (type == Boolean.class) {
                o = new Boolean(Boolean.parseBoolean(value));
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            return false;
        }
        if (possibleValues == null) return true;
        else {
            for (Object oo : possibleValues) {
                if (oo.equals(o)) return true;
            }
            return false;
        }
    }

    public Object[] getPossibleValues() {
        return possibleValues;
    }

    private void setPossibleValues(Object[] possibleValues) throws AlgorithmParameterException {
        for (Object o : possibleValues) {
            if (o != null && !o.getClass().isAssignableFrom(type)) {
                throw new AlgorithmParameterException("Given possible value and given type correspond not.");
            };
        }
        this.possibleValues = possibleValues;
    }

    private boolean checkDefaultValue(Object defaultValue) {
        if (possibleValues == null) return true;
        if (defaultValue == null) return true;
        for (Object o : possibleValues) {
            if (o.equals(defaultValue)) return true;
        }
        return false;
    }

    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Equals if has the same name
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        return name.equals(obj);
    }
}

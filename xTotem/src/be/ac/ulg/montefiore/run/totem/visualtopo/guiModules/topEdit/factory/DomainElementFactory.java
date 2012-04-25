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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory;

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.*;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.exception.AlreadyExistException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.exception.NotFoundException;

import java.util.HashMap;
import java.util.Collection;

/*
* Changes:
* --------
*
*/

/**
*
* This class is to be used to create objects of type T based on a given model. The implementation should provide the
* {@link #createInstance(java.util.HashMap<java.lang.String,java.lang.String>)} method to build a first default instance
* and the {@link #cloneObject(Object)} method to clone the T type.
*
* Model instances are identified by a name and can be added to the factory. The {@link #createObject(String, java.util.HashMap<java.lang.String,java.lang.String>)}
* method returns a clone of the given model.
*
* <p> Note: The {@link #cloneObject(Object)} method should not be abstract but implemented here as a way to clone
* JAXBObject.
*
* <p>Creation date: 3/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public abstract class DomainElementFactory<T> {

    final static ObjectFactory factory = new ObjectFactory();

    protected HashMap<String, T> defaultMap;

    private String defaultModel = "default";

    /**
     * Common operations to all constructors
     */
    protected DomainElementFactory() {
        defaultMap = new HashMap<String, T>();
    }

    /**
     * Build an instance of type T, given T specific parameters.
     * @param params
     * @return
     */
    protected abstract T createInstance(HashMap<String, String> params);

    /**
     * Add the given model instance under the given name
     * @param name
     * @param instance
     * @throws be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.exception.AlreadyExistException If an instance associated with the id already exists
     */
    public void addInstance(String name, T instance) throws AlreadyExistException {
        if (defaultMap.get(name) != null) {
            throw new AlreadyExistException();
        }
        defaultMap.put(name, instance);
    }

    /**
     * Remove the model instance identified by name.
     * @param name
     * @return
     */
    public T removeInstance(String name) {
        if (name.equals(defaultModel)) defaultModel = null;
        return defaultMap.remove(name);
    }

    public Collection<String> getAllInstanceName() {
        return defaultMap.keySet();
    }

    /**
     * Create an object by cloning the model identified by <code>model</code>.
     * @param model
     * @param params
     * @return
     * @throws NotFoundException
     */
    public T createObject(String model, HashMap<String, String> params) throws NotFoundException {
        T toClone = defaultMap.get(model);
        if (toClone == null) throw new NotFoundException(model + " not found.");
        return clone(toClone);
    }

    /**
     * Create an object by cloning the default model instance.
     * @param params
     * @return
     */
    public T createDefaultObject(HashMap<String, String> params) {
        if (defaultMap.get(defaultModel) == null)
            defaultMap.put(defaultModel, createInstance(params));
        try {
            return createObject(defaultModel, params);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * return the model instance associated with name.
     * @param name
     * @return
     */
    public T getModel(String name) {
        return defaultMap.get(name);
    }

    /**
     * returns the default model instance.
     * @return
     */
    public T getDefaultModel() {
        return defaultMap.get(defaultModel);
    }

    public void setDefaultModel(String name) throws NotFoundException {
        if (defaultMap.get(name) == null) {
            throw new NotFoundException("Model " + name + " not in the factory");
        }
        this.defaultModel = name;
    }

    /**
     * clone an instance of T
     * @param instance
     * @return
     */
    public T clone(T instance) {
        return (T)cloneObject(instance);
    }


    /**
     * Clone an object of type T.
     * @param object
     * @return
     */
    protected abstract Object cloneObject(Object object);/* {
        if (object == null) return null;

        Object clone = null;
        try {

            ByteArrayOutputStream os = new ByteArrayOutputStream(512);

            marshaller.marshal(object, os);

            System.out.println(os.toString());

            InputStream is = new ByteArrayInputStream(os.toByteArray());

            clone = (Domain)unmarshaller.unmarshal(is);

            os.close();
            is.close();

        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return clone;
    }
    */
}

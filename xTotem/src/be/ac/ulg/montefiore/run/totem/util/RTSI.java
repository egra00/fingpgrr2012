package be.ac.ulg.montefiore.run.totem.util;


/*
* Changes:
* --------
* 05-Jan-2006: find methods now returns a list of classes, use of isAssignablefrom instead of newInstance (GMO)
*
*/


/**
 * RTSI.java
 *
 * Created: Wed Jan 24 11:15:02 2001
 *
 */

import java.io.*;
import java.net.URL;
import java.net.JarURLConnection;
import java.util.jar.*;
import java.util.zip.*;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;

/**
 * This utility class is looking for all the classes implementing or
 * inheriting from a given interface or class.
 * (RunTime Subclass Identification)
 *
 * @author <a href="mailto:daniel@satlive.org">Daniel Le Berre</a>
 * @version 1.0
 */
public class RTSI {


    /**
     * Display all the classes inheriting or implementing a given
     * class in the currently loaded packages.
     *
     * @param tosubclassname the name of the class to inherit from
     */
    public static List<Class> find(String tosubclassname) {
        List<Class> list = null;
        try {
            Class tosubclass = Class.forName(tosubclassname);
            Package[] pcks = Package.getPackages();
            for (int i = 0; i < pcks.length; i++) {
                list = find(pcks[i].getName(), tosubclass);
            }
        } catch (ClassNotFoundException ex) {
            System.err.println("Class " + tosubclassname + " not found!");
        }
        return list;
    }

    /**
     * Display all the classes inheriting or implementing a given
     * class in a given package.
     *
     * @param pckname        the fully qualified name of the package
     * @param tosubclassname the name of the class to inherit from
     */
    public static List<Class> find(String pckname, String tosubclassname) {
        List<Class> list = null;
        try {
            Class tosubclass = Class.forName(tosubclassname);
            list = find(pckname, tosubclass);
        } catch (ClassNotFoundException ex) {
            System.err.println("Class " + tosubclassname + " not found!");
        }
        return list;
    }

    /**
     * Display all the classes inheriting or implementing a given
     * class in a given package.
     *
     * @param pckgname   the fully qualified name of the package
     * @param tosubclass the Class object to inherit from
     */
    public static List<Class> find(String pckgname, Class tosubclass) {

        List<Class> list = new ArrayList<Class>();
        // Code from JWhich
        // ======
        // Translate the package name into an absolute path
        String name = new String(pckgname);
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        name = name.replace('.', '/');

        // Get a File object for the package
        URL url = RTSI.class.getResource(name);
        // URL url = tosubclass.getResource(name);
        // URL url = ClassLoader.getSystemClassLoader().getResource(name);
        //System.out.println(name+"->"+url);

        // Happens only if the jar file is not well constructed, i.e.
        // if the directories do not appear alone in the jar file like here:
        //
        //          meta-inf/
        //          meta-inf/manifest.mf
        //          commands/                  <== IMPORTANT
        //          commands/Command.class
        //          commands/DoorClose.class
        //          commands/DoorLock.class
        //          commands/DoorOpen.class
        //          commands/LightOff.class
        //          commands/LightOn.class
        //          RTSI.class
        //
        if (url == null) return null;

        File directory = new File(url.getFile());

        // New code
        // ======
        if (directory.exists()) {
            // Get the list of the files contained in the package
            String[] files = directory.list();
            for (int i = 0; i < files.length; i++) {

                // we are only interested in .class files
                if (files[i].endsWith(".class")) {
                    // removes the .class extension
                    String classname = files[i].substring(0, files[i].length() - 6);
                    try {
                        // Try to create an instance of the object
                        Class clazz = Class.forName(pckgname + "." + classname);
                        if (tosubclass.isAssignableFrom(clazz)) {
                            //System.out.println(classname);
                            list.add(clazz);
                        }
                    } catch (ClassNotFoundException cnfex) {
                        System.err.println(cnfex);
                    }
                }
            }
        } else {
            try {
                // It does not work with the filesystem: we must
                // be in the case of a package contained in a jar file.
                JarURLConnection conn = (JarURLConnection) url.openConnection();
                String starts = conn.getEntryName();
                JarFile jfile = conn.getJarFile();
                Enumeration e = jfile.entries();
                while (e.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    String entryname = entry.getName();
                    if (entryname.startsWith(starts)
                            && (entryname.lastIndexOf('/') <= starts.length())
                            && entryname.endsWith(".class")) {
                        String classname = entryname.substring(0, entryname.length() - 6);
                        if (classname.startsWith("/"))
                            classname = classname.substring(1);
                        classname = classname.replace('/', '.');
                        try {

                            Class clazz = Class.forName(classname);
                            if (tosubclass.isAssignableFrom(clazz)) {
                                //System.out.println(classname.substring(classname.lastIndexOf('.') + 1));
                                list.add(clazz);
                            }
                        } catch (ClassNotFoundException cnfex) {
                            System.err.println(cnfex);
                        }
                    }
                }
            } catch (IOException ioex) {
                System.err.println(ioex);
            }
        }
        return list;
    }

    public static void main(String[] args) {
        if (args.length == 2) {
            find(args[0], args[1]);
        } else {
            if (args.length == 1) {
                for (Class c : find(args[0])) {
                    System.out.println(c.getSimpleName());
                };
            } else {
                System.out.println("Usage: java RTSI [<package>] <subclass>");
            }
        }
    }
}// RTSI

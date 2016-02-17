/**
 * @file PluginFactory.java
 * @date 4 Feb 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.plugin.IQuimpPlugin;

/**
 * Plugin jar loader
 * 
 * Assumed is that jar file name with plugin will have structure:
 * 
 * @code
 * plugin_quimp-other-info.jar
 * @endcode
 * where \a plugin must match to class name \c Plugin.
 * First letter in file name can be small nevertheless it is expected that
 * class name starts from capital letter.
 * 
 * @author p.baniukiewicz
 * @date 4 Feb 2016
 *
 */
public class PluginFactory {

    private static final Logger LOGGER =
            LogManager.getLogger(PluginFactory.class.getName());
    private static final String PATTERN = "_quimp"; ///< name pattern of plugins
    private static final String PACKAGE = "uk.ac.warwick.wsbc"; ///< def package

    /**
     * List of plugins found in initial directory \c path passed to constructor
     * 
     * Plugins are organized in list <name, <path, qname, type>> where:
     * -# \b name is the name of plugin extracted from plugin jar filename
     * -# \b path is full path with jar filename
     * -# \b qname is qualified name of plugin class obtained from jar name
     * -# \b type is type of plugin read from IQuimpPlugin.setup() method
     * 
     * This field is set by scanDirectory() method -> getPluginType()
     */
    private HashMap<String, PluginProperties> availPlugins;
    private Path root;

    /**
     * 
     */
    public PluginFactory(final Path path) {
        LOGGER.debug("Attached " + path.toString());
        root = path;
        availPlugins = new HashMap<String, PluginProperties>();
        scanDirectory();
    }

    /**
     * Scan \c path for files that match \c PATTERN name and end with .jar
     * 
     * Fill \c availPlugins field.
     * 
     * @return table of files that fulfill criterion:
     * -# have extension
     * -# extension is \a .jar or \a .JAR
     * -# contain \c PATTERN in name
     */
    private File[] scanDirectory() {
        File fi = new File(root.toString());
        File[] listFiles = fi.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.lastIndexOf('.') <= 0)
                    return false; // no extension
                int lastIndex = name.lastIndexOf('.');
                // get extension
                String ext = name.substring(lastIndex);
                ext.toLowerCase();
                if (!ext.equals(".jar"))
                    return false; // no jar extension
                // now we have .jar file, check name pattern
                if (name.contains(PATTERN))
                    return true;
                else
                    return false;
            }
        });
        // decode names from listFiles and fill availPlugins names and paths
        for (File f : listFiles) {
            String filename = f.getName();
            int lastindex = filename.lastIndexOf(PATTERN);
            // cut from beginning to _quimp
            String pluginName = filename.substring(0, lastindex);
            // change first letter to upper to match class-naming convention
            pluginName = pluginName.substring(0, 1).toUpperCase()
                    + pluginName.substring(1);
            // check plugin type
            try {
                // create entry with classname and path
                availPlugins.put(pluginName, new PluginProperties(f,
                        PACKAGE + "." + pluginName, -1));
                // get type of path.classname plugin
                int type = getPluginType(f,
                        availPlugins.get(pluginName).getClassName());
                // store type in the same object
                availPlugins.get(pluginName).setType(type);
            } catch (MalformedURLException | ClassNotFoundException
                    | NoSuchMethodException | SecurityException
                    | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                LOGGER.error("Type of plugin " + pluginName + " in jar: "
                        + f.getPath()
                        + " can not be obtained. Ignoring this plugin");
                LOGGER.error(e);
            }

        }
        return Arrays.copyOf(listFiles, listFiles.length);
    }

    /**
     * Read type of plugin from plugin jar file
     * 
     * Calls IQuimpPlugin.setup() method from plugin
     * 
     * @param plugin File handler to plugin
     * @param className Formatted fully qualified class name
     * @return Codes of types from IQuimpPlugin
     * @throws MalformedURLException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException When returned type is unknown
     * @throws InvocationTargetException
     */
    private int getPluginType(File plugin, String className)
            throws MalformedURLException, ClassNotFoundException,
            NoSuchMethodException, SecurityException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {

        URL[] url = new URL[] { plugin.toURI().toURL() };
        ClassLoader child = new URLClassLoader(url);
        Class<?> classToLoad =
                Class.forName(className, true, child);
        Object instance = classToLoad.newInstance();
        int result = (int) ((IQuimpPlugin) instance).setup();
        LOGGER.debug("File: " + plugin.getName() + " Returned val: " + result);
        // decode returned result for plugin type
        if ((result & IQuimpPlugin.DOES_SNAKES) == IQuimpPlugin.DOES_SNAKES)
            return IQuimpPlugin.DOES_SNAKES;
        else
            throw new IllegalArgumentException("Plugin returned unknown type");
    }

    /**
     * Return list of plugins of given types.
     * 
     * @param type Type defined in uk.ac.warwick.wsbc.plugin.IQuimpPlugin
     * @return List of names of plugins of type \c type
     */
    public ArrayList<String> getPluginNames(int type) {
        ArrayList<String> ret = new ArrayList<String>();
        // Iterate over our collection
        Iterator<Map.Entry<String, PluginProperties>> it =
                availPlugins.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, PluginProperties> me = it.next();
            if (me.getValue().getType() == type) // if type found
                ret.add(me.getKey()); // add to list of plugins of this type
        }
        return ret;
    }

    /**
     * Return instance of plugin \c name
     * 
     * @param name Name of plugin compatible with general rules
     * @return reference to plugin of \c name
     * @throws MalformedURLException when problem with creating instance
     * @throws ClassNotFoundException when problem with creating instance
     * @throws IllegalAccessException when problem with creating instance
     * @throws InstantiationException when problem with creating instance
     */
    public IQuimpPlugin getInstance(String name)
            throws MalformedURLException, ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        // usually name of plugin is spelled with Capital letter first
        // make sure that name is in correct format
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        // find name in database
        PluginProperties pp = availPlugins.get(name);
        // load class and create instance
        URL[] url = new URL[] { pp.getFile().toURI().toURL() };
        ClassLoader child = new URLClassLoader(url);
        Class<?> classToLoad =
                Class.forName(pp.getClassName(), true, child);
        IQuimpPlugin instance = (IQuimpPlugin) classToLoad.newInstance();
        return instance;
    }
}

/**
 * Store basic plugin properties read from jar file
 * 
 * @author p.baniukiewicz
 * @date 17 Feb 2016
 *
 */
class PluginProperties {
    private File file; ///< handle to file on disk
    private int type; ///< type of plugin
    private String className; ///< name of plugin class

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * Construct plugin properties object
     * 
     * @param file Reference to plugin file
     * @param className Qualified class name of plugin
     * @param type Type of plugin returned by IQuimpPlugin.setup() method
     */
    PluginProperties(final File file, final String className, int type) {
        this.file = file;
        this.type = type;
        this.className = className;
    }

    /**
     * Type setter
     * 
     * @param type type of plugin referred by \c File
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * File getter
     * 
     * @return File object
     */
    public File getFile() {
        return file;
    }

    /**
     * Type getter
     * 
     * @return Type of \c File plugin
     */
    public int getType() {
        return type;
    }
}

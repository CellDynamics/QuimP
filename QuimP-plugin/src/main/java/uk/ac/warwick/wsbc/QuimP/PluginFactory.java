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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

/**
 * Plugin jar loader
 * 
 * Created object is connected with directory where plugins exist. This
 * directory is scanned for jar files that meet given below naming conventions.
 * Every file that meets naming convention is loaded and asked for method
 * IQuimpPlugin.setup(). On success the plugin is registered in \c availPlugins
 * database:
 * 
 * @code
 * <Name, <File, Type, ClassName>>
 * @endcode
 * 
 * Where \b Name is name of plugin extracted form file name (see below required
 * naming conventions), \b File is handle to file on disk, \b Type is type
 * of plugin according to types defined in warwick.wsbc.plugin.IQuimpPlugin and
 * \b ClassName is qualified name of class of plugin. The \b ClassName is
 * extracted from the plugin file name thus real class name of the plugin must
 * be the same. Assumed is that jar file name with plugin will have structure:
 * 
 * @code
 * plugin_quimp-other-info.jar
 * @endcode
 * where \a plugin must match to class name \c Plugin.
 * First letter in file name can be small nevertheless it is expected that
 * class name starts from capital letter. File name is always converted to small
 * letters then first letter is capitalized and then there is \c PACKAGE string
 * appended to the front. All this form \b ClassName field.
 * 
 * Simplified sequence diagrams are as follows:
 * 
 * @startuml
 * actor user
 * participant PluginFactory as PF
 * participant Plugin as PL
 * == Create instance of PluginFactory ==
 * user -> PF : //<<create>>//
 * activate PF
 * PF -> PF : init ""availPlugins""
 * PF -> PF : scanDirectory()
 * activate PF
 * PF -> PF : build qname
 * PF -> PF : getPluginType()
 * activate PF
 * PF -> Plugin : //<<create>>//
 * activate Plugin
 * PF -> Plugin : setup()
 * Plugin --> PF : ""type""
 * destroy Plugin
 * PF -> PF : store at ""availPlugins""
 * deactivate PF
 * deactivate PF
 * == Get names ==
 * user -> PF : getPluginNames(type)
 * loop ""availPlugins""
 * PF -> PF : check ""type""
 * end
 * PF --> user : List
 * == Get Instance ==
 * user -> PF : getInstance(name)
 * PF -> PF : find plugin
 * PF -> Plugin : //<<create>>//
 * activate Plugin
 * PF --> user : ""instance""
 * @enduml
 * 
 * This class try to hide all exceptions that can be thrown during loading
 * plugins from user. In general only when user pass wrong path to plugins
 * directory exception is thrown. In all other cases class returns null pointers
 * or empty lists.
 * Error handling:
 * <ol>
 * <li>Given directory exists but there is no plugins inside
 * <ol>
 * <li>getPluginNames(int) returns empty list (length 0)
 * </ol>
 * <li>Given directory exists but plugins are corrupted - they fulfill naming
 * criterion but they are not valid QuimP plugins
 * <ol>
 * <li>getInstance(final String) returns \c null when correct \c name is given.
 * It means that plugin has been registered by scanDirectory() so it had correct
 * name and supported wsbc.plugin.IQuimpPlugin.setup() method
 * </ol>
 * <li>Given directory does not exist
 * <ol>
 * <li>Constructor throws QuimpPluginException
 * </ol>
 * <li>User asked for unknown name in getInstance(final String)
 * <ol>
 * <li>getInstance(final String) return \c null
 * </ol>
 * </ol>
 * Internally getPluginType(final File, final String) and getInstance(final
 * String) throw exceptions around class loading and running methods from them.
 * Additionally getPluginType(final File, final String) throws exception when
 * unknown type is returned from valid plugin. These exceptions are caught
 * preventing adding that plugin into \c availPlugins database (scanDirectory())
 * or hidden in getInstance that returns \c null in this case. All exceptions
 * are masked besides scanDirectory() that can throw checked PluginException
 * that must be handled by caller. It usually means that given plugin directory
 * does not exist.
 * 
 * @author p.baniukiewicz
 * @date 4 Feb 2016
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
     * -# \b name is the name of plugin extracted from plugin jar filename. Name
     * is always encoded as \b Name - starts with capital letter
     * -# \b path is full path with jar filename
     * -# \b qname is qualified name of plugin class obtained from jar name
     * -# \b type is type of plugin read from IQuimpPlugin.setup() method
     * 
     * This field is set by scanDirectory() method -> getPluginType()
     */
    private HashMap<String, PluginProperties> availPlugins;
    private Path root;

    /**
     * Build object connected to plugin directory.
     * 
     * Can throw exception if there is no directory \c path.
     * 
     * @startuml
     *
     * partition PluginFactory(directory) {
     * (*) --> if "plugin directory exists" then
     * -->[true] init ""availPlugins""
     * --> "scanDirectory()"
     * -right-> (*)
     * else
     * ->[false] "throw QuimpPluginException"
     * --> (*)
     * endif
     * }
     * @enduml
     * 
     * @throws QuimpPluginException when plugin directory can not be read
     */
    public PluginFactory(final Path path) throws QuimpPluginException {
        LOGGER.debug("Attached " + path.toString());
        // check if dir exists
        if (Files.notExists(path))
            throw new QuimpPluginException("Plugin directory can not be read");
        root = path;
        availPlugins = new HashMap<String, PluginProperties>();
        scanDirectory(); // throw PluginException on wrong path
    }

    /**
     * Scan \c path for files that match \c PATTERN name and end with .jar
     * 
     * Fill \c availPlugins field. Field name is filled as Name without
     * dependency how original filename was written. It is converted to small
     * letters and then first char is upper-case written.
     * 
     * @startuml
     * 
     * partition scanDirectory() {
     * (*) --> Get file \nfrom ""root""
     * if "file contains\n**_quimp.jar**" then
     * -->[true] Create qualified name
     * --> getPluginType()
     * --> if Type valid\njar valid\nreadable
     * -->[true] Store at ""availPlugins""
     * --> Get file \nfrom ""root""
     * else
     * -->[false] log error
     * --> Get file \nfrom ""root""
     * endif
     * else
     * -->[false] Get file \nfrom ""root""
     * }
     * @enduml
     * 
     * @return table of files that fulfill criterion:
     * -# have extension
     * -# extension is \a .jar or \a .JAR
     * -# contain \c PATTERN in name
     * If there is no plugins in directory it returns 0 length array
     */
    private File[] scanDirectory() {
        File fi = new File(root.toString());
        File[] listFiles = fi.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, final String name) {
                String sname = name.toLowerCase();
                if (sname.lastIndexOf('.') <= 0)
                    return false; // no extension
                int lastIndex = sname.lastIndexOf('.');
                // get extension
                String ext = sname.substring(lastIndex);
                if (!ext.equals(".jar"))
                    return false; // no jar extension
                // now we have .jar file, check name pattern
                if (sname.contains(PATTERN))
                    return true;
                else
                    return false;
            }
        });
        if (listFiles == null) // should no be because of checking in constr.
            return new File[0]; // but if yes return empty array
        // decode names from listFiles and fill availPlugins names and paths
        for (File f : listFiles) {
            // build plugin name from file name
            String filename = f.getName().toLowerCase();
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
                        PACKAGE + "." + pluginName, IQuimpPlugin.GENERAL));
                // get type of path.classname plugin
                int type = getPluginType(f,
                        availPlugins.get(pluginName).getClassName());
                // store type in the same object
                availPlugins.get(pluginName).setType(type);
                // catch any error in plugin services - plugin is not stored
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
     * @see uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin
     */
    private int getPluginType(final File plugin, final String className)
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
     * @return List of names of plugins of type \c type. If there is no plugins
     * in directory (this type or any) returned list has length 0
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
     * @startuml
     * 
     * partition getInstance(name) {
     * (*) --> Build qualified name\nfrom ""name""
     * --> get plugin data from\n ""availPlugins""
     * if returned ""null""
     * -->[true] log error
     * --->[return ""null""] (*)
     * else
     * partition "jar loader" {
     * -->[false] Open jar
     * --> Load class
     * -down> Create instance
     * }
     * endif
     * --> if **jar loader**\nsuccess
     * ->[true return ""instance""](*)
     * else
     * ->[false return ""null""](*)
     * endif
     * }
     * @enduml
     * 
     * @param name Name of plugin compatible with general rules
     * @return reference to plugin of \c name or \c null when there is any
     * problem with creating instance or given \c name does not exist in
     * \c availPlugins base
     * @throws IllegalArgumentException when incorrect \c name
     */
    public IQuimpPlugin getInstance(final String name) {
        try {
            // usually name of plugin is spelled with Capital letter first
            // make sure that name is in correct format
            String qname =
                    name.substring(0, 1).toUpperCase() + name.substring(1);
            // find name in database
            PluginProperties pp = availPlugins.get(qname);
            if (pp == null)
                throw new IllegalArgumentException(
                        "Plugin of name: " + name + " is not loaded");
            // load class and create instance
            URL[] url = new URL[] { pp.getFile().toURI().toURL() };
            ClassLoader child = new URLClassLoader(url);
            Class<?> classToLoad =
                    Class.forName(pp.getClassName(), true, child);
            IQuimpPlugin instance = (IQuimpPlugin) classToLoad.newInstance();
            return instance;
        } catch (MalformedURLException | ClassNotFoundException
                | InstantiationException | IllegalAccessException
                | IllegalArgumentException e) {
            LOGGER.error("Plugin " + name + " can not be instanced");
            LOGGER.error(e);
            return null;
        }

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

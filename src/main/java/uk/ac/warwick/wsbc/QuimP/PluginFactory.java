/**
 */
package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

/**
 * Plugin jar loader
 * 
 * Created object is connected with directory where plugins exist. This directory is scanned for jar
 * files that meet given below naming conventions. Every file that meets naming convention is loaded
 * and asked for method IQuimpPlugin.setup(). On success the plugin is registered in \c availPlugins
 * database:
 * 
 * @code <Name, <File, Type, ClassName>>
 * @endcode
 * 
 *          Where \b Name is name of plugin extracted form file name (see below required naming
 *          conventions), \b File is handle to file on disk, \b Type is type of plugin according to
 *          types defined in warwick.wsbc.plugin.IQuimpPlugin and \b ClassName is qualified name of
 *          class of plugin. The \b ClassName is extracted from the plugin jar file assuming that
 *          plugin class contains underscore _ in its name. If more classes underscored is found in
 *          jar, only the first discovered is loaded. Thus the following conventions are required:
 *          <ol>
 *          <li>Plugin name must contain \a -quimp to be considered as plugin (see PATTERN field)
 * @code plugin-quimp-other-info.jar
 * @endcode
 *          <li>Class name in plugin must end with underscore to be considered as plugin main class
 *          </ol>
 *
 *          Simplified sequence diagrams are as follows:
 * 
 * @startuml actor user participant PluginFactory as PF participant Plugin as PL == Create instance
 *           of PluginFactory == user -> PF : //<<create>>// activate PF PF -> PF : init
 *           ""availPlugins"" PF -> PF : scanDirectory() activate PF PF -> PF : discover qname
 *           getClassName PF -> PF : getPluginType() activate PF PF -> PL :
 *           //<<getPluginInstance>>// activate PL PF -> PL : getPluginType() PL --> PF : ""type""
 *           PF -> PL : getPluginVersion() PL --> PF : ""version"" destroy PL PF -> PF : store at
 *           ""availPlugins"" deactivate PF deactivate PF == Get names == user -> PF :
 *           getPluginNames(type) loop ""availPlugins"" PF -> PF : check ""type"" end PF --> user :
 *           List == Get Instance == user -> PF : getInstance(name) PF -> PF : find plugin PF -> PL
 *           : //<<getPluginInstance>>// activate PL PF --> user : ""instance""
 * @enduml
 * 
 *         This class try to hide all exceptions that can be thrown during loading plugins from
 *         user. In general only when user pass wrong path to plugins directory exception is thrown.
 *         In all other cases class returns null pointers or empty lists. Error handling:
 *         <ol>
 *         <li>Given directory exists but there is no plugins inside
 *         <ol>
 *         <li>getPluginNames(int) returns empty list (length 0)
 *         </ol>
 *         <li>Given directory exists but plugins are corrupted - they fulfill naming criterion but
 *         they are not valid QuimP plugins
 *         <ol>
 *         <li>getInstance(final String) returns \c null when correct \c name is given. It means
 *         that plugin has been registered by scanDirectory() so it had correct name and supported
 *         wsbc.plugin.IQuimpPlugin.setup() method
 *         </ol>
 *         <li>Given directory does not exist
 *         <ol>
 *         <li>Constructor throws QuimpPluginException
 *         </ol>
 *         <li>User asked for unknown name in getInstance(final String)
 *         <ol>
 *         <li>getInstance(final String) return \c null
 *         </ol>
 *         </ol>
 *         Internally getPluginType(final File, final String) and getInstance(final String) throw
 *         exceptions around class loading and running methods from them. Additionally
 *         getPluginType(final File, final String) throws exception when unknown type is returned
 *         from valid plugin. These exceptions are caught preventing adding that plugin into \c
 *         availPlugins database (scanDirectory()) or hidden in getInstance that returns \c null in
 *         this case. All exceptions are masked besides scanDirectory() that can throw checked
 *         PluginException that must be handled by caller. It usually means that given plugin
 *         directory does not exist.
 * 
 * @author p.baniukiewicz
 */
public class PluginFactory {

    static final Logger LOGGER = LoggerFactory.getLogger(PluginFactory.class.getName());
    private static final String PATTERN = "-quimp"; //!< name pattern of plugins

    /**
     * List of plugins found in initial directory \c path passed to constructor
     * 
     * Plugins are organized in list <name, <path, qname, type>> where: -# \b name is the name of
     * plugin extracted from plugin jar filename. Name is always encoded as \b Name - starts with
     * capital letter -# \b path is full path with jar filename -# \b qname is qualified name of
     * plugin class obtained from jar name -# \b type is type of plugin read from
     * IQuimpPlugin.setup() method
     * 
     * This field is set by scanDirectory() method -> getPluginType()
     */
    private HashMap<String, PluginProperties> availPlugins;
    private Path root;

    /**
     * Accessor to internal database of loaded plugins
     * 
     * @return Non-modifiable database of loaded plugins
     */
    public Map<String, PluginProperties> getRegisterdPlugins() {

        return Collections.unmodifiableMap(availPlugins);
    }

    /**
     * Build object connected to plugin directory.
     * 
     * Can throw exception if there is no directory \c path.
     * 
     * @startuml
     *
     *           partition PluginFactory(directory) { (*) --> if "plugin directory exists" then
     *           -->[true] init ""availPlugins"" --> "scanDirectory()" -right-> (*) else ->[false]
     *           "throw QuimpPluginException" --> (*) endif }
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
     * Fill \c availPlugins field. Field name is filled as Name without dependency how original
     * filename was written. It is converted to small letters and then first char is upper-case
     * written.
     * 
     * @startuml
     * 
     *           partition scanDirectory() { (*) --> Get file \nfrom ""root"" if "file
     *           contains\n**-quimp.jar**" then -->[true] Discover qualified name -->
     *           getPluginType() --> if Type valid\njar valid\nreadable -->[true] Store at
     *           ""availPlugins"" --> Get file \nfrom ""root"" else -->[false] log error --> Get
     *           file \nfrom ""root"" endif else -->[false] Get file \nfrom ""root"" }
     * @enduml
     * 
     * @return table of files that fulfill criterion: -# have extension -# extension is \a .jar or
     *         \a .JAR -# contain \c PATTERN in name If there is no plugins in directory it returns
     *         0 length array
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
            // cut from beginning to -quimp
            String pluginName = filename.substring(0, lastindex);
            // change first letter to upper to match class-naming convention
            pluginName = pluginName.substring(0, 1).toUpperCase() + pluginName.substring(1);
            // check plugin type
            try {
                // ask for class names in jar
                String cname = getClassName(f);
                // make temporary instance
                Object inst = getPluginInstance(f, cname);
                // get type of path.classname plugin
                int type = getPluginType(inst);
                // get version of path.classname plugin
                String ver = getPluginVersion(inst);
                // create entry with classname and path
                availPlugins.put(pluginName, new PluginProperties(f, cname, type, ver));
                LOGGER.debug("Registered plugin: " + pluginName + " "
                        + availPlugins.get(pluginName).toString());
                // catch any error in plugin services - plugin is not stored
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
                    | InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | ClassCastException | IOException
                    | NoClassDefFoundError e) {
                LOGGER.error("Type of plugin " + pluginName + " in jar: " + f.getPath()
                        + " can not be obtained. Ignoring this plugin");
                LOGGER.error(e.getMessage());
            }

        }
        return Arrays.copyOf(listFiles, listFiles.length);
    }

    /**
     * Extracts qualified name of classes in jar file. Class name must contain underscore.
     * 
     * @param pathToJar path to jar file
     * @return Name of first discovered class with underscore
     * @throws IOException When jar can not be opened
     * @throws IllegalArgumentException when there is no classes in jar
     * @see <a href=
     *      "link">http://stackoverflow.com/questions/11016092/how-to-load-classes-at-runtime-from-a-folder-or-jar</a>
     */
    private String getClassName(File pathToJar) throws IOException {
        ArrayList<String> names = new ArrayList<>(); // all discovered names
        JarFile jarFile = new JarFile(pathToJar);
        Enumeration<JarEntry> e = jarFile.entries();

        while (e.hasMoreElements()) {
            JarEntry je = (JarEntry) e.nextElement();
            String entryname = je.getName();
            if (je.isDirectory() || !entryname.endsWith("_.class")) {
                continue;
            }
            // -6 because of .class
            String className = je.getName().substring(0, je.getName().length() - 6);
            className = className.replace('/', '.');
            names.add(className);
            LOGGER.debug("In " + pathToJar.toString() + " found class " + entryname);
        }
        jarFile.close();
        if (names.isEmpty())
            throw new IllegalArgumentException(
                    "getClassName: There is no underscored classes in jar");
        if (names.size() > 1)
            LOGGER.warn("More than one underscored class in jar " + pathToJar.toString()
                    + " Take first one " + names.get(0));
        return names.get(0);
    }

    /**
     * Gets type of plugin
     * 
     * Calls IQuimpPlugin.setup() method from plugin
     * 
     * @startuml start :call ""setup()"" from jar; if (valid plugin type?) then (true) :Return
     *           plugin type; stop else (false) :throw Exception; endif end
     * @enduml
     * 
     * @param instance Instance of plugin
     * @return Codes of types from IQuimpPlugin
     * @throws IllegalArgumentException When returned type is unknown
     * @see uk.ac.warwick.wsbc.QuimP.plugin.IQuimpCorePlugin
     */
    private int getPluginType(Object instance)
            throws IllegalArgumentException, NoSuchMethodException, InvocationTargetException {

        int result = (int) ((IQuimpCorePlugin) instance).setup();
        // decode returned result for plugin type
        if ((result & IQuimpCorePlugin.DOES_SNAKES) == IQuimpCorePlugin.DOES_SNAKES)
            return IQuimpCorePlugin.DOES_SNAKES;
        else
            throw new IllegalArgumentException("Plugin returned unknown type");
    }

    /**
     * Gets version of plugin
     * 
     * Calls IQuimpPlugin.getVersion() method from plugin
     * 
     * @param instance Instance of plugin
     * @return String representing version of plugin or \c null if plugin does not support
     *         versioning
     */
    private String getPluginVersion(Object instance)
            throws NoSuchMethodException, InvocationTargetException {
        return ((IQuimpCorePlugin) instance).getVersion();
    }

    /**
     * Creates instance of plugin
     * 
     * @startuml start :Load jar; :Create instance; end
     * @enduml
     * 
     * @param plugin plugin File handler to plugin
     * @param className
     * @return className Formatted fully qualified class name
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws MalformedURLException
     */
    private Object getPluginInstance(final File plugin, final String className)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException,
            MalformedURLException {
        URL[] url = new URL[] { plugin.toURI().toURL() };
        ClassLoader child = new URLClassLoader(url);
        Class<?> classToLoad = Class.forName(className, true, child);
        Object instance = classToLoad.newInstance();
        return instance;
    }

    /**
     * Return list of plugins of given types.
     * 
     * @param type Type defined in uk.ac.warwick.wsbc.plugin.IQuimpPlugin
     * @return List of names of plugins of type \c type. If there is no plugins in directory (this
     *         type or any) returned list has length 0
     */
    public ArrayList<String> getPluginNames(int type) {
        ArrayList<String> ret = new ArrayList<String>();
        // Iterate over our collection
        Iterator<Map.Entry<String, PluginProperties>> it = availPlugins.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, PluginProperties> me = it.next();
            if (me.getValue().getType() == type) // if type found
                ret.add(me.getKey()); // add to list of plugins of this type
        }
        if (ret.isEmpty())
            LOGGER.warn("No plugins found");
        return ret;
    }

    /**
     * Return instance of plugin \c name
     * 
     * @startuml
     * 
     *           partition getInstance(name) {
     * 
     *           (*) -->if name is not empty --> Build qualified name\nfrom ""getClassName()"" -->
     *           get plugin data from\n ""availPlugins"" if returned ""null"" -->[true] log error
     *           --->[return ""null""] (*) else partition "getPluginInstance" { -->[false] Open jar
     *           --> Load class -down> Create instance } endif --> if **jar loader**\nsuccess
     *           ->[true return ""instance""](*) else ->[false return ""null""](*) endif endif }
     * @enduml
     * 
     * @param name Name of plugin compatible with general rules
     * @return reference to plugin of \c name or \c null when there is any problem with creating
     *         instance or given \c name does not exist in \c availPlugins base
     */
    public IQuimpCorePlugin getInstance(final String name) {
        try {
            if (name.isEmpty())
                throw new IllegalArgumentException("Plugin of name: " + name + " is not loaded");
            // usually name of plugin is spelled with Capital letter first
            // make sure that name is in correct format
            String qname = name.substring(0, 1).toUpperCase() + name.substring(1);
            // find name in database
            PluginProperties pp = availPlugins.get(qname);
            if (pp == null)
                throw new IllegalArgumentException("Plugin of name: " + name + " is not loaded");
            // load class and create instance
            IQuimpCorePlugin instance =
                    (IQuimpCorePlugin) getPluginInstance(pp.getFile(), pp.getClassName());
            return instance;
        } catch (MalformedURLException | ClassNotFoundException | InstantiationException
                | IllegalAccessException | IllegalArgumentException e) {
            LOGGER.error(
                    "Plugin " + name + " can not be instanced (reason: " + e.getMessage() + ")");
            return null;
        }

    }
}

/**
 * Store basic plugin properties read from jar file
 * 
 * @author p.baniukiewicz
 *
 */
class PluginProperties {
    private File file; // !< handle to file on disk
    private int type; // !< type of plugin
    private String className; // !< name of plugin class
    private String version; // !< version returned from plugin

    /**
     * Version getter
     * 
     * @return the version
     */
    public String getVersion() {
        return version;
    }

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
     * @param version Version of plugin returned from IQuimpPlugin.getVersion() method
     */
    PluginProperties(final File file, final String className, int type, final String version) {
        this.file = file;
        this.type = type;
        this.className = className;
        if (version == null) // if plugin does not support versioning may return null
            this.version = "";
        else
            this.version = version;
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

    @Override
    public String toString() {
        return "ClassName: " + className + " path: " + file + " type: " + type + " ver: " + version;
    }
}

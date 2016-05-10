/**
 * @file Serializer.java
 * @date 31 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Save wrapped class together with itself to JSON file. 
 * 
 * This serializer accepts only classes derived from IQuimpSerialize interface. 
 * Saved class is packed in top level structure that contains version of software and wrapped class
 * name. Final JSON looks as follows:
 * @code
 * {
 *  // This part comes from Serializer itself
 *  "className": "SnakePluginList",
 *  "version": [
 *      "version not found in jar",
 *       "build info not found in jar",
 *       "name not found in jar"
 *  ],
 *  "obj": {
 *   // here is content of wrapped object
 *  }
 * }
 * @endcode
 * 
 * Exemplary use case:
 * @code
 * {
 *   Serializer<SnakePluginList> s;
 *   s = new Serializer<>(boaState.snakePluginList, quimpInfo);
 *   s.setPretty(); // set pretty format
 *   s.save(sd.getDirectory() + sd.getFileName()); // save it
 *   s = null; // remove
 * }
 * @endcode
 * 
 * @author p.baniukiewicz
 * @date 31 Mar 2016
 * @see http://stackoverflow.com/questions/14139437/java-type-generic-as-argument-for-gson
 * @see SerializerTest for examples of use
 * @see uk.ac.warwick.wsbc.QuimP.Serializer.registerInstanceCreator(Class<T>, Object)
 */
public class Serializer<T extends IQuimpSerialize> implements ParameterizedType {
    private static final Logger LOGGER = LogManager.getLogger(Serializer.class.getName());
    private transient GsonBuilder gsonBuilder;
    private transient Type t;

    public String className; //!< Name of wrapped class, decoded from object
    public String[] version; //!< Version and other information passed to serializer
    public T obj; //!< Wrapped object being serialized

    /**
     * Default constructor used for restoring object
     * 
     * Template \a T can not be restored during runtime thus the type of wrapped object is not known 
     * for GSon. This is why this type must be passed explicitly to Serializer.
     * 
     * @param t Type of underlying object
     * @see SerializerTest for examples of use
     */
    public Serializer(Type t) {
        gsonBuilder = new GsonBuilder();
        obj = null;
        version = new String[0];
        this.t = t;
    }

    /**
     * Constructor used for saving wrapped class
     * 
     * @param obj Object being saved
     * @param version Extra information saved as top layer
     */
    public Serializer(final T obj, final String[] version) {
        this(obj.getClass());
        this.obj = obj;
        className = obj.getClass().getSimpleName();
        this.version = version;
    }

    /**
     * Save wrapped object passed in constructor as JSON file
     * 
     * Calls uk.ac.warwick.wsbc.QuimP.IQuimpSerialize.beforeSerialize() before save
     * 
     * @param filename Name of file
     * @throws FileNotFoundException if problem with saving
     * @see uk.ac.warwick.wsbc.QuimP.Serializer.setPretty()
     * @see uk.ac.warwick.wsbc.QuimP.Serializer.Serializer(final T obj, final String[] version)
     * @see uk.ac.warwick.wsbc.QuimP.Serializer.toString()
     */
    public void save(final String filename) throws FileNotFoundException {
        String str;
        str = toString(); // produce json
        LOGGER.debug("Saving at: " + filename);
        LOGGER.trace(str);
        PrintWriter f;
        f = new PrintWriter(new File(filename));
        f.print(str);
        f.close();
    }

    /**
     * Load wrapped object from JSON file
     * 
     * Calls uk.ac.warwick.wsbc.QuimP.IQuimpSerialize.afterSerialize() after load
     * 
     * @param filename Name of file
     * @throws IOException if problem with loading
     * @throws Exception if wrong JSON or from afterSerialize() method (specific to wrapped object) 
     * @return New instance of loaded object packed in Serializer class
     */
    public Serializer<T> load(final String filename) throws IOException, Exception {
        Gson gson = gsonBuilder.create();
        LOGGER.debug("Loading from: " + filename);
        FileReader f = new FileReader(new File(filename));
        Serializer<T> localref;
        localref = gson.fromJson(f, this);
        f.close();
        localref.obj.afterSerialize();
        return localref;
    }

    /**
     * Restore wrapped object from JSON string
     * 
     * @param json JSON string
     * @return New instance of loaded object packed in Serializer class
     * @throws Exception if wrong JSON or from afterSerialize() method (specific to wrapped object)
     */
    public Serializer<T> fromString(final String json) throws Exception {
        Gson gson = gsonBuilder.create();
        Serializer<T> localref;
        localref = gson.fromJson(json, this);
        localref.obj.afterSerialize();
        return localref;
    }

    /**
     * Convert wrapped class to JSON representation together with Serializer wrapper
     * 
     * Calls uk.ac.warwick.wsbc.QuimP.IQuimpSerialize.beforeSerialize() before conversion
     * 
     * @return JSON string
     * @see uk.ac.warwick.wsbc.QuimP.Serializer.setPretty()
     */
    public String toString() {
        Gson gson = gsonBuilder.create();
        if (obj != null)
            obj.beforeSerialize();
        return gson.toJson(this);
    }

    /**
     * Sets pretty JSON formatting on save operation
     * 
     * @see uk.ac.warwick.wsbc.QuimP.Serializer.toString()
     * @see uk.ac.warwick.wsbc.QuimP.Serializer.save(final String)
     */
    public void setPretty() {
        gsonBuilder.setPrettyPrinting();
    }

    /**
     * Register constructor for wrapped class.
     * 
     * It may be necessary during loading JSON file if wrapped class needs some parameters to
     * restore its state on uk.ac.warwick.wsbc.QuimP.IQuimpSerialize.afterSerialize() call and
     * those parameters are passed in constructor.
     * 
     * @param type Type of class
     * @param typeAdapter Wrapped object builder that implements InstanceCreator interface. 
     * Example of use:
     * @code{.java}
     * class SnakePluginListInstanceCreator implements InstanceCreator<SnakePluginList> {
     *      private int size;
     *      private PluginFactory pf;
     *      private List<Point2d> dt;
     *      private ViewUpdater vu;
     *
     * public SnakePluginListInstanceCreator(int size, final PluginFactory pf,
     *        final List<Point2d> dataToProcess, final ViewUpdater vu) {
     *      this.size = size;
     *      this.pf = pf;
     *      this.dt = dataToProcess;
     *      this.vu = vu;
     * }
     *
     * @Override
     * public SnakePluginList createInstance(Type arg0) {
     *      return new SnakePluginList(size, pf, dt, vu);
     * }
     * 
     * Serializer<SnakePluginList> out;
     * Serializer<SnakePluginList> s = new Serializer<>(SnakePluginList.class);
     * s.registerInstanceCreator(SnakePluginList.class,
     *           new SnakePluginListInstanceCreator(3, pluginFactory, null, null));
     * out = s.fromString(json);
     * @endcode
     * @see uk.ac.warwick.wsbc.QuimP.IQuimpSerialize.afterSerialize()
     * @see https://github.com/google/gson/blob/master/UserGuide.md#TOC-InstanceCreator-for-a-Parameterized-Type
     */
    public void registerInstanceCreator(Class<T> type, Object typeAdapter) {
        gsonBuilder.registerTypeAdapter(type, typeAdapter);
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[] { t };
    }

    @Override
    public Type getRawType() {
        return Serializer.class;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

}

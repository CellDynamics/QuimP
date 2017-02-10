/**
 */
package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Since;

import uk.ac.warwick.wsbc.QuimP.filesystem.IQuimpSerialize;
import uk.ac.warwick.wsbc.QuimP.filesystem.versions.IQconfOlderConverter;

/**
 * Support saving and loading wrapped class to/from JSON file or string.
 * 
 * The Serializer class wraps provided objects and converts it to Gson together with itself.
 * Serializer adds fields like wrapped class name and versioning data (@link {@link QuimpVersion})
 * to JSON.
 * 
 * Restored object is constructed using its constructor. If JSON file does not contain variable
 * available in class being restored, it will have the value assigned in constructor or null. GSon
 * overrides variables after they have been created in normal process of object building. Check
 * {@link #fromReader(Reader)} for details.
 * 
 * This serializer accepts only classes derived from IQuimpSerialize interface. Saved class is
 * packed in top level structure that contains version of software and wrapped class name. Exemplary
 * use case: {@link uk.ac.warwick.wsbc.QuimP.SerializerTest#testLoad_1()}
 * 
 * There is option to skip call afterSerialzie() method on class restoring. To do so set
 * {@link #doAfterSerialize} to false - derive new class and override this field.
 * 
 * Serializer supports <tt>Since</tt> tags from GSon library. User can write his own converters
 * executed if specified condition is met. Serializer compares version of callee tool (provided in
 * Serializer constructor) with trigger version returned by converter {@link IQconfOlderConverter}
 * and executes conversion provided by it.
 * 
 * @author p.baniukiewicz
 * @param <T>
 * @see <a href=
 *      "link">http://stackoverflow.com/questions/14139437/java-type-generic-as-argument-for-gson</a>
 * @see uk.ac.warwick.wsbc.QuimP.Serializer#registerInstanceCreator(Class, Object)
 * @see #registerConverter(IQconfOlderConverter)
 */
public class Serializer<T extends IQuimpSerialize> implements ParameterizedType {

    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(Serializer.class.getName());

    /**
     * The gson builder.
     */
    public transient GsonBuilder gsonBuilder;
    private transient Type t;

    /**
     * Indicates if afterSerialze should be called.
     */
    protected transient boolean doAfterSerialize;

    /**
     * Name of wrapped class, decoded from object.
     */
    public String className;

    /**
     * Version and other information passed to serializer.
     */
    @Since(17.0202)
    public QuimpVersion timeStamp;

    /**
     * Date when file has been created.
     */
    public String createdOn;

    /**
     * Wrapped object being serialized.
     */
    public T obj;

    /**
     * Version stored in QCONF file loaded by Serialiser.
     * 
     * If class is serialised (saved) it contains version provided with constructor.
     */
    private transient Double loadedQconfVersion;

    /**
     * List of format converters called on every load when certain condition is met.
     */
    private transient ArrayList<IQconfOlderConverter<T>> converters = new ArrayList<>();

    /**
     * Default constructor used for restoring object.
     * 
     * Template T can not be restored during runtime thus the type of wrapped object is not known
     * for GSon. This is why this type must be passed explicitly to Serializer.
     * 
     * @param t
     * @param version Version of framework this class is called from.
     */
    public Serializer(final Type t, final QuimpVersion version) {
        doAfterSerialize = true; // by default use afterSerialize methods to restore object state
        gsonBuilder = new GsonBuilder();
        obj = null;
        this.timeStamp = version;
        this.t = t;
    }

    /**
     * Constructor used for saving wrapped class.
     * 
     * @param obj Object being saved
     * @param version Version of framework this class is called from.
     */
    public Serializer(final T obj, final QuimpVersion version) {
        doAfterSerialize = true; // by default use afterSerialize methods to restore object state
        gsonBuilder = new GsonBuilder();
        this.t = obj.getClass();
        this.obj = obj;
        className = obj.getClass().getSimpleName();
        this.timeStamp = version;
        this.loadedQconfVersion = convertStringVersion(version.getVersion());
    }

    /**
     * Save wrapped object passed in constructor as JSON file.
     * 
     * Calls {@link IQuimpSerialize#beforeSerialize()} before save.
     * 
     * @param filename Name of file
     * @throws FileNotFoundException if problem with saving
     * @see uk.ac.warwick.wsbc.QuimP.Serializer#setPretty()
     * @see uk.ac.warwick.wsbc.QuimP.Serializer#Serializer(IQuimpSerialize, QuimpVersion)
     * @see uk.ac.warwick.wsbc.QuimP.Serializer#toString()
     */
    public void save(final String filename) throws FileNotFoundException {
        String str;
        str = toString(); // produce json
        LOGGER.debug("Saving at: " + filename);
        PrintWriter f;
        f = new PrintWriter(new File(filename));
        f.print(str);
        f.close();
    }

    /**
     * @param filename to load
     * @return Serializer object
     * @throws IOException
     * @throws JsonSyntaxException
     * @throws JsonIOException
     * @throws Exception
     * @see #load(File)
     */
    public Serializer<T> load(final String filename)
            throws IOException, JsonSyntaxException, JsonIOException, Exception {
        File file = new File(filename);
        return load(file);
    }

    /**
     * Load wrapped object from JSON file.
     * 
     * Calls {@link IQuimpSerialize#afterSerialize()} after load
     * 
     * @param filename
     * @return Serialiser object
     * @throws IOException
     * @throws JsonSyntaxException
     * @throws JsonIOException
     * @throws Exception
     * @see #fromReader(Reader)
     */
    public Serializer<T> load(final File filename)
            throws IOException, JsonSyntaxException, JsonIOException, Exception {
        LOGGER.debug("Loading from: " + filename.getPath());
        // gather version from JSON
        FileReader vr = new FileReader(filename);
        loadedQconfVersion = getQconfVersion(vr);
        vr.close(); // on duplicate to avoid problems with moving pointer

        FileReader f = new FileReader(filename);
        return fromReader(f);
    }

    /**
     * Restore wrapped object from JSON string.
     * 
     * @param json
     * @return Serialise object
     * @throws JsonSyntaxException
     * @throws JsonIOException
     * @throws Exception
     * @see #fromReader(Reader)
     */
    public Serializer<T> fromString(final String json)
            throws JsonSyntaxException, JsonIOException, Exception {
        LOGGER.debug("Reading from string");
        // gather version from JSON
        Reader vr = new StringReader(json);
        loadedQconfVersion = getQconfVersion(vr);
        vr.close(); // on duplicate to avoid problems with moving pointer

        Reader reader = new StringReader(json);
        return fromReader(reader);
    }

    /**
     * Restore wrapped object from JSON string.
     * 
     * @param reader
     * @throws IOException when file can not be read
     * @throws JsonSyntaxException on bad file or when class has not been restored correctly
     * @throws JsonIOException This exception is raised when Gson was unable to read an input stream
     *         or write to on
     * @throws Exception from afterSerialize() method (specific to wrapped object)
     * @return New instance of loaded object packed in Serializer class. returned instance has
     *         proper (no nulls or empty strings) fields: \a className, \a createdOn, \a version
     *         (and its subfields, \a obj
     */
    public Serializer<T> fromReader(final Reader reader)
            throws JsonSyntaxException, JsonIOException, Exception {

        // set version to load (read from file)
        gsonBuilder.setVersion(loadedQconfVersion);

        Gson gson = gsonBuilder.create();
        Serializer<T> localref;
        localref = gson.fromJson(reader, this);
        verify(localref); // verification of correctness and conversin to current format
        if (doAfterSerialize)
            localref.obj.afterSerialize();
        return localref;
    }

    /**
     * Perform basic verification of loaded file.
     * 
     * It verifies rather on general level for fields added by Serializer itself. More detailed
     * verification related to serialized class should be performed after full restoration of
     * wrapped object.
     * 
     * @param localref object to verify
     * @throws JsonSyntaxException on bad file or when class has not been restored correctly
     */
    private void verify(Serializer<T> localref) throws JsonSyntaxException {
        // basic verification of loaded file, check whether some fields have reasonable values
        try {
            if (localref.obj == null || localref.className.isEmpty()
                    || localref.createdOn.isEmpty())
                throw new JsonSyntaxException("Can not map loaded gson to class");
            convert(localref);
        } catch (NullPointerException | IllegalArgumentException | QuimpException np) {
            throw new JsonSyntaxException("Can not map loaded gson to class", np);
        }
    }

    /**
     * This method is called on load and goes through registered converters executing them.
     * 
     * @param localref
     * @throws QuimpException on problems with conversion
     * @see #registerConverter(IQconfOlderConverter)
     */
    private void convert(Serializer<T> localref) throws QuimpException {
        if (converters.isEmpty())
            return; // no converters registered

        for (IQconfOlderConverter<T> converter : converters) {
            // compare version loaded from file. If read version from file is smaller than returned
            // by converter - execute conversion
            if (converter.executeForLowerThan() > loadedQconfVersion)
                converter.upgradeFromOld(localref);
        }
    }

    /**
     * This method register format converter on list of converters.
     * 
     * Registered converters are called on every object deserialisation in order that they were
     * registered. Converter is run when version of tool is higher than version of converter.
     * 
     * @param converter
     * @see IQconfOlderConverter
     * @see #convert(Serializer)
     */
    public void registerConverter(IQconfOlderConverter<T> converter) {
        converters.add(converter);
    }

    /**
     * Convert wrapped class to JSON representation together with Serializer wrapper
     * 
     * Calls uk.ac.warwick.wsbc.QuimP.IQuimpSerialize.beforeSerialize() before conversion
     * 
     * @return JSON string
     * @see uk.ac.warwick.wsbc.QuimP.Serializer#setPretty()
     */
    public String toString() {
        Gson gson = gsonBuilder.create();
        // fill date of creation
        Date dNow = new Date();
        SimpleDateFormat df = new SimpleDateFormat("E yyyy.MM.dd 'at' HH:mm:ss a zzz");
        createdOn = df.format(dNow);
        if (obj != null)
            obj.beforeSerialize();
        return gson.toJson(this);
    }

    /**
     * @param obj
     * @param filename
     * @param savePretty
     * @throws FileNotFoundException
     * @see #Dump(Object, String, boolean)
     */
    static void Dump(final Object obj, final String filename, boolean savePretty)
            throws FileNotFoundException {
        File file = new File(filename);
        Serializer.Dump(obj, file, savePretty);
    }

    /**
     * Performs pure dump of provided object without packing it into super class
     * 
     * <p>
     * <b>Warning</b>
     * <p>
     * This method does not call beforeSerialize(). It must be called explicitly before dumping
     * 
     * Can be used for saving already packed objects
     * 
     * @param obj to dump
     * @param filename to be saved under
     * @param savePretty if \a true use pretty format
     * @throws FileNotFoundException when file can not be created
     */
    static void Dump(final Object obj, final File filename, boolean savePretty)
            throws FileNotFoundException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        if (savePretty)
            gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        if (obj != null) {
            String str = gson.toJson(obj);
            PrintWriter f;
            f = new PrintWriter(filename);
            f.print(str);
            f.close();
        }
    }

    /**
     * Sets pretty JSON formatting on save operation.
     * 
     * @see uk.ac.warwick.wsbc.QuimP.Serializer#toString()
     * @see uk.ac.warwick.wsbc.QuimP.Serializer#save(String)
     */
    public void setPretty() {
        gsonBuilder.setPrettyPrinting();
    }

    /**
     * Read QuimP version from QCONF file.
     * 
     * It does not deserialize JSON, just plain string reading from file.
     * 
     * @param reader
     * @return Version string encoded as double. Any -SNAPSHOT suffix is removed. Return 0.0 on
     *         error.
     * @throws JsonSyntaxException on version read error
     */
    public Double getQconfVersion(Reader reader) {
        // key to look for
        Double ret = null;
        final String versionKey = "\"version\"";
        char[] buf = new char[256];
        try {
            reader.read(buf);
        } catch (IOException e) {
            throw new JsonSyntaxException("JSON string can not be read", e);
        }
        String sbuf = new String(buf);
        LOGGER.trace("Header: " + sbuf);
        int pos = sbuf.indexOf(versionKey);
        if (pos < 0)
            throw new JsonSyntaxException("JSON file does not contain version tag");
        pos = sbuf.indexOf("\"", pos + versionKey.length());
        int pos2 = sbuf.indexOf("\"", pos + 1);
        String version = sbuf.substring(pos + 1, pos2);
        ret = convertStringVersion(version);
        return ret;
    }

    /**
     * Convert string in format a.b.c-SNAPSHOT to double a.bc
     * 
     * @param ver String version to convert
     * @return Double representation of version string
     * @throws JsonSyntaxException on wrong conversions (due to e.g. wrong wile read or bad
     *         structure)
     */
    private Double convertStringVersion(String ver) {
        String ret;
        try {
            // remove "" and other stuff
            ret = ver.replaceAll("([ \",]|-SNAPSHOT)", "");
            int dotcount = ret.length() - ret.replace(".", "").length();
            if (dotcount > 2)
                throw new JsonSyntaxException(
                        "Format of version string must follow rule major.minor.inc");
            if (dotcount == 2) {
                int seconddotpos = ret.lastIndexOf('.');
                ret = ret.substring(0, seconddotpos) + ret.substring(seconddotpos + 1);
            }
            return new Double(ret);
        } catch (NumberFormatException ex) {
            throw new JsonSyntaxException("Version string could not be converted to number", ex);
        }
    }

    /**
     * Register constructor for wrapped class.
     * 
     * It may be necessary during loading JSON file if wrapped class needs some parameters to
     * restore its state on uk.ac.warwick.wsbc.QuimP.IQuimpSerialize.afterSerialize() call and those
     * parameters are passed in constructor.
     * 
     * Example of use:
     * 
     * <pre>
     * <code>
     * class SnakePluginListInstanceCreator implements InstanceCreator<SnakePluginList> {
     *             private int size; private PluginFactory pf; private List<Point2d> dt; private
     *             ViewUpdater vu;
     *
     *             public SnakePluginListInstanceCreator(int size, final PluginFactory pf, final
     *             List<Point2d> dataToProcess, final ViewUpdater vu) { this.size = size; this.pf =
     *             pf; this.dt = dataToProcess; this.vu = vu; }
     *
     *             Serializer<SnakePluginList> out; Serializer<SnakePluginList> s = new
     *             Serializer<>(SnakePluginList.class);
     *             s.registerInstanceCreator(SnakePluginList.class, new
     *             SnakePluginListInstanceCreator(3, pluginFactory, null, null)); out =
     *             s.fromString(json);
     * </code>
     * </pre>
     * 
     * @param type Type of class
     * @param typeAdapter Wrapped object builder that implements InstanceCreator interface.
     * @see uk.ac.warwick.wsbc.QuimP.filesystem.IQuimpSerialize#afterSerialize()
     * @see <a href=
     *      "GSon doc">https://github.com/google/gson/blob/master/UserGuide.md#TOC-InstanceCreator-for-a-Parameterized-Type</a>
     */
    public void registerInstanceCreator(Class<T> type, Object typeAdapter) {
        gsonBuilder.registerTypeAdapter(type, typeAdapter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.reflect.ParameterizedType#getActualTypeArguments()
     */
    @Override
    public Type[] getActualTypeArguments() {
        return new Type[] { t };
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.reflect.ParameterizedType#getRawType()
     */
    @Override
    public Type getRawType() {
        return Serializer.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.reflect.ParameterizedType#getOwnerType()
     */
    @Override
    public Type getOwnerType() {
        return null;
    }

}

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
 * @author p.baniukiewicz
 * @date 31 Mar 2016
 *
 */
public class Serializer<T extends IQuimpSerialize> implements ParameterizedType {
    private static final Logger LOGGER = LogManager.getLogger(Serializer.class.getName());
    private transient GsonBuilder gsonBuilder;

    @SuppressWarnings("unused")
    private String className;
    @SuppressWarnings("unused")
    public String[] version;
    public T obj;

    public Serializer() {
        gsonBuilder = new GsonBuilder();
        obj = null;
        version = new String[0];
    }

    /**
     * 
     */
    public Serializer(T obj, String[] version) {
        this();
        this.obj = obj;
        className = obj.getClass().getSimpleName();
        this.version = version;
    }

    public void save(String filename) throws FileNotFoundException {
        Gson gson = gsonBuilder.create();
        if (obj != null)
            obj.beforeSerialize();
        LOGGER.debug("Saving at: " + filename);
        LOGGER.debug(gson.toJson(this));
        PrintWriter f;
        f = new PrintWriter(new File(filename));
        gson.toJson(this, f);
        f.close();
    }

    public Serializer<T> load(T t, String filename) throws IOException, Exception {
        Gson gson = gsonBuilder.create();
        FileReader f = new FileReader(new File(filename));
        Serializer<T> localref;
        localref = gson.fromJson(f, new Serializer<T>(t, new String[0]));
        f.close();
        localref.obj.afterSerialize();
        return localref;
    }

    public Serializer<T> fromString(T t, String json) throws Exception {
        Gson gson = gsonBuilder.create();
        Serializer<T> localref;
        localref = gson.fromJson(json, new Serializer<T>(t, new String[0]));
        localref.obj.afterSerialize();
        return localref;
    }

    public String toString() {
        Gson gson = gsonBuilder.create();
        if (obj != null)
            obj.beforeSerialize();
        return gson.toJson(this);
    }

    public void setPretty() {
        gsonBuilder.setPrettyPrinting();
    }

    public void registerInstanceCreator(Class<T> type, Object typeAdapter) {
        gsonBuilder.registerTypeAdapter(type, typeAdapter);
    }

    @Override
    public Type[] getActualTypeArguments() {
        return new Type[] { obj.getClass() };
    }

    @Override
    public Type getRawType() {
        return Serializer.class;
    }

    @Override
    public Type getOwnerType() {
        // TODO Auto-generated method stub
        return null;
    }

}

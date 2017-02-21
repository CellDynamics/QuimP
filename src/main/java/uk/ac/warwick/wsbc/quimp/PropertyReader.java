/**
 * 
 */
package uk.ac.warwick.wsbc.quimp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * Read properties from resources.
 * 
 * @author p.baniukiewicz
 *
 */
public class PropertyReader {

    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(PropertyReader.class.getName());

    /**
     * Default constructor
     */
    public PropertyReader() {
    }

    /**
     * Read property from property file for QuimP package.
     * 
     * not static because of getClass().Property file should be in the same package as this class or
     * full path should be provided otherwise.
     * 
     * @param propFileName property file name
     * @param propKey name of the key
     * @return value for propKey
     * @see <a href=
     *      "link">http://stackoverflow.com/questions/333363/loading-a-properties-file-from-java-package</a>
     * @see #readProperty(Class, String, String, String)
     */
    public String readProperty(String propFileName, String propKey) {
        InputStream inputStream;
        String result = "";
        Properties prop;

        prop = new Properties();
        inputStream = getClass().getResourceAsStream(propFileName);
        try {
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException(
                        "property file '" + propFileName + "' not found in the classpath");
            }
            result = prop.getProperty(propKey);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return result;
    }

    /**
     * Read property from jar file.
     * 
     * Used when there is many the same properties in many jars.
     * 
     * @param c
     * @param partofFilename A part of expected jar name where property is located.
     * @param propFileName
     * @param propKey
     * @return Value of property for <tt>propKey</tt>
     */
    static public String readProperty(Class<?> c, String partofFilename, String propFileName,
            String propKey) {
        String result = "";
        Properties prop;
        BufferedReader in = null;
        prop = new Properties();

        try {
            Enumeration<URL> resources = c.getClassLoader().getResources(propFileName);
            while (resources.hasMoreElements()) {
                URL reselement = resources.nextElement();
                LOGGER.trace("res " + reselement.toString() + " class " + c.getSimpleName());
                if (reselement.toString().contains(partofFilename)) {
                    in = new BufferedReader(new InputStreamReader(reselement.openStream()));
                }
                if (in != null) {
                    prop.load(in);
                    result = prop.getProperty(propKey);
                    LOGGER.trace("result " + result);
                }
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return result;
    }

}

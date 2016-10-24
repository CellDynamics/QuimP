/**
 * 
 */
package uk.ac.warwick.wsbc.QuimP;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read properties from resources.
 * 
 * @author p.baniukiewicz
 *
 */
public class PropertyReader {
    static final Logger LOGGER = LoggerFactory.getLogger(PropertyReader.class.getName());

    /**
     * Default constructor
     */
    public PropertyReader() {
    }

    /**
     * Read property from property file.
     * 
     * @param propFileName property file name
     * @param propKey name of the key
     * @return value for \a propKey
     * @remarks not static because of \a getClass().Property file should be in the same package as
     *          this class or full path should be provided otherwise.
     * @see http://stackoverflow.com/questions/333363/loading-a-properties-file-from-java-package
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

}

/**
 * @file ConfigReader.java
 * @date 29 Jan 2016
 */
package uk.ac.warwick.wsbc.helpers;

import java.io.FileReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Simple helper class for reading configuration files
 * 
 * The config file is in JSON format and contains two levels of data. On main
 * level there is name of plugin whereas on nested level there is a list of
 * named parameters for that plugin, e.g.
 * 
 * @code
 * {
 * "MeanFilter": { "window": "11" }
 * "LoesFilter": { "smooth": "1.1e-3" }
 * "HatFilter": { "mask": "23", "inner": "11", "sigma": "1e-4" }
 * }
 * @endcode
 * All parameters are named from small letters ant those names must be
 * known to class user. All values are provided as \b strings. To get
 * parameter user must call \c getParam method with proper names of
 * plugin and parameter. User must take care on providing correct names
 * and doing correct conversions. Example of use:
 * @code{.java}
 * // set params
 * ConfigReader cR = new ConfigReader(
 * System.getProperty("user.home")+System.getProperty(
 * "file.separator")+"plugin.json");
 * HashMap<String,Object> map = new HashMap<String,Object>(); map.put("window",
 * cR.getDoubleParam("MeanFilter", "window"));
 * @endcode
 * 
 * @author p.baniukiewicz
 * @date 29 Jan 2016
 *
 */
public class ConfigReader {

    private JSONObject rootObject;

    /**
     * Construct parser object
     * 
     * @param filename name of config file
     * 
     * @throws ConfigReaderException
     * On any error (syntax, file read, etc)
     */
    public ConfigReader(String filename) throws ConfigReaderException {
        JSONParser parser;
        try {
            parser = new JSONParser();
            rootObject = (JSONObject) parser.parse(new FileReader(filename));
        } catch (Exception e) {
            throw new ConfigReaderException(e);
        }
    }

    /**
     * Return raw (as \c String) parameter read from \a pluginName : \a
     * paramName
     * 
     * @param pluginName Name of the plugin listed in read configuration file
     * @param paramName Name of the plugin parameter listed in read
     * configuration file
     * @return String representation of parameter read from configuration file
     * @throws ConfigReaderException
     * On any error (usually bad names)
     */
    public String getRawParam(String pluginName, String paramName)
            throws ConfigReaderException {

        Object val = null;
        try {
            JSONObject plugin = (JSONObject) rootObject.get(pluginName);
            val = plugin.get(paramName);
        } catch (Exception e) {
            throw new ConfigReaderException(e);
        }
        if (val == null)
            throw new ConfigReaderException("Parameter not found");

        return (String) val;
    }

    /**
     * Return converted (as \c int) parameter read from \a pluginName : \a
     * paramName
     * 
     * USer should take care whether the conversion to \c int is possible
     * 
     * @param pluginName Name of the plugin listed in read configuration file
     * @param paramName Name of the plugin parameter listed in read
     * configuration file
     * @return integer representation of parameter read from configuration file
     * @throws ConfigReaderException
     * On any error (usually bad names or bad conversion)
     */
    public int getIntParam(String pluginName, String paramName)
            throws ConfigReaderException {
        int w;
        try {
            w = Integer.parseInt(getRawParam(pluginName, paramName));
        } catch (NumberFormatException e) {
            throw new ConfigReaderException(e);
        }
        return w;
    }

    /**
     * Return converted (as \c double) parameter read from \a pluginName : \a
     * paramName
     * 
     * USer should take care whether the conversion to \c double is possible
     * 
     * @param pluginName Name of the plugin listed in read configuration file
     * @param paramName Name of the plugin parameter listed in read
     * configuration file
     * @return double representation of parameter read from configuration file
     * @throws ConfigReaderException
     * On any error (usually bad names or bad conversion)
     */
    public double getDoubleParam(String pluginName, String paramName)
            throws ConfigReaderException {
        return Double.parseDouble(getRawParam(pluginName, paramName));
    }
}

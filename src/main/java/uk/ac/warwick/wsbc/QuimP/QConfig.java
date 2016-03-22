/**
 * @file QConfig.java
 * @date 22 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author p.baniukiewicz
 * @date 22 Mar 2016
 * @todo TODO serializable objects should implement own interface providing beforesave and aftersave
 *  methods
 */
public class QConfig {
    private static final Logger LOGGER = LogManager.getLogger(QConfig.class.getName());
    private String version;
    public static final String softwareName = "QuimP";
    public SnakePluginList activePluginList;

    /**
     * 
     */
    public QConfig(String version) {
        this.version = version;
    }

    /**
     * Do everything before save
     */
    private void beforeSave() {
        activePluginList.beforeSerialize();

    }

    private void afterLoad() {
        activePluginList.afterdeSerialize();
    }

    public void save(String filename) throws FileNotFoundException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        beforeSave();
        LOGGER.debug(gson.toJson(this));

        PrintWriter f;
        f = new PrintWriter(new File(filename));
        f.write(gson.toJson(this));
        f.close();
    }

}

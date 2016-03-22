/**
 * @file QConfig.java
 * @date 22 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP;

/**
 * @author p.baniukiewicz
 * @date 22 Mar 2016
 * @todo TODO serializable objects should implement own interface providing beforesave and aftersave
 *  methods
 */
public class QConfig {

    public String version;
    public String softwareName = "QuimP";
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
    public void beforeSave() {
        activePluginList.beforeSerialize();

    }

    public void afterLoad() {
        activePluginList.afterdeSerialize();
    }

}

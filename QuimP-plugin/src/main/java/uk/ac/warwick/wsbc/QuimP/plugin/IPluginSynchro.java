/**
 * @file IPluginSynchro.java
 * @date 4 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP.plugin;

import uk.ac.warwick.wsbc.QuimP.ViewUpdater;

/**
 * General interface that add synchronization between plugin and QuimP.
 * 
 * Plugin can demand refreshing screen in QuimP and redrawing all its context.
 * 
 * @author p.baniukiewicz
 * @date 4 Mar 2016
 *
 */
public interface IPluginSynchro {

    /**
     * Attach QuimP object and allows to call QuimP methods from class implementing this interface
     * 
     * @param b Reference to main QuimP object 
     */
    void attachContext(ViewUpdater b);

}

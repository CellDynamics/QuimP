/**
 */
package uk.ac.warwick.wsbc.QuimP;

import java.nio.file.Paths;

import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

/**
 * This class create instance of PluginFactory
 *
 * @author p.baniukiewicz
 *
 */
public class PluginFactoryFactory {
    private static final PluginFactoryFactory instance = new PluginFactoryFactory();

    public PluginFactoryFactory() {
        // TODO Auto-generated constructor stub
    }

    public static PluginFactoryFactory getInstance() {
        return instance;
    }

    /**
     * Provide mocked PluginFactory object that uses sources of plugins available on path
     * @param path
     * @return mocked PluginFactory object
     * @throws QuimpPluginException 
     */
    public static PluginFactory getPluginFactory(String path) throws QuimpPluginException {
        return new PluginFactory(Paths.get(path));

    }
}

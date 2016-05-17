/**
 * @file PluginFactoryFactory.java
 * @date 17 May 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.Mockito;

import quimp.plugin.HatSnakeFilter_;
import quimp.plugin.HedgehogSnakeFilter_;
import quimp.plugin.MeanSnakeFilter_;
import quimp.plugin.SetHeadSnakeFilter_;
import uk.ac.warwick.wsbc.QuimP.plugin.IQuimpPlugin;
import uk.ac.warwick.wsbc.QuimP.plugin.QuimpPluginException;

/**
 * This class mock PluginFactory class making possible to call plugins from local Eclipse workspace.
 * 
 * All plugins must be added to quimp build path as source reference. getMockedPluginFactory(String)
 * should be also filled.
 * 
 * @author p.baniukiewicz
 * @date 17 May 2016
 *
 */
public class PluginFactoryFactory {
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private static final Logger LOGGER = LogManager.getLogger(PluginFactoryFactory.class.getName());
    private static final PluginFactoryFactory instance = new PluginFactoryFactory();

    public PluginFactoryFactory() {
        // TODO Auto-generated constructor stub
    }

    public static PluginFactoryFactory getInstance() {
        return instance;
    }

    /**
     * Provide real PluginFactory object
     * 
     * @param path
     * @return real PluginFactory object
     * @throws QuimpPluginException
     */
    public static PluginFactory getPluginFactory(String path) throws QuimpPluginException {
        return new PluginFactory(Paths.get(path));
    }

    /**
     * Provide mocked PluginFactory object that uses sources of plugins avaiable on path
     * @param path
     * @return mocked PluginFactory object
     */
    public static PluginFactory getMockedPluginFactory(String path) {
        LOGGER.warn("Using mocked filters!!!!");
        //!<
        PluginFactory pluginFactory;
        pluginFactory = Mockito.mock(PluginFactory.class);
        Mockito.when(pluginFactory.getPluginNames(IQuimpPlugin.DOES_SNAKES))
                .thenReturn(new ArrayList<String>(Arrays.asList(
                        "HedgehogSnakeFilterMock",
                        "MeanSnakeFilterMock", 
                        "SetHeadSnakeFilterMock", 
                        "HatSnakeFilterMock")));
        
        Mockito.when(pluginFactory.getInstance("HedgehogSnakeFilterMock"))
                        .thenReturn(new HedgehogSnakeFilter_());
        Mockito.when(pluginFactory.getInstance("MeanSnakeFilterMock"))
                        .thenReturn(new MeanSnakeFilter_());
        Mockito.when(pluginFactory.getInstance("SetHeadSnakeFilterMock"))
                        .thenReturn(new SetHeadSnakeFilter_());
        Mockito.when(pluginFactory.getInstance("HatSnakeFilterMock"))
                        .thenReturn(new HatSnakeFilter_());
        

        HashMap<String, PluginProperties> availPlugins = new HashMap<String, PluginProperties>();
        availPlugins.put(
                "HedgehogSnakeFilterMock",
                new PluginProperties(new File("mocked"), "HedgehogSnakeFilter_",
                        IQuimpPlugin.DOES_SNAKES,
                        pluginFactory.getInstance("HedgehogSnakeFilterMock").getVersion()));
        availPlugins.put(
                "MeanSnakeFilterMock",
                new PluginProperties(new File("mocked"), "MeanSnakeFilter_",
                        IQuimpPlugin.DOES_SNAKES,
                        pluginFactory.getInstance("MeanSnakeFilterMock").getVersion()));
        availPlugins.put(
                "SetHeadSnakeFilterMock",
                new PluginProperties(new File("mocked"), "SetHeadSnakeFilter_",
                        IQuimpPlugin.DOES_SNAKES,
                        pluginFactory.getInstance("SetHeadSnakeFilterMock").getVersion()));
        availPlugins.put(
                "HatSnakeFilterMock",
                new PluginProperties(new File("mocked"), "HatSnakeFilterFilter_",
                        IQuimpPlugin.DOES_SNAKES,
                        pluginFactory.getInstance("HatSnakeFilterMock").getVersion()));

        Mockito.when(pluginFactory.getRegisterdPlugins()).thenReturn(availPlugins);
        return pluginFactory;
        /**/
    }
}

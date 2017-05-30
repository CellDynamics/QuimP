package com.github.celldynamics.quimp;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quimp.plugin.HatSnakeFilter_;
import quimp.plugin.HedgehogSnakeFilter_;
import quimp.plugin.MeanSnakeFilter_;
import quimp.plugin.SetHeadSnakeFilter_;
import com.github.celldynamics.quimp.plugin.IQuimpPlugin;

/**
 * This class mock PluginFactory class making possible to call plugins from local Eclipse workspace.
 * 
 * All plugins must be added to quimp build path as source reference. getPluginFactory(String)
 * should be also filled.
 *
 * <p>
 * <b>Warning</b>
 * <p>
 * This class is related to pom.xml as well. Notice that compilation will use current Working
 * Directory state for every plugin
 * 
 * @author p.baniukiewicz
 *
 */
public class PluginFactoryFactory {
    static final Logger LOGGER = LoggerFactory.getLogger(PluginFactoryFactory.class.getName());
    private static final PluginFactoryFactory instance = new PluginFactoryFactory();

    public PluginFactoryFactory() {
        // TODO Auto-generated constructor stub
    }

    public static PluginFactoryFactory getInstance() {
        return instance;
    }

    /**
     * Provide mocked PluginFactory object that uses sources of plugins avaiable on path
     * 
     * @param path
     * @return mocked PluginFactory object
     */
    public static PluginFactory getPluginFactory(String path) {
        LOGGER.warn("Using mocked filters!!!!");
        //!<
        PluginFactory pluginFactory;
        pluginFactory = Mockito.mock(PluginFactory.class);
        Mockito.when(pluginFactory.getPluginNames(IQuimpPlugin.DOES_SNAKES))
                .thenReturn(new ArrayList<String>(Arrays.asList("HedgehogSnakeFilterMock",
                        "MeanSnakeFilterMock", "SetHeadSnakeFilterMock", "HatSnakeFilterMock")));

        Mockito.when(pluginFactory.getInstance("HedgehogSnakeFilterMock"))
                .thenReturn(new HedgehogSnakeFilter_());
        Mockito.when(pluginFactory.getInstance("MeanSnakeFilterMock"))
                .thenReturn(new MeanSnakeFilter_());
        Mockito.when(pluginFactory.getInstance("SetHeadSnakeFilterMock"))
                .thenReturn(new SetHeadSnakeFilter_());
        Mockito.when(pluginFactory.getInstance("HatSnakeFilterMock"))
                .thenReturn(new HatSnakeFilter_());

        HashMap<String, PluginProperties> availPlugins = new HashMap<String, PluginProperties>();
        availPlugins.put("HedgehogSnakeFilterMock",
                new PluginProperties(new File("mocked"), "HedgehogSnakeFilter_",
                        IQuimpPlugin.DOES_SNAKES,
                        pluginFactory.getInstance("HedgehogSnakeFilterMock").getVersion()));
        availPlugins.put("MeanSnakeFilterMock",
                new PluginProperties(new File("mocked"), "MeanSnakeFilter_",
                        IQuimpPlugin.DOES_SNAKES,
                        pluginFactory.getInstance("MeanSnakeFilterMock").getVersion()));
        availPlugins.put("SetHeadSnakeFilterMock",
                new PluginProperties(new File("mocked"), "SetHeadSnakeFilter_",
                        IQuimpPlugin.DOES_SNAKES,
                        pluginFactory.getInstance("SetHeadSnakeFilterMock").getVersion()));
        availPlugins.put("HatSnakeFilterMock",
                new PluginProperties(new File("mocked"), "HatSnakeFilterFilter_",
                        IQuimpPlugin.DOES_SNAKES,
                        pluginFactory.getInstance("HatSnakeFilterMock").getVersion()));

        Mockito.when(pluginFactory.getRegisterdPlugins()).thenReturn(availPlugins);
        return pluginFactory;
        /**/
    }
}

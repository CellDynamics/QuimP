/**
 * @file DataContainer.java
 * @date 27 May 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;

/**
 * Contains parameter for the whole QuimP.
 * 
 * All modules can add here their configuration files. This structure is used for exchanging data
 * between modules. It can be dynamically modified
 * 
 * @author p.baniukiewicz
 * @date 27 May 2016
 *
 */
public class DataContainer implements IQuimpSerialize {

    public BOAState BOAState;
    public Outlines ECMMState;
    private transient PluginFactory pf;
    private transient ViewUpdater vu;

    public DataContainer() {
        this(new BOAState(null));
    }

    public DataContainer(BOAState bs) {
        this(bs, new Outlines());
    }

    public DataContainer(BOAState bs, Outlines os) {
        this.BOAState = bs;
        this.ECMMState = os;
    }

    public DataContainer(int size, final PluginFactory pf, final ViewUpdater vu) {
        this();
        this.pf = pf;
        this.vu = vu;
    }

    @Override
    public void beforeSerialize() {
        BOAState.beforeSerialize();
        ECMMState.beforeSerialize();

    }

    @Override
    public void afterSerialize() throws Exception {
        BOAState.snakePluginList = new SnakePluginList(BOA_.NUM_SNAKE_PLUGINS, pf, vu);
        BOAState.afterSerialize();
        ECMMState.afterSerialize();
        for (SnakePluginList sL : BOAState.snakePluginListSnapshots) {
            sL.updateRefs(pf, vu);
        }
    }
}

/**
 * Object builder for GSon and DataContainer class
 * 
 * This class is used on load JSon representation of DataContainer class. Rebuilds snakePluginList
 * field that is not serialized. This field keeps current state of plugins
 * 
 * @author p.baniukiewicz
 * @date 22 Mar 2016
 * @see GSon documentation
 */
class DataContainerInstanceCreator implements InstanceCreator<DataContainer> {

    private int size;
    private PluginFactory pf;
    private ViewUpdater vu;

    public DataContainerInstanceCreator(int size, final PluginFactory pf, final ViewUpdater vu) {
        this.size = size;
        this.pf = pf;
        this.vu = vu;
    }

    @Override
    public DataContainer createInstance(Type arg0) {
        DataContainer dt = new DataContainer(size, pf, vu);
        return dt;
    }
}

@Deprecated
class InstanceCreatorForB implements InstanceCreator<BOAState.BOAp> {
    private final BOAState a;

    public InstanceCreatorForB(BOAState a) {
        this.a = a;
    }

    public BOAState.BOAp createInstance(Type type) {
        return a.new BOAp();
    }
}

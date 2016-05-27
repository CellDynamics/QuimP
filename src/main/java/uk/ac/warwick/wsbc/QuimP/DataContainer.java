/**
 * @file DataContainer.java
 * @date 27 May 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import uk.ac.warwick.wsbc.QuimP.BOA_.BOAState;

/**
 * Contain parameter for the whole QuimP.
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

    public DataContainer(BOAState bs) {
        this.BOAState = bs;
    }

    @Override
    public void beforeSerialize() {
        BOAState.beforeSerialize();

    }

    @Override
    public void afterSerialize() throws Exception {
        BOAState.afterSerialize();
    }
}

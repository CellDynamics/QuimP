package uk.ac.warwick.wsbc.QuimP.filesystem;

import java.util.ArrayList;

import uk.ac.warwick.wsbc.QuimP.plugin.ana.ANAp;

/**
 * Serialization container for {@link uk.ac.warwick.wsbc.QuimP.plugin.ana.ANAp}.
 * 
 * @author p.baniukiewicz
 *
 */
public class ANAParamCollection implements IQuimpSerialize {
    public ArrayList<ANAp> aS;

    public ANAParamCollection() {
        aS = new ArrayList<>();
    }

    /**
     * Create <tt>size</tt> elements in array.
     * 
     * @param size
     */
    public ANAParamCollection(int size) {
        aS = new ArrayList<ANAp>(size);
        for (int i = 0; i < size; i++)
            aS.add(new ANAp());
    }

    @Override
    public void beforeSerialize() {

    }

    @Override
    public void afterSerialize() throws Exception {

    }

}

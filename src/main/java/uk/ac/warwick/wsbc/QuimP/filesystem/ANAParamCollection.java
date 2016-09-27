package uk.ac.warwick.wsbc.QuimP.filesystem;

import java.util.ArrayList;
import java.util.Collections;

import uk.ac.warwick.wsbc.QuimP.ANAp;
import uk.ac.warwick.wsbc.QuimP.IQuimpSerialize;

/**
 * Serialization container for {@link uk.ac.warwick.wsbc.QuimP.ANAp}.
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
        aS = new ArrayList<>(Collections.nCopies(size, new ANAp()));
    }

    @Override
    public void beforeSerialize() {

    }

    @Override
    public void afterSerialize() throws Exception {

    }

}

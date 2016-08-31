package uk.ac.warwick.wsbc.QuimP;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Serialization container for {@link uk.ac.warwick.wsbc.QuimP.ANAp}.
 * 
 * @author p.baniukiewicz
 *
 */
public class ANAStates implements IQuimpSerialize {
    public ArrayList<ANAp> aS;

    public ANAStates() {
        aS = new ArrayList<>();
    }

    /**
     * Create <tt>size</tt> elements in array.
     * 
     * @param size
     */
    public ANAStates(int size) {
        aS = new ArrayList<>(Collections.nCopies(size, new ANAp()));
    }

    @Override
    public void beforeSerialize() {

    }

    @Override
    public void afterSerialize() throws Exception {

    }

}

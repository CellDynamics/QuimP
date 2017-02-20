package uk.ac.warwick.wsbc.quimp.filesystem;

import java.util.ArrayList;

import uk.ac.warwick.wsbc.quimp.plugin.ana.ANAp;

// TODO: Auto-generated Javadoc
/**
 * Serialization container for {@link uk.ac.warwick.wsbc.quimp.plugin.ana.ANAp}.
 * 
 * @author p.baniukiewicz
 *
 */
public class ANAParamCollection implements IQuimpSerialize {
    /**
     * Array of configuration options for every cell present in the image.
     */
    public ArrayList<ANAp> aS;

    /**
     * Default constructor.
     * 
     * Create empty store for {@link ANAp} configurations.
     */
    public ANAParamCollection() {
        aS = new ArrayList<>();
    }

    /**
     * Create <tt>size</tt> elements in store for {@link ANAp} configurations.
     * 
     * Size of the store usually equals to the number of cells in the image.
     * 
     * @param size
     */
    public ANAParamCollection(int size) {
        aS = new ArrayList<ANAp>(size);
        for (int i = 0; i < size; i++)
            aS.add(new ANAp());
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize#beforeSerialize()
     */
    @Override
    public void beforeSerialize() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize#afterSerialize()
     */
    @Override
    public void afterSerialize() throws Exception {

    }

}

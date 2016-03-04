/**
 * @file ViewUpdater.java
 * @date 4 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP;

/**
 * Accessor that masks all public methods from object it holds except those allowed to be exposed.
 * 
 * This class is used for limiting access to public methods of QuimP from external plugins. It 
 * prevents calling those methods in unchecked way.
 *  
 * @author p.baniukiewicz
 * @date 4 Mar 2016
 *
 */
public class ViewUpdater {

    private Object o;

    /**
     * Connect object to accessor.
     * @param o Object
     */
    public ViewUpdater(Object o) {
        this.o = o;
    }

    /**
     * Calls updateView method from object \c o
     */
    public void updateView() {
        if (o instanceof BOA_) {
            ((BOA_) o).updateView();
        } else
            throw new RuntimeException("Class not supported");
    }

}

/**
 * @file IQuimpSerialize.java
 * @date 31 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP;

/**
 * Interface that is required by Serializer class
 * 
 * Serializer class calls methods from this interface before and after serialization. The main
 * purpose of these method is to allow to rebuild objects after restoration or to prepare them 
 * before saving. 
 * 
 * @author p.baniukiewicz
 * @date 31 Mar 2016
 *
 */
public interface IQuimpSerialize {
    /**
     * This method is called just before JSON is generated.
     * 
     * @see uk.ac.warwick.wsbc.QuimP.Serializer.save(final String)
     */
    public void beforeSerialize();

    /**
     * This method is called after restoring object from JSON but before returning the object.
     * 
     * @throws Exception from wrapped object in any problem. This is implementation dependent
     * @see uk.ac.warwick.wsbc.QuimP.Serializer.load(final String) 
     */
    public void afterSerialize() throws Exception;

}

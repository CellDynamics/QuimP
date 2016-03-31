/**
 * @file IQuimpSerialize.java
 * @date 31 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP;

/**
 * @author p.baniukiewicz
 * @date 31 Mar 2016
 *
 */
public interface IQuimpSerialize {
    public void beforeSerialize();

    public void afterSerialize() throws Exception;

}

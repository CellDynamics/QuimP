package uk.ac.warwick.wsbc.quimp.filesystem.versions;

import uk.ac.warwick.wsbc.quimp.QuimpException;
import uk.ac.warwick.wsbc.quimp.Serializer;
import uk.ac.warwick.wsbc.quimp.filesystem.IQuimpSerialize;

/**
 * This interface provides method that is run on loaded class reference when current tool version is
 * higher than version of loaded object.
 * 
 * <p>Class implementing this object should extends {@link IQuimpSerialize}
 * 
 * @author p.baniukiewicz
 * @param <T> Type of serialised object
 * @see Serializer
 */
public interface IQconfOlderConverter<T extends IQuimpSerialize> {

  /**
   * This method is run when deserialised class is in version lower that version of loader.
   * 
   * @param localref reference to loaded object
   * @throws QuimpException on problems with conversion
   */
  public void upgradeFromOld(Serializer<T> localref) throws QuimpException;

  /**
   * Threshold to execture converter.
   * 
   * <p>Converter is executed if version returned by this method is lower than that provided in
   * {@link Serializer#Serializer(IQuimpSerialize, uk.ac.warwick.wsbc.quimp.QuimpVersion)}
   * constructor.
   * 
   * @return Version for which this Converter. Format returned: a.bc if version string equals
   *         a.b.c-xxx.
   */
  public Double executeForLowerThan();

}

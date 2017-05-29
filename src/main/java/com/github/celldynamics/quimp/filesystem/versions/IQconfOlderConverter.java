package com.github.celldynamics.quimp.filesystem.versions;

import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.Serializer;
import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;

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
   * {@link Serializer#Serializer(IQuimpSerialize, com.github.celldynamics.quimp.QuimpVersion)}
   * constructor.
   * 
   * @return Version for which this Converter. Format returned: a.bc if version string equals
   *         a.b.c-xxx.
   */
  public Double executeForLowerThan();

}

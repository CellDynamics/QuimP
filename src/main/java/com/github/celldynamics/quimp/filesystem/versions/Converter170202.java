package com.github.celldynamics.quimp.filesystem.versions;

import com.github.celldynamics.quimp.QuimpException;
import com.github.celldynamics.quimp.QuimpVersion;
import com.github.celldynamics.quimp.Serializer;
import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;

/**
 * Converts QCONF files saved in versions lower than 17.02.02.
 * 
 * <p>Replaces version tag from String[3] to {@link QuimpVersion} - current version of QuimP.
 * 
 * @author p.baniukiewicz
 * @param <T> Type of serialised class
 * @see com.github.celldynamics.quimp.QParamsQconf#readParams()
 *
 */
public class Converter170202<T extends IQuimpSerialize> implements IQconfOlderConverter<T> {

  /**
   * Version below the {@link #upgradeFromOld(Serializer)} will be executed.
   */
  public final Double trigger = new Double(17.0205);
  private QuimpVersion version;

  /**
   * @param version new version format. The old version will be replaced with this one
   */
  public Converter170202(QuimpVersion version) {
    this.version = version;
  }

  @Override
  public void upgradeFromOld(Serializer<T> localref) throws QuimpException {
    if (localref.timeStamp == null) {
      localref.timeStamp = version;
    }
  }

  @Override
  public Double executeForLowerThan() {
    return trigger;
  }

}

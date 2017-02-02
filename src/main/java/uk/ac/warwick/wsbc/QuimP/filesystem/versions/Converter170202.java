package uk.ac.warwick.wsbc.QuimP.filesystem.versions;

import uk.ac.warwick.wsbc.QuimP.QuimpException;
import uk.ac.warwick.wsbc.QuimP.QuimpVersion;
import uk.ac.warwick.wsbc.QuimP.Serializer;
import uk.ac.warwick.wsbc.QuimP.filesystem.IQuimpSerialize;

/**
 * Converts QCONF files saved in versions lower than 17.02.02.
 * 
 * Replaces version tag from String[3] to {@link QuimpVersion}
 * 
 * @author p.baniukiewicz
 * @param <T> Type of serialised class
 *
 */
public class Converter170202<T extends IQuimpSerialize> implements IQconfOlderConverter<T> {

    private QuimpVersion version;

    /**
     * @param version new version format
     */
    public Converter170202(QuimpVersion version) {
        this.version = version;
    }

    @Override
    public void upgradeFromOld(Serializer<T> localref) throws QuimpException {
        if (localref.timeStamp == null)
            localref.timeStamp = version;
    }

    @Override
    public Double executeForLowerThan() {
        return new Double(17.0202);
    }

}

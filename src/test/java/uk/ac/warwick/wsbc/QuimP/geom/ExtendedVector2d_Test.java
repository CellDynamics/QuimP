package uk.ac.warwick.wsbc.QuimP.geom;

import java.util.ArrayList;

import javax.vecmath.Vector2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Test;

/**
 * Simple testing class for ExtendedVector2d class
 * 
 * @remarks Will be removed in final version
 * @todo Remove this class in future
 * @author p.baniukiewicz
 *
 */
public class ExtendedVector2d_Test {

    // http://stackoverflow.com/questions/21083834/load-log4j2-configuration-file-programmatically
    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger logger =
            LogManager.getLogger(ExtendedVector2d_Test.class.getName());

    /**
     * @test test toString() method
     * @post Content of List in log file
     */
    @Test
    public void test_ToString() {
        ArrayList<ExtendedVector2d> v = new ArrayList<ExtendedVector2d>();
        ExtendedVector2d vv = new ExtendedVector2d(3.14, -5.6);
        v.add(new ExtendedVector2d(0, 0));
        v.add(new ExtendedVector2d(10, 10));
        v.add(new ExtendedVector2d(3.14, -4.56));
        logger.debug("vector " + vv.toString());
        logger.debug("V1 vector: " + v.toString());
    }

    /**
     * @test casting of {@link ExtendedVector2d} to javax.vecmath.Vector2d
     */
    @Test
    public void test_Casting() {
        Vector2d v = new ExtendedVector2d(10, 10);
        logger.debug("Casting: " + v.toString());

        Vector2d v1 = new Vector2d(5, 5);
        ExtendedVector2d ev1 = new ExtendedVector2d(v1);
        logger.debug("Casting1: " + ev1.toString());
    }
}

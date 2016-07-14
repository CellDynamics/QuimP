/**
 * @file SnakeHandlerTest.java
 * @date 29 Apr 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.FloatPolygon;

/**
 * @author p.baniukiewicz
 * @date 29 Apr 2016
 *
 */
public class SnakeHandlerTest {

    static {
        if (System.getProperty("quimp.debugLevel") == null)
            Configurator.initialize(null, "log4j2_default.xml");
        else
            Configurator.initialize(null, System.getProperty("quimp.debugLevel"));
    }
    private static final Logger LOGGER = LogManager.getLogger(SnakeHandlerTest.class.getName());

    private SnakeHandler sH;
    private String[] info = { "QuimP", "verr", "ddd" };

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        BOA_.qState = new BOAState(null);
        BOA_.qState.boap.FRAMES = 20;
        float x[] = new float[4];
        float y[] = new float[4];
        x[0] = 0;
        y[0] = 0;
        x[1] = 10;
        y[1] = 0;
        x[2] = 10;
        y[2] = 10;
        x[3] = 0;
        y[3] = 10;
        PolygonRoi pr1 = new PolygonRoi(new FloatPolygon(x, y), Roi.POLYGON);
        x[0] = 10;
        y[0] = 10;
        x[1] = 20;
        y[1] = 10;
        x[2] = 20;
        y[2] = 20;
        x[3] = 10;
        y[3] = 20;
        PolygonRoi pr2 = new PolygonRoi(new FloatPolygon(x, y), Roi.POLYGON);

        sH = new SnakeHandler(pr1, 3, 1);
        sH.storeLiveSnake(3);
        Snake s = sH.getLiveSnake();
        s.getHead().getPoint().setX(30);
        sH.storeLiveSnake(4);

    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        sH = null;
    }

    @Test
    public void testSerializeSnakeHandler_1() throws Exception {
        Serializer<SnakeHandler> serializer;
        serializer = new Serializer<>(sH, info);
        serializer.setPretty();
        serializer.save("/tmp/snakehandler1.tmp");
    }

    @Test
    public void testSnakeHandlerToOutline() {
        OutlineHandler oH = new OutlineHandler(sH);
        assertThat(oH.getSize(), is(sH.endFrame - sH.getStartFrame() + 1));
        LOGGER.debug(sH.getStoredSnake(3).toString());
        LOGGER.debug(oH.getOutline(3).toString());
    }

}

/**
 * @file ViewUpdater.java
 * @date 4 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Accessor that masks all public methods from object it holds except those manually exposed.
 * 
 * This class is used for limiting access to public methods of QuimP from external plugins. It 
 * prevents calling those methods in unchecked way.
 * 
 * Support bi-directional communication. Plugin can call:
 * -# updateView() for updating view (and recalculating all plugins)
 * -# getSnakeasXX() for current snake (only for previewing purposes).
 *  
 * @author p.baniukiewicz
 * @date 4 Mar 2016
 *
 */
public class ViewUpdater {
    private static final Logger LOGGER = LogManager.getLogger(ViewUpdater.class.getName());
    private Object o;
    private Snake snake; //!< Hold one snake from main view that can be requested by plugin

    /**
     * Connect object to accessor.
     * @param o Object
     */
    public ViewUpdater(Object o) {
        this.o = o;
    }

    /**
     * Connect current snake (on current frame) to this object. 
     * 
     * Connected snake can be requested by plugins (always as copy)
     * 
     * @param snake Snake to be connected.
     */
    protected void connectSnakeObject(final Snake snake) {
        LOGGER.trace("Remembered snake: " + snake.getSnakeID());
        this.snake = snake;
    }

    /**
     * Calls updateView method from object \c o
     */
    public void updateView() {
        if (o instanceof BOA_) {
            ((BOA_) o).recalculatePlugins();
        } else
            throw new RuntimeException("Class not supported");
    }

    /**
     * Request copy of connected snake for previewing purposes
     * 
     * @return copy of connected snake as list of points or empty list if snake is not connected
     */
    public List<Point2d> getSnakeasPoints() {
        if (snake != null)
            return snake.asList();
        else
            return new ArrayList<Point2d>();
    }

    /**
     * Request copy of connected snake for previewing purposes
     * 
     * @return copy of connected snake as Snake or null on any error
     */
    public Snake getSnakeasSnake() {
        Snake ret = null;
        try {
            if (snake != null)
                ret = new Snake(snake, snake.getSnakeID());
        } catch (BoaException e) {
            ret = null;
        }
        return ret;
    }

}

package com.github.celldynamics.quimp;

import java.util.List;

import org.scijava.vecmath.Point2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accessor that masks all public methods from object it holds except those manually exposed.
 * 
 * <p>This class is used for limiting access to public methods of QuimP from external plugins. It
 * prevents calling those methods in unchecked way.
 * 
 * <p>Support bi-directional communication. Plugin can call: -# updateView() for updating view (and
 * recalculating all plugins) -# getSnakeasXX() for current snake (only for previewing purposes).
 * 
 * @author p.baniukiewicz
 *
 */
public class ViewUpdater {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(ViewUpdater.class.getName());
  private Object ob;
  private Snake snake; //!< Hold one snake from main view that can be requested by plugin

  /**
   * Connect object to accessor.
   * 
   * @param ob Object
   */
  public ViewUpdater(Object ob) {
    this.ob = ob;
  }

  /**
   * Connect current snake (on current frame) to this object.
   * 
   * <p>Connected snake can be requested by plugins (always as copy)
   * 
   * @param snake Snake to be connected. I can be null when user e.g. deleted last object
   */
  protected void connectSnakeObject(final Snake snake) {
    if (snake != null) {
      LOGGER.trace("Remembered snake: " + snake.getSnakeID());
    } else {
      LOGGER.trace("Remembered snake: " + "null");
    }
    this.snake = snake;
  }

  /**
   * Calls updateView method from object ob.
   * 
   * <p>Also stores current plugin configuration locally in QuimP.
   * 
   * @see SnakePluginList
   */
  public void updateView() {
    if (ob instanceof BOA_) {
      ((BOA_) ob).recalculatePlugins();
    } else {
      throw new RuntimeException("Class not supported");
    }
  }

  /**
   * Request copy of connected snake for previewing purposes.
   * 
   * @return copy of connected snake as list of points or empty list if snake is not connected
   */
  public List<Point2d> getSnakeasPoints() {
    if (snake != null) {
      return snake.asList();
    } else {
      return null;
    }
  }

  /**
   * Request copy of connected snake for previewing purposes.
   * 
   * @return copy of connected snake as Snake
   */
  public Snake getSnakeasSnake() {
    Snake ret = null;
    if (snake != null) {
      ret = new Snake(snake, snake.getSnakeID());
    }
    return ret;
  }

}

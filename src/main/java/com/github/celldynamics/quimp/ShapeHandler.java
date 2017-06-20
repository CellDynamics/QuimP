package com.github.celldynamics.quimp;

/**
 * Base class for Shape holders.
 * 
 * @author p.baniukiewicz
 *
 * @param <T> Any class derived from Shape, currently Snake or Outline
 */
public abstract class ShapeHandler<T extends Shape<?>> {
  /**
   * First frame of Outline.
   * 
   * <p>Start and End frame fields are filled on Snake/Outline creation in handler. If segmentation
   * is Successful the {@link Snake}/{@link Outline} is created from current frame to last one in
   * stack. Therefore {@link Snake}/{@link Outline} exist between <tt>startFrame</tt> and
   * <tt>endFrame</tt>.
   * 
   * <p>If {@link Snake}/{@link Outline} is deleted {@link SnakeHandler#deleteStoreAt(int)} the
   * fields <tt>startFrame</tt> and <tt>endFrame</tt> are not updated (user can delete middle
   * Snake breaking continuity). This is why {@link SnakeHandler#isStoredAt(int)} should be used
   * to verify whether there is valid object on frame.
   */
  protected int startFrame;
  /**
   * Last frame of Outline.
   */
  protected int endFrame;

  /**
   * Instantiates a new shape handler.
   */
  ShapeHandler() {
    startFrame = 0;
    endFrame = 0;
  }

  /**
   * Copy constructor for this class.
   * 
   * @param src source object
   */
  ShapeHandler(ShapeHandler<T> src) {
    startFrame = src.startFrame;
    endFrame = src.endFrame;
  }

  /**
   * Gets the start frame.
   * 
   * @return the startFrame
   */
  public int getStartFrame() {
    return startFrame;
  }

  /**
   * Gets the end frame.
   * 
   * @return the endFrame
   */
  public int getEndFrame() {
    return endFrame;
  }
}

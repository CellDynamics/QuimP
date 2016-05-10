package uk.ac.warwick.wsbc.QuimP;

/**
 * Base class for Shape holders
 * 
 * @author p.baniukiewicz
 * @date 21 Apr 2016
 *
 * @param <T> Any class derived from Shape, currently Snake or Outline
 */
public abstract class ShapeHandler<T extends Shape<?>> {
    /**
     * Start and End frame fields are filled on Snake creation in handler. If segmentation is
     * Successful the Snake is created from current frame to last one in stack. Therefore Snake
     * exist between \c startFrame and \c endFrame.
     * But if the Snake is deleted (uk.ac.warwick.wsbc.QuimP.SnakeHandler.deleteStoreAt(int))
     * the fields \c startFrame and \c endFrame are not updated (user can delete middle Snake 
     * breaking continuity). This is why uk.ac.warwick.wsbc.QuimP.SnakeHandler.isStoredAt(int)
     * should be used to verify if there is valid Snake object on frame. 
     */
    protected int startFrame;
    protected int endFrame;
}

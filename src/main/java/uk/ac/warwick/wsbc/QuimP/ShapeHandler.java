package uk.ac.warwick.wsbc.QuimP;

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
     * Start and End frame fields are filled on Snake/Outline creation in handler. If segmentation
     * is Successful the {@link Snake}/{@link Outline} is created from current frame to last one in
     * stack. Therefore {@link Snake}/{@link Outline} exist between <tt>startFrame</tt> and
     * <tt>endFrame</tt>.
     * 
     * If {@link Snake}/{@link Outline} is deleted {@link SnakeHandler#deleteStoreAt(int)} the
     * fields <tt>startFrame</tt> and <tt>endFrame</tt> are not updated (user can delete middle
     * Snake breaking continuity). This is why {@link SnakeHandler#isStoredAt(int)} should be used
     * to verify whether there is valid object on frame.
     */
    protected int startFrame;
    /**
     * Last frame of Outline.
     */
    protected int endFrame;

    ShapeHandler() {
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
}

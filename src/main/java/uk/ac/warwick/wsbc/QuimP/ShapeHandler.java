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
    protected int startFrame;
    protected int endFrame;
}

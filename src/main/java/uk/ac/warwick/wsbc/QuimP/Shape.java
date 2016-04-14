package uk.ac.warwick.wsbc.QuimP;

/**
 * Forms shape from bidirectional list of points.
 * 
 * This abstract class keeps head point of Shape and control number of points in Shape, allows for
 * inserting points to the Shape
 *   
 * @author p.baniukiewicz
 * @date 14 Apr 2016
 *
 * @param <T> Type of point, currently can be Node or Vert
 */
public abstract class Shape<T extends PointListNode<T>> {
    protected int nextTrackNumber = 1; /*!< next node ID's */
    protected T head; /*!< first node in double linked list, always maintained */
    protected int POINTS; /*!< number of points TODO use directly without setter and getter maybe */

    /**
     * Default constructor, creates empty Shape
     */
    public Shape() {
        POINTS = 0;
        head = null;
    }

    /**
     * Creates Shape from existing list of points (can be one point as well)
     * 
     * @param h head point of the list
     * @param N number of points in the list 
     */
    public Shape(T h, int N) {
        head = h;
        POINTS = N;
        nextTrackNumber = N + 1;
    }

    /**
     * Creates Shape from one point, created Shape is looped
     * 
     * @param h head point of the list
     */
    public Shape(T h) {
        this(h, 1);
        head.setHead(true);
        head.setNext(head);
        head.setPrev(head);
        nextTrackNumber = head.getTrackNum() + 1;
    }

    /**
     * Get head of current Shape
     * 
     * @return Point representing head of Shape
     */
    public T getHead() {
        return head;
    }

    /**
     * Insert point \c ne after point \c n
     */
    public T insertPoint(final T n, final T ne) {
        T newNode = ne;
        ne.setTrackNum(nextTrackNumber++);
        newNode.setNext(n.getNext());
        newNode.setPrev(n);
        n.getNext().setPrev(newNode);
        n.setNext(newNode);
        POINTS++;
        return newNode;
    }

    /**
     * Remove selected point from list Check if removed point was head and if it
     * was, the new head is randomly selected
     * 
     * @param n point to remove
     * @param inner direction of normal vectors of Shape
     */
    public void removePoint(T n, boolean inner) {
        // removes node n and links neighbours together
        n.getPrev().setNext(n.getNext());
        n.getNext().setPrev(n.getPrev());

        // if removing head randomly assign a neighbour as new head
        if (n.isHead()) {
            if (Math.random() <= 1.0) {
                head = n.getNext();
            } else {
                head = n.getPrev();
            }
            head.setHead(true);
        }

        POINTS--;

        n.getPrev().updateNormale(inner);
        n.getNext().updateNormale(inner);
        n = null; // FIXME Does it have meaning here?
    }

    /**
     * Get number of points in Shape
     * 
     * @return Number of points
     */
    public int getNumPoints() {
        return POINTS;
    }

}

package uk.ac.warwick.wsbc.QuimP;

public abstract class Shape<T extends BiListofPoints<T>> {
    protected int nextTrackNumber = 1; // node ID's
    protected T head; // first node in double linked list, always maintained
    protected int POINTS; // number of points TODO use directly without setter and getter maybe

    public Shape() {
        POINTS = 0;
    }

    public Shape(T h, int N) {
        head = h;
        POINTS = N;
        nextTrackNumber = N + 1;
    }

    public Shape(T h) {
        this(h, 1);
        head.setHead(true);
        head.setNext(head);
        head.setPrev(head);
        nextTrackNumber = head.getTrackNum() + 1;
    }

    /**
     * Get head of current Snake
     * 
     * @return Point representing head of Shape
     */
    public T getHead() {
        return head;
    }

    /**
     * Insert node \c ne after node \c n
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
     * Remove selected node from list Check if removed node was head and if it
     * was, the new head is randomly selected
     * 
     * @param n Node to remove
     * 
     * @throws Exception
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

    public int getNumPoints() {
        return POINTS;
    }

}

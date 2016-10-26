package uk.ac.warwick.wsbc.QuimP.utils;

/**
 * Basic implementation of Pair.
 * 
 * @author p.baniukiewicz
 *
 */
public class Pair<K, V> {
    public K first;
    public V second;

    public Pair(K left, V right) {
        this.first = left;
        this.second = right;
    }

    /**
     * Just instance creator.
     * 
     * @param left
     * @param right
     * @return New instance of Pair.
     */
    public static <K, V> Pair<K, V> createPair(K left, V right) {
        return new Pair<K, V>(left, right);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pair<?, ?> other = (Pair<?, ?>) obj;
        if (first == null) {
            if (other.first != null)
                return false;
        } else if (!first.equals(other.first))
            return false;
        if (second == null) {
            if (other.second != null)
                return false;
        } else if (!second.equals(other.second))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Pair [left=" + first + ", right=" + second + "]";
    }

}

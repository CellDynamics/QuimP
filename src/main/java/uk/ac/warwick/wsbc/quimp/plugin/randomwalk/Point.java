package uk.ac.warwick.wsbc.quimp.plugin.randomwalk;

// TODO: Auto-generated Javadoc
/**
 * Basic class for storing point in Cartesian system
 * 
 * @author p.baniukiewicz
 *
 */
public class Point {

  /**
   * The col.
   */
  int row, col;

  /**
   * @param row
   * @param col
   */
  public Point(int col, int row) {
    this.row = row;
    this.col = col;
  }

  /**
   * Default constructor
   */
  Point() {
    row = 0;
    col = 0;
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
    result = prime * result + col;
    result = prime * result + row;
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
    Point other = (Point) obj;
    if (col != other.col)
      return false;
    if (row != other.row)
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
    return "Point [row=" + row + ", col=" + col + "]";
  }

}
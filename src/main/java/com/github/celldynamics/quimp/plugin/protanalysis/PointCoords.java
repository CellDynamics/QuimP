package com.github.celldynamics.quimp.plugin.protanalysis;

import java.awt.Point;

/**
 * Keep coordinates of point selected by user with frame and object number.
 * 
 * @author p.baniukiewicz
 *
 */
class PointCoords {

  /**
   * Convenience constructor called from {@link CustomCanvas}.
   * 
   * <p>Do not set frame.
   * 
   * @param point selected point
   * @param cellNo cell number
   */
  public PointCoords(Point point, int cellNo) {
    this.point = point;
    this.cellNo = cellNo;
  }

  /**
   * Construct full coordinates: x,y,cellNo,frame.
   * 
   * @param point selected point
   * @param cellNo cell number
   * @param frame frame number
   */
  public PointCoords(Point point, int cellNo, int frame) {
    this(point, cellNo);
    this.frame = frame;
  }

  /**
   * Screen coordinates of point selected by user.
   */
  public Point point;
  /**
   * Frame (0-indexed).
   */
  public int frame = -1;
  /**
   * Index of cell in {@link Prot_Analysis#outlines}.
   */
  public int cellNo;

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + cellNo;
    result = prime * result + frame;
    result = prime * result + ((point == null) ? 0 : point.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PointCoords other = (PointCoords) obj;
    if (cellNo != other.cellNo) {
      return false;
    }
    if (frame != other.frame) {
      return false;
    }
    if (point == null) {
      if (other.point != null) {
        return false;
      }
    } else if (!point.equals(other.point)) {
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "PointCoords [point=" + point + ", frame=" + frame + ", cellNo=" + cellNo + "]";
  }

}

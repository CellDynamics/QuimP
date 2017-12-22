package com.github.celldynamics.quimp;

import java.io.FileWriter;
import java.io.PrintWriter;

import com.github.celldynamics.quimp.geom.ExtendedVector2d;

import ij.process.ImageProcessor;

/**
 * Calculate forces that affect the snake. Active Contour segmentation.
 * 
 * @author rtyson
 */
public class Constrictor {

  /**
   * Default constructor.
   */
  public Constrictor() {
  }

  /**
   * Compute force power.
   * 
   * @param snake Processed snake
   * @param ip Original image
   * @return status of snake (true if it is frozen)
   */
  public boolean constrict(final Snake snake, final ImageProcessor ip) {

    ExtendedVector2d tempF; // temp vectors for forces
    ExtendedVector2d tempV = new ExtendedVector2d();

    Node n = snake.getHead();
    do { // compute F_total

      if (!n.isFrozen()) {

        // compute F_central
        tempV.setX(n.getNormal().getX() * BOA_.qState.segParam.f_central);
        tempV.setY(n.getNormal().getY() * BOA_.qState.segParam.f_central);
        n.setF_total(tempV);

        // compute F_contract
        tempF = contractionForce(n);
        tempV.setX(tempF.getX() * BOA_.qState.segParam.f_contract);
        tempV.setY(tempF.getY() * BOA_.qState.segParam.f_contract);
        n.addF_total(tempV);

        // compute F_image and F_friction
        tempF = imageForce(n, ip);
        tempV.setX(tempF.getX() * BOA_.qState.segParam.f_image);// - n.getVel().getX() *
        // boap.f_friction);
        tempV.setY(tempF.getY() * BOA_.qState.segParam.f_image);// - n.getVel().getY() *
        // boap.f_friction);
        n.addF_total(tempV);

        // compute new velocities of the node
        tempV.setX(BOA_.qState.boap.delta_t * n.getF_total().getX());
        tempV.setY(BOA_.qState.boap.delta_t * n.getF_total().getY());
        n.addVel(tempV);

        // store the prelimanary point to move the node to
        tempV.setX(BOA_.qState.boap.delta_t * n.getVel().getX());
        tempV.setY(BOA_.qState.boap.delta_t * n.getVel().getY());
        n.setPrelim(tempV);

        // add some friction
        n.getVel().multiply(BOA_.qState.boap.f_friction);

        // freeze node if vel is below velCrit
        if (n.getVel().length() < BOA_.qState.segParam.vel_crit) {
          snake.freezeNode(n);
        }
      }
      n = n.getNext(); // move to next node
    } while (!n.isHead()); // continue while have not reached tail

    // update all nodes to new positions
    n = snake.getHead();
    do {
      n.update(); // use preliminary variables
      n = n.getNext();
    } while (!n.isHead());

    snake.updateNormals(BOA_.qState.segParam.expandSnake);

    return snake.isFrozen(); // true if all nodes frozen
  }

  /**
   * constrictWrite.
   * 
   * @param snake snake
   * @param ip ip
   * @return true on success
   * @deprecated Strictly related to absolute paths on disk. Probably for testing purposes only.
   */
  public boolean constrictWrite(final Snake snake, final ImageProcessor ip) {
    // for writing forces at each frame
    try {
      PrintWriter pw = new PrintWriter(
              new FileWriter("/Users/rtyson/Documents/phd/tmp/test/forcesWrite/forces.txt"), true);
      ExtendedVector2d tempF; // temp vectors for forces
      ExtendedVector2d tempV = new ExtendedVector2d();

      Node n = snake.getHead();
      do { // compute F_total

        // if (!n.isFrozen()) {

        // compute F_central
        tempV.setX(n.getNormal().getX() * BOA_.qState.segParam.f_central);
        tempV.setY(n.getNormal().getY() * BOA_.qState.segParam.f_central);
        pw.print("\n" + n.getTrackNum() + "," + tempV.length() + ",");
        n.setF_total(tempV);

        // compute F_contract
        tempF = contractionForce(n);
        if (n.getCurvatureLocal() > 0) {
          pw.print(tempF.length() + ",");
        } else {
          pw.print((tempF.length() * -1) + ",");
        }
        tempV.setX(tempF.getX() * BOA_.qState.segParam.f_contract);
        tempV.setY(tempF.getY() * BOA_.qState.segParam.f_contract);
        n.addF_total(tempV);

        // compute F_image and F_friction
        tempF = imageForce(n, ip);
        pw.print((tempF.length() * -1) + ",");
        tempV.setX(tempF.getX() * BOA_.qState.segParam.f_image);// - n.getVel().getX()*
        // boap.f_friction);
        tempV.setY(tempF.getY() * BOA_.qState.segParam.f_image);// - n.getVel().getY()*
        // boap.f_friction);
        n.addF_total(tempV);
        pw.print(n.getF_total().length() + "");

        // compute new velocities of the node
        tempV.setX(BOA_.qState.boap.delta_t * n.getF_total().getX());
        tempV.setY(BOA_.qState.boap.delta_t * n.getF_total().getY());
        n.addVel(tempV);

        // add some friction
        n.getVel().multiply(BOA_.qState.boap.f_friction);

        // store the prelimanary point to move the node to
        tempV.setX(BOA_.qState.boap.delta_t * n.getVel().getX());
        tempV.setY(BOA_.qState.boap.delta_t * n.getVel().getY());
        n.setPrelim(tempV);

        // freeze node if vel is below velCrit
        if (n.getVel().length() < BOA_.qState.segParam.vel_crit) {
          snake.freezeNode(n);
        }
        // }
        n = n.getNext(); // move to next node
      } while (!n.isHead()); // continue while have not reached tail

      // update all nodes to new positions
      n = snake.getHead();
      do {
        n.update(); // use preliminary variables
        n = n.getNext();
      } while (!n.isHead());

      snake.updateNormals(BOA_.qState.segParam.expandSnake);

      pw.close();
      return snake.isFrozen(); // true if all nodes frozen
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Contraction force for node.
   * 
   * @param n Node
   * @return force vector
   */
  ExtendedVector2d contractionForce(final Node n) {

    ExtendedVector2d resultR;
    ExtendedVector2d resultL;
    ExtendedVector2d force = new ExtendedVector2d();

    // compute the unit vector pointing to the left neighbor (static method)
    resultL = ExtendedVector2d.unitVector(n.getPoint(), n.getPrev().getPoint());

    // compute the unit vector pointing to the right neighbor
    resultR = ExtendedVector2d.unitVector(n.getPoint(), n.getNext().getPoint());

    force.setX((resultR.getX() + resultL.getX()) * 0.5); // combine vector to left and right
    force.setY((resultR.getY() + resultL.getY()) * 0.5);

    return (force);
  }

  /**
   * Calculate image force.
   * 
   * @param n node
   * @param ip image
   * @return image force at node
   */
  ExtendedVector2d imageForce(final Node n, final ImageProcessor ip) {
    ExtendedVector2d result = new ExtendedVector2d();
    ExtendedVector2d tan = n.getTangent(); // Tangent at node
    int i;
    int j; // loop vars

    double a = 0.75; // subsampling factor
    double deltaI; // intensity contrast
    double x;
    double y; // co-ordinates of the norm
    double xt;
    double yt; // co-ordinates of the tangent
    int insideI = 0;
    int outsideI = 0; // Intensity of neighbourhood of a node (insde/outside of the chain)
    int inI = 0;
    int outI = 0; // number of pixels in the neighbourhood of a node

    // determine num pixels and total intensity of neighbourhood: a rectangle with sampleTan x
    // sampleNorm
    for (i = 0; i <= 1. / a * BOA_.qState.segParam.sample_tan; i++) {
      // determine points on the tangent
      xt = n.getPoint().getX() + (a * i - BOA_.qState.segParam.sample_tan / 2) * tan.getX();
      yt = n.getPoint().getY() + (a * i - BOA_.qState.segParam.sample_tan / 2) * tan.getY();

      for (j = 0; j <= 1. / a * BOA_.qState.segParam.sample_norm / 2; ++j) {
        x = xt + a * j * n.getNormal().getX();
        y = yt + a * j * n.getNormal().getY();

        insideI += ip.getPixel((int) x, (int) y);
        inI++;

        x = xt - a * j * n.getNormal().getX();
        y = yt - a * j * n.getNormal().getY();

        outsideI += ip.getPixel((int) x, (int) y);
        outI++;
      }
    }

    deltaI = ((double) insideI / inI - (double) outsideI / outI) / 255.;
    if (deltaI > 0.) { // else remains at zero
      result.setX(-Math.sqrt(deltaI) * n.getNormal().getX());
      result.setY(-Math.sqrt(deltaI) * n.getNormal().getY());
    }

    return (result);
  }

  /**
   * Expand all snakes while preventing overlaps.
   * 
   * <p>Dead snakes are ignored. Count snakes on frame.
   * 
   * @param nest nest
   * @param frame frame
   * @throws BoaException on snake scale
   */
  public void loosen(final Nest nest, int frame) throws BoaException {
    int nestSize = nest.size();
    Snake snakeA;
    Snake snakeB;

    double[][] prox = new double[nestSize][nestSize]; // dist between snake centroids, triangular
    for (int si = 0; si < nestSize; si++) {
      snakeA = nest.getHandler(si).getLiveSnake();
      snakeA.calcCentroid();
      for (int sj = si + 1; sj < nestSize; sj++) {
        snakeB = nest.getHandler(sj).getLiveSnake();
        snakeB.calcCentroid();
        prox[si][sj] = ExtendedVector2d.lengthP2P(snakeA.getCentroid(), snakeB.getCentroid());
      }
    }

    double stepSize = 0.1;
    double steps = (double) BOA_.qState.segParam.blowup / stepSize;

    for (int i = 0; i < steps; i++) {
      // check for contacts, freeze nodes in contact.
      // Ignore snakes that begin after 'frame'
      for (int si = 0; si < nestSize; si++) {
        // if (nest.getHandler(si).isSnakeHandlerFrozen()) {
        // continue;
        // }
        snakeA = nest.getHandler(si).getLiveSnake();
        if (!snakeA.alive || frame < nest.getHandler(si).getStartFrame()) {
          continue;
        }
        for (int sj = si + 1; sj < nestSize; sj++) {
          snakeB = nest.getHandler(sj).getLiveSnake();
          if (!snakeB.alive || frame < nest.getHandler(si).getStartFrame()) {
            continue;
          }
          if (prox[si][sj] > BOA_.qState.boap.proximity) {
            continue;
          }
          freezeProx(snakeA, snakeB);
        }

      }

      // scale up all snakes by one step (if node not frozen, or dead) unless they start at this
      // frame or after
      for (int s = 0; s < nestSize; s++) {
        if (nest.getHandler(s).isSnakeHandlerFrozen()) {
          continue;
        }
        snakeA = nest.getHandler(s).getLiveSnake();
        if (snakeA.alive && frame > nest.getHandler(s).getStartFrame()) {
          snakeA.scaleSnake(stepSize, stepSize, true);
        }
      }

    }
  }

  /**
   * Freeze Prox.
   * 
   * @param a snake
   * @param b snake
   */
  public void freezeProx(final Snake a, final Snake b) {

    Node bn;
    Node an = a.getHead();
    double prox;

    do {
      bn = b.getHead();
      do {
        if (an.isFrozen() && bn.isFrozen()) {
          // an = an.getNext();
          bn = bn.getNext();
          continue;
        }
        // test proximity and freeze
        prox = ExtendedVector2d.distPointToSegment(an.getPoint(), bn.getPoint(),
                bn.getNext().getPoint());
        if (prox < BOA_.qState.boap.proxFreeze) {
          an.freeze();
          bn.freeze();
          bn.getNext().freeze();
          break;
        }
        bn = bn.getNext();
      } while (!bn.isHead());

      an = an.getNext();
    } while (!an.isHead());

  }

  /**
   * Implode nest.
   * 
   * @param nest nest
   * @param f frame number
   * @throws BoaException on snake.implode
   */
  public void implode(final Nest nest, int f) throws BoaException {
    // System.out.println("imploding snake");
    SnakeHandler snakeH;
    Snake snake;
    for (int s = 0; s < nest.size(); s++) {
      snakeH = nest.getHandler(s);
      if (nest.getHandler(s).isSnakeHandlerFrozen()) {
        continue;
      }
      snake = snakeH.getLiveSnake();
      if (snake.alive && f > snakeH.getStartFrame()) {
        snake.implode();
      }
    }
  }
}
package com.github.celldynamics.quimp.plugin.randomwalk;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.Outline;
import com.github.celldynamics.quimp.QuimP;
import com.github.celldynamics.quimp.geom.TrackOutline;
import com.github.celldynamics.quimp.geom.filters.OutlineProcessor;
import com.github.celldynamics.quimp.plugin.ana.ANAp;
import com.github.celldynamics.quimp.plugin.randomwalk.BinaryFilters.MorphoOperations;
import com.github.celldynamics.quimp.plugin.randomwalk.RandomWalkSegmentation.SeedTypes;
import com.github.celldynamics.quimp.utils.IJTools;
import com.github.celldynamics.quimp.utils.test.RoiSaver;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.AutoThresholder;
import ij.process.Blitter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * Generate new seeds for n+1 frame in stack using previous results of segmentation.
 * 
 * <p>This class supports two methods:
 * <ol>
 * <li>Based on morphological operations
 * <li>Based on contour shrinking (part of QuimP Outline framework)
 * </ol>
 * 
 * <p>In both cases the aim is to shrink the object (which is white) to prevent overlapping
 * foreground
 * and background in next frame (assuming that objects are moving). The same is for background.
 * Finally, the new seed should have set foreground pixels to area inside the object and background
 * pixels in remaining part of image. There should be unseeded strip of pixels around the object.
 * 
 * @author p.baniukiewicz
 */
public abstract class PropagateSeeds {
  static final Logger LOGGER = LoggerFactory.getLogger(PropagateSeeds.class.getName());

  /**
   * Seed propagators available in this class.
   * 
   * @author p.baniukiewicz
   *
   */
  public enum Propagators {
    /**
     * Just copy input as output.
     */
    NONE,
    /**
     * Use contour shrinking.
     * 
     * @see Contour
     */
    CONTOUR,
    /**
     * Use morphological operations.
     * 
     * @see Morphological
     */
    MORPHOLOGICAL

  }

  /**
   * Default setting. Better do not change.
   */
  public boolean darkBackground = true;
  /**
   * Thresholding method used for estimating true background.
   * 
   * <p>If null background is not modified.
   * 
   */
  private AutoThresholder.Method thresholdMethod = null;

  /**
   * Default constructor.
   */
  public PropagateSeeds() {
  }

  /**
   * Allow to store seed history that can be later presented in form of composite image.
   * 
   * @param storeSeeds <tt>true</tt> to store seeds.
   * @param trueBackground if not null, selected method will be used for estimating true background
   *        - excluding bright objects from it
   * @see #getCompositeSeed(ImagePlus, int)
   */
  public PropagateSeeds(boolean storeSeeds, AutoThresholder.Method trueBackground) {
    this.storeSeeds = storeSeeds;
    if (storeSeeds) {
      this.seeds = new ArrayList<>();
    }
    thresholdMethod = trueBackground;
  }

  /**
   * Default resolution used during outlining objects.
   * 
   * @see Contour#getOutline(ImageProcessor)
   */
  public static final int STEPS = 4;
  /**
   * By default seed history is not stored.
   */
  protected boolean storeSeeds = false;
  /**
   * Container for FG and BG seeds pixels used for seed visualisation.
   * 
   * <p>Every imageProcessor in pair contains important bits set to WHITE. For example BG pixels are
   * white here as well as FG pixels.
   * 
   * @see #getCompositeSeed(ImagePlus, int)
   * @see PropagateSeeds#storeSeeds
   */
  protected List<Seeds> seeds;
  /**
   * Scale color values in composite preview.
   * 
   * <p>1.0 stand for opaque colors.
   * 
   * @see #getCompositeSeed(ImagePlus, int)
   */
  public static final double colorScaling = 0.5;

  /**
   * Return demanded propagator.
   * 
   * @param prop propagator to create
   * @param storeseeds true for storing seeds
   * @param trueBackground if not null, selected method will be used for estimating true
   *        background - excluding bright objects from it
   * @return the propagator
   */
  public static PropagateSeeds getPropagator(Propagators prop, boolean storeseeds,
          AutoThresholder.Method trueBackground) {
    switch (prop) {
      case NONE:
        return new PropagateSeeds.Dummy(storeseeds);
      case CONTOUR:
        return new PropagateSeeds.Contour(storeseeds, trueBackground);
      case MORPHOLOGICAL:
        return new PropagateSeeds.Morphological(storeseeds, trueBackground);
      default:
        throw new IllegalArgumentException("Unknown propagator");
    }
  }

  /**
   * Empty propagator. Do nothing.
   * 
   * @author p.baniukiewicz
   *
   */
  public static class Dummy extends PropagateSeeds {

    PropagateSeeds binary;

    /**
     * Default constructor without storing seed history.
     */
    public Dummy() {
      binary = new PropagateSeeds.Morphological();
    }

    /**
     * Allow to store seed history that can be later presented in form of composite image.
     * 
     * @param storeSeeds <tt>true</tt> to store seeds.
     * @see #getCompositeSeed(ImagePlus, int)
     */
    public Dummy(boolean storeSeeds) {
      binary = new PropagateSeeds.Morphological(storeSeeds, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.github.celldynamics.quimp.plugin.randomwalk.PropagateSeeds#propagateSeed(ij.process.
     * ImageProcessor, double, double)
     * 
     */
    @Override
    public Seeds propagateSeed(ImageProcessor previous, ImageProcessor org, double shrinkPower,
            double expandPower) {
      return binary.propagateSeed(previous, org, 0, 0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.celldynamics.quimp.plugin.randomwalk.PropagateSeeds#getCompositeSeed(ij.ImagePlus)
     */
    @Override
    public ImagePlus getCompositeSeed(ImagePlus org, int offset) throws RandomWalkException {
      // Need override as we have different object here (binary, not this).
      return binary.getCompositeSeed(org, offset);
    }

  }

  /**
   * Contain methods for propagating seeds to the next frame using contour shrinking operations.
   * 
   * <p>{@link Contour#scaleMagn} and <t>shrinkPower</t> specified in
   * {@link OutlineProcessor#shrinknl(double, double, double, double, double, double, double, double)}
   * are related to each other. Larger <t>shrinkPower</t> stands for larger number of steps during
   * shrinking
   * (Step size is fixed {@link Contour#stepSize}). Therefore large {@link Contour#scaleMagn} will
   * move concave node too
   * fast because internally {@link Contour#stepSize} is multiplied by factor returned by
   * {@link OutlineProcessor#amplificationFactor(double, double, double)} that ranges 1.0 for
   * positive curvatures to {@link Contour#scaleMagn} for negative with specified
   * {@link Contour#scaleSigma}. Generally increasing <t>shrinkPower</t>,
   * the{@link Contour#scaleMagn} should be deceased.
   * 
   * <p>To increase effect of shifting nodes with negative curvature, normals locally can be
   * adjusted according to node with lowest curvature. See
   * {@link OutlineProcessor#shrinknl(double, double, double, double, double, double, double,
   * double)}
   * 
   * <p>For linear scaling set {@link Contour#scaleMagn} to 1.0 and
   * {@link Contour#averageNormalsDist} to 0.0.
   * 
   * <p>PropagateSeeds_Contour<br />
   * <img src=
   * "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAmcAAAJBCAIAAACMJYVtAAAhEElEQVR42u3d25Kj
   * urYAUf7/p+d5OBErdpdtELrAlDQy+qGju3yjSKeEMToOAPmICBsBAADVBMjc9vMj
   * 7hPYrZrcBHYZAn+9z/gfhj7b+Be/YmxSTW4Cq1Wz/UFHjKYB1eQmcDHW+7Mffw79
   * PoeEX0eIX2/+9e8nN/x06evzLBylMhMzVpObQGpv//j29Qd+/funQr8kPL9h439d
   * vvs4CoRZqslNYBpvL8eh53dyMnQ9l6pOv5NB98m42K8eE1WTm0AudUuO2LSb+VWS
   * QjOP06NDfd+tgAzV5CYwjcMJx7OFtyoZzzITs1STm0Bqbx8ws/zOu3928kddv3ok
   * ryY3gbzqVh8F+jpyLLf966jz12i0/Py985dJS8wy1+QmAAA3pmIAAEA1AQBQTQAA
   * VBMAANUEAEA1AQBQTQDeLLyhgAjcBHBl3fMr3/76d28EUE1uAhNUs/0dpOJinufP
   * BFBNbgI3xnrlV9g6mhfO/fRkxMq3Jw+X+S0Mm1eTm9xE0mT+EvLWgu/nC+eWrxff
   * 8l/MxDLV5CY3kd3bCjPLj+10NPPSt5NrTJ88YWYiWzW5yU0knWv+OrxT6HDLugqF
   * Zh49Vr41nsV0c01uchNzeNt4FGiEmeWC3R3PMhPJ55rctLcgo7cdPztp+ceOB4gu
   * 34acp4cpqslNIJe6Xc7TK1kR/vMnn1n59vI+mYls1eQmN7G44QBecYqbgGoCnOIm
   * wHAAqgkAGd6Ey/7ASBTcBDg55IehmuAmsJqWLYNfqCa4CRjGGt6qJriJx2SuW6kA
   * UznZPijG09XkJjcxt9LtZpZ/l/n8i9g7vUfEVHeL/vpwk5vYupold/Vrtb8tR7KT
   * 3jnmqyY3uYmzceKlDBWL3N4S++5VK/czM5Z4CJTu8NzkJjdTe1txjebyRW4HmbnN
   * UaAnzwtwDkKKanKTm9yczOFCM8+9+vU5x58BaeH49ETCpeWM1d8LGMdNbnJzKnWr
   * V74tX0JhxHi2+j1ofTO7XXWEnG9Wk5vc5GZqb3uZebmY0ed4lpl93PjlYb2czHyt
   * mtzkJje3rublE7i1mPvXhXm3NrPEvcqxLTkXryY3uYnb6p4vKnvyY41HgY7iY1BH
   * wTK5W2tZcYyInLmryU1ucnNxwzGHlpU39NvP6xQ3uQnVRJkM0fwIQU7VxKNuFmnH
   * TYbjeS1LT9O74ad9QDVR6eYfGYsM5SbQzcwCLe/daTDTSBQD3awfu3ITeMvMMzkN
   * aVUTY9yMxrvmJvBWMnvIyUzV5OaQZHITyFtNcqomHnAzOj4GN5fRfrP18xYZz7bJ
   * 6dc9QTW5OW81uUn+fxxmcquZ0VOUKjn9+qaZa3LzRTdjxCNxc6tq2lAJq/n95oa0
   * m1XT7yJhNbmZWt2TS2GdX7WLmQuY+eVOfIKSo5rc3HxEy83s3n5dUa/kus+OAj1q
   * 5r//HyMe0pnuCarJzdndfK+a3Hzc4UYzvy489PkDNngSM4OZWeea3FRNI9qk6n4e
   * 8Ok+nq1+v2DmUDOjVTq/x4HV5KZqmm6m9vax8Swzs801G6TzexxVTW5y8+edcHOx
   * apY8EDMnqObhhL3VqsnNGat5OJk2obqFR4HOx6ctS+ziXTOFM2E1ucnNi/vh5uyG
   * Y+pq/ly0iJnpneLm8tU03VRNJDXz+x2SUzWRwE3hVE1U7egxXAtyqiba3YxnHp6b
   * wOvVPFyRxEgUKavJTSBjMsmpmuAmwMwHzDy+nlEE1eQmN4E3zDweOgrUJufx64Rc
   * qOYmbsbDT4Wb2Put5/3PTprNpKhqbu1m4mpyM7EwHb803eVb2LfWf1DNfnL+UbTw
   * j2pyM6Gh3FTZ8W8HXfbvLndYscRERjPnG9K2m6ya3Bzy5LnJzT4Dz5O/f47yfl30
   * q3B3P7/5r/v874piJ6sdXa7lq5rp5Vzc0rpVqbd1M8fx3kSnHXAz0aTw1z/++oGv
   * F8m8Zeavm5+b2fJfy1RzDzO/Wrp4Nbn59XmmPkLLzd0moJ/7dOEgt+S9oPzml49e
   * rV/5qkmqefaAWaSIlYzj5qWbfyqe/wgtN1eO5a9slBxIyWbm+dEhc82FzJxezrql
   * SLZ1s33BbW5yc8j8stCNzGYWamauOe2BoEXkrDhCu7ObqsnN1NUsl6HQw1s3bzns
   * 43PNzYa0x7wfpdRVc2c3HaHlZhZ1f52AV3J05UStk5PlKswsP/uu/Py9863hjINJ
   * zJx1YFu9WPSebh4pVs+Ou/8f3PQVT6TYyeKFXXIGOWfy0xX1lnfToHbxSSeYeeM5
   * RSeP+l9+ZBo/VZObSau5vZtYUc5nDwRF7RvImZBj/ZpATtXcwc145TlxE8ws+f94
   * /qnc8qdCtvrhrWpil2oGN4FUc81oV679wEyNotnlVM3l3YzXnxA3wcyHk3lUL3k7
   * 6PqUUbGUoGriHTffryY3wcyHzYw6c+Lez94+5yAG2qyaWK2ad9xsXSdsCTcX2ls3
   * epeZtpr9whqXh56Ouf1caX/m5lvVjK5KRpcfnt/NsaqMWK5vp0VuJzDz19Gd9rtq
   * 3RCTn4kwdK/jpmr2Gr/W3IOzhB42s/0O51nkdg4zv+oR90/T65LtZeScsZrcXKOa
   * Iz7T3CicFSvf/lp1789VIkvWyL1cOHf1RW6nqWbLbh79vni9UjhHrErNTdV8Ze9f
   * zM3bA7q7q9SWLF17efNdF7mdvprnT+Ch0/TmlLPXqtTcVM0Mu/5G4TzZ0UuuGf2/
   * yt1dQqGjmefvR4kXuV25mo9+hbRSzkhiHDe5+Uw1J7mIWO7zgwpXvj1Ol+t7xcxj
   * hUVuVbNrOKca2PZalZqbqplnd+9woaLkk87ylW9zmlmoWeJFbpet5psXEptEzl6r
   * UnNTNVPt6Eueu1d5jOX8X77u8Y1mbrDIrWomGdjGu/Zxc0k3X5vbvb2Ln4VzBjev
   * 987ylW9LzDyK18j9ZeZOi9zW7ihpwhlv7+n9/Izn1eMmN5es5tFx1Yec4UQ+VPMV
   * Px/dkK6ox83HwhkvbalJ3cSKcqrmrYdLKadq7uBmhnC+/pXuThcq4QvWnW4mHdgm
   * k1M1ubne+LVmqwknNp9u5r1WUeQyUzVNN4cOHLN953EiN2FI+4SrkcbSZjlVExO7
   * GYmvETCLm9hFTjtU0XbovXqnatrRMg9qJwtnDjfBTFut/6BXNe1l3Bw1rlVNmG4+
   * vL1UE9yceMapmmDm0EDWfLCqmjDdzDmoVU0I52sTysrNqZoQzv4+Nh8EUk0wc9Tm
   * GDjWVU20uLmYnn8O8MS376HdOAjkbCAI55vVjFEHcFUTm7t5LlOUXA8ot5tg5k7b
   * os+31VQTppu93ZjHTWwzv9rmE5QzJxO8OagmN1d1M6pvNpWbMN3cYCvEGxtcNbGT
   * m5FII+6AmaoJbq6XTNUEOTNugnhvg6sm9nAz8glknwMzU000VRPczDnRVE2QM+lE
   * c+wDqObObm5RzWndxNbVXGbPihcmmqoJI9qcE03VBDk/Aln6Bei4c6eZ5FRN003V
   * zOkmTDfn2LNqvgMdN52s/551/02omtyM6V/Pmm6CnNNUs89tSmap8b6ZqsnN/G52
   * ruYkbmJLM/cZ1Z5fPbrn43XehKrJzZWrObObYObqch61x3biTTNVk5v53YwudzGb
   * myDnHnI+oXTXi2mqJje3FPPGiX7x7jPF9tXMvGfFSC2vLX1jlSLV5OaeYt6LoxXE
   * QM5xe30UrynfMLDttglVk5tbTTfjflDfchPMXH9Ue/ejkwY/VROmm6P0jBxugpzT
   * yBm/p48n88jou+FUE9x8aTibxE1sb+Yx2XHay0ZGm5NtcqomRrm50nRzajdBzgUv
   * 6R6dDhypJrjZvZox7kFVE+TMuOFUEy+5ucZ0c+AJRKoJ1cy47VQTqlm768f8boKc
   * wqmaEM75qvnl3lQTj+7VqlmyYZxDi1fdnHq6GUu4CWaqpmqCm31eRtz/L9UEOVVT
   * NbHpdPO4u268akI1F99kT31woprc5Ga/atpgGGQmOVUTM7hp5zicCgRDWmaqpn2N
   * m6oJ081NtFRNcPO97eVDTTBz88GsatrdSn6EniaaSCdn2ExvDGZV005nUGuiCWbO
   * vI1UE6abqglymm6Wbh3VhEFtzm2kmkgh505mXn8j+3EtVZObppsmmmDmpGN71URq
   * N7eKg4kmhDN9NXv8hGpCOB+faKomnp9lrR7O6PNDqon33dwkEaqJ7MnYPZwvaama
   * 3BROycQicqqmakI4VRPMNN1UTaxQzWOTc/dUE3nN3Hm6qZqYyk3VVE08sB9WnhYU
   * O2wF1cRsbi5cDNXEVAPbDYa0UfMKVRPcVE2Qc1czX78CrWqCm6Uv1CUOMN2QVjVV
   * E9zMOt0kEfINabcfz47aAKoJbrZVk0FIOaRVTdUEN1UTzFRN1QQ3VRNol3O3jzZf
   * +uBENdHo5vrV9KEmVFM1VROqmdxN4P6utrqcqokF3FRNewxUUzXBTW6qJqaTk5mq
   * CW6qJsDMm2YOfOmqCW5evHgn0IKZqqma4KZqgpnMVE1ws/crV01MI+c+1XzvgxPV
   * BDdrh7OqCWbuZ6Zqgpu1E03VBDMzJlM1wc2cyVRNMHOzZKomuCmZWNbMETtp/Psn
   * XzJVE6qZM5mqif2qGc9KECm1VE1sXk3JxEJmjpQzHlch27FZ1URON2eoJmuw2XQz
   * Hhci50RTNbH5dNNEE6rZ+pBbJVM1kc3Nl1+hZGJ6M8fI+WQ1o/IBVBOTuRmzv7xk
   * bgLdqtm420qmaiLtiPa1lyeZEM66Bxt4wlH02yCqCdPNF5KpmpjFzK7hfKaaUX/X
   * j2qpmujrZszwkhybxQZm/g5n9H6w6O5kYi1VE93djMR5mchNYFQ471o6qJpnFxhK
   * rKVqYpCb0WkA2vHwj2SCnDWWRrVgdT+cW0vVxFA3644G/blVxWUvo+R6mZKJLeSM
   * IlvaW1jy89FtHaHXtFRNPOBm9ButXhY0On/rkiDYIJxfR5ot59xGxR3GHCfmqSYe
   * czOu/nRxs0+cVRNLmXk88HFHc2OPObRUTXBTMrGHnEfbLPLPbWPQubPZtVRNcFMy
   * sY2Zn5o1fnhSd8yo3ur3tVRNcFMysaWcJ3a1fHhycsPW8+JTaKma4KZkYm8zhz6p
   * 6PIlskTfAldNcDOnm4DhXsbXpZrgpikmVtqJg5aqCW5KJrDh3pzxhagmuCmZMLD1
   * /FUT3Nxz0gz83b/DE1ZN2NU3e8LA4nv8NEKqJrgJ7Kaop6Sa4KZeAreHt1G8Tknh
   * n4onMNu2U01wE+DqgKuPdFyqQTXBTW4CUE0AAFQTAACoJgAAqgkAgGoCAKCaAACo
   * JgAAqgkAgGoCAADVBABANQEAUE0AAFQTAADVBABANQEAgGoCAKCaAACoJgAAqgkA
   * gGoCAKCaAACoJgAAUE0AAFQTAADVBABANQEAUE0AAFQTAACoJgAAqgkAgGoCAKCa
   * AACoJgAAqgkAgGoCAADVBABANQEAUE0AAFQTAADVBABANQEAgGoCAKCaAACoJgAA
   * qgkAgGoCAKCaAACoJgAAUE0AAFQTAADVBABANQEAUE0AAFQTAACoJgAAqgkAgGoC
   * AKCaAACoJgAAqgkAgGqqJgAAqgkAgGoCAKCaAACoJgAAqgkAgGoCAADVBABANQEA
   * UE0AAFQTAADVBABANQEAgGoCAKCaAACoJgAAqgkAgGoCAKCaAACoJgAAUE0AAFQT
   * AADVBABANQEAUE0AAFQTAACoJgAAqgkAgGoCAKCaAH7ICQAASjBsAMw1AQBQTQDT
   * yVzy8yPuE9itmtwEdhkCf73PLseRC98XHLPGhtXkJrBaNdsfdMRoGlBNbgIXY70/
   * +/Hn0O9zSPh1hPj15l//fnLDT5e+Ps/CUSozMWM1uQmk9vaPb19/4Ne/fyr0S8Lz
   * Gzb+1+W7j6NAmKWa3ASm8fZyHHp+JydD13Op6vQ7GXSfjIv96jFRNbkJ5FK35IhN
   * u5lfJSk08zg9OtT33QrIUE1uAtM4nHA8W3irkvEsMzFLNbkJpPb2ATPL77z7Zyd/
   * 1PWrR/JqchPIq271UaCvI8dy27+OOn+NRsvP3zt/mbTELHNNbgIAcGMqBgAAVBMA
   * ANUEAEA1AQBQTQAAVBMAANUE4M3CGwqIwE0AV9Y9v/Ltr3/3RgDV5CYwQTXb30Eq
   * LuZ5/kwA1eQmcGOsV36FraN54dxPT0asfHvycJnfwrB5NbnJTSRN5i8hby34fr5w
   * bvl68S3/xUwsU01uchPZva0ws/zYTkczL307ucb0yRNmJrJVk5vcRNK55q/DO4UO
   * t6yrUGjm0WPlW+NZTDfX5CY3MYe3jUeBRphZLtjd8SwzkXyuyU17CzJ62/Gzk5Z/
   * 7HiA6PJtyHl6mKKa3ARyqdvlPL2SFeE/f/KZlW8v75OZyFZNbnITixsO4BWnuAmo
   * JsApbgIMB6CaAJDhTbjsD4xEwU2Ak0N+GKoJbgKradky+IVqgpuAYazhrWqCm3hM
   * 5rqVCjCVk+2DYjxdTW5yE3Mr3W5m+XeZz7+IvdN7REx1t+ivDze5ia2rWXJXv1b7
   * 23IkO+mdY75qcpObOBsnXspQscjtLbHvXrVyPzNjiYdA6Q7PTW5yM7W3FddoLl/k
   * dpCZ2xwFevK8AOcgpKgmN7nJzckcLjTz3Ktfn3P8GZAWjk9PJFxazlj9vYBx3OQm
   * N6dSt3rl2/IlFEaMZ6vfg9Y3s9tVR8j5ZjW5yU1upva2l5mXixl9jmeZ2ceNXx7W
   * y8nM16rJTW5yc+tqXj6BW4u5f12Yd2szS9yrHNuSc/FqcpObuK3u+aKyJz/WeBTo
   * KD4GdRQsk7u1lhXHiMiZu5rc5CY3Fzccc2hZeUO//bxOcZObUE2UyRBtY9Z7A1s7
   * gGqCm2Dmclr+r25FJ+vd8NM+oJqodDO4CbxpZtQoduEnM41EMcDN8vOBuAk8ZObd
   * pW8bpCOnanLznpsHN4F5tWyWk5mqyc17E01uAtNXk5yqCW7iHe03Wz8vnZnR/WGC
   * mStUk5vzVpOb5P/HYSa3mhk9LamS069vmrkmN7mJiatpQyU08/s9GNJuVk2/i1Wq
   * yc2n1D25FNb5VbuYuUA1v9yJT1ByVJObRrQHNzN7+3VFvZLrPjsK9KiZR7fPNduq
   * Sc6x1eQmN6PeOb/HZx1uNPPrwkOfP2CD56nm3/thZo65Jje5aUSbVN3PAz7dx7PV
   * 7xfMvPx/cq5aTW6qpulmam8fG88yc4JqHj5Bebma3OQmN3epZskDMTObmYeTaTeo
   * Jje5iT7qFh4FOh+ftiyxi6RmkvO9anKTm8K5uOFgJl5xips7uHnUnHxgx1BNZn78
   * fzzw2ORUTSRwUzhVE1U7egx3QjhVE9wEmDl0SEtOteMmN4HcWg61gZyqCW4CzBxq
   * 5v//HD9Vk5vcBDKYeTx0FKhNzv/8pKhqcpOb8NaTaUgbTz6VqLsDim5aTW6mrCY3
   * EwvT8UvTXb6Ffeti2a9+4ztrNZseL27+UU1uprKSmyr71NtBl728yx0W3rD7k+9s
   * 5jH8O2G9p5vtJqsmN5PMXxO52a+a3GweeJ78/XOs9+uiX4Vqnd/8133+d0Wxk9WO
   * LtfyTelk+moer5iypqV1q1Jv66ZqcjPvpPDXP/76ga8Xybxl5q+bn5vZ8l/nGyT1
   * Edodzfxq6eLV5KZqcnO+ghYOckt26/KbXz56tX4lqyb9eafIf4T2WPkgbdtWmn+u
   * yc3z2TY3ufmaur92zZIDKdnMPD86dPeNTDUTmzm9nHVLkWzrpmpyM+n8stCNzGYW
   * JvDXXFM10x8IWkTOiiO0O7upmtxMXc1yGQo9vHXzlsM+7Z+dOEI7lZnHvB+l1FVz
   * ZzdV8/rZcPMZdX+dgFdydOVErZOT5SrMLD/7rvz8vVsbRDVzDyJjRvW4We7m3bNt
   * M7h5PHytA25iV+LWj8QrTyipnDP56Yp6y7t5PHk1Wm6CmYtXc9TlR6bxUzVVM2k1
   * t3cTK8r5ejWrH7jQvQ5+qiZedvOdanITzExazbv+VMhWP7xVTaxczeirGzehmrn8
   * bD8wU6NodjlVk5vD52vcBDMfG8y2+jno+pRRsZSgamJBN+s94CaYeSSR8/7As/Js
   * gxhos2qCm5u7udDeutG7zLRm9vC3yNKoe0z7s9eyjpvHgHNdC908lnZzrCojlut7
   * bAHqBF+azm5mjJG8j8OTn4kwdK/j5ibV7LijV5w/1PXbL3uEc5CZ7XdYcfE8Zj6w
   * I0fbbWMtOWesJjdnqWb5o3eZm+4VzoqVb3+tuvfnCq4la+ReLpz72ALUyeRcrZq9
   * DsGsFM4Rq1JzUzULH73v+UDrDWrvDR7vrlJbsnTt5c3fWoCamaN34XhgwjqnnL1W
   * peamatZV84lHXPtQ7fliCMfvhYf+LLN1d43cLmZeWneyKNjJMvfMjAT3MFLOSGIc
   * N7m5ZjXndPNayJKVb4/T5fpeMfNYYQFq1ewazqkGtr1Wpeamat59rk9ffmGlqwiV
   * r3yb08xCzRIvQD2BmfHSbSuHo/PI2WtVam6qZp5qHsufH3TrGMv5v3zd4xvNfHIB
   * 6my/GdV8dtIZ79rHTW4uU82LQe0Mbl7vneUr35aYeRSvkfvLzJ0WoK7dUZJcvT3H
   * zt7poFA8rx43ublqNad2EzOSzsx45Cb9n97ta2Mmqia4OeK5RpLtldhNrCjnGweC
   * YtgPD396KeVUTW6uPdGc100Y0vZ8TpF47+50ia+Hnr5qcnP5ieakbsKQdogAkXW/
   * bj6FTzWx7HQz6VdIk7kJQ9qH+pTqm8kN30tRTSw43YzHD0p9HV63DWopgyXkjMRX
   * 8qg69101sVQ44+0zgEqH12ncBDNtu27/p5rgZt2mKe13AjdBTltNNcHNJBtFNZHJ
   * zJV2q7izTv3thexVE9xUTZBzmT0rCn4gqj+z8bkmuJmzmj7XxCtmLrBzxdC7Vk1w
   * M6G8zgaCIe2bySw8tvuenKrJzU2rmd5NMHOnZPY50V41wc3em2MeN7HLAG6BE/Za
   * PxGJLM9FNbm5mJtNm2M2N7HjkDYWfHnPezR2K6omN7fYFhO6iR3NnHG3Uk1wc82J
   * pmrCkDaXB5HxSakmNzecbsYSbsKQ1kRTNcHN4a8/VnET5DTRzCinanJz8Vc+rZtg
   * pmqqJrj54BRTNUHO6asZ6cqsmtyc8eSDG1cmmNZNMHPvZP6vk/Xfs+6/FVWTm3Od
   * eRDbuAlyZt+tOlezZDAc75upmtw8Fj4UNLOb2NLMY6Yz3btdEujWWPVtOVWTm8fa
   * H6BM6yaYmX23ii53EaMfWDWxnZsdnuSEboKc2fesePex451nqprcnOhdP3ZyE8zc
   * uppRuHSRauI9N4+1P0Z54oHZhJ3CGcPu9sYJeqUD225PVjW5OV04o/e9RUo3wcwd
   * p5tRJ+CDq8mrJjdnfOOvWUm6TLK4vGZCPPwuAnLuNN2M6lGzasJ0c8wENO5/jfMV
   * N7G9mccEZ7rHG/dWFU7VxCg3533vL89h6+Z7xE2Qc7vppmrCdDPhq432O1RNvCLn
   * 2tWcZTyrmtycaFDb/mr7fGlTNcHMvs9qIjNVk5vrVfPoeKW8r7dSTZhu9t3xVRPc
   * zPmCVRMTyjmJmfHuDZ1Di1fdXHW6GfO4CWbOZ2bFWQPR6+FUE9wc8IJVE6abw19J
   * 9Psx1YRwvvuCVRPMfLmdMei026c+OFFNbq5dzf9eRTzx6QmbMMjMY9ZViqLHpbyK
   * NplqgpuPvux0boKc65upmuAmNwFD2se314MfnKgmN7nZKZnexsDMDQazqml3K/mR
   * sLFMNCGcJpqqaY/jpokmmKmaqgnTTdWEcDJTNcHNFzeQaiKFnNsPad/9ArVqcpOb
   * ny9TMjGTmVvtgK8fAlJNbqpm0WtUTQjnbBNN1YRw5kymauJhMzcOp2piOjdjw22h
   * mkhn5rHj4aBIoKVqcpObkglyqqZqgpudNoRqYiIz194fVRPcnGBbqCbymrnZdFM1
   * wc0JNodq4r290WlBqgluzhZO1UT2ge2e5+ypJripmkDNHrbtme5vfI1aNdHipnCq
   * Jgxp0043VRPcVE2QUzWLXuSoDaCa4GZON4GmIS0zVRPcVE0wk5mqCW5yE2iXc7eP
   * T1764EQ10ejm+tX0oSZUc7bBrGpCNVUTzLz5I6qpmkjo5vrn0KomVJOZqgluqiaW
   * k5OZqgluqibAzJtmDnzpqglu5nQTYKZqgpvcBJjZ5cWrJripmkCTnPtU870PTlQT
   * 3PzzWnyoCWZOPdFUTXDzvdesmmDmbMlUTXAzZzJVE8zcLJmqCW5KJpi5UjJVE9zM
   * mUzVBDP3S6ZqgpuSiYXMPJa63GW2Y7OqiV5ujthZ498/yarJGhjSbjnRVE2kdTMe
   * kcFEE8yUTNXEC25232XjESUyuwnU7oKThzMqjVNN7OtmPGJFcjeBPmbOtc/m11I1
   * kdDNF6opmSDntMlUTew+3XygmpHeTaBtR5wwnFMc/1FN9HUzRj5q7OQm0Lwvfgtn
   * zPKSsmqpmujuZqOYMVgPycTW4eyyL/f9TlhMpaVqItugNoZJEpIJct71M779+fq/
   * R+0dHlNpqZpI4mb5D0ez6ZKJneSMyinj3ZFvdLnD9FqqJh5zs72XJT8fnb91SRAs
   * LeeNYWZtQaO8lzOcmKeaeMzN+P3nOTHncRPouo+O/RyyR2CPObRUTXBTMrGHnEfb
   * SPXPbXtNSMe9WNUEN3dyExi5s946QHP5kcjdI0f1Vr+vpWqCm5KJLeU8savlI5ST
   * G7Z+ZJpCS9UENyUTe5s59En1Ob8o0eUZVBPczOkmYLiX8XWpJrhpiomVduKgpWqC
   * m5IJbLg3Z3whqgluSiYMbD1/1QQ395w0A3/37/CEVRN29c2eMLD4Hj+NkKoJbgK7
   * KeopqSa4qZfA7eFtFC+uUPin4gnMtu1UE9wEuDrg6iPdF2xQTXCTmwBUEwAA1QQA
   * QDUBAIBqAgCgmgAAqCYAAKoJAIBqAgCgmgAAQDUBAFBNAABUEwAA1QQAQDUBAFBN
   * AABUUzUBAFBNAABUEwAA1QQAQDUBAFBNAABUEwAAqCYAAKoJAIBqAgCgmgAAqCYA
   * AKoJAABUEwAA1QQAQDUBAFBNAABUEwAA1QQAQDUBAIBqAgCgmgAAqCYAAKoJAIBq
   * AgCgmgAAQDUBAFBNAABUEwAA1QQAQDUBAFBNAABUEwAAqCYAAKoJAIBqAgCgmgAA
   * qCYAAKoJAABUEwAA1QQAQDUBAFBNAABUEwAA1QQAQDUBAIBqAgCgmgAAqCYAAKoJ
   * AIBqAgCgmgAAQDUBAFBNAABUEwAA1QQAQDUBAFBNAABU00YAAEA1AQBQTQAAVBMA
   * ANUEAEA1AQBQTQAAoJoAAKgmAACqCQCAagIAoJoAAKgmAABQTQAAVBMAANUEAEA1
   * AfzD/wFqNHlO4MJ0zwAAAABJRU5ErkJggg==" />
   * 
   * @author p.baniukiewicz
   * @see OutlineProcessor#shrinknl(double, double, double, double, double, double, double, double)
   * @see OutlineProcessor#amplificationFactor(double, double, double)
   */
  public static class Contour extends PropagateSeeds {

    /**
     * Scale magnification. Used by {@link PropagateSeeds.Contour} only.
     * 
     * @see OutlineProcessor#shrinknl(double, double, double, double, double, double, double,
     *      double)
     */
    public double scaleMagn = 5;
    /**
     * Sigma. Used by {@link PropagateSeeds.Contour} only.
     * 
     * @see OutlineProcessor#shrinknl(double, double, double, double, double, double, double,
     *      double)
     */
    public double scaleSigma = 0.3;
    /**
     * Distance to set normals equal. Used by {@link PropagateSeeds.Contour} only.
     * 
     * @see OutlineProcessor#shrinknl(double, double, double, double, double, double, double,
     *      double)
     * @see Contour#Contour(boolean, ij.process.AutoThresholder.Method, double, double, double,
     *      double)
     */
    public double averageNormalsDist = 0;
    /**
     * Distance to average curvature over. This is multiplier of average node distance. Always at
     * least three nodes are averaged (n-1,n,n+1).
     * 
     * @see OutlineProcessor#shrinknl(double, double, double, double, double, double, double,
     *      double)
     * @see Contour#Contour(boolean, ij.process.AutoThresholder.Method, double, double, double,
     *      double)
     */
    public double averageCurvDist = 1.0;

    /**
     * Step size during object outline shrinking.
     * 
     * @see Outline#scaleOutline(double, double, double, double)
     * @see ANAp
     */
    public static final double stepSize = 0.04;

    /**
     * Default constructor without storing seed history.
     */
    public Contour() {
      this(false, null);
    }

    /**
     * Allow to store seed history that can be later presented in form of composite image.
     * 
     * @param storeSeeds <tt>true</tt> to store seeds.
     * @param trueBackground if not null, selected method will be used for estimating true
     *        background - excluding bright objects from it
     * @see #getCompositeSeed(ImagePlus, int)
     */
    public Contour(boolean storeSeeds, AutoThresholder.Method trueBackground) {
      super(storeSeeds, trueBackground);
    }

    /**
     * Allow additionally to set power of shrinking curved parts of the outline and window for
     * equalising normals.
     * 
     * @param storeSeeds <tt>true</tt> to store seeds.
     * @param trueBackground if not null, selected method will be used for estimating true
     *        background - excluding bright objects from it
     * @param sigma sigma of Gaussian
     * @param scaleMagn maximum amplification of scaling index (for curv<<0)
     * @param averageCurvDist distance to average curvature over. At any case at least 3 nodes are
     *        taken (current, previous and next). See {@link OutlineProcessor}. This parameter plays
     *        as multiplier of average node distance. Setting it to e.g. 5, likely 5 nodes will be
     *        takes in total.
     * @param averageNormalsDist distance to look along for node with smallest curvature (negative)
     *        from current node (forward and backward). Set 0 to turn off this feature. See
     *        {@link OutlineProcessor} and description of previous parameter.
     * @see Contour#Contour(boolean, ij.process.AutoThresholder.Method)
     * @see OutlineProcessor#shrinknl(double, double, double, double, double, double, double,
     *      double)
     */
    public Contour(boolean storeSeeds, AutoThresholder.Method trueBackground, double sigma,
            double scaleMagn, double averageCurvDist, double averageNormalsDist) {
      super(storeSeeds, trueBackground);
      this.scaleMagn = scaleMagn;
      this.scaleSigma = sigma;
      this.averageNormalsDist = averageNormalsDist;
      this.averageCurvDist = averageCurvDist;
    }

    /**
     * Generate seeds for next frame using provided mask.
     * 
     * <p>The mask provided to this method is shrunk to get new seeds of object (that can move
     * meanwhile). The same mask is expanded and subtracted from image forming the background.
     * 
     * <p>Setting <tt>shrinkPower</tt> or <tt>expandPower</tt> to zero prevents contour
     * modifications.
     * 
     * @param previous Previous result of segmentation. BW mask with white object on black
     *        background.
     * @param org original image that new seeds are computed for. Usually it is current image
     * @param shrinkPower Shrink size for objects in pixels.
     * @param expandPower Expand size used to generate background (object is expanded and then
     *        subtracted from background)
     * @return List of background and foreground coordinates.
     * @see PropagateSeeds.Morphological#propagateSeed(ImageProcessor, ImageProcessor, double,
     *      double)
     * @see Outline#scaleOutline(double, double, double, double)
     * @see #getTrueBackground(ImageProcessor, ImageProcessor)
     * @see #setTrueBackgroundProcessing(ij.process.AutoThresholder.Method)
     */
    @Override
    public Seeds propagateSeed(ImageProcessor previous, ImageProcessor org, double shrinkPower,
            double expandPower) {
      // FIXME if there are two separate objects in the same color they will be treated as
      // separate objects
      double stepsshrink = shrinkPower / stepSize; // total shrink/step size
      double stepsexp = (expandPower) / stepSize; // total shrink/step size
      Seeds ret = new Seeds(2);

      List<Outline> outlines = getOutline(previous); // this supports grayscales

      // save extra debug info if property set
      if (QuimP.SUPER_DEBUG) {
        String tmp = System.getProperty("java.io.tmpdir");
        for (Outline o : outlines) {
          long time = new Date().getTime();
          RoiSaver.saveRoi(
                  tmp + File.separator + "propagateSeed_" + time + "_" + outlines.hashCode(),
                  o.asList());
        }
      }
      for (Outline o : outlines) {
        if (o.getNumPoints() < 4) {
          continue;
        }
        ByteProcessor small = new ByteProcessor(previous.getWidth(), previous.getHeight());
        small.setColor(Color.WHITE); // for fill(Roi)
        // shrink outline - copy as we want to expand it later
        Outline copy = new Outline(o);
        LOGGER.debug("Shrink object");
        new OutlineProcessor<Outline>(copy).shrinknl(stepsshrink, stepSize, 0.1, 1.5, scaleSigma,
                scaleMagn, averageCurvDist, averageNormalsDist);
        copy.unfreezeAll();
        Roi fr = copy.asIntRoi();
        fr.setFillColor(Color.WHITE);
        fr.setStrokeWidth(1.1);
        fr.setStrokeColor(Color.WHITE);
        small.fill(fr);
        small.drawRoi(fr);
        // small.resetRoi();
        ret.put(SeedTypes.FOREGROUNDS, small);
      }

      ByteProcessor big = new ByteProcessor(previous.getWidth(), previous.getHeight());
      // big.setColor(Color.BLACK);
      // big.fill();
      for (Outline o : outlines) {
        if (o.getNumPoints() < 4) {
          continue;
        }
        // frezeTh influences artifacts that appear when concave regions are expanded
        // 0 prevent a little
        LOGGER.debug("Expand object");
        new OutlineProcessor<Outline>(o).shrinknl(stepsexp, -stepSize, 0.1, 0, scaleSigma,
                scaleMagn, 1.0, 0);
        o.unfreezeAll();
        Roi fr = o.asFloatRoi();
        fr.setFillColor(Color.WHITE);
        fr.setStrokeColor(Color.WHITE);
        big.drawRoi(fr);
      }
      big.invert();
      // store seeds if option ticked
      ret.put(SeedTypes.BACKGROUND, getTrueBackground(big, org));
      if (storeSeeds) {
        seeds.add(ret);
      }

      return ret;

    }

    /**
     * Convert mask to outline.
     * 
     * @param previous image to be converted outline. White object on black background.
     * @return List of Outline for current frame
     * @see TrackOutline
     */
    public static List<Outline> getOutline(ImageProcessor previous) {
      TrackOutlineLocal track = new TrackOutlineLocal(previous, 0);
      return track.getOutlines(STEPS, false);
    }

  }

  /**
   * Contain methods for propagating seeds to next frame using morphological operations.
   * 
   * @author p.baniukiewicz
   *
   */
  public static class Morphological extends PropagateSeeds {

    /**
     * Default constructor without storing seed history.
     */
    public Morphological() {
      this(false, null);
    }

    /**
     * Allow to store seed history that can be later presented in form of composite image.
     * 
     * @param storeSeeds <tt>true</tt> to store seeds.
     * @param trueBackground if not null, selected method will be used for estimating true
     *        background - excluding bright objects from it
     * @see #getCompositeSeed(ImagePlus, int)
     */
    public Morphological(boolean storeSeeds, AutoThresholder.Method trueBackground) {
      super(storeSeeds, trueBackground);
    }

    /**
     * Generate new seeds using segmented image.
     * 
     * <p>Setting <tt>shrinkPower</tt> or <tt>expandPower</tt> to zero prevents contour
     * modifications.
     * 
     * @param previous segmented image, background on zero
     * @param org original image that new seeds are computed for. Usually it is current image
     * @param shrinkPower number of erode iterations
     * @param expandPower number of dilate iterations
     * 
     * @return Map containing list of coordinates that belong to foreground and background. Map is
     *         addressed by two enums: <tt>FOREGROUND</tt> and <tt>BACKGROUND</tt>
     * @see SeedProcessor#decodeSeedsfromRgb(ImagePlus, List, Color)
     * @see #getTrueBackground(ImageProcessor, ImageProcessor)
     * @see #setTrueBackgroundProcessing(ij.process.AutoThresholder.Method)
     */
    @Override
    public Seeds propagateSeed(ImageProcessor previous, ImageProcessor org, double shrinkPower,
            double expandPower) {
      ImageProcessor cp = previous.duplicate();
      // object smaller than on frame n
      ImageProcessor small = cp.duplicate();
      // object bigger than on frame n
      ImageProcessor big = cp.duplicate();
      // store seeds if option ticked
      Seeds ret = new Seeds(2);
      // need to process each object separately to preserve multi fg seeds.
      Seeds decodedSeeds;
      try {
        decodedSeeds = SeedProcessor.getGrayscaleAsSeeds(cp);
        for (ImageProcessor ip : decodedSeeds.get(SeedTypes.FOREGROUNDS)) {
          // make objects smaller
          small = BinaryFilters.iterateMorphological(ip, MorphoOperations.ERODE, shrinkPower);
          ret.put(SeedTypes.FOREGROUNDS, small);
        }

        // make background bigger but for all, convert to binary first
        big.threshold(0);
        big = BinaryFilters.iterateMorphological(big, MorphoOperations.DILATE, expandPower);
        cp.threshold(0); // input also must be binary (after computing FG)

        // apply big to old background making object bigger and prevent covering objects on
        // frame
        // n+1
        // by previous background (make "empty" not seeded space around objects)
        // IJ.saveAsTiff(new ImagePlus("", big), "/tmp/testIterateMorphological_bigbef.tif");
        // IJ.saveAsTiff(new ImagePlus("", cp), "/tmp/testIterateMorphological_cp.tif");
        for (int x = 0; x < cp.getWidth(); x++) {
          for (int y = 0; y < cp.getHeight(); y++) {
            big.putPixel(x, y, big.getPixel(x, y) | cp.getPixel(x, y));
          }
        }

        big.invert(); // invert to have BG pixels white in seed. (required by convertToList)

        ret.put(SeedTypes.BACKGROUND, getTrueBackground(big, org));
        if (storeSeeds) {
          seeds.add(ret);
        }
      } catch (RandomWalkException e) { // from decodeseeds - no FG seeds
        // this is handled to console only as return is still valid (no FG seeds in output)
        LOGGER.debug("Empty seeds.");
      }
      return ret;
    }

  }

  /**
   * Produce composite image containing seeds generated during segmentation of particular frames.
   * 
   * <p>To have this method working, the Contour object must be created with storeSeeds==true.
   * 
   * @param org Original image (or stack) where composite layer will be added to.
   * @param offset Slice number to display in composite if there is stack provided. Ignored if org
   *        is single image. Set it to 0 to dispplay whole stack.
   * @return Composite image with marked foreground and background.
   * @throws RandomWalkException When seeds were not collected.
   */
  public ImagePlus getCompositeSeed(ImagePlus org, int offset) throws RandomWalkException {
    ImagePlus ret = null;
    try {
      ImageStack smallstack =
              new ImageStack(seeds.get(0).get(SeedTypes.FOREGROUNDS).get(0).getWidth(),
                      seeds.get(0).get(SeedTypes.FOREGROUNDS).get(0).getHeight());
      ImageStack bigstack =
              new ImageStack(seeds.get(0).get(SeedTypes.FOREGROUNDS).get(0).getWidth(),
                      seeds.get(0).get(SeedTypes.FOREGROUNDS).get(0).getHeight());

      for (Seeds p : seeds) {
        // just in case convert to byte
        // ImageProcessor fg = (ImageProcessor) p.get(SeedTypes.FOREGROUNDS, 1).convertToByte(true);
        ImageProcessor fg = SeedProcessor.flatten(p, SeedTypes.FOREGROUNDS, 1).convertToByte(true);
        fg.threshold(0); // need 255 not real value of map
        ImageProcessor bg = (ImageProcessor) p.get(SeedTypes.BACKGROUND, 1).convertToByte(true);
        // make colors transparent
        bg.multiply(colorScaling);
        fg.multiply(colorScaling);
        // set gray lut just in case
        fg.setLut(IJTools.getGrayLut());
        bg.setLut(IJTools.getGrayLut());
        smallstack.addSlice((ImageProcessor) fg);
        bigstack.addSlice((ImageProcessor) bg);
      }
      // check if stack or not. getComposite requires the same type
      if (org.getStack().getSize() == 1) { // single image
        ret = IJTools.getComposite(org.duplicate(), new ImagePlus("", smallstack.getProcessor(1)),
                new ImagePlus("", bigstack.getProcessor(1)));
      } else {
        if (offset > 0) { // stack but show only one image
          ImageProcessor tmp = org.getStack().getProcessor(offset).duplicate();
          ret = IJTools.getComposite(new ImagePlus("", tmp), new ImagePlus("", smallstack),
                  new ImagePlus("", bigstack));
        } else { // stack
          ret = IJTools.getComposite(org.duplicate(), new ImagePlus("", smallstack),
                  new ImagePlus("", bigstack));
        }
      }
    } catch (NullPointerException | IndexOutOfBoundsException e) {
      throw new RandomWalkException("Problem with showing seeds. Seeds were not stored. "
              + "You need at least two time frames to collect one seed or "
              + "segmentation returned empty image.");
    }
    return ret;
  }

  /**
   * Propagate seed.
   * 
   * <p>Each separated object found in <tt>previous</tt> is returned as separate BW map in
   * {@link Seeds} under {@link SeedTypes#FOREGROUNDS} map. <tt>previous</tt> can contain grayscale
   * labelled objects.
   *
   * @param previous the previous
   * @param org original image that new seeds are computed for. Usually it is current image
   * @param shrinkPower the shrink power
   * @param expandPower the expand power
   * @return the map
   * @see #getTrueBackground(ImageProcessor, ImageProcessor)
   * @see #setTrueBackgroundProcessing(ij.process.AutoThresholder.Method)
   */
  public abstract Seeds propagateSeed(ImageProcessor previous, ImageProcessor org,
          double shrinkPower, double expandPower);

  /**
   * Excludes objects from estimated background.
   * 
   * <p>If seed propagator is used, background is obtained by expanding initially segmented cell and
   * then negating the image. Thus background covers all area except cell. If there are other cell
   * there they can influence background mean. To avoid this, that background is thresholded to
   * detect objects that should be excluded from mean. This should be used when there are other
   * objects around.
   * 
   * @param bck Background (white) estimated from Propagator
   * @param org Original 8-bit image
   * @return Background without objects above threshold
   * @see #setTrueBackgroundProcessing(ij.process.AutoThresholder.Method)
   */
  ImageProcessor getTrueBackground(ImageProcessor bck, ImageProcessor org) {
    if (thresholdMethod == null) {
      return bck;
    }
    ImageProcessor orgD = org.duplicate();
    orgD.threshold(new AutoThresholder().getThreshold(thresholdMethod, orgD.getHistogram()));
    orgD.invert();
    orgD.copyBits(bck, 0, 0, Blitter.AND); // cut
    return orgD;
  }

  /**
   * Turn on processing background before using it as seed.
   * 
   * @param method Threshold method. null for turning off processing
   */
  public void setTrueBackgroundProcessing(AutoThresholder.Method method) {
    thresholdMethod = method;
  }

}

/**
 * In purpose of overriding {@link TrackOutline#prepare()} which in super class can remove thin
 * lines.
 * 
 * @author p.baniukiewicz
 *
 */
class TrackOutlineLocal extends TrackOutline {

  /**
   * Default constructor here.
   * 
   * @param imp image to process
   * @param background background color
   */
  public TrackOutlineLocal(ImageProcessor imp, int background) {
    super(imp, background);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.geom.TrackOutline#prepare()
   */
  @Override
  public ImageProcessor prepare() {
    ImageProcessor filtered = imp.duplicate();
    return filtered;
  }

}

package com.github.celldynamics.quimp.plugin.randomwalk;

import java.util.Arrays;

import com.github.celldynamics.quimp.plugin.randomwalk.BinaryFilters.Filters;
import com.github.celldynamics.quimp.plugin.randomwalk.PropagateSeeds.Propagators;

import ij.ImagePlus;

/**
 * This class holds all possible RW parameters.
 * 
 * @author p.baniukiewicz
 * @see RandomWalkSegmentationPlugin_
 * @see RandomWalkView
 * @see RandomWalkParams
 */
public class RandomWalkModel {

  /**
   * Possible sources of seeds.
   * 
   * @author p.baniukiewicz
   *
   */
  public enum SeedSource {
    /**
     * Seed for IJ RGB image.
     */
    RGBImage,
    /**
     * Seed form RGB image created from UI.
     */
    CreatedImage,
    /**
     * Seed form binary mask image.
     */
    MaskImage,
    /**
     * Seed from binary mask image read from Qconf file.
     */
    QconfFile
  }

  /**
   * Parameters of Random Walk algorithm itself.
   * 
   * @see RandomWalkSegmentation
   */
  public RandomWalkParams params;

  /**
   * Get shrink methods supported by PropagateSeeds class in form of String[].
   * 
   * @return array of filters
   * @see PropagateSeeds
   * @see RandomWalkSegmentationPlugin_#runPlugin()
   */
  public String[] getShrinkMethods() {
    return Arrays.stream(Propagators.values()).map(Enum::name).toArray(String[]::new);
  }

  /**
   * Get filtering methods supported by BinaryFilters class in form of String[].
   * 
   * @return array of filters
   * @see BinaryFilters
   * @see RandomWalkSegmentation#run(java.util.Map)
   * @see RandomWalkSegmentation
   */
  public String[] getFilteringMethods() {
    return Arrays.stream(Filters.values()).map(Enum::name).toArray(String[]::new);
  }

  /**
   * Image to process.
   */
  public ImagePlus originalImage;
  /**
   * Selected seed source. Depending on value some of fields may be invalid.
   */
  public SeedSource seedSource;
  /**
   * Seed given by RGB image selected from IJ. Valid for all seed sources.
   */
  public ImagePlus seedImage;
  /**
   * Selected QCONF file. Will fill seedImage.
   */
  public String qconfFile;

  /**
   * Selected shrink algorithm.
   * 
   * @see PropagateSeeds
   */
  public Propagators selectedShrinkMethod;

  /**
   * Shrink method getter.
   * 
   * @return the selectedFilteringMethod number
   * @see PropagateSeeds#getPropagator(Propagators, boolean, ij.process.AutoThresholder.Method)
   * @see Propagators
   */
  public Propagators getselectedShrinkMethod() {
    return selectedShrinkMethod;
  }

  /**
   * Seed propagator setter.
   * 
   * @param selectedShrinkMethod the selectedFilteringMethod to set
   */
  public void setselectedShrinkMethod(Propagators selectedShrinkMethod) {
    this.selectedShrinkMethod = selectedShrinkMethod;
  }

  /**
   * Seed propagator setter.
   * 
   * @param selectedShrinkMethod index of shrink method respecting order returned by
   *        {@link RandomWalkModel#getShrinkMethods()}
   */
  public void setselectedShrinkMethod(int selectedShrinkMethod) {
    this.selectedShrinkMethod = Propagators.valueOf(getShrinkMethods()[selectedShrinkMethod]);
  }

  /**
   * ShrinkPower parameter.
   * 
   * <p>Number of erosions for generating next seed from previous one. Also number of pixels to
   * shrink contour.
   */
  public double shrinkPower;
  /**
   * ExpandPower parameter.
   * 
   * <p>Number of dilations for generating next seed from previous one. Also number of pixels to
   * expand contour.
   */
  public double expandPower;
  /**
   * Estimate background if true.
   * 
   * @see PropagateSeeds#getTrueBackground(ij.process.ImageProcessor, ij.process.ImageProcessor)
   * @see PropagateSeeds
   */
  public boolean estimateBackground;
  /**
   * Selected intermediate filtering algorithm.
   */
  private Filters selectedFilteringMethod;

  /**
   * Filtering getter.
   * 
   * @return the selectedFilteringMethod
   * @see BinaryFilters#getFilter(Filters)
   * @see Filters
   */
  public Filters getSelectedFilteringMethod() {
    return selectedFilteringMethod;
  }

  /**
   * Filtering setter. Creates instance of filter.
   * 
   * @param selectedFilteringMethod the selectedFilteringMethod to set
   */
  public void setSelectedFilteringMethod(Filters selectedFilteringMethod) {
    this.selectedFilteringMethod = selectedFilteringMethod;
    params.intermediateFilter = BinaryFilters.getFilter(selectedFilteringMethod);
  }

  /**
   * Post filtering setter. Creates instance of filter.
   * 
   * @param selectedFilteringMethod index of filter to set according to order returned by
   *        {@link #getFilteringMethods()}
   */
  public void setSelectedFilteringMethod(int selectedFilteringMethod) {
    this.selectedFilteringMethod = Filters.valueOf(getFilteringMethods()[selectedFilteringMethod]);
    params.intermediateFilter = BinaryFilters.getFilter(this.selectedFilteringMethod);
  }

  /**
   * true for HatFilter active.
   */
  public boolean hatFilter;
  /**
   * alev parameter. Valid for hatFilter==true.
   */
  public double alev;
  /**
   * num parameter. Valid for hatFilter==true.
   */
  public int num;
  /**
   * window parameter. Valid for hatFilter==true.
   */
  public int window;
  /**
   * Selected final binary filtering.
   */
  private Filters selectedFilteringPostMethod;

  /**
   * Post filtering getter.
   * 
   * @return the selectedFilteringPostMethod
   * @see BinaryFilters#getFilter(Filters)
   * @see Filters
   */
  public Filters getSelectedFilteringPostMethod() {
    return selectedFilteringPostMethod;
  }

  /**
   * Post filtering setter. Creates instance of filter.
   * 
   * @param selectedFilteringPostMethod the selectedFilteringPostMethod to set
   */
  public void setSelectedFilteringPostMethod(Filters selectedFilteringPostMethod) {
    this.selectedFilteringPostMethod = selectedFilteringPostMethod;
    params.finalFilter = BinaryFilters.getFilter(selectedFilteringPostMethod);
  }

  /**
   * Post filtering setter. Creates instance of filter.
   * 
   * @param selectedFilteringPostMethod index of filter to set according to order returned by
   *        {@link #getFilteringMethods()}
   */
  public void setSelectedFilteringPostMethod(int selectedFilteringPostMethod) {
    this.selectedFilteringPostMethod =
            Filters.valueOf(getFilteringMethods()[selectedFilteringPostMethod]);
    params.finalFilter = BinaryFilters.getFilter(this.selectedFilteringPostMethod);
  }

  /**
   * true for showing seeds.
   */
  public boolean showSeeds;
  /**
   * true for showing preview.
   */
  public boolean showPreview;

  /**
   * Default constructor setting default parameters.
   */
  public RandomWalkModel() {
    params = new RandomWalkParams();
    originalImage = null;
    seedSource = SeedSource.RGBImage;
    seedImage = null;
    qconfFile = null;
    selectedShrinkMethod = Propagators.NONE;
    shrinkPower = 10;
    expandPower = 15;
    estimateBackground = false;
    selectedFilteringMethod = Filters.NONE;
    hatFilter = false;
    alev = 0.9;
    num = 1;
    window = 15;
    selectedFilteringPostMethod = Filters.NONE;
    showSeeds = false;
    showPreview = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "RandomWalkModel [params=" + params + ", originalImage=" + originalImage
            + ", seedSource=" + seedSource + ", seedImage=" + seedImage + ", qconfFile=" + qconfFile
            + ", selectedShrinkMethod=" + selectedShrinkMethod + ", shrinkPower=" + shrinkPower
            + ", expandPower=" + expandPower + ", selectedFilteringMethod="
            + selectedFilteringMethod + ", hatFilter=" + hatFilter + ", alev=" + alev + ", num="
            + num + ", window=" + window + ", selectedFilteringPostMethod="
            + selectedFilteringPostMethod + ", showSeeds=" + showSeeds + ", showPreview="
            + showPreview + ", getShrinkMethods()=" + Arrays.toString(getShrinkMethods())
            + ", getFilteringMethods()=" + Arrays.toString(getFilteringMethods())
            + ", getselectedShrinkMethod()=" + getselectedShrinkMethod()
            + ", getSelectedFilteringMethod()=" + getSelectedFilteringMethod()
            + ", getSelectedFilteringPostMethod()=" + getSelectedFilteringPostMethod() + "]";
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
    long temp;
    temp = Double.doubleToLongBits(alev);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(expandPower);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (hatFilter ? 1231 : 1237);
    result = prime * result + num;
    result = prime * result + ((originalImage == null) ? 0 : originalImage.getTitle().hashCode());
    result = prime * result + ((params == null) ? 0 : params.hashCode());
    result = prime * result + ((qconfFile == null) ? 0 : qconfFile.hashCode());
    result = prime * result + ((seedImage == null) ? 0 : seedImage.getTitle().hashCode());
    result = prime * result + ((seedSource == null) ? 0 : seedSource.hashCode());
    result = prime * result
            + ((selectedFilteringMethod == null) ? 0 : selectedFilteringMethod.hashCode());
    result = prime * result
            + ((selectedFilteringPostMethod == null) ? 0 : selectedFilteringPostMethod.hashCode());
    result = prime * result
            + ((selectedShrinkMethod == null) ? 0 : selectedShrinkMethod.hashCode());
    result = prime * result + (showPreview ? 1231 : 1237);
    result = prime * result + (showSeeds ? 1231 : 1237);
    temp = Double.doubleToLongBits(shrinkPower);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + window;
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
    RandomWalkModel other = (RandomWalkModel) obj;
    if (Double.doubleToLongBits(alev) != Double.doubleToLongBits(other.alev)) {
      return false;
    }
    if (Double.doubleToLongBits(expandPower) != Double.doubleToLongBits(other.expandPower)) {
      return false;
    }
    if (hatFilter != other.hatFilter) {
      return false;
    }
    if (num != other.num) {
      return false;
    }
    if (originalImage == null) {
      if (other.originalImage != null) {
        return false;
      }
    } else if (!originalImage.getTitle().equals(other.originalImage.getTitle())) {
      return false;
    }
    if (params == null) {
      if (other.params != null) {
        return false;
      }
    } else if (!params.equals(other.params)) {
      return false;
    }
    if (qconfFile == null) {
      if (other.qconfFile != null) {
        return false;
      }
    } else if (!qconfFile.equals(other.qconfFile)) {
      return false;
    }
    if (seedImage == null) {
      if (other.seedImage != null) {
        return false;
      }
    } else if (!seedImage.getTitle().equals(other.seedImage.getTitle())) {
      return false;
    }
    if (seedSource != other.seedSource) {
      return false;
    }
    if (selectedFilteringMethod != other.selectedFilteringMethod) {
      return false;
    }
    if (selectedFilteringPostMethod != other.selectedFilteringPostMethod) {
      return false;
    }
    if (selectedShrinkMethod != other.selectedShrinkMethod) {
      return false;
    }
    if (showPreview != other.showPreview) {
      return false;
    }
    if (showSeeds != other.showSeeds) {
      return false;
    }
    if (Double.doubleToLongBits(shrinkPower) != Double.doubleToLongBits(other.shrinkPower)) {
      return false;
    }
    if (window != other.window) {
      return false;
    }
    return true;
  }

}

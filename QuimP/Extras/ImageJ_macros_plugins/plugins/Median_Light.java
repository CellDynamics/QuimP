/***
 * Image/J Plugins
 * Copyright (C) 2002 Jarek Sacha
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.Blitter;
import ij.process.ImageProcessor;

/**
 *  Half-median filter modyfying only light pixels.
 *
 * @author     Jarek Sacha
 * @created    March 29, 2002
 * @version    1.0
 */
public class Median_Light implements PlugInFilter {

  /**
   *  Overloaded method of PlugInFilter.
   *
   * @param  arg  Optional argument, not used by this plugin.
   * @param  imp  Optional argument, not used by this plugin.
   * @return      Flag word that specifies the filters capabilities.
   */
  public int setup(String arg, ImagePlus imp) {
    return PlugInFilter.DOES_ALL;
  }


  /**
   *  Main processing method for the Median_Light plugin. Overloaded method of
   *  PlugInFilter.
   *
   * @param  ip  Image processor to be filtered.
   */
  public void run(ImageProcessor ip) {
    ImageProcessor ip1 = ip.duplicate();
    ip1.medianFilter();
    ip.copyBits(ip1, 0, 0, Blitter.MIN);
  }
}
